package com.bignerdranch.android.photogallery.data;

import android.net.Uri;

import androidx.annotation.NonNull;

/**
 * Created by Tom Buczynski on 27.08.2023.
 */
public class GalleryItem {
   private String id;
   private String title;
   private String url_s;
   private String owner;

   public GalleryItem(String id, String title, String url, String owner) {
      this.id = id;
      this.title = title;
      url_s = url;
      this.owner = owner;
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

   public String getOwner() {
      return owner;
   }

   public Uri getPhotoWebPageUri() {
      return Uri.parse("https://www.flickr.com/photos")
              .buildUpon()
              .appendPath(owner)
              .appendPath(id)
              .build();
   }

   @NonNull
   @Override
   public String toString() {
      return id + ": " + title;
   }
}
