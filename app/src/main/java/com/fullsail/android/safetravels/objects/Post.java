package com.fullsail.android.safetravels.objects;

import android.net.Uri;

public class Post {

    String uid;
    String title;
    String post;
    String date;
    String location;
    String username;
    String datePosted;
    Uri profileImgUri, uri1, uri2, uri3, uri4;

    public Post(String uID, String title, String post, String date, String location, String username, String datePosted, Uri profileImgUri, Uri uri1, Uri uri2, Uri uri3, Uri uri4) {
        this.uid = uID;
        this.title = title;
        this.post = post;
        this.date = date;
        this.location = location;
        this.username = username;
        this.datePosted = datePosted;
        this.profileImgUri = profileImgUri;
        this.uri1 = uri1;
        this.uri2 = uri2;
        this.uri3 = uri3;
        this.uri4 = uri4;
    }

    public String getUid() {
        return uid;
    }

    public Uri getProfileImgUri() {
        return profileImgUri;
    }

    public Uri getUri1() {
        return uri1;
    }

    public Uri getUri2() {
        return uri2;
    }

    public Uri getUri3() {
        return uri3;
    }

    public Uri getUri4() {
        return uri4;
    }

    public String getTitle() {
        return title;
    }

    public String getPost() {
        return post;
    }

    public String getDate() {
        return date;
    }

    public String getLocation() {
        return location;
    }

    public String getUsername() {
        return username;
    }

    public String getDatePosted() {
        return datePosted;
    }
}
