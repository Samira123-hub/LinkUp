package com.example.project;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.homepage2.MainActivity;
import com.example.project.MyDataBase;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.homepage2.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class notifications extends AppCompatActivity {

    RecyclerView recyclerView;
    List<Notification> notificationList;
    NotificationAdapter adapter;
    ProgressBar progressBar;
    ImageView backArrow;
    MyDataBase myDataBase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notifications);

        myDataBase = new MyDataBase(this);
        recyclerView = findViewById(R.id.recyclerview);
        progressBar = findViewById(R.id.progressBar);
        backArrow = findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goProfil = new Intent(notifications.this, MainActivity.class);
                startActivity(goProfil);
                finish();
            }
        });
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
        notificationList = new ArrayList<>();

        adapter = new NotificationAdapter(notificationList,notifications.this);
        recyclerView.setAdapter(adapter);

        loadNotifications();

    }

    private void loadNotifications() {
        progressBar.setVisibility(View.VISIBLE);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                Cursor cursor = myDataBase.getNotifications(userId);
                notificationList.clear();
                if (cursor.moveToFirst()) {
                    do {
                        @SuppressLint("Range") String type = cursor.getString(cursor.getColumnIndex("type"));
                        @SuppressLint("Range") String postContent = cursor.getString(cursor.getColumnIndex("context"));
                        @SuppressLint("Range") String fromUserId = cursor.getString(cursor.getColumnIndex("from_user_id"));
                        @SuppressLint("Range")String fromUserName = cursor.getString(cursor.getColumnIndex("from_user_name"));
                        @SuppressLint("Range") String timestamp = cursor.getString(cursor.getColumnIndex("timestamp"));

                        Notification notification = new Notification(userId, type, postContent, fromUserId, fromUserName, timestamp);
                        notificationList.add(notification);
                    } while (cursor.moveToNext());
                }
                cursor.close();

                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            } catch (Exception e) {
                e.printStackTrace();
                progressBar.setVisibility(View.GONE);
                Log.e("NotificationError", "Failed to load notifications ",e);
                Toast.makeText(this, "Failed to load notifications: "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, 1000);
    }
}
