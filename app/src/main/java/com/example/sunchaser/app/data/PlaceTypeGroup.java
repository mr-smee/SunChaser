package com.example.sunchaser.app.data;

/**
 * Created by smee on 03/06/15.
 */
public enum PlaceTypeGroup {

    CULTURE   (0),
    EATING    (1),
    LEISURE   (2),
    SHOPPING  (3),
    NIGHTLIFE (4),
    LODGING   (5),
    TRAVEL    (6),
    ;

    private final int id;

    private PlaceTypeGroup(final int id) {
        this.id = id;
    }

    public static PlaceTypeGroup fromId(long id) {
        for (PlaceTypeGroup group : values()) {
            if (group.id == id) {
                return group;
            }
        }
        return null;
    }

    public int getId() {
        return id;
    }
}

