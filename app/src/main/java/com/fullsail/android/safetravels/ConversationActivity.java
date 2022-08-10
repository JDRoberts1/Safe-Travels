package com.fullsail.android.safetravels;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import com.fullsail.android.safetravels.objects.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConversationActivity extends AppCompatActivity {
    private static final String TAG = "ConversationActivity";

    CircleImageView messageImg;
    TextView conversationLabel;
    RecyclerView chatRCV;
    EditText message;
    FloatingActionButton sendBttn;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        messageImg = findViewById(R.id.message_Img);
        conversationLabel = findViewById(R.id.conversation_Label);
        chatRCV = findViewById(R.id.chat_RCV);
        message= findViewById(R.id.message_ETV_Chat);
        sendBttn = findViewById(R.id.send_Bttn);

        Intent i = getIntent();
        if (i != null){
            user = (User) i.getParcelableExtra(UserMessageSearchActivity.TAG);
        }

        setUpUi();
    }

    // TODO: Set up conversation activity
    private void setUpUi() {
        if (!user.getUri().isEmpty()){
            Uri imgUri = Uri.parse(user.getUri());
            messageImg.setImageURI(imgUri);
        }
        else {
            messageImg.setImageResource(R.drawable.default_img);
        }

        String labelString = "Conversation with" + user.getUsername();
        conversationLabel.setText(labelString);
    }

}