package com.example.tanbeeherapp;



import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

public class FellowVitalsActivity extends AppCompatActivity {
    private static final String TAG = "FellowVitalsActivity";
    private TextView tvTitle, tvHeartRateValue, tvAccelerometerValue;
    private Button btnUpdate;
    private DatabaseReference mDatabase;
    private FirebaseAuth auth;
    private String fellowUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fellow_vitals);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        // Check if user is logged in
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Get the fellow's user ID from the intent
        fellowUserId = getIntent().getStringExtra("fellowUserId");
        if (fellowUserId == null) {
            Toast.makeText(this, "Fellow user ID not provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Log.d(TAG, "Fellow user ID: " + fellowUserId);

        // Bind UI elements
        tvTitle = findViewById(R.id.tvTitle);
        tvHeartRateValue = findViewById(R.id.tvHeartRateValue);
        tvAccelerometerValue = findViewById(R.id.tvAccelerometerValue);
        btnUpdate = findViewById(R.id.btnUpdate);

        // Connect to Firebase Database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Fetch the fellow's name and set it in the title
        DatabaseReference fellowRef = mDatabase.child("Users").child(fellowUserId).child("name");
        fellowRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String fellowName = dataSnapshot.getValue(String.class);
                if (fellowName != null) {
                    tvTitle.setText(fellowName + "'s Vital Signs");
                    Log.d(TAG, "Fellow name fetched: " + fellowName);
                } else {
                    tvTitle.setText("Fellow's Vital Signs");
                    Log.d(TAG, "Fellow name not found");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(FellowVitalsActivity.this, "Error fetching fellow name: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error fetching fellow name: " + databaseError.getMessage());
            }
        });

        // Fetch initial vital signs
        fetchVitalSigns();

        // Update button to refresh the vital signs
        btnUpdate.setOnClickListener(v -> fetchVitalSigns());
    }

    private void fetchVitalSigns() {
        DatabaseReference vitalsRef = mDatabase.child("Users").child(fellowUserId).child("vitals");
        vitalsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.d(TAG, "Vitals data exists for fellow user ID: " + fellowUserId);
                    Integer heartRate = dataSnapshot.child("heartRate").getValue(Integer.class);
                    if (heartRate != null) {
                        tvHeartRateValue.setText(heartRate + " bpm");
                        Log.d(TAG, "Heart rate fetched: " + heartRate);
                    } else {
                        tvHeartRateValue.setText("No heart rate data");
                        Log.d(TAG, "Heart rate not found");
                    }

                    Float accelerometer = dataSnapshot.child("Accelerometer").getValue(Float.class);
                    if (accelerometer != null) {
                        tvAccelerometerValue.setText(accelerometer + " m/s²");
                        Log.d(TAG, "Accelerometer fetched: " + accelerometer);
                    } else {
                        tvAccelerometerValue.setText("No accelerometer data");
                        Log.d(TAG, "Accelerometer not found");
                    }
                } else {
                    tvHeartRateValue.setText("No heart rate data");
                    tvAccelerometerValue.setText("No accelerometer data");
                    Log.d(TAG, "Vitals data not found for fellow user ID: " + fellowUserId);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(FellowVitalsActivity.this, "Error fetching vitals: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error fetching vitals: " + databaseError.getMessage());
            }
        });
    }
}