package com.bignerdranch.android.photogallery.ui;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bignerdranch.android.photogallery.R;
import com.bignerdranch.android.photogallery.data.ImageDownloader;
import com.bignerdranch.android.photogallery.data.GalleryItem;
import com.bignerdranch.android.photogallery.data.Result;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by Tom Buczynski on 28.08.2023.
 */
public class GalleryRecyclerViewAdapter extends RecyclerView.Adapter<GalleryRecyclerViewAdapter.GalleryItemViewHolder> {

    private static final int CACHE_PRELOAD_CNT = 10;
    private List<GalleryItem> mGalleryItems = new ArrayList<>();

    private boolean mIsLastItemBinded = false;

    private final ImageDownloader<GalleryItemViewHolder> mDownloader;
    private final Resources mRes;

    public GalleryRecyclerViewAdapter(@NonNull Resources res, @NonNull Handler uiHandler, @NonNull LruCache<String, Result<Bitmap>> bitmapLruCache) {
        mRes = res;
        mDownloader = new ImageDownloader<>(uiHandler, bitmapLruCache);
        mDownloader.start();
    }

    @NonNull
    @Override
    public GalleryItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View photoView = LayoutInflater.from(parent.getContext()).inflate(R.layout.gallery_item, parent,false);

        return new GalleryItemViewHolder(photoView);
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryItemViewHolder holder, int position) {
        //holder.bind(mGalleryItems.get(position), position);

        Result<Bitmap> cachedResultBitmap = mDownloader.downloadAsync(holder, mGalleryItems.get(position).getUrl());

        if (cachedResultBitmap != null)
            holder.onDownloadFinished(cachedResultBitmap);
        else
            holder.bind(R.drawable.baseline_hourglass_empty_24);

        int p = position;
        int cnt = CACHE_PRELOAD_CNT;
        while (++p < getItemCount() && cnt-- > 0) {
            mDownloader.preloadAsync(mGalleryItems.get(p).getUrl());
        }
        p = position;
        cnt = CACHE_PRELOAD_CNT;
        while (--p >= 0 && cnt-- > 0) {
            mDownloader.preloadAsync(mGalleryItems.get(p).getUrl());
        }


        if (position == getItemCount() - 1)
            mIsLastItemBinded = true;
    }

    @Override
    public int getItemCount() {
        return mGalleryItems.size();
    }

    public void update(List<GalleryItem> galleryItems) {
        int oldCount = mGalleryItems.size();
        int newCount = galleryItems.size();

        mGalleryItems = galleryItems;

        if (newCount > oldCount) {
            notifyItemRangeInserted(oldCount, newCount - oldCount);
        } else if (newCount < oldCount) {
            notifyItemRangeRemoved(newCount, oldCount - newCount);
        }

        mIsLastItemBinded = false;
    }

    public void clear() {
        int oldCount = mGalleryItems.size();

        mGalleryItems.clear();

        notifyItemRangeRemoved(0, oldCount);

        mIsLastItemBinded = false;
    }

    public boolean isLastItemBinded() {
        if (mIsLastItemBinded) {
            mIsLastItemBinded = false;

            return true;
        }

        return false;
    }

    public void quit() {
        mDownloader.quit();
    }

    public class GalleryItemViewHolder extends RecyclerView.ViewHolder implements ImageDownloader.Callback {

        private final ImageView mPhotoView;

        public GalleryItemViewHolder(@NonNull View itemView) {
            super(itemView);

            mPhotoView = (ImageView) itemView;
        }

        public void bind(Drawable image) {
            mPhotoView.setImageDrawable (image);
        }

        public void bind(@DrawableRes int imageRes) {
            Drawable image = ResourcesCompat.getDrawable(mRes, imageRes, null);
            bind(image);
        }

        @Override
        public void onDownloadFinished(Result<Bitmap> result) {
            if (result.getErrorCode() == Result.ERR_OK) {
                BitmapDrawable image = new BitmapDrawable(mRes, result.getContent());
                bind(image);
            } else {
                Log.e("ViewHolder", "Error:" + result.getErrorCode() + " " + result.getErrorMessage());
                bind(R.drawable.baseline_highlight_off_24);
            }
        }
    }

}
