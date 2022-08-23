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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
    EditText passwordETV;
    Button cancelBttn;
    Button updateProfileBttn;
    Button uploadBttn;
    ImageView profileImage;
    CollectionReference cR;
    StorageReference storageReference;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        storageReference = FirebaseStorage.getInstance().getReference(user.getUid());

        uName = findViewById(R.id.username_Edit_ETV);
        emailETV = findViewById(R.id.email_Edit_ETV);
        passwordETV = findViewById(R.id.password_Edit_ETV);
        errorLabel = findViewById(R.id.error_Edit_Label);

        updateProfileBttn = findViewById(R.id.update_bttn);
        updateProfileBttn.setOnClickListener(updateClick);

        cancelBttn = findViewById(R.id.cancel_Edit_Bttn);
        cancelBttn.setOnClickListener(cancelClick);

        uploadBttn = findViewById(R.id.upload_Edit_Bttn);
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

    private void backToProfile(){
        startActivity(new Intent(EditProfileActivity.this, UserProfileActivity.class));
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

    // Method to update users email address
    private void updateEmail(String newEmail) {
        user.updateEmail(newEmail)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Map<String, Object> data = new HashMap<>();
                        data.put("email", newEmail);

                        db.collection("users")
                                .document(user.getUid())
                                .set(data, SetOptions.merge() );


                        Log.d(TAG, "User email address updated.");
                        logOutIntent();
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

    // On
    View.OnClickListener cancelClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            backToProfile();
        }
    };

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
                }

                // If username is different, Update the username
                if (!user.getDisplayName().equals(newUserName)){

                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(newUserName)
                            .build();

                    user.updateProfile(profileUpdates)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Map<String, Object> data = new HashMap<>();
                                    data.put("username", newUserName);

                                    db.collection("users")
                                            .document(user.getUid())
                                            .set(data, SetOptions.merge() );

                                    // TODO: UPDATE USERNAME IN BLOG POSTS
                                    updateBlogPosts(newUserName);

                                    Log.d(TAG, "User profile updated.");
                                    backToProfile();

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

                                    Map<String, Object> data = new HashMap<>();
                                    data.put("profileImg", imgUri);

                                    db.collection("users")
                                            .document(user.getUid())
                                            .set(data, SetOptions.merge() );

                                    Log.d(TAG, "User profile updated.");

                                    StorageReference reference = storageReference.child(user.getUid());
                                    reference.putFile(imgUri);
                                }

                                backToProfile();
                            })
                            .addOnFailureListener(e -> errorLabel.setText(e.getLocalizedMessage()));
                }

                // If username is different, Update the username
                if (!passwordETV.getText().toString().isEmpty()){
                    String newPassword = passwordETV.getText().toString();
                    user.updatePassword(newPassword)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Map<String, Object> pw = new HashMap<>();
                                        pw.put("password", newPassword);

                                        db.collection("users")
                                                .document(user.getUid())
                                                .set(pw, SetOptions.merge() );


                                        Log.d(TAG, "User password updated.");
                                        logOutIntent();
                                    }
                                }
                            });
                }

                errorLabel.setText(R.string.prompt_profile_updated);

            }
            else {
                errorLabel.setText(R.string.warning_empty_field);
            }

        }
    };

    private void updateBlogPosts(String newUserName) {
        // Get User List collection
        db = FirebaseFirestore.getInstance();
        cR = db.collection("blogPosts");

        cR.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error);
                    return;
                }

                for (QueryDocumentSnapshot doc : value) {
                    String uId = doc.getString("uid");

                    if (uId != null && uId.equals(user.getUid())) {
                        DocumentReference dR = db.collection("blogPosts").document(doc.getId());
                        dR.update("username", user.getDisplayName());
                    }
                }
            }
        });

    }

    // OnClickListener for Image upload button
    View.OnClickListener uploadClick = v -> {

        // Request Permission
        if (ActivityCompat.checkSelfPermission(EditProfileActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            requestPerms.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

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
