package com.bignerdranch.android.photogallery.data;

/**
 * Created by Tom Buczynski on 30.08.2023.
 */
public class GalleryPage {
   private int page;
   private int pages;
   private int perpage;
   private int total;
   private GalleryItem[] photo;

   public int getPage() {
      return page;
   }

   public int getPages() {
      return pages;
   }

   public int getPerpage() {
      return perpage;
   }

   public int getTotal() {
      return total;
   }

   public GalleryItem[] getPhotos() {
      return photo;
   }
}
