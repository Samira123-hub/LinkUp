package com.example.loginregister;

//import static com.example.loginregister.Activity_signup.EMAIL_REGEX;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.homepage2.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {
    private TextView signUpBtn;
    private TextView forgotPassword;
    private DatabaseHelper dbHelper;
    private EditText emailEt;
    private Button signInBtn;
    private FirebaseAuth Auth;
    private com.google.android.material.textfield.TextInputEditText passwordEt;
    public static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[a-z]{2,6}$";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
//        dbHelper = new DatabaseHelper(this);
        emailEt = findViewById(R.id.emailEt);
        passwordEt = findViewById(R.id.passwordEt);
        signInBtn = findViewById(R.id.signInBtn);
        Auth=FirebaseAuth.getInstance();

        signUpBtn = findViewById(R.id.signUpBtn);
        forgotPassword = findViewById(R.id.forgotPassword);
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(Login.this, Activity_signup.class);
                startActivity(it);
            }
        });
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent itt = new Intent(Login.this, Register.class);
                startActivity(itt);
            }
        });
        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                loginUser();
            }
        });
    }
    private void loginUser() {
        String email = emailEt.getText().toString().trim();
        String password = passwordEt.getText().toString().trim();
        if(email.isEmpty()|| !email.matches(EMAIL_REGEX)){
            emailEt.setError("please input valid email");
            return;
        }
        if(password.isEmpty()||password.length()<6){
            passwordEt.setError("please input valid password");
            return;
        }
        Auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = Auth.getCurrentUser();
                    if (user != null) {
                        user.reload().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> reloadTask) {
                                if (reloadTask.isSuccessful() && user.isEmailVerified()) {
                                    Intent it = new Intent(Login.this, com.example.homepage2.MainActivity.class);
                                    startActivity(it);
                                    Toast.makeText(Login.this, "Login successful!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(Login.this, "Please verify your email before logging in.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
                else{
                    String exception = task.getException().getMessage();
                    Toast.makeText(getApplicationContext(), "Error: " + exception, Toast.LENGTH_SHORT).show();

                }}
        });
       /* SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = {
                DatabaseHelper.COLUMN_ID,
                DatabaseHelper. COLUMN_EMAIL,
                DatabaseHelper.COLUMN_PASSWORD
        };
        String selection = DatabaseHelper. COLUMN_EMAIL + " = ? AND " + DatabaseHelper.COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = {email, password};

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_NAME,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
            Intent it = new Intent(Login.this, MainActivity3.class);
            startActivity(it);
        } else {
            Toast.makeText(this, "Invalid email or password!", Toast.LENGTH_SHORT).show();
  }*/

}
}