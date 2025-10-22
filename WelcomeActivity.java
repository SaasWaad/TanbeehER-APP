package com.example.tanbeeherapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Bind buttons
        findViewById(R.id.btnGoToLogin).setOnClickListener(v -> {
            startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
        });

        findViewById(R.id.btnGoToSignUp).setOnClickListener(v -> {
            startActivity(new Intent(WelcomeActivity.this, SignUpActivity.class));
        });

        findViewById(R.id.btnSignUpFellow).setOnClickListener(v -> {
            startActivity(new Intent(WelcomeActivity.this, SignUpFellowActivity.class));
        });
    }
}