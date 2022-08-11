package com.fullsail.android.safetravels;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.fullsail.android.safetravels.objects.User;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    BottomNavigationView navView;
    TextView welcomeLabel;
    CircleImageView iv;
    RecyclerView blogRCV;

    FirebaseFirestore db;
    CollectionReference cR;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser cUser = mAuth.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        welcomeLabel = findViewById(R.id.welcomeLabel);
        iv = findViewById(R.id.profile_img_main);


        displayInfo();
        displayPosts();
        setUpBottomNav();
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
        navView = findViewById(R.id.nav_view);
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
    private void displayPosts(){
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

                    String id = (String) doc.get("title");

                    if (id != null && !doc.getId().equals("sample")) {

                    }
                }

            }
        });
    }

}