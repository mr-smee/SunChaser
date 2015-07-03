package com.example.sunchaser.app.data;

import com.google.android.gms.maps.model.LatLng;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by smee on 03/06/15.
 */
public class Place {

    private final String placeId;
    private final String name;
    private final LatLng location;
    private final String vicinity;
    private final Set<PlaceType> types;
    private final String listIconUrl;
    private final int priceLevel;
    private final float rating;
    private final List<Photo> photos;

    public Place(String placeId, String name, LatLng location, String vicinity, Set<PlaceType> types, String listIconUrl,
                 int priceLevel, float rating, List<Photo> photos) {
        this.placeId = placeId;
        this.name = name;
        this.location = location;
        this.vicinity = vicinity;
        this.listIconUrl = listIconUrl;
        this.priceLevel = priceLevel;
        this.rating = rating;
        this.photos = Collections.unmodifiableList(photos);
        this.types = Collections.unmodifiableSet(types);

    }


    public String getId() {
        return placeId;
    }

    public String getName() {
        return name;
    }

    public LatLng getLocation() {
        return location;
    }

    public String getVicinity() {
        return vicinity;
    }

    public Set<PlaceType> getTypes() {
        return types;
    }

    public String getListIconUrl() {
        return listIconUrl;
    }

    public Integer getPriceLevel() {
        return priceLevel;
    }

    public float getRating() {
        return rating;
    }

    public List<Photo> getPhotos() {
        return photos;
    }

    @Override
    public String toString() {
        return "Place{" +
                "placeId='" + placeId + '\'' +
                ", name='" + name + '\'' +
                ", location=" + location +
                ", vicinity='" + vicinity + '\'' +
                ", types=" + types +
                ", listIconUrl='" + listIconUrl + '\'' +
                ", priceLevel=" + priceLevel +
                ", rating=" + rating +
                ", photos=" + photos +
                '}';
    }
}
