package com.fullsail.android.safetravels;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.fullsail.android.safetravels.objects.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConversationActivity extends AppCompatActivity {
    private static final String TAG = "ConversationActivity";

    CircleImageView messageImg;
    TextView conversationLabel;
    RecyclerView chatRCV;
    EditText message;
    FloatingActionButton sendBttn;
    User user;

    FirebaseDatabase db;
    DatabaseReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        db = FirebaseDatabase.getInstance();
        ref = db.getReference();

        messageImg = findViewById(R.id.message_Img);
        conversationLabel = findViewById(R.id.conversation_Label);
        chatRCV = findViewById(R.id.chat_RCV);
        message= findViewById(R.id.message_ETV_Chat);
        sendBttn = findViewById(R.id.send_Bttn);

        Intent i = getIntent();
        if (i != null){
            user = i.getParcelableExtra(UserMessageSearchActivity.TAG);
        }

        setUpUi();
    }

    // TODO: Set up conversation activity
    private void setUpUi() {
        if (user.getUri() != null){
            Uri imgUri = user.getUri();
            messageImg.setImageURI(imgUri);
        }
        else {
            messageImg.setImageResource(R.drawable.default_img);
        }

        String labelString = "Conversation with" + user.getUsername();
        conversationLabel.setText(labelString);
    }

    // TODO: Set up onClickListener for send button
    View.OnClickListener sendClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {



        }
    };

}