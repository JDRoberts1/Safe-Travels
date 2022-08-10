package com.fullsail.android.safetravels.objects;

public class Post {

    String uId;
    String title;
    String post;
    String date;
    String location;
    String username;
    String datePosted;

    public Post(String uId, String title, String post, String date, String location, String username, String datePosted) {
        this.uId = uId;
        this.title = title;
        this.post = post;
        this.date = date;
        this.location = location;
        this.username = username;
        this.datePosted = datePosted;
    }

    public String getuId() {
        return uId;
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
