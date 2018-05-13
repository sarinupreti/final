package com.example.sarin.guff;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.LinkedHashMap;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText username, email, password, passwordAgain, phoneNumberId;
    private Button signUpButton;
    private FirebaseAuth mAuth;
    private ProgressDialog mProgress;
    private ImageButton profilePicture;
    private DatabaseReference databaseReference;
    private Uri filePath;
    private final int PICK_IMAGE_REQUEST = 71;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private StorageReference mStorage;
    private FirebaseUser mFireBaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        username = findViewById(R.id.nameRegister);
        email = findViewById(R.id.emailRegister);
        password = findViewById(R.id.passwordRegister);
        passwordAgain = findViewById(R.id.passwordAgainRegister);
        phoneNumberId = findViewById(R.id.phonenumberRegister);
        signUpButton = findViewById(R.id.registerButton);
        mAuth = FirebaseAuth.getInstance();
        mProgress = new ProgressDialog(this);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = username.getText().toString();
                String emailId = email.getText().toString();
                String userPassword = password.getText().toString();
                String confirmPassowrd = passwordAgain.getText().toString();
                String userPhoneNumber = phoneNumberId.getText().toString();
                if (!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(emailId) && !TextUtils.isEmpty(userPassword) && !TextUtils.isEmpty(userPhoneNumber)) {

                    String name = userName;
                    Boolean check = false;
                    for (int i = 0; i < name.length(); i++) {
                        if (!(name.charAt(i) >= 'a' && name.charAt(i) <= 'z') && !(name.charAt(i) >= 'A' && name.charAt(i) <= 'Z') && !(name.charAt(i) == ' ')) {
                            check = true;
                            break;
                        }
                    }

                    String id = userPhoneNumber;
                    Boolean check1 = false;
                    if (id.length() == 10) {
                        for (int i = 0; i < id.length(); i++) {
                            if (i < 3) {
                                if (!(id.charAt(i) >= '0' && id.charAt(i) <= '9')) {
                                    check1 = true;
                                    break;
                                }
                            }

//                        }
                            else if (i < 11) {
                                if (!(id.charAt(i) >= '0' && id.charAt(i) <= '9')) {
                                    check1 = true;
                                    break;
                                }
                            }
                        }
                    } else {
                        check1 = true;
                    }

                    try {
                        String substring = emailId.substring(emailId.length() - 10, emailId.length());

                        if (!substring.equals("@gmail.com")) {
                            Toast.makeText(RegisterActivity.this, "Please user email provided by your college", Toast.LENGTH_LONG).show();
                        } else if (check) {
                            Toast.makeText(RegisterActivity.this, "User name is not valid. use only A-Z, a-z or space", Toast.LENGTH_LONG).show();
                        } else if (userPassword.length() < 6) {
                            Toast.makeText(RegisterActivity.this, "Password should be At least 6 characters long", Toast.LENGTH_LONG).show();
                        } else if (userPassword.compareTo(confirmPassowrd) != 0) {
                            Toast.makeText(RegisterActivity.this, "Password is not Matched", Toast.LENGTH_LONG).show();
                        } else if (check1) {
                            Toast.makeText(RegisterActivity.this, "userPhoneNumber Number is not valid.", Toast.LENGTH_LONG).show();
                        } else {
                            registerNewUsers(userName, emailId, userPassword, userPhoneNumber);
                        }
                    } catch (Exception ex) {
                        Toast.makeText(RegisterActivity.this, "Please use the email provided by college.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "You missed somewhere.Check again", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private void uploadImage() {
        String uId = mFireBaseUser.getUid();
        if (filePath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            StorageReference store = mStorage.child("Profile picture").child(uId + ".jpg");     // random() er bodole "uId" likhle valo hobe.
            final StorageReference thumb_store = mStorage.child("Profile picture").child("Thumb").child(uId + ".jpg");
            store.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(RegisterActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(RegisterActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded " + (int) progress + "%");
                        }
                    });
        }
    }

    public void registerNewUsers(final String userName, final String emailId, String userPassword, final String userPhoneNumber) {

        mProgress.setMessage("Registering...");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();
        mAuth.createUserWithEmailAndPassword(emailId, userPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    String device_token = FirebaseInstanceId.getInstance().getToken();

                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    String uid = currentUser.getUid();
                    databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(uid);

                    LinkedHashMap<String, String> user = new LinkedHashMap<>();
                    user.put("Name", userName);
                    user.put("Status", "Hi there, I'm using chat app");
                    user.put("Image", "default");
                    user.put("Email", emailId);
                    user.put("Thumb_image", "default");
                    user.put("Id", userPhoneNumber);
                    user.put("device_token", device_token);

                    databaseReference.setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                sendEmailVerfication();
                                Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_LONG).show();
                                Intent go = new Intent(RegisterActivity.this, LoginActivity.class);
                                go.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(go);
                                finish();

                                Toast.makeText(RegisterActivity.this, "Please verify your email. A email has been been send to you.", Toast.LENGTH_LONG).show();
                            }
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
                        error = "Default error!";
                        e.printStackTrace();
                    }

                    mProgress.cancel();
                    Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_LONG).show();

                }
            }
        });
    }

    private void sendEmailVerfication() {

        final FirebaseUser user = mAuth.getCurrentUser();
        if (user !=null){

            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if (task.isSuccessful()){
                        Toast.makeText(RegisterActivity.this, "Check your email for verfication", Toast.LENGTH_SHORT).show();

                        Intent intent = new  Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        FirebaseAuth.getInstance().signOut();
                    }
                }
            });
        }

      }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null)

        {
            Uri uri = data.getData();
            StorageReference store = mStorage.child("Profile picture").child(uri.getLastPathSegment());


            store.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Toast.makeText(RegisterActivity.this, "Upload done", Toast.LENGTH_SHORT).show();


                }
            });


        }


    }
}