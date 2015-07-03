package com.example.sunchaser.app.data;

/**
 * Created by smee on 13/06/15.
 */
public interface ImageModel {

    public abstract String getUrl();
    public abstract String getUrl(Integer maxWidth, Integer maxHeight);

    public abstract int getWidth();

    public abstract int getHeight();

}
