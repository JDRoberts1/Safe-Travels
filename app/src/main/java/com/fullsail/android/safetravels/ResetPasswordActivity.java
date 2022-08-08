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

public class ResetPasswordActivity extends AppCompatActivity {

    private static final String TAG = "ResetPasswordActivity";
    Button sendResetLinkBttn;
    EditText email;
    TextView errorLabel;
    FirebaseAuth auth = FirebaseAuth.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        sendResetLinkBttn = findViewById(R.id.email_bttn);
        sendResetLinkBttn.setOnClickListener(sendClick);

        email = findViewById(R.id.email_reset_ETV);
        errorLabel = findViewById(R.id.error_label_reset);
    }

    View.OnClickListener sendClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            String userEmail = email.getText().toString();

            if (userEmail.isEmpty() || userEmail.trim().isEmpty()){

                errorLabel.setText(R.string.warning_empty_field);
            }
            else{

                auth.sendPasswordResetEmail(userEmail)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Email sent.");
                            }
                        })
                .addOnFailureListener(e -> errorLabel.setText(e.getLocalizedMessage()));
            }

            if (auth.getCurrentUser() != null){
                auth.signOut();
            }

            Intent logInIntent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
            startActivity(logInIntent);
        }
    };

}
