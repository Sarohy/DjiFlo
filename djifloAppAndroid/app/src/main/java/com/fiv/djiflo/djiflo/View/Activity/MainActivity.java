package com.fiv.djiflo.djiflo.View.Activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import com.fiv.djiflo.djiflo.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import com.google.firebase.database.ValueEventListener;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView =  (ImageView) findViewById(R.id.imgSplash);
        /****** Create Thread that will sleep for 5 seconds****/
        final Thread background = new Thread() {
            public void run() {
                try {
                    // Thread will sleep for 5 seconds
                    sleep(6*1000);

                    // After 5 seconds redirect to ano«ther intent
                    Intent i=new Intent(getBaseContext(),HomeActivity.class);
                    startActivity(i);

                    //Remove activity
                    finish();
                } catch (Exception e) {
                }
            }
        };
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference SplashRef = database.getReference().child("Splash");
        SplashRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Picasso.with(getApplicationContext()).load((String)dataSnapshot.getValue()).placeholder(R.mipmap.ic_launcher).into(imageView);
                background.start();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // start thread

    }
}
