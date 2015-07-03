package com.example.sunchaser.app.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sunchaser.R;
import com.example.sunchaser.app.asynctask.LoaderId;
import com.example.sunchaser.app.asynctask.PlaceOfInterestDataLoader;
import com.example.sunchaser.app.data.Place;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by smee on 05/06/15.
 */
public class PlaceOfInterestDetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Collection<Place>> {

    public static final String ARGS_KEY_PLACE_ID = "place_id";

    private static final String MAPS_URI_FORMAT = "google.navigation:";
    private static final String SAVED_STATE_KEY_PAGER_FRAGMENT_ID = "fragment_id";
    private static final String LOG_TAG = PlaceOfInterestDetailActivityFragment.class.getSimpleName();

    private String placeId;
    private ShareActionProvider shareActionProvider;
    private Fragment pagerFragment;

    public PlaceOfInterestDetailActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(LoaderId.PLACE_OF_INTEREST_LOCATION.getId(), null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View inflatedView = inflater.inflate(R.layout.fragment_place_of_interest_detail, container, false);

        Bundle args = getArguments();
        placeId = args.getString(ARGS_KEY_PLACE_ID);

        if (savedInstanceState == null) {
            pagerFragment = new ImageViewPagerFragment();
            getActivity().getSupportFragmentManager().beginTransaction()
                    .add(R.id.place_detail_image_container, pagerFragment)
                    .commit();
        } else {
            int pagerFragmentId = savedInstanceState.getInt(SAVED_STATE_KEY_PAGER_FRAGMENT_ID);
            pagerFragment = getActivity().getSupportFragmentManager().findFragmentById(pagerFragmentId);
        }

        return inflatedView;
    }

    private void setPlaceDetails(final Place place) {

        // TODO: check for null and show error page...

        getActivity().setTitle(place.getName());

        TextView vicinityView = (TextView) getActivity().findViewById(R.id.placeDetail_vicinity);
        vicinityView.setText(place.getVicinity());

        RatingBar ratingBar = (RatingBar) getActivity().findViewById(R.id.placeDetail_rating);
        if (place.getRating() >= 1) {
            ratingBar.setRating(place.getRating());
        } else {
            getActivity().findViewById(R.id.place_detail_rating_section).setVisibility(View.GONE);
        }

        RatingBar priceLevelBar = (RatingBar) getActivity().findViewById(R.id.placeDetail_priceLevel);
        if (place.getPriceLevel() >= 1) {
            priceLevelBar.setRating(place.getPriceLevel());
        } else {
            getActivity().findViewById(R.id.place_detail_price_section).setVisibility(View.GONE);
        }

        if (place.getPhotos().isEmpty()) {
            getActivity().findViewById(R.id.place_detail_image_container).setVisibility(View.GONE);
        } else {
            ((ImageViewPagerFragment) pagerFragment).setImageInfo(place.getPhotos());
        }

        View directionsView = getActivity().findViewById(R.id.place_detail_get_directions);
        directionsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Use street address in query instead...
                String locationQuery = place.getLocation().latitude + "," + place.getLocation().longitude;
                Uri uri = Uri.parse(MAPS_URI_FORMAT).buildUpon()
                        .appendQueryParameter("q", locationQuery)
                        .build();
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, uri);

                if (mapIntent.resolveActivity(getActivity().getPackageManager()) == null) {
                    String message = getString(R.string.no_map_app_installed);
                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                    return;
                }

                startActivity(mapIntent);
            }
        });

        updateShareActionListener(place);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(SAVED_STATE_KEY_PAGER_FRAGMENT_ID, pagerFragment.getId());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_place_of_interest_detail_fragment, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);
        shareActionProvider = (ShareActionProvider) (MenuItemCompat.getActionProvider(menuItem));
    }

    private void updateShareActionListener(Place place) {
        if (shareActionProvider != null) {
            shareActionProvider.setShareIntent(createSharePlaceDetailIntent(place));
        } else {
            Log.d(LOG_TAG, "Share Action Provider is null");
        }
    }

    private Intent createSharePlaceDetailIntent(Place place) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");

        String contentString = "I've just visited " + place.getName() + " thanks to #SunChaserApp"; // TODO: Make this less awful

        shareIntent.putExtra(Intent.EXTRA_TEXT, contentString);
        return shareIntent;
    }

    @Override
    public Loader<Collection<Place>> onCreateLoader(int id, Bundle args) {
        List<String> placeIds = new ArrayList<>();
        placeIds.add(placeId);
        return new PlaceOfInterestDataLoader(getActivity(), placeIds);
    }

    @Override
    public void onLoadFinished(Loader<Collection<Place>> loader, Collection<Place> data) {
        if (data.isEmpty()) {
            Log.d(LOG_TAG, "Failed to load place details from database: " + placeId);
            return;
        }

        Place place = data.iterator().next();
        Log.d(LOG_TAG, "Loaded place from database: " + place.toString());
        setPlaceDetails(place);
    }

    @Override
    public void onLoaderReset(Loader<Collection<Place>> loader) {

    }
}