package com.fullsail.android.safetravels.objects;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class Post implements Parcelable {

    String uid;
    String title;
    String post;
    String date;
    String location;
    String username;
    String datePosted;
    Uri profileImgUri, uri1, uri2, uri3, uri4;
    String postId;

    public Post(String uid, String title, String post, String date, String location, String username, String datePosted, Uri profileImgUri, Uri uri1, Uri uri2, Uri uri3, Uri uri4) {
        this.uid = uid;
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

    protected Post(Parcel in) {
        uid = in.readString();
        title = in.readString();
        post = in.readString();
        date = in.readString();
        location = in.readString();
        username = in.readString();
        datePosted = in.readString();
        profileImgUri = in.readParcelable(Uri.class.getClassLoader());
        uri1 = in.readParcelable(Uri.class.getClassLoader());
        uri2 = in.readParcelable(Uri.class.getClassLoader());
        uri3 = in.readParcelable(Uri.class.getClassLoader());
        uri4 = in.readParcelable(Uri.class.getClassLoader());
        postId = in.readString();
    }

    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel in) {
            return new Post(in);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

    public String getPostId() { return postId; }

    public void setPostId(String postId) {
        this.postId = postId;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(uid);
        dest.writeString(title);
        dest.writeString(post);
        dest.writeString(date);
        dest.writeString(location);
        dest.writeString(username);
        dest.writeString(datePosted);
        dest.writeParcelable(profileImgUri, flags);
        dest.writeParcelable(uri1, flags);
        dest.writeParcelable(uri2, flags);
        dest.writeParcelable(uri3, flags);
        dest.writeParcelable(uri4, flags);
        dest.writeString(postId);
    }

}
