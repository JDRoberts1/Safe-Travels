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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";
    boolean updatedImg = false;
    Bitmap imageBitmap = null;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_IMAGE_GALLERY = 2;
    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);

    TextView errorLabel;
    EditText uName;
    EditText emailETV;
    Button passwordReset;
    Button deleteAcctBttn;
    Button updateProfileBttn;
    Button uploadBttn;
    ImageView profileImage;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        uName = findViewById(R.id.username_edit);
        emailETV = findViewById(R.id.email_register_edit);
        errorLabel = findViewById(R.id.error_label_edit);

        deleteAcctBttn = findViewById(R.id.delete_acct_bttn);
        deleteAcctBttn.setOnClickListener(deleteClick);

        updateProfileBttn = findViewById(R.id.update_bttn);
        updateProfileBttn.setOnClickListener(updateClick);

        passwordReset = findViewById(R.id.reset_pw_bttn);
        passwordReset.setOnClickListener(resetClick);

        uploadBttn = findViewById(R.id.upload_bttn_edit);
        uploadBttn.setOnClickListener(uploadClick);

        profileImage = findViewById(R.id.profile_picture_iv_edit);

        displayInfo();
    }

    // Activity Result for Camera Intent
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

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

        updatedImg = true;
        profileImage.setImageBitmap(imageBitmap);
    }

    // Activity Contracts
    // Contract to Request Permission if not granted
    public ActivityResultLauncher<String> requestPerms = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> Log.i(TAG, "onActivityResult: " + result));

    // Display current users profile information
    private void displayInfo(){

        emailETV.setText(user.getEmail());

        if (user.getDisplayName() != null) {
            uName.setText(user.getDisplayName());
        }

        if (user.getPhotoUrl() != null) {
            profileImage.setImageURI(user.getPhotoUrl());
        }
    }

    // Intent to send the user back to Log In Screen
    private void logOutIntent(){
        FirebaseAuth.getInstance().signOut();
        Intent logInIntent = new Intent(EditProfileActivity.this, LoginActivity.class);
        startActivity(logInIntent);
    }

    // Method to validate user entries for blanks and whitespace
    private boolean nullCheck(String email, String username){

        if (email.isEmpty() || email.trim().isEmpty()){
            return false;
        }
        else return !username.isEmpty() && !username.trim().isEmpty();
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

    // Method to remove users account
    private void deleteUser() {
        user.delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "User account deleted.");
                    }
                });

        logOutIntent();
    }

    // Method to update users email address
    private void updateEmail(String newEmail) {
        user.updateEmail(newEmail)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "User email address updated.");
                    }
                })
                .addOnFailureListener(e -> errorLabel.setText(e.getLocalizedMessage()));
    }

    // Method used to retrieve the users profile image uri
    public Uri getImgUri(Context c, Bitmap bitmapImg, String uuid){
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmapImg.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(c.getContentResolver(), bitmapImg, uuid, null);
        return Uri.parse(path);
    }

    // OnClickListener for Update button Press
    View.OnClickListener updateClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            String newEmail = emailETV.getText().toString();
            String newUserName = uName.getText().toString();

            // Check for empty fields
            if (nullCheck(newEmail, newUserName)){

                // If the user email has been changed update the email
                if (!user.getEmail().equals(newEmail)){

                    updateEmail(newEmail);
                    logOutIntent();
                }

                // If username is different, Update the username
                if (!user.getDisplayName().equals(newUserName)){
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(newUserName)
                            .build();

                    user.updateProfile(profileUpdates)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "User profile updated.");
                                }
                            })
                    .addOnFailureListener(e -> errorLabel.setText(e.getLocalizedMessage()));
                }

                // Check if user has updated their img
                if (updatedImg){
                    Uri imgUri = getImgUri(EditProfileActivity.this, imageBitmap, user.getUid());

                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setPhotoUri(imgUri)
                            .build();

                    user.updateProfile(profileUpdates)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "User profile updated.");
                                }
                            })
                            .addOnFailureListener(e -> errorLabel.setText(e.getLocalizedMessage()));
                }

                errorLabel.setText(R.string.prompt_profile_updated);

            }
            else {
                errorLabel.setText(R.string.warning_empty_field);
            }

        }
    };

    // OnClickListener for Reset Password button
    View.OnClickListener resetClick = v -> {
        Intent resetIntent = new Intent(EditProfileActivity.this, ResetPasswordActivity.class);
        startActivity(resetIntent);
    };

    // OnClickListener for Delete Account button
    View.OnClickListener deleteClick = v -> deleteUser();

    // OnClickListener for Image upload button
    View.OnClickListener uploadClick = v -> {

        // TODO: Request Permission
        if (ActivityCompat.checkSelfPermission(EditProfileActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            requestPerms.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        // TODO: SET UP CAMERA code
        final CharSequence[] items = {"Take A Photo", "Choose from Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
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
}
