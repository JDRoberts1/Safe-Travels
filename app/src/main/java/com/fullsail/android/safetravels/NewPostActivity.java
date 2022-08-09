package com.fullsail.android.safetravels;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class NewPostActivity extends AppCompatActivity {

    private static final String TAG = "NewPostActivity";
    EditText new_Post_ETV;
    EditText location_Post_ETV;
    EditText date_Post_ETV;
    ImageView post_Img_1, post_Img_2, post_Img_3, post_Img_4;
    Button post_Bttn;
    Button cancel_Post_Bttn;

    Bitmap imageBitmap = null;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_IMAGE_GALLERY = 2;
    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        new_Post_ETV = findViewById(R.id.new_Post_ETV);
        location_Post_ETV = findViewById(R.id.location_Post_ETV);
        date_Post_ETV = findViewById(R.id.date_Post_ETV);

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
    }

    // Activity Contracts
    // Contract to Request Permission if not granted
    public ActivityResultLauncher<String> requestPerms = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> Log.i(TAG, "onActivityResult: " + result));

    // onActivityResult method to handle img results
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //profileImage.setVisibility(View.VISIBLE);

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

        //profileImage.setImageBitmap(imageBitmap);

    }

    // TODO: Set up cancel click OnClickListener
    View.OnClickListener cancelClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Send the user back to home screen
            startActivity(new Intent(NewPostActivity.this, HomeActivity.class));
        }
    };

    // TODO: Set up post click OnClickListener
    View.OnClickListener postClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Check for null or empty fields

            // Post to user's post collection
        }
    };

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
        if (new_Post_ETV.getText().toString().isEmpty() || new_Post_ETV.getText().toString().trim().isEmpty()){
            return false;
        }
        else if (location_Post_ETV.getText().toString().isEmpty() || location_Post_ETV.getText().toString().trim().isEmpty()){
            return false;
        }
        else return !date_Post_ETV.getText().toString().isEmpty() && !date_Post_ETV.getText().toString().trim().isEmpty();
    }

}