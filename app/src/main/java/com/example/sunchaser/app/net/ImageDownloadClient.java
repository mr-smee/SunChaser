package com.example.sunchaser.app.net;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by smee on 08/06/15.
 */
public class ImageDownloadClient {

    public Bitmap loadImageFromUrl(String urlToLoad) {

        // TODO: Store the images somewhere so we don't keep loading them
//        Bitmap image = ImageRepository.getInstance().loadImage(urlToLoad);
//        if (image != null) {
//            return image;
//        }

        Bitmap image = null;
        InputStream in = null;

        try {
            in = new URL(urlToLoad).openStream();
            image = BitmapFactory.decodeStream(in);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null)
                    in.close();
            } catch (IOException ioe) {
                // Don't really care
            }
        }

//        ImageRepository.getInstance().storeImage(urlToLoad, image);
        return image;
    }
}
