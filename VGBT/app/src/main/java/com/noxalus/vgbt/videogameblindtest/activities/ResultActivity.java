package com.noxalus.vgbt.videogameblindtest.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.BaseGameActivity;
import com.noxalus.vgbt.videogameblindtest.R;

public class ResultActivity extends BaseGameActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
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

    private void submitScore(){

        int score = getIntent().getIntExtra("score", 0);
        String mode = getIntent().getStringExtra("mode");

        if (score > 0)
            unlockAchievementGPGS(getResources().getString(R.string.achievement_i_dont_suck_really___));

        if (score >= 1000)
            unlockAchievementGPGS(getResources().getString(R.string.achievement_virtuoso));

        if (mode.equals("nom"))
            unlockAchievementGPGS(getResources().getString(R.string.achievement_i_tried_to_guess_the_name));
        else if (mode.equals("jeu"))
            unlockAchievementGPGS(getResources().getString(R.string.achievement_i_tried_to_guess_the_game));
        else if (mode.equals("compositeur"))
            unlockAchievementGPGS(getResources().getString(R.string.achievement_i_tried_to_guess_the_composer));

        final TextView scoreTextView = (TextView) findViewById(R.id.scoreTextView);
        scoreTextView.setText(Integer.toString(score));

        final TextView bestScoreTextView = (TextView) findViewById(R.id.bestScoreTextView);

        final Button backButton = (Button) findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ResultActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        SharedPreferences settings = getSharedPreferences("VGBT", 0);
        SharedPreferences.Editor editor = settings.edit();

        int bestScoreName = settings.getInt("bestScoreName", 0);
        int bestScoreGame = settings.getInt("bestScoreGame", 0);
        int bestScoreComposer = settings.getInt("bestScoreComposer", 0);

        switch (mode)
        {
            case "nom":
                if (score > bestScoreName) {
                    bestScoreName = score;
                    editor.putInt("bestScoreName", score);
                }

                bestScoreTextView.setText("(Best: " + Integer.toString(bestScoreName) + ")");

                submitScoreGPGS(score, getResources().getString(R.string.leaderboard_guess_the_name));

                break;

            case "jeu":
                if (score > bestScoreGame) {
                    bestScoreGame = score;
                    editor.putInt("bestScoreGame", score);
                }

                bestScoreTextView.setText("(Best: " + Integer.toString(bestScoreGame) + ")");

                submitScoreGPGS(score, getResources().getString(R.string.leaderboard_guess_the_game));

                break;

            case "compositeur":
                if (score > bestScoreComposer) {
                    bestScoreComposer = score;
                    editor.putInt("bestScoreComposer", score);
                }

                bestScoreTextView.setText("(Best: " + Integer.toString(bestScoreComposer) + ")");

                submitScoreGPGS(score, getResources().getString(R.string.leaderboard_guess_the_composer));

                break;
        }

        editor.commit();
    }

    public void submitScoreGPGS(int score, String leaderboardId) {
        if (isSignedIn())
            Games.Leaderboards.submitScore(getApiClient(), leaderboardId, score);
    }

    public void unlockAchievementGPGS(String achievementId) {
        if (isSignedIn())
            Games.Achievements.unlock(getApiClient(), achievementId);
    }

    @Override
    public void onSignInFailed() {
        submitScore();
    }

    @Override
    public void onSignInSucceeded() {
        submitScore();
    }
}
