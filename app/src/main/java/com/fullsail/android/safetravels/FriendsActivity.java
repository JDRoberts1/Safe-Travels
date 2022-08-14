package com.fullsail.android.safetravels;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class FriendsActivity extends AppCompatActivity {

    ImageButton addBttn;
    BottomNavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        addBttn = findViewById(R.id.add_Friends_Button);
        addBttn.setOnClickListener(addClick);
        setUpBottomNav();
    }

    View.OnClickListener addClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(FriendsActivity.this, UserFriendSearchActivity.class));
        }
    };

    public void setUpBottomNav(){
        navView = findViewById(R.id.navView);
        navView.setSelectedItemId(R.id.navigation_friends);

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
}