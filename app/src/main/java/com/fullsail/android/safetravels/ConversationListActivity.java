package com.fullsail.android.safetravels;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fullsail.android.safetravels.adapters.ConversationListAdapter;
import com.fullsail.android.safetravels.adapters.PendingFriendsListAdapter;
import com.fullsail.android.safetravels.adapters.UserListAdapter;
import com.fullsail.android.safetravels.objects.Message;
import com.fullsail.android.safetravels.objects.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class ConversationListActivity extends AppCompatActivity implements ConversationListAdapter.clickListener {

    public static final String TAG = "ConversationListActivity";
    BottomNavigationView navView;
    RecyclerView messagesRCV;
    FirebaseFirestore db;
    CollectionReference cR;
    ArrayList<User> users;
    ArrayList<User> messageUsers = new ArrayList<>();
    ConversationListAdapter conversationListAdapter;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = mAuth.getCurrentUser();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages_list);
        db = FirebaseFirestore.getInstance();
        getUsers();

        messagesRCV = findViewById(R.id.messages_RCV);

        setUpBottomNav();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.conversation_list_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_new_message){
            // Take the user to the user search activity
            startActivity(new Intent(ConversationListActivity.this, UserMessageSearchActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }


    // Method to retrieve users from Firebase Collection
    public void getUsers(){
        users = new ArrayList<>();
        cR = db.collection("users");
        cR.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error);
                    return;
                }

                for (QueryDocumentSnapshot doc : value) {
                    String id = (String) doc.get("userId");
                    if (id != null && !id.equals(currentUser.getUid())) {
                        String username = (String) doc.get("username");
                        String imgUrl = (String) doc.get("img");

                        User u;
                        if (imgUrl != null){
                            u = new User(username, id, Uri.parse(imgUrl));
                        }
                        else{
                            u = new User(username, id, null);
                        }

                        users.add(u);
                    }
                }

                Log.i(TAG, "onEvent: " + users.size());
                checkForMessages();
            }
        });


    }

    private void checkForMessages(){
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        CollectionReference reference = cR.document(currentUser.getUid()).collection("messages");

        messageUsers = new ArrayList<>();
        reference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                for (DocumentSnapshot doc : value){
                    String docId = doc.getId();

                    for (User u : users){
                        if (u.getUid().equals(docId)){
                            messageUsers.add(u);
                        }
                    }


                }

                conversationListAdapter = new ConversationListAdapter(ConversationListActivity.this.getApplicationContext(), messageUsers);
                conversationListAdapter.setClickListener(ConversationListActivity.this);
                messagesRCV.setLayoutManager(new LinearLayoutManager(ConversationListActivity.this.getApplicationContext()));
                messagesRCV.setAdapter(conversationListAdapter);
            }
        });


    }

    // Method to set up bottom nav bar
    public void setUpBottomNav(){
        navView = findViewById(R.id.navView);
        navView.setSelectedItemId(R.id.navigation_messages);

        navView.setOnItemSelectedListener(item -> {
            // Check if selected item is the page user is already on
            if (item.getItemId() != navView.getSelectedItemId()){

                // Check selected item id and start activity intent
                if (item.getItemId() == R.id.navigation_home){
                    Intent i = new Intent(getApplicationContext(), HomeActivity.class);
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

    @Override
    public void onItemClick(User u) {
        Intent i = new Intent(this, ConversationActivity.class);
        i.putExtra(ConversationActivity.TAG, u);
        startActivity(i);
    }
}