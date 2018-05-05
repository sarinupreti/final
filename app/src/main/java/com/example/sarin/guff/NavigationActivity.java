package com.example.sarin.guff;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class NavigationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private static final int GALLERY_PIC =  1;
    private DatabaseReference onlineDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser current;

    private static final int MAX_LENGTH = 100;
    private TextView displayName;
    private TextView phoneNumber;
    private ImageView profilePicture;
    private StorageReference mStorage;
    private Toolbar toolbar;
    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionPagerAdapter;
    private TabLayout mTabLayout;
    private DatabaseReference mDatabaseReference;
    private String uId;
    private FirebaseUser mFireBaseUser;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent searchFriendIntent = new Intent(NavigationActivity.this, SearchFriendsActivity.class);
                startActivity(searchFriendIntent);

            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        navigationView.setNavigationItemSelectedListener(this);

        //////////////////////////////////////// tabs /////////////////////////////////////

        // Tabs
        mViewPager = findViewById(R.id.main_tabPager);
        mSectionPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionPagerAdapter);

        mTabLayout = findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.setSelectedTabIndicatorColor(Color.parseColor("#4f1a97"));
        mTabLayout.setSelectedTabIndicatorHeight((int) (5 * getResources().getDisplayMetrics().density));
        mTabLayout.setTabTextColors(Color.parseColor("#4f1a97"), Color.parseColor("#4f1a97"));

        ////////////////////////////////////profile////////////////////////////////////////////
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uId = user.getUid();

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(uId);
        profilePicture = headerView.findViewById(R.id.nav_profilePic);
        phoneNumber = headerView.findViewById(R.id.loginTitle);
        displayName = headerView.findViewById(R.id.nav_userName);


        current = FirebaseAuth.getInstance().getCurrentUser();
        onlineDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(current.getUid()).child("Online");

        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("Name").getValue().toString();
                String image = dataSnapshot.child("Image").getValue().toString();
                final String thumbImage = dataSnapshot.child("Thumb_image").getValue().toString();
                String v_id = dataSnapshot.child("Id").getValue().toString();

                displayName.setText(name);
                phoneNumber.setText(v_id);

                if(!thumbImage.equals("default")) {
                    Picasso.with(NavigationActivity.this).load(thumbImage).placeholder(R.drawable.avatar_default).into(profilePicture);
                }

                profilePicture.setOnClickListener(new View.OnClickListener() {
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





    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }



    private void sendToStart(){
        Intent startIntent = new Intent(NavigationActivity.this, StartActivity.class);
        startActivity(startIntent);
        finish();
    }





    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();


        if(item.getItemId() == R.id.nav_profile){
            startActivity(new Intent(NavigationActivity.this, SettingsActivity.class));
        }
        else if(item.getItemId() == R.id.nav_changePassword){
            startActivity(new Intent(NavigationActivity.this, ChangePasswordActivity.class));
        }
        else if(item.getItemId() == R.id.nav_delete){
            startActivity(new Intent(NavigationActivity.this, DeleteAccountActivity.class));
        }
        else if(item.getItemId() == R.id.nav_logout){



            if ((onlineDatabase != (null)))

            {
                onlineDatabase.setValue(ServerValue.TIMESTAMP);
                FirebaseAuth.getInstance().signOut();
                sendToStart();

            }else
            {
                DatabaseReference onlineDatabase  = FirebaseDatabase.getInstance().getReference().child("users").child(current.getUid()).child("Online");

            }


        }
        else if(item.getItemId() == R.id.nav_findFriends){
            startActivity(new Intent(NavigationActivity.this, AllUsersActivity.class));
        }



        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
