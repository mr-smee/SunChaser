package com.example.sunchaser.app.data.dbcontract;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by smee on 25/05/15.
 */
public final class GeolocationEntry implements BaseColumns {

    public static final Uri CONTENT_URI =
            DbContract.BASE_CONTENT_URI.buildUpon().appendPath(DbContract.PATH_LOCATION).build();

    public static final String CONTENT_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + DbContract.CONTENT_AUTHORITY + "/" + DbContract.PATH_LOCATION;
    public static final String CONTENT_ITEM_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + DbContract.CONTENT_AUTHORITY + "/" + DbContract.PATH_LOCATION;

    // Table name
    public static final String TABLE_NAME = "location";

    /** The unique GeoNames integer ID of the location */
    public static final String COLUMN_LOCATION_ID = "id";

    /** Human readable location string, provided by the API. */
    public static final String COLUMN_LOCATION_NAME = "location_name";

    /** Latitude of the location (stored as a float) */
    public static final String COLUMN_COORD_LONG = "coord_long";

    /** Longitude of the location (stored as a float) */
    public static final String COLUMN_COORD_LAT = "coord_lat";

    /** The page name of the Wikipedia article for this location, if one exists */
    public static final String COLUMN_WIKI_PAGE_NAME = "wiki_article_title";

    public static Uri buildLocationUri(long id) {
        return ContentUris.withAppendedId(CONTENT_URI, id);
    }
}