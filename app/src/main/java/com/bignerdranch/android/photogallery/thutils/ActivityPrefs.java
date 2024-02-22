package com.bignerdranch.android.photogallery.thutils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

/**
 * Created by Tom Buczynski on 21.10.2019.
 */
public class ActivityPrefs {
    public static String getStringPref(@NonNull Activity activity, String key, String defValue) {
        SharedPreferences preferences = activity.getPreferences(Context.MODE_PRIVATE);

        return preferences.getString(key, defValue);
    }

    public static void putStringPref(@NonNull Activity activity, String key, String value) {
        SharedPreferences preferences = activity.getPreferences(Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }
}
