package com.fullsail.android.safetravels.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.fullsail.android.safetravels.R;
import com.fullsail.android.safetravels.objects.Post;
import com.google.common.collect.ArrayTable;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostListAdapter extends RecyclerView.Adapter<PostListAdapter.ViewHolder> {

    ArrayList<Post> posts;
    Context mContext;

    public PostListAdapter(ArrayList<Post> posts, Context mContext) {
        this.posts = posts;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_rcv_item, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post p = posts.get(position);

        if (p.getProfileImgUri() != null){
            holder.userImageView.setImageURI(p.getProfileImgUri());
        }

        holder.username_CV.setText(p.getUsername());
        holder.title_CV.setText(p.getTitle());
        holder.date_Posted_CV.setText(p.getDatePosted());
    }

    @Override
    public int getItemCount() {
        if (posts != null){
            return posts.size();
        }
        return 0;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder{

        CardView postItemView;
        CircleImageView userImageView;
        TextView username_CV;
        TextView title_CV;
        TextView date_Posted_CV;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            postItemView = itemView.findViewById(R.id.postItemView);
            userImageView = itemView.findViewById(R.id.userImageView);
            username_CV = itemView.findViewById(R.id.username_CV);
            title_CV = itemView.findViewById(R.id.title_CV);
            date_Posted_CV = itemView.findViewById(R.id.date_Posted_CV);
        }
    }

}
