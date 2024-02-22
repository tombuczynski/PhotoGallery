package com.bignerdranch.android.photogallery.thutils;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.View;
import android.widget.Toast;

import com.bignerdranch.android.photogallery.R;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

/**
 * Created by Tom Buczynski on 18.02.2024.
 */
public class AppNotifications {

    private final String mChannelId;
    private final String mChannelName;
    private final String mChannelDescription;
    private boolean mChannelCreated;

    public AppNotifications(String channelId, String channelName, String channelDescription) {
        mChannelId = channelId;
        mChannelName = channelName;
        mChannelDescription = channelDescription;
        mChannelCreated = false;
    }

    public static void showToast(Context c, @NonNull String text, boolean showLong) {
        Toast.makeText(c, text, showLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
    }

    public static void showToast(Context c, @StringRes int textRes, boolean showLong) {
        showToast(c, c.getString(textRes), showLong);
    }

    public static void showSnackbarBasic(View v, @NonNull String text, boolean showLong) {
        Snackbar.make(v, text, showLong ? BaseTransientBottomBar.LENGTH_LONG : BaseTransientBottomBar.LENGTH_SHORT).show();
    }

    public static void showSnackbarBasic(View v, @StringRes int textRes, boolean showLong) {
        Snackbar.make(v, textRes, showLong ? BaseTransientBottomBar.LENGTH_LONG : BaseTransientBottomBar.LENGTH_SHORT).show();
    }

    public void notificationBasic(Context c, String title, String text, int notifyId, @Nullable Intent tapIntent) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(c);

        if (! mChannelCreated && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(mChannelId, mChannelName, importance);
            channel.setDescription(mChannelDescription);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this.
            notificationManager.createNotificationChannel(channel);

            mChannelCreated = true;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(c, mChannelId)
                .setSmallIcon(R.drawable.ic_app_notify)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        if (tapIntent != null) {
            PendingIntent pendingIntent = PendingIntent.getActivity(c, 0, tapIntent, PendingIntent.FLAG_IMMUTABLE);
            builder.setContentIntent(pendingIntent);
            builder.setAutoCancel(true);
        }

        if (ActivityCompat.checkSelfPermission(c, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(notifyId, builder.build());
    }

    public static boolean areNotificationsEnabled(Context c) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(c);

        return notificationManager.areNotificationsEnabled();
    }
}
