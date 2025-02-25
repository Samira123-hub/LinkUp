package com.example.project;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.chatlinkup.frindes;
import com.example.homepage2.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class friends extends AppCompatActivity {

    RecyclerView recyclerView;
    List<frindes> friendList;
    private FirebaseAuth firebaseAuth;
    FriendAdapter adapter;
    private MyDataBase myDataBase;
    ProgressBar progressBar;
    ImageView backArrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        // Initialisation des vues
        recyclerView = findViewById(R.id.recyclerview);
        progressBar = findViewById(R.id.progressBar);
        backArrow = findViewById(R.id.backArrow);

        // Bouton retour
        backArrow.setOnClickListener(view -> {
            Intent goProfil = new Intent(friends.this, Profil.class);
            startActivity(goProfil);
            finish();
        });

        // Initialisation de RecyclerView
        friendList = new ArrayList<>();
        adapter = new FriendAdapter(friendList, this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setAdapter(adapter);

        // Charger les amis
        fetchFriends();
    }

    private void fetchFriends() {
        progressBar.setVisibility(View.VISIBLE);
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            Log.e("MainActivity", "Utilisateur non authentifié");
            Intent intent = new Intent(this, Profil.class);
            startActivity(intent);
            finish();
            return;
        }

        String currentUserId = currentUser.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Friends")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        friendList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String userId1 = document.getString("userId1");
                            String userId2 = document.getString("userId2");
                            String userName1 = document.getString("userName1");
                            String userName2 = document.getString("userName2");

                            myDataBase = new MyDataBase(this);
                            myDataBase.open();

                            // Récupérer les images pour les utilisateurs
                            String image1 = getImageBase64(userId1);
                            String image2 = getImageBase64(userId2);

                            // Ajouter l'ami correspondant
                            if (currentUserId.equals(userId1)) {
                                friendList.add(new frindes("", userName1, "", "", image2, 0, userId1));
                            } else if (currentUserId.equals(userId2)) {
                                friendList.add(new frindes("", userName2, "", "", image1, 0, userId2));
                            }
                        }
                        adapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);


                    } else {
                        Log.e("MainActivity", "Erreur lors de la récupération des données", task.getException());
                        Toast.makeText(this, "Erreur lors de la récupération des amis", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String getImageBase64(String userId) {
        byte[] profileBytes = myDataBase.retrieveImageByUserId(userId, "profile");
        if (profileBytes != null) {
            Bitmap profileBitmap = BitmapFactory.decodeByteArray(profileBytes, 0, profileBytes.length);
            return BitmapToBase64(profileBitmap);
        }
        return null;
    }

    public String BitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
}
