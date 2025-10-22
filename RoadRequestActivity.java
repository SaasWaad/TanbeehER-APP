package com.example.tanbeeherapp;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RoadRequestActivity extends AppCompatActivity {

    private TextView tvUserName;
    private DatabaseReference mDatabase;
    private FirebaseAuth auth;
    private ValueEventListener vitalsListener;
    private Handler handler;
    private Runnable navigationRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_road_request);

        // Enable the back arrow in the toolbar (acts as a Return button)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Return");
        }

        // Initialize views
        tvUserName = findViewById(R.id.tvUserName);
        Button btnCancelRequest = findViewById(R.id.btnCancelRequest);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Get current user and set name
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();
            String username = email != null ? email.split("@")[0] : "User";
            tvUserName.setText(getString(R.string.user_name_placeholder).replace("Reema Alhazmi", username));
            monitorVitals(username);
        } else {
            // If user is not logged in, redirect to LoginActivity
            startActivity(new Intent(RoadRequestActivity.this, LoginActivity.class));
            finish();
            return;
        }

        // Set up delayed navigation to the URL after 30 seconds
        handler = new Handler(Looper.getMainLooper());
        navigationRunnable = () -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://tanbeehermap.infinityfreeapp.com/"));
            startActivity(browserIntent);
            finish();
        };
        handler.postDelayed(navigationRunnable, 10_00); // 30 seconds delay

        // Handle Cancel Road Request button
        btnCancelRequest.setOnClickListener(v -> {
            // Remove the Firebase listener to stop monitoring
            if (vitalsListener != null) {
                mDatabase.child("Users").child(currentUser.getEmail().split("@")[0]).child("vitals")
                        .removeEventListener(vitalsListener);
            }
            // Cancel the delayed navigation
            handler.removeCallbacks(navigationRunnable);
            // Navigate back to MainActivity
            startActivity(new Intent(RoadRequestActivity.this, MainActivity.class));
            finish();
        });
    }

    private void monitorVitals(String username) {
        DatabaseReference vitalsRef = mDatabase.child("Users").child(username).child("vitals");
        vitalsListener = vitalsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Integer heartRate = dataSnapshot.child("heartRate").getValue(Integer.class);
                String motion = dataSnapshot.child("motion").getValue(String.class);

                // Define alert conditions (adjust as needed)
                boolean isHeartRateAbnormal = heartRate != null && heartRate > 100;
                boolean isMotionAbnormal = motion != null && motion.contains("steps/min") &&
                        Integer.parseInt(motion.replaceAll("[^0-9]", "")) < 10;

                if (isHeartRateAbnormal || isMotionAbnormal) {
                    // Remove the listener to prevent multiple alerts
                    vitalsRef.removeEventListener(this);
                    // Cancel the delayed navigation
                    handler.removeCallbacks(navigationRunnable);

                    // Navigate to AlertActivity
                    Intent intent = new Intent(RoadRequestActivity.this, AlertActivity.class);
                    if (isHeartRateAbnormal) {
                        intent.putExtra("alertMessage", "High heart rate detected: " + heartRate + " bpm");
                    } else {
                        intent.putExtra("alertMessage", "Low motion detected: " + motion);
                    }
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(RoadRequestActivity.this,
                        getString(R.string.error_fetching_data, databaseError.getMessage()),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Handle back arrow press in the toolbar (Return button)
        if (vitalsListener != null) {
            FirebaseUser currentUser = auth.getCurrentUser();
            if (currentUser != null) {
                mDatabase.child("Users").child(currentUser.getEmail().split("@")[0]).child("vitals")
                        .removeEventListener(vitalsListener);
            }
        }
        // Cancel the delayed navigation
        handler.removeCallbacks(navigationRunnable);
        startActivity(new Intent(RoadRequestActivity.this, MainActivity.class));
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up the listener and delayed navigation when the activity is destroyed
        if (vitalsListener != null) {
            FirebaseUser currentUser = auth.getCurrentUser();
            if (currentUser != null) {
                mDatabase.child("Users").child(currentUser.getEmail().split("@")[0]).child("vitals")
                        .removeEventListener(vitalsListener);
            }
        }
        handler.removeCallbacks(navigationRunnable);
    }
}