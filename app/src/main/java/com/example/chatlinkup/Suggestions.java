package com.example.chatlinkup;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.homepage2.R;
import com.example.project.MyDataBase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class Suggestions extends Fragment {
    private ArrayList<frindes> filteredList = new ArrayList<>();
    private ArrayList<frindes> friendsList = new ArrayList<>();
    private Adap adapter;
    ProgressBar progressBar;
    private TextView noSuggestionTextView;
    private ListView listView;
    private MyDataBase myDataBase;

    public Suggestions() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Charger le layout du fragment
        View rootView1 = inflater.inflate(R.layout.fragment_suggestions, container, false);
        progressBar = rootView1.findViewById(R.id.progressBar);
        // Initialiser les vues
        listView = rootView1.findViewById(R.id.suggestion);
        noSuggestionTextView = rootView1.findViewById(R.id.no_suggestions_message);

        // Configurer l'adaptateur
        adapter = new Adap(getContext(), filteredList);
        listView.setAdapter(adapter);

        // Charger les données depuis Firebase
        loadFriendsFromFirebase();

        return rootView1;
    }

    private void loadFriendsFromFirebase() {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();  // Récupérer l'utilisateur actuel

        if (currentUser == null) {
            Log.e("AuthError", "No user is logged in!");
            return;  // Quitter si aucun utilisateur n'est connecté
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                friendsList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    // Vérifier si l'utilisateur actuel est l'utilisateur du document
                    String senderId = document.getId();
                    if (senderId.equals(currentUser.getUid())) {
                        continue;  // Ne pas ajouter l'utilisateur actuel dans la liste des suggestions
                    }

                    String name = document.getString("name");
                    if (name == null) {
                        Log.w("FirestoreWarning", "Le champ 'name' est manquant pour l'utilisateur avec 'uid' : " + senderId);
                    }

                    // Récupérer l'image depuis SQLite
                    myDataBase = new MyDataBase(getContext());
                    myDataBase.open();
                    byte[] profileBytes = myDataBase.retrieveImageByUserId(senderId, "profile");

                    String image = null;  // Utilisation de cette variable pour stocker l'image si elle existe.
                    if (profileBytes != null) {
                        // Convertir l'image en Bitmap
                        Bitmap profileBitmap = BitmapFactory.decodeByteArray(profileBytes, 0, profileBytes.length);
                        image = BitmapToBase64(profileBitmap);  // Convertir Bitmap en Base64 pour l'utiliser plus tard
                        Log.d("Invitations", "Image récupérée avec succès pour l'utilisateur " + senderId);
                    } else {
                        Log.d("Invitations", "Aucune image trouvée pour l'utilisateur " + senderId);
                    }

                    // Ajouter l'utilisateur et son image à la liste des amis
                    friendsList.add(new frindes(name, "", "", "", image, 0, senderId));
                }

                // Mettre à jour la liste filtrée et l'interface
                filteredList.clear();
                filteredList.addAll(friendsList);
                updateListVisibility();
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                adjustListViewHeight();
            } else {
                Log.e("FirestoreError", "Erreur lors de la récupération des utilisateurs", task.getException());
            }
        });
    }


    public String BitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void updateListVisibility() {
        // Montrer ou cacher la liste en fonction du contenu
        if (filteredList.isEmpty()) {
            noSuggestionTextView.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        } else {
            noSuggestionTextView.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
        }
    }

    private void adjustListViewHeight() {
        // Ajuster dynamiquement la hauteur de la ListView
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null || listAdapter.getCount() == 0) return;

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    public void filterList1(String query) {
        // Filtrer la liste en fonction de la recherche
        if (TextUtils.isEmpty(query)) {
            filteredList.clear();
            filteredList.addAll(friendsList);
        } else {
            ArrayList<frindes> filtered = new ArrayList<>();
            for (frindes friend : friendsList) {
                if ((!TextUtils.isEmpty(friend.nom) && friend.nom.toLowerCase().contains(query.toLowerCase()))
                        || (!TextUtils.isEmpty(friend.prenom) && friend.prenom.toLowerCase().contains(query.toLowerCase()))) {
                    filtered.add(friend);
                }
            }
            filteredList.clear();
            filteredList.addAll(filtered);
        }

        // Mettre à jour l'interface
        updateListVisibility();
        adapter.notifyDataSetChanged();
        adjustListViewHeight();
    }
}


class Adap extends BaseAdapter {
    private Context context;
    private ArrayList<frindes> ar;

    public Adap(Context context, ArrayList<frindes> arr) {
        this.context = context;
        this.ar = arr;
    }

    @Override
    public int getCount() {
        return ar.size();
    }

    @Override
    public Object getItem(int i) {
        return ar.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.suggestions, parent, false);
        }
        TextView message = convertView.findViewById(R.id.user_message);
        Button sup = convertView.findViewById(R.id.btn1);
        Button add = convertView.findViewById(R.id.btn2);

        // Gérer le clic sur le bouton pour supprimer l'élément
        sup.setOnClickListener(v -> {
            // Supprimer l'élément de la liste des données
            ar.remove(i);

            // Mettre à jour la ListView
            notifyDataSetChanged();
        });
        frindes currentFriend = ar.get(i);
        add.setOnClickListener(v -> {
            sup.setVisibility(View.GONE);
            add.setVisibility(View.GONE);
            message.setText("Request sent successfully to "+ currentFriend.getNom());
            message.setVisibility(View.VISIBLE);
            notifyDataSetChanged();
            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseUser currentUser = auth.getCurrentUser();
            if (currentUser == null) {
                Log.e("AuthError", "Aucun utilisateur connecté !");
                return;
            }

            String senderId = currentUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("Users").document(senderId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String senderName = documentSnapshot.getString("name");


                            // Récupérer l'ID de l'utilisateur ciblé
                            String targetUserId = currentFriend.getId();

                            // Ajouter les informations nécessaires à l'invitation
                            Map<String, Object> invitationData = new HashMap<>();
                            invitationData.put("receiverId", targetUserId);
                            invitationData.put("senderId", senderId);
                            invitationData.put("senderprofile", documentSnapshot.getString("profile"));
                            invitationData.put("receivedname", currentFriend.nom);
                            invitationData.put("receivedprofile", currentFriend.image);
                            invitationData.put("senderName", senderName != null ? senderName : "Nom non disponible");
                            invitationData.put("timestamp", System.currentTimeMillis());
                            String invitationId = UUID.randomUUID().toString();  // Générer un ID unique
                            invitationData.put("invitationId", invitationId);
                            // Ajouter l'invitation dans Firestore
                            db.collection("Invitations").add(invitationData)
                                    .addOnSuccessListener(documentReference -> {
                                        Log.d("FirestoreSuccess", "Invitation ajoutée avec ID : " + documentReference.getId());

                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("FirestoreError", "Erreur lors de l'ajout de l'invitation", e);
                                    });
                        } else {
                            Log.e("FirestoreError", "Le document n'existe pas pour l'UID : " + senderId);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FirestoreError", "Erreur lors de la récupération du document", e);
                    });
        });

        // Initialiser les vues de l'élément
        TextView nom = convertView.findViewById(R.id.username);
        CircleImageView profile = convertView.findViewById(R.id.photo);
        // Remplir les données
        nom.setText(currentFriend.nom + " " + currentFriend.prenom);
        // Si l'image est en Base64
        String base64Image = currentFriend.image; // Assurez-vous que "image" contient une chaîne Base64 valide

        if (base64Image != null && !base64Image.isEmpty()) {
            // Décoder la chaîne Base64 en tableau d'octets
            byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);

            // Convertir le tableau d'octets en Bitmap
            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

            // Mettre l'image décodée dans le CircleImageView
            profile.setImageBitmap(decodedBitmap);
        }

        return convertView;
    }
}




