package com.fiv.djiflo.djiflo.View.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.fiv.djiflo.djiflo.DataLayer.PlayList;
import com.fiv.djiflo.djiflo.DataLayer.Song;
import com.fiv.djiflo.djiflo.DataLayer.User;
import com.fiv.djiflo.djiflo.R;
import com.fiv.djiflo.djiflo.View.Activity.DetailUserActivity;
import com.fiv.djiflo.djiflo.View.Activity.HomeActivity;
import com.fiv.djiflo.djiflo.View.Adapter.PlayListAdapter;
import com.fiv.djiflo.djiflo.View.Adapter.SongPlayingAdapter;
import com.fiv.djiflo.djiflo.View.Adapter.UserAdapter;
import com.fiv.djiflo.djiflo.service.SongService;
import com.fiv.djiflo.djiflo.util.PlayerConstants;
import com.fiv.djiflo.djiflo.util.UtilFunctions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class SearchFragment extends Fragment {
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    private SongPlayingAdapter adapterSearchedSong;
    private ArrayList<Song> searchedSongs;
    private ListView lv;
    private TextView tvSong,tvPlaylist,tvUser;
    private ArrayList<User> usersList;
    private UserAdapter adapterUsers;
    private ArrayList<PlayList> playLists;
    private PlayListAdapter adapterPlayList;
    private SearchFragmentInterface Interface;
    private boolean isSong=true,isPlaylist=false,isUser=false;
    private String filterText="";
    private LinearLayout nothingLL;
    private AVLoadingIndicatorView loaderSearch;
    SharedPreferences spUser;
    String userId;



    public SearchFragment() {
        // Required empty public constructor
    }
    @SuppressLint("ValidFragment")
    public SearchFragment(Context c) {
        this.Interface= (SearchFragmentInterface) c;
    }

    public static SearchFragment newInstance(String param1, String param2) {
        SearchFragment fragment = new SearchFragment();
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
        View v= inflater.inflate(R.layout.fragment_search, container, false);
        queryingDatabaseSongs();
        init();
        getViews(v);
        setListeners();
        return v;
    }

    private void setListeners() {
        lv.setAdapter(adapterSearchedSong);
        tvUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loaderSearch.smoothToShow();
                tvUser.setBackgroundResource(R.drawable.round_corner_tab);
                tvPlaylist.setBackgroundResource(R.drawable.round_corner_tab_trans);
                tvSong.setBackgroundResource(R.drawable.round_corner_tab_trans);
                usersList.clear();
                queryingDatabaseUsers();
                lv.setAdapter(adapterUsers);
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Intent intent=new Intent(getActivity(),DetailUserActivity.class);
                        intent.putExtra("User",usersList.get(i));
                        getActivity().startActivity(intent);
                    }

                });
                adapterUsers.notifyDataSetChanged();
                isSong=false;
                isUser=true;
                isPlaylist=false;
            }
        });
        tvPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loaderSearch.smoothToShow();
                tvPlaylist.setBackgroundResource(R.drawable.round_corner_tab);
                tvUser.setBackgroundResource(R.drawable.round_corner_tab_trans);
                tvSong.setBackgroundResource(R.drawable.round_corner_tab_trans);
                playLists.clear();
                queryingDatabasePlaylist();
                lv.setAdapter(adapterPlayList);
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Interface.selectedPlaylist(playLists.get(i));
                    }
                });
                isSong=false;
                isUser=false;
                isPlaylist=true;

            }
        });
        tvSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loaderSearch.smoothToShow();
                tvSong.setBackgroundResource(R.drawable.round_corner_tab);
                tvPlaylist.setBackgroundResource(R.drawable.round_corner_tab_trans);
                tvUser.setBackgroundResource(R.drawable.round_corner_tab_trans);
                searchedSongs.clear();
                queryingDatabaseSongs();
                lv.setAdapter(adapterSearchedSong);
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        PlayerConstants.SONGS_LIST.add(searchedSongs.get(i));
                        PlayerConstants.SONG_NUMBER=PlayerConstants.SONGS_LIST.size()-1;
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
                isSong=true;
                isUser=false;
                isPlaylist=false;

            }
        });

        int[] colors = {0, 0xFFFFFFFF, 0}; // white for the example
        lv.setDivider(new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, colors));
        lv.setDividerHeight(1);


    }

    private void queryingDatabasePlaylist() {
        final Query users = database.getReference().child("Playlists");
        users.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                adapterPlayList.getFilter().filter(filterText);
                if (playLists.size()==0){
                    nothingLL.setVisibility(View.VISIBLE);
                    lv.setVisibility(View.GONE);
                }
                else {
                    nothingLL.setVisibility(View.GONE);
                    lv.setVisibility(View.VISIBLE);
                }
                loaderSearch.smoothToHide();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        users.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                PlayList playList = dataSnapshot.getValue(PlayList.class);
                String userKey = dataSnapshot.getKey();
                assert playList != null;
                playList.setId(userKey);
                playLists.add(playList);
                if (playLists.size()==0){
                    nothingLL.setVisibility(View.VISIBLE);
                    lv.setVisibility(View.GONE);
                }
                else {
                    nothingLL.setVisibility(View.GONE);
                    lv.setVisibility(View.VISIBLE);
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
    }

    private void queryingDatabaseUsers() {
        final Query users = database.getReference().child("Users");
        users.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (usersList.size()==0){
                    nothingLL.setVisibility(View.VISIBLE);
                    lv.setVisibility(View.GONE);
                }
                else {
                    nothingLL.setVisibility(View.GONE);
                    lv.setVisibility(View.VISIBLE);
                }
                adapterUsers.getFilter().filter(filterText);
                loaderSearch.smoothToHide();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        users.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                User user = dataSnapshot.getValue(User.class);
                String userKey = dataSnapshot.getKey();
                assert user != null;
                user.setId(userKey);
                if (!userKey.equals(userId))
                usersList.add(user);
                if (usersList.size()==0){
                    nothingLL.setVisibility(View.VISIBLE);
                    lv.setVisibility(View.GONE);
                }
                else {
                    nothingLL.setVisibility(View.GONE);
                    lv.setVisibility(View.VISIBLE);
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
    }

    private void init() {
        searchedSongs=new ArrayList<>();
        usersList=new ArrayList<>();
        playLists=new ArrayList<>();
        adapterSearchedSong=new SongPlayingAdapter(getActivity(),searchedSongs);
        adapterUsers=new UserAdapter(getActivity(),usersList);
        adapterPlayList=new PlayListAdapter(getActivity(),playLists);
        spUser = getActivity().getSharedPreferences("User", MODE_PRIVATE);
        userId=spUser.getString("UserId",null);


    }

    private void getViews(View v) {
        lv= (ListView) v.findViewById(R.id.lv_songs);
        tvSong= (TextView) v.findViewById(R.id.tv_tab_song);
        tvPlaylist= (TextView) v.findViewById(R.id.tv_tab_playlist);
        tvUser= (TextView) v.findViewById(R.id.tv_tab_user);
        nothingLL=v.findViewById(R.id.ll_nothing);
        loaderSearch=v.findViewById(R.id.loaderSearch);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
    
    public void queryingDatabaseSongs(){
        final Query sortedSongs = database.getReference().child("Musics");
        sortedSongs.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                adapterSearchedSong.getFilter().filter(filterText);
                if (searchedSongs.size()==0){
                    nothingLL.setVisibility(View.VISIBLE);
                    lv.setVisibility(View.GONE);
                }
                else {
                    nothingLL.setVisibility(View.GONE);
                    lv.setVisibility(View.VISIBLE);
                }
                loaderSearch.smoothToHide();
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
                searchedSongs.add(song);
                if (searchedSongs.size()==0){
                    nothingLL.setVisibility(View.VISIBLE);
                    lv.setVisibility(View.GONE);
                }
                else {
                    nothingLL.setVisibility(View.GONE);
                    lv.setVisibility(View.VISIBLE);
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

    }

    public void filter(String newText) {
        this.filterText=newText;
        if (!isPlaylist&&!isUser)
            adapterSearchedSong.getFilter().filter(newText);
        else if (!isSong&&!isUser)
            adapterPlayList.getFilter().filter(newText);
        else
            adapterUsers.getFilter().filter(newText);
    }

    public interface SearchFragmentInterface{
        public void selectedPlaylist(PlayList p);
    }

}
