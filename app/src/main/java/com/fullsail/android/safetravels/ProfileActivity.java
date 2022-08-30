package com.fullsail.android.safetravels;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.fullsail.android.safetravels.adapters.HomePostListAdapter;
import com.fullsail.android.safetravels.objects.Post;
import com.fullsail.android.safetravels.objects.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    BottomNavigationView navView;
    public static final String TAG = "ProfileActivity";
    TextView usernameLabel;
    CircleImageView iv;
    ListView userBlogRCV;
    HomePostListAdapter adapter;
    FirebaseFirestore db;
    CollectionReference cR;
    User user;
    ImageButton addBttn;
    ImageButton removeBttn;
    ImageButton messageBttn;
    final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    final FirebaseUser currentUser = mAuth.getCurrentUser();
    boolean friends = false;
    boolean pending = false;

    final ArrayList<Post> posts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Intent userIntent = getIntent();
        if (userIntent.getParcelableExtra(TAG) != null){
            user = userIntent.getParcelableExtra(TAG);
        }

        usernameLabel = findViewById(R.id.username_Profile_Label);
        iv = findViewById(R.id.profile_img_main);
        userBlogRCV = findViewById(R.id.user_Posts_LV);

        addBttn = findViewById(R.id.add_Bttn);
        addBttn.setOnClickListener(addClick);

        removeBttn = findViewById(R.id.remove_Bttn);
        removeBttn.setOnClickListener(removeClick);

        messageBttn = findViewById(R.id.message_Bttn);
        messageBttn.setOnClickListener(messageClick);

        navView = findViewById(R.id.navView);
        setUpBottomNav();
        savePosts();
        displayInfo();
    }

    // Method to display selected users profile information
    private void displayInfo(){
        pendingCheck();
        friendCheck();

        if (user != null) {
            String profileString = user.getUsername() + " Profile";
            usernameLabel.setText(profileString);
        }
        else{
            String welcomeString = "Profile";
            usernameLabel.setText(welcomeString);
        }

        if (user.getUri() != null){
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(user.getUid());
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
        else{
            iv.setImageResource(R.drawable.default_img);
        }


    }

    final View.OnClickListener addClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            // Add request to each users pending request
            Map<String, Object> pendingFriend = new HashMap<>();
            if (currentUser != null) {
                pendingFriend.put("username", currentUser.getDisplayName());
                pendingFriend.put("userId", currentUser.getUid());
                pendingFriend.put("img", currentUser.getPhotoUrl());
            }


            db.collection("users")
                    .document(user.getUid())
                    .collection("pendingFriends")
                    .document(currentUser.getUid())
                    .set(pendingFriend)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Log.i(TAG, "onSuccess: ");
                            addBttn.setVisibility(View.INVISIBLE);
                        }
                    })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.i(TAG, "onFailure: " + e.getLocalizedMessage());
                }
            });

        }
    };

    final View.OnClickListener removeClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (currentUser != null) {
                db.collection("users")
                        .document(user.getUid())
                        .collection("friends")
                        .document(currentUser.getUid())
                        .delete();
            }

            removeBttn.setVisibility(View.INVISIBLE);
            addBttn.setVisibility(View.VISIBLE);

            db.collection("users")
                    .document(currentUser.getUid())
                    .collection("friends")
                    .document(user.getUid())
                    .delete();
        }
    };

    // TODO:: Set up message action
    final View.OnClickListener messageClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

    public void setUpBottomNav(){
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

    // Set up Blog Post Recycle View (Display Current users Blog Posts)
    private void savePosts(){
        // Get User Post collection
        db = FirebaseFirestore.getInstance();
        cR = db.collection("blogPosts");

        cR.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error);
                    return;
                }

                if (value != null) {
                    for (QueryDocumentSnapshot doc : value) {

                        String uId = doc.getString("uid");

                        if (uId != null && uId.equals(user.getUid())) {

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
                            posts.add(newUserPost);
                        }
                    }
                }

                Log.i(TAG, "onEvent: " + posts.size());
                displayPosts();

            }
        });
    }

    // Display selected users posts
    private void displayPosts() {
        adapter = new HomePostListAdapter(this.getApplicationContext(),R.layout.user_listview_item ,posts);
        userBlogRCV.setAdapter(adapter);
        Log.i(TAG, "displayPosts: " + posts.size());
    }

    // Determine button view
    // Method to check if current user and selected user are friends
    private void friendCheck(){
        db.collection("users")
                .document(currentUser.getUid())
                .collection("friends")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.w(TAG, "Listen failed.", error);
                            return;
                        }

                        if (value != null) {
                            for (QueryDocumentSnapshot doc : value){
                                String uid = (String) doc.get("uid");
                                if (uid != null && uid.equals(user.getUid())) {
                                    addBttn.setVisibility(View.INVISIBLE);
                                    removeBttn.setVisibility(View.VISIBLE);
                                    Log.i(TAG, "onEvent: FOUND FRIEND");
                                }
                                else {
                                    Log.i(TAG, "onEvent: NO FOUND FRIEND");
                                }
                            }
                        }

                    }
                });

    }

    // Method to check if current user and selected user have a pending request
    private void pendingCheck(){
        db.collection("users")
                .document(user.getUid())
                .collection("pendingFriends")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.w(TAG, "Listen failed.", error);
                            return;
                        }

                        if (value != null) {
                            for (QueryDocumentSnapshot doc : value){
                                String uid = (String) doc.get("userId");
                                if (uid != null && uid.equals(currentUser.getUid())) {
                                    pending = true;
                                    Log.i(TAG, "onEvent: FOUND PENDING");
                                    addBttn.setVisibility(View.INVISIBLE);
                                }
                                else {
                                    pending = false;
                                    Log.i(TAG, "onEvent: NO FOUND PENDING");
                                }
                            }
                        }

                    }
                });

    }

}