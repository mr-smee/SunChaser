package com.example.sunchaser.app.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sunchaser.R;
import com.example.sunchaser.app.asynctask.CustomGeolocationLoader;
import com.example.sunchaser.app.asynctask.GeolocationDataLoader;
import com.example.sunchaser.app.asynctask.InfoWindowImageDownloadTask;
import com.example.sunchaser.app.asynctask.LoaderId;
import com.example.sunchaser.app.asynctask.RemoveCustomGeolocationAsyncTask;
import com.example.sunchaser.app.data.GeolocationModel;
import com.example.sunchaser.app.data.WeatherDrawableUtil;
import com.example.sunchaser.app.data.WeatherModel;
import com.example.sunchaser.app.data.WikiImageModel;
import com.example.sunchaser.app.sync.SunChaserSyncAdapter;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * A placeholder fragment containing a simple view.
 */
public class WeatherMapFragment extends Fragment implements OnMapReadyCallback, LoaderManager.LoaderCallbacks<Collection<GeolocationModel>>, InfoWindowImageDownloadTask.ImageDisplay {

    private static final String LOG_TAG = WeatherMapFragment.class.getSimpleName();

    private static final long LOCATION_TIMEOUT_MILLIS = 5 * 60 * 1000;

    private final Map<Marker, GeolocationModel> locationModels = new HashMap<>();
    private final String LOADER_KEY_LONGITUDE = "longitude";
    private final String LOADER_KEY_LATITUDE = "latitude";

    private int lastLoaderId = 1;

    private GoogleMap map;
    private Bitmap infoWindowImage;
    private Marker selectedMarker;
    private CameraUpdate cameraUpdate;
    private View progressIndicator;

    public WeatherMapFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.fragment_weather_map, container, false);

        FragmentManager fragmentManager = getChildFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment) fragmentManager.findFragmentById(R.id.fragment_weather_map);

        mapFragment.getMapAsync(this);

//        final View mapView = mapFragment.getView();
//        if (mapView.getViewTreeObserver().isAlive()) {
//            mapView.getViewTreeObserver().addOnGlobalLayoutListener(
//                    new ViewTreeObserver.OnGlobalLayoutListener() {
//                        @SuppressLint("NewApi") // We check which build version we are using.
//                        @Override
//                        public void onGlobalLayout() {
//                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
//                                mapView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
//                            } else {
//                                mapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//                            }
//                            if (cameraUpdate != null) {
//                                map.moveCamera(cameraUpdate);
//                            }
//                        }
//                    });
//        }

        progressIndicator = inflatedView.findViewById(R.id.weather_map_custom_point_progress);

        return inflatedView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if(googleMap == null) {
            return;
        }

        map = googleMap;
        map.setMyLocationEnabled(true);

        ViewGroup root = (ViewGroup) getActivity().findViewById(android.R.id.content);
        map.setInfoWindowAdapter(new SunChaserInfoWindowAdapter(getActivity().getLayoutInflater(), root));
        map.setOnInfoWindowClickListener(new SunChaserInfoWindowClickListener());
        map.setOnMarkerClickListener(new SunChaserMarkerClickListener());
        map.setOnMarkerDragListener(new SunChaserMarkerDragListener());
        map.setOnMapLongClickListener(new SunChaserMapLongClickListener());

        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        String provider = locationManager.getBestProvider(getSearchProviderCriteria(), true);
        Location location = locationManager.getLastKnownLocation(provider);

        if (location == null) {
            Toast.makeText(getActivity(), "Can't get current location", Toast.LENGTH_SHORT).show();
        } else {
            updateMap(location);
        }
//        if (location == null || isLocationReadingTooOld(location)) {
//            locationManager.requestSingleUpdate(provider, getLocationListener(), null);
//        } else {
//            updateMap(location);
//        }

    }
//TODO: Change this to use Picasso
    private InfoWindowImageDownloadTask.ImageDisplay getImageDisplay() {
        return this;
    }

    @Override
    public void refreshView(Marker marker, Bitmap image) {
        if (marker.equals(selectedMarker) && selectedMarker.isInfoWindowShown()) {
            infoWindowImage = image;
            selectedMarker.showInfoWindow();
        }
    }

    // TODO: Move the next two methods somewhere so they can be shared
//    private boolean isLocationReadingTooOld(Location location) {
//        return System.currentTimeMillis() - location.getTime() >  LOCATION_TIMEOUT_MILLIS;
//    }

    private Criteria getSearchProviderCriteria() {
        Criteria searchProviderCriteria = new Criteria();

        searchProviderCriteria.setPowerRequirement(Criteria.POWER_LOW);
        searchProviderCriteria.setAccuracy(Criteria.ACCURACY_COARSE);
        searchProviderCriteria.setCostAllowed(false);

        return searchProviderCriteria;
    }
//
//    private LocationListener getLocationListener() {
//        LocationListener locationListener = new LocationListener() {
//
//            @Override
//            public void onStatusChanged(String provider, int status, Bundle extras) {
//            }
//
//            @Override
//            public void onProviderEnabled(String provider) {
//            }
//
//            @Override
//            public void onProviderDisabled(String provider) {
//            }
//
//            @Override
//            public void onLocationChanged(Location location) {
//                LocationManager locManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
//                locManager.removeUpdates(this);
//                updateMap(location);
//            }
//        };
//
//        return locationListener;
//    }

    private void updateMap(Location location) {
        SunChaserSyncAdapter.syncImmediately(getActivity());
        getLoaderManager().initLoader(LoaderId.WEATHER_MAP_LOCATION_AND_WEATHER.getId(), null, this);
    }

    @Override
    public Loader<Collection<GeolocationModel>> onCreateLoader(int i, Bundle bundle) {
        LoaderId loaderId = LoaderId.fromId(i);

        if (loaderId == LoaderId.WEATHER_MAP_LOCATION_AND_WEATHER) {
            return new GeolocationDataLoader(getActivity());
        }

        double latitude = bundle.getDouble(LOADER_KEY_LATITUDE);
        double longitude = bundle.getDouble(LOADER_KEY_LONGITUDE);
        return new CustomGeolocationLoader(getActivity(), latitude, longitude);
    }

    @Override
    public void onLoadFinished(Loader<Collection<GeolocationModel>> loader, Collection<GeolocationModel> locations) {
        addMarkersToMap(locations);
    }

    @Override
    public void onLoaderReset(Loader<Collection<GeolocationModel>> loader) {
        map.clear();
    }

    private void addMarkersToMap(Collection<GeolocationModel> locations) {
        if (locations.isEmpty()) {
            return;
        }

        boolean moveCamera = locationModels.isEmpty();

        progressIndicator.setVisibility(View.GONE);

        Log.d(LOG_TAG, "Loaded " + locations.size() + " items");

        LatLngBounds.Builder builder = LatLngBounds.builder();

        for (GeolocationModel locationModel : locations) {
            if (locationModels.containsValue(locationModel)) {
                continue;
            }

            List<WeatherModel> weatherForecast = locationModel.getWeatherForecast();
            if (weatherForecast == null || weatherForecast.isEmpty()) {
                Log.d(LOG_TAG, "Found no weather entries for " + locationModel.getLocationName());
                continue;
            }

            WeatherModel todaysWeather = weatherForecast.get(0);
            int weatherIconResourceId = WeatherDrawableUtil.getIconResourceForWeatherCondition(todaysWeather.getWeatherConditionCode());

            MarkerOptions options = new MarkerOptions().position(locationModel.getLocation())
                                                        .title(locationModel.getLocationName())
                                                        .icon(BitmapDescriptorFactory.fromResource(weatherIconResourceId))
                                                        .snippet(todaysWeather.getWeatherDescription())
                                                        .draggable(true);
            Marker marker = map.addMarker(options);
            locationModels.put(marker, locationModel);
        }

        if (locationModels.size() == 0 || !moveCamera) {
            return;
        }

        for (Map.Entry<Marker, GeolocationModel> entry : locationModels.entrySet()) {
            GeolocationModel locationModel = entry.getValue();
            builder.include(locationModel.getLocation());
        }

        LatLngBounds bounds = builder.build();
        cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 75);
        map.animateCamera(cameraUpdate);
    }

    /**
     * Interface for a class that can show the detail of a selected GeoLocation
     */
    public interface CallbackHandler {
        public void showGeoLocationDetail(int locationId);
    }

    private class SunChaserInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private final LayoutInflater inflater;
        private final ViewGroup root;

        public SunChaserInfoWindowAdapter(LayoutInflater inflater, ViewGroup root) {
            this.inflater = inflater;
            this.root = root;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            // Use default info window style
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            View inflatedView = inflater.inflate(R.layout.fragment_info_window, root, false);

            GeolocationModel model = locationModels.get(marker);
            if (model == null) {
                return null;
            }
            TextView placeNameView = (TextView) inflatedView.findViewById(R.id.info_window_place_name);
            placeNameView.setText(model.getLocationName());

            inflateWeatherForecast(inflatedView, model.getWeatherForecast());

            ImageView imageView = (ImageView) inflatedView.findViewById(R.id.info_window_place_image);
            imageView.setImageBitmap(infoWindowImage);

            if (infoWindowImage == null) {
                return inflatedView;
            }

            int width = infoWindowImage.getWidth();
            int height = infoWindowImage.getHeight();
            int bounding = dpToPx(250);

            float xScale = ((float) bounding) / width;
            float yScale = ((float) bounding) / height;
            float scale = (xScale <= yScale) ? xScale : yScale;

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) imageView.getLayoutParams();
            params.width = (int)(width * scale);
            params.height = (int)(height * scale);
            imageView.setLayoutParams(params);

            return inflatedView;
        }

        // TODO: This is duplicated in ImageViewPagerFragment
        private int dpToPx(int dp) {
            float density = getActivity().getApplicationContext().getResources().getDisplayMetrics().density;
            return Math.round((float)dp * density);
        }

        private void inflateWeatherForecast(View container, List<WeatherModel> weatherForecast) {
            if (weatherForecast != null && !weatherForecast.isEmpty()) {
                LinearLayout weatherSection = (LinearLayout) container.findViewById(R.id.info_window_weather);

                for (WeatherModel weather : weatherForecast) {
                    ImageView imageView = (ImageView) inflater.inflate(R.layout.fragment_info_window_weather, root, false);
                    imageView.setImageResource(WeatherDrawableUtil.getArtResourceForWeatherCondition(weather.getWeatherConditionCode()));
                    weatherSection.addView(imageView);
                }
            }
        }

    }

    private class SunChaserMarkerClickListener implements GoogleMap.OnMarkerClickListener {

        @Override
        public boolean onMarkerClick(Marker marker) {
            if (marker != selectedMarker) {
                infoWindowImage = null;
            }

            selectedMarker = marker;

            GeolocationModel locationModel = locationModels.get(marker);
            if (locationModel == null) {
                return true;
            }

            Set<WikiImageModel> imageInfo = locationModel.getImageInfo();

            if (imageInfo == null || imageInfo.isEmpty()) {
                return false;
            }

            String imageUrl = null;

            for (WikiImageModel imageModel : imageInfo) {
                String imageModelUrl = imageModel.getUrl();
                if (imageModelUrl.toLowerCase().contains(locationModel.getLocationName().toLowerCase())) {
                    // TODO: Can probably add more checks here, but might not be worth it
                    if (imageUrl == null || imageModelUrl.length() < imageUrl.length()) {
                        Log.d(LOG_TAG, "Found candidate for preferred image: " + imageModelUrl);
                        imageUrl = imageModelUrl;
                    }
                }
            }

            if (imageUrl == null) {
                imageUrl = imageInfo.iterator().next().getUrl();
            }

            new InfoWindowImageDownloadTask(marker, getImageDisplay()).execute(imageUrl);

            return false;
        }
    }

    private class SunChaserInfoWindowClickListener implements GoogleMap.OnInfoWindowClickListener {

        @Override
        public void onInfoWindowClick(Marker marker) {
            GeolocationModel locationModel = locationModels.get(marker);
            if (locationModel != null) {
                ((CallbackHandler) getActivity()).showGeoLocationDetail(locationModel.getLocationId());
            }
        }
    }

    private class SunChaserMapLongClickListener implements GoogleMap.OnMapLongClickListener {

        @Override
        public void onMapLongClick(LatLng latLng) {
            Bundle args = new Bundle();
            args.putDouble(LOADER_KEY_LATITUDE, latLng.latitude);
            args.putDouble(LOADER_KEY_LONGITUDE, latLng.longitude);
            getLoaderManager().initLoader(lastLoaderId++, args, WeatherMapFragment.this);
            progressIndicator.setVisibility(View.VISIBLE);
        }
    }

    private class SunChaserMarkerDragListener implements GoogleMap.OnMarkerDragListener {

        private static final int SWIPE_MIN_DISTANCE = 80;   //default is 120
        private static final int SWIPE_THRESHOLD_VELOCITY = 50;

        private Marker beingDragged;
        private LatLng startLocation;
        private long startTime;

        @Override
        public void onMarkerDragStart(Marker marker) {
            if (marker != beingDragged) {
                startLocation = marker.getPosition();
                startTime = System.currentTimeMillis();
                marker.setAlpha(0.5f);
                beingDragged = marker;
            }
        }

        @Override
        public void onMarkerDrag(Marker marker) {
            // TODO: Set alpha so marker fades out?
        }

        @Override
        public void onMarkerDragEnd(Marker marker) {
            Point startPoint = map.getProjection().toScreenLocation(startLocation);
            Point endPoint = map.getProjection().toScreenLocation(marker.getPosition());

            double hChange = endPoint.x - startPoint.x;
            double vChange = endPoint.y - startPoint.y;

            double hSquared = hChange * hChange;
            double vSquared = vChange * vChange;
            double swipeDistance = Math.sqrt(hSquared + vSquared);

            GeolocationModel locationModel = locationModels.get(marker);
            if (swipeDistance < SWIPE_MIN_DISTANCE) {
                marker.setPosition(locationModel.getLocation());
                marker.setAlpha(1f);
                return;
            }

            long dragDuration = System.currentTimeMillis() - startTime;
            double swipeVelocity = 100 * (swipeDistance / dragDuration);

            if (swipeVelocity > SWIPE_THRESHOLD_VELOCITY) {
                marker.remove();
                locationModels.remove(marker);
                // TODO: Kick off task to remove data from database
                new RemoveCustomGeolocationAsyncTask(getActivity(), locationModel.getLocationId()).execute();
            } else {
                marker.setPosition(locationModel.getLocation());
                marker.setAlpha(1f);

            }
        }
    }
}
