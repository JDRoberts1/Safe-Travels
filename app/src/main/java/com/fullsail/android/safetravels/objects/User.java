package com.fullsail.android.safetravels.objects;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {

    String username;
    String uid;
    Uri uri;

    public User(String username, String uid, Uri uri) {
        this.username = username;
        this.uid = uid;
        this.uri = uri;
    }
    
    protected User(Parcel in) {
        username = in.readString();
        uid = in.readString();
        uri = in.readParcelable(Uri.class.getClassLoader());
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getUsername() {
        return username;
    }

    public String getUid() {
        return uid;
    }

    public Uri getUri() {
        return uri;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(username);
        dest.writeString(uid);
        dest.writeParcelable(uri, flags);
    }
}
