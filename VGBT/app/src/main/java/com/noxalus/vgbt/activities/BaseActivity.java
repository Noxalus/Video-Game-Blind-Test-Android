package com.noxalus.vgbt.activities;

import android.app.Activity;
import android.util.Log;

import com.amazon.ags.api.AmazonGamesCallback;
import com.amazon.ags.api.AmazonGamesClient;
import com.amazon.ags.api.AmazonGamesFeature;
import com.amazon.ags.api.AmazonGamesStatus;

import java.util.EnumSet;

public class BaseActivity extends Activity
{
    //reference to the agsClient
    protected AmazonGamesClient agsClient;

    AmazonGamesCallback callback = new AmazonGamesCallback() {
        @Override
        public void onServiceNotReady(AmazonGamesStatus status) {
            //unable to use service
            Log.d("VGBT", status.name());
        }
        @Override
        public void onServiceReady(AmazonGamesClient amazonGamesClient) {
            agsClient = amazonGamesClient;
            Log.d("VGBT", "Ready to use Amazon GameCircle services!");
            //ready to use GameCircle
        }
    };

    //list of features your game uses (in this example, achievements and leaderboards)
    EnumSet<AmazonGamesFeature> myGameFeatures = EnumSet.of(
            AmazonGamesFeature.Achievements, AmazonGamesFeature.Leaderboards);

    @Override
    public void onResume() {
        super.onResume();
        AmazonGamesClient.initialize(this, callback, myGameFeatures);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (agsClient != null) {
            agsClient.release();
        }
    }
}
