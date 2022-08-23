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
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.fullsail.android.safetravels.objects.Post;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NewPostActivity extends AppCompatActivity {

    private static final String TAG = "NewPostActivity";
    EditText post_Title_ETV;
    EditText post_ETV;
    EditText location_ETV;
    EditText date_ETV;
    ImageView post_Img_1, post_Img_2, post_Img_3, post_Img_4;
    Uri uri1, uri2, uri3, uri4;
    Button post_Bttn;
    Button cancel_Post_Bttn;
    StorageReference storageReference;

    Bitmap imageBitmap = null;
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_IMAGE_GALLERY = 2;
    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        post_Title_ETV = findViewById(R.id.post_Title_ETV);
        post_ETV = findViewById(R.id.new_Post_ETV);
        location_ETV = findViewById(R.id.location_Post_ETV);
        date_ETV = findViewById(R.id.date_Post_ETV);

        post_Img_1 = findViewById(R.id.post_Img_1);
        post_Img_1.setOnClickListener(imgClick);

        post_Img_2 = findViewById(R.id.post_Img_2);
        post_Img_2.setOnClickListener(imgClick);

        post_Img_3 = findViewById(R.id.post_Img_3);
        post_Img_3.setOnClickListener(imgClick);

        post_Img_4  = findViewById(R.id.post_Img_4);
        post_Img_4.setOnClickListener(imgClick);

        post_Bttn = findViewById(R.id.post_Bttn);
        post_Bttn.setOnClickListener(postClick);

        cancel_Post_Bttn = findViewById(R.id.cancel_Post_Bttn);
        cancel_Post_Bttn.setOnClickListener(cancelClick);

        storageReference = FirebaseStorage.getInstance().getReference(currentUser.getUid());
    }

    // Activity Contracts
    // Contract to Request Permission if not granted
    public ActivityResultLauncher<String> requestPerms = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> Log.i(TAG, "onActivityResult: " + result));

    // onActivityResult method to handle img results
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

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
                    imageBitmap = (Bitmap) MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        Uri imgUri = getImgUri(this, imageBitmap, currentUser.getUid());
        saveAndDisplayImg(imgUri);

    }

    // saveAndDisplayImg Method
    // Method to determine next empty imageview and place the image.
    private void saveAndDisplayImg(Uri imgUri) {
        if (uri1 == null){
            uri1 = imgUri;
            Picasso.get().load(uri1).into(post_Img_1);
        }
        else if(uri2 == null){
            uri2 = imgUri;
            Picasso.get().load(uri2).into(post_Img_2);
        }
        else if(uri3 == null){
            uri3 = imgUri;
            Picasso.get().load(uri3).into(post_Img_3);
        }
        else if(uri4 == null){
            uri4 = imgUri;
            Picasso.get().load(uri4).into(post_Img_4);
        }
    }

    // Set up cancel click OnClickListener
    View.OnClickListener cancelClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            backToHome();
        }
    };

    // Set up post click OnClickListener
    View.OnClickListener postClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            StorageReference reference = storageReference.child("postImages");


            // Check for null or empty fields
            if (!checkForEmptyFields()){
                Post newUserPost = createPost();

                if (newUserPost != null){
                    if (newUserPost.getUri1() != null){
                        StorageReference ref = reference.child(newUserPost.getTitle() + "1");
                        ref.putFile(newUserPost.getUri1());
                    }
                    if (newUserPost.getUri2() != null){
                        StorageReference ref = reference.child(newUserPost.getTitle() + "2");
                        ref.putFile(newUserPost.getUri2());
                    }
                    if (newUserPost.getUri3() != null){
                        StorageReference ref = reference.child(newUserPost.getTitle() + "3");
                        ref.putFile(newUserPost.getUri3());
                    }
                    if (newUserPost.getUri4() != null){
                        StorageReference ref = reference.child(newUserPost.getTitle() + "4");
                        ref.putFile(newUserPost.getUri4());
                    }

                    // Add new post to blogPosts collection
                    db.collection("blogPosts")
                            .document()
                            .set(newUserPost)
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

                    // Add new post to user's blogPosts collection
                    db.collection("users")
                            .document(currentUser.getUid())
                            .collection("posts")
                            .document()
                            .set(newUserPost)
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

                    backToHome();
                }

            }
            else{
                Toast.makeText(NewPostActivity.this, "All fields must be filled out", Toast.LENGTH_SHORT).show();
            }
        }
    };

    // Method to Create new Post object
    private Post createPost() {

        String uid = currentUser.getUid();
        String title = post_Title_ETV.getText().toString();
        String post = post_ETV.getText().toString();
        String date = date_ETV.getText().toString();
        String location = location_ETV.getText().toString();
        String username = currentUser.getDisplayName();
        Uri imgUri = null;

        // Create timestamp for the date user posted the entry
        Date dateTS = new Date();
        Timestamp tS = new Timestamp(dateTS.getTime());
        String datePosted = tS.toString();
        Post newUserPost;

        if (validateDate(date)){

            // TODO: Add images to post
            if (currentUser.getPhotoUrl() != null){
                imgUri = currentUser.getPhotoUrl();
            }

            if (uri1 == null){
                newUserPost = new Post(uid, title, post, date, location, username, datePosted, imgUri, null, null, null, null);
            }
            else if(uri2 == null){
                newUserPost = new Post(uid, title, post, date, location, username, datePosted, imgUri, uri1, null, null, null);
            }
            else if(uri3 == null){
                newUserPost = new Post(uid, title, post, date, location, username, datePosted, imgUri, uri1, uri2, null, null);
            }
            else if(uri4 == null){
                newUserPost = new Post(uid, title, post, date, location, username, datePosted, imgUri, uri1, uri2, uri3, null);
            }
            else {
                newUserPost = new Post(uid, title, post, date, location, username, datePosted, imgUri, uri1, uri2, uri3, uri4);
            }

            return newUserPost;
        }
        else {
            Toast.makeText(this, "Please enter a valid date ie. 01/01/2022", Toast.LENGTH_SHORT).show();
            return null;
        }

    }

    // OnClickListener for when the user clicks the Img View button.
    View.OnClickListener imgClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Request Permission
            if (ActivityCompat.checkSelfPermission(NewPostActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                requestPerms.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }

            // Set up camera code
            final CharSequence[] items = {"Take A Photo", "Choose from Gallery", "Cancel"};

            AlertDialog.Builder builder = new AlertDialog.Builder(NewPostActivity.this);
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
        }
    };

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

    // Method to retrieve the image uri string and return URI
    public Uri getImgUri(Context c, Bitmap bitmapImg, String uuid){
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmapImg.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(c.getContentResolver(), bitmapImg, uuid, null);
        return Uri.parse(path);
    }

    // Method to validate user entries for blanks and whitespace
    private boolean checkForEmptyFields(){
        // Check if any fiends are empty or only contain whitespace
        if (post_Title_ETV.getText().toString().isEmpty() || post_Title_ETV.getText().toString().trim().isEmpty()){
            return true;
        }
        else if (post_ETV.getText().toString().isEmpty() || post_ETV.getText().toString().trim().isEmpty()){
            return true;
        }
        else if (location_ETV.getText().toString().isEmpty() || location_ETV.getText().toString().trim().isEmpty()){
            return true;
        }
        else return date_ETV.getText().toString().isEmpty() || date_ETV.getText().toString().trim().isEmpty();
    }

    // Method to validate the Date entry submitted by the user
    private boolean validateDate(String d){

        if (d.matches("([0-9]{2})/([0-9]{2})/([0-9]{4})")){
            return true;
        }
        else {
            return false;
        }

    }

    // Intent method to Send the user back to home screen
    private void backToHome(){
        startActivity(new Intent(NewPostActivity.this, HomeActivity.class));
    }

}