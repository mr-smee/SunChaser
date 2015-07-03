package com.example.sunchaser.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.sunchaser.R;
import com.example.sunchaser.app.preferences.SharedPreferenceUtils;
import com.example.sunchaser.app.sync.SunChaserSyncAdapter;
import com.google.android.gms.maps.model.LatLng;


public class MainActivity extends ActionBarActivity implements WeatherMapFragment.CallbackHandler, GeolocationDetailActivityFragment.CallbackHandler {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SunChaserSyncAdapter.initializeSyncAdapter(this);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            // Check persistent storage to see if we've already shown the intro page...

            if (!SharedPreferenceUtils.hasShownIntroScreen(this)) {
                Intent intent = new Intent(this, IntroScreenActivity.class);
                startActivity(intent);
                finish();
            }
        }

        // Always recreate the map fragment, otherwise it has (0,0) size and never gets laid out properly
        WeatherMapFragment mapFragment = new WeatherMapFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.main_container, mapFragment)
                .commit();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void showGeoLocationDetail(int locationId) {
        // TODO: this just works for a single panel layout at the moment...
        Intent intent = new Intent(this, GeolocationDetailActivity.class);
        intent.putExtra(GeolocationDetailActivity.EXTRA_LOCATION_ID, locationId);
        Bundle args = new Bundle();
        startActivity(intent);
    }

    @Override
    public void showAccommodation(LatLng searchCentre, String locationName) {
        // TODO: this doesn't do anything at the moment
        Toast.makeText(this, "Not implemented yet", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showPlacesOfInterest(LatLng searchCentre, String locationName) {
        // TODO: this just works for a single panel layout at the moment...
        Intent intent = new Intent(this, PlaceOfInterestListActivity.class);
        intent.putExtra(PlaceOfInterestListActivity.EXTRA_KEY_LATITUDE, searchCentre.latitude);
        intent.putExtra(PlaceOfInterestListActivity.EXTRA_KEY_LONGITUDE, searchCentre.longitude);
        startActivity(intent);
    }
}
