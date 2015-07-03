package com.example.sunchaser.app.net;

import android.util.Log;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by smee on 02/06/15.
 */
public abstract class SunChaserHttpRequestClient<R> {

    protected final String LOG_TAG = getClass().getSimpleName();

    public final R makeRequest() {

        if (!isRequestNecessary()) {
            return getDefaultValue();
        }

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String responseString = null;

        try {
            URL url = getRequestUrl();

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null) {
                // Nothing to do.
                return getDefaultValue();
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // New line isn't really necessary, but makes debug much easier (assuming response was multiline, of course)
                buffer.append(line).append("\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return getDefaultValue();
            }

            responseString = buffer.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
            return getDefaultValue();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                    return getDefaultValue();
                }
            }
        }

        try {
            return processResponse(responseString);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error processing response", e);
            return getDefaultValue();
        }
    }

    protected abstract R getDefaultValue();

    protected abstract boolean isRequestNecessary();

    protected abstract URL getRequestUrl() throws MalformedURLException;

    protected abstract R processResponse(String responseString) throws JSONException;
}
