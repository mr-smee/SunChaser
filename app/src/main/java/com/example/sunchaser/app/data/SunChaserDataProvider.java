/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.sunchaser.app.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.example.sunchaser.app.data.dbcontract.DbContract;
import com.example.sunchaser.app.data.dbcontract.GeolocationEntry;
import com.example.sunchaser.app.data.dbcontract.PlaceOfInterestEntry;
import com.example.sunchaser.app.data.dbcontract.PlaceOfInterestSearchEntry;
import com.example.sunchaser.app.data.dbcontract.PlaceOfInterestSearchResultRelationEntry;
import com.example.sunchaser.app.data.dbcontract.WeatherEntry;
import com.example.sunchaser.app.data.dbcontract.WikiArticleEntry;
import com.example.sunchaser.app.data.dbcontract.WikiImageEntry;

public class SunChaserDataProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher URI_MATCHER = buildUriMatcher();
    private DbHelper dbHelper;

    static final int WEATHER = 100;
    static final int WEATHER_FOR_LOCATION_ID = 101;
    static final int WEATHER_WITH_LOCATION_AND_DATE = 102;
    static final int LOCATION = 300;

    static final int PLACE_OF_INTEREST = 500;
    static final int PLACE_OF_INTEREST_SEARCH = 501;
    static final int PLACE_OF_INTEREST_SEARCH_RESULTS = 502;
    static final int PLACE_OF_INTEREST_IMAGE = 503;

    static final int WIKI_ARTICLES = 600;
    static final int WIKI_IMAGES = 601;

    private static final SQLiteQueryBuilder WEATHER_QUERY_BUILDER;
    private static final SQLiteQueryBuilder POI_QUERY_BUILDER;

    static {
        WEATHER_QUERY_BUILDER = new SQLiteQueryBuilder();

        //This is an inner join which looks like
        //weather INNER JOIN location ON weather.location_id = location._id
        WEATHER_QUERY_BUILDER.setTables(
                WeatherEntry.TABLE_NAME + " INNER JOIN " +
                        GeolocationEntry.TABLE_NAME +
                        " ON " + WeatherEntry.TABLE_NAME +
                        "." + WeatherEntry.COLUMN_LOC_KEY +
                        " = " + GeolocationEntry.TABLE_NAME +
                        "." + GeolocationEntry.COLUMN_LOCATION_ID +
                        " LEFT JOIN " + WikiArticleEntry.TABLE_NAME +
                        " ON " + WikiArticleEntry.TABLE_NAME +
                        "." + WikiArticleEntry.COLUMN_TITLE +
                        " = " + GeolocationEntry.TABLE_NAME +
                        "." + GeolocationEntry.COLUMN_WIKI_PAGE_NAME
        );

        POI_QUERY_BUILDER = new SQLiteQueryBuilder();

        //This is an inner join which looks like
        //(poi INNER JOIN poi_search_results ON poi.api_id = poi_search_results.poi_id)
        //      INNER JOIN poi_search ON poi_search_results.search_id = poi_search._id
        POI_QUERY_BUILDER.setTables(
                "(" + PlaceOfInterestSearchEntry.TABLE_NAME + " INNER JOIN " +
                        PlaceOfInterestSearchResultRelationEntry.TABLE_NAME +
                        " ON " + PlaceOfInterestSearchEntry.TABLE_NAME +
                        "." + PlaceOfInterestSearchEntry._ID +
                        " = " + PlaceOfInterestSearchResultRelationEntry.TABLE_NAME +
                        "." + PlaceOfInterestSearchResultRelationEntry.COLUMN_POI_SEARCH_ID + ") " +
                        "INNER JOIN " + PlaceOfInterestEntry.TABLE_NAME +
                        " ON " + PlaceOfInterestEntry.TABLE_NAME +
                        "." + PlaceOfInterestEntry.COLUMN_API_ID +
                        " = " + PlaceOfInterestSearchResultRelationEntry.TABLE_NAME +
                        "." + PlaceOfInterestSearchResultRelationEntry.COLUMN_POI_ID
        );
    }

    //location.location_setting = ?
    private static final String LOCATION_SETTING_SELECTION =
            GeolocationEntry.TABLE_NAME +
                    "." + GeolocationEntry.COLUMN_LOCATION_ID + " = ?";

    //location.location_setting = ? AND date >= ?
    private static final String LOCATION_SETTING_WITH_START_DATE_SELECTION =
            GeolocationEntry.TABLE_NAME +
                    "." + GeolocationEntry.COLUMN_LOCATION_ID + " = ? AND " +
                    WeatherEntry.COLUMN_DATE + " >= ? ";

    //location.location_setting = ? AND date = ?
    private static final String LOCATION_SETTING_AND_DAY_SELECTION =
            GeolocationEntry.TABLE_NAME +
                    "." + GeolocationEntry.COLUMN_LOCATION_ID + " = ? AND " +
                    WeatherEntry.COLUMN_DATE + " = ? ";

    private Cursor getWeatherByLocation(Uri uri, String[] projection, String sortOrder) {
        int locationId = WeatherEntry.getLocationSettingFromUri(uri);
//        long startDate = WeatherEntry.getStartDateFromUri(uri);

        String[] selectionArgs;
        String selection;

//        if (startDate == 0) {
            selection = LOCATION_SETTING_SELECTION;
            selectionArgs = new String[]{Integer.toString(locationId)};
//        } else {
//            selectionArgs = new String[]{locationSetting, Long.toString(startDate)};
//            selection = LOCATION_SETTING_WITH_START_DATE_SELECTION;
//        }

        return getWeather(projection, selection, selectionArgs, sortOrder);
    }

    private Cursor getWeather(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return WEATHER_QUERY_BUILDER.query(dbHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
    }

//    private Cursor getWeatherByLocationSettingAndDate(
//            Uri uri, String[] projection, String sortOrder) {
//        String locationSetting = WeatherEntry.getLocationSettingFromUri(uri);
//        long date = WeatherEntry.getDateFromUri(uri);
//
//        return WEATHER_QUERY_BUILDER.query(dbHelper.getReadableDatabase(),
//                projection,
//                LOCATION_SETTING_AND_DAY_SELECTION,
//                new String[]{locationSetting, Long.toString(date)},
//                null,
//                null,
//                sortOrder
//        );
//    }

    private Cursor loadExistingSearchResults(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return POI_QUERY_BUILDER.query(dbHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
    }

    static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = DbContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, DbContract.PATH_WEATHER, WEATHER);
        matcher.addURI(authority, DbContract.PATH_WEATHER + "/*", WEATHER_FOR_LOCATION_ID);
//        matcher.addURI(authority, DbContract.PATH_WEATHER + "/*/#", WEATHER_WITH_LOCATION_AND_DATE);
        matcher.addURI(authority, DbContract.PATH_LOCATION, LOCATION);
        matcher.addURI(authority, DbContract.PATH_POI, PLACE_OF_INTEREST);
        matcher.addURI(authority, DbContract.PATH_POI_SEARCH, PLACE_OF_INTEREST_SEARCH);
        matcher.addURI(authority, DbContract.PATH_POI_SEARCH_RELATION, PLACE_OF_INTEREST_SEARCH_RESULTS);
        matcher.addURI(authority, DbContract.PATH_POI_IMAGE, PLACE_OF_INTEREST_IMAGE);
        matcher.addURI(authority, DbContract.PATH_WIKI, WIKI_ARTICLES);
        matcher.addURI(authority, DbContract.PATH_WIKI_IMAGE, WIKI_IMAGES);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        dbHelper = new DbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = URI_MATCHER.match(uri);

        switch (match) {
//            case WEATHER_WITH_LOCATION_AND_DATE:
//                return WeatherEntry.CONTENT_ITEM_TYPE;
            case WEATHER_FOR_LOCATION_ID:
                return WeatherEntry.CONTENT_TYPE;
            case WEATHER:
                return WeatherEntry.CONTENT_TYPE;
            case LOCATION:
                return GeolocationEntry.CONTENT_TYPE;
            case PLACE_OF_INTEREST:
                return PlaceOfInterestEntry.CONTENT_TYPE;
            case PLACE_OF_INTEREST_SEARCH:
                return PlaceOfInterestSearchEntry.CONTENT_TYPE;
            case WIKI_ARTICLES:
                return WikiArticleEntry.CONTENT_TYPE;
            case WIKI_IMAGES:
                return WikiImageEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (URI_MATCHER.match(uri)) {
            // "weather/*/*"
//            case WEATHER_WITH_LOCATION_AND_DATE: {
//                retCursor = getWeatherByLocationSettingAndDate(uri, projection, sortOrder);
//                break;
//            }
            // "weather/*"
            case WEATHER_FOR_LOCATION_ID: {
                retCursor = getWeatherByLocation(uri, projection, sortOrder);
                break;
            }
            // "weather"
            case WEATHER: {
                retCursor = getWeather(projection, selection, selectionArgs, sortOrder);
                //retCursor = dbHelper.getReadableDatabase().query(WeatherEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            }
            // "location"
            case LOCATION: {
                retCursor = dbHelper.getReadableDatabase().query(GeolocationEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            }
            case PLACE_OF_INTEREST: {
                retCursor = dbHelper.getReadableDatabase().query(PlaceOfInterestEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            }
            case PLACE_OF_INTEREST_SEARCH_RESULTS: {
                retCursor = loadExistingSearchResults(projection, selection, selectionArgs, sortOrder);
                break;
            }
            case PLACE_OF_INTEREST_IMAGE: {
                retCursor = dbHelper.getReadableDatabase().query(GooglePlacesImageEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            }
            case WIKI_ARTICLES: {
                retCursor = dbHelper.getReadableDatabase().query(WikiArticleEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            }
            case WIKI_IMAGES: {
                retCursor = dbHelper.getReadableDatabase().query(WikiImageEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final int match = URI_MATCHER.match(uri);
        Uri returnUri;

        switch (match) {
            case WEATHER: {
                normalizeDate(values);
                long _id = db.insert(WeatherEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = WeatherEntry.buildWeatherUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case LOCATION: {
                long _id = db.insert(GeolocationEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = GeolocationEntry.buildLocationUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case PLACE_OF_INTEREST: {
                long _id = db.insert(PlaceOfInterestEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = PlaceOfInterestEntry.buildLocationUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case PLACE_OF_INTEREST_IMAGE: {
                long _id = db.insert(GooglePlacesImageEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = PlaceOfInterestEntry.buildLocationUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case PLACE_OF_INTEREST_SEARCH: {
                long _id = db.insert(PlaceOfInterestSearchEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = PlaceOfInterestSearchEntry.buildLocationUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case PLACE_OF_INTEREST_SEARCH_RESULTS: {
                long _id = db.insert(PlaceOfInterestSearchResultRelationEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = PlaceOfInterestSearchResultRelationEntry.buildLocationUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case WIKI_ARTICLES: {
                long _id = db.insert(WikiArticleEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = WikiArticleEntry.buildLocationUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case WIKI_IMAGES: {
                long _id = db.insert(WikiImageEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = WikiImageEntry.buildLocationUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final int match = URI_MATCHER.match(uri);
        int rowsDeleted;

        // Setting this means we'll get the number of deleted rows
        if (selection == null) {
            selection = "1";
        }

        switch (match) {
            case WEATHER: {
                rowsDeleted = db.delete(WeatherEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case LOCATION: {
                rowsDeleted = db.delete(GeolocationEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            // TODO: Do we ever want to do these separately? Should there just be one delete-POI-related-stuff query that deletes as a transaction?
            case PLACE_OF_INTEREST: {
                rowsDeleted = db.delete(PlaceOfInterestEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case PLACE_OF_INTEREST_IMAGE: {
                rowsDeleted = db.delete(GooglePlacesImageEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case PLACE_OF_INTEREST_SEARCH: {
                rowsDeleted = db.delete(PlaceOfInterestSearchEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case PLACE_OF_INTEREST_SEARCH_RESULTS: {
                rowsDeleted = db.delete(PlaceOfInterestSearchResultRelationEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case WIKI_ARTICLES: {
                rowsDeleted = db.delete(WikiArticleEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case WIKI_IMAGES: {
                rowsDeleted = db.delete(WikiImageEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // A null value deletes all rows.  Notify the uri listeners (using the content resolver) if
        // the rowsDeleted != 0
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    private void normalizeDate(ContentValues values) {
        // normalize the date value
        if (values.containsKey(WeatherEntry.COLUMN_DATE)) {
            long dateValue = values.getAsLong(WeatherEntry.COLUMN_DATE);
            values.put(WeatherEntry.COLUMN_DATE, DbContract.normalizeDate(dateValue));
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Student: This is a lot like the delete function.  We return the number of rows impacted
        // by the update.
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final int match = URI_MATCHER.match(uri);
        int rowsUpdated;

        switch (match) {
            case WEATHER: {
                rowsUpdated = db.update(WeatherEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case LOCATION: {
                rowsUpdated = db.update(GeolocationEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case PLACE_OF_INTEREST: {
                rowsUpdated = db.update(PlaceOfInterestEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case PLACE_OF_INTEREST_IMAGE: {
                rowsUpdated = db.update(GooglePlacesImageEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case PLACE_OF_INTEREST_SEARCH: {
                rowsUpdated = db.update(PlaceOfInterestSearchEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case PLACE_OF_INTEREST_SEARCH_RESULTS: {
                rowsUpdated = db.update(PlaceOfInterestSearchResultRelationEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case WIKI_ARTICLES: {
                rowsUpdated = db.update(WikiArticleEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case WIKI_IMAGES: {
                rowsUpdated = db.update(WikiImageEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsUpdated != 0 || selection == null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final int match = URI_MATCHER.match(uri);
        switch (match) {
            case WEATHER:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        normalizeDate(value);
                        long _id = db.insert(WeatherEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        dbHelper.close();
        super.shutdown();
    }
}