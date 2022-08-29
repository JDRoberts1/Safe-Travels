package com.fullsail.android.safetravels.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.fullsail.android.safetravels.R;
import com.fullsail.android.safetravels.objects.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class PendingFriendsListAdapter extends ArrayAdapter<PendingFriendsListAdapter.ViewHolder> {

    private static final long BASE_ID = 0x1011;
    private static final String TAG = "ListViewAdapter";
    private final Context mContext;
    private final ArrayList<User> mResults;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser cUser = mAuth.getCurrentUser();
    StorageReference storageReference = FirebaseStorage.getInstance().getReference(cUser.getUid());
    buttonListener buttonListener;

    public interface buttonListener{
        public void onAcceptClick(int position);
        public void onDeclineClick(int position);
    }

    public void setButtonListener(buttonListener listener){
        this.buttonListener = listener;
    }

    public PendingFriendsListAdapter(@NonNull Context context, int resource, ArrayList<User> mResults) {
        super(context, resource);
        this.mContext = context;
        this.mResults = mResults;
    }

    @Override
    public int getCount() {
        if(mResults != null){
            return mResults.size();
        }
        return 0;
    }

    @Override
    public long getItemId(int position) {
        return BASE_ID + position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        User u = mResults.get(position);

        if (convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.friend_listview_item, parent, false);
            vh = new ViewHolder(convertView);
            convertView.setTag(vh);
        } else{
            vh = (ViewHolder) convertView.getTag();

        }

        if (u != null){
            vh._usernameLabel.setText(u.getUsername());

            storageReference = FirebaseStorage.getInstance().getReference(u.getUid());

            StorageReference imgReference = storageReference.child(u.getUid());
            final long MEGABYTE = 1024 * 1024;
            imgReference.getBytes(MEGABYTE)
                    .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            if (bytes.length > 0){
                                InputStream is = new ByteArrayInputStream(bytes);
                                Bitmap bmp = BitmapFactory.decodeStream(is);
                                vh._userImg.setImageBitmap(bmp);
                            }
                            else {
                                vh._userImg.setImageResource(R.drawable.default_img);
                            }
                        }
                    });

            vh.addBttn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    buttonListener.onAcceptClick(position);
                }
            });

            vh.declineBttn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    buttonListener.onDeclineClick(position);
                }
            });
        }


        return convertView;
    }

    static class ViewHolder{
        TextView _usernameLabel;
        ImageView _userImg;
        Button addBttn;
        Button declineBttn;

        public ViewHolder(View layout){
            _usernameLabel = layout.findViewById(R.id.userName_LV_Label);
            _userImg = layout.findViewById(R.id.user_ListView_Img);
            addBttn = layout.findViewById(R.id.accept_bttn);
            declineBttn = layout.findViewById(R.id.decline_Bttn);
        }
    }
}
