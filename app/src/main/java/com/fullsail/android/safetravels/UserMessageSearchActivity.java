package com.fullsail.android.safetravels;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.fullsail.android.safetravels.adapters.UserListAdapter;
import com.fullsail.android.safetravels.objects.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class UserMessageSearchActivity extends AppCompatActivity {

    public static final String TAG = "UserMessageSearchActivity";
    SearchView searchView;
    ListView usersListView;
    FirebaseFirestore db;
    CollectionReference cR;
    ArrayList<User> users;
    UserListAdapter adpt;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser cUser = mAuth.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_search);

        // Get User List collection
        db = FirebaseFirestore.getInstance();
        cR = db.collection("users");

        searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(query);

        usersListView = findViewById(R.id.users_LV);
        usersListView.setOnItemClickListener(userCLick);

        users = new ArrayList<>();
        getUsers();
        adpt = new UserListAdapter(this.getApplicationContext(), R.layout.user_listview_item ,users);
        usersListView.setAdapter(adpt);
    }

    // Set up Item on click listener
    AdapterView.OnItemClickListener userCLick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            User selectedUser = users.get(position);
            Intent i = new Intent(UserMessageSearchActivity.this, ConversationActivity.class);
            i.putExtra(ConversationActivity.TAG, selectedUser);
            startActivity(i);
            overridePendingTransition(0,0);
        }
    };

    // Set up OnQueryTextListener
    SearchView.OnQueryTextListener query = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            if (!query.isEmpty()){

                for (User u : users){

                    if (u.getUsername().toLowerCase().contains(query.toLowerCase())){

                        adpt.getFilter().filter(query);
                    }
                    else{
                        // Search query not found in List View
                        Toast.makeText(UserMessageSearchActivity.this,
                                "Not found",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                }
            }

            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            adpt.getFilter().filter(newText);
            return false;
        }
    };

    // Method to retrieve users from Firebase Collection
    public void getUsers(){
        cR.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error);
                    return;
                }

                for (QueryDocumentSnapshot doc : value) {
                    String id = (String) doc.get("userId");
                    if (id != null && !id.equals(cUser.getUid())) {
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
                        Log.i(TAG, "Snapshot: " + users.size());
                    }
                }
                adpt.notifyDataSetChanged();

            }
        });
    }
}