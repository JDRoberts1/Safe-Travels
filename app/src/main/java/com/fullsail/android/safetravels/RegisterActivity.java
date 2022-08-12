package com.fullsail.android.safetravels;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    public static final String TAG = "RegisterActivity";

    EditText uName;
    EditText emailETV;
    EditText passwordETV;
    Button signUpBttn;
    TextView signInTV;
    TextView errorLabel;
    Button uploadTV;
    ImageView profileImage;

    Bitmap imageBitmap = null;
    boolean imgUploaded = false;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_IMAGE_GALLERY = 2;
    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.Theme_SafeTravels_Fullscreen);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        errorLabel = findViewById(R.id.error_label_regis);

        uName = findViewById(R.id.username_etv);
        emailETV = findViewById(R.id.email_register_etv);
        passwordETV = findViewById(R.id.password_register_etv);

        signUpBttn = findViewById(R.id.sign_up_bttn);
        signUpBttn.setOnClickListener(registerClick);

        signInTV = findViewById(R.id.signIn_TV);
        signInTV.setClickable(true);
        signInTV.setOnClickListener(signInClick);

        uploadTV = findViewById(R.id.upload_bttn);
        uploadTV.setOnClickListener(uploadClick);

        profileImage = findViewById(R.id.profile_picture_iv_etv);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        profileImage.setVisibility(View.VISIBLE);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
        }
        else if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == RESULT_OK){

            if (data != null) {
                try {
                    imageBitmap = (Bitmap) MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        profileImage.setImageBitmap(imageBitmap);
        imgUploaded = true;

    }

    // Activity Contracts

    // Contract to Request Permission if not granted
    public ActivityResultLauncher<String> requestPerms = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> Log.i(TAG, "onActivityResult: " + result));

    // Intent to take user to the Log In activity
    private void logInScreenIntent() {
        // Intent to Main screen
        Intent logInScreenIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(logInScreenIntent);
    }

    // Intent for Taking a Photo with Camera
    private void dispatchTakePictureIntent(Intent i) {
        try {
            startActivityForResult(i, REQUEST_IMAGE_CAPTURE);
        } catch (ActivityNotFoundException e) {
            // display error state to the user
        }
    }

    // Intent for choosing image from gallery
    private void dispatchGetPictureIntent(Intent i) {
        try {
            startActivityForResult(i, REQUEST_IMAGE_GALLERY);
        } catch (ActivityNotFoundException e) {
            // display error state to the user
        }
    }

    // Method used to retrieve the users profile image uri
    public Uri getImgUri(Context c, Bitmap bitmapImg, String uuid){
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmapImg.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(c.getContentResolver(), bitmapImg, uuid, null);
        return Uri.parse(path);
    }

    // Method to validate user entries for blanks and whitespace
    private boolean checkForEmptyFields(){
        // Check if any fiends are empty or only contain whitespace
        if (uName.getText().toString().isEmpty() || uName.getText().toString().trim().isEmpty()){
            return false;
        }
        else if (emailETV.getText().toString().isEmpty() || emailETV.getText().toString().trim().isEmpty()){
            return false;
        }
        else return !passwordETV.getText().toString().isEmpty() && !passwordETV.getText().toString().trim().isEmpty();
    }

    // Method to update UI and take the user to the log in screen
    private void updateUI(FirebaseUser user) {

        String userName = uName.getText().toString();
        String password = passwordETV.getText().toString();

        Map<String, Object> newUser = new HashMap<>();
        newUser.put("username", userName);
        newUser.put("userId", user.getUid());
        newUser.put("email", user.getEmail());
        newUser.put("password", password);

        UserProfileChangeRequest profileUpdates;
        Uri imgUri = null;
        if (imgUploaded){
            imgUri = getImgUri(this, imageBitmap, user.getUid());

            profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(userName)
                    .setPhotoUri(imgUri)
                    .build();

            newUser.put("profileImg", imgUri);

        } else{

            profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(userName)
                    .build();
        }

        // Update profile call
        user.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                    {
                        Log.d(TAG, "User profile updated.");
                    }
                });



        // Create a new user document with a user ID
        // users Collection is used to start a storage location for all users posts, friends and messages.
        db.collection("users")
                .document(user.getUid())
                .set(newUser)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });

        // Create a new user document with a user ID
        // userList Collection is used to keep track of all users for user search
        Map<String, Object> userListIem = new HashMap<>();
        userListIem.put("username", userName);
        userListIem.put("userId", user.getUid());
        userListIem.put("profileImg", imgUri);

        // Add a new user to userList with a user ID
        db.collection("userList")
                .document(user.getUid())
                .set(userListIem)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document: ", e);
                    }
                });

    }

    // OnClickListener for Sign-In textview
    View.OnClickListener signInClick = v -> {
        // Create Intent to take user to sign in screen
        logInScreenIntent();
    };

    // OnClickListener for Upload Image button
    View.OnClickListener uploadClick = v -> {

        // Request Permission
        if (ActivityCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            requestPerms.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        // Set up camera code
        final CharSequence[] items = {"Take A Photo", "Choose from Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
        builder.setTitle(R.string.prompt_photo);
        builder.setItems(items, (dialog, which) -> {
            if (items[which].equals("Take A Photo")){

                dispatchTakePictureIntent(cameraIntent);
            }
            else if (items[which].equals("Choose from Gallery")){
                galleryIntent.setType("image/*");
                dispatchGetPictureIntent(galleryIntent);
            }
        });

        builder.show();
    };

    // OnClickListener for Register button
    View.OnClickListener registerClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            if (checkForEmptyFields()){
                final String email = emailETV.getText().toString();
                final String password = passwordETV.getText().toString();

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(RegisterActivity.this, task -> {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "createUserWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();

                                if (user != null) {
                                    updateUI(user);
                                }

                                logInScreenIntent();

                            }
                            else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            }
                        })
                        .addOnFailureListener(e -> errorLabel.setText(e.getLocalizedMessage()));
            }
            else{
                errorLabel.setText(R.string.warning_empty_field);
            }
        }
    };
}
