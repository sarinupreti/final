package com.example.sarin.guff;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.BreakIterator;
import java.text.CharacterIterator;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import id.zelory.compressor.Compressor;

public class ChatActivity extends AppCompatActivity {


    private Toolbar toolbar;
    private String mChatUser, chatWithUserName;
    private DatabaseReference databaseReference;
    private TextView chatUserName, chatOnlineStatus;
    private CircleImageView chatProfilePic;
    private DatabaseReference onlineDatabase, mRootRef, mRootRefUser;
    private FirebaseUser current;
    private StorageReference mImageStorage;
    private StorageReference mFileStorage;

    private ImageButton mChatAddBtn, mChatLocationBtn, mChatFileBtn, mChatSendBtn ;
    private String mCurrentUserId;
    private RecyclerView recyclerViewChat;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;
    private ProgressBar progressBar;

    private final static  int FILE_SELECT_CODE=1101;
    private static final int GALLERY_PIC = 2;
    private static final int PLACE_PICKER_REQUEST = 123;


    EmojiconEditText emojiconEditText;
    ImageView emojiImageView;
    View rootView;
    EmojIconActions emojIcon;
    private BreakIterator myTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        current = FirebaseAuth.getInstance().getCurrentUser();
        onlineDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(current.getUid()).child("Online");
        onlineDatabase.keepSynced(true);


        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.keepSynced(true);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mRootRefUser = FirebaseDatabase.getInstance().getReference("users");
        mImageStorage = FirebaseStorage.getInstance().getReference();
        mFileStorage = FirebaseStorage.getInstance().getReference();

        progressBar = findViewById(R.id.progressBarId);
        mChatUser = getIntent().getStringExtra("key");
        chatWithUserName = getIntent().getStringExtra("user_name");
        toolbar = findViewById(R.id.toolbarId_chat);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(null);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar, null);
        actionBar.setCustomView(action_bar_view);


        chatUserName = findViewById(R.id.chat_user_name);
        chatProfilePic = findViewById(R.id.chat_profile_pic);
        chatOnlineStatus = findViewById(R.id.chat_online_status);

        chatUserName.setText(chatWithUserName);

        mChatAddBtn = findViewById(R.id.chat_add_btn);
        mChatSendBtn = findViewById(R.id.chat_send_btn);
        mChatFileBtn = findViewById(R.id.chat_file_btn);
        mChatLocationBtn = findViewById(R.id.chat_location_btn);





        mCurrentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        recyclerViewChat = findViewById(R.id.chat_recyclerView);
        mLinearLayout = new LinearLayoutManager(this);
        mAdapter = new MessageAdapter(messagesList);
        recyclerViewChat.setHasFixedSize(true);
        recyclerViewChat.setLayoutManager(mLinearLayout);
        recyclerViewChat.setAdapter(mAdapter);



        rootView = findViewById(R.id.root_view);
        emojiImageView = findViewById(R.id.emoji_btn);
        emojiconEditText = findViewById(R.id.emojicon_edit_text);
        emojIcon = new EmojIconActions(this, rootView, emojiconEditText, emojiImageView);
        emojIcon.ShowEmojIcon();
        emojIcon.setIconsIds(R.drawable.ic_action_keyboard, R.drawable.smiley);

        loadMessages();

        // for checking deleted sms

        DatabaseReference current = FirebaseDatabase.getInstance().getReference().child("messages").child(mCurrentUserId).child(mChatUser);
        current.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (final DataSnapshot data : dataSnapshot.getChildren()) {
                    final int position = Integer.parseInt(data.child("position").getValue().toString());
                    if (data.child("isDelete").getValue().toString().equals("true") && position != -1 ) {

                        String current_user_ref = "messages/" + mCurrentUserId + "/" + mChatUser + "/" + data.getKey().toString();
                        Map delete = new HashMap();
                        delete.put(current_user_ref, null);

                        mRootRef.updateChildren(delete, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                messagesList.remove(position);
                                mAdapter.notifyItemRemoved(position);

                            }
                        });

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //status bar for chat

        databaseReference.child("users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String online = dataSnapshot.child("Online").getValue().toString();
                final String image = dataSnapshot.child("Thumb_image").getValue().toString();

                if(online.equals("true")) {
                    chatOnlineStatus.setText("Online");
                }
                else {
                    GetTimeAgo timeAgo = new GetTimeAgo();
                    long time = Long.parseLong(online);
                    String lastSeenTime = GetTimeAgo.getTimeAgo(time, getApplicationContext());
                    chatOnlineStatus.setText(lastSeenTime);
                }

                chatProfilePic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getApplication(), FullScreenImageView.class);
                        intent.setType(image);
                        startActivity(intent);
                    }
                });

                Picasso.with(ChatActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.avatar_default).into(chatProfilePic, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Picasso.with(ChatActivity.this).load(image).placeholder(R.drawable.avatar_default).into(chatProfilePic);
                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mRootRef.child("Chat").child(mCurrentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser).child("seen").setValue(true);



                if(dataSnapshot.hasChild(mChatUser)) {

                    mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser).child("seen").setValue(true);

                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen", false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + mCurrentUserId + "/" + mChatUser, chatAddMap);
                    chatUserMap.put("Chat/" + mChatUser + "/" + mCurrentUserId, chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                Log.d("CHAT_LOG", databaseError.getMessage().toString());
                            }
                        }
                    });
                    chatAddMap = new HashMap();
                    chatAddMap.put("seen", true);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatAddMap1 = new HashMap();
                    chatAddMap1.put("seen", false);
                    chatAddMap1.put("timestamp", ServerValue.TIMESTAMP);

                    chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + mCurrentUserId + "/" + mChatUser, chatAddMap);
                    chatUserMap.put("Chat/" + mChatUser + "/" + mCurrentUserId, chatAddMap1);


                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError != null) {

                                Log.d("CHAT_LOG", databaseError.getMessage().toString());

                            }

                        }
                    });

                }

            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mChatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

        mChatAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(ChatActivity.this);
            }
        });


        mChatFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileSelector();
            }
        });
        mChatLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLocationSelector();
            }
        });
    }


    private void openLocationSelector(){
        try {
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    private void openFileSelector() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try{

            startActivityForResult( Intent.createChooser(intent, "SELECT A FILE TO UPLOAD"),
                    FILE_SELECT_CODE);

        }catch (android.content.ActivityNotFoundException ex)
        {
            Toast.makeText(this, "NO FILE MANAGER FOUND", Toast.LENGTH_SHORT).show();
        }

    }



    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final String message_id, message, fromUser, smsType;
        int position = -1;
        try {
            position = mAdapter.getPosition();
            message_id = mAdapter.getSms_id();
            message = mAdapter.getSms();
            fromUser = mAdapter.getFromUser();
            smsType = mAdapter.getSmsType();
        } catch (Exception e) {
            return super.onContextItemSelected(item);
        }

        if(item.getItemId() == 0) {
            if(!smsType.equals("image")) {
                if (position != -1) {
                    ClipboardManager clipboard = (ClipboardManager) ChatActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                    String text = message;
                    ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(ChatActivity.this, "Copy text", Toast.LENGTH_SHORT).show();
                }
            }
            else {
                Toast.makeText(ChatActivity.this, "You can not copy image", Toast.LENGTH_SHORT).show();
            }
        }

        else if(item.getItemId() == 1) {
            if(smsType.equals("image")) {
                Intent intent = new Intent(this, FullScreenImageView.class);
                intent.setType(message);
                startActivity(intent);
            }
        }
        else if(item.getItemId() == 2) {
            if(fromUser.equals(mCurrentUserId)){
                deleteMessage(message, message_id, position);

            } else {
                Toast.makeText(ChatActivity.this, "Cannot delete others message", Toast.LENGTH_SHORT).show();

            }
        }else if (item.getItemId() ==3 ){
            if(!smsType.equals("text")) {
                Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(message));
                startActivity(intent);
            }else {
                Toast.makeText(this, "You cannot open a text . Its Encrypted", Toast.LENGTH_SHORT).show();
            }

        }
        return super.onContextItemSelected(item);
    }

    public void deleteMessageFromOneSide (String sms_id, final int position) {
        //    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child(mCurrentUserId).child(mChatUser);
        String current_user_ref = "messages/" + mCurrentUserId + "/" + mChatUser + "/" + sms_id;
        Map delete = new HashMap();
        delete.put(current_user_ref, null);
        mRootRef.updateChildren(delete, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                Toast.makeText(ChatActivity.this, String.valueOf(position) + " Delete text", Toast.LENGTH_SHORT).show();
                messagesList.remove(position);
                mAdapter.notifyItemRemoved(position);

            }
        });
    }




    public void deleteMessage(String sms, String sms_id, final int position) {
        String current_user_ref = "messages/" + mCurrentUserId + "/" + mChatUser + "/" + sms_id;
        DatabaseReference current = FirebaseDatabase.getInstance().getReference().child("messages").child(mChatUser).child(mCurrentUserId).child(sms_id);
        current.child("isDelete").setValue("true");
        current.child("position").setValue(position);
        Map delete = new HashMap();
        delete.put(current_user_ref, null);

        mRootRef.updateChildren(delete, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                messagesList.remove(position);
                mAdapter.notifyItemRemoved(position);
                Toast.makeText(ChatActivity.this, " Sms has been deleted from both side", Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PLACE_PICKER_REQUEST){
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                if (place != null){
                    LatLng latLng = place.getLatLng();
                    MapModel mapModel = new MapModel(latLng.latitude + "", latLng.longitude + "");
//                    databaseReference.push().setValue(mapModel);
                    Toast.makeText(this, (CharSequence) mapModel, Toast.LENGTH_LONG).show();
                    }


                }
            }



        if (requestCode ==  FILE_SELECT_CODE && resultCode == RESULT_OK){

            Uri fileUri = data.getData();

            String uriFileString = fileUri.toString();
            File myFile = new File(uriFileString);


            String displayFileName = null;
            if (uriFileString.startsWith("content://")){
                Cursor cursor = null;
                try{
                    cursor = ChatActivity.this.getContentResolver().query(fileUri, null,null, null,null);
                    if (cursor != null && cursor.moveToFirst()) {
                        displayFileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));


                    }
                }finally {
                    cursor.close();
                }

            }
            else if (uriFileString.startsWith("file://")){
                displayFileName= myFile.getName();

            }


            Toast.makeText(this, "THIS  WORKS ", Toast.LENGTH_SHORT).show();
            StorageReference riversRef = mFileStorage.child("files/" + displayFileName);

            riversRef.putFile(fileUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Get a URL to the uploaded content
                            Uri downloadUrl = taskSnapshot.getDownloadUrl();
                            Toast.makeText(ChatActivity.this, "Uploaded ", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Toast.makeText(ChatActivity.this, "failed uploading file " , Toast.LENGTH_SHORT).show();
                            // ...
                        }
                    });


            ////////////////////////////sending file  ////////////////////////////////////////////////////

            final String current_user_ref = "messages/" + mCurrentUserId + "/" + mChatUser;
            final String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUserId;

            DatabaseReference user_message_push = mRootRef.child("messages")
                    .child(mCurrentUserId).child(mChatUser).push();

            final String push_id = user_message_push.getKey();


            StorageReference filepath = mImageStorage.child("message_files").child( push_id + displayFileName);
            filepath.putFile(fileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if (task.isSuccessful()) {
                        final String download_url = task.getResult().getDownloadUrl().toString();




                         if(!TextUtils.isEmpty(download_url)){

                            String date = DateFormat.getDateTimeInstance().format(new Date());
                            final String time = DateFormat.getTimeInstance().format(new Date());

                            String current_user_ref = "messages/" + mCurrentUserId + "/" + mChatUser;
                            String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUserId;

                            DatabaseReference user_message_push = mRootRef.child("messages")
                                    .child(mCurrentUserId).child(mChatUser).push();

                            String push_id = user_message_push.getKey();

                            final Map messageMap = new HashMap();
                            messageMap.put("message", download_url);
                            messageMap.put("seen", false);
                            messageMap.put("type", "file");
                            messageMap.put("time", date);
                            messageMap.put("time_only", time);
                            messageMap.put("from", mCurrentUserId);
                            messageMap.put("sms_id", push_id);
                            messageMap.put("isDelete", "false");
                            messageMap.put("position", -1);

                            Map messageUserMap = new HashMap();
                            messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
                            messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

                            emojiconEditText.setText("");
                            mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser).child("seen").setValue(true);
                            mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser).child("timestamp").setValue(ServerValue.TIMESTAMP);

                            mRootRef.child("Chat").child(mChatUser).child(mCurrentUserId).child("seen").setValue(false);
                            mRootRef.child("Chat").child(mChatUser).child(mCurrentUserId).child("timestamp").setValue(ServerValue.TIMESTAMP);

                            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {


                                }
                            });

                        }


                    }
                }
            });

       }


        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {


                progressBar.setVisibility(View.VISIBLE);

                Uri imageUri = result.getUri();

                final String current_user_ref = "messages/" + mCurrentUserId + "/" + mChatUser;
                final String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUserId;

                DatabaseReference user_message_push = mRootRef.child("messages")
                        .child(mCurrentUserId).child(mChatUser).push();

                final String push_id = user_message_push.getKey();

                File thumb_filePath = new File(imageUri.getPath());
                Bitmap thumb_bitmap = new Compressor(this)
                        .setMaxWidth(1000)
                        .setMaxHeight(1000)
                        .setQuality(1000)
                        .compressToBitmap(thumb_filePath);

                //for uploading thumb image to database

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumb_byte = baos.toByteArray();


                StorageReference filepath = mImageStorage.child("message_images").child(push_id + ".jpg");
                final StorageReference thumb_store = mImageStorage.child("message_images").child("Thumb").child(push_id + ".jpg");

                filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()) {

                            UploadTask uploadTask = thumb_store.putBytes(thumb_byte);

                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                                    String download_url = thumb_task.getResult().getDownloadUrl().toString();

                                    if (thumb_task.isSuccessful()) {

                                        String date = DateFormat.getDateTimeInstance().format(new Date());

                                        Map messageMap = new HashMap();
                                        messageMap.put("message", download_url);
                                        messageMap.put("seen", false);
                                        messageMap.put("type", "image");
                                        messageMap.put("time", date);
                                        messageMap.put("from", mCurrentUserId);
                                        messageMap.put("sms_id", push_id);
                                        messageMap.put("isDelete", "false");
                                        messageMap.put("position", -1);

                                        Map messageUserMap = new HashMap();
                                        messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
                                        messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

                                        emojiconEditText.setText("");


                                        mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser).child("seen").setValue(true);
                                        mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser).child("timestamp").setValue(ServerValue.TIMESTAMP);

                                        mRootRef.child("Chat").child(mChatUser).child(mCurrentUserId).child("seen").setValue(false);
                                        mRootRef.child("Chat").child(mChatUser).child(mCurrentUserId).child("timestamp").setValue(ServerValue.TIMESTAMP);

                                        mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                                if (databaseError != null) {

                                                    //    mProgressBar.dismiss();
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_LONG).show();
                                                    Log.d("CHAT_LOG", databaseError.getMessage().toString());

                                                } else {
                                                    //    mProgressBar.dismiss();
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    Toast.makeText(ChatActivity.this, "Image is send", Toast.LENGTH_LONG).show();
                                                }

                                            }
                                        });

                                    } else{

                                    }
                                }
                            });
                        }
                        else{

                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_LONG).show();
                        }
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void loadMessages() {
        mRootRef.child("messages").child(mCurrentUserId).child(mChatUser).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {


                Messages message = dataSnapshot.getValue(Messages.class);
                messagesList.add(message);
                mAdapter.notifyDataSetChanged();

                recyclerViewChat.scrollToPosition(messagesList.size() - 1);
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


    private boolean containsURL(String content){
        String REGEX = "\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
        Pattern p = Pattern.compile(REGEX,Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(content);
        if(m.find()) {
            return true;
        }

        return false;
    }

    private Spannable stripLinks(String content) {
        Spannable s = new SpannableString(content);
        URLSpan[] spans = s.getSpans(0, s.length(), URLSpan.class);
        for (URLSpan span : spans) {
            s.removeSpan(span);
        }

        return s;
    }

    private void sendMessage() {


        String message = emojiconEditText.getText().toString();

        if(!TextUtils.isEmpty(message)){

            String date = DateFormat.getDateTimeInstance().format(new Date());
            String time = DateFormat.getTimeInstance().format(new Date());

            String current_user_ref = "messages/" + mCurrentUserId + "/" + mChatUser;
            String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUserId;

            DatabaseReference user_message_push = mRootRef.child("messages")
                    .child(mCurrentUserId).child(mChatUser).push();

            String push_id = user_message_push.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("time", date);
            messageMap.put("time_only", time);
            messageMap.put("from", mCurrentUserId);
            messageMap.put("sms_id", push_id);
            messageMap.put("isDelete", "false");
            messageMap.put("position", -1);

            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
            messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);
            emojiconEditText.setText("");
            mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser).child("seen").setValue(true);
            mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser).child("timestamp").setValue(ServerValue.TIMESTAMP);

            mRootRef.child("Chat").child(mChatUser).child(mCurrentUserId).child("seen").setValue(false);
            mRootRef.child("Chat").child(mChatUser).child(mCurrentUserId).child("timestamp").setValue(ServerValue.TIMESTAMP);

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                    if(databaseError != null){
                        Log.d("CHAT_LOG", databaseError.getMessage().toString());
                    }

                }
            });

        }

    }





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        super.onOptionsItemSelected(item);

        return true;
    }

    // online -------------------------

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