package com.example.sarin.guff;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class ForgetPasswordActivity extends AppCompatActivity {

    private Button button;
    private ImageButton backButton;
    private TextInputEditText email;
    private FirebaseAuth mFirebaseAuth;
    private Toolbar toolbar;
    private ProgressDialog mProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);
        button = findViewById(R.id.forgotPasswordButton);
        backButton = findViewById(R.id.forgetPasswordBackButton);
        email = findViewById(R.id.emailForgetPassword);
        mFirebaseAuth = FirebaseAuth.getInstance();

        mProgressBar = new ProgressDialog(this);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goback();

            }

       });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userEmail = email.getText().toString().trim();

                if(!userEmail.equals("")) {

                    mProgressBar.setMessage("Sending an email with password reset link");
                    mProgressBar.setCanceledOnTouchOutside(false);
                    mProgressBar.show();

                    mFirebaseAuth.sendPasswordResetEmail(userEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                mProgressBar.dismiss();
                                Toast.makeText(ForgetPasswordActivity.this, "Email has been send", Toast.LENGTH_LONG).show();

                                goback();


                            }
                            else {
                                String error = "";

                                try {
                                    throw task.getException();
                                } catch (FirebaseAuthInvalidUserException e) {
                                    error = "Invalid Email!";
                                } catch (Exception e) {
                                    error = "Default error!";
                                    e.printStackTrace();
                                }

                                mProgressBar.hide();
                                Toast.makeText(ForgetPasswordActivity.this, error, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
                else {
                    Toast.makeText(ForgetPasswordActivity.this, "Email is missing", Toast.LENGTH_LONG).show();
                }
            }


        });
    }

    private void goback() {

        Intent backButtonIntent = new Intent(ForgetPasswordActivity.this, LoginActivity.class);
        startActivity(backButtonIntent);
        finish();
    }


}
