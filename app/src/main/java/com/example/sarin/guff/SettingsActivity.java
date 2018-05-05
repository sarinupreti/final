package com.example.sarin.guff;


import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    private static final int MAX_LENGTH = 100;
    private FirebaseUser mFireBaseUser;
    private DatabaseReference mDatabaseReference;
    private TextView display;
    private TextView c_status, id;

    private ImageView proPic;

    private Button changeStatus, changePic;
    private static final int GALLERY_PIC = 1;
    private StorageReference mStorage;
    private ProgressDialog mProgressBar;

    private DatabaseReference onlineDatabase;
    private FirebaseUser current;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mFireBaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String uId = mFireBaseUser.getUid();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(uId);

        mStorage = FirebaseStorage.getInstance().getReference();

        mProgressBar = new ProgressDialog(this);

        display = findViewById(R.id.setting_displayName);
        c_status = findViewById(R.id.setting_status);
        proPic = findViewById(R.id.setting_image);
        changeStatus = findViewById(R.id.setting_changeStatusButton);
        changePic = findViewById(R.id.setting_changeImageButton);
        id = findViewById(R.id.setting_displayId);

        current = FirebaseAuth.getInstance().getCurrentUser();
        onlineDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(current.getUid()).child("Online");

        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("Name").getValue().toString();
                String image = dataSnapshot.child("Image").getValue().toString();
                String status = dataSnapshot.child("Status").getValue().toString();
                final String thumbImage = dataSnapshot.child("Thumb_image").getValue().toString();
                String v_id = dataSnapshot.child("Id").getValue().toString();

                display.setText(name);
                c_status.setText(status);
                id.setText(v_id);

                if(!thumbImage.equals("default")) {
                    Picasso.with(SettingsActivity.this).load(thumbImage).placeholder(R.drawable.avatar_default).into(proPic);
                    }

                proPic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getApplication(), FullScreenImageView.class);
                        intent.setType(thumbImage);
                        startActivity(intent);
                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        changeStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, ChangeStatus.class));
            }
        });

        changePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery = new Intent();
                gallery.setType("image/*");
                gallery.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(gallery, "SELECT IMAGE"), GALLERY_PIC);

            }
        });


    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);


        if(requestCode == GALLERY_PIC && resultCode == RESULT_OK){
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                mProgressBar.setTitle("Uploading Image");
                mProgressBar.setMessage("Please wait while we uploading the image and process...");
                mProgressBar.setCanceledOnTouchOutside(false);
                mProgressBar.show();

                Uri resultUri = result.getUri();
                String uId = mFireBaseUser.getUid();

                File thumb_filePath = new File(resultUri.getPath());
                Bitmap thumb_bitmap = new Compressor(this)
                        .setMaxWidth(1000)
                        .setMaxHeight(1000)
                        .setQuality(1000)
                        .compressToBitmap(thumb_filePath);

                //for uploading thumb image to database

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumb_byte = baos.toByteArray();

                StorageReference store = mStorage.child("Profile picture").child(uId + ".jpg");     // random() er bodole "uId" likhle valo hobe.
                final StorageReference thumb_store = mStorage.child("Profile picture").child("Thumb").child(uId + ".jpg");

                store.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){

                            final String download_url = task.getResult().getDownloadUrl().toString();

                            UploadTask uploadTask = thumb_store.putBytes(thumb_byte);

                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                                    String download_thumb_url = thumb_task.getResult().getDownloadUrl().toString();

                                    if(thumb_task.isSuccessful()) {

                                        Map updateHashMap = new HashMap();      //    Map <String, String> likhle hobe na.
                                        updateHashMap.put("Image", download_url);
                                        updateHashMap.put("Thumb_image", download_thumb_url);

                                        mDatabaseReference.updateChildren(updateHashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()) {
                                                    mProgressBar.dismiss();
                                                    Toast.makeText(SettingsActivity.this, "Uploaded", Toast.LENGTH_LONG).show();
                                                }
                                                else{
                                                    mProgressBar.dismiss();
                                                    Toast.makeText(SettingsActivity.this, "Error", Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                                    }
                                    else{

                                    }
                                }
                            });
                        }
                        else{
                            mProgressBar.dismiss();
                            Toast.makeText(SettingsActivity.this, "Error", Toast.LENGTH_LONG).show();
                        }
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        onlineDatabase.setValue("true");
    }

    @Override
    public void onPause() {
        super.onPause();
        onlineDatabase.setValue(ServerValue.TIMESTAMP);
    }

}