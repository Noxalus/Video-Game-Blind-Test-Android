package com.noxalus.vgbt.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.noxalus.vgbt.R;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button playButton = (Button) findViewById(R.id.playButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkAvailable()) {
                    Intent intent = new Intent(MainActivity.this, ModeActivity.class);
                    startActivity(intent);
                }
                else
                {
                    Toast toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.not_connected), Toast.LENGTH_LONG);
                    TextView textView = (TextView) toast.getView().findViewById(android.R.id.message);
                    if( textView != null)
                        textView.setGravity(Gravity.CENTER);

                    toast.show();
                }
            }
        });

        final Button achievementsButton = (Button) findViewById(R.id.achievementsButton);
        achievementsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        final Button leaderBoardButton = (Button) findViewById(R.id.leaderboardButton);
        leaderBoardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onStart(){
        super.onStart();
    }

    @Override
    public void onStop(){
        super.onStop();
    }

    @Override
    public void onActivityResult(int request, int response, Intent data) {
        super.onActivityResult(request, response, data);
    }

    public void getLeaderboardGPGS() {
    }

    public void getAchievementsGPGS() {
    }
}
