package com.example.sarin.guff;


import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;


public class LoginActivity extends AppCompatActivity {


    private TextInputEditText email, pass;

    private TextView forgotpassword, signUp;

    private Button button;

    private FirebaseAuth mAuth;

    private FirebaseAuth.AuthStateListener mAuthListener;

    private ProgressDialog mprogress;

    private Toolbar toolbar;


    private ImageButton backButton;


    private DatabaseReference usersDatabaseReference;


    @SuppressLint("WrongViewCast")

    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);


        mAuth = FirebaseAuth.getInstance();

        email = findViewById(R.id.emailLogin);

        pass = findViewById(R.id.passwordLogin);

        button = findViewById(R.id.signInButton);

        forgotpassword = findViewById(R.id.forgotPasswordText);

        signUp = findViewById(R.id.signUpText);

        backButton = findViewById(R.id.backButtonLogin);


        usersDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users");


        mprogress = new ProgressDialog(this);


        backButton.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View v) {


                Intent backButtonIntent = new Intent(LoginActivity.this, StartActivity.class);

                startActivity(backButtonIntent);

                finish();


            }

        });


        forgotpassword.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View v) {


                Intent forgotPasswordIntent = new Intent(LoginActivity.this, ForgetPasswordActivity.class);

                startActivity(forgotPasswordIntent);

                finish();


            }

        });


        signUp.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View v) {

                Intent registerPageIntent = new Intent(LoginActivity.this, RegisterActivity.class);

                startActivity(registerPageIntent);

                finish();


            }

        });


        mAuthListener = new FirebaseAuth.AuthStateListener() {

            @Override

            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {


                if (firebaseAuth.getCurrentUser() != null) {

                    Intent mIntent = new Intent(LoginActivity.this, NavigationActivity.class);

                    mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    startActivity(mIntent);

                    finish();

                }


            }

        };


        button.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View v) {

                signInUser();

            }

        });

    }

    @Override

    public void onStart() {

        super.onStart();

        mAuth.addAuthStateListener(mAuthListener);

    }


    public void signInUser() {

        String emailId = email.getText().toString();

        String password = pass.getText().toString();

        if (!TextUtils.isEmpty(emailId) && !TextUtils.isEmpty(password)) {


            if (password.length() < 6) {

                Toast.makeText(LoginActivity.this, "Password Should be At least 6 characters", Toast.LENGTH_LONG).show();

            } else {

                mprogress.setMessage("Sign in...");

                mprogress.setCanceledOnTouchOutside(false);

                mprogress.show();

                mAuth.signInWithEmailAndPassword(emailId, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                    @Override

                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (!task.isSuccessful()) {


                            String onlineUserID = mAuth.getCurrentUser().getUid();
                            String device_token = FirebaseInstanceId.getInstance().getToken();
                            usersDatabaseReference.child(onlineUserID).child("device_token").setValue(device_token).addOnSuccessListener(new OnSuccessListener<Void>() {

                                @Override

                                public void onSuccess(Void aVoid) {


                                    mprogress.cancel();

                                    Toast.makeText(LoginActivity.this, "Email or Password is wrong", Toast.LENGTH_LONG).show();

                                }

                            }).addOnFailureListener(new OnFailureListener() {

                                @Override

                                public void onFailure(@NonNull Exception e) {


                                    Toast.makeText(LoginActivity.this, "EEERRRORR", Toast.LENGTH_SHORT).show();

                                }

                            });


                        } else {


                            String error = "";


                            try {


                                throw task.getException();


                            } catch (FirebaseAuthInvalidUserException e) {


                                error = "Invalid Email!";


                            } catch (FirebaseAuthInvalidCredentialsException e) {


                                error = "Invalid Password!";


                            } catch (Exception e) {


                                error = "Logged in Success";


                                e.printStackTrace();


                            }


                            mprogress.cancel();

                            Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();

                        }

                    }


                });

            }

        } else {


            Toast.makeText(LoginActivity.this, "Email or Password is missing", Toast.LENGTH_LONG).show();

        }

    }

}

