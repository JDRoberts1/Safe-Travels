package com.fullsail.android.safetravels.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.fullsail.android.safetravels.R;
import com.fullsail.android.safetravels.objects.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConversationListAdapter extends RecyclerView.Adapter<ConversationListAdapter.ViewHolder> {

    final Context context;
    final ArrayList<User> users;
    clickListener clickListener;

    public interface clickListener{
        void onItemClick(User u);
    }

    public void setClickListener(clickListener listener){
        this.clickListener = listener;
    }

    public ConversationListAdapter(Context context, ArrayList<User> users){
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.card_view_users, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User u = users.get(position);
        holder.usernameLabel.setText(u.getUsername());

        StorageReference storageReference = FirebaseStorage.getInstance().getReference(u.getUid());

        StorageReference imgReference = storageReference.child(u.getUid());
        final long MEGABYTE = 1024 * 1024;
        imgReference.getBytes(MEGABYTE)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        if (bytes.length > 0) {
                            InputStream is = new ByteArrayInputStream(bytes);
                            Bitmap bmp = BitmapFactory.decodeStream(is);
                            holder.circleImageView.setImageBitmap(bmp);
                        } else {
                            holder.circleImageView.setImageResource(R.drawable.default_img);
                        }
                    }
                });

        if (u.getUid() != null){
            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.onItemClick(u);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        if (!users.isEmpty()){
            return users.size();
        }
        else {
            return 0;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public final CircleImageView circleImageView;
        public final TextView usernameLabel;
        public final TextView messageLabel;
        public final CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.cardView = (CardView) itemView.findViewById(R.id.cardview);
            this.circleImageView = itemView.findViewById(R.id.userProfileImgView);
            this.usernameLabel = itemView.findViewById(R.id.usernameLabel);
            this.messageLabel = itemView.findViewById(R.id.messageLabel);
        }
    }
}
