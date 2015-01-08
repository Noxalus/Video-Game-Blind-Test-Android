package com.noxalus.vgbt.videogameblindtest.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.noxalus.vgbt.videogameblindtest.R;

public class ResultActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        int score = getIntent().getIntExtra("score", 0);
        String mode = getIntent().getStringExtra("mode");

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

                break;

            case "jeu":
                if (score > bestScoreGame) {
                    bestScoreGame = score;
                    editor.putInt("bestScoreGame", score);
                }

                bestScoreTextView.setText("(Best: " + Integer.toString(bestScoreGame) + ")");

                break;

            case "compositeur":
                if (score > bestScoreComposer) {
                    bestScoreComposer = score;
                    editor.putInt("bestScoreComposer", score);
                }

                bestScoreTextView.setText("(Best: " + Integer.toString(bestScoreComposer) + ")");
                break;
        }

        editor.commit();
    }
}
