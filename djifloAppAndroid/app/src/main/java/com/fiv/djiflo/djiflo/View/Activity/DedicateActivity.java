package com.fiv.djiflo.djiflo.View.Activity;

import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.fiv.djiflo.djiflo.DataLayer.User;
import com.fiv.djiflo.djiflo.R;
import com.fiv.djiflo.djiflo.View.Adapter.UserDedicateAdapter;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;

public class DedicateActivity extends AppCompatActivity {

    private ListView lv;
    private ArrayList<User> userList;
    private UserDedicateAdapter adapterUser;
    private AVLoadingIndicatorView loaderUser;
    private LinearLayout nothingLL;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dedicate);
        init();
        getViews();
        setListeners();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference UserFollowReqRef = database.getReference().child("UsersFollow");
        SharedPreferences sharedPreferences = getSharedPreferences("User", MODE_PRIVATE);
        final String userId = sharedPreferences.getString("UserId", null);
        UserFollowReqRef=UserFollowReqRef.child(userId);
        UserFollowReqRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                adapterUser.notifyDataSetChanged();
                if (userList.size()==0){
                    nothingLL.setVisibility(View.VISIBLE);
                    lv.setVisibility(View.GONE);
                }
                else {
                    nothingLL.setVisibility(View.GONE);
                    lv.setVisibility(View.VISIBLE);
                }
                loaderUser.hide();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        UserFollowReqRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                DatabaseReference UserRef = database.getReference().child("Users").child(dataSnapshot.getValue().toString());
                UserRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User u=dataSnapshot.getValue(User.class);
                        userList.add(u);
                        if (userList.size()==0){
                            nothingLL.setVisibility(View.VISIBLE);
                            lv.setVisibility(View.GONE);
                        }
                        else {
                            nothingLL.setVisibility(View.GONE);
                            lv.setVisibility(View.VISIBLE);
                        }
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
    private void setListeners() {
        lv.setAdapter(adapterUser);
        int[] colors = {0, 0xFFFFFFFF, 0}; // white for the example
        lv.setDivider(new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, colors));
        lv.setDividerHeight(1);
    }

    private void init() {
        userList=new ArrayList<>();
        adapterUser=new UserDedicateAdapter(getApplicationContext(),userList);
    }

    private void getViews() {
        lv= (ListView) findViewById(R.id.lv_users);
        loaderUser=findViewById(R.id.loaderUser);
        nothingLL=findViewById(R.id.ll_nothing);
    }

}
