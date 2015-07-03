package com.example.sunchaser.app.net;

import android.net.Uri;
import android.util.Log;

import com.example.sunchaser.app.data.Photo;
import com.example.sunchaser.app.data.Place;
import com.example.sunchaser.app.data.PlaceType;
import com.example.sunchaser.app.preferences.PlaceOfInterestSearchOptions;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by smee on 02/06/15.
 */
public class PlaceOfInterestClient extends SunChaserHttpRequestClient<PlaceSearchResultPage> {

    public static final String PLACES_API_KEY = "AIzaSyAkvkQm2w_1_TTjvg2eYk5SVQKtOPEdTcY";
    private static final String PLACES_SEARCH_FORMAT = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";

    private final PlaceOfInterestSearchOptions options;
    private final LatLng location;
    private final String pageToken;


    public PlaceOfInterestClient(PlaceOfInterestSearchOptions options, LatLng location, String pageToken) {
        this.options = options;
        this.location = location;
        this.pageToken = pageToken;
    }

    // TODO: Possibly get rid of this from the abstract class and move the checks into the calling classes?
    @Override
    protected boolean isRequestNecessary() {
        return true;
    }

    @Override
    protected URL getRequestUrl() throws MalformedURLException {
        Set<PlaceType> types = options.getAllSelectedPlaceTypes();
        String formattedPlaceTypes = formatPlaceTypes(types);
        Uri uri = Uri.parse(PLACES_SEARCH_FORMAT).buildUpon()
                        .appendQueryParameter("location", location.latitude + "," + location.longitude)
                        .appendQueryParameter("radius", Integer.toString(options.getRadiusMetres()))
                        .appendQueryParameter("types", formattedPlaceTypes)
                        .appendQueryParameter("pagetoken", pageToken)
                        .appendQueryParameter("key", PLACES_API_KEY)
                        .build();
        return new URL(uri.toString());
    }

    @Override
    protected PlaceSearchResultPage processResponse(String responseString) throws JSONException {

        List<Place> places = new ArrayList<Place>();
        String nextPageToken = null;

        try {
            JSONObject resultObject = new JSONObject(responseString);
            JSONArray placesArray = resultObject.getJSONArray("results");

            if (resultObject.has("next_page_token")) {
                nextPageToken = resultObject.getString("next_page_token");
            }

            for (int p=0; p < placesArray.length(); p++) {
                boolean missingValue = false;

                String placeId = "";
                LatLng placeLL = null;
                String placeName = "";
                String vicinity = "";
                String listIcon = "";
                int priceLevel = 0;
                float rating = 0;
                Set<PlaceType> placeTypes = new HashSet<PlaceType>();
                List<Photo> photos = new ArrayList<Photo>();

                try{
                    JSONObject placeObject = placesArray.getJSONObject(p);
                    placeId = placeObject.getString("place_id");

                    JSONObject location = placeObject.getJSONObject("geometry").getJSONObject("location");
                    placeLL = new LatLng(
                            Double.valueOf(location.getString("lat")),
                            Double.valueOf(location.getString("lng")));

                    listIcon = placeObject.getString("icon");

                    JSONArray types = placeObject.getJSONArray("types");
                    for (int index = 0; index < types.length(); index++) {
                        String typeName = types.getString(index);
                        PlaceType type = PlaceType.fromInternalName(typeName);
                        if (type != null) {
                            placeTypes.add(type);
                        }
                    }

                    vicinity = placeObject.getString("vicinity");
                    placeName = placeObject.getString("name");

                    // All essential stuff was found, now check optional data, which may not be in the response
                    if (placeObject.has("price_level")) {
                        priceLevel = placeObject.getInt("price_level");
                    }

                    if (placeObject.has("rating")) {
                        rating = (float) placeObject.getDouble("rating");
                    }

                    if (placeObject.has("photos")) {
                        JSONArray photoArray = placeObject.getJSONArray("photos");
                        for (int index = 0; index < photoArray.length(); index++) {
                            JSONObject photo = photoArray.getJSONObject(index);

                            int height = photo.getInt("height");
                            int width = photo.getInt("width");
                            String photoReference = photo.getString("photo_reference");
                            JSONArray htmlAttrs = photo.getJSONArray("html_attributions");
                            String[] htmlAttributions = new String[htmlAttrs.length()];
                            for (int attrIndex = 0; attrIndex < htmlAttrs.length(); attrIndex++) {
                                htmlAttributions[attrIndex] = htmlAttrs.getString(attrIndex);
                            }

                            photos.add(new Photo(height, width, photoReference, htmlAttributions));
                        }
                        Log.d(LOG_TAG, "Found " + photos.size() + " photos for place " + placeName + " (" + placeId + ")");
                    }
                } catch(JSONException jse){
                    missingValue=true;
                    jse.printStackTrace();
                }

                if (!missingValue) {
                    Place place = new Place(placeId, placeName, placeLL, vicinity, placeTypes, listIcon, priceLevel, rating, photos);
                    Log.d(LOG_TAG, "Parsed new place from JSON: " + place.toString());
                    places.add(place);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new PlaceSearchResultPage(places, pageToken, nextPageToken);
    }

    private String formatPlaceTypes(Set<PlaceType> placeTypes) {
        if (placeTypes == null || placeTypes.size() == 0) {
            return "";
        }

        StringBuilder placeTypeString = new StringBuilder();
        for (PlaceType placeType : placeTypes) {
            if (placeTypeString.length() != 0) {
                placeTypeString.append('|');
            }
            placeTypeString.append(placeType.getInternalName());
        }

        return placeTypeString.toString();
    }

    @Override
    protected PlaceSearchResultPage getDefaultValue() {
        return null;
    }

}
