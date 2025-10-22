package com.example.tanbeeherapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpFellowActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, etEmergencyContact;
    private FirebaseAuth auth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_fellow);

        // Initialize views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etEmergencyContact = findViewById(R.id.etEmergencyContact);
        Button btnSignUp = findViewById(R.id.btnSignUp);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Handle Sign Up button
        btnSignUp.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String emergencyContact = etEmergencyContact.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty() || emergencyContact.isEmpty()) {
                Toast.makeText(SignUpFellowActivity.this, R.string.empty_fields_error, Toast.LENGTH_SHORT).show();
                return;
            }

            // Create user with email and password
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Sign up successful, save user data
                            String username = email.split("@")[0];
                            DatabaseReference userRef = mDatabase.child("Users").child(username);
                            userRef.child("email").setValue(email);

                            // Link the emergency contact (Main User)
                            DatabaseReference emergencyContactRef = mDatabase.child("Users").child(emergencyContact);
                            emergencyContactRef.child("linkedFellow").setValue(username);

                            Toast.makeText(SignUpFellowActivity.this, "Sign Up Successful", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SignUpFellowActivity.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(SignUpFellowActivity.this,
                                    "Sign Up Failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}