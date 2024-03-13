package com.bignerdranch.android.photogallery.ui;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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
import com.bignerdranch.android.photogallery.thutils.SystemActions;

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

    private static final int CACHE_PRELOAD_CNT = 20;
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
        GalleryItem item = mGalleryItems.get(position);

        Result<Bitmap> cachedResultBitmap = mDownloader.downloadAsync(holder, item.getUrl());

        if (cachedResultBitmap != null)
            holder.bind(cachedResultBitmap, item.getPhotoWebPageUri());
        else
            holder.bind(R.drawable.baseline_hourglass_empty_24, item.getPhotoWebPageUri());

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
        private Uri mPhotoWebPageUri;

        public GalleryItemViewHolder(@NonNull View itemView) {
            super(itemView);

            mPhotoView = (ImageView) itemView;

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SystemActions.openWebPageInBrowser(mPhotoView.getContext(), mPhotoWebPageUri);
                }
            });
        }

        public void updateImage(Drawable image) {
            mPhotoView.setImageDrawable (image);
        }

        public void updateImage(@DrawableRes int imageRes) {
            Drawable image = ResourcesCompat.getDrawable(mRes, imageRes, null);
            updateImage(image);
        }

        public void bind(@DrawableRes int imageRes, Uri photoWebPageUri) {
            mPhotoWebPageUri = photoWebPageUri;
            updateImage(imageRes);
        }

        public void bind(Result<Bitmap> result, Uri photoWebPageUri) {
            mPhotoWebPageUri = photoWebPageUri;
            BitmapDrawable image = new BitmapDrawable(mRes, result.getContent());
            updateImage(image);
        }

        @Override
        public void onDownloadFinished(Result<Bitmap> result) {
            if (result.getErrorCode() == Result.ERR_OK) {
                BitmapDrawable image = new BitmapDrawable(mRes, result.getContent());
                updateImage(image);
            } else {
                Log.e("ViewHolder", "Error:" + result.getErrorCode() + " " + result.getErrorMessage());
                updateImage(R.drawable.baseline_highlight_off_24);
            }
        }
    }

}
