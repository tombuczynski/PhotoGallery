package com.bignerdranch.android.photogallery.data;

import android.net.Uri;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.nio.charset.StandardCharsets;

/**
 * Created by Tom Buczynski on 27.08.2023.
 */
public class FlickrFetcher extends JSONFetcher {

   public static final int ERR_FLICKR = 2000;

   public static Result<GalleryPage> fetchRecentGalleryItems(String apiKey, Integer page) {
      String url = createRESTRequest(apiKey, "flickr.photos.getRecent", "page", page.toString());

      //Result<JSONObject> r = fetchJSONFromURL(url, StandardCharsets.UTF_8);

      Result<String> r = fetchStringFromURL(url, StandardCharsets.UTF_8);

      if (r.getErrorCode() != ERR_OK)
         return new Result<>(null, r.getErrorCode(), r.getErrorMessage());

      try {

         Gson gson = new Gson();
         FlickrPhotoGallery photos = gson.fromJson(r.getContent(), FlickrPhotoGallery.class);

         if ("ok".equalsIgnoreCase(photos.getStat())){
            return new Result<>(photos.getPage(), r.getErrorCode(), r.getErrorMessage());
         } else if ("fail".equalsIgnoreCase(photos.getStat())){
            return new Result<>(null, photos.getCode() + ERR_FLICKR, photos.getMessage());
         } else {
            return new Result<>(null, ERR_FLICKR, "");
         }

      } catch (JsonSyntaxException e) {
         return new Result<>(null, ERR_JSON, e.getLocalizedMessage());
      }
   }

   private static String createRESTRequest(String apiKey, String method, String... params) {

      if ((params.length % 2) != 0)
         throw new IllegalArgumentException("params");

      Uri.Builder uriBuilder = Uri.parse("https://www.flickr.com/services/rest/")
              .buildUpon()
              .appendQueryParameter("method", method)
              .appendQueryParameter("extras", "url_s")
              .appendQueryParameter("format", "json")
              .appendQueryParameter("nojsoncallback", "1")
              .appendQueryParameter("api_key", apiKey);

      for (int i = 0; i < params.length; i += 2) {
         String name = params[i];
         String value = params[i + 1];

         uriBuilder.appendQueryParameter(name, value);
      }

      return uriBuilder.build().toString();

      //"https://www.flickr.com/services/rest/?method=flickr.photos.getRecent&api_key=8de810430d28d6e9d948125bbea00f09&extras=url_s&format=json&nojsoncallback=1"
   }

}
