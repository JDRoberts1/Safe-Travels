package com.fullsail.android.safetravels;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;

import com.fullsail.android.safetravels.adapters.PostListAdapter;
import com.fullsail.android.safetravels.objects.Post;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
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
    FirebaseUser user = mAuth.getCurrentUser();
    ArrayList<Post> posts = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        usernameLabel = findViewById(R.id.username_Profile_Label);
        iv = findViewById(R.id.profile_img_main);
        navView = findViewById(R.id.navView);
        editBttn = findViewById(R.id.edit_Profile_Bttn);
        editBttn.setClickable(true);
        editBttn.setOnClickListener(editClick);

        savePosts();
        displayInfo();
        setUpBottomNav();
    }

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

            // Set up dialog options
            final CharSequence[] items = {"Confirm", "Cancel"};

            AlertDialog.Builder builder = new AlertDialog.Builder(UserProfileActivity.this);
            builder.setTitle(R.string.prompt_log_out_confirmation);
            builder.setItems(items, (dialog, which) -> {
                if (items[which].equals("Confirm")){
                    FirebaseAuth.getInstance().signOut();
                    logOutIntent();
                }
            });

            builder.show();

        }
        else if (item.getItemId() == R.id.delete_acct_Bttn){

            String id = user.getUid();

            AlertDialog.Builder builder = new AlertDialog.Builder(UserProfileActivity.this);
            builder.setTitle(R.string.prompt_delete_account);
            builder.setIcon(R.drawable.ic_baseline_delete_24);
            builder.setMessage(R.string.prompt_delete_account_confirmation);
            builder.setPositiveButton(R.string.action_remove_account, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    user.delete()
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
            });

            builder.setNegativeButton(R.string.action_cancel, null);

            builder.show();


        }
        return super.onOptionsItemSelected(item);
    }

    View.OnClickListener editClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Intent to edit profile activity
            startActivity(new Intent(UserProfileActivity.this, EditProfileActivity.class));
        }
    };

    AdapterView.OnItemClickListener itemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Post selectedPost = posts.get(position);
            Intent viewIntent = new Intent(UserProfileActivity.this, ViewPostActivity.class);
            viewIntent.putExtra(HomeActivity.TAG, selectedPost);
            startActivity(viewIntent);
        }
    };

    private void removeUserInfo(String id) {
        dR = db.collection("users").document(id);
        dR.delete();

        cR = db.collection("blogPosts");
        cR.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error);
                    return;
                }

                for (QueryDocumentSnapshot doc : value){

                    String uid = (String) doc.get("uid");
                    String docId = doc.getId();

                    if (uid != null && uid.equals(user.getUid())) {
                        dR = cR.document(docId);
                        dR.delete();
                    }
                }
            }
        });
    }

    // Intent to send the user back to Log In Screen
    private void logOutIntent(){
        startActivity(new Intent(UserProfileActivity.this, LoginActivity.class));
    }

    // Method to display current users profile information
    private void displayInfo(){
        if (user.getDisplayName() != null) {
            String profileString = user.getDisplayName() + " Profile";
            usernameLabel.setText(profileString);
        }
        else{
            String welcomeString = "Profile";
            usernameLabel.setText(welcomeString);
        }

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

    public void setUpBottomNav(){
        navView = findViewById(R.id.navView);
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

                    String uId = doc.getString("uid");

                    if (uId != null && uId.equals(user.getUid())) {

                        Post newUserPost;
                        String date = (String) doc.get("date");
                        Log.i(TAG, "onEvent: " + date);

                        String datePosted = (String) doc.get("datePosted");
                        String location = (String) doc.get("location");
                        String post = (String) doc.get("post");
                        String postId = doc.getId();

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
                        newUserPost.setPostId(postId);

                        // Add Post object to ArrList
                        posts.add(newUserPost);
                    }
                }
                displayPosts();

            }
        });
    }

    private void displayPosts() {
        userBlogLV = findViewById(R.id.user_Posts_LV);
        userBlogLV.setOnItemClickListener(itemClick);
        adapter = new PostListAdapter(this.getApplicationContext(), R.layout.post_rcv_item ,posts);
        userBlogLV.setAdapter(adapter);
        Log.i(TAG, "displayPosts: " + posts.size());
    }
}
