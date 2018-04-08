package com.fiv.djiflo.djiflo.View.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fiv.djiflo.djiflo.DataLayer.User;
import com.fiv.djiflo.djiflo.R;
import com.fiv.djiflo.djiflo.util.PlayerConstants;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by apple on 16/11/2017.
 */

public class UserDedicateAdapter extends BaseAdapter {
    Context context;
    ArrayList<User> users;
    private static LayoutInflater inflater=null;
    public UserDedicateAdapter(Context context, ArrayList<User> songs) {
        this.context=context;
        this.users=songs;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return users.size();
    }

    @Override
    public Object getItem(int i) {
        return users.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View vi=view;
        if(vi==null)
            vi = inflater.inflate(R.layout.listview_user_list_dedicate, null);

        TextView title = (TextView)vi.findViewById(R.id.tv_user_name);
        TextView dedicate = (TextView)vi.findViewById(R.id.tv_tab_dedicate);
        ImageView thumb_image=(ImageView)vi.findViewById(R.id.iv_user_photo); // thumb image

        title.setText(users.get(i).getName());
        Picasso.with(context).load(users.get(i).getImageURL()).placeholder(R.drawable.ic_person_white_36dp).into(thumb_image);
        final int pos=i;
        dedicate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences = context.getSharedPreferences("User", context.MODE_PRIVATE);
                final String userId = sharedPreferences.getString("UserId", null);
                final FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference UserReqRef = database.getReference().child("UsersDedication").child(userId);
                UserReqRef=UserReqRef.child(PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getMusicId());
                UserReqRef.push().setValue(users.get(pos).getId());

            }
        });

        return vi;
    }
}
