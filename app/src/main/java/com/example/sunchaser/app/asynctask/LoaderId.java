package com.example.sunchaser.app.asynctask;

/**
 * Created by smee on 08/06/15.
 */
public enum LoaderId {

    WEATHER_MAP_LOCATION_AND_WEATHER(0),
    WEATHER_MAP_CUSTOM_LOCATION(1),

    GEOLOCATION_DETAIL_LOCATION(10),
    GEOLOCATION_DETAIL_WIKI_IMAGES(11),

    PLACE_OF_INTEREST_LOCATION(20),
    ;

    private final int id;

    private LoaderId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static LoaderId fromId(int id) {
        for (LoaderId li : values()) {
            if (li.id == id) {
                return li;
            }
        }
        return null;
    }
}
