package com.bignerdranch.android.photogallery.data;

/**
 * Created by Tom Buczynski on 30.08.2023.
 */
public class FlickrPhotoGallery {
    private GalleryPage photos;
    private String stat;
    private Integer code;
    private String message;

    public GalleryPage getPage() {
        return photos;
    }

    public String getStat() {
        return stat;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
