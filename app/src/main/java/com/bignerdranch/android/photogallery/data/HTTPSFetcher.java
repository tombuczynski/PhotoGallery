package com.bignerdranch.android.photogallery.data;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HttpsURLConnection;

import androidx.annotation.NonNull;

/**
 * Created by Tom Buczynski on 19.07.2023.
 */
public class HTTPSFetcher {
    public static final int ERR_OK = 0;
    public static final int ERR_HTTPS_URL_REQUIRED = 1;
    public static final int ERR_IO = 2;
    public static final int ERR_CANCELED = 3;

    public static class Result<T> {
        private final int mErrorCode;
        private final String mErrorMessage;
        private final T mContent;

        public Result(T content, int errorCode, String errorMessage) {
            mErrorCode = errorCode;
            mErrorMessage = errorMessage;
            mContent = content;
        }

        public int getErrorCode() {
            return mErrorCode;
        }

        public String getErrorMessage() {
            return mErrorMessage;
        }

        public T getContent() {
            return mContent;
        }
    }
    public static Result<byte[]> fetchBytesFromURL(@NonNull String url) {
        HttpsURLConnection urlConnection = null;
        InputStream inputStream = null;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            urlConnection = (HttpsURLConnection) (new URL(url)).openConnection();
            urlConnection.setDoInput(true);
            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(5000);
            urlConnection.setReadTimeout(2000);
            urlConnection.setUseCaches(false);
            urlConnection.connect();

            inputStream = urlConnection.getInputStream();

            int responseCode = urlConnection.getResponseCode();
            if (responseCode != HttpsURLConnection.HTTP_OK) {
                return new Result<>(null, responseCode, urlConnection.getResponseMessage());
            }

            byte[] buf = new byte[1024];
            int length;
            while ((length = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, length);
            }

            return new Result<>(outputStream.toByteArray(), ERR_OK, "OK");

        } catch (IOException e) {
            return new Result<>(null, ERR_IO, e.getLocalizedMessage());
        } catch (ClassCastException e) {
            return new Result<>(null, ERR_HTTPS_URL_REQUIRED, e.getLocalizedMessage());
        } finally {
            if (urlConnection != null) {

                try {
                    outputStream.close();
                    if (inputStream != null) {
                            inputStream.close();
                    }
                } catch (IOException ignored) {

                }

                urlConnection.disconnect();
            }
        }
    }

    public static Result<String> fetchStringFromURL(@NonNull String url, Charset charset) {
        Result<byte[]> r = fetchBytesFromURL(url);

        if (r.getErrorCode() != ERR_OK)
            return new Result<>(null, r.getErrorCode(), r.getErrorMessage());

        return new Result<>(charset != null ? new String((byte[])r.getContent(), charset) : new String((byte[])r.getContent(), StandardCharsets.UTF_8),
                r.getErrorCode(), r.getErrorMessage());
    }

}
