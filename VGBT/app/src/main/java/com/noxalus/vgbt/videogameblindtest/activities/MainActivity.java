package com.noxalus.vgbt.videogameblindtest.activities;

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

import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.BaseGameActivity;
import com.noxalus.vgbt.videogameblindtest.R;


public class MainActivity extends BaseGameActivity {

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

        /*
        final Button proposalsButton = (Button) findViewById(R.id.proposalsButton);
        proposalsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ProposalActivity.class);
                startActivity(intent);
            }
        });
        */

        final Button achievementsButton = (Button) findViewById(R.id.achievementsButton);
        achievementsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSignedIn())
                    getAchievementsGPGS();
                else
                    loginGPGS();
            }
        });

        final Button leaderBoardButton = (Button) findViewById(R.id.leaderboardButton);
        leaderBoardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSignedIn())
                    getLeaderboardGPGS();
                else
                    loginGPGS();
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

    private void loginGPGS() {
        try {
            runOnUiThread(new Runnable(){
                public void run() {
                    getGameHelper().beginUserInitiatedSignIn();
                }
            });
        } catch (final Exception ex) {
        }
    }

    @Override
    public void onActivityResult(int request, int response, Intent data) {
        super.onActivityResult(request, response, data);
    }

    public void getLeaderboardGPGS() {
        if (isSignedIn())
            startActivityForResult(Games.Leaderboards.getAllLeaderboardsIntent(getApiClient()), 100);
    }

    public void getAchievementsGPGS() {
        if (isSignedIn())
            startActivityForResult(Games.Achievements.getAchievementsIntent(getApiClient()), 100);
    }

    @Override
    public void onSignInFailed() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSignInSucceeded() {
        // TODO Auto-generated method stub

    }
}
