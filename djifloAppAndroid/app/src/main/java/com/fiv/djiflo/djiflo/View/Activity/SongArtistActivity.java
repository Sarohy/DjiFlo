package com.fiv.djiflo.djiflo.View.Activity;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.fiv.djiflo.djiflo.DataLayer.Artist;
import com.fiv.djiflo.djiflo.R;
import com.fiv.djiflo.djiflo.util.PlayerConstants;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class SongArtistActivity extends AppCompatActivity {

    private TextView tvName;
    Artist artist;
    private CircleImageView imgArtist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_artist);
        imgArtist= (CircleImageView) findViewById(R.id.iv_artist_photo);
        tvName= (TextView) findViewById(R.id.tv_artist_name);
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ArtistRef = database.getReference().child("Artists").child(PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getArtist());;
        ArtistRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getKey().toLowerCase().equals("name"))
                    tvName.setText(dataSnapshot.getValue(String.class));
                if (dataSnapshot.getKey().toLowerCase().equals("imageurl"))
                    Picasso.with(getApplicationContext()).load(dataSnapshot.getValue(String.class)).into(imgArtist);
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
    }
}
