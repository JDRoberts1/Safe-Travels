package com.fullsail.android.safetravels.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.fullsail.android.safetravels.ProfileActivity;
import com.fullsail.android.safetravels.R;
import com.fullsail.android.safetravels.objects.Post;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomePostListAdapter extends ArrayAdapter<Post> {

    ArrayList<Post> posts;
    Context mContext;
    private static final long BASE_ID = 0x1011;
    public static final String TAG = "HomePostListAdapter";


    public HomePostListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Post> posts) {
        super(context, resource, posts);
        this.posts = posts;
        this.mContext = context;
    }

    @Override
    public int getCount() {
        if (posts != null){
            return posts.size();
        }
        return 0;
    }

    @Override
    public Post getItem(int position) {

        if (posts != null && position >= 0 && position < posts.size()){
            return posts.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return BASE_ID + position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        Post p = posts.get(position);

        if (convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.post_rcv_item, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else{
            holder = (ViewHolder) convertView.getTag();
        }

        if (p.getProfileImgUri() != null){
            holder.userImageView.setImageURI(p.getProfileImgUri());
        }
        else {
            holder.userImageView.setImageResource(R.drawable.default_img);
        }


        holder.username_CV.setText(p.getUsername());
        holder.title_CV.setText(p.getTitle());
        holder.date_Posted_CV.setText(p.getDatePosted());

        return convertView;
    }

    public static class ViewHolder{

        CircleImageView userImageView;
        TextView username_CV;
        TextView title_CV;
        TextView date_Posted_CV;

        public ViewHolder(View layout){
            userImageView = layout.findViewById(R.id.userImageView);
            username_CV = layout.findViewById(R.id.username_CV);
            title_CV = layout.findViewById(R.id.title_CV);
            date_Posted_CV = layout.findViewById(R.id.date_Posted_CV);
        }

    }

}
