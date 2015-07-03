package com.example.sunchaser.app.data;

import android.content.Context;

import com.example.sunchaser.R;

/**
 * Created by smee on 27/05/15.
 */
public class TemperatureUtil {

    public static String formatTemperature(Context context, double temperature) {
        //TODO: Settings for metric/imperial
        boolean isMetric = true; //Utility.isMetric(context);

        double temp = getTemp(temperature, isMetric);
        return context.getString(R.string.format_temperature, temp);
    }

    private static double getTemp(double temperature, boolean isMetric) {
        double temp;
        if ( !isMetric ) {
            temp = 9*temperature/5+32;
        } else {
            temp = temperature;
        }
        return temp;
    }
}
