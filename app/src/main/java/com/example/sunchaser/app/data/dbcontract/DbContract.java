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
package com.example.sunchaser.app.data.dbcontract;

import android.net.Uri;
import android.text.format.Time;

/**
 * Defines table and column names for the weather database.
 */
public class DbContract {

    // TODO: Move this into strings.xml?
    public static final String CONTENT_AUTHORITY = "com.example.sunchaser.app";

    // TODO: Tidy this whole package up

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    public static final String PATH_WEATHER = "weather";
    public static final String PATH_LOCATION = "location";
    public static final String PATH_POI = "poi";
    public static final String PATH_POI_SEARCH = "poi_search";
    public static final String PATH_POI_SEARCH_RELATION = "poi_search_relation";
    public static final String PATH_POI_IMAGE = "poi_image";
    public static final String PATH_WIKI = "wiki";
    public static final String PATH_WIKI_IMAGE = "wiki_image";

    // To make it easy to query for the exact date, we normalize all dates that go into
    // the database to the start of the the Julian day at UTC.
    public static long normalizeDate(long startDate) {
        // normalize the start date to the beginning of the (UTC) day
        Time time = new Time();
        time.set(startDate);
        int julianDay = Time.getJulianDay(startDate, time.gmtoff);
        return time.setJulianDay(julianDay);
    }

}