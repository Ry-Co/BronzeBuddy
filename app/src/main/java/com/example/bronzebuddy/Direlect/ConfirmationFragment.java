package com.example.bronzebuddy.Direlect;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.bronzebuddy.MainActivity;
import com.example.bronzebuddy.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfirmationFragment extends Fragment implements AccountSetupActivity.ProfilePictureListener {
    private static final String TAG = ConfirmationFragment.class.getSimpleName();

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseStorage mStorage= FirebaseStorage.getInstance();

    Bitmap profilePicture;
    StorageReference storageReference = mStorage.getReference();
    EditText fNameET,lNameET, eMailET, phoneNumberET;
    ImageButton profilePicImageButton, skinToneImageButton;
    Button saveButton;
    ProgressBar progBarSave;

    private AlphaAnimation buttonClick = new AlphaAnimation(.1F, 1F);


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.confirmation_fragment, container, false);
        initLayout(v);
        setListeners();
        return v;
    }

    public void initLayout(View v){
        buttonClick.setDuration(275);
        buttonClick.setFillAfter(true);

        FirebaseUser cu = mAuth.getCurrentUser();
        fNameET = v.findViewById(R.id.fnET);
        lNameET = v.findViewById(R.id.lnET);
        eMailET = v.findViewById(R.id.emET);
        phoneNumberET = v.findViewById(R.id.pnET);
        profilePicImageButton = v.findViewById(R.id.profilePictureButton);
        skinToneImageButton = v.findViewById(R.id.skinToneImageButton);
        saveButton = v.findViewById(R.id.saveButton);
        progBarSave = v.findViewById(R.id.progressBarSave);

        String name1 = cu.getDisplayName().trim();
        String[] name = name1.split(" ");

        fNameET.setText(name[0]);
        lNameET.setText(name[1]);
        eMailET.setText(cu.getEmail());
        phoneNumberET.setText(cu.getPhoneNumber());
        progBarSave.setVisibility(View.GONE);

        profilePicImageButton.setImageURI(cu.getPhotoUrl());
        skinToneImageButton.setBackgroundResource(((AccountSetupActivity)getActivity()).getUsersSkinToneResource());
    }

    public void setListeners(){
        profilePicImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //go to users photos to upload new image and upload profile image
                //check permissions for storage
                changeProfilePictureDialog();

            }
        });

        skinToneImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //go back to skin tone selection screen
                Fragment fragmentA = new SkinToneFragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragContainerLayout, fragmentA).commit();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveButton.startAnimation(buttonClick);
                //save users inputs, put them to the server and go to main activity
                progBarSave.setIndeterminate(true);
                progBarSave.setVisibility(View.VISIBLE);
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("firstName",fNameET.getText().toString());
                userMap.put("lastName",lNameET.getText().toString());
                userMap.put("email",eMailET.getText().toString());
                userMap.put("phoneNumber",phoneNumberET.getText().toString());
                userMap.put("skinTone", ((AccountSetupActivity) getActivity()).getUsersSkinTone());
                userMap.put("userID", mAuth.getCurrentUser().getUid());


                db.collection("users").document(mAuth.getCurrentUser().getUid()).set(userMap);



                UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                        .setDisplayName(fNameET.getText().toString()+" "+lNameET.getText().toString())
                        .build();

                mAuth.getCurrentUser().updateProfile(profileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG,"Profile updated");
                    }
                });


                saveProfilePicture();

                Intent intent = new Intent(getContext(), MainActivity.class);
                startActivity(intent);
            }
        });
    }


    public void changeProfilePictureDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setCancelable(false).setTitle("Change Profile Picture")
                .setMessage("Do you want to change your profile picture?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        choosePicture();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        Dialog dia = builder.create();
        dia.setCanceledOnTouchOutside(false);
        dia.setCancelable(false);
        dia.show();
    }

    public void choosePicture(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        getActivity().startActivityForResult(Intent.createChooser(intent, "Select Picture"),((AccountSetupActivity)getActivity()).getPICK_IMAGE_REQUEST());
    }

    public void saveProfilePicture(){
        new LocalStorageWorker_old(getContext())
                .setFileName(mAuth.getCurrentUser().getUid())
                .setDirectoryName("images")
                .saveImage(true,profilePicture);
    }

    @Override
    public void onProfileBitmapChange() {
        // update UI change here profilePicImageButton
        Bitmap temp = ((AccountSetupActivity)getActivity()).getProfilePictureBitmap();
        String path = getImagePath(((AccountSetupActivity)getActivity()).getProfilePictureURI());
        Bitmap profPic;
        try {
            profPic = modifyOrientation(temp,path);
            profilePicImageButton.setImageBitmap(profPic);
            profilePicture = profPic;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((AccountSetupActivity)getActivity()).registerProfilePictureListener(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ((AccountSetupActivity)getActivity()).unregisterProfilePictureListener(this);
    }

    public static Bitmap modifyOrientation(Bitmap bitmap, String image_absolute_path) throws IOException {
        ExifInterface ei = new ExifInterface(image_absolute_path);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotate(bitmap, 90);

            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotate(bitmap, 180);

            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotate(bitmap, 270);

            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                return flip(bitmap, true, false);

            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                return flip(bitmap, false, true);

            default:
                return bitmap;
        }
    }

    public static Bitmap rotate(Bitmap bitmap, float degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static Bitmap flip(Bitmap bitmap, boolean horizontal, boolean vertical) {
        Matrix matrix = new Matrix();
        matrix.preScale(horizontal ? -1 : 1, vertical ? -1 : 1);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public String getImagePath(Uri uri){
        Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":")+1);
        cursor.close();

        cursor = getActivity().getContentResolver().query(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
    }
}

/*
Uri filePath =((AccountSetupActivity) getActivity()).getProfilePictureURI();
        if (filePath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(getContext());
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            StorageReference ref = storageReference.child("images/" + profilePictureID);
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(getContext(), "Uploaded", Toast.LENGTH_SHORT).show();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getContext(), "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded " + (int) progress + "%");
                        }
                    }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(getContext(), "Profile Created", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getContext(),MainActivity.class);
                        startActivity(intent);
                    }else{
                        Toast.makeText(getContext(), "Error creating profile", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getContext(),MainActivity.class);
                        startActivity(intent);
                    }

                }
            });
        }
 */