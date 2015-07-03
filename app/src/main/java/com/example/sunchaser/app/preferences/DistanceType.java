package com.example.sunchaser.app.preferences;

/**
 * Created by smee on 05/06/15.
 */
public enum DistanceType {
    METRIC(0),
    IMPERIAL(1);

    private final int id;

    private DistanceType(int id) {
        this.id = id;
    }

    public static DistanceType fromId(int id) {
        for (DistanceType type : values()) {
            if (type.id == id) {
                return type;
            }
        }
        return METRIC;
    }

    public int getId() {
        return id;
    }
}
