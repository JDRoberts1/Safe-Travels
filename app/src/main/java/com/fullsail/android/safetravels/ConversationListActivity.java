package com.fullsail.android.safetravels;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ConversationListActivity extends AppCompatActivity {

    BottomNavigationView navView;
    ImageButton newMessageBttn;
    RecyclerView messagesRCV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages_list);
        newMessageBttn = findViewById(R.id.new_Message_Bttn);
        newMessageBttn.setOnClickListener(newMessageClick);
        messagesRCV = findViewById(R.id.messages_RCV);
        setUpBottomNav();
    }

    // TODO: Set up OnClick for new Message
    View.OnClickListener newMessageClick = v -> {
        // Take the user to the user search activity
        startActivity(new Intent(ConversationListActivity.this, UserMessageSearchActivity.class));
    };

    // TODO: Set up RCV for users messages
    public void setUpRCV(){

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
}