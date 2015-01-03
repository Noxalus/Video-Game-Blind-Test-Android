package vgbt.noxalus.com.videogameblindtest;

import android.content.Intent;
import android.media.AudioManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Random;

public class QuizActivity extends ActionBarActivity implements OnClickListener, OnTouchListener, OnCompletionListener, OnBufferingUpdateListener {

    private ImageButton buttonPlayPause;
    private SeekBar seekBarProgress;
    public TextView musicNameTextView;

    private MediaPlayer mediaPlayer;
    private int mediaFileLengthInMilliseconds;

    private final Handler handler = new Handler();

    Random r;
    private int currentMusicId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);
        initView();
    }

    private void initView() {
        buttonPlayPause = (ImageButton)findViewById(R.id.ButtonTestPlayPause);
        buttonPlayPause.setOnClickListener(this);

        musicNameTextView = (TextView)findViewById(R.id.musicName);

        seekBarProgress = (SeekBar)findViewById(R.id.SeekBarTestPlay);
        seekBarProgress.setMax(99); // It means 100% .0-99
        seekBarProgress.setOnTouchListener(this);

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnCompletionListener(this);

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                if (!mediaPlayer.isPlaying()) {

                    mediaFileLengthInMilliseconds = mediaPlayer.getDuration();

                    mediaPlayer.start();

                    musicNameTextView.setText("extrait" + currentMusicId + ".mp3 (" + mediaFileLengthInMilliseconds + ")");

                    buttonPlayPause.setImageResource(R.drawable.button_pause);
                    primarySeekBarProgressUpdater();
                }
            }
        });

        r = new Random();

        Button nextButton = (Button)findViewById(R.id.nextButton);
        nextButton.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextMusic();
            }
        }));

        nextMusic();
    }

    private void generateNewRandomId()
    {
        currentMusicId = r.nextInt(252) + 1;
    }

    private void nextMusic()
    {
        generateNewRandomId();

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }

        mediaPlayer.reset();

        loadMusic();
    }

    private void loadMusic()
    {
        try {
            String currentMusicUrl = getResources().getString(R.string.baseUrl) + "extrait" + currentMusicId + ".mp3";
            mediaPlayer.setDataSource(currentMusicUrl);
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void primarySeekBarProgressUpdater() {
        seekBarProgress.setProgress((int)(((float)mediaPlayer.getCurrentPosition() / mediaFileLengthInMilliseconds) * 100));
        if (mediaPlayer.isPlaying()) {
            Runnable notification = new Runnable() {
                public void run() {
                    primarySeekBarProgressUpdater();
                }
            };

            handler.postDelayed(notification,1000);
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.ButtonTestPlayPause){
            loadMusic();

            if(!mediaPlayer.isPlaying()){
                mediaPlayer.start();
                buttonPlayPause.setImageResource(R.drawable.button_pause);
            }else {
                mediaPlayer.pause();
                buttonPlayPause.setImageResource(R.drawable.button_play);
            }

            primarySeekBarProgressUpdater();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(v.getId() == R.id.SeekBarTestPlay){

            if(mediaPlayer.isPlaying()) {
                SeekBar sb = (SeekBar)v;
                int playPositionInMilliseconds = (mediaFileLengthInMilliseconds / 100) * sb.getProgress();
                mediaPlayer.seekTo(playPositionInMilliseconds);
            }
        }
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        buttonPlayPause.setImageResource(R.drawable.button_play);
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        seekBarProgress.setSecondaryProgress(percent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_quiz, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
