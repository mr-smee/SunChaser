package com.example.sunchaser.app.data.dbcontract;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by smee on 07/06/15.
 */
public class WikiArticleEntry implements BaseColumns {

    public static final Uri CONTENT_URI =
            DbContract.BASE_CONTENT_URI.buildUpon().appendPath(DbContract.PATH_WIKI).build();

    public static final String CONTENT_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + DbContract.CONTENT_AUTHORITY + "/" + DbContract.PATH_WIKI;
    public static final String CONTENT_ITEM_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + DbContract.CONTENT_AUTHORITY + "/" + DbContract.PATH_WIKI;

    public static final String TABLE_NAME = "wiki_article";

    public static final String COLUMN_ARTICLE_ID = "article_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_EXTRACT = "extract";
    public static final String COLUMN_IMAGES = "images";

    /** The time this entry was added, so we can cull old results that may not be needed any more */
    public static final String COLUMN_TIMESTAMP = "timestamp";


    public static Uri buildLocationUri(long id) {
        return ContentUris.withAppendedId(CONTENT_URI, id);
    }
}
