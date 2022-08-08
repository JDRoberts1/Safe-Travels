package com.fullsail.android.safetravels;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LaunchActivity extends AppCompatActivity {

    final Handler handler = new Handler();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser cUser = mAuth.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_SafeTravels_Fullscreen);
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // TODO: check if a user is currently logged in
                //toLogInScreen();

                // check if a user is currently logged in
                if (cUser != null){
                    toMainScreen();
                }
                else{
                    toLogInScreen();
                }
            }
        }, 1000);
    }

    // MARK: toMainScreen
    // method to take the user to the main screen if already logged in
    private void toMainScreen(){
        Intent mainScreenIntent = new Intent(this, HomeActivity.class);
        startActivity(mainScreenIntent);
    }

    // MARK: toLogInScreen
    // method to take the user to the log in screen if not already logged in
    private void toLogInScreen(){
        Intent logInScreenIntent = new Intent(this, LoginActivity.class);
        startActivity(logInScreenIntent);
    }
}
