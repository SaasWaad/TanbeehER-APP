package com.example.tanbeeherapp;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AlertActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);

        // Enable the back arrow in the toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        TextView tvAlertMessage = findViewById(R.id.tvAlertMessage);
        Button btnBack = findViewById(R.id.btnBack);

        // Get the alert message from the intent
        String alertMessage = getIntent().getStringExtra("alertMessage");
        if (alertMessage != null) {
            tvAlertMessage.setText(alertMessage);
        } else {
            tvAlertMessage.setText("An alert has been triggered!");
        }

        // Navigate back to MainActivity
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(AlertActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Handle back arrow press in the toolbar
        Intent intent = new Intent(AlertActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
        return true;
    }
}