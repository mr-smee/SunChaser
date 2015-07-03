package com.example.sunchaser.app.asynctask;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.example.sunchaser.app.data.GooglePlacesImageEntry;
import com.example.sunchaser.app.data.Photo;
import com.example.sunchaser.app.data.Place;
import com.example.sunchaser.app.data.PlaceType;
import com.example.sunchaser.app.data.dbcontract.PlaceOfInterestEntry;
import com.example.sunchaser.app.data.dbcontract.PlaceOfInterestSearchResultRelationEntry;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by smee on 11/06/15.
 */
public class PlaceOfInterestDataLoader extends AsyncTaskLoader<Collection<Place>> {

    private static final String LOG_TAG = PlaceOfInterestDataLoader.class.getSimpleName();

    private static final String[] RESULT_COLUMNS = {
            PlaceOfInterestEntry.TABLE_NAME + "." + PlaceOfInterestEntry.COLUMN_API_ID,
            PlaceOfInterestEntry.TABLE_NAME + "." + PlaceOfInterestEntry.COLUMN_NAME,
            PlaceOfInterestEntry.TABLE_NAME + "." + PlaceOfInterestEntry.COLUMN_VICINITY,
            PlaceOfInterestEntry.TABLE_NAME + "." + PlaceOfInterestEntry.COLUMN_COORD_LAT,
            PlaceOfInterestEntry.TABLE_NAME + "." + PlaceOfInterestEntry.COLUMN_COORD_LON,
            PlaceOfInterestEntry.TABLE_NAME + "." + PlaceOfInterestEntry.COLUMN_PRICE_LEVEL,
            PlaceOfInterestEntry.TABLE_NAME + "." + PlaceOfInterestEntry.COLUMN_RATING,
            PlaceOfInterestEntry.TABLE_NAME + "." + PlaceOfInterestEntry.COLUMN_PLACE_TYPES,
            PlaceOfInterestEntry.TABLE_NAME + "." + PlaceOfInterestEntry.COLUMN_ICON,
    };

    private static final int COLUMN_INDEX_PLACE_ID          = 0;
    private static final int COLUMN_INDEX_PLACE_NAME        = 1;
    private static final int COLUMN_INDEX_PLACE_VICINITY    = 2;
    private static final int COLUMN_INDEX_PLACE_COORD_LAT   = 3;
    private static final int COLUMN_INDEX_PLACE_COORD_LON   = 4;
    private static final int COLUMN_INDEX_PRICE_LEVEL       = 5;
    private static final int COLUMN_INDEX_RATING            = 6;
    private static final int COLUMN_INDEX_PLACE_TYPES       = 7;
    private static final int COLUMN_INDEX_PLACE_ICON        = 8;
    private final List<String> placeIds;

    private static final String[] IMAGE_COLUMNS = {
            GooglePlacesImageEntry.TABLE_NAME + "." + GooglePlacesImageEntry.COLUMN_REFERENCE,
            GooglePlacesImageEntry.TABLE_NAME + "." + GooglePlacesImageEntry.COLUMN_WIDTH,
            GooglePlacesImageEntry.TABLE_NAME + "." + GooglePlacesImageEntry.COLUMN_HEIGHT,
            GooglePlacesImageEntry.TABLE_NAME + "." + GooglePlacesImageEntry.COLUMN_ATTRIBUTIONS,
    };

    private static final int IMAGE_COLUMN_INDEX_REFERENCE    = 0;
    private static final int IMAGE_COLUMN_INDEX_WIDTH        = 1;
    private static final int IMAGE_COLUMN_INDEX_HEIGHT       = 2;
    private static final int IMAGE_COLUMN_INDEX_ATTRIBUTIONS = 3;

    private Collection<Place> results;

    public PlaceOfInterestDataLoader(Context context, List<String> placeIds) {
        super(context);
        this.placeIds = placeIds;
    }


    @Override
    public Collection<Place> loadInBackground() {
        Uri searchResultLoadUri = PlaceOfInterestSearchResultRelationEntry.CONTENT_URI;

        StringBuilder whereClauseBuilder = new StringBuilder(PlaceOfInterestEntry.TABLE_NAME + "." + PlaceOfInterestEntry.COLUMN_API_ID + " IN (?");
        for (int i = 0; i < placeIds.size() - 1; i++) {
            whereClauseBuilder.append(",?");
        }
        whereClauseBuilder.append(")");
        String whereClause = whereClauseBuilder.toString();

        String[] whereClauseValues = new String[placeIds.size()];
        placeIds.toArray(whereClauseValues);

        Cursor cursor = getContext().getContentResolver().query(searchResultLoadUri, RESULT_COLUMNS, whereClause, whereClauseValues, null);
        if (cursor.moveToFirst()) {
            Log.d(LOG_TAG, "Found " + cursor.getCount() + " existing results");
            // If we got some results, just return those
            return extractDataFromCursor(cursor);
        }

        return results;
    }

    private Collection<Place> extractDataFromCursor(Cursor cursor) {
        List<Place> places = new ArrayList<Place>(cursor.getCount());
        do {
            String placeId = cursor.getString(COLUMN_INDEX_PLACE_ID);
            String placeName = cursor.getString(COLUMN_INDEX_PLACE_NAME);
            double latitude = cursor.getDouble(COLUMN_INDEX_PLACE_COORD_LAT);
            double longitude = cursor.getDouble(COLUMN_INDEX_PLACE_COORD_LON);
            LatLng placeLocation = new LatLng(latitude, longitude);
            String vicinity = cursor.getString(COLUMN_INDEX_PLACE_VICINITY);
            String packedPlaceTypes = cursor.getString(COLUMN_INDEX_PLACE_TYPES);
            String iconUrl = cursor.getString(COLUMN_INDEX_PLACE_ICON);
            Set<PlaceType> types = unpackPlaceTypes(packedPlaceTypes);
            int priceLevel = cursor.getInt(COLUMN_INDEX_PRICE_LEVEL);
            float rating = cursor.getFloat(COLUMN_INDEX_RATING);
            List<Photo> photos = loadPhotos(placeId);

            Place place = new Place(placeId, placeName, placeLocation, vicinity, types, iconUrl, priceLevel, rating, photos);
            places.add(place);
            Log.d(LOG_TAG, "Loaded place from database: " + place.toString());
        } while (cursor.moveToNext());

        return places;
    }

    private List<Photo> loadPhotos(String placeId) {
        // TODO: It would be more efficient  to load these as a join rather than separate loads...
        Uri photosUri = GooglePlacesImageEntry.CONTENT_URI;
        String whereClause = GooglePlacesImageEntry.COLUMN_PLACE_ID + " = ?";
        String[] whereClauseValues = {placeId};

        Cursor cursor = getContext().getContentResolver().query(photosUri, IMAGE_COLUMNS, whereClause, whereClauseValues, null);
        List<Photo> photos = new ArrayList<>(cursor.getCount());

        if (cursor.moveToFirst()) {
            Log.d(LOG_TAG, "Found " + cursor.getCount() + " images for place " + placeId);

            do {
                String reference = cursor.getString(IMAGE_COLUMN_INDEX_REFERENCE);
                int width = cursor.getInt(IMAGE_COLUMN_INDEX_WIDTH);
                int height = cursor.getInt(IMAGE_COLUMN_INDEX_HEIGHT);
                String attributions = cursor.getString(IMAGE_COLUMN_INDEX_ATTRIBUTIONS);
                String[] attributionsArray = GooglePlacesImageEntry.unpackAttributions(attributions);

                photos.add(new Photo(height, width, reference, attributionsArray));
            } while (cursor.moveToNext());
        }

        return photos;
    }

    // TODO: Move this so it's not duplicated
    private Set<PlaceType> unpackPlaceTypes(String packedData) {
        Set<PlaceType> placeTypes = new HashSet<>();

        if (packedData == null || packedData.isEmpty()) {
            return placeTypes;
        }

        String[] typeNames = packedData.split("|");
        for (String typeName : typeNames) {
            PlaceType placeType = PlaceType.fromInternalName(typeName);
            if (placeType != null) {
                placeTypes.add(placeType);
            }
        }

        return placeTypes;
    }

    /**
     * Called when there is new data to deliver to the client.  The
     * super class will take care of delivering it; the implementation
     * here just adds a little more logic.
     */
    @Override
    public void deliverResult(Collection<Place> places) {
        if (isReset()) {
            // An async query came in while the loader is stopped.  We
            // don't need the result.
            if (places != null) {
                onReleaseResources(places);
            }
        }
        Collection<Place> oldModels = results;
        results = places;

        if (isStarted()) {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            super.deliverResult(results);
        }

        // At this point we can release the resources associated with
        // 'oldApps' if needed; now that the new result is delivered we
        // know that it is no longer in use.
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
            // If we currently have a result available, deliver it
            // immediately.
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
    public void onCanceled(Collection<Place> models) {
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
    protected void onReleaseResources(Collection<Place> places) {
        // For a simple List<> there is nothing to do.  For something
        // like a Cursor, we would close it here.
    }
}
