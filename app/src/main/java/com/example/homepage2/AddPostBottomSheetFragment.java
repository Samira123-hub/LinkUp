package com.example.homepage2;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.project.MyDataBase;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddPostBottomSheetFragment extends BottomSheetDialogFragment {
    private MyDataBase myDataBase;
    String userName;
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView addedImageView;
    private LinearLayout linearLayoutAddPhoto, linearLayoutSelectedPhoto;

    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_post, container, false);
        myDataBase = new MyDataBase(getContext());
        myDataBase.open();
        // Initialisation Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("posts");
        storageReference = FirebaseStorage.getInstance().getReference("post_images");

        // Récupérer les éléments du formulaire
        EditText postContentEditText = view.findViewById(R.id.postContentEditText);
        addedImageView = view.findViewById(R.id.addedImageView);
        Button submitPostButton = view.findViewById(R.id.submitPostButton);
        TextView addImageButton = view.findViewById(R.id.addPhoto);
        linearLayoutAddPhoto = view.findViewById(R.id.linearLayoutAddPhoto);
        linearLayoutSelectedPhoto = view.findViewById(R.id.linearLayoutSelectedPhoto);

        // Bouton "Ajouter une image"
        addImageButton.setOnClickListener(v -> openGallery());

        submitPostButton.setOnClickListener(v -> {
            String postContent = postContentEditText.getText().toString().trim();
            if (postContent.isEmpty() || addedImageView.getDrawable() == null) {
                Toast.makeText(getContext(), "Veuillez ajouter un contenu et une image", Toast.LENGTH_SHORT).show();
                return;
            }

            Bitmap bitmap = ((BitmapDrawable) addedImageView.getDrawable()).getBitmap();



            // Récupérer l'utilisateur actuel
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseFirestore.getInstance().collection("Users").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String userName = documentSnapshot.getString("name");
                            Log.d("AddPost", "User Name: " + userName);

                            String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());

                            // Insérer le post dans SQLite
                            boolean isInserted = myDataBase.insertPost(userId, userName,postContent , bitmap , currentDate.toString());
                            if (isInserted) {
                                Toast.makeText(getContext(), "Post ajouté avec succès!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getContext(), MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            } else {
                                Toast.makeText(getContext(), "Erreur lors de l'ajout du post", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getContext(), "Utilisateur introuvable", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("AddPost", "Erreur lors de la récupération de l'utilisateur", e);
                        Toast.makeText(getContext(), "Erreur lors de la récupération des données utilisateur.", Toast.LENGTH_SHORT).show();
                    });
        });

        return view;
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                linearLayoutAddPhoto.setVisibility(View.GONE);
                linearLayoutSelectedPhoto.setVisibility(View.VISIBLE);
                addedImageView.setImageURI(selectedImageUri);
                addedImageView.setTag(selectedImageUri); // Sauvegarder l'URI
            }
        }
    }

}

