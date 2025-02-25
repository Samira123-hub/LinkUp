package com.example.homepage2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatlinkup.suggestion_d_amis;
import com.example.project.MyDataBase;
import com.example.project.Profil;
import com.example.project.notifications;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ImageView profil1;
    ImageView profil2;
    private MyDataBase myDataBase;
    private String userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        profil1 = findViewById(R.id.imageView1);
        profil2 = findViewById(R.id.imageView3);
        // Utilisateur connecté, récupérez son ID unique
        userId = user.getUid();
        // Initialiser la base de données SQLite
        myDataBase = new MyDataBase(this);
        myDataBase.open();
        // Configuration des icônes et des clics
        setupLogoClick();
        setupSearchIconClick();
        setupPostEditClick();
        setupSettingsClick();
        setupChatClick();
        setupProfil2Click();
        setupProfilClick();
        setupNotifClick();
        addFriendsClick();
        loadImages();
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Exécuter la fonction lorsque l'activité est reprise
        loadImages();
        populatePostList();
    }
    private void setupSettingsClick() {
        ImageView setting = findViewById(R.id.settingIcon);
        setting.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }

    private void setupNotifClick() {
        ImageView notification = findViewById(R.id.notificationIcon);
        notification.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, notifications.class);
            startActivity(intent);
        });
    }

    private void setupProfilClick() {
        ImageView profil = findViewById(R.id.imageView3);
        profil.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Profil.class);
            startActivity(intent);
        });
    }

    private void setupProfil2Click() {
        ImageView profil = findViewById(R.id.imageView1);
        profil.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Profil.class);
            startActivity(intent);
        });
    }

    private void setupChatClick() {
        ImageView chat = findViewById(R.id.messageIcon);
        chat.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, com.example.chatlinkup.MainActivity.class);
            startActivity(intent);
        });
    }

    private void setupLogoClick() {
        ImageView logo = findViewById(R.id.logoApp);
        logo.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            startActivity(intent);
        });
    }

    private void addFriendsClick() {
        ImageView addFriends = findViewById(R.id.addIcon);
        addFriends.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, suggestion_d_amis.class);
            startActivity(intent);
        });
    }

    private void setupSearchIconClick() {
        ImageView search = findViewById(R.id.searchIcon);
        search.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SecondActivity.class);
            startActivity(intent);
        });
    }

    private void setupPostEditClick() {
        EditText postEdit = findViewById(R.id.postEdit);
        postEdit.setOnClickListener(v -> {
            AddPostBottomSheetFragment bottomSheetFragment = new AddPostBottomSheetFragment();
            bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
        });
    }


    private void populatePostList() {
        ListView postsListView = findViewById(R.id.postsListView);
        myDataBase.open();
        List<Post> posts = myDataBase.getAllPosts();
        Log.d("MyApp", "Number of posts retrieved: " + posts.size());
        if (posts != null && !posts.isEmpty()) {
            PostAdapter adapter = new PostAdapter(this, posts);
            postsListView.setAdapter(adapter);
        } else {
            Log.d("MyApp", "Aucun post à afficher");
        }
    }

    public void loadImages() {
        // Utiliser AsyncTask pour charger les images de manière asynchrone
        new LoadImageTask().execute();
    }

    public class LoadImageTask extends AsyncTask<Void, Void, Bitmap[]> {
        @Override
        protected Bitmap[] doInBackground(Void... voids) {
            byte[] coverBytes = myDataBase.retrieveImageByUserId(userId, "cover");
            byte[] profileBytes = myDataBase.retrieveImageByUserId(userId, "profile");

            Bitmap coverBitmap = null;
            Bitmap profileBitmap = null;

            if (coverBytes != null) {
                coverBitmap = BitmapFactory.decodeByteArray(coverBytes, 0, coverBytes.length);
            }
            if (profileBytes != null) {
                profileBitmap = BitmapFactory.decodeByteArray(profileBytes, 0, profileBytes.length);
            }

            return new Bitmap[]{coverBitmap, profileBitmap};
        }

        @Override
        protected void onPostExecute(Bitmap[] bitmaps) {
            super.onPostExecute(bitmaps);
            if (bitmaps[1] != null) {
                profil2.setImageBitmap(bitmaps[1]);
                profil1.setImageBitmap(bitmaps[1]); // Mettre à jour les deux ImageViews
            }
        }
    }
}
