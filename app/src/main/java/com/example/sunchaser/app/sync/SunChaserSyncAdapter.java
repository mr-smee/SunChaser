package com.example.sunchaser.app.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;

import com.example.sunchaser.R;
import com.example.sunchaser.app.data.GeolocationModel;
import com.example.sunchaser.app.data.WikiArticle;
import com.example.sunchaser.app.data.dbcontract.GeolocationEntry;
import com.example.sunchaser.app.data.dbcontract.PlaceOfInterestEntry;
import com.example.sunchaser.app.data.dbcontract.PlaceOfInterestSearchEntry;
import com.example.sunchaser.app.data.dbcontract.PlaceOfInterestSearchResultRelationEntry;
import com.example.sunchaser.app.data.dbcontract.WeatherEntry;
import com.example.sunchaser.app.data.dbcontract.WikiArticleEntry;
import com.example.sunchaser.app.net.GeolocationClient;
import com.example.sunchaser.app.net.WeatherClient;
import com.example.sunchaser.app.net.WikipediaArticleClient;
import com.example.sunchaser.app.net.WikipediaImageInfoClient;
import com.example.sunchaser.app.preferences.SharedPreferenceUtils;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SunChaserSyncAdapter extends AbstractThreadedSyncAdapter {

    public static final String BROADCAST_TAG_SYNC_FINISHED = "sunchaser_sync_finished";
    public static final String EXTRA_KEY_PROGRESS = "sync_progress";
    public static final int EXTRA_PROGRESS_FETCHING_GEOLOCATIONS =  0;
    public static final int EXTRA_PROGRESS_FETCHING_WEATHER =       1;
    public static final int EXTRA_PROGRESS_FETCHING_WIKI_ARTICLES = 2;
    public static final int EXTRA_PROGRESS_FETCHING_WIKI_IMAGES =   4;
    public static final int EXTRA_PROGRESS_FINISHED =               8;


    public final String LOG_TAG = SunChaserSyncAdapter.class.getSimpleName();

    private static final long LOCATION_TIMEOUT_MILLIS = 5 * 60 * 1000;

    // Interval at which to sync with the weather, in seconds.
    // 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;

    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    private static final int WEATHER_NOTIFICATION_ID = 3004;

    private final String[] LOCATION_COLUMNS = {
            GeolocationEntry.COLUMN_LOCATION_ID,
            GeolocationEntry.COLUMN_LOCATION_NAME,
            GeolocationEntry.COLUMN_COORD_LAT,
            GeolocationEntry.COLUMN_COORD_LONG,
            GeolocationEntry.COLUMN_WIKI_PAGE_NAME
    };

    private static final int COLUMN_INDEX_ID =         0;
    private static final int COLUMN_INDEX_NAME =       1;
    private static final int COLUMN_INDEX_LAT =        2;
    private static final int COLUMN_INDEX_LONG =       3;
    private static final int COLUMN_INDEX_WIKI_PAGE =  4;

    private final String[] WIKI_COLUMNS = {
            WikiArticleEntry.COLUMN_ARTICLE_ID,
            WikiArticleEntry.COLUMN_IMAGES,
    };

    private static final int WIKI_COLUMN_INDEX_ID =     0;
    private static final int WIKI_COLUMN_INDEX_IMAGES = 1;

    private static final String[] NOTIFY_WEATHER_PROJECTION = new String[] {
            WeatherEntry.COLUMN_WEATHER_ID,
            WeatherEntry.COLUMN_MAX_TEMP,
            WeatherEntry.COLUMN_MIN_TEMP,
            WeatherEntry.COLUMN_SHORT_DESC
    };
    // these indices must match the projection
    private static final int INDEX_WEATHER_ID = 0;
    private static final int INDEX_MAX_TEMP = 1;
    private static final int INDEX_MIN_TEMP = 2;
    private static final int INDEX_SHORT_DESC = 3;

    public SunChaserSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "onPerformSync Called.");

        LocationManager locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        String locationProvider = locationManager.getBestProvider(getSearchProviderCriteria(), true);
        Location location = locationManager.getLastKnownLocation(locationProvider);

        if (location == null || isLocationReadingTooOld(location)) {
            locationManager.requestSingleUpdate(locationProvider, getLocationListener(), null);
            return;
        }

        syncData(location);
    }

    private void syncData(Location location) {

        // TODO: Make this better... if the first client call succeeds but the second doesn't, what do we do?
        Context context = getContext();
        sendProgressBroadcast(EXTRA_PROGRESS_FETCHING_GEOLOCATIONS);
        // TODO: Since we've had them passed back it's probably safe to assume these have been saved...
        List<GeolocationModel> insertedNewLocations = new GeolocationClient(context, location).makeRequest();

        // TODO: Change this to make it less confusing.

        // Load all nearby locations
        Uri locationLoadUri = GeolocationEntry.CONTENT_URI;
        Cursor locationCursor = null;

        boolean dataRefreshed = false;

        if (!insertedNewLocations.isEmpty()) {
            locationCursor = context.getContentResolver().query(locationLoadUri, LOCATION_COLUMNS, null, null, null);

            sendProgressBroadcast(EXTRA_PROGRESS_FETCHING_WIKI_ARTICLES);
            fetchWikiArticles(locationCursor);

            Uri wikiLoadUri = WikiArticleEntry.CONTENT_URI;
            Cursor wikiArticleCursor = context.getContentResolver().query(wikiLoadUri, WIKI_COLUMNS, null, null, null);

            sendProgressBroadcast(EXTRA_PROGRESS_FETCHING_WIKI_IMAGES);
            fetchWikiImages(wikiArticleCursor);

            dataRefreshed = true;
        }

        if (dataRefreshed || weatherDataOutOfDate()) {
            if (locationCursor == null) {
                locationCursor = context.getContentResolver().query(locationLoadUri, LOCATION_COLUMNS, null, null, null);
            }

            sendProgressBroadcast(EXTRA_PROGRESS_FETCHING_WEATHER);
            fetchWeather(locationCursor);

            dataRefreshed = true;
        }


        if (dataRefreshed) {
            SharedPreferenceUtils.updateLastSync(context, location);
            // TODO: Prune locations and wiki data? Could do it the same way as with POI data
            pruneOldWeatherData();
            prunePlaceOfInterestData();
        }

        sendProgressBroadcast(EXTRA_PROGRESS_FINISHED);
    }

    private void sendProgressBroadcast(int progress) {
        Intent intent = new Intent(BROADCAST_TAG_SYNC_FINISHED);
        intent.putExtra(EXTRA_KEY_PROGRESS, progress);
        getContext().sendBroadcast(intent);
    }

    private void pruneOldWeatherData() {
        String whereString = WeatherEntry.COLUMN_DATE + " <= ?";

        Time dayTime = new Time();
        dayTime.setToNow();

        // we start at the day returned by local time. Otherwise this is a mess.
        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

        // now we work exclusively in UTC
        dayTime = new Time();
        getContext().getContentResolver().delete(WeatherEntry.CONTENT_URI, whereString, new String[]{Long.toString(dayTime.setJulianDay(julianStartDay - 1))});
    }

    private void prunePlaceOfInterestData() {
        Time dayTime = new Time();
        dayTime.setToNow();

        // we start at the day returned by local time. Otherwise this is a mess.
        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

        // now we work exclusively in UTC
        dayTime = new Time();

        ContentResolver contentResolver = getContext().getContentResolver();

        String searchRelationWhereString = PlaceOfInterestSearchResultRelationEntry.COLUMN_TIMESTAMP + " < ?";
        int deletedResults = contentResolver.delete(PlaceOfInterestSearchResultRelationEntry.CONTENT_URI,
                searchRelationWhereString,
                new String[] {
                        Long.toString(dayTime.setJulianDay(julianStartDay-1))
                });
        Log.d(LOG_TAG, "Deleted " + deletedResults + " search result sets");

        String searchWhereString = PlaceOfInterestSearchEntry.COLUMN_TIMESTAMP + " < ?";
        int deletedSearches = contentResolver.delete(PlaceOfInterestSearchEntry.CONTENT_URI,
                searchWhereString,
                new String[]{
                        Long.toString(dayTime.setJulianDay(julianStartDay - 1))
                });
        Log.d(LOG_TAG, "Deleted " + deletedSearches + " searches");

        // Don't delete PlaceOfInterestEntries unless there are no searches that reference them...
        String placeWhereString = "NOT EXISTS (" +
                "SELECT 1 FROM " + PlaceOfInterestSearchResultRelationEntry.TABLE_NAME +
                " WHERE " + PlaceOfInterestSearchResultRelationEntry.COLUMN_POI_ID + " = " + PlaceOfInterestEntry.COLUMN_API_ID +
                ")";
        int deletedPlaces = contentResolver.delete(PlaceOfInterestEntry.CONTENT_URI, placeWhereString, null);
        Log.d(LOG_TAG, "Deleted " + deletedPlaces + " places of interest");
    }

    private boolean weatherDataOutOfDate() {
        long now = System.currentTimeMillis();
        long lastSyncTime = SharedPreferenceUtils.getLastSyncTime(getContext());
        long syncFrequencyMillis = SYNC_FLEXTIME * 1000;

        Log.d(LOG_TAG, "Current time: " + now);
        Log.d(LOG_TAG, "Last sync time: " + lastSyncTime);
        Log.d(LOG_TAG, "Sync frequency: " + syncFrequencyMillis);
        Log.d(LOG_TAG, "Time since last sync: " + (now - lastSyncTime));

        return now - lastSyncTime > syncFrequencyMillis;
    }

    private boolean fetchWikiArticles(Cursor locationCursor) {
        if (!locationCursor.moveToFirst()) {
            return false;
        }

        List<String> pageTitles = new ArrayList<>();

        do {
            pageTitles.add(locationCursor.getString(COLUMN_INDEX_WIKI_PAGE));
        } while (locationCursor.moveToNext());

        new WikipediaArticleClient(getContext(), pageTitles).makeRequest();

        return true;
    }

    private boolean fetchWikiImages(Cursor wikiArticleCursor) {
        if (!wikiArticleCursor.moveToFirst()) {
            return false;
        }

        Set<String> imageFileNames = new HashSet<>();

        do {
            String packedImageNames = wikiArticleCursor.getString(WIKI_COLUMN_INDEX_IMAGES);
            imageFileNames.addAll(WikiArticleEntry.unpackImageFilenames(packedImageNames));
        } while (wikiArticleCursor.moveToNext());
        
        new WikipediaImageInfoClient(getContext(), imageFileNames).makeRequest();
        return true;
    }

    private boolean fetchWeather(Cursor locationCursor) {
        if (!locationCursor.moveToFirst()) {
            return false;
        };

        do {
            double latitude = locationCursor.getFloat(COLUMN_INDEX_LAT);
            double longitude = locationCursor.getFloat(COLUMN_INDEX_LONG);
            int locationId = locationCursor.getInt(COLUMN_INDEX_ID);

            LatLng location = new LatLng(latitude, longitude);
            new WeatherClient(getContext(), location, locationId).makeRequest();

        } while (locationCursor.moveToNext());

        return true;
    }

    private boolean isLocationReadingTooOld(Location location) {
        return System.currentTimeMillis() - location.getTime() >  LOCATION_TIMEOUT_MILLIS;
    }

    private Criteria getSearchProviderCriteria() {
        Criteria searchProviderCriteria = new Criteria();

        searchProviderCriteria.setPowerRequirement(Criteria.POWER_LOW);
        searchProviderCriteria.setAccuracy(Criteria.ACCURACY_COARSE);
        searchProviderCriteria.setCostAllowed(false);

        return searchProviderCriteria;
    }

    private LocationListener getLocationListener() {
        return new LocationListener() {

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }

            @Override
            public void onLocationChanged(Location location) {
                LocationManager locManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
                locManager.removeUpdates(this);
                syncData(location);
            }
        };
    }

//    /**
//     * Helper method to handle insertion of a new location in the weather database.
//     *
//     * @param locationSetting The location string used to request updates from the server.
//     * @param cityName        A human-readable city name, e.g "Mountain View"
//     * @param lat             the latitude of the city
//     * @param lon             the longitude of the city
//     * @return the row ID of the added location.
//     */
//    long addLocation(String locationSetting, String cityName, double lat, double lon) {
//        long locationId;
//
//        // Students: First, check if the location with this city name exists in the db
//        // If it exists, return the current ID
//        Cursor locationCursor = getContext().getContentResolver().query(GeolocationEntry.CONTENT_URI,
//                new String[]{GeolocationEntry._ID},
//                GeolocationEntry.COLUMN_WIKI_PAGE_NAME + " = ?",
//                new String[]{locationSetting},
//                null);
//
//        if (locationCursor.moveToFirst()) {
//            int locationIdIndex = locationCursor.getColumnIndex(GeolocationEntry._ID);
//            locationId = locationCursor.getLong(locationIdIndex);
//        } else {
//
//            // Otherwise, insert it using the content resolver and the base URI
//            ContentValues values = new ContentValues();
//            values.put(GeolocationEntry.COLUMN_WIKI_PAGE_NAME, locationSetting);
//            values.put(GeolocationEntry.COLUMN_LOCATION_NAME, cityName);
//            values.put(GeolocationEntry.COLUMN_COORD_LAT, lat);
//            values.put(GeolocationEntry.COLUMN_COORD_LONG, lon);
//
//            Uri insertedRowUri = getContext().getContentResolver().insert(GeolocationEntry.CONTENT_URI, values);
//
//            locationId = ContentUris.parseId(insertedRowUri);
//        }
//        return locationId;
//    }



    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }


    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        SunChaserSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    private void notifyWeather() {
//        Context context = getContext();
//        //checking the last update and notify if it' the first of the day
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
//        String lastNotificationKey = context.getString(R.string.pref_last_notification);
//        long lastSync = prefs.getLong(lastNotificationKey, 0);
//
//        String allowNotificationKey = context.getString(R.string.pref_notifications_key);
//        boolean allowNotifications = prefs.getBoolean(allowNotificationKey, Boolean.parseBoolean(context.getString(R.string.pref_notifications_default)));
//
//        if (allowNotifications && (System.currentTimeMillis() - lastSync >= DAY_IN_MILLIS)) {
//            // Last sync was more than 1 day ago, let's send a notification with the weather.
//            String locationQuery = Utility.getPreferredLocation(context);
//
//            Uri weatherUri = WeatherEntry.buildWeatherLocationWithDate(locationQuery, System.currentTimeMillis());
//
//            // we'll query our contentProvider, as always
//            Cursor cursor = context.getContentResolver().query(weatherUri, NOTIFY_WEATHER_PROJECTION, null, null, null);
//
//            if (cursor.moveToFirst()) {
//                int weatherId = cursor.getInt(INDEX_WEATHER_ID);
//                double high = cursor.getDouble(INDEX_MAX_TEMP);
//                double low = cursor.getDouble(INDEX_MIN_TEMP);
//                String desc = cursor.getString(INDEX_SHORT_DESC);
//
//                int iconId = Utility.getIconResourceForWeatherCondition(weatherId);
//                String title = context.getString(R.string.app_name);
//
//                // Define the text of the forecast.
//                String contentText = String.format(context.getString(R.string.format_notification),
//                        desc,
//                        Utility.formatTemperature(context, high),
//                        Utility.formatTemperature(context, low));
//
//                //build your notification here.
//                Intent intent = new Intent(context, DetailActivity.class).setData(weatherUri);
//                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
//                stackBuilder.addParentStack(DetailActivity.class);
//                stackBuilder.addNextIntent(intent);
//
//                PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
//
//                Notification notification = new NotificationCompat.Builder(context)
//                        .setSmallIcon(iconId)
//                        .setContentTitle(title)
//                        .setContentText(contentText)
//                        .setContentIntent(pendingIntent)
//                        .build();
//
//
//                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//                notificationManager.notify(WEATHER_NOTIFICATION_ID, notification);
//
//                //refreshing last sync
//                SharedPreferences.Editor editor = prefs.edit();
//                editor.putLong(lastNotificationKey, System.currentTimeMillis());
//                editor.commit();
//            }
//        }

    }

}