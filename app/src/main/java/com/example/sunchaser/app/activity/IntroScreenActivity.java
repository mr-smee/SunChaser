package com.example.sunchaser.app.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.sunchaser.R;
import com.example.sunchaser.app.preferences.SharedPreferenceUtils;
import com.example.sunchaser.app.sync.SunChaserSyncAdapter;

/**
 * Created by smee on 12/06/15.
 */
public class IntroScreenActivity extends Activity {


    private static IntentFilter syncIntentFilter = new IntentFilter(SunChaserSyncAdapter.BROADCAST_TAG_SYNC_FINISHED);

    private static final String LOG_TAG = IntroScreenActivity.class.getSimpleName();

    private TextView progressText;
    private BroadcastReceiver syncBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final int progress = intent.getIntExtra(SunChaserSyncAdapter.EXTRA_KEY_PROGRESS, 0);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    switch (progress) {
                        case SunChaserSyncAdapter.EXTRA_PROGRESS_FETCHING_GEOLOCATIONS: {
                            if (progressText!= null) {
                                progressText.setText(getString(R.string.intro_screen_loading_geolocations));
                            }
                            break;
                        }
                        case SunChaserSyncAdapter.EXTRA_PROGRESS_FETCHING_WEATHER: {
                            if (progressText!= null) {
                                progressText.setText(getString(R.string.intro_screen_loading_weather));
                            }
                            break;
                        }
                        case SunChaserSyncAdapter.EXTRA_PROGRESS_FETCHING_WIKI_ARTICLES: {
                            if (progressText!= null) {
                                progressText.setText(getString(R.string.intro_screen_loading_wiki_articles));
                            }
                            break;
                        }
                        case SunChaserSyncAdapter.EXTRA_PROGRESS_FETCHING_WIKI_IMAGES: {
                            if (progressText!= null) {
                                progressText.setText(getString(R.string.intro_screen_loading_wiki_images));
                            }
                            break;
                        }
                        case SunChaserSyncAdapter.EXTRA_PROGRESS_FINISHED: {
                            if (progressText!= null) {
                                Log.d(LOG_TAG, "Sync has finished...");
                                SharedPreferenceUtils.setHasShownIntroScreen(IntroScreenActivity.this);
                                Intent mainIntent = new Intent(IntroScreenActivity.this, MainActivity.class);
                                startActivity(mainIntent);
                                finish();
                            }
                            break;
                        }
                    }
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro_screen);

        progressText = (TextView) findViewById(R.id.intro_screen_progress_text);

        SunChaserSyncAdapter.syncImmediately(this);
    }



    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(syncBroadcastReceiver, syncIntentFilter);
    }

    @Override
    protected void onPause() {
        unregisterReceiver(syncBroadcastReceiver);
        super.onPause();
    }

}

