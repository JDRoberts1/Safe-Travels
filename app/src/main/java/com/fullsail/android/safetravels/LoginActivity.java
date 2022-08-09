package com.fullsail.android.safetravels;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    public static final String TAG = "LoginActivity";
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    Button signInBttn;
    TextView forgotPasswordTV;
    TextView createAccountTV;

    TextView errorLabel;
    EditText emailETV;
    EditText passwordETV;


    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_SafeTravels_Fullscreen);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        signInBttn = findViewById(R.id.sign_in_bttn);
        signInBttn.setOnClickListener(signInClick);
        errorLabel = findViewById(R.id.sign_in_error_label);
        forgotPasswordTV = findViewById(R.id.forgot_password_bttn);
        forgotPasswordTV.setClickable(true);
        forgotPasswordTV.setOnClickListener(forgotPWClick);
        createAccountTV = findViewById(R.id.register_bttn);
        createAccountTV.setClickable(true);
        createAccountTV.setOnClickListener(createAcctClick);
        emailETV = findViewById(R.id.email_login_etv);
        passwordETV = findViewById(R.id.password_login_etv);
    }

    // OnClickListener for Sign-In textview
    View.OnClickListener signInClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            String email = emailETV.getText().toString();
            String password = passwordETV.getText().toString();

            if (nullCheck(email, password)){
                logInUser(email, password);
            }
            else {
                errorLabel.setText(R.string.warning_empty_field);
            }
        }
    };

    // OnClickListener for Forgot Password button
    View.OnClickListener forgotPWClick = v -> forgotPWIntent();

    // OnClickListener for Create Account button
    View.OnClickListener createAcctClick = v -> registerIntent();

    // Intent method to take user to Reset Password activity
    private void forgotPWIntent() {
        Intent passwordIntent = new Intent(this, ResetPasswordActivity.class);
        startActivity(passwordIntent);
    }

    // Intent method to take user to Register activity
    private void registerIntent() {
        Intent logInScreenIntent = new Intent(this, RegisterActivity.class);
        startActivity(logInScreenIntent);
    }

    // Method to validate user entries for blanks and whitespace
    private boolean nullCheck(String email, String pw){

        if (email.isEmpty() || email.trim().isEmpty()){
            return false;
        }
        else return !pw.isEmpty() && !pw.trim().isEmpty();
    }

    // Method to log user in
    private void logInUser(String email, String password){

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        errorLabel.setText(task.getException().getLocalizedMessage());
                    }
                });

    }

    // Method to update the UI and take the user to the main screen
    private void updateUI(FirebaseUser user) {
        // TODO: Create Intent to take user to the main screen
        if (user != null){
            Intent mainScreenIntent = new Intent(this, HomeActivity.class);
            mainScreenIntent.putExtra(TAG, user);
            startActivity(mainScreenIntent);
        }
    }

}

