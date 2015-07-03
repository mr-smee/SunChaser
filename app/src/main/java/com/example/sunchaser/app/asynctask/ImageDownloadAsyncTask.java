package com.example.sunchaser.app.asynctask;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.example.sunchaser.app.net.ImageDownloadClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by smee on 08/06/15.
 */
public class ImageDownloadAsyncTask extends AsyncTask<String, Void, Void> {

    private final ImageDisplay callbackHandler;
    private final List<Bitmap> images = new ArrayList<>();

    public ImageDownloadAsyncTask(ImageDisplay callbackHandler) {
        this.callbackHandler = callbackHandler;
    }

    @Override
    protected Void doInBackground(String... imgUrls) {
        for (String imgUrl : imgUrls) {
            Bitmap bitmap = new ImageDownloadClient().loadImageFromUrl(imgUrl);
            if (bitmap != null) {
                images.add(bitmap);
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        callbackHandler.setImagesToDisplay(images);
    }

    public static interface ImageDisplay {
        public abstract void setImagesToDisplay(List<Bitmap> images);
    }
}
