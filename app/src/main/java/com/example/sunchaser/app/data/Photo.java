package com.example.sunchaser.app.data;

import com.example.sunchaser.app.net.PlaceOfInterestClient;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Created by smee on 03/06/15.
 */
public class Photo implements ImageModel {

    // TODO: Move this inot another HTTPClient thingy? This should probably just hold stuff we got from the JSON feed
    private final static String PHOTO_URL_FORMAT = "https://maps.googleapis.com/maps/api/place/photo?photoreference=%1s&key=%2s";
    private final static String PHOTO_URL_FORMAT_FRAGMENT_WIDTH_LIMIT = "&maxwidth=";
    private final static String PHOTO_URL_FORMAT_FRAGMENT_HEIGHT_LIMIT = "&maxheight=";

    private final int height;
    private final int width;
    private final String photoReference;
    private final List<String> htmlAttributions;

    public Photo(int height, int width, String photoReference, String[] htmlAttributions) {

        this.height = height;
        this.width = width;
        this.photoReference = photoReference;
        this.htmlAttributions = Arrays.asList(htmlAttributions);
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getWidth() {
        return width;
    }

    public String getPhotoReference() {
        return photoReference;
    }

    public List<String> getHtmlAttributions() {
        return htmlAttributions;
    }

    @Override
    public String getUrl() {
        return getUrl(null, null);
    }

    @Override
    public String getUrl(Integer maxWidth, Integer maxHeight) {
        String url = String.format(Locale.US, PHOTO_URL_FORMAT, photoReference, PlaceOfInterestClient.PLACES_API_KEY);

        if (maxWidth != null) {
            url += PHOTO_URL_FORMAT_FRAGMENT_WIDTH_LIMIT + maxWidth;
        }
        if (maxHeight != null) {
            url += PHOTO_URL_FORMAT_FRAGMENT_HEIGHT_LIMIT + maxHeight;
        }

        return url;
    }
}