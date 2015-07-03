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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.sunchaser.app.data.dbcontract.GeolocationEntry;
import com.example.sunchaser.app.data.dbcontract.PlaceOfInterestEntry;
import com.example.sunchaser.app.data.dbcontract.PlaceOfInterestSearchEntry;
import com.example.sunchaser.app.data.dbcontract.PlaceOfInterestSearchResultRelationEntry;
import com.example.sunchaser.app.data.dbcontract.WeatherEntry;
import com.example.sunchaser.app.data.dbcontract.WikiArticleEntry;
import com.example.sunchaser.app.data.dbcontract.WikiImageEntry;

/**
 * Manages a local database for weather data.
 */
public class DbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "sunchaser.db";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_LOCATION_TABLE = "CREATE TABLE " + GeolocationEntry.TABLE_NAME + " (" +
                GeolocationEntry._ID + " INTEGER PRIMARY KEY, " +
                GeolocationEntry.COLUMN_LOCATION_ID + " INTEGER NOT NULL, " +
                GeolocationEntry.COLUMN_LOCATION_NAME + " TEXT NOT NULL, " +
                GeolocationEntry.COLUMN_COORD_LAT + " REAL NOT NULL, " +
                GeolocationEntry.COLUMN_COORD_LONG + " REAL NOT NULL, " +
                GeolocationEntry.COLUMN_WIKI_PAGE_NAME + " TEXT NOT NULL);";

        sqLiteDatabase.execSQL(SQL_CREATE_LOCATION_TABLE);

        final String SQL_CREATE_WEATHER_TABLE = "CREATE TABLE " + WeatherEntry.TABLE_NAME + " (" +
                // Why AutoIncrement here, and not above?
                // Unique keys will be auto-generated in either case.  But for weather
                // forecasting, it's reasonable to assume the user will want information
                // for a certain date and all dates *following*, so the forecast data
                // should be sorted accordingly.
                WeatherEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                // the ID of the location entry associated with this weather data
                WeatherEntry.COLUMN_LOC_KEY + " INTEGER NOT NULL, " +
                WeatherEntry.COLUMN_DATE + " INTEGER NOT NULL, " +
                WeatherEntry.COLUMN_SHORT_DESC + " TEXT NOT NULL, " +
                WeatherEntry.COLUMN_WEATHER_ID + " INTEGER NOT NULL," +

                WeatherEntry.COLUMN_MIN_TEMP + " REAL NOT NULL, " +
                WeatherEntry.COLUMN_MAX_TEMP + " REAL NOT NULL, " +

                WeatherEntry.COLUMN_HUMIDITY + " REAL NOT NULL, " +
                WeatherEntry.COLUMN_PRESSURE + " REAL NOT NULL, " +
                WeatherEntry.COLUMN_WIND_SPEED + " REAL NOT NULL, " +
                WeatherEntry.COLUMN_DEGREES + " REAL NOT NULL, " +

                // Set up the location column as a foreign key to location table.
                " FOREIGN KEY (" + WeatherEntry.COLUMN_LOC_KEY + ") REFERENCES " +
                GeolocationEntry.TABLE_NAME + " (" + GeolocationEntry._ID + "), " +

                // To assure the application have just one weather entry per day
                // per location, it's created a UNIQUE constraint with REPLACE strategy
                " UNIQUE (" + WeatherEntry.COLUMN_DATE + ", " +
                WeatherEntry.COLUMN_LOC_KEY + ") ON CONFLICT REPLACE);";

        sqLiteDatabase.execSQL(SQL_CREATE_WEATHER_TABLE);


        final String SQL_CREATE_POI_SEARCH_TABLE = "CREATE TABLE " + PlaceOfInterestSearchEntry.TABLE_NAME + " (" +
                PlaceOfInterestSearchEntry._ID + " INTEGER PRIMARY KEY, " +
                PlaceOfInterestSearchEntry.COLUMN_COORD_LAT + " TEXT NOT NULL, " +
                PlaceOfInterestSearchEntry.COLUMN_COORD_LON + " TEXT NOT NULL, " +
                PlaceOfInterestSearchEntry.COLUMN_RADIUS_METRES + " INTEGER NOT NULL, " +
                PlaceOfInterestSearchEntry.COLUMN_PLACE_TYPES + " TEXT NOT NULL, " +
                PlaceOfInterestSearchEntry.COLUMN_CURRENT_PAGE_TOKEN + " TEXT, " +
                PlaceOfInterestSearchEntry.COLUMN_NEXT_PAGE_TOKEN + " TEXT, " +
                PlaceOfInterestSearchEntry.COLUMN_TIMESTAMP + " INTEGER NOT NULL);";

        sqLiteDatabase.execSQL(SQL_CREATE_POI_SEARCH_TABLE);


        final String SQL_CREATE_POI_TABLE = "CREATE TABLE " + PlaceOfInterestEntry.TABLE_NAME + " (" +
                PlaceOfInterestEntry._ID + " INTEGER PRIMARY KEY NOT NULL, " +
                PlaceOfInterestEntry.COLUMN_API_ID + " TEXT NOT NULL, " +
                PlaceOfInterestEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                PlaceOfInterestEntry.COLUMN_ICON + " TEXT NOT NULL, " +
                PlaceOfInterestEntry.COLUMN_VICINITY + " TEXT NOT NULL, " +
                PlaceOfInterestEntry.COLUMN_PRICE_LEVEL + " REAL, " +
                PlaceOfInterestEntry.COLUMN_RATING + " REAL, " +
                PlaceOfInterestEntry.COLUMN_COORD_LAT + " REAL NOT NULL, " +
                PlaceOfInterestEntry.COLUMN_COORD_LON + " REAL NOT NULL, " +
                PlaceOfInterestEntry.COLUMN_PLACE_TYPES + " TEXT NOT NULL, " +
                PlaceOfInterestEntry.COLUMN_TIMESTAMP + " INTEGER NOT NULL, " +

                " UNIQUE (" + PlaceOfInterestEntry.COLUMN_API_ID + ") ON CONFLICT REPLACE);";

        sqLiteDatabase.execSQL(SQL_CREATE_POI_TABLE);


        final String SQL_CREATE_POI_SEARCH_RELATION = "CREATE TABLE " + PlaceOfInterestSearchResultRelationEntry.TABLE_NAME + " (" +
                PlaceOfInterestSearchResultRelationEntry._ID + " INTEGER PRIMARY KEY NOT NULL, " +
                PlaceOfInterestSearchResultRelationEntry.COLUMN_POI_SEARCH_ID + " INTEGER NOT NULL, " +
                PlaceOfInterestSearchResultRelationEntry.COLUMN_POI_ID + " TEXT NOT NULL, " +
                PlaceOfInterestSearchResultRelationEntry.COLUMN_TIMESTAMP + " INTEGER NOT NULL, " +

                // Set up the location column as a foreign key to location table.
                " FOREIGN KEY (" + PlaceOfInterestSearchResultRelationEntry.COLUMN_POI_SEARCH_ID + ") REFERENCES " +
                PlaceOfInterestSearchEntry.TABLE_NAME + " (" + PlaceOfInterestSearchEntry._ID + "), " +

                " FOREIGN KEY (" + PlaceOfInterestSearchResultRelationEntry.COLUMN_POI_ID + ") REFERENCES " +
                PlaceOfInterestEntry.TABLE_NAME + " (" + PlaceOfInterestEntry.COLUMN_API_ID + ")); ";

        sqLiteDatabase.execSQL(SQL_CREATE_POI_SEARCH_RELATION);


        final String SQL_CREATE_WIKI_ARTICLE_TABLE = "CREATE TABLE " + WikiArticleEntry.TABLE_NAME + " (" +
                WikiArticleEntry._ID + " INTEGER PRIMARY KEY NOT NULL, " +
                WikiArticleEntry.COLUMN_ARTICLE_ID + " TEXT NOT NULL, " +
                WikiArticleEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                WikiArticleEntry.COLUMN_EXTRACT + " TEXT NOT NULL, " +
                WikiArticleEntry.COLUMN_IMAGES + " TEXT, " +
                WikiArticleEntry.COLUMN_TIMESTAMP + " INTEGER NOT NULL, " +

                " UNIQUE (" + WikiArticleEntry.COLUMN_TITLE + ") ON CONFLICT REPLACE);";

        sqLiteDatabase.execSQL(SQL_CREATE_WIKI_ARTICLE_TABLE);


        final String SQL_CREATE_WIKI_IMAGE_TABLE = "CREATE TABLE " + WikiImageEntry.TABLE_NAME + " (" +
                WikiImageEntry._ID + " INTEGER PRIMARY KEY NOT NULL, " +
                WikiImageEntry.COLUMN_FILENAME + " TEXT NOT NULL, " +
                WikiImageEntry.COLUMN_WIDTH + " INTEGER NOT NULL, " +
                WikiImageEntry.COLUMN_HEIGHT + " INTEGER NOT NULL, " +
                WikiImageEntry.COLUMN_URL + " TEXT NOT NULL, " +
                WikiImageEntry.COLUMN_MIME_TYPE + " TEXT NOT NULL, " +
                WikiImageEntry.COLUMN_THUMBNAIL_URL + " TEXT NOT NULL, " +
                WikiImageEntry.COLUMN_THUMBNAIL_MIME_TYPE + " TEXT NOT NULL, " +
                WikiImageEntry.COLUMN_TIMESTAMP + " INTEGER NOT NULL, " +

                " UNIQUE (" +  WikiImageEntry.COLUMN_FILENAME + ") ON CONFLICT REPLACE);";

        sqLiteDatabase.execSQL(SQL_CREATE_WIKI_IMAGE_TABLE);


        final String SQL_CREATE_POI_IMAGE_TABLE = "CREATE TABLE " + GooglePlacesImageEntry.TABLE_NAME + " (" +
                GooglePlacesImageEntry._ID + " INTEGER PRIMARY KEY NOT NULL, " +
                GooglePlacesImageEntry.COLUMN_PLACE_ID + " TEXT NOT NULL, " +
                GooglePlacesImageEntry.COLUMN_WIDTH + " INTEGER NOT NULL, " +
                GooglePlacesImageEntry.COLUMN_HEIGHT + " INTEGER NOT NULL, " +
                GooglePlacesImageEntry.COLUMN_REFERENCE + " TEXT NOT NULL, " +
                GooglePlacesImageEntry.COLUMN_ATTRIBUTIONS + " TEXT NOT NULL, " +
                GooglePlacesImageEntry.COLUMN_TIMESTAMP + " INTEGER NOT NULL, " +

                " FOREIGN KEY (" + GooglePlacesImageEntry.COLUMN_PLACE_ID + ") REFERENCES " +
                    PlaceOfInterestSearchEntry.TABLE_NAME + " (" + PlaceOfInterestEntry.COLUMN_API_ID + "), " +

                " UNIQUE (" +  GooglePlacesImageEntry.COLUMN_REFERENCE + ") ON CONFLICT REPLACE);";

        sqLiteDatabase.execSQL(SQL_CREATE_POI_IMAGE_TABLE);


    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + GeolocationEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + WeatherEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PlaceOfInterestSearchResultRelationEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PlaceOfInterestSearchEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PlaceOfInterestEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + GooglePlacesImageEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + WikiArticleEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + WikiImageEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}