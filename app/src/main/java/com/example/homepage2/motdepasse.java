package com.example.homepage2;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.homepage2.R;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class motdepasse extends AppCompatActivity {

    private static final String TAG = "motdepasseActivity";
    private EditText currentPasswordEditText, newPasswordEditText, confirmPasswordEditText;
    private Button saveButton;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_motdepasse);

        // Initialize views
        currentPasswordEditText = findViewById(R.id.currentPasswordEditText);
        newPasswordEditText = findViewById(R.id.newPasswordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        saveButton = findViewById(R.id.saveButton);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        saveButton.setOnClickListener(v -> changePassword());

        // Setup back button to SettingsActivity
        ImageView fleche = findViewById(R.id.sfleche);
        fleche.setOnClickListener(v -> {
            Intent intent = new Intent(motdepasse.this, SettingsActivity.class);
            startActivity(intent);
        });
    }

    private void changePassword() {
        String currentPassword = currentPasswordEditText.getText().toString().trim();
        String newPassword = newPasswordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(currentPassword) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "New password and confirm password do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUser != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), currentPassword);
            currentUser.reauthenticate(credential)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            currentUser.updatePassword(newPassword).addOnCompleteListener(updateTask -> {
                                if (updateTask.isSuccessful()) {
                                    Toast.makeText(motdepasse.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                                    Log.d(TAG, "Password updated successfully");
                                    Intent intent = new Intent(motdepasse.this, SettingsActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(motdepasse.this, "Failed to update password: " + updateTask.getException(), Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, "Failed to update password", updateTask.getException());
                                }
                            });
                        } else {
                            Toast.makeText(motdepasse.this, "Current password is incorrect: " + task.getException(), Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Current password is incorrect", task.getException());
                        }
                    });
        } else {
            Toast.makeText(this, "No authenticated user found", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "No authenticated user found");
        }
    }
}
