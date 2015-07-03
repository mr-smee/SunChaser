package com.example.sunchaser.app.data.dbcontract;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by smee on 08/06/15.
 */
public class WikiImageEntry implements BaseColumns {

    public static final Uri CONTENT_URI =
            DbContract.BASE_CONTENT_URI.buildUpon().appendPath(DbContract.PATH_WIKI_IMAGE).build();

    public static final String CONTENT_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + DbContract.CONTENT_AUTHORITY + "/" + DbContract.PATH_WIKI_IMAGE;
    public static final String CONTENT_ITEM_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + DbContract.CONTENT_AUTHORITY + "/" + DbContract.PATH_WIKI_IMAGE;

    public static final String TABLE_NAME = "wiki_image";

    public static final String COLUMN_FILENAME = "filename";
    public static final String COLUMN_WIDTH = "width";
    public static final String COLUMN_HEIGHT = "height";
    public static final String COLUMN_URL = "url";
    public static final String COLUMN_MIME_TYPE = "mime";
    public static final String COLUMN_THUMBNAIL_URL = "thumbnail_url";
    public static final String COLUMN_THUMBNAIL_MIME_TYPE = "thumbnail_mime";

    /** The time this entry was added, so we can cull old results that may not be needed any more */
    public static final String COLUMN_TIMESTAMP = "timestamp";


    public static Uri buildLocationUri(long id) {
        return ContentUris.withAppendedId(CONTENT_URI, id);
    }
}
