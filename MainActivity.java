package com.example.tanbeeherapp;



import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private TextView tvUserName, tvUserAge, tvUserEmail, tvFellowName, tvFellowHeartRate, tvFellowAccelerometer, tvFellowFallDetected;
    private FirebaseAuth auth;
    private DatabaseReference mDatabase;
    private ValueEventListener fellowVitalsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Enable the back arrow in the toolbar (acts as a Return button)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Return");
        }

        // Initialize views
        tvUserName = findViewById(R.id.tvUserName);
        tvUserAge = findViewById(R.id.tvUserAge);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvFellowName = findViewById(R.id.tvFellowName);
        tvFellowHeartRate = findViewById(R.id.tvFellowHeartRate);
        tvFellowAccelerometer = findViewById(R.id.tvFellowAccelerometer);
        tvFellowFallDetected = findViewById(R.id.tvFellowFallDetected);
        Button btnAddFellow = findViewById(R.id.btnAddFellow);
        Button btnSubmitRoadRequest = findViewById(R.id.btnSubmitRoadRequest);
        Button btnLogout = findViewById(R.id.btnLogout);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Get current user and set profile data
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();
            String username = email != null ? email.split("@")[0] : "User";
            tvUserName.setText(username);
            tvUserEmail.setText(email);

            // Fetch user profile data (e.g., age) and linked Fellow
            DatabaseReference userRef = mDatabase.child("Users").child(username);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // Set user age
                    Long age = dataSnapshot.child("age").getValue(Long.class);
                    if (age != null) {
                        tvUserAge.setText("Age: " + age);
                    } else {
                        tvUserAge.setText("Age: Not Set");
                    }

                    // Get linked Fellow
                    String linkedFellow = dataSnapshot.child("linkedFellow").getValue(String.class);
                    if (linkedFellow != null) {
                        tvFellowName.setText("Fellow: " + linkedFellow);
                        fetchFellowVitals(linkedFellow);
                    } else {
                        tvFellowName.setText("Fellow: Not Linked");
                        tvFellowHeartRate.setText(R.string.no_heart_rate_data);
                        tvFellowAccelerometer.setText(R.string.no_accelerometer_data);
                        tvFellowFallDetected.setText("Fall Detected: Not Available");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(MainActivity.this,
                            "Error fetching user data: " + databaseError.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });

            // Handle Add Fellow button
            btnAddFellow.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Link a Fellow");

                // Set up the input
                final EditText input = new EditText(MainActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setHint("Enter Fellow's username");
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("Link", (dialog, which) -> {
                    String fellowUsername = input.getText().toString().trim();
                    if (!fellowUsername.isEmpty()) {
                        // Check if the Fellow exists in Firebase
                        mDatabase.child("Users").child(fellowUsername).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    // Fellow exists, link them
                                    userRef.child("linkedFellow").setValue(fellowUsername);
                                    tvFellowName.setText("Fellow: " + fellowUsername);
                                    fetchFellowVitals(fellowUsername);
                                    Toast.makeText(MainActivity.this, "Fellow linked successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MainActivity.this, "Fellow not found", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(MainActivity.this,
                                        "Error checking Fellow: " + databaseError.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(MainActivity.this, "Please enter a username", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

                builder.show();
            });

            // Handle Submit Road Request button
            btnSubmitRoadRequest.setOnClickListener(v -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://tanbeehermap.infinityfreeapp.com/"));
                startActivity(browserIntent);
            });

            // Handle Logout button
            btnLogout.setOnClickListener(v -> {
                auth.signOut();
                Toast.makeText(MainActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            });
        } else {
            // If user is not logged in, redirect to LoginActivity
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }
    }

    private void fetchFellowVitals(String fellowName) {
        DatabaseReference fellowVitalsRef = mDatabase.child("Users").child(fellowName).child("vitals");
        if (fellowVitalsListener != null) {
            fellowVitalsRef.removeEventListener(fellowVitalsListener);
        }
        fellowVitalsListener = fellowVitalsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Integer heartRate = dataSnapshot.child("heartRate").getValue(Integer.class);
                Float accelerometer = dataSnapshot.child("Accelerometer").getValue(Float.class);
                Boolean fallDetected = dataSnapshot.child("fallDetected").getValue(Boolean.class);

                // Update UI
                if (heartRate != null) {
                    tvFellowHeartRate.setText(getString(R.string.heart_rate_value, heartRate));
                } else {
                    tvFellowHeartRate.setText(R.string.no_heart_rate_data);
                }

                if (accelerometer != null) {
                    tvFellowAccelerometer.setText(getString(R.string.accelerometer_value, accelerometer + " m/s²"));
                } else {
                    tvFellowAccelerometer.setText(R.string.no_accelerometer_data);
                }

                if (fallDetected != null) {
                    tvFellowFallDetected.setText("Fall Detected: " + fallDetected);
                } else {
                    tvFellowFallDetected.setText("Fall Detected: Not Available");
                }

                // Trigger alerts for abnormal conditions
                boolean isHeartRateAbnormal = heartRate != null && (heartRate > 120 || heartRate < 50);
                boolean isAccelerometerAbnormal = accelerometer != null && accelerometer < 0.5; // Example threshold
                boolean isFallDetected = fallDetected != null && fallDetected;

                if (isHeartRateAbnormal || isAccelerometerAbnormal || isFallDetected) {
                    Intent intent = new Intent(MainActivity.this, AlertActivity.class);
                    if (isHeartRateAbnormal) {
                        intent.putExtra("alertMessage", "Abnormal heart rate detected for Fellow: " + fellowName + " (" + heartRate + " bpm)");
                    } else if (isAccelerometerAbnormal) {
                        intent.putExtra("alertMessage", "Low movement detected for Fellow: " + fellowName + " (" + accelerometer + " m/s²)");
                    } else {
                        intent.putExtra("alertMessage", "Fall detected for Fellow: " + fellowName);
                    }
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this,
                        getString(R.string.error_fetching_data, databaseError.getMessage()),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Handle back arrow press in the toolbar (Return button)
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up the listener when the activity is destroyed
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null && fellowVitalsListener != null) {
            String fellowName = tvFellowName.getText().toString().replace("Fellow: ", "");
            if (!fellowName.equals("Not Linked")) {
                mDatabase.child("Users").child(fellowName).child("vitals")
                        .removeEventListener(fellowVitalsListener);
            }
        }
    }
}