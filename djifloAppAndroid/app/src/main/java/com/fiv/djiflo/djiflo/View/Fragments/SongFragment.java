package com.fiv.djiflo.djiflo.View.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.Collections;

import static android.content.Context.MODE_PRIVATE;


public class SongFragment extends Fragment {
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference LikeRef;
    DatabaseReference BannerRef = database.getReference().child("Banner");;
    ArrayList<Song> mostPlayedSongs,likedSongs;
    private RecyclerView rvMostPlayed,rvLiked;
    private SongsAdapter adapterMostPlayed,adapterLiked;
    ImageView ivBanner;
    private String userId;
    AVLoadingIndicatorView loaderMostPlayed,loaderLiked;
    static AVLoadingIndicatorView loaderPlaySong;
    private SharedPreferences sharedPreferences;

    public SongFragment() {
        // Required empty public constructor
    }

    public static SongFragment newInstance() {
        SongFragment fragment = new SongFragment();
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
        View v= inflater.inflate(R.layout.fragment_song_home, container, false);
        init();
        databaseQuerying();
        getViews(v);
        setListeners(v);
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        rvMostPlayed.setLayoutManager(mLayoutManager);
        rvMostPlayed.setItemAnimator(new DefaultItemAnimator());
        rvMostPlayed.setAdapter(adapterMostPlayed);
        final LinearLayoutManager mLayoutManager1 = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        rvLiked.setLayoutManager(mLayoutManager1);
        rvLiked.setItemAnimator(new DefaultItemAnimator());
        rvLiked.setAdapter(adapterLiked);
        return v;
    }
    private void init() {
        mostPlayedSongs=new ArrayList<>();
        likedSongs=new ArrayList<>();
        adapterMostPlayed = new SongsAdapter(getActivity(),mostPlayedSongs);
        adapterLiked = new SongsAdapter(getActivity(),likedSongs);
        sharedPreferences=getActivity().getSharedPreferences("User",MODE_PRIVATE);
        userId=sharedPreferences.getString("UserId",null);
    }


    private void databaseQuerying() {
        final Query sortedSongs = database.getReference().child("Musics").orderByChild("stream").limitToLast(10);
        sortedSongs.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Collections.reverse(mostPlayedSongs);
                adapterMostPlayed.notifyDataSetChanged();
                loaderMostPlayed.hide();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        sortedSongs.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Song song = dataSnapshot.getValue(Song.class);
                String songKey = dataSnapshot.getKey();
                assert song != null;
                song.setMusicId(songKey);
                mostPlayedSongs.add(song);
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
        BannerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Picasso.with(getActivity()).load(dataSnapshot.getValue(String.class)).into(ivBanner);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        if (userId != null) {
            LikeRef = database.getReference().child("UserLikes").getRef().child(userId);
            LikeRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    loaderLiked.hide();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            LikeRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    DatabaseReference SongRef = database.getReference().child("Musics").getRef().child(dataSnapshot.getKey());
                    SongRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            likedSongs.add(dataSnapshot.getValue(Song.class));
                            adapterLiked.notifyDataSetChanged();
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
    }




    @Override
    public void onDetach() {
        super.onDetach();
    }

    public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.MyViewHolder> {

        private ArrayList<Song> songArrayList;
        private Context context;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView title, duration;
            public RoundedImageView art;

            public MyViewHolder(View view) {
                super(view);
                title = (TextView) view.findViewById(R.id.tv_song_name);
                duration = (TextView) view.findViewById(R.id.tv_song_duration);
                art = (RoundedImageView) view.findViewById(R.id.iv_song_art);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        HomeActivity.playingRequest();
                        final int itemPosition = rvMostPlayed.getChildLayoutPosition(view);
                        PlayerConstants.SONG_PAUSED = false;
                        PlayerConstants.SONGS_LIST.add(songArrayList.get(itemPosition));
                        PlayerConstants.SONG_NUMBER = PlayerConstants.SONGS_LIST.size()-1;
                        boolean isServiceRunning = UtilFunctions.isServiceRunning(SongService.class.getName(), getActivity());
                        if (!isServiceRunning) {
                            Intent i = new Intent(getActivity(),SongService.class);
                            getActivity().startService(i);
                        } else {
                            PlayerConstants.SONG_CHANGE_HANDLER.sendMessage(PlayerConstants.SONG_CHANGE_HANDLER.obtainMessage());
                        }
                        HomeActivity.updateUI();
                        //changeButton();
                    }
                });
            }
        }


        public SongsAdapter(Context context, ArrayList<Song> songArrayList) {
            this.context=context;
            this.songArrayList = songArrayList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.song_rv_horizontal, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            Song song = songArrayList.get(position);
            holder.title.setText(song.getName());
            holder.duration.setText(song.getDuration());
            //holder.art.setImageBitmap(song.getArt());
            Picasso.with(context).load(song.getArtURL()).fit().into(holder.art);
        }

        @Override
        public int getItemCount() {
            return songArrayList.size();
        }

    }
    private void getViews(View v) {
        ivBanner= (ImageView) v.findViewById(R.id.iv_banner);
        rvMostPlayed = (RecyclerView) v.findViewById(R.id.rv_most_played);
        rvLiked= (RecyclerView) v.findViewById(R.id.rv_liked);
        loaderMostPlayed= (AVLoadingIndicatorView) v.findViewById(R.id.loaderMostPlayed);
        loaderLiked= (AVLoadingIndicatorView) v.findViewById(R.id.loaderLiked);
        loaderPlaySong= (AVLoadingIndicatorView) v.findViewById(R.id.loaderPlaySong);

    }
    private void setListeners(View v) {

    }


}
