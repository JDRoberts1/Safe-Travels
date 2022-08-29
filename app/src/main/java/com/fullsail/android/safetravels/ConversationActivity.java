package com.fullsail.android.safetravels;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fullsail.android.safetravels.adapters.MessageAdapter;
import com.fullsail.android.safetravels.objects.Message;
import com.fullsail.android.safetravels.objects.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDate;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConversationActivity extends AppCompatActivity {
    private static final String TAG = "ConversationActivity";

    CircleImageView messageImg;
    TextView conversationLabel;
    RecyclerView chatRCV;
    EditText messageETV;
    FloatingActionButton sendBttn;
    User selectedUser;
    FirebaseUser currentUser;
    MessageAdapter messageAdapter;

    FirebaseFirestore db;
    DatabaseReference ref;
    ArrayList<Message> messages;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        db = FirebaseFirestore.getInstance();
        messages = new ArrayList<>();

        messageImg = findViewById(R.id.message_Img);
        conversationLabel = findViewById(R.id.conversation_Label);
        chatRCV = findViewById(R.id.chat_RCV);
        chatRCV.setLayoutManager(new LinearLayoutManager(ConversationActivity.this.getApplicationContext()));
        messageAdapter = new MessageAdapter(messages, ConversationActivity.this.getApplicationContext());
        chatRCV.setAdapter(messageAdapter);
        messageETV = findViewById(R.id.message_ETV_Chat);
        sendBttn = findViewById(R.id.send_Bttn);
        sendBttn.setOnClickListener(sendClick);


        Intent i = getIntent();
        if (i != null){
            selectedUser = i.getParcelableExtra(UserMessageSearchActivity.TAG);
        }

        setUpUi();

    }

    // TODO: Set up conversation activity
    private void setUpUi() {
        if (selectedUser.getUri() != null){
            Uri imgUri = selectedUser.getUri();
            messageImg.setImageURI(imgUri);
        }
        else {
            messageImg.setImageResource(R.drawable.default_img);
        }

        String labelString = "Conversation with " + selectedUser.getUsername();
        conversationLabel.setText(labelString);
    }

    // TODO: Set up onClickListener for send button
    View.OnClickListener sendClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String message = messageETV.getText().toString();

            if (!message.isEmpty()){
                sendMessage(message);
                messageETV.setText("");
            }

        }
    };

    private void sendMessage(String message) {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        LocalDate tS = LocalDate.now();

        Message newMessage = new Message(selectedUser.getUid(), currentUser.getUid(), message, tS.toString(), false);

        updateDatabase(newMessage);
    }

    private void updateDatabase(Message newMessage) {
        CollectionReference reference = db.collection("users").document(currentUser.getUid()).collection("messages");
        DocumentReference documentReference = reference.document(selectedUser.getUid());
        CollectionReference threadReference = documentReference.collection("thread");
        threadReference.add(newMessage)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference docReference) {
                        CollectionReference reference = db.collection("users").document(selectedUser.getUid()).collection("messages");
                        DocumentReference documentReference = reference.document(currentUser.getUid());
                        CollectionReference threadReference = documentReference.collection("thread");
                        threadReference.add(newMessage);
                    }
                });

    }

    public void getMessage(){

    }

}