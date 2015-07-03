package com.example.sunchaser.app.data;

/**
 * Created by smee on 09/06/15.
 */
public class WeatherModel {

    private final int locationId;
    private final long date;
    private final String weatherDescription;
    private final int weatherConditionCode;
    private final double minTemperature;
    private final double maxTemperature;

    public WeatherModel(int locationId, long date, String weatherDescription, int weatherConditionCode, double maxTemperature, double minTemperature) {
        this.locationId = locationId;
        this.date = date;
        this.weatherDescription = weatherDescription;
        this.weatherConditionCode = weatherConditionCode;
        this.maxTemperature = maxTemperature;
        this.minTemperature = minTemperature;
    }

    public int getLocationId() {
        return locationId;
    }

    public long getDate() {
        return date;
    }

    public String getWeatherDescription() {
        return weatherDescription;
    }

    public int getWeatherConditionCode() {
        return weatherConditionCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WeatherModel that = (WeatherModel) o;

        if (locationId != that.locationId) return false;
        return date == that.date;

    }

    @Override
    public int hashCode() {
        int result = locationId;
        result = 31 * result + (int) (date ^ (date >>> 32));
        return result;
    }

    public double getMaxTemperature() {
        return maxTemperature;
    }

    public double getMinTemperature() {
        return minTemperature;
    }
}
