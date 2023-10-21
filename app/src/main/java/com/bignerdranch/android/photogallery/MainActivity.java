package com.bignerdranch.android.photogallery;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bignerdranch.android.photogallery.data.GalleryItem;
import com.bignerdranch.android.photogallery.data.GalleryPage;
import com.bignerdranch.android.photogallery.data.Result;
import com.bignerdranch.android.photogallery.thutils.AssetsProperties;
import com.bignerdranch.android.photogallery.ui.GalleryRecyclerViewAdapter;
import com.bignerdranch.android.photogallery.ui.GalleryViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final float PHOTO_REQUIRED_WIDTH = 200f;
    private RecyclerView mRecyclerView;

    private ProgressBar mProgressLoading;

    private GalleryViewModel mViewModel;

    private GalleryRecyclerViewAdapter mRecyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //HTTPSFetcher.Result r = HTTPSFetcher.getStringFromURL("https://www.onet.pl", StandardCharsets.UTF_8);

        setupViewModel();
        setupListView();
        mProgressLoading = findViewById(R.id.progressBarLoading);

        boolean isLoadind =  mViewModel.loadPageAsync();
        Log.d(TAG, "loadPageAsync isLoadind: " + isLoadind);
    }

    private void setupViewModel() {
        mViewModel = new ViewModelProvider(this).get(GalleryViewModel.class);

        mViewModel.setApiKey(AssetsProperties.getStringProp(this, "api_keys.properties", "flickr_key"));


        mViewModel.getGalleryPages().observe(this, pages -> {
            int pagesCount = pages.size();

            if (pagesCount > 0) {
                Result<GalleryPage> lastPageResult = pages.get(pagesCount - 1);

                Log.d(TAG, "Loading result: " + lastPageResult.getErrorMessage());
                Log.d(TAG, "Loading content Id: " + lastPageResult.getContentId());

                if (! Objects.equals(lastPageResult.getContentId(), mViewModel.getQueryText())) {
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
                } else {
                    Toast.makeText(this, lastPageResult.getErrorMessage(), Toast.LENGTH_LONG).show();
                }
            }
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

            int spanCount = (int)(widthDpi / PHOTO_REQUIRED_WIDTH) + 1;

            if (spanCount != layoutManager.getSpanCount()) {
                layoutManager.setSpanCount(spanCount);

                Log.d(TAG, String.format("New span count = %d", spanCount));
            }
        });
    }

    private void setupSearchViewMenuItem(MenuItem menuItem) {
        SearchView searchView = (SearchView) Objects.requireNonNull(menuItem.getActionView());

        String query = mViewModel.getQueryText();

        if (query != null ) {
            searchView.setIconified(false);
            searchView.setQuery(query, false);
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void refreshGallery(@Nullable String queryText) {
        mRecyclerViewAdapter.clear();
        mViewModel.setQueryText(queryText);
        mViewModel.reloadPagesAsync();

        mProgressLoading.setVisibility(View.VISIBLE);
    }
}