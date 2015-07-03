package com.example.sunchaser.app.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;

import com.example.sunchaser.R;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by smee on 25/05/15.
 */
public class SharedPreferenceUtils {

    private final static String PREFS_KEY_INTRO_SCREEN_SHOWN = "intro_screen_shown";

    public static boolean hasShownIntroScreen(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.contains(PREFS_KEY_INTRO_SCREEN_SHOWN);
    }

    public static void setHasShownIntroScreen(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit()
                .putBoolean(PREFS_KEY_INTRO_SCREEN_SHOWN, true)
                .commit();
    }

    public static int getDistanceFromLastSyncLocation(Context context, LatLng currentLocation) {
        float defaultLatitude = Float.MIN_VALUE;
        float defaultLongitude = Float.MIN_VALUE;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String latitudeKey = context.getString(R.string.preference_latitude_key);
        if (preferences.contains(latitudeKey)) {
            float latitude = preferences.getFloat(latitudeKey, defaultLatitude);
            float longitude = preferences.getFloat(context.getString(R.string.preference_longitude_key), defaultLongitude);

            // TODO: If we pass in a longer array we'll get a bearing back - might be useful in location detail view?
            float[] distance = new float[1];
            Location.distanceBetween(latitude, longitude, currentLocation.latitude, currentLocation.longitude, distance);

            return (int) distance[0];
        } else {
            return 0;
        }
    }

    public static long getLastSyncTime(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String lastSyncTimeKey = context.getString(R.string.preference_last_sync_time_key);
        return preferences.getLong(lastSyncTimeKey, 0);
    }

    public static boolean hasEverSynced(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String lastSyncTimeKey = context.getString(R.string.preference_last_sync_time_key);
        return preferences.contains(lastSyncTimeKey);
    }

    public static void updateLastSync(Context context, Location currentLocation) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String lastSyncLatitudeKey = context.getString(R.string.preference_latitude_key);
        String lastSyncLongitudeKey = context.getString(R.string.preference_longitude_key);
        String lastSyncTimeKey = context.getString(R.string.preference_last_sync_time_key);

        preferences.edit()
                .putFloat(lastSyncLatitudeKey, (float) currentLocation.getLatitude())
                .putFloat(lastSyncLongitudeKey, (float) currentLocation.getLongitude())
                .putLong(lastSyncTimeKey, System.currentTimeMillis())
                .commit();
    }

    public static LatLng getLastSyncLocation(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String lastSyncLatitudeKey = context.getString(R.string.preference_latitude_key);
        String lastSyncLongitudeKey = context.getString(R.string.preference_longitude_key);

        float latitude = preferences.getFloat(lastSyncLatitudeKey, 0);
        float longitude = preferences.getFloat(lastSyncLongitudeKey, 0);

        return new LatLng(latitude, longitude);
    }
}
