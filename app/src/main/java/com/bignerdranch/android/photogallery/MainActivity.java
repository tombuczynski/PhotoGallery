package com.bignerdranch.android.photogallery;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewTreeObserver;

import com.bignerdranch.android.photogallery.data.FlickrFetcher;
import com.bignerdranch.android.photogallery.data.GalleryItem;
import com.bignerdranch.android.photogallery.data.GalleryPage;
import com.bignerdranch.android.photogallery.data.Result;
import com.bignerdranch.android.photogallery.thutils.AssetsProperties;
import com.bignerdranch.android.photogallery.ui.GalleryRecyclerViewAdapter;
import com.bignerdranch.android.photogallery.ui.GalleryViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public static final float PHOTO_REQUIRED_WIDTH = 150f;

    private GalleryViewModel mViewModel;

    private GalleryRecyclerViewAdapter mRecyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //HTTPSFetcher.Result r = HTTPSFetcher.getStringFromURL("https://www.onet.pl", StandardCharsets.UTF_8);

        setupViewModel();
        setupListView();

        boolean isLoadind =  mViewModel.loadPageAsync();
        Log.d(TAG, "loadContentAsync isLoadind: " + isLoadind);
    }

    private void setupViewModel() {
        mViewModel = new ViewModelProvider(this).get(GalleryViewModel.class);

        mViewModel.setApiKey(AssetsProperties.getStringProp(this, "api_keys.properties", "flickr_key"));


        mViewModel.getGalleryPages().observe(this, pages -> {
            int pagesCount = pages.size();

            if (pagesCount > 0) {
                Result<GalleryPage> lastPageResult = pages.get(pagesCount - 1);

                Log.d(TAG, "Loading result: " + lastPageResult.getErrorMessage());

                if (lastPageResult.getErrorCode() == FlickrFetcher.ERR_OK) {
                    List<GalleryItem> galleryItems = new ArrayList<>();

                    for (int i = 0; i < pagesCount; i++) {
                        GalleryPage page = pages.get(i).getContent();

                        galleryItems.addAll(Arrays.asList(page.getPhotos()));
                    }

                    mRecyclerViewAdapter.update(galleryItems, false);
                }
            }
        });
    }

    private void setupListView() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view_photos);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(layoutManager);
        mRecyclerViewAdapter = new GalleryRecyclerViewAdapter();
        recyclerView.setAdapter(mRecyclerViewAdapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            DisplayMetrics metrics = getResources().getDisplayMetrics();

            float widthDpi = recyclerView.getWidth() / metrics.density;

            int spanCount = (int)(widthDpi / PHOTO_REQUIRED_WIDTH) + 1;

            if (spanCount != layoutManager.getSpanCount()) {
                layoutManager.setSpanCount(spanCount);

                Log.d(TAG, String.format("New span count = %d", spanCount));
            }
        });
    }
}