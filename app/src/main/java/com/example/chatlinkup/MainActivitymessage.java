package com.example.chatlinkup;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.homepage2.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import com.example.project.MyDataBase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class MainActivitymessage extends AppCompatActivity {
    MyDataBase myDataBase;
    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;
    private List<com.example.chatlinkup.Message> messageList;
    private EditText editTextMessage;
    private ImageButton buttonSend;
    private String currentUserId ;
    private FirebaseFirestore db;
    private String otherUserId ; // ID autre utilisateur

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_layout);
// Récupérer les données depuis l'Intent
        Intent intent = getIntent();
        String nom = intent.getStringExtra("nom");
        otherUserId = intent.getStringExtra("autherId");
        TextView nomTextView = findViewById(R.id.nomTextView);
        nomTextView.setText(nom);
        CircleImageView profileImageView = findViewById(R.id.profileImageView);
        myDataBase = new MyDataBase(this);
        myDataBase.open();
// Récupérer l'image en tant que tableau d'octets
        byte[] profileBytes = myDataBase.retrieveImageByUserId(otherUserId, "profile");
// Vérifier si l'image a été trouvée dans la base de données
        if (profileBytes != null && profileBytes.length > 0) {
            Bitmap profileBitmap = BitmapFactory.decodeByteArray(profileBytes, 0, profileBytes.length);
            if (profileBitmap != null) {
                profileImageView.setImageBitmap(profileBitmap);
            } else {
                Log.e("ImageError", "Failed to decode image");
                profileImageView.setImageResource(R.drawable.profil); // Image par défaut en cas d'erreur
            }
        } else {
            Log.e("ImageError", "Image bytes are null or empty");
            profileImageView.setImageResource(R.drawable.default_profile); // Image par défaut
        }


        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Récupérer l'ID de l'utilisateur actuel
        db = FirebaseFirestore.getInstance();

        // Initialisation des composants
        recyclerView = findViewById(R.id.recyclerView);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);

        // Initialiser la liste de messages
        messageList = new ArrayList<>();
        loadMessages(currentUserId);
        // Initialiser l'adapter et le layout manager
        messageAdapter = new MessageAdapter(messageList,currentUserId);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(messageAdapter);

        // Action lors du clic sur le bouton Envoyer
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    // Méthode pour envoyer un message
    private void sendMessage() {
        String messageText = editTextMessage.getText().toString();
        if (!messageText.isEmpty()) {
            long timestamp = System.currentTimeMillis();
            boolean isCurrentUserSender = currentUserId.equals(otherUserId) ? true : false;

            // Créer un message
            Message message = new Message(currentUserId, otherUserId, messageText, timestamp,isCurrentUserSender);

            // Ajouter le message dans Firestore
            db.collection("messages").add(message);

            // Ajouter le message à la liste et mettre à jour RecyclerView
            messageList.add(message);
            messageAdapter.notifyItemInserted(messageList.size() - 1);
            recyclerView.scrollToPosition(messageList.size() - 1);
            editTextMessage.setText(""); // Effacer le champ texte
        }
    }

    private void loadMessages(String currentUserId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Accéder à la collection "messages"
        db.collection("messages")
                .whereIn("senderId", Arrays.asList(currentUserId, otherUserId)) // Sélectionner un des deux comme senderId
                .whereIn("receiverId", Arrays.asList(currentUserId, otherUserId)) // Sélectionner un des deux comme receiverId
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        messageList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Message message = document.toObject(Message.class);
                            messageList.add(message);
                        }
                        // Trier les messages par timestamp
                        messageList.sort((m1, m2) -> Long.compare(m1.getTimestamp(), m2.getTimestamp()));
                        messageAdapter.notifyDataSetChanged();
                    } else {
                        System.out.println("Erreur lors de la récupération des messages : " + task.getException().getMessage());
                    }
                });

    }

}

