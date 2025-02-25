package com.example.loginregister;

import static android.widget.Toast.makeText;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.homepage2.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Activity_signup extends AppCompatActivity {

    private FirebaseAuth Auth;
    private EditText firstnameEt, familynameEt, emailEt, passwordEt, dateEt;
    private RadioGroup rgGender;
    private Button signUpBtn;
    public static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[a-z]{2,6}$";
    public static final String DOB_REGEX = "^\\d{2}/\\d{2}/\\d{4}$";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        firstnameEt = findViewById(R.id.firstnameEt);
        familynameEt = findViewById(R.id.surnameEt);
        emailEt = findViewById(R.id.emailEt);
        passwordEt = findViewById(R.id.passwordEt);
        dateEt = findViewById(R.id.dateEt);
        rgGender = findViewById(R.id.rgGender);
        signUpBtn = findViewById(R.id.signUpBtn);
        Auth = FirebaseAuth.getInstance();

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUpUser();
            }
        });

    }

    private void signUpUser() {
        String fullname = firstnameEt.getText().toString().trim()+" "+familynameEt.getText().toString().trim();
        String email = emailEt.getText().toString().trim();
        String password = passwordEt.getText().toString().trim();
        String dob = dateEt.getText().toString().trim();
        String gender = ((RadioButton) findViewById(rgGender.getCheckedRadioButtonId())).getHint().toString();

        if (fullname.isEmpty()) {
            firstnameEt.setError("Please input a valid name");
            return;
        }
        if (dob.isEmpty() || !dob.matches(DOB_REGEX)) {
            dateEt.setError("Please input a valid date of birth (dd/MM/yyyy)");
            return;
        }

        if (email.isEmpty()) {
            emailEt.setError("Please input a valid email");
            return;
        }
        if (password.isEmpty() || password.length() < 6) {
            passwordEt.setError("Password must be at least 6 characters");
            return;
        }

        // Create Firebase account and upload user data to Firestore
        createAccount(fullname, email, password,dob, gender);
    }

    private void createAccount(String fullname, String email, String password,String dob, String gender) {
        Auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = Auth.getCurrentUser();
                            if (user != null) {
                                user.sendEmailVerification()
                                        .addOnCompleteListener(emailTask -> {
                                            if (emailTask.isSuccessful()) {
                                                Log.d("EmailVerification", "Verification email sent.");
                                            } else {
                                                Log.e("EmailVerification", "Error sending verification email: " + emailTask.getException());
                                            }
                                        });
                                // Upload user data to Firestore
                                uploadUser(user, fullname, email,password, dob, gender);
                            }
                            Toast.makeText(getApplicationContext(), "User created successfully!", Toast.LENGTH_SHORT).show();
                        } else {
                            String exception = Objects.requireNonNull(task.getException()).getMessage();
                            Toast.makeText(getApplicationContext(), "Error: " + exception, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void uploadUser(FirebaseUser user, String fullname, String email,String password, String dob, String gender) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", fullname);
        userMap.put("email", email);
        userMap.put("password", password);
        userMap.put("date of birth", dob);
        userMap.put("gender", gender);
        userMap.put("uid", user.getUid());



        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users")
                .document(user.getUid())
                .set(userMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "User added to Firestore successfully", Toast.LENGTH_SHORT).show();
                            // Navigate to the main activity or home screen
                            Intent intent = new Intent(Activity_signup.this, Login.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "Error adding user to Firestore: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}