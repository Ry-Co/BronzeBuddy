package com.example.bronzebuddy.Direlect;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import com.example.bronzebuddy.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AccountSetupActivity extends AppCompatActivity implements SkinToneFragment.SkinToneListener {
    int usersSkinTone;
    Bitmap profilePictureBitmap;
    private final int PICK_IMAGE_REQUEST = 71;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseStorage mStorage = FirebaseStorage.getInstance();
    StorageReference sRef = mStorage.getReference();
    private Uri filePath;
    private List<ProfilePictureListener> mListeners;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListeners = new ArrayList<>();
        usersSkinTone = -1;
        setContentView(R.layout.activity_account_setup);
        getSupportActionBar().setTitle("Account Setup");
        Fragment fragmentA = new SkinToneFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragContainerLayout, fragmentA).commit();
    }

    @Override
    public void onSkinToneSelected(int skinTone) {
        usersSkinTone = skinTone;
    }

    public int getUsersSkinTone(){
        return usersSkinTone;
    }

    public int getUsersSkinToneResource(){
        if(usersSkinTone == 0){
            return R.color.lightestSkinTone;
        }else if(usersSkinTone == 1){
            return R.color.lighterSkinTone;
        }else if(usersSkinTone == 2){
            return R.color.lightSkinTone;
        }else if(usersSkinTone == 3){
            return R.color.darkSkinTone;
        }else if(usersSkinTone == 4){
            return R.color.darkerSkinTone;
        }else if(usersSkinTone == 5){
            return R.color.darkestSkinTone;
        }else{
            return -1;
        }
    }

    public int getPICK_IMAGE_REQUEST(){
        return PICK_IMAGE_REQUEST;
    }

    public Bitmap getProfilePictureBitmap(){
        return profilePictureBitmap;
    }

    public Uri getProfilePictureURI(){
        return filePath;
    }

    public void setProfilePictureBitmap(Bitmap b){
        profilePictureBitmap = b;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null ){
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                setProfilePictureBitmap(bitmap);
                profilePictureChanged();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public interface ProfilePictureListener{
        void onProfileBitmapChange();
    }

    public synchronized void registerProfilePictureListener(ProfilePictureListener listener){
        mListeners.add(listener);
    }

    public synchronized void unregisterProfilePictureListener(ProfilePictureListener listener){
        mListeners.remove(listener);
    }

    public synchronized void profilePictureChanged(){
        for(ProfilePictureListener listener : mListeners){
            listener.onProfileBitmapChange();
        }
    }

}
