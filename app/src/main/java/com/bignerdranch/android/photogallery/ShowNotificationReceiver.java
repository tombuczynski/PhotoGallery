package com.bignerdranch.android.photogallery;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.bignerdranch.android.photogallery.thutils.AppNotifications;
import com.bignerdranch.android.photogallery.workers.Constants;

import java.util.Objects;

public class ShowNotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "ShowNotificationReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {

        if (Objects.equals(intent.getAction(), Constants.ACTION_SHOW_NOTIFICATION)) {
            Log.i(TAG, "onReceive: ACTION_SHOW_NOTIFICATION");

            if (getResultCode() != Activity.RESULT_OK) {
                Log.w(TAG, "Show Notification action canceled");

                return;
            }

            String searchText = intent.getStringExtra(Constants.KEY_SEARCH_TEXT);
            if (searchText != null)
                showNotification(context, searchText);
        }
    }

    private static void showNotification(Context c, String searchText) {

        AppNotifications notifications = new AppNotifications(Constants.NOTIFY_CHANNEL_ID, c.getString(R.string.notify_channel_name), c.getString(R.string.notify_channel_description));

        Intent tapIntent = new Intent(c, MainActivity.class);
        tapIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

        notifications.notificationBasic(c,
                c.getString(R.string.notification_title), c.getString(R.string.notification_description, searchText),
                1, tapIntent);
    }

}