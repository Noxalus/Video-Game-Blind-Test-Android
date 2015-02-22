package com.noxalus.vgbt.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import com.noxalus.vgbt.R;
import com.noxalus.vgbt.dialogs.GoToExcludeElementDialogFragment;

public class PlayActivity extends Activity
{
    float x = 0;
    float y = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        final SharedPreferences settings = getSharedPreferences("VGBT", 0);
        final SharedPreferences.Editor editor = settings.edit();

        final TextView textView = (TextView) findViewById(R.id.playInfoTextView);

        final Button rankedGameButton = (Button) findViewById(R.id.rankedGameButton);

        rankedGameButton.setOnTouchListener(new View.OnTouchListener()
        {
            public boolean onTouch(View view, MotionEvent event)
            {
                if (event.getAction() == android.view.MotionEvent.ACTION_UP)
                {
                    Log.d("VGBT", "Position: " + event.getX() + ", " + event.getY());
                    x = event.getX();
                    y = event.getY();
                }

                return false;
            }
        });

        rankedGameButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    textView.startAnimation(AnimationUtils.loadAnimation(PlayActivity.this, android.R.anim.fade_out));
                    textView.startAnimation(AnimationUtils.loadAnimation(PlayActivity.this, android.R.anim.slide_out_right));
                    textView.setX(x);
                    textView.setY(y);
                    /*
                    Integer rankedGameNumber = settings.getInt("rankedGameNumber", 0);

                    Intent intent = new Intent(PlayActivity.this, ModeActivity.class);
                    intent.putExtra("ranked", true);
                    startActivity(intent);

                    rankedGameNumber++;
                    editor.putInt("rankedGameNumber", rankedGameNumber);
                    editor.commit();
                    */
                }
            }
        );

        final Button trainingGameButton = (Button) findViewById(R.id.trainingGameButton);
        trainingGameButton.setOnClickListener(new View.OnClickListener()
          {
              @Override
              public void onClick(View v)
              {
                  Integer trainingGameNumber = settings.getInt("trainingGameNumber", 0);

                  if (trainingGameNumber == 0)
                  {
                      Intent modeIntent = new Intent(PlayActivity.this, ModeActivity.class);
                      Intent optionsIntent = new Intent(PlayActivity.this, ExcludeGameSeriesActivity.class);

                      // Display the popup to go to exclude game serie activity
                      GoToExcludeElementDialogFragment dialog = new GoToExcludeElementDialogFragment(getApplicationContext(), optionsIntent, modeIntent);

                      FragmentManager fm = getFragmentManager();
                      dialog.show(fm, "GO_TO_EXCLUDE_ELEMENT");
                  }
                  else
                  {
                      Intent intent = new Intent(PlayActivity.this, ModeActivity.class);
                      startActivity(intent);
                  }

                  trainingGameNumber++;
                  editor.putInt("trainingGameNumber", trainingGameNumber);
                  editor.commit();
              }
          }
        );
    }
}
