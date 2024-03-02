package com.bignerdranch.android.photogallery;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.bignerdranch.android.photogallery.data.GalleryItem;
import com.bignerdranch.android.photogallery.data.GalleryPage;
import com.bignerdranch.android.photogallery.data.Result;
import com.bignerdranch.android.photogallery.thutils.AppNotifications;
import com.bignerdranch.android.photogallery.thutils.AppPrefs;
import com.bignerdranch.android.photogallery.thutils.AssetsProperties;
import com.bignerdranch.android.photogallery.ui.GalleryRecyclerViewAdapter;
import com.bignerdranch.android.photogallery.ui.GalleryViewModel;
import com.bignerdranch.android.photogallery.workers.Constants;
import com.bignerdranch.android.photogallery.workers.ContentUpdateWorker;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final float PHOTO_REQUIRED_WIDTH = 200f;
    private boolean mIsLoading;
    private RecyclerView mRecyclerView;

    private ProgressBar mProgressLoading;

    private GalleryViewModel mViewModel;

    private GalleryRecyclerViewAdapter mRecyclerViewAdapter;
    private WorkManager mWorkManager;

    private String mApiKey;
    private boolean mContentUpdateWorkerStarted;

    private BroadcastReceiver mShowNotificationReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //HTTPSFetcher.Result r = HTTPSFetcher.getStringFromURL("https://www.onet.pl", StandardCharsets.UTF_8);
        mWorkManager = WorkManager.getInstance(getApplicationContext());

        setupViewModel();
        setupListView();
        mProgressLoading = findViewById(R.id.progressBarLoading);


        mIsLoading = mViewModel.loadPageAsync();
        Log.d(TAG, "loadPageAsync isLoadind: " + mIsLoading);
    }

    private void setupViewModel() {
        mViewModel = new ViewModelProvider(this).get(GalleryViewModel.class);

        mViewModel.setApiKey(mApiKey = AssetsProperties.getStringProp(this, "api_keys.properties", Constants.KEY_API));

        String queryText = AppPrefs.getStringPref(this, Constants.KEY_SEARCH_TEXT, null);
        mViewModel.setQueryText(queryText);

        mViewModel.getGalleryPages().observe(this, pages -> {
            int pagesCount = pages.size();

            if (pagesCount > 0) {
                Result<GalleryPage> lastPageResult = pages.get(pagesCount - 1);

                Log.d(TAG, "Loading result: " + lastPageResult.getErrorMessage());
                Log.d(TAG, "Loading content Id: " + lastPageResult.getContentId());

                if (!Objects.equals(lastPageResult.getContentId(), mViewModel.getQueryText())) {
                    mViewModel.reloadPagesAsync();
                    return;
                }

                if (lastPageResult.getErrorCode() == Result.ERR_OK) {
                    List<GalleryItem> galleryItemsList = new ArrayList<>();

                    for (int i = 0; i < pagesCount; i++) {
                        GalleryItem[] galleryItems = pages.get(i).getContent().getPhotos();

                        for (GalleryItem item : galleryItems) {
                            if (item.getUrl() == null || item.getUrl().trim().length() == 0)
                                continue;

                            galleryItemsList.add(item);
                        }
                    }

                    mRecyclerViewAdapter.update(galleryItemsList);

                    mProgressLoading.setVisibility(View.INVISIBLE);

                    if (mIsLoading && pagesCount == 1) {
                        GalleryItem[] photos = lastPageResult.getContent().getPhotos();

                        String id;
                        if (photos != null && photos.length > 0) {
                            id = photos[0].getId();
                        } else {
                            id = "";
                        }

                        AppPrefs.putStringPref(getApplicationContext(), Constants.KEY_LAST_ID, id);

                        //startContentUpdateWorker();

                        mIsLoading = false;
                    }

                } else {
                    AppNotifications.showToast(this, lastPageResult.getErrorMessage(), true);
                }

            }
        });

        mWorkManager.getWorkInfosForUniqueWorkLiveData(Constants.CONTENT_UPDATE_WORK).observe(this,
                workInfos -> {
                    boolean started = false;
                    if (workInfos != null && !workInfos.isEmpty()) {

                        WorkInfo workInfo = workInfos.get(0);
                        if (workInfo != null) {
                            started = !workInfo.getState().isFinished();
                        }
                    }

                    mContentUpdateWorkerStarted = started;

                });
    }

    private void setupListView() {
        mRecyclerView = findViewById(R.id.recycler_view_photos);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerViewAdapter = new GalleryRecyclerViewAdapter(getResources(), mViewModel.getUiHandler(), mViewModel.getBitmapLruCache());
        mRecyclerView.setAdapter(mRecyclerViewAdapter);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                //Log.d(TAG, "Scroll state: " + newState);

                if (newState == RecyclerView.SCROLL_STATE_IDLE && mRecyclerViewAdapter.isLastItemBinded()) {
                    Log.d(TAG, "Load new page !");

                    mViewModel.loadNextPageAsync();
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                //Log.d(TAG, String.format("Scroll delta: dx=%d, dy=%d", dx, dy));
            }
        });

        mRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            DisplayMetrics metrics = getResources().getDisplayMetrics();

            float widthDpi = mRecyclerView.getWidth() / metrics.density;

            int spanCount = (int) (widthDpi / PHOTO_REQUIRED_WIDTH) + 1;

            if (spanCount != layoutManager.getSpanCount()) {
                layoutManager.setSpanCount(spanCount);

                Log.d(TAG, String.format("New span count = %d", spanCount));
            }
        });
    }

    private void setupSearchViewMenuItem(@NonNull MenuItem menuItem) {
        SearchView searchView = (SearchView) Objects.requireNonNull(menuItem.getActionView());

        String queryText = mViewModel.getQueryText();

        if (queryText != null) {
            searchView.setIconified(false);
            searchView.setQuery(queryText, false);
            searchView.clearFocus();
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "SerchView onQueryTextSubmit: " + query);
                refreshGallery(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                Log.d(TAG, "SerchView onClose");
                refreshGallery(null);
                return false;
            }
        });
    }

    @Override
    protected void onStop() {
        registerShowNotificationReceiver(true);
        super.onStop();
    }

    @Override
    protected void onStart() {
        registerShowNotificationReceiver(false);
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mRecyclerViewAdapter.quit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        setupSearchViewMenuItem(menu.findItem(R.id.menuitem_search));

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem mi = menu.findItem(R.id.menuitem_notification);

        mi.setTitle(mContentUpdateWorkerStarted ? R.string.menu_stop_notify : R.string.menu_start_notify);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int menuItemId = item.getItemId();

        if (menuItemId == R.id.menuitem_notification) {
            toggleNotifications();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void toggleNotifications() {

        if (mContentUpdateWorkerStarted) {
            stopContentUpdateWorker();
        } else {
            startContentUpdateWorkerWithCheck();
        }

        invalidateOptionsMenu();

    }

    private void refreshGallery(@Nullable String queryText) {
        mRecyclerViewAdapter.clear();
        mViewModel.setQueryText(queryText);
        AppPrefs.putStringPref(this, Constants.KEY_SEARCH_TEXT, queryText);
        mIsLoading = true;
        mViewModel.reloadPagesAsync();

        mProgressLoading.setVisibility(View.VISIBLE);
    }

    private void startContentUpdateWorkerWithCheck() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (isPermissionGranted(Manifest.permission.POST_NOTIFICATIONS)) {
                startCUWorker();
            } else {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 0);
            }
        } else {
            if (AppNotifications.areNotificationsEnabled(this)) {
                startCUWorker();
            } else {
                AppNotifications.showSnackbarBasic(findViewById(R.id.recycler_view_photos), R.string.notification_disabled,  false);
            }
        }
    }

    private void startCUWorker() {
        Data inputData = ContentUpdateWorker.createWorkerInputData(mApiKey);

//        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(ContentUpdateWorker.class)
//                .setInputData(inputData)
//                .build();

        Constraints c = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(ContentUpdateWorker.class, 15, TimeUnit.MINUTES)
                .setInputData(inputData)
                //.setInitialDelay(30, TimeUnit.SECONDS)
                .setConstraints(c)
                .build();

        //mWorkManager.enqueueUniqueWork(Constants.CONTENT_UPDATE_WORK, ExistingWorkPolicy.KEEP, workRequest);
        mWorkManager.enqueueUniquePeriodicWork(Constants.CONTENT_UPDATE_WORK, ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, workRequest);

        AppNotifications.showToast(this, R.string.notify_started, false);
        mContentUpdateWorkerStarted = true;
    }

    private void stopContentUpdateWorker() {
        mWorkManager.cancelUniqueWork(Constants.CONTENT_UPDATE_WORK);

        AppNotifications.showToast(this, R.string.notify_stopped, false);
        mContentUpdateWorkerStarted = false;
    }

    private boolean isPermissionGranted(String permission) {
        return ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCUWorker();
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                AppNotifications.showSnackbarBasic(findViewById(R.id.recycler_view_photos), R.string.no_notification_permission,  false);
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private void registerShowNotificationReceiver(boolean unregister) {

        if (unregister && mShowNotificationReceiver != null)
            unregisterReceiver(mShowNotificationReceiver);
        else {
            mShowNotificationReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (Objects.equals(intent.getAction(), Constants.ACTION_SHOW_NOTIFICATION)) {
                        Log.d(TAG, "onReceive: ACTION_SHOW_NOTIFICATION");

                        setResultCode(Activity.RESULT_CANCELED);
                    }
                }
            };

            IntentFilter filter = new IntentFilter(Constants.ACTION_SHOW_NOTIFICATION);
            registerReceiver(mShowNotificationReceiver, filter, Constants.PERM_SHOW_NOTIFICATION_RECEIVE, null);
        }

    }

}