package com.example.sunchaser.app.asynctask;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.example.sunchaser.app.net.ImageDownloadClient;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by smee on 08/06/15.
 */
public class InfoWindowImageDownloadTask extends AsyncTask<String, Void, Void> {

    private final Marker marker;
    private ImageDisplay display;
    private Bitmap image;

    public InfoWindowImageDownloadTask(Marker marker, ImageDisplay display) {
        this.marker = marker;
        this.display = display;
    }

    @Override
    protected Void doInBackground(String... imgUrls) {
        if (imgUrls.length == 0) {
            return null;
        }

        String imgUrl = imgUrls[0];
        Bitmap bitmap = new ImageDownloadClient().loadImageFromUrl(imgUrl);
        if (bitmap != null) {
            image = bitmap;
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (image != null) {
            display.refreshView(marker, image);
        }
    }

    public static interface ImageDisplay {
        public abstract void refreshView(Marker marker, Bitmap image);
    }
}
