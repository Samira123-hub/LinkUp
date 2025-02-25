package com.example.homepage2;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.homepage2.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Information extends AppCompatActivity {

    private static final String TAG = "InformationActivity";
    private EditText firstNameEditText, lastNameEditText, birthDateEditText, emailEditText;
    private Button saveButton;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);

        // Initialize views
        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        birthDateEditText = findViewById(R.id.birthDateEditText);
        emailEditText = findViewById(R.id.emailEditText);
        saveButton = findViewById(R.id.saveButton);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Calendar and DatePicker setup
        ImageView calendarIcon = findViewById(R.id.calendarIcon);
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            String selectedDate = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year);
            birthDateEditText.setText(selectedDate);
        };

        View.OnClickListener showDatePicker = v -> new DatePickerDialog(
                Information.this,
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();

        birthDateEditText.setOnClickListener(showDatePicker);
        calendarIcon.setOnClickListener(showDatePicker);

        saveButton.setOnClickListener(v -> {
            Log.d(TAG, "Save button clicked");
            Toast.makeText(Information.this, "button clicked", Toast.LENGTH_SHORT).show();
            saveUserData();
        });

        // Setup back button to SettingsActivity
        ImageView fleche = findViewById(R.id.sfleche);
        fleche.setOnClickListener(v -> {
            Intent intent = new Intent(Information.this, SettingsActivity.class);
            startActivity(intent);
        });
    }

    private void saveUserData() {
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String birthDate = birthDateEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String userName = "";
        if (!firstName.isEmpty() && !lastName.isEmpty()) {
            userName = firstName + " " + lastName;
        }

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", userName);
        userMap.put("date of birth", birthDate);
        userMap.put("email", email);

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("Users").document(userId).set(userMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(Information.this, "User information updated successfully", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "User information updated successfully");
                                Intent intent = new Intent(Information.this, SettingsActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Exception exception = task.getException();
                                if (exception != null) {
                                    Log.e(TAG, "Failed to update user information: " + exception.getMessage(), exception);
                                } else {
                                    Log.e(TAG, "Failed to update user information: Unknown error");
                                }
                                Toast.makeText(Information.this, "Failed to update user information: " + exception, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            Log.e(TAG, "No authenticated user found");
            Toast.makeText(this, "No authenticated user found", Toast.LENGTH_SHORT).show();
        }
    }
}
