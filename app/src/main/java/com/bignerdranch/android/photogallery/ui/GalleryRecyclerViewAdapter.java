package com.bignerdranch.android.photogallery.ui;

import android.annotation.SuppressLint;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bignerdranch.android.photogallery.data.GalleryItem;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by Tom Buczynski on 28.08.2023.
 */
public class GalleryRecyclerViewAdapter extends RecyclerView.Adapter<GalleryRecyclerViewAdapter.GalleryItemViewHolder> {

   private List<GalleryItem> mGalleryItems = new ArrayList<>();

   @NonNull
   @Override
   public GalleryItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      TextView textView = new TextView(parent.getContext());

      return new GalleryItemViewHolder(textView);
   }

   @Override
   public void onBindViewHolder(@NonNull GalleryItemViewHolder holder, int position) {
      holder.bind(mGalleryItems.get(position), position);
   }

   @Override
   public int getItemCount() {
      return mGalleryItems.size();
   }

   public void update(List<GalleryItem> galleryItems) {
      mGalleryItems = galleryItems;

      notifyDataSetChanged();
   }

   public static class GalleryItemViewHolder extends RecyclerView.ViewHolder {

      private final TextView mTextView;

      public GalleryItemViewHolder(@NonNull View itemView) {
         super(itemView);

         mTextView = (TextView)itemView;
      }

      public void bind(@NonNull GalleryItem item, int position) {
         mTextView.setText("" + (position+1) +" " + item.toString());
      }
   }

}
