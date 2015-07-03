package com.example.sunchaser.app.data;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by smee on 09/06/15.
 */
public class GeolocationModel {

    private final int locationId;
    private final LatLng location;
    private final String locationName;
    private final String extractTitle;
    private final String extract;
    private String[] imageNames;
    private final List<WeatherModel> weatherForecast = new ArrayList<WeatherModel>();
    private final Set<WikiImageModel> images = new HashSet<>();

    public GeolocationModel(int locationId, LatLng location, String locationName, String extractTitle, String extract, String[] imageNames) {
        this.locationId = locationId;
        this.location = location;
        this.locationName = locationName;
        this.extractTitle = extractTitle;
        this.extract = extract;
        this.imageNames = imageNames;
    }

    public int getLocationId() {
        return locationId;
    }

    public LatLng getLocation() {
        return location;
    }

    public String getLocationName() {
        return locationName;
    }

    public String[] getImageNames() {
        return imageNames;
    }

    public void setImageNames(String[] imageNames) {
        this.imageNames = imageNames;
    }

    public void setImages(Set<WikiImageModel> images) {
        this.images.addAll(images);
    }

    public Set<WikiImageModel> getImageInfo() {
        return images;
    }

    public List<WeatherModel> getWeatherForecast() {
        return weatherForecast;
    }

    public void setWeatherForecast(List<WeatherModel> weatherForecast) {
        this.weatherForecast.clear();
        this.weatherForecast.addAll(weatherForecast);
    }

    public String getExtractTitle() {
        return extractTitle;
    }

    public String getExtract() {
        return extract;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GeolocationModel that = (GeolocationModel) o;

        return locationId == that.locationId;
    }

    @Override
    public int hashCode() {
        return locationId;
    }

    public void addWeatherEntry(WeatherModel weatherModel) {
        int weatherIndex = weatherForecast.indexOf(weatherModel);
        if (weatherIndex >= 0) {
            weatherForecast.set(weatherIndex, weatherModel);
        } else {
            weatherForecast.add(weatherModel);
        }
    }
}
