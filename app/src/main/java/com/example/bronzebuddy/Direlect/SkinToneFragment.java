package com.example.bronzebuddy.Direlect;

import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.bronzebuddy.Direlect.ConfirmationFragment;
import com.example.bronzebuddy.R;

public class SkinToneFragment extends Fragment implements View.OnClickListener{
    ImageView lightestCheck, lighterCheck, lightCheck, darkCheck, darkerCheck, darkestCheck;
    ImageButton lightest, lighter, light, dark, darker, darkest;
    TextView whyTheseColorsTV;
    private AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.1F);
    private AlphaAnimation buttonClickRev = new AlphaAnimation(0.1F, 1F);
    private SkinToneListener listener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.skin_tone_fragment, container, false);
        initLayout(v);
        return v;
    }

    public void initLayout(View v) {
        buttonClick.setDuration(275);
        buttonClick.setFillAfter(true);
        buttonClickRev.setDuration(275);
        buttonClickRev.setFillAfter(true);

        whyTheseColorsTV = v.findViewById(R.id.whyTextView);
        whyTheseColorsTV.setClickable(true);
        whyTheseColorsTV.setMovementMethod(LinkMovementMethod.getInstance());
        String text = "<a href='https://en.wikipedia.org/wiki/Fitzpatrick_scale'> Why these six skin tones? </a>";
        whyTheseColorsTV.setText(Html.fromHtml(text));

        lightestCheck = v.findViewById(R.id.lightestCheck);
        lighterCheck = v.findViewById(R.id.lighterCheck);
        lightCheck = v.findViewById(R.id.lightCheck);
        darkCheck = v.findViewById(R.id.darkCheck);
        darkerCheck = v.findViewById(R.id.darkerCheck);
        darkestCheck = v.findViewById(R.id.darkestCheck);

        lightestCheck.setVisibility(View.GONE);
        lighterCheck.setVisibility(View.GONE);
        lightCheck.setVisibility(View.GONE);
        darkCheck.setVisibility(View.GONE);
        darkerCheck.setVisibility(View.GONE);
        darkestCheck.setVisibility(View.GONE);

        lightest = v.findViewById(R.id.lightestImageButton);
        lighter = v.findViewById(R.id.lighterImageButton);
        light = v.findViewById(R.id.lightImageButton);
        dark = v.findViewById(R.id.darkImageButton);
        darker = v.findViewById(R.id.darkerImageButton);
        darkest = v.findViewById(R.id.darkestImageButton);

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
                listener.onSkinToneSelected(0);
                sendToConfirmationFragment();
                break;
            case R.id.lighterImageButton:
                v.startAnimation(buttonClick);
                lighterCheck.setVisibility(View.INVISIBLE);
                lighterCheck.startAnimation(buttonClick);
                listener.onSkinToneSelected(1);
                sendToConfirmationFragment();
                break;
            case R.id.lightImageButton:
                v.startAnimation(buttonClick);
                lightCheck.setVisibility(View.INVISIBLE);
                lightCheck.startAnimation(buttonClick);
                listener.onSkinToneSelected(2);
                sendToConfirmationFragment();
                break;
            case R.id.darkImageButton:
                v.startAnimation(buttonClick);
                darkCheck.setVisibility(View.INVISIBLE);
                darkCheck.startAnimation(buttonClick);
                listener.onSkinToneSelected(3);
                sendToConfirmationFragment();
                break;
            case R.id.darkerImageButton:
                v.startAnimation(buttonClick);
                darkerCheck.setVisibility(View.INVISIBLE);
                darkerCheck.startAnimation(buttonClick);
                listener.onSkinToneSelected(4);
                sendToConfirmationFragment();
                break;
            case R.id.darkestImageButton:
                v.startAnimation(buttonClick);
                darkestCheck.setVisibility(View.INVISIBLE);
                darkestCheck.startAnimation(buttonClick);
                listener.onSkinToneSelected(5);
                sendToConfirmationFragment();
                break;
        }
    }

    public void sendToConfirmationFragment(){
        Fragment fragmentB = new ConfirmationFragment();
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragContainerLayout, fragmentB).commit();
    }


    public interface SkinToneListener{
        void onSkinToneSelected(int skinTone);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if( context instanceof SkinToneListener){
            listener = (SkinToneListener) context;
        }else{
            throw new RuntimeException(context.toString()
            + " must implement skinTone listener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}



