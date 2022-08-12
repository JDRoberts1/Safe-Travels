package com.fullsail.android.safetravels;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.fullsail.android.safetravels.adapters.HomePostListAdapter;
import com.fullsail.android.safetravels.adapters.PostListAdapter;
import com.fullsail.android.safetravels.objects.Post;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    BottomNavigationView navView;
    TextView welcomeLabel;
    CircleImageView iv;
    ListView blogLV;
    HomePostListAdapter adapter;
    FirebaseFirestore db;
    CollectionReference cR;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser cUser = mAuth.getCurrentUser();
    CircleImageView otherUserProfile;

    ArrayList<Post> posts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        welcomeLabel = findViewById(R.id.welcomeLabel);
        iv = findViewById(R.id.profile_img_main);
        navView = findViewById(R.id.nav_view);

        savePosts();
        displayInfo();
        setUpBottomNav();
    }

    // OnItemSelectedListener for when a user taps on a post
    AdapterView.OnItemSelectedListener itemClick = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            Post selectedPost = posts.get(position);
            Intent i = new Intent(HomeActivity.this, ViewPostActivity.class);
            i.putExtra(TAG, selectedPost);
            startActivity(i);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private void displayPosts() {
        blogLV = findViewById(R.id.recent_Posts_LV);
        blogLV.setOnItemSelectedListener(itemClick);
        adapter = new HomePostListAdapter(this.getApplicationContext(), R.layout.post_rcv_item, posts);
        blogLV.setAdapter(adapter);
        Log.i(TAG, "displayPosts: " + posts.size());
    }

    // Method to display current users profile information
    private void displayInfo(){
        if (cUser.getDisplayName() != null) {
            String welcomeString = "Welcome, "+ cUser.getDisplayName();
            welcomeLabel.setText(welcomeString);
        }
        else{
            String welcomeString = "Welcome, User";
            welcomeLabel.setText(welcomeString);
        }

        if (cUser.getPhotoUrl() != null) {
            iv.setImageURI(cUser.getPhotoUrl());
        }
        else{
            iv.setImageResource(R.drawable.default_img);
        }
    }

    // Method to set up bottom nav bar
    public void setUpBottomNav(){
        navView.setSelectedItemId(R.id.navigation_home);

        navView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
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
            }
        });

    }

    // TODO: Set up Blog Post Recycle View (Display All Blog Post)
    private void savePosts(){
        // Get User List collection
        db = FirebaseFirestore.getInstance();
        cR = db.collection("blogPosts");

        cR.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error);
                    return;
                }

                for (QueryDocumentSnapshot doc : value) {

                    if (!doc.getId().equals("sample")) {

                        Post newUserPost;
                        String date = (String) doc.get("date");;
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

                        Uri uri1 = null;
                        Uri uri2 = null;
                        Uri uri3 = null;
                        Uri uri4 = null;

//                        if (uri1 != null){
//                            newUserPost = new Post(uid, title, post, date, location, username, datePosted, profileImgUri, null, null, null, null);
//                        }
//                        else if(uri2 == null){
//                            newUserPost = new Post(uid, title, post, date, location, username, datePosted, profileImgUri, uri1, null, null, null);
//                        }
//                        else if(uri3 == null){
//                            newUserPost = new Post(uid, title, post, date, location, username, datePosted, profileImgUri, uri1, uri2, null, null);
//                        }
//                        else if(uri4 == null){
//                            newUserPost = new Post(uid, title, post, date, location, username, datePosted, profileImgUri, uri1, uri2, uri3, null);
//                        }
//                        else {
//                            newUserPost = new Post(uid, title, post, date, location, username, datePosted, profileImgUri, uri1, uri2, uri3, uri4);
//                        }

                        newUserPost = new Post(uid, title, post, date, location, username, datePosted, profileImgUri, null, null, null, null);
                        posts.add(newUserPost);
                    }
                }

                Log.i(TAG, "onEvent: " + posts.size());
                displayPosts();

            }
        });
    }

}