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

public class MainActivity2 extends AppCompatActivity {
    private TextView txt3;
    private TextView txt4;
    private androidx.appcompat.widget.AppCompatButton joinBtn2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main2);
        txt3 = findViewById(R.id.txt3);
        txt4 = findViewById(R.id.txt4);
        joinBtn2 = findViewById(R.id.joinBtn2);
        joinBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(MainActivity2.this, Login.class);
                startActivity(it);
            }
        });

    }
}