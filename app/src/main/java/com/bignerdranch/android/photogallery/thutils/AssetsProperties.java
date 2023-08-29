package com.bignerdranch.android.photogallery.thutils;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Tom Buczynski on 11.11.2019.
 */
public class AssetsProperties {
    public static String getStringProp(Context context, String propFileName, String key) {
        AssetManager assetMan = context.getResources().getAssets();
        Properties p = new Properties();

        try {
            InputStream is = assetMan.open(propFileName);
            try {
                p.load(is);
            } catch (IllegalArgumentException e) {
                is.close();
                return null;
            }
            is.close();
        } catch (IOException e) {
            return null;
        }

        return p.getProperty(key);
    }
}
