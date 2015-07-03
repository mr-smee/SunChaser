package com.example.sunchaser.app.net;

import com.example.sunchaser.app.data.Place;

import java.util.List;

/**
 * Created by smee on 03/06/15.
 */
public class PlaceSearchResultPage {

    private final List<Place> places;
    private final String currentPageToken;
    private final String nextPageToken;

    public PlaceSearchResultPage(List<Place> places, String currentPageToken, String nextPageToken) {
        this.places = places;
        this.currentPageToken = currentPageToken;
        this.nextPageToken = nextPageToken;
    }

    public List<Place> getPlaces() {
        return places;
    }

    public String getCurrentPageToken() {
        return currentPageToken;
    }

    public String getNextPageToken() {
        return nextPageToken;
    }
}
