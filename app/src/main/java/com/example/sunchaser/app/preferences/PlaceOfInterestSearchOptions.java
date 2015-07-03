package com.example.sunchaser.app.preferences;

import android.content.SharedPreferences;

import com.example.sunchaser.app.data.PlaceType;
import com.example.sunchaser.app.data.PlaceTypeGroup;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by smee on 03/06/15.
 */
public class PlaceOfInterestSearchOptions {

    private static final String PREFERENCE_KEY_RADIUS = "radiusKm";
    private static final String PREFERENCE_KEY_PLACE_TYPE_GROUP = "place_types";
    private static final int SEARCH_RADIUS_DEFAULT = 5;

    private final int radiusKm;
    private final Set<PlaceTypeGroup> typeGroups;

    public PlaceOfInterestSearchOptions(int radiusKm, Set<PlaceTypeGroup> typeGroups) {
        this.radiusKm = radiusKm;
        this.typeGroups = typeGroups;
    }

    public int getRadiusMetres() {
        return radiusKm * 1000;
    }

    public int getRadiusKm() {
        return radiusKm;
    }

    public Set<PlaceTypeGroup> getTypeGroups() {
        return typeGroups;
    }

    public static PlaceOfInterestSearchOptions restoreFromSharedPreferences(SharedPreferences preferences) {

        int selectedRadius = preferences.getInt(PREFERENCE_KEY_RADIUS, SEARCH_RADIUS_DEFAULT);

        String selectedGroupIds = preferences.getString(PlaceOfInterestSearchOptions.PREFERENCE_KEY_PLACE_TYPE_GROUP, "" );
        Set<PlaceTypeGroup> selectedGroups = getGroupsFromString(selectedGroupIds);

        return new PlaceOfInterestSearchOptions(selectedRadius, selectedGroups);
    }

    public void persistToSharedPreferences(SharedPreferences preferences) {
        SharedPreferences.Editor editor = preferences.edit();

        editor.putInt(PREFERENCE_KEY_RADIUS, radiusKm);
        editor.putString(PREFERENCE_KEY_PLACE_TYPE_GROUP, getStringFromGroups(getTypeGroups()));

        editor.apply();
    }

    private String getStringFromGroups(Set<PlaceTypeGroup> typeGroups) {
        StringBuilder sb = new StringBuilder();

        for (PlaceTypeGroup group : typeGroups) {
            if (sb.length() != 0) {
                sb.append(',');
            }
            sb.append(group.getId());
        }

        return sb.toString();
    }

    private static Set<PlaceTypeGroup> getGroupsFromString(String selectedGroupIds) {
        String[] groupIds = selectedGroupIds.split(",");
        Set<PlaceTypeGroup> groups = new HashSet<PlaceTypeGroup>(groupIds.length);

        for (String id : groupIds) {
            try {
                groups.add(PlaceTypeGroup.fromId(Integer.valueOf(id)));
            } catch (NumberFormatException e) {
                System.out.println("Found invalid group ID in encoded string: "  + id);
            }

        }

        return groups;
    }

    public Set<PlaceType> getAllSelectedPlaceTypes() {
        Set<PlaceType> placeTypes = new HashSet<PlaceType>();
        for (PlaceTypeGroup group : typeGroups) {
            for (PlaceType type : PlaceType.values()) {
                if (type.getGroups().contains(group)) {
                    placeTypes.add(type);
                }
            }
        }
        return placeTypes;
    }

}
