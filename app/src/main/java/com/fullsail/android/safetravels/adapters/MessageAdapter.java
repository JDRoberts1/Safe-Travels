package com.fullsail.android.safetravels.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fullsail.android.safetravels.R;
import com.fullsail.android.safetravels.objects.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private static final String TAG = "MessageAdapter";
    boolean status;
    final ArrayList<Message> messages;
    final Context mContext;
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    final int send = 1;
    final int receive = 2;

    public MessageAdapter(ArrayList<Message> messages, Context mContext) {
        this.messages = messages;
        this.mContext = mContext;
        Log.i(TAG, "MessageAdapter: " + messages.size());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view;
        if (viewType == send){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_send, parent, false);
        }
        else{
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_recieve, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message m = messages.get(position);

        if (m.getFromUID().equals(currentUser.getUid())){
            holder.messageSent.setText(m.getMessage());
        }
        else {
            holder.messageReceive.setText(m.getMessage());
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message m = messages.get(position);

        if (m.getFromUID().equals(currentUser.getUid())){
            return send;
        }
        else{

            return receive;
        }
    }

    @Override
    public int getItemCount() {
        if (!messages.isEmpty()){
            return messages.size();
        }
        return 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        final TextView messageReceive;
        final TextView messageSent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            messageReceive = itemView.findViewById(R.id.message_Recv_TV);
            messageSent = itemView.findViewById(R.id.message_Sent_TV);
        }


    }
}
