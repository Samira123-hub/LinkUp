package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.homepage2.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class photos extends AppCompatActivity {
    RecyclerView recyclerView;
    List<String> imageUrls;
    PhotoAdapter adapter;
    ProgressBar progressBar;
    ImageView backArrow;
    MyDataBase myDataBase;
    String userId; // Assure-toi de récupérer l'ID utilisateur approprié

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photos);

        recyclerView = findViewById(R.id.recyclerview);
        progressBar = findViewById(R.id.progressBar);
        backArrow = findViewById(R.id.backArrow);
        backArrow.setOnClickListener(view -> {
            Intent goProfil = new Intent(photos.this, Profil.class);
            startActivity(goProfil);
            finish();
        });
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        imageUrls = new ArrayList<>();

        adapter = new PhotoAdapter(imageUrls, this);
        recyclerView.setAdapter(adapter);

        // Initialiser la base de données
        myDataBase = new MyDataBase(this);
        myDataBase.open();

        // Récupérer l'ID utilisateur (par exemple depuis Firebase Auth)
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userId = user.getUid();
        }

        loadImages();
    }

    private void loadImages() {
        progressBar.setVisibility(View.VISIBLE);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                // Utiliser la méthode getUserImages pour récupérer les images de l'utilisateur depuis SQLite
                imageUrls.addAll(myDataBase.getUserImages(userId));
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            } catch (Exception e) {
                e.printStackTrace();
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Erreur lors du chargement des images", Toast.LENGTH_SHORT).show();
            }
        }, 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myDataBase.close();
    }
}
