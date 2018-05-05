package com.example.sarin.guff;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatsFragment extends Fragment {
    private RecyclerView conversationList;
    private DatabaseReference conversationDatabase;
    private DatabaseReference mMessageDatabase;
    private DatabaseReference mUsersDatabase;
    private FirebaseAuth mAuth;
    private String mCurrent_user_id;
    private View mMainView;

    public ChatsFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMainView = inflater.inflate(R.layout.fragment_chats, container, false);

        conversationList = mMainView.findViewById(R.id.conv_list);
        mAuth = FirebaseAuth.getInstance();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        conversationDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurrent_user_id);
        conversationDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        mUsersDatabase.keepSynced(true);
        mMessageDatabase = FirebaseDatabase.getInstance().getReference().child("messages").child(mCurrent_user_id);
        mUsersDatabase.keepSynced(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        conversationList.setHasFixedSize(true);
        conversationList.setLayoutManager(linearLayoutManager);
        return mMainView;
    }

    public void chatFriendSearch(String searchText) {

        Query firebaseSearchQuery = conversationDatabase.orderByChild("name").startAt(searchText).endAt(searchText + "\uf8ff");

        FirebaseRecyclerAdapter<Conversation, conversationViewHolder> firebaseConvAdapter = new FirebaseRecyclerAdapter<Conversation, conversationViewHolder>(
                Conversation.class,
                R.layout.user_single_layout_alluser,
                conversationViewHolder.class,
                firebaseSearchQuery
        ) {
            @Override
            protected void populateViewHolder(final conversationViewHolder conversationViewHolder, final Conversation conversation, int i) {


                final String list_user_id = getRef(i).getKey();

                Query lastMessageQuery = mMessageDatabase.child(list_user_id).limitToLast(1);

                lastMessageQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        String data = dataSnapshot.child("message").getValue().toString();
                        conversationViewHolder.setMessage(data, conversation.isSeen());

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


                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String userName = dataSnapshot.child("Name").getValue().toString();
                        String thumb_image = dataSnapshot.child("Thumb_image").getValue().toString();

                        if(dataSnapshot.hasChild("Online")) {

                            String userOnline = dataSnapshot.child("Online").getValue().toString();
                            conversationViewHolder.setUserOnline(userOnline);

                        }
                        conversationViewHolder.setName(userName);
                        conversationViewHolder.setUserImage(thumb_image, getContext());
                        conversationViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                chatIntent.putExtra("key", list_user_id);
                                chatIntent.putExtra("user_name", userName);
                                startActivity(chatIntent);

                            }
                        });


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };

        conversationList.setAdapter(firebaseConvAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();

        Query conversationQuery = conversationDatabase.orderByChild("timestamp");

        FirebaseRecyclerAdapter<Conversation, conversationViewHolder> firebaseConvAdapter = new FirebaseRecyclerAdapter<Conversation, conversationViewHolder>(
                Conversation.class,
                R.layout.user_single_layout_alluser,
                conversationViewHolder.class,
                conversationQuery
        ) {
            @Override
            protected void populateViewHolder(final conversationViewHolder convViewHolder, final Conversation conv, int i) {


                final String list_user_id = getRef(i).getKey();

                Query lastMessageQuery = mMessageDatabase.child(list_user_id).limitToLast(1);

                lastMessageQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        String data = dataSnapshot.child("message").getValue().toString();
                        convViewHolder.setMessage(data, conv.isSeen());

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


                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String userName = dataSnapshot.child("Name").getValue().toString();
                        String userThumb = dataSnapshot.child("Thumb_image").getValue().toString();
//                        String time = dataSnapshot.child("time_only").getValue().toString();


                        if(dataSnapshot.hasChild("Online")) {

                            String userOnline = dataSnapshot.child("Online").getValue().toString();
                            convViewHolder.setUserOnline(userOnline);

                        }

                        convViewHolder.setName(userName);
                        convViewHolder.setUserImage(userThumb, getContext());
//                        convViewHolder.setTime(time);


                        convViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {


                                Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                chatIntent.putExtra("key", list_user_id);
                                chatIntent.putExtra("user_name", userName);
                                startActivity(chatIntent);

                            }
                        });


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };

        conversationList.setAdapter(firebaseConvAdapter);

    }

    public static class conversationViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public conversationViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }

        @SuppressLint("ResourceAsColor")
        public void setMessage(String message, boolean isSeen){

            TextView userStatusView = mView.findViewById(R.id.status_allUsers);
            userStatusView.setText(message);

            if(!isSeen){
                userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.BOLD_ITALIC);
                mView.setBackgroundColor(R.color.greyBackground);

            } else {

                userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.NORMAL);

            }

        }

        public void setName(String name){

            TextView userNameView = mView.findViewById(R.id.display_name_allUsers);
            userNameView.setText(name);

        }

        public void setTime(String time){

            TextView userNameView = mView.findViewById(R.id.display_name_allUsers);
            userNameView.setText(time);

        }

        public void setUserImage(final String thumb_image, final Context ctx){

            final CircleImageView userImageView = mView.findViewById(R.id.proPic_allUsers);

            Picasso.with(ctx).load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.avatar_default).into(userImageView, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.avatar_default).into(userImageView);
                }
            });

        }

        public void setUserOnline(String online_status) {

            ImageView userOnlineView = mView.findViewById(R.id.onLine);

            if(online_status.equals("true")){

                userOnlineView.setVisibility(View.VISIBLE);

            } else {

                userOnlineView.setVisibility(View.INVISIBLE);

            }

        }


    }

}