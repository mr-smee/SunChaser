package com.example.sunchaser.app.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.example.sunchaser.R;

import java.util.ArrayList;

public class PlaceOfInterestMapActivity extends ActionBarActivity {

    public static final String EXTRA_KEY_LOCATION_NAME = "location_name";
    public static final String EXTRA_KEY_LOCATION_IDS = "location_ids";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_of_interest_map);

        if (savedInstanceState == null) {

            String locationName = getIntent().getStringExtra(EXTRA_KEY_LOCATION_NAME);
            ArrayList<String> placeIds = getIntent().getStringArrayListExtra(EXTRA_KEY_LOCATION_IDS);

            Bundle args = new Bundle();
            args.putString(PlaceOfInterestMapActivityFragment.ARGS_KEY_LOCATION_NAME, locationName);
            args.putStringArrayList(PlaceOfInterestMapActivityFragment.ARGS_KEY_PLACE_IDS, placeIds);

            PlaceOfInterestMapActivityFragment fragment = new PlaceOfInterestMapActivityFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.poi_map_container, fragment)
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_place_of_interest_map, menu);
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
