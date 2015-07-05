package com.example.sunchaser.app.activity;

import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sunchaser.R;
import com.example.sunchaser.app.asynctask.GeolocationDataLoader;
import com.example.sunchaser.app.asynctask.LoaderId;
import com.example.sunchaser.app.data.DateUtil;
import com.example.sunchaser.app.data.GeolocationModel;
import com.example.sunchaser.app.data.TemperatureUtil;
import com.example.sunchaser.app.data.WeatherDrawableUtil;
import com.example.sunchaser.app.data.WeatherModel;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 */
public class GeolocationDetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Collection<GeolocationModel>> {

    private static final String LOG_TAG = GeolocationDetailActivityFragment.class.getSimpleName();

    public static final String EXTRA_LOCATION_ID = "locationId";
    public static final String SAVED_STATE_KEY_PAGER_FRAGMENT_ID = "fragment_id";

    private static final String MAPS_URI_FORMAT = "google.navigation:";
    private static final long LOCATION_TIMEOUT_MILLIS = 5 * 60 * 1000;

    private View weatherAttribution;
    private View wikiExtractToggle;
    private TextView wikiExtractToggleText;
    private TextView wikiExtract;
    private View wikiAttribution;
    private View directionsButton;
    private View accommodationButton;
    private View placesOfInterestButton;
    private Location userCurrentLocation;
    private LatLng thisLocation;
    private String locationName;
    private View fragment;
    private LayoutInflater inflater;
    private ShareActionProvider shareActionProvider;
    private Fragment pagerFragment;

    public GeolocationDetailActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(LoaderId.GEOLOCATION_DETAIL_LOCATION.getId(), null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.inflater = inflater;
        fragment = inflater.inflate(R.layout.fragment_geolocation_detail, container, false);

        weatherAttribution = fragment.findViewById(R.id.location_detail_weather_attribution);
        wikiExtractToggle = fragment.findViewById(R.id.location_detail_wiki_extract_toggle);
        wikiExtractToggleText = (TextView)fragment.findViewById(R.id.location_detail_wiki_extract_toggle_text);
        wikiExtract = (TextView)fragment.findViewById(R.id.location_detail_wiki_extract);
        wikiAttribution = fragment.findViewById(R.id.location_detail_wiki_attribution);
        directionsButton = fragment.findViewById(R.id.location_detail_get_directions);
        accommodationButton = fragment.findViewById(R.id.location_detail_accommodation);
        placesOfInterestButton = fragment.findViewById(R.id.location_detail_places_of_interest);

        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        String provider = locationManager.getBestProvider(getSearchProviderCriteria(), true);
        userCurrentLocation = locationManager.getLastKnownLocation(provider);

        if (userCurrentLocation == null || isLocationReadingTooOld(userCurrentLocation)) {
            locationManager.requestSingleUpdate(provider, getLocationListener(), null);
        }

        if (savedInstanceState == null) {
            pagerFragment = new ImageViewPagerFragment();
            getActivity().getSupportFragmentManager().beginTransaction()
                    .add(R.id.location_detail_image_container, pagerFragment)
                    .commit();
        } else {
            int pagerFragmentId = savedInstanceState.getInt(SAVED_STATE_KEY_PAGER_FRAGMENT_ID);
            pagerFragment = getActivity().getSupportFragmentManager().findFragmentById(pagerFragmentId);
        }

        return fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(SAVED_STATE_KEY_PAGER_FRAGMENT_ID, pagerFragment.getId());
        super.onSaveInstanceState(outState);
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
        LocationListener locationListener = new LocationListener() {

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
                LocationManager locManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                locManager.removeUpdates(this);
                setDirectionButtonOnClickListener();
            }
        };

        return locationListener;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_geolocation_detail_fragment, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);
        shareActionProvider = (ShareActionProvider) (MenuItemCompat.getActionProvider(menuItem));
        updateShareActionListener();
    }

    private void updateShareActionListener() {
        if (shareActionProvider != null) {
            shareActionProvider.setShareIntent(createSharePlaceDetailIntent());
        }
    }

    private Intent createSharePlaceDetailIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        String contentString = getActivity().getString(R.string.format_social_text, locationName);

        shareIntent.putExtra(Intent.EXTRA_TEXT, contentString);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");

        return shareIntent;
    }

    private void setWeatherAttributionOnClickListener() {
        weatherAttribution.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri webpage = Uri.parse("http://www.openweathermap.org");
                Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });
    }

    private void setWikiExtractToggleOnClickListener() {
        wikiExtractToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (wikiExtract.getVisibility() == View.VISIBLE) {
                    // TODO: maybe set min height and just show the start of the extract?
                    wikiExtract.setVisibility(View.GONE);
                    wikiAttribution.setVisibility(View.GONE);
                    wikiExtractToggleText.setText(getString(R.string.location_detail_wiki_extract_show));
                } else {
                    wikiExtract.setVisibility(View.VISIBLE);
                    wikiAttribution.setVisibility(View.VISIBLE);
                    wikiExtractToggleText.setText(getString(R.string.location_detail_wiki_extract_hide));
                }
            }
        });
    }

    private void setDirectionButtonOnClickListener() {

        directionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Uri uri = Uri.parse(MAPS_URI_FORMAT).buildUpon().appendQueryParameter("q", locationName).build();
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, uri);

                if (mapIntent.resolveActivity(getActivity().getPackageManager()) == null) {
                    String message = getString(R.string.no_map_app_installed);
                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                    return;
                }

                startActivity(mapIntent);
            }
        });

    }

    private void setAccommodationButtonOnClickListener() {
        accommodationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((CallbackHandler)getActivity()).showAccommodation(thisLocation, locationName);
            }
        });
    }

    private void setPlacesOfInterestButtonOnClickListener() {
        placesOfInterestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((CallbackHandler)getActivity()).showPlacesOfInterest(thisLocation, locationName);
            }
        });
    }

    @Override
    public Loader<Collection<GeolocationModel>> onCreateLoader(int id, Bundle bundle) {
        Bundle args = getArguments();
        int locationId = args.getInt(EXTRA_LOCATION_ID);
        return new GeolocationDataLoader(getActivity(), locationId);
    }

    @Override
    public void onLoadFinished(Loader<Collection<GeolocationModel>> loader, Collection<GeolocationModel> locationModels) {

        if (locationModels.isEmpty()) {
            return;
        }
        List<GeolocationModel> modelList = new ArrayList<>(locationModels);
        // TODO Should only be one entry here
        GeolocationModel geolocationModel = modelList.get(0);

        locationName = geolocationModel.getLocationName();
        thisLocation = geolocationModel.getLocation();
        getActivity().setTitle(geolocationModel.getLocationName());

        inflateWikiContent(geolocationModel.getExtractTitle(), geolocationModel.getExtract());
        inflateWeatherForecast(geolocationModel.getWeatherForecast());

        setWeatherAttributionOnClickListener();
        setWikiExtractToggleOnClickListener();
        setDirectionButtonOnClickListener();
        setAccommodationButtonOnClickListener();
        setPlacesOfInterestButtonOnClickListener();
        updateShareActionListener();


        ((ImageViewPagerFragment)pagerFragment).setImageInfo(geolocationModel.getImageInfo());
    }

    private void inflateWeatherForecast(List<WeatherModel> weatherForecast) {
        ViewGroup forecastContainer = (ViewGroup)fragment.findViewById(R.id.location_detail_weather_forecast);

        for (WeatherModel model : weatherForecast) {
            View inflatedView = inflater.inflate(R.layout.fragment_detail_weather, forecastContainer, false);

            TextView date = (TextView) inflatedView.findViewById(R.id.weatherforecast_date);
            date.setText(DateUtil.getFriendlyDayString(getActivity(), model.getDate()));

            ImageView weatherIcon = (ImageView) inflatedView.findViewById(R.id.weatherForecast_icon);
            weatherIcon.setImageResource(WeatherDrawableUtil.getArtResourceForWeatherCondition(model.getWeatherConditionCode()));

            TextView maxTemperature = (TextView) inflatedView.findViewById(R.id.weatherForecast_temperatureMax);
            maxTemperature.setText(TemperatureUtil.formatTemperature(getActivity(), model.getMaxTemperature()));

            TextView minTemperature = (TextView) inflatedView.findViewById(R.id.weatherForecast_temperatureMin);
            minTemperature.setText(TemperatureUtil.formatTemperature(getActivity(), model.getMinTemperature()));

            forecastContainer.addView(inflatedView);
        }
    }

    private void inflateWikiContent(final String wikiPageTitle, String wikiExtractString) {
        wikiExtract.setText(wikiExtractString);
        wikiAttribution.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri webpage = Uri.parse("http://en.wikipedia.org/wiki").buildUpon()
                        .appendPath(wikiPageTitle)
                        .build();
                Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    public void onLoaderReset(Loader<Collection<GeolocationModel>> loader) {

    }

    public static interface CallbackHandler {
        public abstract void showAccommodation(LatLng geoLocation, String locationName);
        public abstract void showPlacesOfInterest(LatLng geoLocation, String locationName);
    }
}
