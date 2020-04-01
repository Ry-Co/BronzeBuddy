package com.example.bronzebuddy.Direlect;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class userObject implements Parcelable {

    String skinTone, name, location, userID;
    Bitmap userProfilePicture;

    public userObject(String sT, String na, String loc, String uID, Bitmap userPP){
        skinTone = sT;
        name = na;
        location = loc;
        userID = uID;
        userProfilePicture = userPP;
    }

    protected userObject(Parcel in) {
        skinTone = in.readString();
        name = in.readString();
        location = in.readString();
        userID = in.readString();
        userProfilePicture = in.readParcelable(Bitmap.class.getClassLoader());
    }

    public static final Creator<userObject> CREATOR = new Creator<userObject>() {
        @Override
        public userObject createFromParcel(Parcel in) {
            return new userObject(in);
        }

        @Override
        public userObject[] newArray(int size) {
            return new userObject[size];
        }
    };

    public String getSkinTone() {
        return skinTone;
    }

    public void setSkinTone(String skinTone) {
        this.skinTone = skinTone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public Bitmap getUserProfilePicture() {
        return userProfilePicture;
    }

    public void setUserProfilePicture(Bitmap userProfilePicture) {
        this.userProfilePicture = userProfilePicture;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(skinTone);
        dest.writeString(name);
        dest.writeString(location);
        dest.writeString(userID);
        dest.writeParcelable(userProfilePicture, flags);
    }
}
