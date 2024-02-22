package com.bignerdranch.android.photogallery.workers;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.bignerdranch.android.photogallery.MainActivity;
import com.bignerdranch.android.photogallery.R;
import com.bignerdranch.android.photogallery.data.FlickrFetcher;
import com.bignerdranch.android.photogallery.data.GalleryItem;
import com.bignerdranch.android.photogallery.data.GalleryPage;
import com.bignerdranch.android.photogallery.data.Result;
import com.bignerdranch.android.photogallery.thutils.AppNotifications;
import com.bignerdranch.android.photogallery.thutils.AppPrefs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

/**
 * Created by Tom Buczynski on 07.02.2024.
 */
public class ContentUpdateWorker extends Worker {
    private static final String TAG = "ContentUpdateWorker";
    public ContentUpdateWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String searchText = AppPrefs.getStringPref(getApplicationContext(), Constants.KEY_SEARCH_TEXT, null);

        String apiKey = getInputData().getString(Constants.KEY_API);

        com.bignerdranch.android.photogallery.data.Result<GalleryPage> result;

        if (searchText == null) {
            result = FlickrFetcher.getRecentPhotos(apiKey, 1);
        } else {
            result = FlickrFetcher.searchPhotos(apiKey, 1, searchText);
        }

        if (result.getErrorCode() != com.bignerdranch.android.photogallery.data.Result.ERR_OK)
            return Result.retry();

        GalleryItem[] photos =  result.getContent().getPhotos();

        String id;
        if (photos != null && photos.length > 0) {
            id = photos[0].getId();
        } else {
            id = "";
        }

        String lastId = AppPrefs.getStringPref(getApplicationContext(), Constants.KEY_LAST_ID, null);

        if (! id.equals(lastId)) {
            if (id.isEmpty())
                Log.w(TAG, "No photos found, searchText: " + searchText);
            else {
                Log.i(TAG, "New photo ID: " + id + ", searchText: " + searchText);

                if (searchText != null)
                    showNotification(searchText);
            }

            AppPrefs.putStringPref(getApplicationContext(), Constants.KEY_LAST_ID, id);
        } else {
            if (lastId.isEmpty())
                Log.w(TAG, "Still no photos, searchText: " + searchText);
            else
                Log.i(TAG, "Old photo ID: " + lastId + ", searchText: " + searchText);
        }

        return Result.success();
    }


    @NonNull
    public static Data createWorkerInputData(@NonNull String apiKey) {
        Data.Builder dataBuild = new Data.Builder();

        dataBuild.putString(Constants.KEY_API, apiKey);

        return dataBuild.build();
    }

    private void showNotification(String searchText) {
        Context c = getApplicationContext();

        AppNotifications notifications = new AppNotifications(Constants.NOTIFY_CHANNEL_ID, c.getString(R.string.notify_channel_name), c.getString(R.string.notify_channel_description));

        Intent tapIntent = new Intent(c, MainActivity.class);
        tapIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

        notifications.notificationBasic(this.getApplicationContext(),
                c.getString(R.string.notification_title), c.getString(R.string.notification_description, searchText),
                1, tapIntent);
    }

}


