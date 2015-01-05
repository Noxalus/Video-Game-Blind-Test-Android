package vgbt.noxalus.com.videogameblindtest.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import vgbt.noxalus.com.videogameblindtest.R;

public class ModeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode);

        final Button nameButton = (Button) findViewById(R.id.nameButton);
        nameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ModeActivity.this, QuizActivity.class);
                intent.putExtra("mode", "nom");
                startActivity(intent);
            }
        });

        final Button gameButton = (Button) findViewById(R.id.gameButton);
        gameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ModeActivity.this, QuizActivity.class);
                intent.putExtra("mode", "jeu");
                startActivity(intent);
            }
        });

        final Button composerButton = (Button) findViewById(R.id.composerButton);
        composerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ModeActivity.this, QuizActivity.class);
                intent.putExtra("mode", "compositeur");
                startActivity(intent);
            }
        });
    }
}
