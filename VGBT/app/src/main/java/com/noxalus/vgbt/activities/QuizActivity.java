package com.noxalus.vgbt.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import com.noxalus.vgbt.R;
import com.noxalus.vgbt.entities.Question;
import com.noxalus.vgbt.tasks.AsyncResponse;
import com.noxalus.vgbt.tasks.GetQuizAsyncTask;

public class QuizActivity extends Activity implements OnClickListener, OnTouchListener, OnCompletionListener, OnBufferingUpdateListener, AsyncResponse {

    private ImageButton buttonPlayPause;
    private SeekBar seekBarProgress;
    public TextView musicNameTextView;
    public TextView currentTimeTextView;
    public TextView scoreTextView;
    public ArrayList<ImageView> lifeIconImageViewList;

    private HashMap<Integer, Button> answerButtonMap;

    private boolean mediaPlayerIsReleased = false;
    private boolean waitToPlayMusic = false;
    private boolean musicIsReady = false;

    private boolean answersReady = false;

    Handler nextQuestionHandler = new Handler();

    private MediaPlayer mediaPlayer;
    private int mediaFileLengthInMilliseconds;

    private final Handler handler = new Handler();

    private ArrayList<Question> questions = null;
    private int currentQuestionId = -1;
    private int score = 0;
    private int life = 3;
    private boolean answerGiven = false;

    String mode;

    MediaPlayer correctSound;
    MediaPlayer wrongSound;

    long startTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        initView();
        getQuiz();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onStart()
    {
        super.onStart();
    }

    @Override
    public void onStop()
    {
        super.onStop();

        mediaPlayer.release();
        mediaPlayerIsReleased = true;
    }

    @Override
    public void onPause()
    {
        super.onPause();

        if (!mediaPlayerIsReleased && mediaPlayer.isPlaying())
            mediaPlayer.stop();
    }


    private void updateAnswers()
    {
        for (int i = 0; i < 4; i++) {
            answerButtonMap.get(i).setBackgroundResource(R.drawable.button);
            answerButtonMap.get(i).setText(questions.get(currentQuestionId).getAnswers().get(i));
        }
    }

    private void getQuiz()
    {
        // AsyncTask can't be executed multiple times
        // we need to create a new instance each time
        GetQuizAsyncTask getQuizAsyncTask = new GetQuizAsyncTask();
        getQuizAsyncTask.delegate = this;

        getQuizAsyncTask.execute(getResources().getString(R.string.api) + "?type=" + mode + "&questionNumber=" + getResources().getInteger(R.integer.number_of_question_to_ask));
    }

    @Override
    public void processFinish(ArrayList<Question> output) {
        if (!mediaPlayerIsReleased) {
            questions = output;
            answersReady = true;
            nextQuestion();
            updateAnswers();
            loadMusic();
        }
    }

    private void initView() {
        correctSound = MediaPlayer.create(QuizActivity.this, R.raw.correct);
        wrongSound = MediaPlayer.create(QuizActivity.this, R.raw.wrong);

        buttonPlayPause = (ImageButton)findViewById(R.id.imageButton);
        buttonPlayPause.setOnClickListener(this);

        //musicNameTextView = (TextView)findViewById(R.id.musicName);
        //currentTimeTextView = (TextView)findViewById(R.id.currentTime);

        scoreTextView = (TextView)findViewById(R.id.scoreTextView);

        nextQuestionHandler = new Handler();

        // Create life icons
        lifeIconImageViewList = new ArrayList<ImageView>();
        for (int i = 0; i < life; i++) {
            ImageView lifeIcon = new ImageView(getApplicationContext());
            lifeIcon.setImageDrawable(getResources().getDrawable(R.drawable.life));
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(64, 64);
            layoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, scoreTextView.getId());
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);

            if (i == 0)
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            else
                layoutParams.addRule(RelativeLayout.LEFT_OF, 4242 + i - 1);

            lifeIcon.setLayoutParams(layoutParams);

            RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.RootRelativeLayout);

            lifeIcon.setId(4242 + i);

            lifeIconImageViewList.add(lifeIcon);

            relativeLayout.addView(lifeIcon);
        }

        mode = getIntent().getStringExtra("mode");

        answerButtonMap = new HashMap<Integer, Button>();

        answerButtonMap.put(0, (Button)findViewById(R.id.answerButton1));
        answerButtonMap.put(1, (Button)findViewById(R.id.answerButton2));
        answerButtonMap.put(2, (Button)findViewById(R.id.answerButton3));
        answerButtonMap.put(3, (Button)findViewById(R.id.answerButton4));

        for (int i = 0; i < 4; i++) {
            ButtonClickListener buttonClickListener = new ButtonClickListener(i, answerButtonMap.get(i));
            answerButtonMap.get(i).setOnClickListener(buttonClickListener);
        }

        /*
        seekBarProgress = (SeekBar)findViewById(R.id.SeekBarTestPlay);
        seekBarProgress.setMax(99); // It means 100% .0-99
        seekBarProgress.setOnTouchListener(this);
        */

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnCompletionListener(this);

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                musicIsReady = true;
                if (!mediaPlayer.isPlaying() && !waitToPlayMusic) {
                    playMusic();
                }
            }
        });
    }

    private void playMusic()
    {
        mediaFileLengthInMilliseconds = mediaPlayer.getDuration();

        String time = convertMillisecondToTime(mediaFileLengthInMilliseconds);

        /*
        if (mediaFileLengthInMilliseconds > 0)
            mediaPlayer.seekTo(mediaFileLengthInMilliseconds - 100000);
        */

        mediaPlayer.start();
        buttonPlayPause.setImageResource(R.drawable.button_pause);

        startTime = System.currentTimeMillis();

        //musicNameTextView.setText("extrait" + questions.get(currentQuestionId).getExtractId() + ".mp3 (" + time + ")");
        //primarySeekBarProgressUpdater();
    }

    private void nextQuestion()
    {
        currentQuestionId++;

        if (currentQuestionId >= questions.size())
        {
            currentQuestionId = -1;
            answersReady = false;
            getQuiz();
        }
    }

    private void loadMusic()
    {
        try {
            String currentMusicUrl = getResources().getString(R.string.api)  + "/mp3.php?id=" + questions.get(currentQuestionId).getExtractId() + "&time=" + getResources().getInteger(R.integer.second_to_stream);
            mediaPlayer.setDataSource(currentMusicUrl);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.prepareAsync();
            musicIsReady = false;
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
        if(v.getId() == R.id.imageButton){
            loadMusic();

            if(!mediaPlayer.isPlaying()){
                mediaPlayer.start();
                buttonPlayPause.setImageResource(R.drawable.button_pause);
            }else {
                mediaPlayer.pause();
                buttonPlayPause.setImageResource(R.drawable.button_play);
            }

            //primarySeekBarProgressUpdater();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        /*
        if(v.getId() == R.id.SeekBarTestPlay){
            if(mediaPlayer.isPlaying()) {
                SeekBar sb = (SeekBar)v;
                int playPositionInMilliseconds = (mediaFileLengthInMilliseconds / 100) * sb.getProgress();
                mediaPlayer.seekTo(playPositionInMilliseconds);
            }
        }
        */
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        buttonPlayPause.setImageResource(R.drawable.button_play);
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        //seekBarProgress.setSecondaryProgress(percent);
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

    public class ButtonClickListener implements View.OnClickListener
    {
        int id;
        Button button;

        public ButtonClickListener(int id, Button button)
        {
            super();

            this.id = id;
            this.button = button;
        }

        @Override
        public void onClick(View v)
        {
            if (!answerGiven && answersReady) {
                if (questions.get(currentQuestionId).getAnswerIndex() == id) {
                    button.setBackgroundResource(R.drawable.button_correct);
                    correctSound.start();

                    long millis = System.currentTimeMillis() - startTime;
                    int seconds = (int) (millis / 1000);
                    seconds = seconds % 60;

                    int maxPointForExtract = getResources().getInteger(R.integer.max_point_for_extract);
                    int maxSecondToAnswer = getResources().getInteger(R.integer.max_seconds_to_answer);
                    float factor = (float)maxSecondToAnswer / (float)maxPointForExtract;

                    if (seconds >= maxSecondToAnswer || millis < 750)
                        score++;
                    else
                        score += Math.ceil((maxSecondToAnswer - seconds) / factor);

                    scoreTextView.setText(Integer.toString(score));
                } else {
                    wrongSound.start();

                    int answerId = questions.get(currentQuestionId).getAnswerIndex();
                    answerButtonMap.get(answerId).setBackgroundResource(R.drawable.button_correct);
                    button.setBackgroundResource(R.drawable.button_wrong);
                    life--;

                    lifeIconImageViewList.get(life).setImageDrawable(getResources().getDrawable(R.drawable.life_empty));
                }

                answerGiven = true;

                if (life > 0) {
                    if (mediaPlayer.isPlaying())
                        mediaPlayer.stop();

                    mediaPlayer.reset();

                    waitToPlayMusic = true;

                    nextQuestion();
                    loadMusic();
                }

                nextQuestionHandler.postDelayed(new Runnable() {
                    public void run() {
                        if (life <= 0)
                        {
                            if (mediaPlayer.isPlaying())
                                mediaPlayer.stop();

                            mediaPlayer.release();
                            mediaPlayerIsReleased = true;

                            Intent intent = new Intent(QuizActivity.this, ResultActivity.class);
                            intent.putExtra("score", score);
                            intent.putExtra("mode", mode);
                            startActivity(intent);
                        }
                        else {
                            answerGiven = false;
                            if (!mediaPlayer.isPlaying() && musicIsReady)
                                playMusic();
                            else
                                waitToPlayMusic = false;

                            updateAnswers();
                        }
                    }
                }, 1500);
            }
        }
    }
}
