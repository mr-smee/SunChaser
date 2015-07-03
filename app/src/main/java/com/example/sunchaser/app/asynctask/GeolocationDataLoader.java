package com.example.sunchaser.app.asynctask;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;

import com.example.sunchaser.app.data.GeolocationModel;
import com.example.sunchaser.app.data.WeatherModel;
import com.example.sunchaser.app.data.dbcontract.GeolocationEntry;
import com.example.sunchaser.app.data.dbcontract.WeatherEntry;
import com.example.sunchaser.app.data.dbcontract.WikiArticleEntry;
import com.google.android.gms.maps.model.LatLng;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by smee on 09/06/15.
 */
public class GeolocationDataLoader extends AsyncTaskLoader<Collection<GeolocationModel>> {

    private final String[] BASE_COLUMNS = {
            GeolocationEntry.COLUMN_LOCATION_ID,
            GeolocationEntry.COLUMN_LOCATION_NAME,
            GeolocationEntry.COLUMN_COORD_LAT,
            GeolocationEntry.COLUMN_COORD_LONG,
            GeolocationEntry.COLUMN_WIKI_PAGE_NAME,
            WeatherEntry.COLUMN_WEATHER_ID,
            WeatherEntry.COLUMN_SHORT_DESC,
            WeatherEntry.COLUMN_DATE,
            WeatherEntry.COLUMN_MAX_TEMP,
            WeatherEntry.COLUMN_MIN_TEMP,
            WikiArticleEntry.COLUMN_TITLE,
            WikiArticleEntry.COLUMN_EXTRACT,
            WikiArticleEntry.COLUMN_IMAGES,
    };

    private static final int BASE_COLUMN_INDEX_ID =               0;
    private static final int BASE_COLUMN_INDEX_NAME =             1;
    private static final int BASE_COLUMN_INDEX_LAT =              2;
    private static final int BASE_COLUMN_INDEX_LONG =             3;
    private static final int BASE_COLUMN_INDEX_WIKI_PAGE =        4;
    private static final int BASE_COLUMN_INDEX_WEATHER_ID =       5;
    private static final int BASE_COLUMN_INDEX_WEATHER_DESC =     6;
    private static final int BASE_COLUMN_INDEX_WEATHER_DATE =     7;
    private static final int BASE_COLUMN_INDEX_WEATHER_MAX_TEMP = 8;
    private static final int BASE_COLUMN_INDEX_WEATHER_MIN_TEMP = 9;
    private static final int BASE_COLUMN_INDEX_WIKI_TITLE       = 10;
    private static final int BASE_COLUMN_INDEX_WIKI_EXTRACT     = 11;
    private static final int BASE_COLUMN_INDEX_WIKI_IMAGES      = 12;
    private final String[] geolocationIds;

    private Collection<GeolocationModel> results;
    

    public GeolocationDataLoader(Context context, int... geolocationIds) {
        super(context);
        this.geolocationIds = new String[geolocationIds.length];
        for (int i = 0; i < geolocationIds.length; i++) {
            this.geolocationIds[i] = Integer.toString(geolocationIds[i]);
        }
    }


    @Override
    public Collection<GeolocationModel> loadInBackground() {

        // Load base details of the geolocation
        Uri locationLoadUri = WeatherEntry.CONTENT_URI;

        String sortOrder = WeatherEntry.TABLE_NAME + "." + WeatherEntry.COLUMN_DATE + " ASC";

        Cursor locationCursor;
        if (geolocationIds.length == 0) {
            locationCursor = getContext().getContentResolver().query(locationLoadUri, BASE_COLUMNS, null, null, sortOrder);
        } else {
            StringBuilder whereClauseBuilder = new StringBuilder(GeolocationEntry.TABLE_NAME + "." + GeolocationEntry.COLUMN_LOCATION_ID + " IN (?");
            for (int i = 0; i < geolocationIds.length - 1; i++) {
                whereClauseBuilder.append(", ?");
            }
            whereClauseBuilder.append(")");
            String whereClause = whereClauseBuilder.toString();
            locationCursor = getContext().getContentResolver().query(locationLoadUri, BASE_COLUMNS, whereClause, geolocationIds, sortOrder);
        }


        if (!locationCursor.moveToFirst()) {
            return Collections.emptySet();
        }

        Map<Integer, GeolocationModel> results = new HashMap<>(locationCursor.getCount());

        do {
            int locationId = locationCursor.getInt(BASE_COLUMN_INDEX_ID);

            GeolocationModel existingModel = results.get(locationId);

            if (existingModel == null) {
                double latitude = locationCursor.getDouble(BASE_COLUMN_INDEX_LAT);
                double longitude = locationCursor.getDouble(BASE_COLUMN_INDEX_LONG);
                LatLng location = new LatLng(latitude, longitude);
                String locationName = locationCursor.getString(BASE_COLUMN_INDEX_NAME);
                String wikiTitle = locationCursor.getString(BASE_COLUMN_INDEX_WIKI_TITLE);
                String wikiExtract = locationCursor.getString(BASE_COLUMN_INDEX_WIKI_EXTRACT);
                String packedWikiImageNames = locationCursor.getString(BASE_COLUMN_INDEX_WIKI_IMAGES);
                String[] wikiImageNames = (packedWikiImageNames == null) ? new String[0] : packedWikiImageNames.split("\\|");

                existingModel = new GeolocationModel(locationId, location, locationName, wikiTitle, wikiExtract, wikiImageNames);
                results.put(locationId, existingModel);
            }

            long date = locationCursor.getLong(BASE_COLUMN_INDEX_WEATHER_DATE);
            String weatherDescription = locationCursor.getString(BASE_COLUMN_INDEX_WEATHER_DESC);
            int weatherConditionCode = locationCursor.getInt(BASE_COLUMN_INDEX_WEATHER_ID);
            double maxTemperature = locationCursor.getDouble(BASE_COLUMN_INDEX_WEATHER_MAX_TEMP);
            double minTemperature = locationCursor.getInt(BASE_COLUMN_INDEX_WEATHER_MIN_TEMP);
            WeatherModel weatherModel = new WeatherModel(locationId, date, weatherDescription, weatherConditionCode, maxTemperature, minTemperature);

            // We load weather with ascending dates, so always add to the end of the weather forecast list
            // Unless we got a duplicate somehow, in which case overwrite the old copy
            existingModel.addWeatherEntry(weatherModel);

        } while (locationCursor.moveToNext());

        for (GeolocationModel locationModel : results.values()) {
            ImageInfoDbLoader imageInfoDbLoader = new ImageInfoDbLoader(getContext(), locationModel.getImageNames());
            locationModel.setImages(imageInfoDbLoader.loadInBackground());
        }

        return results.values();
    }

    /**
     * Called when there is new data to deliver to the client.  The
     * super class will take care of delivering it; the implementation
     * here just adds a little more logic.
     */
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
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    /**
     * Handles a request to cancel a load.
     */
    @Override
    public void onCanceled(Collection<GeolocationModel> models) {
        super.onCanceled(models);

        // At this point we can release the resources associated with 'apps'
        // if needed.
        onReleaseResources(models);
    }

    /**
     * Handles a request to completely reset the Loader.
     */
    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
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
