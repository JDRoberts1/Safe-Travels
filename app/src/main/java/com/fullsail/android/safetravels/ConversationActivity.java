package com.fullsail.android.safetravels;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConversationActivity extends AppCompatActivity {
    public static final String TAG = "ConversationActivity";

    CircleImageView messageImg;
    TextView conversationLabel;
    RecyclerView chatRCV;
    EditText messageETV;
    FloatingActionButton sendBttn;
    User selectedUser;
    FirebaseUser currentUser;
    MessageAdapter messageAdapter;

    FirebaseFirestore db;
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
            selectedUser = i.getParcelableExtra(TAG);
        }

        setUpUi();

    }

    // Set up conversation activity
    private void setUpUi() {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(selectedUser.getUid());
        StorageReference imgReference = storageReference.child(selectedUser.getUid());

        final long MEGABYTE = 1024 * 1024;
        imgReference.getBytes(MEGABYTE)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        if (bytes.length > 0){
                            InputStream is = new ByteArrayInputStream(bytes);
                            Bitmap bmp = BitmapFactory.decodeStream(is);
                            messageImg.setImageBitmap(bmp);
                            selectedUser.setUri(getImgUri(getApplicationContext(), bmp, selectedUser.getUid()));

                        }
                        else {
                            messageImg.setImageResource(R.drawable.default_img);
                        }
                    }
                });

        String labelString = "Conversation with " + selectedUser.getUsername();
        conversationLabel.setText(labelString);
    }

    // TODO: Set up onClickListener for send button
    final View.OnClickListener sendClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String message = messageETV.getText().toString();

            if (!message.isEmpty()){
                sendMessage(message);
                messageETV.setText("");
            }

        }
    };

    public Uri getImgUri(Context c, Bitmap bitmapImg, String uuid){
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmapImg.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(c.getContentResolver(), bitmapImg, uuid, null);
        return Uri.parse(path);
    }

    private void sendMessage(String message) {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        LocalDate tS = LocalDate.now();

        Message newMessage = new Message(selectedUser.getUid(), currentUser.getUid(), message, tS.toString(), false);

        updateDatabase(newMessage);
    }

    private void updateDatabase(Message newMessage) {
        CollectionReference reference = db.collection("users").document(currentUser.getUid()).collection("messages");
        DocumentReference documentReference = reference.document(selectedUser.getUid());
        documentReference.set(selectedUser);
        CollectionReference threadReference = documentReference.collection("thread");
        threadReference.add(newMessage)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference docReference) {
                        CollectionReference reference = db.collection("users").document(selectedUser.getUid()).collection("messages");
                        DocumentReference documentReference = reference.document(currentUser.getUid());
                        User cUser = new User(currentUser.getDisplayName(), currentUser.getUid() ,currentUser.getPhotoUrl());
                        documentReference.set(cUser);
                        CollectionReference threadReference = documentReference.collection("thread");
                        threadReference.add(newMessage);
                    }
                });

    }
}