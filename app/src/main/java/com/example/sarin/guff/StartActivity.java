package com.example.sarin.guff;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class StartActivity extends AppCompatActivity {

    private Button have, not;
    private TextView forgetPassword;

    LinearLayout linearLayout;
    AnimationDrawable animationDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        have = findViewById(R.id.have);
        not = findViewById(R.id.not);



        have.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent login = new Intent(StartActivity.this, LoginActivity.class);
                startActivity(login);
            }
        });

        not.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent reges = new Intent(StartActivity.this, RegisterActivity.class);
                startActivity(reges);
            }
        });

    }
}
