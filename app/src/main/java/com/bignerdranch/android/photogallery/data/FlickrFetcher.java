package com.bignerdranch.android.photogallery.data;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tom Buczynski on 27.08.2023.
 */
public class FlickrFetcher extends JSONFetcher {

   public static Result<List<GalleryItem>> fetchRecentGalleryItems(String apiKey) {
      String url = createRESTRequest(apiKey, "flickr.photos.getRecent");

      Result<JSONObject> r = fetchJSONFromURL(url, StandardCharsets.UTF_8);

      if (r.getErrorCode() != ERR_OK)
         return new Result<>(null, r.getErrorCode(), r.getErrorMessage());

      try {
         ArrayList<GalleryItem> itemList = new ArrayList<>();

         JSONObject photos = r.getContent().getJSONObject("photos");
         JSONArray photoArray = photos.getJSONArray("photo");

         for (int i = 0; i < photoArray.length(); i++) {
            JSONObject photo = photoArray.getJSONObject(i);

            if (! photo.has("url_s"))
               continue;

            String id = photo.getString("id");
            String title = photo.getString("title");
            String url_s = photo.getString("url_s");
            GalleryItem item = new GalleryItem(id, title, url_s);

            itemList.add(item);
         }

         return new Result<>(itemList, r.getErrorCode(), r.getErrorMessage());
      } catch (JSONException e) {
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
