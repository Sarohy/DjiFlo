package com.fiv.djiflo.djiflo.View.Activity;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.fiv.djiflo.djiflo.DataLayer.User;
import com.fiv.djiflo.djiflo.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class DetailUserActivity extends AppCompatActivity {

    private TextView tvEmail,tvPhone,tvName,tvFollow;
    private SharedPreferences sharedPreferences;
    private String userId;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference UserFollowRef = database.getReference().child("UsersFollow");
    DatabaseReference UserFollowReqRef = database.getReference().child("UsersFollowReq");
    User u;
    CircleImageView imageView;
    boolean checked=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_user);
        getViews();
        setListeners();
        Bundle b=getIntent().getExtras();
        if (b!=null){
            u= (User) b.get("User");
            tvName.setText(u.getName());
            if (!u.getPhone().equals("")){
                tvPhone.setText(u.getPhone());
            }
            else {
                tvPhone.setText("--");
            }
            if (!u.getEmailId().equals("")){
                tvEmail.setText(u.getEmailId());
            }
            else {
                tvEmail.setText("--");
            }
            Picasso.with(this).load(u.getImageURL()).placeholder(R.drawable.ic_person_white_36dp).into(imageView);

        }
        sharedPreferences=getSharedPreferences("User",MODE_PRIVATE);
        userId=sharedPreferences.getString("UserId",null);
        UserFollowRef=UserFollowRef.child(userId);
        UserFollowRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (u.getId().equals(dataSnapshot.getValue(String.class))){
                    tvFollow.setText("Followed");
                    tvFollow.setBackgroundResource(R.drawable.round_corner_tab);
                    checked=true;
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
        UserFollowReqRef=UserFollowReqRef.child(u.getId());
        UserFollowReqRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (userId.equals(dataSnapshot.getValue(String.class))){
                    tvFollow.setText("Request Sent");
                    checked=true;
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

    private void setListeners() {
        tvFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!checked){
                    UserFollowReqRef=database.getReference().child("UsersFollowReq");
                    UserFollowReqRef.child(u.getId()).push().setValue(userId);
                }
            }
        });
    }

    private void getViews() {
        imageView= (CircleImageView) findViewById(R.id.iv_user_photo);
        tvEmail= (TextView) findViewById(R.id.tv_user_email);
        tvFollow= (TextView) findViewById(R.id.tv_follow);
        tvName= (TextView) findViewById(R.id.tv_user_name);
        tvPhone= (TextView) findViewById(R.id.tv_user_phone);
    }
}
