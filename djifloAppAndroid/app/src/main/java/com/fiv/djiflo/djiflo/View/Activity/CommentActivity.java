package com.fiv.djiflo.djiflo.View.Activity;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.fiv.djiflo.djiflo.DataLayer.Comment;
import com.fiv.djiflo.djiflo.R;
import com.fiv.djiflo.djiflo.View.Adapter.CommentAdapter;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.Iterator;

public class CommentActivity extends AppCompatActivity {

    EditText etComment;
    Button btnSend;
    ListView lvComments;
    SharedPreferences spUser;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference CommentRef = database.getReference().child("UserComments");
    ArrayList<Comment> comments;
    String musicId;
    CommentAdapter adapter;
    private String userId;
    private ArrayList<String> listComments;
    private LinearLayout nothingLL;
    private AVLoadingIndicatorView loaderComment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        init();
        comments=new ArrayList<>();
        listComments=new ArrayList<>();
        adapter=new CommentAdapter(this,comments);
        lvComments.setAdapter(adapter);
        musicId= String.valueOf(getIntent().getExtras().getString("MusicId"));
        spUser=getSharedPreferences("User",MODE_PRIVATE);
        userId=spUser.getString("UserId",null);
        CommentRef=database.getReference().child("UserComments");
        CommentRef=CommentRef.getRef().child(musicId);

        CommentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (comments.size()==0){
                    nothingLL.setVisibility(View.VISIBLE);
                    lvComments.setVisibility(View.GONE);
                }
                else {
                    nothingLL.setVisibility(View.GONE);
                    lvComments.setVisibility(View.VISIBLE);
                }
                loaderComment.hide();
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        CommentRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Iterable<DataSnapshot> children=dataSnapshot.getChildren();
                Iterator<DataSnapshot> it=children.iterator();
                while (it.hasNext()){
                    DataSnapshot data=it.next();
                    Comment c= data.getValue(Comment.class);
                    c.setSongId(musicId);
                    Log.d("User",dataSnapshot.getKey());
                    c.setUserId(dataSnapshot.getKey());
                    comments.add(c);
                    newComment();
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                comments.clear();
                Iterable<DataSnapshot> children=dataSnapshot.getChildren();
                Iterator<DataSnapshot> it=children.iterator();
                while (it.hasNext()){
                    DataSnapshot data=it.next();
                    Comment c= data.getValue(Comment.class);
                    c.setSongId(musicId);
                    Log.d("User",dataSnapshot.getKey());
                    c.setUserId(dataSnapshot.getKey());
                    comments.add(c);
                    newComment();
                }
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

    private void newComment() {
        listComments.clear();
        for (int i=0;i<comments.size();i++){
            listComments.add(comments.get(i).getComment());
        }
        if (comments.size()==0){
            nothingLL.setVisibility(View.VISIBLE);
            lvComments.setVisibility(View.GONE);
        }
        else {
            nothingLL.setVisibility(View.GONE);
            lvComments.setVisibility(View.VISIBLE);
        }

    }

    private void init() {
        getters();
        setListeners();
    }

    private void setListeners() {
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CommentRef=database.getReference().child("UserComments");
                CommentRef = CommentRef.getRef().child(musicId);
                if (userId != null) {
                    CommentRef = CommentRef.getRef().child(userId);
                    Comment c=new Comment(etComment.getText().toString());
                    CommentRef.push().setValue(c);
                }
                etComment.setText("");
            }
        });
    }

    public void getters() {
        etComment= (EditText) findViewById(R.id.etComment);
        btnSend= (Button) findViewById(R.id.btnSend);
        lvComments= (ListView) findViewById(R.id.lvComments);
        nothingLL=findViewById(R.id.ll_nothing);
        loaderComment=findViewById(R.id.loaderComment);
    }
}
