package com.example.sunchaser.app.data.dbcontract;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by smee on 03/06/15.
 */
public class PlaceOfInterestSearchResultRelationEntry implements BaseColumns {

    public static final Uri CONTENT_URI =
            DbContract.BASE_CONTENT_URI.buildUpon().appendPath(DbContract.PATH_POI_SEARCH_RELATION).build();

    public static final String CONTENT_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + DbContract.CONTENT_AUTHORITY + "/" + DbContract.PATH_POI_SEARCH_RELATION;
    public static final String CONTENT_ITEM_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + DbContract.CONTENT_AUTHORITY + "/" + DbContract.PATH_POI_SEARCH_RELATION;

    // Table name
    public static final String TABLE_NAME = "poi_search_results";

    /** The unique ID of a search that was performed */
    public static final String COLUMN_POI_SEARCH_ID = "search_id";

    /** The unique ID of a PlaceOfInterestEntry that was returned by the search */
    public static final String COLUMN_POI_ID = "poi_id";

    /** The time this entry was added, so we can cull old results that may not be needed any more */
    public static final String COLUMN_TIMESTAMP = "timestamp";


    public static Uri buildLocationUri(long id) {
        return ContentUris.withAppendedId(CONTENT_URI, id);
    }

}
