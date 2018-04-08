package com.fiv.djiflo.djiflo.View.Fragments;

import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.fiv.djiflo.djiflo.DataLayer.Notification;
import com.fiv.djiflo.djiflo.R;
import com.fiv.djiflo.djiflo.View.Adapter.NotificationAdapter;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;


public class NotificationFragment extends Fragment {

    private ArrayList<Notification> notiList;
    private ListView lv;
    private NotificationAdapter adapterNoti;
    private LinearLayout nothingLL;
    private AVLoadingIndicatorView loaderNoti;

    public NotificationFragment() {
        // Required empty public constructor
    }
public static NotificationFragment newInstance(String param1, String param2) {
        NotificationFragment fragment = new NotificationFragment();
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
        View v= inflater.inflate(R.layout.fragment_notification, container, false);
        init();
        getViews(v);
        setListeners();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference UserFollowReqRef = database.getReference().child("UsersFollowReq");
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("User", getActivity().MODE_PRIVATE);
        final String userId = sharedPreferences.getString("UserId", null);
        UserFollowReqRef=UserFollowReqRef.child(userId);
        UserFollowReqRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (notiList.size()==0){
                    nothingLL.setVisibility(View.VISIBLE);
                    lv.setVisibility(View.GONE);
                }
                else {
                    nothingLL.setVisibility(View.GONE);
                    lv.setVisibility(View.VISIBLE);
                }
                adapterNoti.notifyDataSetChanged();
                loaderNoti.hide();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        UserFollowReqRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Notification notification=new Notification();
                notification.setUserId(userId);
                notification.setMessage("wants to add you");
                notification.setType("Follow Request");
                notification.setoUser(dataSnapshot.getValue().toString());
                notification.setId(dataSnapshot.getKey());
                notiList.add(notification);
                if (notiList.size()==0){
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
                int i=0;
                for ( ;i<notiList.size();i++){
                    if (notiList.get(i).getoUser().equals(dataSnapshot.getValue().toString())){
                        break;
                    }
                }
                notiList.remove(i);
                adapterNoti.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        DatabaseReference DedicateRef = database.getReference().child("UsersDedication").child(userId);
        DedicateRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                final Notification n=new Notification();
                n.setMusicId(dataSnapshot.getKey());
                n.setMessage("dedicate you a song named");
                n.setType("Song Dedicated");
                n.setUserId(userId);
                DatabaseReference DedicateInnerRef = database.getReference().child("UsersDedication").child(userId).child(dataSnapshot.getKey());
                DedicateInnerRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        adapterNoti.notifyDataSetChanged();
                        if (notiList.size()==0){
                            nothingLL.setVisibility(View.VISIBLE);
                            lv.setVisibility(View.GONE);
                        }
                        else {
                            nothingLL.setVisibility(View.GONE);
                            lv.setVisibility(View.VISIBLE);
                        }
                        loaderNoti.hide();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                DedicateInnerRef.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        n.setoUser(dataSnapshot.getValue(String.class));
                        notiList.add(n);
                        if (notiList.size()==0){
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
        return v;
    }

    private void setListeners() {
        lv.setAdapter(adapterNoti);
        int[] colors = {0, 0xFFFFFFFF, 0}; // white for the example
        lv.setDivider(new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, colors));
        lv.setDividerHeight(1);
    }

    private void init() {
        notiList=new ArrayList<>();
        adapterNoti=new NotificationAdapter(getContext(),notiList);
    }

    private void getViews(View v) {
        lv= (ListView) v.findViewById(R.id.lv_notification);
        nothingLL=v.findViewById(R.id.ll_nothing);
        loaderNoti=v.findViewById(R.id.loaderNoti);
    }


    @Override
    public void onDetach() {
        super.onDetach();
    }
}
