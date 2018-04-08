package com.fiv.djiflo.djiflo.View.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.fiv.djiflo.djiflo.DataLayer.Comment;
import com.fiv.djiflo.djiflo.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

/**
 * Created by apple on 16/11/2017.
 */

public class CommentAdapter extends BaseAdapter {
    Context context;
    ArrayList<Comment> comments;
    private static LayoutInflater inflater=null;
    public CommentAdapter(Context context, ArrayList<Comment> songs) {
        this.context=context;
        this.comments=songs;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return comments.size();
    }

    @Override
    public Object getItem(int i) {
        return comments.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View vi=view;
        if(vi==null)
            vi = inflater.inflate(R.layout.listview_comment, null);

        TextView comment = (TextView)vi.findViewById(R.id.tv_comment); // title
        final TextView userName = (TextView)vi.findViewById(R.id.tv_userName); // artist name
        TextView date = (TextView)vi.findViewById(R.id.tv_date_time); // duration

        comment.setText(comments.get(i).getComment());
        userName.setText(comments.get(i).getUserId());
        date.setText(comments.get(i).getDate());
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ArtistRef = database.getReference().child("Users").child(comments.get(i).getUserId());;
        ArtistRef.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getKey().toLowerCase().equals("name")){
                    userName.setText(dataSnapshot.getValue(String.class));
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return vi;
    }
}
