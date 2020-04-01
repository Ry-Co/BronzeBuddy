package com.example.bronzebuddy.Workers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class LocalStorageWorker {
    private static final String TAG = LocalStorageWorker.class.getSimpleName();
    Context mContext;
    boolean external = false;
    SharedPreferences settings;
    SharedPreferences.Editor editor;
    String PREFS_NAME = "BBSharedPrefs";
    private String directoryName, fileName;

    public LocalStorageWorker(Context context) {
        mContext = context;
        settings = mContext.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();
        Log.i(TAG, "LocalStorageWorker:SUCCESS:Worker built");
    }

    public void saveSkinToneSP(int skinToneInt) {
        editor.putInt("skinTone", skinToneInt);
        editor.apply();
        Log.i(TAG, "LocalStorageWorker:SUCCESS:Skin tone saved");

    }

    public int loadSkinToneSP() {
        return settings.getInt("skinTone", -1);
    }
}
