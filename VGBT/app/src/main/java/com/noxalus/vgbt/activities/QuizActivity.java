package com.noxalus.vgbt.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.noxalus.vgbt.R;
import com.noxalus.vgbt.entities.GameSerie;
import com.noxalus.vgbt.entities.Question;
import com.noxalus.vgbt.tasks.GetQuizAsyncResponse;
import com.noxalus.vgbt.tasks.GetQuizAsyncTask;

public class QuizActivity extends Activity implements OnClickListener, OnCompletionListener, GetQuizAsyncResponse {

    private ImageButton buttonPlayPause;
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
    public void onResume()
    {
        super.onResume();

        if (!mediaPlayer.isPlaying()) {
            waitToPlayMusic = false;
            playMusic();
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();

        waitToPlayMusic = true;

        if (!mediaPlayerIsReleased)
            mediaPlayer.pause();
    }

    private void updateAnswers()
    {
        for (int i = 0; i < 4; i++) {
            answerButtonMap.get(i).setBackgroundResource(R.drawable.button);
            answerButtonMap.get(i).setText(questions.get(currentQuestionId).getAnswers().get(i));
        }
    }

    private String getExcludeGameSeries()
    {
        SharedPreferences settings = getSharedPreferences("VGBT", 0);

        Set<String> savedExcludeGameSeries = settings.getStringSet("excludeGameSeries", null);

        String excludeGameSeries = "";
        boolean firstElement = true;
        if (savedExcludeGameSeries != null) {
            for (String excludeGameSerie : savedExcludeGameSeries) {
                if (firstElement) {
                    excludeGameSeries += excludeGameSerie;
                    firstElement = false;
                }
                else
                    excludeGameSeries += "," + excludeGameSerie;
            }
        }

        return excludeGameSeries;
    }

    private String getExcludeGames()
    {
        SharedPreferences settings = getSharedPreferences("VGBT", 0);

        Set<String> savedExcludeGames = settings.getStringSet("excludeGames", null);

        String excludeGames = "";
        boolean firstElement = true;
        if (savedExcludeGames != null) {
            for (String excludeGame : savedExcludeGames) {
                if (firstElement) {
                    excludeGames += excludeGame;
                    firstElement = false;
                }
                else
                    excludeGames += "," + excludeGame;
            }
        }

        return excludeGames;
    }

    private void getQuiz()
    {
        // AsyncTask can't be executed multiple times
        // we need to create a new instance each time
        GetQuizAsyncTask getQuizAsyncTask = new GetQuizAsyncTask();
        getQuizAsyncTask.delegate = this;

        String excludeGameSeries = getExcludeGameSeries();
        if (excludeGameSeries.length() > 0)
            excludeGameSeries = "&excludeGameSeries=" + excludeGameSeries;

        String excludeGames = getExcludeGames();
        if (excludeGames.length() > 0)
            excludeGames = "&excludeGames=" + excludeGames;

        getQuizAsyncTask.execute(getResources().getString(R.string.api) + "?type=" + mode + excludeGameSeries + excludeGames + "&questionNumber=" + getResources().getInteger(R.integer.number_of_question_to_ask));
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

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
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
        mediaPlayer.start();
        buttonPlayPause.setImageResource(R.drawable.button_pause);

        startTime = System.currentTimeMillis();
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
            String currentMusicUrl = getResources().getString(R.string.api)  + "mp3.php?id=" + questions.get(currentQuestionId).getExtractId() + "&time=" + getResources().getInteger(R.integer.second_to_stream);
            mediaPlayer.setDataSource(currentMusicUrl);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.prepareAsync();
            musicIsReady = false;
        } catch (Exception e) {
            e.printStackTrace();
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
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        buttonPlayPause.setImageResource(R.drawable.button_play);
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
                            finish();
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
