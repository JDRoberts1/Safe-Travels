package com.fullsail.android.safetravels;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.fullsail.android.safetravels.adapters.HomePostListAdapter;
import com.fullsail.android.safetravels.objects.Post;
import com.fullsail.android.safetravels.objects.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeActivity extends AppCompatActivity{

    public static final String TAG = "HomeActivity";
    BottomNavigationView navView;
    TextView welcomeLabel;
    CircleImageView iv;
    ListView blogLV;
    HomePostListAdapter adapter;
    FirebaseFirestore db;
    StorageReference storageReference;
    CollectionReference cR;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();


    ArrayList<Post> posts = new ArrayList<>();
    ArrayList<User> users = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        welcomeLabel = findViewById(R.id.welcomeLabel);
        iv = findViewById(R.id.profile_img_main);
        navView = findViewById(R.id.navView);

        savePosts();
        displayInfo();
        setUpBottomNav();
    }

    // OnItemClickListener for when a user taps on a post
    AdapterView.OnItemClickListener itemClick = (parent, view, position, id) -> {
        Post selectedPost = posts.get(position);
        Intent i = new Intent(HomeActivity.this, ViewPostActivity.class);
        i.putExtra(TAG, selectedPost);
        startActivity(i);
    };

    // Method to display all travel posts in blogPOst collection.
    private void displayPosts() {
        blogLV = findViewById(R.id.recent_Posts_LV);
        adapter = new HomePostListAdapter(this.getApplicationContext(), R.layout.post_rcv_item, posts);
        blogLV.setAdapter(adapter);
        blogLV.setOnItemClickListener(itemClick);
    }

    // Method to display current users profile information
    private void displayInfo(){
        if (user.getDisplayName() != null) {
            String welcomeString = "Welcome, "+ user.getDisplayName();
            welcomeLabel.setText(welcomeString);
        }
        else{
            String welcomeString = "Welcome, User";
            welcomeLabel.setText(welcomeString);
        }

        storageReference = FirebaseStorage.getInstance().getReference(user.getUid());
        StorageReference imgReference = storageReference.child(user.getUid());
        final long MEGABYTE = 1024 * 1024;
        imgReference.getBytes(MEGABYTE)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        if (bytes.length > 0){
                            InputStream is = new ByteArrayInputStream(bytes);
                            Bitmap bmp = BitmapFactory.decodeStream(is);
                            iv.setImageBitmap(bmp);
                        }
                    }
                });

    }

    // Method to set up bottom nav bar
    public void setUpBottomNav(){
        navView.setSelectedItemId(R.id.navigation_home);

        navView.setOnItemSelectedListener(item -> {
            // Check if selected item is the page user is already on
            if (item.getItemId() != navView.getSelectedItemId()){

                // Check selected item id and start activity intent
                if (item.getItemId() == R.id.navigation_messages){
                    Intent i = new Intent(getApplicationContext(), ConversationListActivity.class);
                    startActivity(i);
                }
                else if (item.getItemId() == R.id.navigation_new_post){
                    Intent i = new Intent(getApplicationContext(), NewPostActivity.class);
                    startActivity(i);
                }
                else if (item.getItemId() == R.id.navigation_friends){
                    Intent i = new Intent(getApplicationContext(), FriendsActivity.class);
                    startActivity(i);
                }
                else if (item.getItemId() == R.id.navigation_profile){
                    Intent i = new Intent(getApplicationContext(), UserProfileActivity.class);
                    startActivity(i);
                }
                overridePendingTransition(0,0);
                return true;
            }
            return false;
        });

    }

    // Set up Blog Post ListView (Display All Blog Post)
    private void savePosts(){
        // Get Post List collection
        
        db = FirebaseFirestore.getInstance();
        db.collection("blogPosts")
                .addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.w(TAG, "Listen failed.", error);
                return;
            }

            if (value != null) {
                for (QueryDocumentSnapshot doc : value) {

                    Log.i(TAG, "onEvent: " + doc);

                    if (!doc.getId().equals("sample")) {

                        Post newUserPost;
                        String date = (String) doc.get("date");
                        String datePosted = (String) doc.get("datePosted");
                        String location = (String) doc.get("location");
                        String post = (String) doc.get("post");

                        String uriString = (String) doc.get("profileImgUri");
                        Uri profileImgUri = null;
                        if (uriString != null){
                            profileImgUri = Uri.parse(uriString);
                        }

                        String title = (String) doc.get("title");
                        Log.i(TAG, "onEvent: " + title);
                        String uid = (String) doc.get("uid");
                        String username = (String) doc.get("username");

                        String uriString1 = (String) doc.get("uri1");
                        String uriString2 = (String) doc.get("uri2");
                        String uriString3 = (String) doc.get("uri3");
                        String uriString4 = (String) doc.get("uri3");

                        // Check for empty URI's and set non-null fields
                        if(uriString1 == null && uriString2 == null && uriString3 == null && uriString4 == null){
                            newUserPost = new Post(uid, title, post, date, location, username, datePosted, profileImgUri, null, null, null, null);
                        }
                        else if (uriString1 != null && uriString2 == null && uriString3 == null && uriString4 == null){
                            Uri uri1 = Uri.parse(uriString1);
                            newUserPost = new Post(uid, title, post, date, location, username, datePosted, profileImgUri, uri1, null, null, null);
                        }
                        else if(uriString1 != null && uriString2 != null && uriString3 == null && uriString4 == null){
                            Uri uri1 = Uri.parse(uriString1);
                            Uri uri2 = Uri.parse(uriString2);
                            newUserPost = new Post(uid, title, post, date, location, username, datePosted, profileImgUri, uri1, uri2, null, null);
                        }
                        else if( uriString1 != null && uriString2 != null && uriString3 != null && uriString4 == null){
                            Uri uri1 = Uri.parse(uriString1);
                            Uri uri2 = Uri.parse(uriString2);
                            Uri uri3 = Uri.parse(uriString3);
                            newUserPost = new Post(uid, title, post, date, location, username, datePosted, profileImgUri, uri1, uri2, uri3, null);
                        }
                        else{
                            Uri uri1 = Uri.parse(uriString1);
                            Uri uri2 = Uri.parse(uriString2);
                            Uri uri3 = Uri.parse(uriString3);
                            Uri uri4 = Uri.parse(uriString4);
                            newUserPost = new Post(uid, title, post, date, location, username, datePosted, profileImgUri, uri1, uri2, uri3, uri4);
                        }

                        // Add post ID
                        newUserPost.setPostId(doc.getId());

                        // Add Post object to ArrList
                        posts.add(newUserPost);
                    }
                }
            }

            Log.i(TAG, "onEvent: " + posts.size());
            displayPosts();

        });
    }
}