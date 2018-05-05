package com.example.sarin.guff;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class SearchFriendsActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private DatabaseReference databaseReference;

    private DatabaseReference onlineDatabase;
    private FirebaseUser current;
    private RecyclerView mResultList;

    private DatabaseReference userDatabase;
    private String seachedUsername = "name";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);


        databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        databaseReference.keepSynced(true);

        recyclerView = findViewById(R.id.all_Users_RecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        current = FirebaseAuth.getInstance().getCurrentUser();
        onlineDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(current.getUid()).child("Online");
        onlineDatabase.keepSynced(true);

        // search

        userDatabase = FirebaseDatabase.getInstance().getReference("users");
        userDatabase.keepSynced(true);

        mResultList = findViewById(R.id.all_Users_RecyclerView);
        mResultList.setHasFixedSize(true);
        mResultList.setLayoutManager(new LinearLayoutManager(this));

        toolbar = findViewById(R.id.toolbar_allUsers);
        setSupportActionBar(toolbar);



        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back_inverted);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        getSupportActionBar().setTitle("Find Friends");
    }
    public static class ProfileUsersViewHolder extends RecyclerView.ViewHolder {

        View profileUsersView;

        public ProfileUsersViewHolder(View itemView) {
            super(itemView);

            profileUsersView = itemView;
        }

        public void setDetails(Context ctx, String userName, String userStatus, String userImage) {

            TextView user_name = profileUsersView.findViewById(R.id.display_name_allUsers);
            TextView user_status = profileUsersView.findViewById(R.id.status_allUsers);
            ImageView user_image = profileUsersView.findViewById(R.id.proPic_allUsers);


            user_name.setText(userName);
            user_status.setText(userStatus);

            if(userImage.equals("default")) {
                Glide.with(ctx).load(R.drawable.avatar_default).into(user_image);
            }
            else {
                Glide.with(ctx).load(userImage).into(user_image);
            }
        }

        public void setOnlineStatus(String pic) {
            ImageView image = profileUsersView.findViewById(R.id.onLine);

            if(pic.equals("true")) {
                image.setVisibility(View.VISIBLE);
            }
            else {
                image.setVisibility(View.INVISIBLE);
            }
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter <Users, UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(
                Users.class, R.layout.user_single_layout_alluser, UsersViewHolder.class, databaseReference) {
            @Override
            protected void populateViewHolder(final UsersViewHolder viewHolder, Users model, int position) {
                viewHolder.setDisplayName(model.getName());
                viewHolder.setUserStatus(model.getStatus());
                viewHolder.set_thumbImage(model.getThumb_image(), getApplicationContext());

                final String userKey = getRef(position).getKey();


                databaseReference.child(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild("Online")) {
                            String pic = dataSnapshot.child("Online").getValue().toString();
                            viewHolder.setOnlineStatus(pic);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent profileIntent = new Intent(SearchFriendsActivity.this, ChatActivity.class);
                        profileIntent.putExtra("key", userKey);
                        startActivity(profileIntent);
                    }
                });

            }
        };
        recyclerView.setAdapter(firebaseRecyclerAdapter);

    }
    public static class UsersViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public UsersViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }
        public void setDisplayName(String name){
            TextView mTextView = mView.findViewById(R.id.display_name_allUsers);
            mTextView.setText(name);
        }
        public void setUserStatus(String status){
            TextView mTextView = mView.findViewById(R.id.status_allUsers);
            mTextView.setText(status);
        }
        public void set_thumbImage(final String thumb, final Context cntx) {
            final CircleImageView circleImageView = mView.findViewById(R.id.proPic_allUsers);

            Picasso.with(cntx).load(thumb).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.avatar_default).into(circleImageView, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    Picasso.with(cntx).load(thumb).placeholder(R.drawable.avatar_default).into(circleImageView);
                }
            });
        }

        public void setOnlineStatus(String pic) {
            ImageView image = mView.findViewById(R.id.onLine);

            if(pic.equals("true")) {
                image.setVisibility(View.VISIBLE);
            }
            else {
                image.setVisibility(View.INVISIBLE);
            }

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);
        MenuItem menuItem = menu.findItem(R.id.search_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }


    @Override
    public boolean onQueryTextChange(String newText) {
        usernameFirebaseUserSearch(newText);
        return true;
    }


//    @Override
//    public boolean onOptionsItemSelected(MenuItem item){
//        super.onOptionsItemSelected(item);
//
//        int id = item.getItemId();
//
//        if(item.isChecked()) {
//            item.setChecked(false);
//        }
//        else {
//            item.setChecked(true);
//        }
//
//        if(id == R.id.search_by_name) {
//            seachedUsername = "name";
//        }
//        else if(id == R.id.search_by_id){
//            seachedUsername = "id";
//        }
//        return true;
//    }
//

    private void usernameFirebaseUserSearch(String search) {

        if(seachedUsername == "name") {
            Query firebaseSearchQuery = userDatabase.orderByChild("Name").startAt(search).endAt(search + "\uf8ff");
            FirebaseRecyclerAdapter<Users, ProfileUsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, ProfileUsersViewHolder>(

                    Users.class, R.layout.user_single_layout_alluser, ProfileUsersViewHolder.class,
                    firebaseSearchQuery

            ) {
                @Override
                protected void populateViewHolder(final ProfileUsersViewHolder viewHolder, Users model, int position) {


                    viewHolder.setDetails(getApplicationContext(), model.getName(), model.getStatus(), model.getThumb_image());

                    final String userKey = getRef(position).getKey();

                    databaseReference.child(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if (dataSnapshot.hasChild("Online")) {
                                String pic = dataSnapshot.child("Online").getValue().toString();
                                viewHolder.setOnlineStatus(pic);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    viewHolder.profileUsersView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent profileIntent = new Intent(SearchFriendsActivity.this, ProfileActivity.class);
                            profileIntent.putExtra("key", userKey);
                            startActivity(profileIntent);
                        }
                    });

                }
            };

            mResultList.setAdapter(firebaseRecyclerAdapter);
        }
        else if(seachedUsername == "id") {

            Query firebaseSearchQuery = userDatabase.orderByChild("Id").startAt(search).endAt(search + "\uf8ff");
            FirebaseRecyclerAdapter<Users, ProfileUsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, ProfileUsersViewHolder>(

                    Users.class, R.layout.user_single_layout_alluser, ProfileUsersViewHolder.class,
                    firebaseSearchQuery

            ) {
                @Override
                protected void populateViewHolder(final ProfileUsersViewHolder viewHolder, Users model, int position) {


                    viewHolder.setDetails(getApplicationContext(), model.getName(), model.getStatus(), model.getThumb_image());

                    final String userKey = getRef(position).getKey();

                    databaseReference.child(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild("Online")) {
                                String pic = dataSnapshot.child("Online").getValue().toString();
                                viewHolder.setOnlineStatus(pic);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    viewHolder.profileUsersView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent profileIntent = new Intent(SearchFriendsActivity.this, ProfileActivity.class);
                            profileIntent.putExtra("key", userKey);
                            startActivity(profileIntent);
                        }
                    });

                }
            };

            mResultList.setAdapter(firebaseRecyclerAdapter);
        }

    }



    @Override
    public void onResume() {
        super.onResume();
        onlineDatabase.setValue("true");
        onlineDatabase.keepSynced(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        onlineDatabase.setValue(ServerValue.TIMESTAMP);
        onlineDatabase.keepSynced(true);
    }
}