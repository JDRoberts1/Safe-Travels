package com.fullsail.android.safetravels.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.fullsail.android.safetravels.R;
import com.fullsail.android.safetravels.objects.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class UserListAdapter extends ArrayAdapter<User> {

    private static final long BASE_ID = 0x1011;
    private static final String TAG = "ListViewAdapter";
    private final Context mContext;
    private final ArrayList<User> mResults;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser cUser = mAuth.getCurrentUser();

    public UserListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<User> users) {
        super(context, resource, users);
        this.mContext = context;
        this.mResults = users;
    }

    @Override
    public int getCount() {
        if(mResults != null){
            return mResults.size();
        }
        return 0;
    }

    @Override
    public User getItem(int position) {

        if (mResults != null && position >= 0 && position < mResults.size()){
            return mResults.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return BASE_ID + position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        User u = (User) getItem(position);

        if (convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.user_listview_item, parent, false);
            vh = new ViewHolder(convertView);
            convertView.setTag(vh);
        } else{
            vh = (ViewHolder) convertView.getTag();

        }

        if (u != null){
            vh._usernameLabel.setText(u.getUsername());
        }

        if (u.getUri() != null){
            Uri imgUri = Uri.parse(u.getUri());
            vh._userImg.setImageURI(imgUri);
        }
        else {
            vh._userImg.setImageResource(R.drawable.default_img);
        }

        return convertView;
    }

    static class ViewHolder{
        TextView _usernameLabel;
        ImageView _userImg;

        public ViewHolder(View layout){
            _usernameLabel = layout.findViewById(R.id.userName_LV_Label);
            _userImg = layout.findViewById(R.id.user_ListView_Img);
        }
    }
}
