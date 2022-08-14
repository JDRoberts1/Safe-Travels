package com.fullsail.android.safetravels;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class UserValidationActivity extends AppCompatActivity {

    private static final String TAG = "UserValidationActivity";
    Handler handler;
    ScrollView pendingView;
    ScrollView approvedView;
    ScrollView rejectedView;
    boolean status;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_verification);

        Intent statusIntent = getIntent();
        status = statusIntent.getBooleanExtra(RegisterActivity.TAG, true);
        Log.i(TAG, "onCreate: " + status);

        pendingView = findViewById(R.id.pendingView);
        approvedView = findViewById(R.id.approvedView);
        rejectedView = findViewById(R.id.rejectedView);

        handler = new Handler();
        handler.postDelayed(() -> {
            // check if a user is currently logged in
            if (status){
                pendingView.setVisibility(View.GONE);
                approvedView.setVisibility(View.VISIBLE);
            }
            else{
                pendingView.setVisibility(View.GONE);
                rejectedView.setVisibility(View.VISIBLE);
            }
        }, 5000);


        handler = new Handler();
        handler.postDelayed(() -> {
            // check if a user is currently logged in
            toLogInScreen();
        }, 8000);


    }

    private void toLogInScreen() {
        startActivity(new Intent(UserValidationActivity.this, LoginActivity.class));
    }
}