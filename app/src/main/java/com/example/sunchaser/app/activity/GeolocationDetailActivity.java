package com.example.sunchaser.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.example.sunchaser.R;
import com.example.sunchaser.app.data.PlaceTypeGroup;
import com.google.android.gms.maps.model.LatLng;


public class GeolocationDetailActivity extends ActionBarActivity implements GeolocationDetailActivityFragment.CallbackHandler{

    public static final String EXTRA_LOCATION_ID = "locationId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geolocation_detail);
        if (savedInstanceState == null) {

            int locationId = getIntent().getIntExtra(EXTRA_LOCATION_ID, 0);

            Bundle args = new Bundle();
            args.putInt(GeolocationDetailActivityFragment.EXTRA_LOCATION_ID, locationId);
            // TODO: Pass the Uri through instead?
//            args.putParcelable(DetailFragment.DETAIL_URI, getIntent().getData());

            GeolocationDetailActivityFragment detailFragment = new GeolocationDetailActivityFragment();
            detailFragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.geolocation_detail_container, detailFragment)
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_geolocation_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void showAccommodation(LatLng geoLocation, String locationName) {
        Intent intent = new Intent(this, PlaceOfInterestListActivity.class);

        intent.putExtra(PlaceOfInterestListActivity.EXTRA_KEY_LOCATION_NAME, locationName);
        intent.putExtra(PlaceOfInterestListActivity.EXTRA_KEY_LATITUDE, geoLocation.latitude);
        intent.putExtra(PlaceOfInterestListActivity.EXTRA_KEY_LONGITUDE, geoLocation.longitude);
        intent.putExtra(PlaceOfInterestListActivity.EXTRA_KEY_PLACE_TYPES, PlaceTypeGroup.LODGING.getId());

        startActivity(intent);
    }

    @Override
    public void showPlacesOfInterest(LatLng geoLocation, String locationName) {
        Intent intent = new Intent(this, PlaceOfInterestListActivity.class);

        intent.putExtra(PlaceOfInterestListActivity.EXTRA_KEY_LOCATION_NAME, locationName);
        intent.putExtra(PlaceOfInterestListActivity.EXTRA_KEY_LATITUDE, geoLocation.latitude);
        intent.putExtra(PlaceOfInterestListActivity.EXTRA_KEY_LONGITUDE, geoLocation.longitude);

        startActivity(intent);
    }
}
