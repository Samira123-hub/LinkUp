package com.example.project;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.homepage2.Post;
import com.example.homepage2.PostAdapter;
import com.example.homepage2.R;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class Profil extends AppCompatActivity {

    private ImageView coverImage;
    private ImageView profileImage;
    private FloatingActionButton coverButton;
    private FloatingActionButton profilButton;
    private ShapeableImageView profilPic;
    //    private RecyclerView recyclerview;
    private MaterialButton postIcon;
    private MaterialButton imageIcon;
    private MaterialButton friendsIcon;
    private MaterialButton aboutIcon;
    private static final int COVER_PHOTO_REQUEST_CODE = 101;
    private static final int PROFILE_PHOTO_REQUEST_CODE = 102;

    private MyDataBase myDataBase;
    private String userId;
    TextView userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_profil);

        // Initialisation des éléments de l'interface utilisateur
        userName = findViewById(R.id. userName);
        postIcon = findViewById(R.id.postIcon);
        imageIcon = findViewById(R.id.imageIcon);
        friendsIcon = findViewById(R.id.friendsIcon);
        aboutIcon = findViewById(R.id.aboutIcon);
        profilPic = findViewById(R.id.profilPic);
        coverImage = findViewById(R.id.coverPhoto);
        profileImage = findViewById(R.id.profilPhoto);
        coverButton = findViewById(R.id.coverPhotoChangeButton);
        profilButton = findViewById(R.id.profilPhotoChangeButton);

        // Obtenez l'ID de l'utilisateur actuel (par exemple via Firebase ou une autre méthode)
        // Remplacez ceci par l'ID de l'utilisateur actuel
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Utilisateur connecté, récupérez son ID unique
        userId = user.getUid();
        // Initialiser la base de données SQLite
        myDataBase = new MyDataBase(this);
        myDataBase.open();

        // Définir les actions des boutons de navigation
        setNavigationButtons();

        // Charger les images sauvegardées
        loadImages();
        populatePostList();

        // Configurer les boutons pour changer la photo de profil et la photo de couverture
        setImagePickers();
// Vérifier si l'utilisateur est connecté
        if (user != null) {


            // Récupérer les données de l'utilisateur depuis Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("Users").document(userId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // Récupérer le nom de l'utilisateur depuis la collection
                                String name = document.getString("name"); // Récupérer le champ "name" du document

                                // Afficher le nom de l'utilisateur dans le TextView
                                userName.setText(name);
                            } else {
                                userName.setText("Utilisateur introuvable");
                            }
                        } else {
                            System.out.println("Erreur lors de la récupération des données de l'utilisateur : " + task.getException().getMessage());
                            userName.setText("Erreur de récupération");
                        }
                    });
        } else {
            // L'utilisateur n'est pas connecté
            userName.setText("Utilisateur non connecté");
        }
        // Bouton pour créer une publication
        EditText createPubButton = findViewById(R.id.createPubButton);
        createPubButton.setOnClickListener(v -> {
            AddPostBottomSheetFragment bottomSheetFragment = new AddPostBottomSheetFragment();
            bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
        });
    }

    private void setNavigationButtons() {
        postIcon.setOnClickListener(view -> {
            startActivity(new Intent(Profil.this, Profil.class));
            finish();
        });

        imageIcon.setOnClickListener(view -> {
            startActivity(new Intent(Profil.this, photos.class));
            finish();
        });

        friendsIcon.setOnClickListener(view -> {
            startActivity(new Intent(Profil.this, friends.class));
            finish();
        });

        aboutIcon.setOnClickListener(view -> {
            startActivity(new Intent(Profil.this, about.class));
            finish();
        });
    }
    ///////////////////////////////////////////////////////////////////////////


    private void populatePostList() {
        ListView postsListView = findViewById(R.id.postsListView);
        myDataBase.open();
        List<Post> posts = myDataBase.getUserPosts(userId);
        Log.d("MyApp", "Number of posts retrieved: " + posts.size());
        if (posts != null && !posts.isEmpty()) {
            PostAdapter adapter = new PostAdapter(Profil.this, posts);
            postsListView.setAdapter(adapter);
        } else {
            Log.d("MyApp", "Aucun post à afficher");
        }
    }
    /////////////////////////////////////////////////////////////////////////////

    private void setImagePickers() {
        coverButton.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.black)));
        profilButton.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.black)));

        // Photo de couverture
        coverButton.setOnClickListener(view ->
                ImagePicker.with(this)
                        .crop()
                        .compress(1024)
                        .maxResultSize(1080, 1080)
                        .start(COVER_PHOTO_REQUEST_CODE)
        );

        // Photo de profil
        profilButton.setOnClickListener(view ->
                ImagePicker.with(this)
                        .crop()
                        .compress(1024)
                        .maxResultSize(1080, 1080)
                        .start(PROFILE_PHOTO_REQUEST_CODE)
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                try {
                    Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));

                    if (requestCode == COVER_PHOTO_REQUEST_CODE) {
                        coverImage.setImageBitmap(bitmap);
                        coverImage.invalidate(); // Forcer le redessin
                        saveImageToDatabase(bitmap, "cover");
                    } else if (requestCode == PROFILE_PHOTO_REQUEST_CODE) {
                        profileImage.setImageBitmap(bitmap);
                        profilPic.setImageBitmap(bitmap); // Mettre à jour les deux ImageViews
                        profilPic.invalidate(); // Forcer le redessin
                        saveImageToDatabase(bitmap, "profile");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void saveImageToDatabase(Bitmap bitmap, String type) {
        // Convertir le Bitmap en tableau d'octets
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] imageBytes = stream.toByteArray();

        // Insérer dans la base de données SQLite avec l'ID utilisateur
        myDataBase.insertImage(userId, type, imageBytes);
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

            if (bitmaps[0] != null) {
                coverImage.setImageBitmap(bitmaps[0]);
            }

            if (bitmaps[1] != null) {
                profileImage.setImageBitmap(bitmaps[1]);
                profilPic.setImageBitmap(bitmaps[1]); // Mettre à jour les deux ImageViews
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myDataBase.close();
    }
}
