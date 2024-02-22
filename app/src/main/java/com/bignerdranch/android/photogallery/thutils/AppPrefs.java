package com.bignerdranch.android.photogallery.thutils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.prefs.Preferences;

import androidx.annotation.NonNull;

/**
 * Created by Tom Buczynski on 21.10.2019.
 */
public class AppPrefs {
    private static final String PREFERENCE_FILE_NAME = "appsettings";

    public static String getStringPref(@NonNull Context context, String key, String defValue) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);

        return preferences.getString(key, defValue);
    }

    public static void putStringPref(@NonNull Context context, String key, String value) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }
}
