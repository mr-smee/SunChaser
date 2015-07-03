package com.example.sunchaser.app.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.sunchaser.R;
import com.example.sunchaser.app.asynctask.LoaderId;
import com.example.sunchaser.app.asynctask.PlaceOfInterestDataLoader;
import com.example.sunchaser.app.data.Place;
import com.example.sunchaser.app.sync.SunChaserSyncAdapter;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlaceOfInterestMapActivityFragment extends Fragment implements OnMapReadyCallback, LoaderManager.LoaderCallbacks<Collection<Place>> {

    private static final String LOG_TAG = PlaceOfInterestDetailActivityFragment.class.getSimpleName();

    public static final String ARGS_KEY_PLACE_IDS = "place_ids";
    public static final String ARGS_KEY_LOCATION_NAME = "location_name";

    private final Map<Marker, Place> places = new HashMap<>();

    private GoogleMap map;
    private Bitmap infoWindowImage;
    private Marker selectedMarker;

    public PlaceOfInterestMapActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.fragment_place_of_interest_map, container, false);

        FragmentManager fragmentManager = getChildFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment) fragmentManager.findFragmentById(R.id.fragment_poi_map);
        mapFragment.getMapAsync(this);

        String locationName = getArguments().getString(ARGS_KEY_LOCATION_NAME);
        getActivity().setTitle(locationName);

        return inflatedView;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (googleMap == null) {
            return;
        }

        map = googleMap;
        map.setMyLocationEnabled(true);

        ViewGroup root = (ViewGroup) getActivity().findViewById(android.R.id.content);
//        map.setInfoWindowAdapter(new SunChaserInfoWindowAdapter(getActivity().getLayoutInflater(), root));
//        map.setOnMarkerClickListener(new SunChaserMarkerClickListener());
        map.setOnInfoWindowClickListener(new PoiMapInfoWindowClickListener());
//        map.setOnMapLongClickListener(new SunChaserMapLongClickListener());

        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        String provider = locationManager.getBestProvider(getSearchProviderCriteria(), true);
        Location location = locationManager.getLastKnownLocation(provider);

        if (location == null) {
            Toast.makeText(getActivity(), "Can't get current location", Toast.LENGTH_SHORT).show();
        } else {
            updateMap(location);
        }
    }

    private Criteria getSearchProviderCriteria() {
        Criteria searchProviderCriteria = new Criteria();

        searchProviderCriteria.setPowerRequirement(Criteria.POWER_LOW);
        searchProviderCriteria.setAccuracy(Criteria.ACCURACY_COARSE);
        searchProviderCriteria.setCostAllowed(false);

        return searchProviderCriteria;
    }

    private void updateMap(Location location) {
        SunChaserSyncAdapter.syncImmediately(getActivity());
        getLoaderManager().initLoader(LoaderId.WEATHER_MAP_LOCATION_AND_WEATHER.getId(), null, this);
    }

    @Override
    public Loader<Collection<Place>> onCreateLoader(int i, Bundle bundle) {

        Bundle arguments = getArguments();
        ArrayList<String> placeIds = arguments.getStringArrayList(ARGS_KEY_PLACE_IDS);

        return new PlaceOfInterestDataLoader(getActivity(), placeIds);
    }

    @Override
    public void onLoadFinished(Loader<Collection<Place>> loader, Collection<Place> locations) {
        addMarkersToMap(locations);
    }

    @Override
    public void onLoaderReset(Loader<Collection<Place>> loader) {
        map.clear();
    }

    private void addMarkersToMap(Collection<Place> places) {
        if (places.isEmpty()) {
            return;
        }

        Log.d(LOG_TAG, "Loaded " + places.size() + " items");

        LatLngBounds.Builder builder = LatLngBounds.builder();

        for (Place place : places) {
            MarkerOptions options = new MarkerOptions().position(place.getLocation())
                    .title(place.getName())
                    //.icon(BitmapDescriptorFactory.fromResource(weatherIconResourceId))
                    .snippet(place.getVicinity());

            Marker marker = map.addMarker(options);

            this.places.put(marker, place);
        }

        if (this.places.size() == 0) {
            return;
        }

        for (Map.Entry<Marker, Place> entry : this.places.entrySet()) {
            Place place = entry.getValue();
            builder.include(place.getLocation());
        }

        LatLngBounds bounds = builder.build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 75);
        map.animateCamera(cameraUpdate);
    }

    private class PoiMapInfoWindowClickListener implements GoogleMap.OnInfoWindowClickListener {

        @Override
        public void onInfoWindowClick(Marker marker) {
            Place place = places.get(marker);
            if (place == null) {
                return;
            }

            Intent intent = new Intent(getActivity(), PlaceOfInterestDetailActivity.class);
            intent.putExtra(PlaceOfInterestDetailActivity.EXTRA_KEY_PLACE_ID, place.getId());
            startActivity(intent);
        }
    }
}
