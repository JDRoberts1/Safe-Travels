package com.fullsail.android.safetravels;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.fullsail.android.safetravels.objects.Post;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewPostActivity extends AppCompatActivity {

    CircleImageView profileImgView;
    TextView titleTV;
    TextView postTV;
    ImageView imageView1;
    ImageView imageView2;
    ImageView imageView3;
    ImageView imageView4;
    TextView dateTV;
    TextView locationTV;
    BottomNavigationView navView;

    Post p = null;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser cUser = mAuth.getCurrentUser();
    public static final String TAG = "ViewPostActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_post);

        Intent i = getIntent();
        p = i.getParcelableExtra(HomeActivity.TAG);


        profileImgView = findViewById(R.id.poster_ImgView);
        titleTV = findViewById(R.id.title_Post_TV);
        postTV = findViewById(R.id.post_View_TV);
        imageView1 = findViewById(R.id.imageView1);
        imageView2 = findViewById(R.id.imageView2);
        imageView3 = findViewById(R.id.imageView3);
        imageView4 = findViewById(R.id.imageView4);
        dateTV = findViewById(R.id.date_Post_TV);
        locationTV = findViewById(R.id.location_Post_TV);
        navView = findViewById(R.id.navView);

        titleTV.setText(p.getTitle());
        postTV.setText(p.getPost());
        dateTV.setText(p.getDatePosted());
        locationTV.setText(p.getLocation());

        setUpImages();
        setUpBottomNav();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if (p.getUid().equals(cUser.getUid())){
            getMenuInflater().inflate(R.menu.current_user_post_menu, menu);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent editIntent = new Intent(this, EditPostActivity.class);
        editIntent.putExtra(TAG, p);
        startActivity(editIntent);
        return super.onOptionsItemSelected(item);
    }

    private void setUpImages() {
        if (p.getProfileImgUri() != null){
            profileImgView.setImageURI(p.getProfileImgUri());
        }

        if (p.getUri1() != null){
            imageView1.setImageURI(p.getUri1());
        }
        else {
            imageView1.setVisibility(View.GONE);
        }

        if (p.getUri2() != null){
            imageView2.setImageURI(p.getUri2());
        }
        else{
            imageView2.setVisibility(View.GONE);
        }

        if (p.getUri3() != null){

            imageView3.setImageURI(p.getUri3());
        }
        else{
            imageView3.setVisibility(View.GONE);
        }

        if (p.getUri4() != null){
            imageView4.setImageURI(p.getUri4());
        }
        else {
            imageView4.setVisibility(View.GONE);
        }
    }

    // Method to set up bottom nav bar
    public void setUpBottomNav(){
        navView.setSelectedItemId(R.id.navigation_home);

        navView.setOnItemSelectedListener(item -> {
            // Check if selected item is the page user is already on
            if (item.getItemId() != navView.getSelectedItemId()){

                // Check selected item id and start activity intent
                if (item.getItemId() == R.id.navigation_messages){
                    Intent i = new Intent(getApplicationContext(), ConversationListActivity.class);
                    startActivity(i);
                }
                else if (item.getItemId() == R.id.navigation_new_post){
                    Intent i = new Intent(getApplicationContext(), NewPostActivity.class);
                    startActivity(i);
                }
                else if (item.getItemId() == R.id.navigation_friends){
                    Intent i = new Intent(getApplicationContext(), FriendsActivity.class);
                    startActivity(i);
                }
                else if (item.getItemId() == R.id.navigation_profile){
                    Intent i = new Intent(getApplicationContext(), UserProfileActivity.class);
                    startActivity(i);
                }
                overridePendingTransition(0,0);
                return true;
            }
            return false;
        });

    }
}