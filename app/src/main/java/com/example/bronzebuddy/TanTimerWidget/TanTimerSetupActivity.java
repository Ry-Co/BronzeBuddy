package com.example.bronzebuddy.TanTimerWidget;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bronzebuddy.R;
import com.example.bronzebuddy.SkinToneActivity;
import com.example.bronzebuddy.Workers.LocalStorageWorker;

public class TanTimerSetupActivity extends AppCompatActivity {
    private static final String TAG = TanTimerSetupActivity.class.getSimpleName();

    LocalStorageWorker localStorageWorker;
    ImageButton skinToneImageButton;
    SeekBar SPFbar;
    TextView SPFnumberTV, warningTV;
    NumberPicker hourPicker, minutePicker;
    Button startTimerButton;

    private double localUVI;
    private double SPF = 30;
    private int usersSkinTone;
    private int SKIN_TONE_REQUEST_CODE = 02;
    private int minutes, hours;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tan_timer_setup);
        initLayout();
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkExtras();
        localStorageWorker = new LocalStorageWorker(this);
        if (localStorageWorker.loadSkinToneSP() == -1) {
            Intent i = new Intent(this, SkinToneActivity.class);
            startActivityForResult(i, SKIN_TONE_REQUEST_CODE);
        } else {
            usersSkinTone = localStorageWorker.loadSkinToneSP();
            int suggestedTimeInMinutes = calcSuggestedTime();
            minutes = suggestedTimeInMinutes % 60;
            hours = Math.round(suggestedTimeInMinutes / 60);
            setPickers(hours, minutes);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setSkinToneButton();
    }

    public void initLayout() {
        getSupportActionBar().setTitle("Tan Timer");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        skinToneImageButton = findViewById(R.id.skinToneImageButton);
        setSkinToneButton();
        SPFbar = findViewById(R.id.spfBar);
        setSeekBarListeners();
        warningTV = findViewById(R.id.warningTV);
        SPFnumberTV = findViewById(R.id.SPFnumberTV);
        hourPicker = findViewById(R.id.hourNumberPicker);
        minutePicker = findViewById(R.id.minuteHourPicker);
        setupPickers();
        startTimerButton = findViewById(R.id.startTimerButton);
        startTimerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hours = hourPicker.getValue();
                minutes = minutePicker.getValue();
                //start timer activity with selected time
                //set reminders for turning and sunscreen application
                Intent intent = new Intent(TanTimerSetupActivity.this, TanTimerActivity.class);
                //put hours and minutes into INT extras
                intent.putExtra("hours", hours);
                intent.putExtra("minutes", minutes);
                startActivity(intent);


            }
        });

        skinToneImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TanTimerSetupActivity.this, SkinToneActivity.class);
                startActivity(intent);
            }
        });

    }

    public void setSeekBarListeners() {
        SPFbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //Toast.makeText(getContext(),"seekbar progress: "+progress, Toast.LENGTH_SHORT).show();
                if (progress * 5 == 0) {
                    showWarning();
                } else {
                    warningTV.setVisibility(View.GONE);
                }
                SPFnumberTV.setText(Integer.toString(5 * progress));
                SPF = 5 * progress;
                int suggestedTimeInMinutes = calcSuggestedTime();
                minutes = suggestedTimeInMinutes % 60;
                hours = Math.round(suggestedTimeInMinutes / 60);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(getContext(),"seekbar touch started!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(getContext(),"seekbar touch stopped!", Toast.LENGTH_SHORT).show();
                if (hours > 24) {
                    warningTV.setText("Timer Maxed Out!");
                    warningTV.setVisibility(View.VISIBLE);
                } else if (hours == 0 && minutes == 0) {
                    warningTV.setText("[It is not recommended to sunbathe without protection!]");
                    warningTV.setVisibility(View.VISIBLE);
                } else {
                    warningTV.setVisibility(View.INVISIBLE);
                }
                setPickers(hours, minutes);
            }
        });
    }

    public void setupPickers() {
        hourPicker.setMinValue(0);
        hourPicker.setMaxValue(23);
        hourPicker.setWrapSelectorWheel(true);
        minutePicker.setMinValue(00);
        minutePicker.setMaxValue(59);
        minutePicker.setWrapSelectorWheel(true);
    }

    public void checkExtras() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            for (String key : extras.keySet()) {
                switch (key) {
                    case "UVI":
                        localUVI = extras.getDouble(key);
                        break;
                    case "skinTone":
                        usersSkinTone = extras.getInt(key);
                        break;
                }
            }

        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    } //enabling back arrow

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == SKIN_TONE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                usersSkinTone = data.getIntExtra("result", -1);
                getIntent().removeExtra("skinTone");
                getIntent().putExtra("skinTone", usersSkinTone);
            }
            if (resultCode == RESULT_CANCELED) {
                //maybe re prompt user to select skin tone
            }
        }
    }

    public void setPickers(int hours, int minutes) {
        hourPicker.setValue(hours);
        minutePicker.setValue(minutes);
    }

    public void showWarning() {
        warningTV.setVisibility(View.VISIBLE);
    }

    public int calcSuggestedTime() {
        double uviMult = getUVIMult();
        double stMult = getSTMult();
        return Math.round((float) ((stMult * uviMult) * (SPF * 1 / 2)));
    }

    public double getUVIMult() {
        if (localUVI <= 5.5) {
            return (double) 1;
        } else if (localUVI <= 8.5) {
            return (double) 1 / 2;
        } else if (localUVI <= 10.5) {
            return (double) 1 / 3;
        } else {
            return (double) 1 / 4;
        }
    }

    public double getSTMult() {
        double d = 0.0;
        switch (usersSkinTone) {
            case 0:
                d = 10;
                break;
            case 1:
                d = 15;
                break;
            case 2:
                d = 30;
                break;
            case 3:
                d = 35;
                break;
            case 4:
                d = 45;
                break;
            case 5:
                d = 90;
                break;
        }
        return d;
    }

    public void setSkinToneButton() {
        switch (usersSkinTone) {
            case -1:
                skinToneImageButton.setImageResource(R.color.colorAccent);
                break;
            case 0:
                skinToneImageButton.setImageResource(R.color.lightestSkinTone);
                break;
            case 1:
                skinToneImageButton.setImageResource(R.color.lighterSkinTone);
                break;
            case 2:
                skinToneImageButton.setImageResource(R.color.lightSkinTone);
                break;
            case 3:
                skinToneImageButton.setImageResource(R.color.darkSkinTone);
                break;
            case 4:
                skinToneImageButton.setImageResource(R.color.darkerSkinTone);
                break;
            case 5:
                skinToneImageButton.setImageResource(R.color.darkestSkinTone);
                break;
        }
    }

}
