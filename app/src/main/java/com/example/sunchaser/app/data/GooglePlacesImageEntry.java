package com.example.sunchaser.app.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import com.example.sunchaser.app.data.dbcontract.DbContract;

import java.util.List;

/**
 * Created by smee on 12/06/15.
 */
public class GooglePlacesImageEntry implements BaseColumns {

    public static final Uri CONTENT_URI =
            DbContract.BASE_CONTENT_URI.buildUpon().appendPath(DbContract.PATH_POI_IMAGE).build();

    public static final String CONTENT_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + DbContract.CONTENT_AUTHORITY + "/" + DbContract.PATH_POI_IMAGE;
    public static final String CONTENT_ITEM_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + DbContract.CONTENT_AUTHORITY + "/" + DbContract.PATH_POI_IMAGE;

    public static final String TABLE_NAME = "poi_image";

    public static final String COLUMN_PLACE_ID = "place_id";
    public static final String COLUMN_WIDTH = "width";
    public static final String COLUMN_HEIGHT = "height";
    public static final String COLUMN_REFERENCE = "reference";
    public static final String COLUMN_ATTRIBUTIONS = "attributions";

    /** The time this entry was added, so we can cull old results that may not be needed any more */
    public static final String COLUMN_TIMESTAMP = "timestamp";

    private static final String PACKED_STRING_SEPARATOR = "|";

    public static Uri buildLocationUri(long id) {
        return ContentUris.withAppendedId(CONTENT_URI, id);
    }

    public static String packAttributions(List<String> attributions) {
        if (attributions.size() == 0) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (String attrString : attributions) {
            if (builder.length() != 0) {
                builder.append(PACKED_STRING_SEPARATOR);
            }
            builder.append(attrString);
        }

        return builder.toString();
    }

    public static String[] unpackAttributions(String packedData) {
        if (packedData == null || packedData.isEmpty()) {
            return new String[0];
        }

        return packedData.split(PACKED_STRING_SEPARATOR);
    }
}
