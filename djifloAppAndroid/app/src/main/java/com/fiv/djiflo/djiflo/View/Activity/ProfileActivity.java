package com.fiv.djiflo.djiflo.View.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fiv.djiflo.djiflo.DataLayer.User;
import com.fiv.djiflo.djiflo.R;
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
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private Button btnChoose, btnUpload;
    private ImageView imageView,imageViewNew;
    private Uri filePath;
    private EditText etEmail,etPhone,etName;
    private Button btnSave;
    private String email,phoneNumber,imageUrl=null,name;

    FirebaseStorage storage;
    StorageReference storageReference;

    private User u;
    private final int PICK_IMAGE_REQUEST = 71;
    private SharedPreferences spUser;
    private String userId;
    private FirebaseDatabase database=FirebaseDatabase.getInstance();;
    private DatabaseReference UserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getViews();
        setListeners();
        btnUpload.setEnabled(false);
        spUser=getSharedPreferences("User",MODE_PRIVATE);
        userId=spUser.getString("UserId",null);
        UserRef=database.getReference().child("Users").child(userId);
        UserRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                    u=dataSnapshot.getValue(User.class);
                    etEmail.setText(u.getEmailId());
                    etName.setText(u.getName());
                    etPhone.setText(u.getPhone());
                    Picasso.with(getApplicationContext()).load(u.getImageURL()).placeholder(R.drawable.ic_person_white_36dp).into(imageView);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
    }

    private void setListeners() {
        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email=etEmail.getText().toString();
                phoneNumber=etPhone.getText().toString();
                name=etName.getText().toString();
                u.setPhone(phoneNumber);
                u.setName(name);
                u.setEmailId(email);
                if (imageUrl!=null) {
                    u.setImageURL(imageUrl);
                }
                UserRef=database.getReference().child("Users").child(userId);
                UserRef.setValue(u);
                finish();
            }
        });
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
        btnUpload.setEnabled(true);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageViewNew.setImageBitmap(bitmap);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
    private void uploadImage() {

        if(filePath != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            final String imageName=UUID.randomUUID().toString();
            StorageReference ref = storageReference.child("UserImages/"+imageName);
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Uri downloadUri = taskSnapshot.getMetadata().getDownloadUrl();
                            imageUrl=(downloadUri.toString());
                            Toast.makeText(ProfileActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(ProfileActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });
            btnUpload.setEnabled(false);
        }
        else {
            Toast.makeText(getApplicationContext(),"You haven't selected any image",Toast.LENGTH_SHORT).show();
        }
    }
    private void getViews() {
        btnChoose = (Button) findViewById(R.id.btnChoose);
        btnUpload = (Button) findViewById(R.id.btnUpload);
        imageView = (ImageView) findViewById(R.id.imgView);
        imageViewNew = (ImageView) findViewById(R.id.imgViewNew);
        etEmail= (EditText) findViewById(R.id.et_user_email);
        btnSave= (Button) findViewById(R.id.tv_tab_save);
        etName= (EditText) findViewById(R.id.et_user_name);
        etPhone= (EditText) findViewById(R.id.et_user_phone);
    }
}
