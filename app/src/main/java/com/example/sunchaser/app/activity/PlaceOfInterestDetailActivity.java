package com.example.sunchaser.app.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.example.sunchaser.R;

/**
 * Created by smee on 05/06/15.
 */
public class PlaceOfInterestDetailActivity extends ActionBarActivity {

    public static final String EXTRA_KEY_PLACE_ID = "place_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_of_interest_detail);

        if (savedInstanceState == null) {

            String placeId = getIntent().getStringExtra(EXTRA_KEY_PLACE_ID);

            Bundle args = new Bundle();
            args.putString(PlaceOfInterestDetailActivityFragment.ARGS_KEY_PLACE_ID, placeId);

            PlaceOfInterestDetailActivityFragment fragment = new PlaceOfInterestDetailActivityFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.place_detail_container, fragment)
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_place_of_interest_detail, menu);
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
