package com.example.bronzebuddy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bronzebuddy.Workers.LocalStorageWorker;

public class SkinToneActivity extends AppCompatActivity implements View.OnClickListener {
    ImageView lightestCheck, lighterCheck, lightCheck, darkCheck, darkerCheck, darkestCheck;
    ImageButton lightest, lighter, light, dark, darker, darkest;
    TextView whyTheseColorsTV;
    LocalStorageWorker localStorageWorker;
    private AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.1F);
    private AlphaAnimation buttonClickRev = new AlphaAnimation(0.1F, 1F);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skin_tone);
        localStorageWorker = new LocalStorageWorker(getApplicationContext());
        initLayout();
    }

    public void initLayout() {
        buttonClick.setDuration(275);
        buttonClick.setFillAfter(true);
        buttonClickRev.setDuration(275);
        buttonClickRev.setFillAfter(true);

        whyTheseColorsTV = findViewById(R.id.whyTextView);
        whyTheseColorsTV.setClickable(true);
        whyTheseColorsTV.setMovementMethod(LinkMovementMethod.getInstance());
        String text = "<a href='https://en.wikipedia.org/wiki/Fitzpatrick_scale'> Why these six skin tones? </a>";
        whyTheseColorsTV.setText(Html.fromHtml(text));

        lightestCheck = findViewById(R.id.lightestCheck);
        lighterCheck = findViewById(R.id.lighterCheck);
        lightCheck = findViewById(R.id.lightCheck);
        darkCheck = findViewById(R.id.darkCheck);
        darkerCheck = findViewById(R.id.darkerCheck);
        darkestCheck = findViewById(R.id.darkestCheck);

        lightestCheck.setVisibility(View.GONE);
        lighterCheck.setVisibility(View.GONE);
        lightCheck.setVisibility(View.GONE);
        darkCheck.setVisibility(View.GONE);
        darkerCheck.setVisibility(View.GONE);
        darkestCheck.setVisibility(View.GONE);

        lightest = findViewById(R.id.lightestImageButton);
        lighter = findViewById(R.id.lighterImageButton);
        light = findViewById(R.id.lightImageButton);
        dark = findViewById(R.id.darkImageButton);
        darker = findViewById(R.id.darkerImageButton);
        darkest = findViewById(R.id.darkestImageButton);

        lightest.setOnClickListener(this);
        lighter.setOnClickListener(this);
        light.setOnClickListener(this);
        dark.setOnClickListener(this);
        darker.setOnClickListener(this);
        darkest.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lightestImageButton:
                v.startAnimation(buttonClick);
                lightestCheck.setVisibility(View.INVISIBLE);
                lightestCheck.startAnimation(buttonClick);
                returnIntent(0);
                break;
            case R.id.lighterImageButton:
                v.startAnimation(buttonClick);
                lighterCheck.setVisibility(View.INVISIBLE);
                lighterCheck.startAnimation(buttonClick);
                returnIntent(1);
                break;
            case R.id.lightImageButton:
                v.startAnimation(buttonClick);
                lightCheck.setVisibility(View.INVISIBLE);
                lightCheck.startAnimation(buttonClick);
                returnIntent(2);
                break;
            case R.id.darkImageButton:
                v.startAnimation(buttonClick);
                darkCheck.setVisibility(View.INVISIBLE);
                darkCheck.startAnimation(buttonClick);
                returnIntent(3);
                break;
            case R.id.darkerImageButton:
                v.startAnimation(buttonClick);
                darkerCheck.setVisibility(View.INVISIBLE);
                darkerCheck.startAnimation(buttonClick);
                returnIntent(4);
                break;
            case R.id.darkestImageButton:
                v.startAnimation(buttonClick);
                darkestCheck.setVisibility(View.INVISIBLE);
                darkestCheck.startAnimation(buttonClick);
                returnIntent(5);
                break;
        }
    }

    public void returnIntent(int result) {
        localStorageWorker.saveSkinToneSP(result);
        Intent returnIntent = new Intent();
        returnIntent.putExtra("result", result);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
        return;
    }
}
