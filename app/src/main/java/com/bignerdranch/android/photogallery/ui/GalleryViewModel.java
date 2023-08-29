package com.bignerdranch.android.photogallery.ui;

import com.bignerdranch.android.photogallery.data.FlickrFetcher;
import com.bignerdranch.android.photogallery.data.GalleryItem;
import com.bignerdranch.android.photogallery.data.HTTPSFetcher;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * Created by Tom Buczynski on 25.08.2023.
 */
public class GalleryViewModel extends ViewModel {
    private Executor mExecutor = null;

    private MutableLiveData<HTTPSFetcher.Result<List<GalleryItem>>> mResult;
    private String mApiKey = "";


    public LiveData<HTTPSFetcher.Result<List<GalleryItem>>> getResult() {
        if (mResult == null)
            mResult = new MutableLiveData<>();

        return mResult;
    }

    public void setApiKey(String apiKey) {
        mApiKey = apiKey;
    }

    protected Executor getExecutor() {
        if (mExecutor== null)
            mExecutor = Executors.newSingleThreadExecutor();

        return mExecutor;
    }

    public void loadContentAsync()
    {
        getResult();
        getExecutor().execute(() -> mResult.postValue(FlickrFetcher.fetchRecentGalleryItems(mApiKey)));
    }

}
