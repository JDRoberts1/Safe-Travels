package com.fullsail.android.safetravels;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.fullsail.android.safetravels.adapters.HomePostListAdapter;
import com.fullsail.android.safetravels.adapters.PendingFriendsListAdapter;
import com.fullsail.android.safetravels.adapters.UserListAdapter;
import com.fullsail.android.safetravels.objects.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class FriendsActivity extends AppCompatActivity {

    private static final String TAG = "FriendsActivity";
    ImageButton addBttn;
    TextView pendingCount;
    TextView friendCount;
    BottomNavigationView navView;
    ListView pendingFriendsLV;
    ListView friendsLV;
    FirebaseFirestore db = FirebaseFirestore.getInstance();;;
    CollectionReference cR;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = mAuth.getCurrentUser();

    ArrayList<User> pendingFriends = new ArrayList<>();
    ArrayList<User> friends = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        addBttn = findViewById(R.id.add_Friends_Button);
        addBttn.setOnClickListener(addClick);

        pendingCount = findViewById(R.id.pending_TV);
        friendCount = findViewById(R.id.friends_TV);

        friendCheck();
        pendingCheck();
        setUpBottomNav();

    }

    View.OnClickListener addClick = v -> startActivity(new Intent(FriendsActivity.this, UserFriendSearchActivity.class));

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
                        String uid = (String) doc.get("userId");
                        if (uid!= null){
                            String username = (String) doc.get("username");
                            String img = (String) doc.get("img");

                            User friend = new User(username, uid, Uri.parse(img));
                            friends.add(friend);
                        }

                       if (!friends.isEmpty()){
                           String count = String.valueOf(friends.size());
                           friendCount.setText(count);
                       }
                    }
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

                for (QueryDocumentSnapshot doc : value){
                    String uid = (String) doc.get("userId");
                    if (uid!= null){
                        String username = (String) doc.get("username");
                        String img = (String) doc.get("img");

                        User friend = new User(username, uid, Uri.parse(img));
                        pendingFriends.add(friend);
                    }
                    String count = "(" + String.valueOf(pendingFriends.size()) + ")";
                    pendingCount.setText( count );
                }
            }
        });

        setUpPendingList();

    }

    // method to set up the Listview to display pending friend requests
    public void setUpPendingList(){
        pendingFriendsLV = findViewById(R.id.pending_Friends_LV);
        PendingFriendsListAdapter adapter = new PendingFriendsListAdapter(this.getApplicationContext(), R.layout.friend_listview_item, pendingFriends);
        pendingFriendsLV.setAdapter(adapter);
    }

    // method to set up the Listview to display pending friend requests
    public void setUpFriendList(){
        friendsLV = findViewById(R.id.friends_LV);
        UserListAdapter adapter = new UserListAdapter(this.getApplicationContext(), R.layout.post_rcv_item, friends);
        pendingFriendsLV.setAdapter(adapter);
    }
}