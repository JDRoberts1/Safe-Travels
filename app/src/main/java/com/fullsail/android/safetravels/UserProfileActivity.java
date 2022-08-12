package com.fullsail.android.safetravels;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;

import com.fullsail.android.safetravels.adapters.PostListAdapter;
import com.fullsail.android.safetravels.objects.Post;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends AppCompatActivity {

    private static final String TAG = "UserProfileActivity";
    BottomNavigationView navView;
    TextView usernameLabel;
    TextView editBttn;
    CircleImageView iv;
    ListView userBlogLV;
    PostListAdapter adapter;
    FirebaseFirestore db;
    CollectionReference cR;
    DocumentReference dR;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser cUser = mAuth.getCurrentUser();
    ArrayList<Post> posts = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        usernameLabel = findViewById(R.id.username_Profile_Label);
        iv = findViewById(R.id.profile_img_main);
        navView = findViewById(R.id.nav_view);
        editBttn = findViewById(R.id.edit_Profile_Bttn);
        editBttn.setClickable(true);
        editBttn.setOnClickListener(editClick);

        savePosts();
        displayInfo();
        setUpBottomNav();
    }

    View.OnClickListener editClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Intent to edit profile activity
            startActivity(new Intent(UserProfileActivity.this, EditProfileActivity.class));
        }
    };

    // Menu Set Up
    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        if (menu instanceof MenuBuilder){
            ((MenuBuilder) menu).setOptionalIconsVisible(true);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logOut_Bttn){
            FirebaseAuth.getInstance().signOut();
            logOutIntent();
        }
        else if (item.getItemId() == R.id.delete_acct_Bttn){

            String id = cUser.getUid();

            cUser.delete()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                removeUserInfo(id);
                                logOutIntent();

                                Log.d(TAG, "User account deleted.");
                            }
                        }
                    });

        }
        return super.onOptionsItemSelected(item);
    }

    private void removeUserInfo(String id) {
        dR = db.collection("users").document(id);
        dR.delete();

        dR = db.collection("userList").document(id);
        dR.delete();
    }

    // Intent to send the user back to Log In Screen
    private void logOutIntent(){
        startActivity(new Intent(UserProfileActivity.this, LoginActivity.class));
    }

    // Method to display current users profile information
    private void displayInfo(){
        if (cUser.getDisplayName() != null) {
            String profileString = cUser.getDisplayName() + " Profile";
            usernameLabel.setText(profileString);
        }
        else{
            String welcomeString = "Profile";
            usernameLabel.setText(welcomeString);
        }

        if (cUser.getPhotoUrl() != null) {
            iv.setImageURI(cUser.getPhotoUrl());
        }
        else{
            iv.setImageResource(R.drawable.default_img);
        }
    }

    public void setUpBottomNav(){
        navView = findViewById(R.id.nav_view);
        navView.setSelectedItemId(R.id.navigation_profile);

        navView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                // Check if selected item is the page user is already on
                if (item.getItemId() != navView.getSelectedItemId()){

                    // Check selected item id and start activity intent
                    if (item.getItemId() == R.id.navigation_home){
                        Intent i = new Intent(getApplicationContext(), HomeActivity.class);
                        startActivity(i);
                    }
                    else if (item.getItemId() == R.id.navigation_messages){
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
                    overridePendingTransition(0,0);
                    return true;
                }
                return false;
            }
        });

    }

    // TODO: Set up Blog Post Recycle View (Display Current users Blog Posts)
    private void savePosts(){
        // Get User List collection
        db = FirebaseFirestore.getInstance();
        cR = db.collection("users").document(cUser.getUid()).collection("posts");

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
                displayPosts();

            }
        });
    }

    private void displayPosts() {
        userBlogLV = findViewById(R.id.user_Posts_LV);
        adapter = new PostListAdapter(this.getApplicationContext(), R.layout.post_rcv_item ,posts);
        userBlogLV.setAdapter(adapter);
        Log.i(TAG, "displayPosts: " + posts.size());
    }
}
