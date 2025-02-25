package com.example.loginregister;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.homepage2.R;
import com.google.firebase.auth.FirebaseAuth;

public class Register extends AppCompatActivity {

    private EditText emailEditText;
    private AppCompatButton resetPasswordButton;
    private FirebaseAuth firebaseAuth;
    private TextView resendBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        emailEditText = findViewById(R.id.etEmail);
        resetPasswordButton = findViewById(R.id.verifyBtn);
        firebaseAuth = FirebaseAuth.getInstance();
        resendBtn = findViewById(R.id.resendBtn);

        resetPasswordButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            if (!email.isEmpty()) {
                resetPassword(email);
            } else {
                Toast.makeText(Register.this, "Please enter your email", Toast.LENGTH_SHORT).show();
            }
        });

        resendBtn.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            if (!email.isEmpty()) {
                resendPasswordResetEmail(email);
            } else {
                Toast.makeText(Register.this, "Please enter your email", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetPassword(String email) {
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(Register.this, "Reset password email sent,verify you email", Toast.LENGTH_SHORT).show();
                        Intent it = new Intent(Register.this, Login.class);
                        startActivity(it);
                        finish(); // Close the activity
                    } else {
                        Toast.makeText(Register.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void resendPasswordResetEmail(String email) {
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(Register.this, "Resend password email sent, verify your email", Toast.LENGTH_SHORT).show();
                        Intent it = new Intent(Register.this, Login.class);
                        startActivity(it);
                        finish();
                    } else {
                        Toast.makeText(Register.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
