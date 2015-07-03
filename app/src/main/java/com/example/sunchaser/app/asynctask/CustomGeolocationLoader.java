package com.example.sunchaser.app.asynctask;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.example.sunchaser.app.data.GeolocationModel;
import com.example.sunchaser.app.data.WeatherModel;
import com.example.sunchaser.app.data.WikiArticle;
import com.example.sunchaser.app.data.WikiImageModel;
import com.example.sunchaser.app.net.GeolocationClient;
import com.example.sunchaser.app.net.WeatherClient;
import com.example.sunchaser.app.net.WikipediaArticleClient;
import com.example.sunchaser.app.net.WikipediaImageInfoClient;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by smee on 13/06/15.
 */
public class CustomGeolocationLoader extends AsyncTaskLoader<Collection<GeolocationModel>> {

    private final double latitude;
    private final double longitude;
    private Collection<GeolocationModel> results;

    public CustomGeolocationLoader(Context context, double latitude, double longitude) {
        super(context);
        this.latitude = latitude;
        this.longitude = longitude;
    }


    @Override
    public Collection<GeolocationModel> loadInBackground() {
        // Load details from GeoNames, retrying with a wider radius until we find somewhere
        //          (Add a setting for location sensitivity for when we generate the bounding box to search in?)
        LatLng centre = new LatLng(latitude, longitude);
        int radiusKm = 1; // TODO: Setting?
        Collection<GeolocationModel> nearestLocations = null;

        for (int i = 0; i < 7; i++) {
            nearestLocations = new GeolocationClient(getContext(), centre, radiusKm, 1, true).makeRequest();
            if (nearestLocations != null && !nearestLocations.isEmpty()) {
                break;
            }
            radiusKm *= 2;
        }

        // Didn't get anything, don't add location
        // TODO: Toast warning user nothing was found nearby
        if (nearestLocations == null || nearestLocations.isEmpty()) {
            return Collections.emptyList();
        }

        results = new ArrayList<>(nearestLocations);

        List<String> pageTitles = new ArrayList<>(results.size());
        for (GeolocationModel model : results) {
            pageTitles.add(model.getExtractTitle());
        }

        Map<String, WikiArticle> wikiArticles = new WikipediaArticleClient(getContext(), pageTitles).makeRequest();
        for (GeolocationModel model : results) {
            WikiArticle article = wikiArticles.get(model.getExtractTitle());
            if (article != null) {
                model.setImageNames(article.getImageTitles());
            }
        }
        // TODO: Tidy up!

        for (GeolocationModel model : results) {
            Set<String> imageUrls = new HashSet<>();
            Collections.addAll(imageUrls, model.getImageNames());
            Set<WikiImageModel> images = new WikipediaImageInfoClient(getContext(), imageUrls).makeRequest();
            model.setImages(images);

            List<WeatherModel> weatherForecast = new WeatherClient(getContext(), centre, model.getLocationId()).makeRequest();
            model.setWeatherForecast(weatherForecast);
        }

        return results;
    }

    @Override
    public void deliverResult(Collection<GeolocationModel> geolocationModels) {
        if (isReset()) {
            if (geolocationModels != null) {
                onReleaseResources(geolocationModels);
            }
        }
        Collection<GeolocationModel> oldModels = results;
        results = geolocationModels;

        if (isStarted()) {
            super.deliverResult(results);
        }

        if (oldModels != null) {
            onReleaseResources(oldModels);
        }
    }

    /**
     * Handles a request to start the Loader.
     */
    @Override
    protected void onStartLoading() {
        if (results != null) {
            deliverResult(results);
        }

        if (takeContentChanged() || results == null) {
            forceLoad();
        }
    }

    /**
     * Handles a request to stop the Loader.
     */
    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    /**
     * Handles a request to cancel a load.
     */
    @Override
    public void onCanceled(Collection<GeolocationModel> models) {
        super.onCanceled(models);
        onReleaseResources(models);
    }

    /**
     * Handles a request to completely reset the Loader.
     */
    @Override
    protected void onReset() {
        super.onReset();

        onStopLoading();

        if (results != null) {
            onReleaseResources(results);
            results = null;
        }

    }

    /**
     * Helper function to take care of releasing resources associated
     * with an actively loaded data set.
     */
    protected void onReleaseResources(Collection<GeolocationModel> models) {
        // For a simple List<> there is nothing to do.  For something
        // like a Cursor, we would close it here.
    }
}
