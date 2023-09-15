package com.bignerdranch.android.photogallery.ui;

import com.bignerdranch.android.photogallery.data.FlickrFetcher;
import com.bignerdranch.android.photogallery.data.GalleryPage;
import com.bignerdranch.android.photogallery.data.Result;

import java.util.ArrayList;
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

    private MutableLiveData<List<Result<GalleryPage>>> mGalleryPages;
    private String mApiKey = "";

    private Integer mRequestedPage = null;

    public Integer getRequestedPage() {
        return mRequestedPage;
    }

    public void setRequestedPage(Integer requestedPage) {
        mRequestedPage = requestedPage;
    }


    public LiveData<List<Result<GalleryPage>>> getGalleryPages() {
        if (mGalleryPages == null)
            mGalleryPages = new MutableLiveData<>(new ArrayList<>());

        return mGalleryPages;
    }

    public void setApiKey(String apiKey) {
        mApiKey = apiKey;
    }

    protected Executor getExecutor() {
        if (mExecutor== null)
            mExecutor = Executors.newSingleThreadExecutor();

        return mExecutor;
    }

    public boolean loadPageAsync()
    {
        List<Result<GalleryPage>> galleryPages = getGalleryPages().getValue();
        assert galleryPages != null;

        if (mRequestedPage != null && mRequestedPage == galleryPages.size() && galleryPages.get(mRequestedPage - 1).getErrorCode() == FlickrFetcher.ERR_OK) {
            return false;
        }

        if (mRequestedPage == null) {
            mRequestedPage = 1;
        } else if (mRequestedPage > galleryPages.size()){
            return true;
        } else {
            galleryPages.remove(mRequestedPage - 1);
        }

        executeFetchPage(galleryPages);

        return true;
    }

    private void executeFetchPage(List<Result<GalleryPage>> galleryPages) {

        getExecutor().execute(() -> {
            Result<GalleryPage> result = FlickrFetcher.fetchRecentGalleryItems(mApiKey, mRequestedPage);

            List<Result<GalleryPage>> updatedGalleryPages = new ArrayList<>(galleryPages);
            updatedGalleryPages.add(result);

            mGalleryPages.postValue(updatedGalleryPages);
        });
    }

    public void loadNextPageAsync() {
        if (! loadPageAsync()) {
            List<Result<GalleryPage>> galleryPages = getGalleryPages().getValue();
            assert galleryPages != null;

            if (galleryPages.get(mRequestedPage - 1).getContent().getPages() > mRequestedPage) {
                mRequestedPage++;
                executeFetchPage(galleryPages);
            }
        }
    }
}
