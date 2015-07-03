package com.example.sunchaser.app.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;

import com.example.sunchaser.R;
import com.example.sunchaser.app.asynctask.LoadPlacesOfInterestAsyncTask;
import com.example.sunchaser.app.data.Place;
import com.example.sunchaser.app.data.PlaceTypeGroup;
import com.example.sunchaser.app.preferences.PlaceOfInterestSearchOptions;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlaceOfInterestListActivityFragment extends Fragment implements LoadPlacesOfInterestAsyncTask.PlaceListDisplay, PlaceListAdapter.PlaceListCallbackHandler {

    private static final String LOG_TAG = PlaceOfInterestListActivityFragment.class.getSimpleName();

    public static final String ARGS_KEY_LOCATION_NAME = "location_name";
    public static final String ARGS_KEY_LATITUDE = "latitude";
    public static final String ARGS_KEY_LONGITUDE = "longitude";
    public static final String ARGS_KEY_PLACE_TYPES = "place_types";

    private final Set<Place> places = new HashSet<>();
    private PlaceListAdapter adapter;
    private LatLng location;
    private PlaceOfInterestSearchOptions searchOptions;
    private SharedPreferences sharedPreferences;
    private View footerView;

    public PlaceOfInterestListActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.fragment_place_of_interest_list, container, false);
        ListView listView = (ListView)inflatedView.findViewById(R.id.place_list);

        adapter = new PlaceListAdapter(getActivity(), new ArrayList<Place>(), null, this);
        listView.setAdapter(adapter);

        final String locationName = getArguments().getString(ARGS_KEY_LOCATION_NAME);
        getActivity().setTitle(locationName);

        footerView = inflatedView.findViewById(R.id.place_list_show_on_map);
        footerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<String> placeIds = new ArrayList<>();
                for (Place place : places) {
                    placeIds.add(place.getId());
                }

                Intent intent = new Intent(getActivity(), PlaceOfInterestMapActivity.class);
                intent.putExtra(PlaceOfInterestMapActivity.EXTRA_KEY_LOCATION_NAME, locationName);
                intent.putExtra(PlaceOfInterestMapActivity.EXTRA_KEY_LOCATION_IDS, placeIds);
                startActivity(intent);
            }
        });

        double latitude = getArguments().getDouble(ARGS_KEY_LATITUDE);
        double longitude = getArguments().getDouble(ARGS_KEY_LONGITUDE);
        location = new LatLng(latitude, longitude);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        searchOptions = PlaceOfInterestSearchOptions.restoreFromSharedPreferences(sharedPreferences);

        int placeTypeId = getArguments().getInt(ARGS_KEY_PLACE_TYPES, -1);

        // If passed in, override the groups in the searchOptions with the specified place types. Don't persist the change though
        if (placeTypeId != -1) {
            PlaceTypeGroup typeGroup = PlaceTypeGroup.fromId(placeTypeId);
            Set<PlaceTypeGroup> typeGroups = new HashSet<>(1);
            typeGroups.add(typeGroup);

            searchOptions = new PlaceOfInterestSearchOptions(searchOptions.getRadiusKm(), typeGroups);
        }

        // NOTE: We don't need to set off the loader task here, as it's automatically done as a
        //       side-effect of setting the values of the search filter options
        setSearchOptionValues(inflatedView, searchOptions);

        return inflatedView;
    }

    private void setSearchOptionValues(View inflatedView, PlaceOfInterestSearchOptions searchOptions) {
        int selectedRadius = searchOptions.getRadiusKm();
        SeekBar radiusSeekBar = (SeekBar) inflatedView.findViewById(R.id.search_radiusSeekBar);
        radiusSeekBar.setProgress(selectedRadius);
        // TODO: Do we want this listener, or do we want the user to confirm before performing a new search?
        radiusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int radius, boolean fromUser) {
                if (fromUser) {
                    updateSearchRadius(radius);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        PlaceTypeGroup[] groups = PlaceTypeGroup.values();
        Spinner spinner = (Spinner) inflatedView.findViewById(R.id.search_filter_dropdown);

        ArrayAdapter<PlaceTypeGroup> adapter = new ArrayAdapter<PlaceTypeGroup>(getActivity(), android.R.layout.simple_spinner_item, groups);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        Set<PlaceTypeGroup> selectedTypeGroups = searchOptions.getTypeGroups();
        for (PlaceTypeGroup group : selectedTypeGroups) {
            spinner.setSelection(adapter.getPosition(group));
        }

        // TODO: Do we want this listener, or do we want the user to confirm before performing a new search?
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                PlaceTypeGroup group = PlaceTypeGroup.fromId(id);
                setSelectedGroup(group);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Clear the list
            }
        });
    }

    private void updateSearchRadius(int radiusKm) {
        searchOptions = new PlaceOfInterestSearchOptions(radiusKm, searchOptions.getTypeGroups());
        onPreferenceUpdate();
    }

    // TODO: Tidy this class up!
    private void setSelectedGroup(PlaceTypeGroup group) {
        Set<PlaceTypeGroup> groups = new HashSet<>(1);
        groups.add(group);
        searchOptions = new PlaceOfInterestSearchOptions(searchOptions.getRadiusKm(), groups);
        onPreferenceUpdate();
    }

    private void onPreferenceUpdate() {
        searchOptions.persistToSharedPreferences(sharedPreferences);
        adapter.clear();
        places.clear();
        loadMorePlaces("");
    }

    @Override
    public void setPlaceList(List<Place> placesToAdd, String nextPageToken) {
        Log.d(LOG_TAG, "Adding " + placesToAdd.size() + " to list of places");
        this.places.addAll(placesToAdd);
        adapter.addAll(placesToAdd);
        adapter.setNextPageToken(nextPageToken);
    }

    @Override
    public void loadMorePlaces(String nextPageToken) {
        Log.d(LOG_TAG, "Loading some more places...");
        new LoadPlacesOfInterestAsyncTask(this, getActivity().getContentResolver(), location, nextPageToken).execute(searchOptions);
    }

    @Override
    public void showPlaceDetail(String id) {
        Intent intent = new Intent(getActivity(), PlaceOfInterestDetailActivity.class);
        intent.putExtra(PlaceOfInterestDetailActivity.EXTRA_KEY_PLACE_ID, id);
        startActivity(intent);
    }

}
