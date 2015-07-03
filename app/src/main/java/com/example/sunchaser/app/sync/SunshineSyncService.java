package com.example.sunchaser.app.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class SunshineSyncService extends Service {

    private static final Object sSyncAdapterLock = new Object();
    private static SunChaserSyncAdapter sSunChaserSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d("SunshineSyncService", "onCreate - SunshineSyncService");
        synchronized (sSyncAdapterLock) {
            if (sSunChaserSyncAdapter == null) {
                sSunChaserSyncAdapter = new SunChaserSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sSunChaserSyncAdapter.getSyncAdapterBinder();
    }
}