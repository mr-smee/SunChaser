package com.example.sunchaser.app.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.example.sunchaser.R;

public class PlaceOfInterestListActivity extends ActionBarActivity {

    public static final String EXTRA_KEY_LOCATION_NAME = "location_name";
    public static final String EXTRA_KEY_LATITUDE = "latitude";
    public static final String EXTRA_KEY_LONGITUDE = "longitude";
    public static final String EXTRA_KEY_PLACE_TYPES = "place_types";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_of_interest_list);

        if (savedInstanceState == null) {

            double latitude = getIntent().getDoubleExtra(EXTRA_KEY_LATITUDE, 0);
            double longitude = getIntent().getDoubleExtra(EXTRA_KEY_LONGITUDE, 0);
            int placeTypeGroupId = getIntent().getIntExtra(EXTRA_KEY_PLACE_TYPES, -1);
            String locationName = getIntent().getStringExtra(EXTRA_KEY_LOCATION_NAME);

            Bundle args = new Bundle();
            args.putString(PlaceOfInterestListActivityFragment.ARGS_KEY_LOCATION_NAME, locationName);
            args.putDouble(PlaceOfInterestListActivityFragment.ARGS_KEY_LATITUDE, latitude);
            args.putDouble(PlaceOfInterestListActivityFragment.ARGS_KEY_LONGITUDE, longitude);
            if (placeTypeGroupId != -1) {
                args.putInt(PlaceOfInterestListActivityFragment.ARGS_KEY_PLACE_TYPES, placeTypeGroupId);
            }

            PlaceOfInterestListActivityFragment fragment = new PlaceOfInterestListActivityFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.place_list_container, fragment)
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_place_of_interest_list, menu);
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
}
