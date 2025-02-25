package com.example.homepage2;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.project.MyDataBase;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CommentBottomSheetFragment extends BottomSheetDialogFragment {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String currentUserId;
    private String currentUserName;
    private String postContent;
    private MyDataBase myDataBase;

    private List<Commentaire> commentaires = new ArrayList<>();
    private CommentAdapter adapter;

    private EditText commentInput;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comment_bottom_sheet, container, false);

        // Initialisation Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Vérifiez si l'utilisateur est connecté
        if (auth.getCurrentUser() != null) {
            currentUserId = auth.getCurrentUser().getUid();
            db.collection("Users").document(currentUserId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            currentUserName = documentSnapshot.getString("name");
                        }
                    });

        } else {
            Toast.makeText(getContext(), "Utilisateur non connecté", Toast.LENGTH_SHORT).show();
            return view;
        }
        Bundle args = getArguments();
        if (args != null) {
            postContent = args.getString("postContent", ""); // Par défaut, une chaîne vide si la clé n'existe pas
        }

        // Initialiser les composants de l'interface
        commentInput = view.findViewById(R.id.addComment);
        view.findViewById(R.id.commentButton).setOnClickListener(v -> addComment());

        // Charger les commentaires existants
        populateCommentList(view);

        return view;
    }

    private void populateCommentList(View view) {
        ListView commentListView = view.findViewById(R.id.listComment);
        if (postContent == null || postContent.isEmpty()) {
            Toast.makeText(getContext(), "No comment found for this post", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("comments")
                .whereEqualTo("postContent", postContent) // Filtrer les commentaires par postContent
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        commentaires.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Récupérer l'image depuis SQLite
                            myDataBase = new MyDataBase(getContext());
                            myDataBase.open();
                            String userId = document.getString("userId"); // Assurez-vous que le champ userId existe dans Firestore
                            byte[] profileBytes = myDataBase.retrieveImageByUserId(userId, "profile");
                            Bitmap profileBitmap = null;
                            if (profileBytes != null) {
                                profileBitmap = BitmapFactory.decodeByteArray(profileBytes, 0, profileBytes.length);
                            }

                            String userName = document.getString("userName");
                            String content = document.getString("content");
                            String date = document.getString("date");

                            Commentaire comment = new Commentaire(profileBitmap, userName, content, date);
                            commentaires.add(comment);
                        }

                        adapter = new CommentAdapter(requireContext(), commentaires);
                        commentListView.setAdapter(adapter);
                    } else {
                        Toast.makeText(getContext(), "Erreur lors de la récupération des commentaires", Toast.LENGTH_SHORT).show();
                    }
                });

    }


    private void addComment() {
        // Récupérer le contenu du commentaire
        String content = commentInput.getText().toString().trim();
        if (content.isEmpty()) {
            Toast.makeText(getContext(), "Le commentaire ne peut pas être vide", Toast.LENGTH_SHORT).show();
            return;
        }

        // Récupérer la date actuelle
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());

        // Préparer les données pour Firestore
        Map<String, Object> commentData = new HashMap<>();
        commentData.put("userId",currentUserId);
        commentData.put("userName", currentUserName);
        commentData.put("postContent",postContent);
        commentData.put("content", content);
        commentData.put("date", date);
        // Ajouter le commentaire dans Firestore
        db.collection("comments")
                .add(commentData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Commentaire ajouté!", Toast.LENGTH_SHORT).show();
                    commentInput.setText(""); // Réinitialiser le champ de saisie
                    if (getView() != null) { // Vérifier que la vue est disponible avant de la rafraîchir
                        populateCommentList(getView()); // Rafraîchir la liste des commentaires
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Erreur lors de l'ajout du commentaire", Toast.LENGTH_SHORT).show());
    }

    private byte[] bitmapToByteArray(Bitmap bitmap) {
        if (bitmap == null) return null;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        return outputStream.toByteArray();
    }
}
