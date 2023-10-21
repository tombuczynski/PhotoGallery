package com.bignerdranch.android.photogallery.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.LruCache;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import androidx.annotation.NonNull;
import androidx.core.os.HandlerCompat;

/**
 * Created by Tom Buczynski on 17.09.2023.
 */
public class ImageDownloader<T extends ImageDownloader.Callback> extends HandlerThread {

    private static final String TAG = "ImageDownloader";
    public static final int ERR_INVALID_BITMAP = 5;
    private static final int MESSAGE_DOWNLOAD = 1;
    private static final int MESSAGE_PRELOAD = 2;


    private final ConcurrentHashMap<T, String> mTargetObject2UrlMap;

    private final Set<String> mPreloadRequests = ConcurrentHashMap.newKeySet();

    private final LruCache<String, Result<Bitmap>> mBitmapLruCache;

    public interface Callback {

        void onDownloadFinished(Result<Bitmap> result);

    }
    private Handler mDownloadsHandler;
    private final Handler mUiHandler;
    private boolean mQuit = false;
    public ImageDownloader(@NonNull Handler uiHandler, @NonNull LruCache<String, Result<Bitmap>> bitmapLruCache) {
        super(TAG);

        mUiHandler = uiHandler;
        mBitmapLruCache = bitmapLruCache;
        mTargetObject2UrlMap = new ConcurrentHashMap<>();
    }

    @Override
    public boolean quit() {
        mQuit = true;
        mDownloadsHandler.removeMessages(MESSAGE_DOWNLOAD);
        mTargetObject2UrlMap.clear();
        mPreloadRequests.clear();

        return super.quit();
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized void start() {
        super.start();

        Looper looper = getLooper();
        mDownloadsHandler = HandlerCompat.createAsync(looper, msg -> {

            if (msg.what == MESSAGE_DOWNLOAD) {
                T targetObject = (T) msg.obj;

                Log.d(TAG, "Download request for url: " + mTargetObject2UrlMap.get(targetObject));

                handleDownloadRequest(targetObject);

                return true;
            }
            else if (msg.what == MESSAGE_PRELOAD) {
                String url = (String) msg.obj;
                Log.d(TAG, "Preload request for url: " + url);

                handlePreloadRequest(url);

                return true;
            }

            return false;
        });
    }

    public void preloadAsync(@NonNull String imageUrl) {
        if (mBitmapLruCache.get(imageUrl) != null || ! mPreloadRequests.add(imageUrl))
            return;

        mDownloadsHandler.obtainMessage(MESSAGE_PRELOAD, imageUrl).sendToTarget();
    }

    public Result<Bitmap> downloadAsync(@NonNull T targetObject, @NonNull String imageUrl) {
        Result<Bitmap> cachedBitmapResult = mBitmapLruCache.get(imageUrl);

        if (cachedBitmapResult != null) {
            mTargetObject2UrlMap.computeIfPresent(targetObject, (k, v) -> imageUrl);

            return cachedBitmapResult;
        }

        mTargetObject2UrlMap.put(targetObject, imageUrl);

        Message msg =  mDownloadsHandler.obtainMessage(MESSAGE_DOWNLOAD, targetObject);
        mDownloadsHandler.sendMessageAtFrontOfQueue(msg);

        return null;
    }

    private void handlePreloadRequest(String url) {

        if (url == null)
            return;

        if (mBitmapLruCache.get(url) == null) {

            Result<byte[]> resultBytes = HTTPSFetcher.fetchBytesFromURL(url);

            if (resultBytes.getErrorCode() == Result.ERR_OK) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(resultBytes.getContent(), 0, resultBytes.getContent().length);

                if (bitmap != null) {
                    Result<Bitmap> resultBitmap = new Result<>(bitmap, resultBytes.getErrorCode(), resultBytes.getErrorMessage());

                    mBitmapLruCache.put(url, resultBitmap);
                }
            }
        }

        mPreloadRequests.remove(url);
    }

    private void handleDownloadRequest(T targetObject) {
        String url = mTargetObject2UrlMap.get(targetObject);

        if (url == null)
            return;

        Result<Bitmap> cachedBitmapResult = mBitmapLruCache.get(url);

        Result<Bitmap> resultBitmap;

        if (cachedBitmapResult != null){
            resultBitmap = cachedBitmapResult;
        } else {
            Result<byte[]> resultBytes = HTTPSFetcher.fetchBytesFromURL(url);

            if (resultBytes.getErrorCode() == Result.ERR_OK) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(resultBytes.getContent(), 0, resultBytes.getContent().length);

                if (bitmap != null) {
                    resultBitmap = new Result<>(bitmap, resultBytes.getErrorCode(), resultBytes.getErrorMessage());
                } else {
                    resultBitmap = new Result<>(bitmap, ERR_INVALID_BITMAP, "Invalid bitmap, can't decode");
                }
            } else {
                resultBitmap = new Result<>(null, resultBytes.getErrorCode(), resultBytes.getErrorMessage());
            }
        }

        mUiHandler.post(() -> {
            if (mQuit)
                return;

            String urlActual = mTargetObject2UrlMap.get(targetObject);
            if (! url.equals(urlActual))
                return;

            mTargetObject2UrlMap.remove(targetObject);

            targetObject.onDownloadFinished(resultBitmap);

            if (resultBitmap.getErrorCode() == Result.ERR_OK)
                mBitmapLruCache.put(url, resultBitmap);
        });
    }
}
