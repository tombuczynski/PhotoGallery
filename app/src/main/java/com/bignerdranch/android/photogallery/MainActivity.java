package com.bignerdranch.android.photogallery;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.bignerdranch.android.photogallery.data.HTTPSFetcher;
import com.bignerdranch.android.photogallery.thutils.AssetsProperties;
import com.bignerdranch.android.photogallery.ui.GalleryRecyclerViewAdapter;
import com.bignerdranch.android.photogallery.ui.GalleryViewModel;

import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private GalleryViewModel mViewModel;

    private GalleryRecyclerViewAdapter mRecyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //HTTPSFetcher.Result r = HTTPSFetcher.getStringFromURL("https://www.onet.pl", StandardCharsets.UTF_8);

        mViewModel = new ViewModelProvider(this).get(GalleryViewModel.class);

        mViewModel.setApiKey(AssetsProperties.getStringProp(this, "api_keys.properties", "flickr_key"));

        RecyclerView recyclerView = findViewById(R.id.recycler_view_photos);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mRecyclerViewAdapter = new GalleryRecyclerViewAdapter();
        recyclerView.setAdapter(mRecyclerViewAdapter);

        mViewModel.getResult().observe(this, result -> {
            Log.d(TAG, result.getErrorMessage());

            mRecyclerViewAdapter.update(result.getContent());
        });

        mViewModel.loadContentAsync();
    }
}