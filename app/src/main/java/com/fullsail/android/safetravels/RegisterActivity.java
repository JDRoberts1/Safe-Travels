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
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
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
    Spinner spinner;
    Uri imgUri = null;
    ArrayList<String> bannedEmails = new ArrayList<>();
    ArrayList<String> usernames = new ArrayList<>();
    Bitmap imageBitmap = null;
    boolean imgUploaded = false;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    StorageReference storageReference;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_IMAGE_GALLERY = 2;
    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);

    boolean approvalStatus;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.Theme_SafeTravels_Fullscreen);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        Intent bannedIntent = getIntent();
        bannedEmails = bannedIntent.getStringArrayListExtra(LoginActivity.TAG);
        getUsernames();

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

        spinner = findViewById(R.id.spinner);

        profileImage = findViewById(R.id.profile_picture_iv_etv);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        profileImage.setVisibility(View.VISIBLE);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = null;
            if (data != null) {
                extras = data.getExtras();
            }
            if (extras != null) {
                imageBitmap = (Bitmap) extras.get("data");
            }
        }
        else if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == RESULT_OK){

            if (data != null) {
                try {
                    imageBitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        Uri imgUri = getNewImageUri(this, imageBitmap);
        Picasso.get().load(imgUri).into(profileImage);
        imgUploaded = true;
    }

    // Activity Contracts
    // Contract to Request Permission if not granted
    public ActivityResultLauncher<String> requestPerms = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> Log.i(TAG, "onActivityResult: " + result));

    // Intent to take user to the Log In activity
    private void verificationIntent() {
        // Intent to Main screen
        Intent verificationIntent = new Intent(RegisterActivity.this, UserValidationActivity.class);
        verificationIntent.putExtra(TAG, approvalStatus);
        startActivity(verificationIntent);
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

    // Method to update UI and take the user to the log in screen
    private void updateUI(FirebaseUser user) {

        storageReference = FirebaseStorage.getInstance().getReference(user.getUid());

        String userName = uName.getText().toString();
        String password = passwordETV.getText().toString();

        Map<String, Object> newUser = new HashMap<>();
        newUser.put("username", userName);
        newUser.put("userId", user.getUid());
        newUser.put("email", user.getEmail());
        newUser.put("password", password);

        UserProfileChangeRequest profileUpdates;
        if (imgUploaded){
            imgUri = getImgUri(this, imageBitmap, user.getUid());
            profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(userName)
                    .setPhotoUri(imgUri)
                    .build();

            StorageReference reference = storageReference.child(user.getUid());
            reference.putFile(imgUri);
            newUser.put("imgUri", imgUri);

        }
        else{
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
                .addOnSuccessListener(aVoid -> Log.d(TAG, "DocumentSnapshot successfully written!"))
                .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));

    }

    // OnClickListener for Sign-In textview
    View.OnClickListener signInClick = v -> {
        // Create Intent to take user to sign in screen
        verificationIntent();
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
                String sex = spinner.getSelectedItem().toString();
                String username = uName.getText().toString();

                if (bannedEmails.contains(email.toLowerCase())){
                    errorLabel.setText(R.string.warning_rejected_register);
                }
                else if (usernames.contains(username.toLowerCase())){
                    errorLabel.setText(R.string.warning_used_username);
                }
                else {
                    if (sex.equals("Female")){

                        // Update status for Intent that will be sent to validation screen
                        approvalStatus = true;

                        // Create users account
                        mAuth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(RegisterActivity.this, task -> {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d(TAG, "createUserWithEmail:success");
                                        FirebaseUser user = mAuth.getCurrentUser();

                                        if (user != null) {
                                            updateUI(user);
                                        }



                                        verificationIntent();

                                    }
                                    else {
                                        // If sign in fails, display a message to the user.
                                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                    }
                                })
                                .addOnFailureListener(e -> errorLabel.setText(e.getLocalizedMessage()));
                    }
                    else if(sex.equals("Male")){
                        // Add user to list of banned emails
                        approvalStatus = false;
                        banEmail(email);
                        verificationIntent();
                    }
                    else{
                        Log.i(TAG, "onClick: ");
                    }
                }

            }
            else{
                errorLabel.setText(R.string.warning_empty_field);
            }
        }
    };

    private void banEmail(String email) {
        Map<String, Object> newUser = new HashMap<>();
        newUser.put("email", email);


        db.collection("bannedEmails")
                .document()
                .set(newUser)
                .addOnCompleteListener(task -> {



                });
    }

    // Method to retrieve user emails from database and place them in a String ArrayList
    private void getUsernames() {
        CollectionReference cR = db.collection("user");

        cR.addSnapshotListener((value, error) -> {
            if (value != null) {
                for (QueryDocumentSnapshot doc : value){
                    String username = (String) doc.get("username");
                    if (username != null) {
                        usernames.add(username.toLowerCase());
                    }
                }
            }

            Log.i(TAG, "onEvent: " + usernames.size());
        });
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

    // Get New URI Method
    public Uri getNewImageUri(Context c, Bitmap bmp){
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(c.getContentResolver(), bmp, "uid", null);
        return Uri.parse(path);
    }

}
