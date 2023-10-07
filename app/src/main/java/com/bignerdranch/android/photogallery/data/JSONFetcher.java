package com.bignerdranch.android.photogallery.data;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;

import androidx.annotation.NonNull;

/**
 * Created by Tom Buczynski on 26.08.2023.
 */
public class JSONFetcher extends HTTPSFetcher {
    public static final int ERR_JSON = 4;

    public static Result<JSONObject> fetchJSONFromURL(@NonNull String url, Charset charset) {
        Result<String> r = fetchStringFromURL(url, charset);

        if (r.getErrorCode() != Result.ERR_OK)
            return new Result<>(null, r.getErrorCode(), r.getErrorMessage());

        try {
            JSONObject json = new JSONObject(r.getContent());

            return new Result<>(json, r.getErrorCode(), r.getErrorMessage());
        } catch (JSONException e) {
            return new Result<>(null, ERR_JSON, e.toString());
        }
    }
}
