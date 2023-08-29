package com.bignerdranch.android.photogallery.data;

import androidx.annotation.NonNull;

/**
 * Created by Tom Buczynski on 27.08.2023.
 */
public class GalleryItem {
   private final String mId;
   private final String mTitle;
   private final String mUrl;

   public GalleryItem(String id, String title, String url) {
      mId = id;
      mTitle = title;
      mUrl = url;
   }

   public String getId() {
      return mId;
   }

   public String getTitle() {
      return mTitle;
   }

   public String getUrl() {
      return mUrl;
   }

   @NonNull
   @Override
   public String toString() {
      return mId + ": " + mTitle;
   }
}
