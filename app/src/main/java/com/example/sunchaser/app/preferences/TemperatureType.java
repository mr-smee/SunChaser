package com.example.sunchaser.app.preferences;

/**
 * Created by smee on 05/06/15.
 */
public enum TemperatureType {
    CELSIUS(0),
    IMPERIAL(1),
    ;

    private final int id;

    private TemperatureType(int id) {
        this.id = id;
    }

    public static TemperatureType fromId(int id) {
        for (TemperatureType type : values()) {
            if (type.id == id) {
                return type;
            }
        }
        return CELSIUS;
    }

    public int getId() {
        return id;
    }
}
