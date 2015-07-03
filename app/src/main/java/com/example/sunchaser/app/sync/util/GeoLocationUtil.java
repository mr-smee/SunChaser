package com.example.sunchaser.app.sync.util;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

/**
 * Created by smee on 25/05/15.
 */
public class GeoLocationUtil {

    private static final double EARTH_RADIUS_KM = 6371;
    private static final double DEGREES_TO_RADIANS = Math.PI / 180.0;
    private static final double RADIANS_TO_DEGREES = 180.0 / Math.PI;


    public static LatLngBounds generateBoundingBox(LatLng centre, int radiusKm) {
        double latitudeChange = getLatitudeChange(radiusKm);
        double northLatitude = centre.latitude + latitudeChange;
        double southLatitude = centre.latitude - latitudeChange;

        double westLongitude = centre.longitude - longitudeChange(centre.latitude, radiusKm);
        double eastLongitude = centre.longitude + longitudeChange(centre.latitude, radiusKm);

        LatLng northWest = new LatLng(northLatitude, westLongitude);
        LatLng southEast = new LatLng(southLatitude, eastLongitude);
        return LatLngBounds.builder()
                .include(northWest)
                .include(southEast)
                .build();
    }

    public static LatLngBounds generateBoundingBox(Location centre, int radiusKm) {
        LatLng centreLatLng = new LatLng(centre.getLatitude(), centre.getLongitude());
        return generateBoundingBox(centreLatLng, radiusKm);
    }

    private static double getLatitudeChange(double km) {
        return (km / EARTH_RADIUS_KM) * RADIANS_TO_DEGREES;
    }

    private static double longitudeChange(double latitude, double km) {
        double r = EARTH_RADIUS_KM * Math.cos(latitude * DEGREES_TO_RADIANS);
        return (km / r) * RADIANS_TO_DEGREES;
    }

}
