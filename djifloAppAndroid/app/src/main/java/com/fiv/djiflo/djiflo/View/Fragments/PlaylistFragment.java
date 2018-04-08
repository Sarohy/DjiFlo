package com.fiv.djiflo.djiflo.View.Fragments;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fiv.djiflo.djiflo.DataLayer.PlayList;
import com.fiv.djiflo.djiflo.DataLayer.Song;
import com.fiv.djiflo.djiflo.R;
import com.fiv.djiflo.djiflo.View.Activity.HomeActivity;
import com.fiv.djiflo.djiflo.View.Adapter.SongPlayingAdapter;
import com.fiv.djiflo.djiflo.service.SongService;
import com.fiv.djiflo.djiflo.util.PlayerConstants;
import com.fiv.djiflo.djiflo.util.UtilFunctions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class PlaylistFragment extends Fragment {
    private Button btnPlay;
    private ListView lv;
    private RoundedImageView ivPlaylist;
    private TextView tvPlaylistName;
    private ArrayList<Song> songList;
    private SongPlayingAdapter adapterSong;

    public PlaylistFragment() {
        // Required empty public constructor
    }

    public static PlaylistFragment newInstance() {
        PlaylistFragment fragment = new PlaylistFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v= inflater.inflate(R.layout.fragment_playlist, container, false);
        init();
        getViews(v);
        setListeners();
        Bundle b=getArguments();
        if (b!=null){
            final PlayList p= (PlayList) b.get("playlist");
            tvPlaylistName.setText(p.getName());
            Picasso.with(getContext()).load(p.getImageURL()).into(ivPlaylist);
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference PlaylistSongRef = database.getReference().child("PlayListMusics").child(p.getId());
            PlaylistSongRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    adapterSong.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            PlaylistSongRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    final DatabaseReference SongRef = database.getReference().child("Musics").child(dataSnapshot.getValue(String.class));
                    SongRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Song s=dataSnapshot.getValue(Song.class);
                            s.setMusicId(dataSnapshot.getKey());
                            songList.add(s);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
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
        return v;
    }

    private void init() {
        songList=new ArrayList<>();
        adapterSong=new SongPlayingAdapter(getContext(),songList);
    }

    private void setListeners() {
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlayerConstants.SONGS_LIST.addAll(songList);
                PlayerConstants.SONG_NUMBER=PlayerConstants.SONGS_LIST.size()-songList.size();
                Toast.makeText(getContext(),String.valueOf(PlayerConstants.SONG_NUMBER),Toast.LENGTH_SHORT).show();
                PlayerConstants.SONG_PAUSED=false;
                boolean isServiceRunning = UtilFunctions.isServiceRunning(SongService.class.getName(), getContext());
                if (!isServiceRunning) {
                    Intent intent = new Intent(getContext(),SongService.class);
                    getContext().startService(intent);
                } else {
                    PlayerConstants.SONG_CHANGE_HANDLER.sendMessage(PlayerConstants.SONG_CHANGE_HANDLER.obtainMessage());
                }
                HomeActivity.updateUI();

            }
        });
        lv.setAdapter(adapterSong);
        int[] colors = {0, 0xFFFFFFFF, 0}; // white for the example
        lv.setDivider(new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, colors));
        lv.setDividerHeight(1);
    }


    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void getViews(View v) {
        btnPlay= (Button) v.findViewById(R.id.btnPlay);
        lv=(ListView)v.findViewById(R.id.lv_playlist_songs);
        ivPlaylist=(RoundedImageView)v.findViewById(R.id.iv_playlist_art);
        tvPlaylistName=(TextView)v.findViewById(R.id.tv_playlist_name);
    }
}
