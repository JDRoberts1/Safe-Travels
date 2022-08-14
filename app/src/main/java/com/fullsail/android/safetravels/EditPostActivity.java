package com.fullsail.android.safetravels;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.fullsail.android.safetravels.objects.Post;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.ByteArrayOutputStream;

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

    CollectionReference cR;
    DocumentReference dR;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser cUser = mAuth.getCurrentUser();
    Bitmap imageBitmap = null;
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_IMAGE_GALLERY = 2;
    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);

        Intent i = getIntent();
        p = (Post) i.getParcelableExtra(HomeActivity.TAG);

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

        setUpImages();

    }


    View.OnClickListener imgClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.edit_post_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_delete_post){
            deletePost();
        }
        else if (item.getItemId() == R.id.menu_save_post){
            updatePost();
        }
        return super.onOptionsItemSelected(item);
    }

    private void updatePost() {
        String id = cUser.getUid();
        dR = db.collection("blogPosts").document(id).collection("posts").document(p.getPostId());

        String post_Title = post_Title_ETV.getText().toString();
        String post = post_ETV.getText().toString();
        String location = location_ETV.getText().toString();
        String date = date_ETV.getText().toString();

        dR.update("date",date);
        dR.update("location",post_Title);
        dR.update("post",post);
        dR.update("title",location);
    }

    private void deletePost() {
        String id = cUser.getUid();
        dR = db.collection("blogPosts").document(id).collection("posts").document(p.getPostId());
        dR.delete();

        cR = db.collection("users").document(cUser.getUid()).collection("posts");
        cR.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error);
                    return;
                }

                for (QueryDocumentSnapshot doc : value){

                    String ts = p.getDatePosted();
                    String title = p.getTitle();

                    String docTS = (String) doc.get("datePosted");
                    String docTitle = (String) doc.get("title");

                    if (docTS != null && docTitle != null) {
                        if (docTS.equals(ts) && docTitle.equals(title)) {
                            dR = cR.document(doc.getId());
                            dR.delete();
                        }
                    }
                }
            }
        });
    }

    View.OnClickListener cancelClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            backToProfile();
        }
    };

    // Method to set not null images
    private void setUpImages() {
        if (p.getUri1() != null){
            imageView1.setImageURI(p.getUri1());
        }

        if (p.getUri2() != null){
            imageView2.setImageURI(p.getUri2());
        }

        if (p.getUri3() != null){
            imageView3.setImageURI(p.getUri3());
        }

        if (p.getUri4() != null){
            imageView4.setImageURI(p.getUri4());
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

    // Intent method to Send the user back to home screen
    private void backToProfile(){
        startActivity(new Intent(EditPostActivity.this, UserProfileActivity.class));
    }
}