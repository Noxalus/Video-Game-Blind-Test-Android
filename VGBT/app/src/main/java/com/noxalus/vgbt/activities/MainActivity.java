package com.noxalus.vgbt.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.noxalus.vgbt.R;
import com.noxalus.vgbt.tasks.GetExtractNumberAsyncResponse;
import com.noxalus.vgbt.tasks.GetExtractNumberAsyncTask;

public class MainActivity extends BaseActivity implements GetExtractNumberAsyncResponse
{
    TextView extractNumberTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        extractNumberTextView = (TextView) findViewById(R.id.extractNumberTextView);

        if (isNetworkAvailable())
        {
           getExtractNumber();
        }

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
                getAchievements();
            }
        });

        final Button leaderBoardButton = (Button) findViewById(R.id.leaderboardButton);
        leaderBoardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLeaderboards();
            }
        });

        final Button optionsButton = (Button) findViewById(R.id.optionsButton);
        optionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ExcludeGameSeriesActivity.class);
                startActivity(intent);
            }
        });
    }

    private void getExtractNumber()
    {
        // AsyncTask can't be executed multiple times
        // we need to create a new instance each time
        GetExtractNumberAsyncTask getExtractNumberAsyncTask = new GetExtractNumberAsyncTask();
        getExtractNumberAsyncTask.delegate = this;

        getExtractNumberAsyncTask.execute(getResources().getString(R.string.api) + "?extractNumber=1");
    }

    @Override
    public void processFinish(int output)
    {
        SharedPreferences settings = getSharedPreferences("VGBT", 0);
        SharedPreferences.Editor editor = settings.edit();

        int extractNumber = settings.getInt("extractNumber", 0);

        if (extractNumber > 0 && extractNumber < output) {
            extractNumberTextView.setText(Html.fromHtml(output + " extracts<br>(<b>" + (output - extractNumber) + " new</b>)"));
            editor.putBoolean("newExtracts", true);
        }
        else {
            extractNumberTextView.setText(output + " extracts");
            editor.putBoolean("newExtracts", false);
        }

        editor.putInt("extractNumber", output);
        editor.commit();
    }

    private boolean isNetworkAvailable()
    {
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

    public void getLeaderboards()
    {
        if (agsClient != null) {
            if (agsClient.getPlayerClient().isSignedIn())
                agsClient.getLeaderboardsClient().showLeaderboardsOverlay();
            else
                agsClient.showSignInPage();
        }
    }

    public void getAchievements()
    {
        if (agsClient != null) {
            if (agsClient.getPlayerClient().isSignedIn())
                agsClient.getAchievementsClient().showAchievementsOverlay();
            else
                agsClient.showSignInPage();
        }
    }
}
