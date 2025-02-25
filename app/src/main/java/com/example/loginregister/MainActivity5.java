package com.example.loginregister;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.homepage2.R;

public class MainActivity5 extends AppCompatActivity {
    private TextView txt5;
    private TextView txt2;

    private androidx.appcompat.widget.AppCompatButton joinBtn5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main5);
        txt5 = findViewById(R.id.txt5);
        txt2 = findViewById(R.id.txt2);
        joinBtn5 = findViewById(R.id.joinBtn5);
        joinBtn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(MainActivity5.this, MainActivity2.class);
                startActivity(it);
            }
        });

    }
}