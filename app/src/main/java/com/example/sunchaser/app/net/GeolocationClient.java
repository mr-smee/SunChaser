package com.example.sunchaser.app.net;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.util.Log;

import com.example.sunchaser.app.data.GeolocationModel;
import com.example.sunchaser.app.data.dbcontract.GeolocationEntry;
import com.example.sunchaser.app.preferences.SharedPreferenceUtils;
import com.example.sunchaser.app.sync.util.GeoLocationUtil;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

/**
 * Created by smee on 26/05/15.
 */
public class GeolocationClient extends SunChaserHttpRequestClient<List<GeolocationModel>> {

    private static final String NEARBY_PLACES_LOOKUP_FORMAT = "http://api.geonames.org/citiesJSON?";
    private static final int API_CALL_SUCCESS = 200;
    // TODO: Change this so we depend on the search threshold
    private static final int LOCATION_RESYNC_THRESHOLD_METRES = 500;

    private static final int DEFAULT_LOCATIONS_TO_REQUEST = 15;

    private final Context context;
    private final LatLng centre;
    private final boolean forceUpdate;
    private final LatLngBounds lookupBounds;
    private final int locationsToRequest;


    public GeolocationClient(Context context, Location currentLocation) {
        this(context,
                new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                // TODO: Get search radius from preferences
                150,
                DEFAULT_LOCATIONS_TO_REQUEST,
                false);
    }

    public GeolocationClient(Context context, LatLng centre, int radiusKm, int locationsToRequest, boolean forceUpdate) {
        this.context = context;
        this.centre = centre;
        this.forceUpdate = forceUpdate;
        this.lookupBounds = GeoLocationUtil.generateBoundingBox(centre, radiusKm);
        this.locationsToRequest = locationsToRequest;
    }

    @Override
    protected boolean isRequestNecessary() {
        Log.d(LOG_TAG, "Current location: " + centre);

        return (centre != null && (forceUpdate || !isLocationDataStillValid(centre)));
    }

    @Override
    protected URL getRequestUrl() throws MalformedURLException {
        String language = Locale.getDefault().getLanguage();
        // TODO: Make new account with a less stupid name!
        String username = "smee";

        Uri uri = Uri.parse(NEARBY_PLACES_LOOKUP_FORMAT)
                .buildUpon()
                .appendQueryParameter("north", Double.toString(lookupBounds.northeast.latitude))
                .appendQueryParameter("east",  Double.toString(lookupBounds.northeast.longitude))
                .appendQueryParameter("south", Double.toString(lookupBounds.southwest.latitude))
                .appendQueryParameter("west",  Double.toString(lookupBounds.southwest.longitude))
                .appendQueryParameter("lang",  language)
                .appendQueryParameter("maxRows", Integer.toString(locationsToRequest))
                .appendQueryParameter("username", username)
                .build();

        return new URL(uri.toString());
    }

    @Override
    protected List<GeolocationModel> processResponse(String jsonData) {

        ContentResolver contentResolver = context.getContentResolver();

        Vector<ContentValues> cVVector = new Vector<ContentValues>();
        List<GeolocationModel> results = new ArrayList<>();

        try {
            JSONObject geoData = new JSONObject(jsonData);
            if (!geoData.has("geonames")) {
                return getDefaultValue();
            }

            JSONArray geoNames = geoData.getJSONArray("geonames");

            for (int i = 0; i < geoNames.length(); i++) {
                // Values we're going to read out:
                int id;
                double latitude;
                double longitude;
                String name;
                String wikipediaPageName = "";

                // Read the data out...
                JSONObject populationCentreData = geoNames.getJSONObject(i);

                id = populationCentreData.getInt("geonameId");
                latitude = populationCentreData.getDouble("lat");
                longitude = populationCentreData.getDouble("lng");
                name = populationCentreData.getString("name");
                if (populationCentreData.has("wikipedia")) {
                    String wikipediaPageUrl = populationCentreData.getString("wikipedia");
                    Log.d(LOG_TAG, "Wikipedia page URL for " + name + ": " + wikipediaPageUrl);
                    if (wikipediaPageUrl != null && !wikipediaPageUrl.isEmpty()) {
                        if (wikipediaPageUrl.endsWith("/")) {
                            wikipediaPageUrl = wikipediaPageUrl.substring(0, wikipediaPageUrl.length()-1);
                        }
                        wikipediaPageName = wikipediaPageUrl.substring(wikipediaPageUrl.lastIndexOf('/')+1);
                        wikipediaPageName = wikipediaPageName.replaceAll("%2C", ",");
                    } else {
                        // Nothing provided, let's try the place name... cross your fingers!
                        wikipediaPageName = name;
                    }
                    Log.d(LOG_TAG, "Wikipedia page name for " + name + ": " + wikipediaPageName);
                }

                GeolocationModel model = new GeolocationModel(id, new LatLng(latitude, longitude), name, wikipediaPageName, "", Collections.EMPTY_LIST);
                results.add(model);

                ContentValues values = new ContentValues();

                values.put(GeolocationEntry.COLUMN_LOCATION_ID, id);
                values.put(GeolocationEntry.COLUMN_LOCATION_NAME, name);
                values.put(GeolocationEntry.COLUMN_COORD_LAT, latitude);
                values.put(GeolocationEntry.COLUMN_COORD_LONG, longitude);
                values.put(GeolocationEntry.COLUMN_WIKI_PAGE_NAME, wikipediaPageName);

                cVVector.add(values);
            }


        } catch (JSONException e) {
            Log.e(LOG_TAG, "Could not parse JSON response", e);
            return getDefaultValue();
        }

        //  If we've got any new results, clear any old data before inserting the new stuff
        int inserted = 0;
        if (cVVector.size() > 0) {
            ContentValues[] valuesToInsert = cVVector.toArray(new ContentValues[cVVector.size()]);
            inserted = contentResolver.bulkInsert(GeolocationEntry.CONTENT_URI, valuesToInsert);
        }

        Log.d(LOG_TAG, "Fetch complete. " + inserted + " Inserted");

        return results;
    }

    private boolean isLocationDataStillValid(LatLng currentLocation) {
        if (currentLocation == null) {
            return false;
        }

        int distanceFromLastSync = SharedPreferenceUtils.getDistanceFromLastSyncLocation(context, currentLocation);

        return SharedPreferenceUtils.hasEverSynced(context)
                && distanceFromLastSync < LOCATION_RESYNC_THRESHOLD_METRES;
    }

    @Override
    protected List<GeolocationModel> getDefaultValue() {
        return Collections.emptyList();
    }

}
