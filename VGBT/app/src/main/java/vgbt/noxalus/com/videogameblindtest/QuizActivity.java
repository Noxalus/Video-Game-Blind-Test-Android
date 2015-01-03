package vgbt.noxalus.com.videogameblindtest;

import android.media.AudioManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import vgbt.noxalus.com.videogameblindtest.entities.Question;
import vgbt.noxalus.com.videogameblindtest.tasks.AsyncResponse;
import vgbt.noxalus.com.videogameblindtest.tasks.GetQuizAsyncTask;

public class QuizActivity extends ActionBarActivity implements OnClickListener, OnTouchListener, OnCompletionListener, OnBufferingUpdateListener, AsyncResponse {

    private ImageButton buttonPlayPause;
    private SeekBar seekBarProgress;
    public TextView musicNameTextView;
    public TextView currentTimeTextView;

    public Button answerButton1;
    public Button answerButton2;
    public Button answerButton3;
    public Button answerButton4;

    private MediaPlayer mediaPlayer;
    private int mediaFileLengthInMilliseconds;

    private final Handler handler = new Handler();

    private ArrayList<Question> questions = null;
    private int currentQuestionId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        initView();
        getQuiz();
    }

    @Override
    public void onPause()
    {
        super.onPause();

        if (mediaPlayer.isPlaying())
            mediaPlayer.stop();
    }

    private void getQuiz()
    {
        GetQuizAsyncTask getQuizAsyncTask = new GetQuizAsyncTask();
        getQuizAsyncTask.delegate = this;

        getQuizAsyncTask.execute(getResources().getString(R.string.api));
    }

    private void initView() {
        buttonPlayPause = (ImageButton)findViewById(R.id.ButtonTestPlayPause);
        buttonPlayPause.setOnClickListener(this);

        musicNameTextView = (TextView)findViewById(R.id.musicName);
        currentTimeTextView = (TextView)findViewById(R.id.currentTime);

        answerButton1 = (Button)findViewById(R.id.answerButton1);
        answerButton2 = (Button)findViewById(R.id.answerButton2);
        answerButton3 = (Button)findViewById(R.id.answerButton3);
        answerButton4 = (Button)findViewById(R.id.answerButton4);

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

                    String time = convertMillisecondToTime(mediaFileLengthInMilliseconds);

                    /*
                    if (mediaFileLengthInMilliseconds > 0)
                        mediaPlayer.seekTo(mediaFileLengthInMilliseconds - 100000);
                    */

                    mediaPlayer.start();

                    musicNameTextView.setText("extrait" + questions.get(currentQuestionId).getExtractId() + ".mp3 (" + time + ")");

                    buttonPlayPause.setImageResource(R.drawable.button_pause);
                    primarySeekBarProgressUpdater();
                }
            }
        });

        Button nextButton = (Button)findViewById(R.id.nextButton);
        nextButton.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextQuestion();
            }
        }));
    }

    private void nextQuestion()
    {
        currentQuestionId++;

        answerButton1.setText(questions.get(currentQuestionId).getAnswers().get(0));
        answerButton2.setText(questions.get(currentQuestionId).getAnswers().get(1));
        answerButton3.setText(questions.get(currentQuestionId).getAnswers().get(2));
        answerButton4.setText(questions.get(currentQuestionId).getAnswers().get(3));

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }

        mediaPlayer.reset();

        loadMusic();
    }

    private void loadMusic()
    {
        try {
            String currentMusicUrl = getResources().getString(R.string.baseUrl) + "extrait" + questions.get(currentQuestionId).getExtractId() + ".mp3";
            mediaPlayer.setDataSource(currentMusicUrl);
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void primarySeekBarProgressUpdater() {
        int progress = (int)(((float)mediaPlayer.getCurrentPosition() / mediaFileLengthInMilliseconds) * 100);
        onProgressChanged(seekBarProgress, progress, false);

        String currentPositionText = convertMillisecondToTime(mediaPlayer.getCurrentPosition());

        currentTimeTextView.setText(currentPositionText);

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

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int secondaryPosition = seekBar.getSecondaryProgress();
        if (progress > secondaryPosition)
            seekBar.setProgress(secondaryPosition);
    }

    private String convertMillisecondToTime(int milliseconds)
    {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(milliseconds),
                TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds))
        );
    }

    @Override
    public void processFinish(ArrayList<Question> output) {
        questions = output;
        nextQuestion();
    }
}
