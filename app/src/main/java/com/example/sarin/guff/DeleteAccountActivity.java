package com.example.sarin.guff;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class DeleteAccountActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private Button deleteButton;
    private ProgressDialog progressDialog;
    private FirebaseUser firebaseUser;
    private RadioButton radio;
    private RadioGroup radioGroup;
    private DatabaseReference databaseReference;

    private DatabaseReference onlineDatabase, deleteDatabase;
    private FirebaseUser current;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_account);

        toolbar = findViewById(R.id.toolbarId_delete);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Delete Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        deleteButton = findViewById(R.id.delete_Button);
        radioGroup = findViewById(R.id.radio_group);
        progressDialog = new ProgressDialog(this);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        deleteDatabase = FirebaseDatabase.getInstance().getReference();



        current = FirebaseAuth.getInstance().getCurrentUser();
        onlineDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(current.getUid()).child("Online");
/*
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedItem = "";
                try {
                    int radioId = radioGroup.getCheckedRadioButtonId();
                    radio = findViewById(radioId);
                    selectedItem = radio.getText().toString();
                }
                catch (Exception e) {
                    Toast.makeText(DeleteAccountActivity.this, "you don't have selected any item", Toast.LENGTH_LONG).show();
                }
                if(selectedItem.equals("Yes")) {
                    progressDialog.setMessage("Deleting your account...");
                    progressDialog.show();



                    databaseReference.child(firebaseUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                firebaseUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()) {
                                            progressDialog.dismiss();
                                            Toast.makeText(DeleteAccountActivity.this, "Account has been deleted", Toast.LENGTH_LONG).show();
                                            finish();
                                            startActivity(new Intent(DeleteAccountActivity.this, StartActivity.class));
                                        }
                                        else {
                                            progressDialog.dismiss();
                                            Toast.makeText(DeleteAccountActivity.this, "something is wrong", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });

                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(DeleteAccountActivity.this, "something is wrong", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
                else if(selectedItem.equals("No")){
                    finish();
                }
            }
        });
*/
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

////delete chat
//       deleteDatabase.child("Chat").addValueEventListener(new ValueEventListener() {
//        @Override
//        public void onDataChange(DataSnapshot dataSnapshot) {
//            if(dataSnapshot.hasChild(current.getUid())) {
//
//                deleteDatabase.child("Chat").child(current.getUid()).addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//
//                        for (DataSnapshot data : dataSnapshot.getChildren()){
//                            final DataSnapshot Data = data;
//                            deleteDatabase.child("Chat").child(current.getUid()).child(data.getKey()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
//                                @Override
//                                public void onSuccess(Void aVoid) {
//                                    deleteDatabase.child("Chat").child(Data.getKey()).child(current.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
//                                        @Override
//                                        public void onSuccess(Void aVoid) {
//
//                                        }
//                                    });
//                                }
//                            });
//
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//
//                    }
//                });
//            }
//        }
//
//        @Override
//        public void onCancelled(DatabaseError databaseError) {
//
//        }
//    });
//

    // --------------*/
