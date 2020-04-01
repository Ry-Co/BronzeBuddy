package com.example.bronzebuddy.TanTimerWidget;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bronzebuddy.MainActivity;
import com.example.bronzebuddy.R;

import java.util.Locale;

public class TanTimerActivity extends AppCompatActivity {
    private static final String TAG = TanTimerActivity.class.getSimpleName();
    private static long START_TIME_IN_MILLIS;
    int hours, minutes;
    CountDownTimer mCountDownTimer;
    ProgressBar mProgressBar;
    Button startButton, resetButton;
    TextView timerTV;
    Uri notification;
    Ringtone r;
    Vibrator v;
    private long MINUTE_IN_MILLIS = 60000;
    private long HOUR_IN_MILLIS = 60 * MINUTE_IN_MILLIS;
    private long FLIP_INTERVAL = 10 * MINUTE_IN_MILLIS;//10 * MINUTE_IN_MILLIS;
    private long LAST_FLIP_TIME_STAMP = 0;
    private SoundPool soundPool;
    private boolean mTimerRunning;
    private long mTimeLeftInMillis;
    private long mEndTime;
    private long[] vibratePattern = {0, 100, 1000};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkExtras();

        START_TIME_IN_MILLIS = hours * HOUR_IN_MILLIS + minutes * MINUTE_IN_MILLIS;
        mTimeLeftInMillis = START_TIME_IN_MILLIS;
        getFlipInterval();
        setContentView(R.layout.activity_tan_timer);
        initLayout();
    }

    private void initLayout() {
        notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        mProgressBar = findViewById(R.id.progressBar);
        startButton = findViewById(R.id.startButton);
        resetButton = findViewById(R.id.resetButton);
        timerTV = findViewById(R.id.timerTV);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            soundPool = new SoundPool.Builder()
                    .setMaxStreams(1)
                    .setAudioAttributes(audioAttributes)
                    .build();
        } else {
            soundPool = new SoundPool(1, AudioManager.STREAM_ALARM, 0);
        }
        updateCountDownText();
        setClickListeners();
    }

    private void setClickListeners() {
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTimerRunning) {
                    pauseTimer();
                } else {
                    startTimer();
                }
            }
        });
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTimer();
            }
        });
    }

    public void startTimer() {
        mEndTime = System.currentTimeMillis() + mTimeLeftInMillis;
        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis = millisUntilFinished;
                updateCountDownText();
                checkPings();
            }

            @Override
            public void onFinish() {
                mTimerRunning = false;
                updateButtons();
                playAlarm();
                showFinishedDialog();
                //one more flip or just stop the timer and alert to finish
            }
        }.start();
        mTimerRunning = true;
        updateButtons();
    }

    public void pauseTimer() {
        mCountDownTimer.cancel();
        mTimerRunning = false;
        updateButtons();
    }

    public void resetTimer() {
        mTimeLeftInMillis = START_TIME_IN_MILLIS;
        updateCountDownText();
        updateButtons();
    }

    private void updateCountDownText() {
        int seconds = (int) (mTimeLeftInMillis / 1000) % 60;
        int minutes = (int) ((mTimeLeftInMillis / (1000 * 60)) % 60);
        int hours = (int) ((mTimeLeftInMillis / (1000 * 60 * 60)) % 24);
        String timeLeftFormatted = String.format(Locale.getDefault(), "%2d:%02d:%02d", hours, minutes, seconds);
        timerTV.setText(timeLeftFormatted);
        updateProgressBar();
    }

    private void updateButtons() {
        if (mTimerRunning) {
            resetButton.setVisibility(View.INVISIBLE);
            startButton.setText("Pause");
        } else {
            startButton.setText("Start");

            if (mTimeLeftInMillis < 1000) {
                startButton.setVisibility(View.INVISIBLE);
            } else {
                startButton.setVisibility(View.VISIBLE);
            }

            if (mTimeLeftInMillis < START_TIME_IN_MILLIS) {
                resetButton.setVisibility(View.VISIBLE);
            } else {
                resetButton.setVisibility(View.INVISIBLE);
            }
        }
    }

    public void updateProgressBar() {
        int x = (int) (100 * (float) mTimeLeftInMillis / (float) START_TIME_IN_MILLIS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mProgressBar.setProgress(x, true);
        } else {
            mProgressBar.setProgress(x);
        }
    }

    private void playAlarm() {
        try {
            pauseTimer();
            r.play();
            startVibrate();
            showFlipDialog();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopAlarm() {
        try {
            r.stop();
            stopVibrate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startVibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createWaveform(vibratePattern, 0));
        } else {
            //deprecated in API 26
            v.vibrate(vibratePattern, 0);
        }
    }

    private void stopVibrate() {
        v.cancel();
    }

    private void checkPings() {
        long totalElapsedTime = START_TIME_IN_MILLIS - mTimeLeftInMillis;
        long elapsedTimeSinceLastFlip = totalElapsedTime - LAST_FLIP_TIME_STAMP;
        if (LAST_FLIP_TIME_STAMP == 0) {
            if (totalElapsedTime > FLIP_INTERVAL) {
                LAST_FLIP_TIME_STAMP = totalElapsedTime;
                //Log.e(TAG, "FLIPPING USER:: LFTS:" + LAST_FLIP_TIME_STAMP + " TET:" + totalElapsedTime + " ETSLF:" + elapsedTimeSinceLastFlip);
                playAlarm();
            }
        } else {
            if (elapsedTimeSinceLastFlip > FLIP_INTERVAL) {
                LAST_FLIP_TIME_STAMP = totalElapsedTime;
                //Log.e(TAG, "FLIPPING USER:: LFTS:" + LAST_FLIP_TIME_STAMP + " TET:" + totalElapsedTime + " ETSLF:" + elapsedTimeSinceLastFlip);
                playAlarm();
            }
        }
    }

    private void getFlipInterval() {
        if (mTimeLeftInMillis > 30 * MINUTE_IN_MILLIS) {
            //leave default flip interval of 10 minutes
        } else {
            FLIP_INTERVAL = START_TIME_IN_MILLIS / 2;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        soundPool.release();
        soundPool = null;
    }

    private void checkExtras() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            for (String key : extras.keySet()) {
                switch (key) {
                    case "hours":
                        hours = extras.getInt(key);
                        break;
                    case "minutes":
                        minutes = extras.getInt(key);
                        break;
                }
            }

        }
    }

    public void showFlipDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Flip!")
                .setMessage("Flip over and start the timer again for an even tan!")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        stopAlarm();
                        startTimer();
                    }
                });
        Dialog dia = builder.create();
        dia.setCanceledOnTouchOutside(false);
        dia.setCancelable(false);
        dia.show();
    }

    public void showFinishedDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Finished!")
                .setMessage("Take a break from the sun and re-hydrate!")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        stopAlarm();
                        Intent i = new Intent(TanTimerActivity.this, MainActivity.class);
                        startActivity(i);
                    }
                });
        Dialog dia = builder.create();
        dia.setCanceledOnTouchOutside(false);
        dia.setCancelable(false);
        dia.show();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("millisLeft", mTimeLeftInMillis);
        outState.putBoolean("timerRunning", mTimerRunning);
        outState.putLong("endTime", mEndTime);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mTimeLeftInMillis = savedInstanceState.getLong("millisLeft");
        mTimerRunning = savedInstanceState.getBoolean("timerRunning");
        updateCountDownText();
        updateButtons();

        if (mTimerRunning) {
            mEndTime = savedInstanceState.getLong("endTime");
            mTimeLeftInMillis = mEndTime - System.currentTimeMillis();
            startTimer();
        }
    }
}
