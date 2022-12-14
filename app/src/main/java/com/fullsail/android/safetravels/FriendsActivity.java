package com.fullsail.android.safetravels;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.fullsail.android.safetravels.adapters.PendingFriendsListAdapter;
import com.fullsail.android.safetravels.adapters.UserListAdapter;
import com.fullsail.android.safetravels.objects.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
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

public class FriendsActivity extends AppCompatActivity implements PendingFriendsListAdapter.buttonListener {

    private static final String TAG = "FriendsActivity";
    ImageButton addBttn;
    TextView pendingCount;
    TextView friendCount;
    BottomNavigationView navView;
    ListView pendingFriendsLV;
    ListView friendsLV;
    final FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference cR;
    DocumentReference dR;
    final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    final FirebaseUser currentUser = mAuth.getCurrentUser();
    PendingFriendsListAdapter pendingFriendsListAdapter;
    UserListAdapter userListAdapter;

    final ArrayList<User> pendingFriends = new ArrayList<>();
    final ArrayList<User> friends = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        pendingFriendsLV = findViewById(R.id.pending_Friends_LV);


        friendsLV = findViewById(R.id.friends_LV);
        friendsLV.setOnItemClickListener(friendSelect);


        addBttn = findViewById(R.id.add_Friends_Button);
        addBttn.setOnClickListener(addClick);

        pendingCount = findViewById(R.id.pending_TV);
        friendCount = findViewById(R.id.friends_TV);

        friendCheck();
        pendingCheck();
        setUpBottomNav();


    }

    final View.OnClickListener addClick = v -> startActivity(new Intent(FriendsActivity.this, UserFriendSearchActivity.class));

    public void setUpBottomNav(){
        navView = findViewById(R.id.navView);
        navView.setSelectedItemId(R.id.navigation_friends);

        navView.setOnItemSelectedListener(item -> {
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

    private void friendCheck(){

        cR = db.collection("users")
                .document(currentUser.getUid())
                .collection("friends");

        cR.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error);
                    return;
                }

                if (value != null){
                    for (QueryDocumentSnapshot doc : value){
                        String uid = (String) doc.get("uid");
                        if (uid!= null){
                            String username = (String) doc.get("username");
                            String img = (String) doc.get("uri");

                            User friend = new User(username, uid, Uri.parse(img));
                            friends.add(friend);
                        }

                    }

                    userListAdapter = new UserListAdapter(FriendsActivity.this.getApplicationContext(), R.layout.post_rcv_item, friends);
                    friendsLV.setAdapter(userListAdapter);
                    updateFriendsCount();
                }
            }
        });


    }

    // Method to check if current user and selected user have a pending request
    private void pendingCheck(){

        cR = db.collection("users")
                .document(currentUser.getUid())
                .collection("pendingFriends");

        cR.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error);
                    return;
                }

                if (value != null) {
                    for (QueryDocumentSnapshot doc : value){
                        String uid = (String) doc.get("userId");
                        if (uid!= null){
                            String username = (String) doc.get("username");
                            String img = (String) doc.get("img");

                            User friend = new User(username, uid, Uri.parse(img));
                            pendingFriends.add(friend);
                        }
                    }
                }

                pendingFriendsListAdapter = new PendingFriendsListAdapter(FriendsActivity.this.getApplicationContext(), R.layout.friend_listview_item, pendingFriends);
                pendingFriendsListAdapter.setButtonListener(FriendsActivity.this);
                pendingFriendsLV.setAdapter(pendingFriendsListAdapter);
                updatePendingFriendsCount();
            }
        });

    }

    // method to set up the Listview to display pending friend requests
    public void updatePendingFriendsCount(){
        String count = "(" + pendingFriends.size() + ")";
        pendingCount.setText( count );
    }

    // method to set up the Listview to display pending friend requests
    public void updateFriendsCount(){

        if (!friends.isEmpty()){
            String count = String.valueOf(friends.size());
            friendCount.setText(count);
        }

    }

    public void removeUser(User friend){
        // Remove user from pending list
        pendingFriends.remove(friend);

        // Remove user from collection
        cR = db.collection("users")
                .document(currentUser.getUid())
                .collection("pendingFriends");

        dR = cR.document(friend.getUid());

        dR.delete();

        // Refresh Listview
        // Update pending count
        pendingFriendsListAdapter.notifyDataSetChanged();

    }

    final AdapterView.OnItemClickListener friendSelect = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            User f = friends.get(position);
            Intent i = new Intent(FriendsActivity.this, ProfileActivity.class);
            i.putExtra(ProfileActivity.TAG, f);
            startActivity(i);
        }
    };

    public void addToOtherUserFriend(User friend){
       User newFriend = new User(currentUser.getDisplayName(), currentUser.getUid(), currentUser.getPhotoUrl());

        // Add to other user Friends collection
        cR = db.collection("users")
                .document(friend.getUid())
                .collection("friends");

        cR.document(currentUser.getUid()).set(newFriend);
    }

    public void addToFriends(User friend){
        // Add to current user's Friends collection
        cR = db.collection("users")
                .document(currentUser.getUid())
                .collection("friends");

       cR.document(friend.getUid()).set(friend);

        // Add user to friends list
        friends.add(friend);

        // Update Listview
        // Update friend count
        userListAdapter.notifyDataSetChanged();

    }

    // onClickListener for Accept Bttn
    @Override
    public void onAcceptClick(int position) {
        User newFriend = pendingFriends.get(position);
        removeUser(newFriend);
        addToFriends(newFriend);
        addToOtherUserFriend(newFriend);
        updateFriendsCount();
        updatePendingFriendsCount();
    }

    // onClickListener for Decline Bttn
    @Override
    public void onDeclineClick(int position) {
        User declineFriend = pendingFriends.get(position);
        // Remove user from pending list
        removeUser(declineFriend);
        updateFriendsCount();
    }
}