package com.fiv.djiflo.djiflo.View.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.fiv.djiflo.djiflo.DataLayer.Notification;
import com.fiv.djiflo.djiflo.DataLayer.Song;
import com.fiv.djiflo.djiflo.R;
import com.fiv.djiflo.djiflo.View.Activity.HomeActivity;
import com.fiv.djiflo.djiflo.service.SongService;
import com.fiv.djiflo.djiflo.util.PlayerConstants;
import com.fiv.djiflo.djiflo.util.UtilFunctions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by apple on 16/11/2017.
 */

public class NotificationAdapter extends BaseAdapter {
    Context context;
    ArrayList<Notification> notifications;
    private static LayoutInflater inflater=null;
    public NotificationAdapter(Context context, ArrayList<Notification> songs) {
        this.context=context;
        this.notifications=songs;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return notifications.size();
    }

    @Override
    public Object getItem(int i) {
        return notifications.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View vi=view;
        if(vi==null)
            vi = inflater.inflate(R.layout.listview_notification, null);

        final TextView message = (TextView)vi.findViewById(R.id.tv_message); // title
        final TextView accept = (TextView)vi.findViewById(R.id.tv_tab_accept); // artist name
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final int pos=i;
        if (notifications.get(i).getType().toLowerCase().equals("follow request")) {
            accept.setText("Accept");
            SharedPreferences sharedPreferences = context.getSharedPreferences("User", context.MODE_PRIVATE);
            final String userId = sharedPreferences.getString("UserId", null);
            DatabaseReference UserReqRef = database.getReference().child("Users").child(notifications.get(i).getoUser());
            UserReqRef.addChildEventListener(new ChildEventListener() {

                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    if (dataSnapshot.getKey().toLowerCase().equals("name")) {
                        message.setText(dataSnapshot.getValue().toString() + " " + notifications.get(pos).getMessage());
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
            accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (accept.getText().toString().toLowerCase().equals("accept")) {
                        final FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference UserFollowRef = database.getReference().child("UsersFollow").child(userId);
                        UserFollowRef.push().setValue(notifications.get(pos).getoUser());
                        UserFollowRef = database.getReference().child("UsersFollow").child(notifications.get(pos).getoUser());
                        UserFollowRef.push().setValue(userId);
                        DatabaseReference UserReqRef = database.getReference().child("UsersFollowReq").child(userId).child(notifications.get(pos).getId());
                        UserReqRef.removeValue();
                        accept.setText("Accepted");
                    } else {
                        Toast.makeText(context,"Accepted Already",Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }
        else if (notifications.get(i).getType().toLowerCase().equals("song dedicated"))
        {
            accept.setText("Play");
            SharedPreferences sharedPreferences = context.getSharedPreferences("User", context.MODE_PRIVATE);
            final String userId = sharedPreferences.getString("UserId", null);
            DatabaseReference UserReqRef = database.getReference().child("Users").child(notifications.get(i).getoUser());
            DatabaseReference MusicRef = database.getReference().child("Musics").child(notifications.get(i).getMusicId());
           final ArrayList<Song> song = new ArrayList<>();
            MusicRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Song so=dataSnapshot.getValue(Song.class);
                    so.setMusicId(dataSnapshot.getKey());
                    song.add(so);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            UserReqRef.addChildEventListener(new ChildEventListener() {

                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    if (dataSnapshot.getKey().toLowerCase().equals("name")) {
                        message.setText(dataSnapshot.getValue().toString() + " " + notifications.get(pos).getMessage()+" "+song.get(0).getName());
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
            accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (accept.getText().toString().toLowerCase().equals("play")) {
                        PlayerConstants.SONGS_LIST.add(song.get(0));
                        PlayerConstants.SONG_NUMBER=PlayerConstants.SONGS_LIST.size()-1;
                        PlayerConstants.SONG_PAUSED=false;
                        boolean isServiceRunning = UtilFunctions.isServiceRunning(SongService.class.getName(), context);
                        if (!isServiceRunning) {
                            Intent i = new Intent(context,SongService.class);
                            context.startService(i);
                        } else {
                            PlayerConstants.SONG_CHANGE_HANDLER.sendMessage(PlayerConstants.SONG_CHANGE_HANDLER.obtainMessage());
                        }
                        HomeActivity.updateUI();

                    }
                }
            });
        }
        return vi;
    }
}
