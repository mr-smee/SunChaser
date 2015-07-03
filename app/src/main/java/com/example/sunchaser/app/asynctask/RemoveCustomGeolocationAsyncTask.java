package com.example.sunchaser.app.asynctask;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import com.example.sunchaser.app.data.dbcontract.GeolocationEntry;

/**
 * Created by smee on 15/06/15.
 */
public class RemoveCustomGeolocationAsyncTask extends AsyncTask<Void, Void, Void> {

    private final Context context;
    private final int locationId;

    public RemoveCustomGeolocationAsyncTask(Context context, int locationId) {
        this.context = context;
        this.locationId = locationId;
    }

    @Override
    protected Void doInBackground(Void... voids) {

        // TODO: Remove other stuff? Or just leave it to be cleared by the sync adapter?
        // WeatherEntry.COLUMN_LOC_KEY

        Uri locationUri = GeolocationEntry.CONTENT_URI;
        String whereClause = GeolocationEntry.COLUMN_LOCATION_ID + " = ?";
        String[] whereClauseValues = new String[] {Integer.toString(locationId)};

        context.getContentResolver().delete(locationUri, whereClause, whereClauseValues);

        return null;
    }
}
