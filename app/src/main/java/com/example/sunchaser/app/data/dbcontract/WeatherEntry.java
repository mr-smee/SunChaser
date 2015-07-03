package com.example.sunchaser.app.data.dbcontract;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by smee on 25/05/15.
 */
public final class WeatherEntry implements BaseColumns {

    public static final Uri CONTENT_URI =
            DbContract.BASE_CONTENT_URI.buildUpon().appendPath(DbContract.PATH_WEATHER).build();

    public static final String CONTENT_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + DbContract.CONTENT_AUTHORITY + "/" + DbContract.PATH_WEATHER;
    public static final String CONTENT_ITEM_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + DbContract.CONTENT_AUTHORITY + "/" + DbContract.PATH_WEATHER;

    public static final String TABLE_NAME = "weather";

    // Column with the foreign key into the location table.
    public static final String COLUMN_LOC_KEY = "location_id";
    // Date, stored as long in milliseconds since the epoch
    public static final String COLUMN_DATE = "date";
    // Weather id as returned by API, to identify the icon to be used
    public static final String COLUMN_WEATHER_ID = "weather_id";

    // Short description and long description of the weather, as provided by API.
    // e.g "clear" vs "sky is clear".
    public static final String COLUMN_SHORT_DESC = "short_desc";

    // Min and max temperatures for the day (stored as floats)
    public static final String COLUMN_MIN_TEMP = "min";
    public static final String COLUMN_MAX_TEMP = "max";

    // Humidity is stored as a float representing percentage
    public static final String COLUMN_HUMIDITY = "humidity";

    // Humidity is stored as a float representing percentage
    public static final String COLUMN_PRESSURE = "pressure";

    // Windspeed is stored as a float representing windspeed  mph
    public static final String COLUMN_WIND_SPEED = "wind";

    // Degrees are meteorological degrees (e.g, 0 is north, 180 is south).  Stored as floats.
    public static final String COLUMN_DEGREES = "degrees";

    public static Uri buildWeatherUri(long id) {
        return ContentUris.withAppendedId(CONTENT_URI, id);
    }

    /*
        Student: Fill in this buildWeatherLocation function
     */
    public static Uri buildWeatherLocation(int locationId) {
        return CONTENT_URI.buildUpon()
                .appendPath(Integer.toString(locationId))
                .build();
    }

    public static Uri buildWeatherLocationWithStartDate(
            String locationSetting, long startDate) {
        long normalizedDate = DbContract.normalizeDate(startDate);
        return CONTENT_URI.buildUpon().appendPath(locationSetting)
                .appendQueryParameter(COLUMN_DATE, Long.toString(normalizedDate)).build();
    }

    public static Uri buildWeatherLocationWithDate(String locationSetting, long date) {
        return CONTENT_URI.buildUpon().appendPath(locationSetting)
                .appendPath(Long.toString(DbContract.normalizeDate(date))).build();
    }

    public static int getLocationSettingFromUri(Uri uri) {
        return Integer.valueOf(uri.getPathSegments().get(1));
    }

    public static long getDateFromUri(Uri uri) {
        return Long.parseLong(uri.getPathSegments().get(2));
    }

    public static long getStartDateFromUri(Uri uri) {
        String dateString = uri.getQueryParameter(COLUMN_DATE);
        if (null != dateString && dateString.length() > 0)
            return Long.parseLong(dateString);
        else
            return 0;
    }
}