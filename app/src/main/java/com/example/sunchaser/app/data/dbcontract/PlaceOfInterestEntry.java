package com.example.sunchaser.app.data.dbcontract;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by smee on 03/06/15.
 */
public class PlaceOfInterestEntry implements BaseColumns {

    public static final Uri CONTENT_URI =
            DbContract.BASE_CONTENT_URI.buildUpon().appendPath(DbContract.PATH_POI).build();

    public static final String CONTENT_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + DbContract.CONTENT_AUTHORITY + "/" + DbContract.PATH_POI;
    public static final String CONTENT_ITEM_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + DbContract.CONTENT_AUTHORITY + "/" + DbContract.PATH_POI;

    public static final String TABLE_NAME = "poi";

    public static final String COLUMN_API_ID = "api_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_ICON = "default_icon";
    public static final String COLUMN_VICINITY = "vicinity";
    public static final String COLUMN_COORD_LAT = "latitude";
    public static final String COLUMN_COORD_LON = "longitude";
    public static final String COLUMN_PLACE_TYPES = "place_types";
    public static final String COLUMN_RATING = "rating";
    public static final String COLUMN_PRICE_LEVEL = "price_level";

    /** The time this entry was added, so we can cull old results that may not be needed any more */
    public static final String COLUMN_TIMESTAMP = "timestamp";


    public static Uri buildLocationUri(long id) {
        return ContentUris.withAppendedId(CONTENT_URI, id);
    }

}
