package com.example.sunchaser.app.asynctask;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.example.sunchaser.app.data.GooglePlacesImageEntry;
import com.example.sunchaser.app.data.Photo;
import com.example.sunchaser.app.data.Place;
import com.example.sunchaser.app.data.PlaceType;
import com.example.sunchaser.app.data.dbcontract.PlaceOfInterestEntry;
import com.example.sunchaser.app.data.dbcontract.PlaceOfInterestSearchEntry;
import com.example.sunchaser.app.data.dbcontract.PlaceOfInterestSearchResultRelationEntry;
import com.example.sunchaser.app.net.PlaceOfInterestClient;
import com.example.sunchaser.app.net.PlaceSearchResultPage;
import com.example.sunchaser.app.preferences.PlaceOfInterestSearchOptions;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by smee on 02/06/15.
 */
public class LoadPlacesOfInterestAsyncTask extends AsyncTask<PlaceOfInterestSearchOptions, Void, PlaceSearchResultPage> {

    private static final String LOG_TAG = LoadPlacesOfInterestAsyncTask.class.getSimpleName();

    private final PlaceListDisplay callbackHandler;
    private ContentResolver contentResolver;
    private final LatLng location;
    private final String pageToken;

    private static final String[] RESULT_COLUMNS = {
            PlaceOfInterestSearchEntry.TABLE_NAME + "." + PlaceOfInterestSearchEntry.COLUMN_NEXT_PAGE_TOKEN,
            PlaceOfInterestSearchEntry.TABLE_NAME + "." + PlaceOfInterestSearchEntry.COLUMN_COORD_LAT,
            PlaceOfInterestSearchEntry.TABLE_NAME + "." + PlaceOfInterestSearchEntry.COLUMN_COORD_LON,
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

    private static final int COLUMN_INDEX_NEXT_PAGE_TOKEN   = 0;
    private static final int COLUMN_SEARCH_COORD_LAT        = 1;
    private static final int COLUMN_SEARCH_COORD_LON        = 2;
    private static final int COLUMN_INDEX_PLACE_ID          = 3;
    private static final int COLUMN_INDEX_PLACE_NAME        = 4;
    private static final int COLUMN_INDEX_PLACE_VICINITY    = 5;
    private static final int COLUMN_INDEX_PLACE_COORD_LAT   = 6;
    private static final int COLUMN_INDEX_PLACE_COORD_LON   = 7;
    private static final int COLUMN_INDEX_PRICE_LEVEL       = 8;
    private static final int COLUMN_INDEX_RATING            = 9;
    private static final int COLUMN_INDEX_PLACE_TYPES       = 10;
    private static final int COLUMN_INDEX_PLACE_ICON        = 11;


    public LoadPlacesOfInterestAsyncTask(PlaceListDisplay callbackHandler, ContentResolver contentResolver, LatLng location, String pageToken) {
        this.callbackHandler = callbackHandler;
        this.contentResolver = contentResolver;
        this.location = location;
        this.pageToken = pageToken;
    }

    @Override
    protected PlaceSearchResultPage doInBackground(PlaceOfInterestSearchOptions... searchOptionses) {

        // Check cache for existing results for this search
        // Either return existing entries, or, if none exist:
        //     make query, get resulting Place data back
        //     store data in cache
        //     return result

        PlaceOfInterestSearchOptions searchOptions = searchOptionses[0];

        Uri searchResultLoadUri = PlaceOfInterestSearchResultRelationEntry.CONTENT_URI;

        String whereClause = PlaceOfInterestSearchEntry.TABLE_NAME + "." + PlaceOfInterestSearchEntry.COLUMN_COORD_LAT + " = ? AND " +
                PlaceOfInterestSearchEntry.TABLE_NAME + "." + PlaceOfInterestSearchEntry.COLUMN_COORD_LON + " = ? AND " +
                PlaceOfInterestSearchEntry.TABLE_NAME + "." + PlaceOfInterestSearchEntry.COLUMN_PLACE_TYPES + " = ? AND " +
                PlaceOfInterestSearchEntry.TABLE_NAME + "." + PlaceOfInterestSearchEntry.COLUMN_RADIUS_METRES + " = ? AND " +
                PlaceOfInterestSearchEntry.TABLE_NAME + "." + PlaceOfInterestSearchEntry.COLUMN_CURRENT_PAGE_TOKEN + " = ?";

        String[] whereClauseValues = new String[] {
                Double.toString(location.latitude),
                Double.toString(location.longitude),
                packPlaceTypes(searchOptions.getAllSelectedPlaceTypes()),
                Integer.toString(searchOptions.getRadiusMetres()),
                pageToken,
        };

        Cursor cursor = contentResolver.query(searchResultLoadUri, RESULT_COLUMNS, whereClause, whereClauseValues, null);
        if (cursor.moveToFirst()) {
            Log.d(LOG_TAG, "Found " + cursor.getCount() + " existing results");
            // If we got some results, just return those
            return extractDataFromCursor(pageToken, cursor);
        }

        Log.d(LOG_TAG, "No existing results found. Performing search...");
        // Otherwise, fetch some places...

        PlaceSearchResultPage searchResultPage = new PlaceOfInterestClient(searchOptions, location, pageToken).makeRequest();

        int insertedSearchId = storeSearchEntry(pageToken, searchOptions, searchResultPage);
        storePlaceEntries(searchResultPage);
        storeSearchRelations(searchResultPage, insertedSearchId);

        return searchResultPage;
    }

    private void storeSearchRelations(PlaceSearchResultPage searchResultPage, int insertedSearchId) {
        List<Place> places = searchResultPage.getPlaces();
        List<ContentValues> contentValues = new ArrayList<>(places.size());

        for (Place place : places) {
            ContentValues cv = new ContentValues();

            cv.put(PlaceOfInterestSearchResultRelationEntry.COLUMN_POI_SEARCH_ID, insertedSearchId);
            cv.put(PlaceOfInterestSearchResultRelationEntry.COLUMN_POI_ID, place.getId());
            cv.put(PlaceOfInterestSearchResultRelationEntry.COLUMN_TIMESTAMP, System.currentTimeMillis());

            contentValues.add(cv);
        }

        ContentValues[] contentValuesArray = new ContentValues[places.size()];
        contentValues.toArray(contentValuesArray);

        contentResolver.bulkInsert(PlaceOfInterestSearchResultRelationEntry.CONTENT_URI, contentValuesArray);
    }

    private void storePlaceEntries(PlaceSearchResultPage searchResultPage) {
        List<Place> places = searchResultPage.getPlaces();
        List<ContentValues> contentValues = new ArrayList<>(places.size());

        long currentTimestamp = System.currentTimeMillis();

        for (Place place : places) {
            ContentValues cv = new ContentValues();

            cv.put(PlaceOfInterestEntry.COLUMN_API_ID, place.getId());
            cv.put(PlaceOfInterestEntry.COLUMN_NAME, place.getName());
            cv.put(PlaceOfInterestEntry.COLUMN_ICON, place.getListIconUrl());
            cv.put(PlaceOfInterestEntry.COLUMN_VICINITY, place.getVicinity());
            cv.put(PlaceOfInterestEntry.COLUMN_COORD_LAT, place.getLocation().latitude);
            cv.put(PlaceOfInterestEntry.COLUMN_COORD_LON, place.getLocation().longitude);
            cv.put(PlaceOfInterestEntry.COLUMN_PLACE_TYPES, packPlaceTypes(place.getTypes()));
            cv.put(PlaceOfInterestEntry.COLUMN_RATING, place.getRating());
            cv.put(PlaceOfInterestEntry.COLUMN_PRICE_LEVEL, place.getPriceLevel());
            cv.put(PlaceOfInterestEntry.COLUMN_TIMESTAMP, currentTimestamp);

            contentValues.add(cv);

        }

        ContentValues[] contentValuesArray = new ContentValues[contentValues.size()];
        contentValues.toArray(contentValuesArray);

        contentResolver.bulkInsert(PlaceOfInterestEntry.CONTENT_URI, contentValuesArray);

        storePhotos(places);
    }

    private void storePhotos(List<Place> places) {
        List<ContentValues> contentValues = new ArrayList<>(places.size());

        long currentTimestamp = System.currentTimeMillis();

        for (Place place : places) {
            List<Photo> photos = place.getPhotos();
            for (Photo photo : photos) {
                ContentValues cv = new ContentValues();

                cv.put(GooglePlacesImageEntry.COLUMN_PLACE_ID, place.getId());
                cv.put(GooglePlacesImageEntry.COLUMN_REFERENCE, photo.getPhotoReference());
                cv.put(GooglePlacesImageEntry.COLUMN_WIDTH, photo.getWidth());
                cv.put(GooglePlacesImageEntry.COLUMN_HEIGHT, photo.getHeight());
                cv.put(GooglePlacesImageEntry.COLUMN_ATTRIBUTIONS, GooglePlacesImageEntry.packAttributions(photo.getHtmlAttributions()));
                cv.put(GooglePlacesImageEntry.COLUMN_TIMESTAMP, currentTimestamp);

                contentValues.add(cv);
            }
        }

        ContentValues[] contentValuesArray = new ContentValues[contentValues.size()];
        contentValues.toArray(contentValuesArray);

        contentResolver.bulkInsert(GooglePlacesImageEntry.CONTENT_URI, contentValuesArray);
    }

    private int storeSearchEntry(String currentPageToken, PlaceOfInterestSearchOptions searchOptions, PlaceSearchResultPage searchResultPage) {
        ContentValues searchValues = new ContentValues();
        searchValues.put(PlaceOfInterestSearchEntry.COLUMN_COORD_LAT, Double.toString(location.latitude));
        searchValues.put(PlaceOfInterestSearchEntry.COLUMN_COORD_LON, Double.toString(location.longitude));
        searchValues.put(PlaceOfInterestSearchEntry.COLUMN_RADIUS_METRES, searchOptions.getRadiusMetres());
        searchValues.put(PlaceOfInterestSearchEntry.COLUMN_PLACE_TYPES, packPlaceTypes(searchOptions.getAllSelectedPlaceTypes()));
        searchValues.put(PlaceOfInterestSearchEntry.COLUMN_CURRENT_PAGE_TOKEN, currentPageToken);
        searchValues.put(PlaceOfInterestSearchEntry.COLUMN_NEXT_PAGE_TOKEN, searchResultPage.getNextPageToken());
        searchValues.put(PlaceOfInterestSearchEntry.COLUMN_TIMESTAMP, System.currentTimeMillis());

        Uri insertedEntryUri = contentResolver.insert(PlaceOfInterestSearchEntry.CONTENT_URI, searchValues);
        return Integer.valueOf(insertedEntryUri.getPathSegments().get(1));
    }

    private PlaceSearchResultPage extractDataFromCursor(String currentPageToken, Cursor cursor) {
        String nextPageToken = cursor.getString(COLUMN_INDEX_NEXT_PAGE_TOKEN);

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
            // TODO: Read photo data out? Maybe not needed here
            ArrayList<Photo> photos = new ArrayList<>();

            Place place = new Place(placeId, placeName, placeLocation, vicinity, types, iconUrl, priceLevel, rating, photos);
            places.add(place);
            Log.d(LOG_TAG, "Loaded place from database: " + place.toString());
        } while (cursor.moveToNext());

        return new PlaceSearchResultPage(places, currentPageToken, nextPageToken);
    }


    private String packPlaceTypes(Set<PlaceType> placeTypes) {
        if (placeTypes == null || placeTypes.size() == 0) {
            return "";
        }

        StringBuilder placeTypeString = new StringBuilder();
        for (PlaceType placeType : placeTypes) {
            if (placeTypeString.length() != 0) {
                placeTypeString.append('|');
            }
            placeTypeString.append(placeType.getInternalName());
        }

        return placeTypeString.toString();
    }

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


    @Override
    protected void onPostExecute(PlaceSearchResultPage page) {
        callbackHandler.setPlaceList(page.getPlaces(), page.getNextPageToken());
    }

    public static interface PlaceListDisplay {
        public abstract void setPlaceList(List<Place> places, String nextPageToken);
    }
}
