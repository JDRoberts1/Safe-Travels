package com.fullsail.android.safetravels;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class EditPostActivity extends AppCompatActivity {

    private static final String TAG = "EditPostActivity";
    EditText post_Title_ETV;
    EditText post_ETV;
    EditText location_ETV;
    EditText date_ETV;
    ImageView imageView1, imageView2, imageView3, imageView4;
    Uri uri1, uri2, uri3, uri4;
    Button cancel_Post_Bttn;

    Post p = null;
    DocumentReference dR;
    StorageReference storageReference;
    final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    final FirebaseUser cUser = mAuth.getCurrentUser();
    Bitmap imageBitmap = null;
    final FirebaseFirestore db = FirebaseFirestore.getInstance();
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_IMAGE_GALLERY = 2;
    final Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    final Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);
        storageReference = FirebaseStorage.getInstance().getReference(cUser.getUid());

        Intent i = getIntent();
        p = i.getParcelableExtra(ViewPostActivity.TAG);

        post_Title_ETV = findViewById(R.id.post_Title_ETV);
        post_ETV = findViewById(R.id.new_Post_ETV);
        location_ETV = findViewById(R.id.location_Post_ETV);
        date_ETV = findViewById(R.id.date_Post_ETV);


        imageView1 = findViewById(R.id.post_Img_1);
        imageView1.setOnClickListener(imgClick);

        imageView2 = findViewById(R.id.post_Img_2);
        imageView2.setOnClickListener(imgClick);

        imageView3 = findViewById(R.id.post_Img_3);
        imageView3.setOnClickListener(imgClick);

        imageView4 = findViewById(R.id.post_Img_4);
        imageView4.setOnClickListener(imgClick);

        cancel_Post_Bttn = findViewById(R.id.cancel_Post_Bttn);
        cancel_Post_Bttn.setOnClickListener(cancelClick);

        displayInfo();
        setUpImages();

    }

    private void displayInfo(){
        post_Title_ETV.setText(p.getTitle());
        post_ETV.setText(p.getPost());
        location_ETV.setText(p.getLocation());
        date_ETV.setText(p.getDate());
    }

    // Activity Contracts
    // Contract to Request Permission if not granted
    public final ActivityResultLauncher<String> requestPerms = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> Log.i(TAG, "onActivityResult: " + result));

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

        Uri imgUri = getImgUri(this, imageBitmap, cUser.getUid());
        saveAndDisplayImg(imgUri);

    }

    final View.OnClickListener imgClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Request Permission
            if (ActivityCompat.checkSelfPermission(EditPostActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                requestPerms.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }

            // Set up camera code
            final CharSequence[] items = {"Take A Photo", "Choose from Gallery", "Cancel"};

            AlertDialog.Builder builder = new AlertDialog.Builder(EditPostActivity.this);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.edit_post_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_delete_post){

            AlertDialog.Builder builder = new AlertDialog.Builder(EditPostActivity.this);
            builder.setTitle(R.string.prompt_delete_post);
            builder.setMessage(R.string.prompt_delete_post_confirmation);
            builder.setPositiveButton(R.string.action_remove_post, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    deletePost();
                }

            });

            builder.setNegativeButton(R.string.action_cancel, null);

            builder.show();

        }
        else if (item.getItemId() == R.id.menu_save_post){
            updatePost();
        }
        return super.onOptionsItemSelected(item);
    }


    private void updatePost() {

        StorageReference reference = storageReference.child("postImages");

        // Check for null or empty fields
        if(!checkForEmptyFields()){

            String post_Title = post_Title_ETV.getText().toString();
            String post = post_ETV.getText().toString();
            String location = location_ETV.getText().toString();
            String date = date_ETV.getText().toString();

            if(validateDate(date)){
                dR = db.collection("blogPosts").document(p.getPostId());

                dR.update("date", date);
                dR.update("location",location);
                dR.update("post",post);
                dR.update("title",post_Title);
                dR.update("username", cUser.getDisplayName());

                if (uri1 != null){
                    dR.update("uri1", uri1);
                    StorageReference ref = reference.child(post_Title + "1");
                    ref.putFile(uri1);
                }

                if(uri2 != null){
                    dR.update("uri2", uri2);
                    StorageReference ref = reference.child(post_Title + "2");
                    ref.putFile(uri2);
                }

                if(uri3 != null){
                    dR.update("uri3", uri3);
                    StorageReference ref = reference.child(post_Title + "3");
                    ref.putFile(uri3);
                }

                if(uri4 != null){
                    dR.update("uri4", uri4);
                    StorageReference ref = reference.child(post_Title + "4");
                    ref.putFile(uri3);
                }

                backToProfile();
            }
            else{
                Toast.makeText(this, "Please enter a valid date ie. 01/01/2022", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(this, "All fields must be filled out", Toast.LENGTH_SHORT).show();
        }
    }

    private void deletePost() {
        dR = db.collection("blogPosts").document(p.getPostId());
        dR.delete();
        backToProfile();

    }

    final View.OnClickListener cancelClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            backToProfile();
        }
    };

    // Method to set not null images
    private void setUpImages() {

        StorageReference storageReference = FirebaseStorage.getInstance().getReference(p.getUid());
        StorageReference imgReference;

        final long MEGABYTE = 1024 * 1024;
        StorageReference pathReference = storageReference.child("postImages");
        if (p.getUri1() != null){
            imgReference = pathReference.child(p.getTitle() + "1");
            imgReference.getBytes(MEGABYTE)
                    .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            if (bytes.length > 0){
                                InputStream is = new ByteArrayInputStream(bytes);
                                Bitmap bmp = BitmapFactory.decodeStream(is);
                                imageView1.setImageBitmap(bmp);
                            }
                        }
                    });
        }
        else {
            imageView1.setVisibility(View.GONE);
        }

        if (p.getUri2() != null){
            imgReference = pathReference.child(p.getTitle() + "2");
            imgReference.getBytes(MEGABYTE)
                    .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            if (bytes.length > 0){
                                InputStream is = new ByteArrayInputStream(bytes);
                                Bitmap bmp = BitmapFactory.decodeStream(is);
                                imageView2.setImageBitmap(bmp);
                            }
                        }
                    });
        }


        if (p.getUri3() != null){
            imgReference = pathReference.child(p.getTitle() + "3");
            imgReference.getBytes(MEGABYTE)
                    .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            if (bytes.length > 0){
                                InputStream is = new ByteArrayInputStream(bytes);
                                Bitmap bmp = BitmapFactory.decodeStream(is);
                                imageView3.setImageBitmap(bmp);
                            }
                        }
                    });
        }


        if (p.getUri4() != null){
            imgReference = pathReference.child(p.getTitle() + "4");
            imgReference.getBytes(MEGABYTE)
                    .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            if (bytes.length > 0){
                                InputStream is = new ByteArrayInputStream(bytes);
                                Bitmap bmp = BitmapFactory.decodeStream(is);
                                imageView4.setImageBitmap(bmp);
                            }
                        }
                    });
        }
        
    }

    // saveAndDisplayImg Method
    // Method to determine next empty imageview and place the image.
    private void saveAndDisplayImg(Uri imgUri) {
        if (uri1 == null){
            uri1 = imgUri;
            Picasso.get().load(uri1).into(imageView1);
        }
        else if(uri2 == null){
            uri2 = imgUri;
            Picasso.get().load(uri2).into(imageView2);
        }
        else if(uri3 == null){
            uri3 = imgUri;
            Picasso.get().load(uri3).into(imageView3);
        }
        else if(uri4 == null){
            uri4 = imgUri;
            Picasso.get().load(uri4).into(imageView4);
        }
    }

    // Method to validate the Date entry submitted by the user
    private boolean validateDate(String d){

        return d.matches("([0-9]{2})/([0-9]{2})/([0-9]{4})");

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

    // Intent method to Send the user back to home screen
    private void backToProfile(){
        startActivity(new Intent(EditPostActivity.this, UserProfileActivity.class));
    }
}