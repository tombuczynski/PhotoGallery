package com.bignerdranch.android.photogallery.data;

import androidx.annotation.NonNull;

/**
 * Created by Tom Buczynski on 27.08.2023.
 */
public class GalleryItem {
   private String id;
   private String title;
   private String url_s;

   public GalleryItem(String id, String title, String url) {
      this.id = id;
      this.title = title;
      url_s = url;
   }

   public GalleryItem() {
   }

   public String getId() {
      return id;
   }

   public String getTitle() {
      return title;
   }

   public String getUrl() {
      return url_s;
   }

   @NonNull
   @Override
   public String toString() {
      return id + ": " + title;
   }
}
