package com.bignerdranch.android.photogallery.data;

/**
 * Created by Tom Buczynski on 13.09.2023.
 */
public class Result<T> {
    public static final int ERR_OK = 0;
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
