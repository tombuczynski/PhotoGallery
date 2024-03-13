package com.bignerdranch.android.photogallery.thutils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;

/**
 * Created by Tom Buczynski on 08.03.2024.
 */
public class SystemActions {

    public static boolean openWebPageInBrowser(@NonNull Context c, Uri webPage) {

        Intent intent = new Intent(Intent.ACTION_VIEW, webPage);

        if (intent.resolveActivity(c.getPackageManager()) != null) {
            c.startActivity(intent);

            return true;
        }

        return false;
    }
}
