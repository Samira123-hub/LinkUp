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
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.homepage2.R;
import com.example.project.MyDataBase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Invetations extends Fragment {
    private MyDataBase myDataBase;
    private ArrayList<frindes> filteredList;
    private ArrayList<frindes> friendsList;
    private TextView noInvitationsTextView;
    private ListView listView;
    private Adapterl adapter;

    public Invetations() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_invetations, container, false);

        // Initialiser les listes
        friendsList = new ArrayList<>();
        filteredList = new ArrayList<>();

        // Initialiser les vues
        listView = rootView.findViewById(R.id.invitationslist);
        noInvitationsTextView = rootView.findViewById(R.id.no_invitations_message);

        // Initialisation de l'adaptateur
        adapter = new Adapterl(getContext(), filteredList);
        listView.setAdapter(adapter);

        // Charger les invitations utilisateur
        loadUserInvitations();

        return rootView;
    }

    private void loadUserInvitations() {
        // Vérifier si l'utilisateur est connecté
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (TextUtils.isEmpty(currentUserId)) {
            Log.e("Invetations", "Utilisateur non connecté !");
            noInvitationsTextView.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
            return;
        }

        // Charger les invitations depuis Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Invitations")
                .whereEqualTo("receiverId", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        friendsList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String documentId = document.getId(); // L'ID du document
                            String senderId = document.getString("senderId");
                            String nom = document.getString("senderName");

                            // Récupérer l'image depuis SQLite
                            myDataBase = new MyDataBase(getContext());
                            myDataBase.open();
                            byte[] profileBytes = myDataBase.retrieveImageByUserId(senderId, "profile");

                            String image = null;  // On utilise cette variable pour stocker l'image si elle existe.
                            if (profileBytes != null) {
                                // Convertir l'image en Bitmap
                                Bitmap profileBitmap = BitmapFactory.decodeByteArray(profileBytes, 0, profileBytes.length);
                                image = BitmapToBase64(profileBitmap);  // Convertir Bitmap en Base64 pour l'utiliser plus tard
                                Log.d("Invetations", "Image récupérée avec succès pour l'utilisateur " + senderId);
                            } else {
                                Log.d("Invetations", "Aucune image trouvée pour l'utilisateur " +senderId);
                            }

                            // Ajouter à la liste avec l'image récupérée
                            friendsList.add(new frindes(nom, "", "","", image, 0, documentId)); // Utilisation de l'image
                        }

                        // Mettre à jour la liste filtrée et l'interface utilisateur
                        filteredList.clear();
                        filteredList.addAll(friendsList);
                        updateListVisibility();
                        adapter.notifyDataSetChanged();
                        adjustListViewHeight();
                    } else {
                        Log.e("Invetations", "Erreur lors du chargement des invitations", task.getException());
                        noInvitationsTextView.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.GONE);
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
        if (filteredList.isEmpty()) {
            noInvitationsTextView.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        } else {
            noInvitationsTextView.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
        }
    }

    private void adjustListViewHeight() {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

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

    public void filterList(String query) {
        if (TextUtils.isEmpty(query)) {
            filteredList.clear();
            filteredList.addAll(friendsList);
        } else {
            List<frindes> filtered = new ArrayList<>();
            for (frindes friend : friendsList) {
                if (friend.nom.toLowerCase().contains(query.toLowerCase())
                        || friend.prenom.toLowerCase().contains(query.toLowerCase())) {
                    filtered.add(friend);
                }
            }
            filteredList.clear();
            filteredList.addAll(filtered);
        }

        updateListVisibility();
        adapter.notifyDataSetChanged();
        adjustListViewHeight();
    }
}


class Adapterl extends BaseAdapter {
    private Context context;
    private ArrayList<frindes> ar;

    public Adapterl(Context context, ArrayList<frindes> arr) {
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
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(R.layout.invitation, viewGroup, false);
        }

        // Récupérer l'élément actuel
        frindes currentFriend = ar.get(i);

        // Initialiser les vues
        TextView nom = view.findViewById(R.id.username);
        Button add = view.findViewById(R.id.buttonconfermer);
        TextView message = view.findViewById(R.id.user_message);
        Button delete = view.findViewById(R.id.buttonsup);
        CircleImageView profile = view.findViewById(R.id.photo);

        // Remplir les données dans les vues
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

        // Ajouter le listener pour le bouton "Confirmer"
        add.setOnClickListener(v -> {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseUser currentUser = auth.getCurrentUser();

            if (currentUser == null) {
                Log.e("AuthError", "Aucun utilisateur connecté !");
                return;
            }

            String senderId = currentUser.getUid(); // L'ID de l'utilisateur actuel
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Récupérer l'ID de l'utilisateur ciblé
            String targetUserId = currentFriend.getId();

            // Rechercher l'invitation dans la collection "Invitations"
            db.collection("Invitations")
                    .whereEqualTo("senderId", targetUserId)
                    .whereEqualTo("receiverId", senderId)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        String invitationId = targetUserId; // L'ID de l'invitation

                        // Supprimer l'invitation
                        db.collection("Invitations").document(invitationId)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        // Extraire les données du document
                                        String receiverid = documentSnapshot.getString("senderId");
                                        String receiverName = documentSnapshot.getString("receivedname");
                                        String receiverProfileImage = documentSnapshot.getString("receivedprofile");
                                        String senderName = documentSnapshot.getString("senderName");
                                        String senderProfileImage = documentSnapshot.getString("senderprofile");
                                        db.collection("Invitations").document(invitationId)
                                                .delete()
                                                .addOnSuccessListener(aVoid -> {
                                                    Log.d("FirestoreSuccess", "Invitation supprimée avec succès"+targetUserId);

                                                    // Ajouter l'amitié dans la collection "Friends"
                                                    Map<String, Object> friendData = new HashMap<>();
                                                    friendData.put("userId1", senderId);
                                                    friendData.put("userName1", senderName);
                                                    friendData.put("userProfileImage1", senderProfileImage);

                                                    friendData.put("userId2", receiverid);
                                                    friendData.put("userName2", receiverName);
                                                    friendData.put("userProfileImage2", receiverProfileImage);

                                                    db.collection("Friends").add(friendData)
                                                            .addOnSuccessListener(documentReference -> {
                                                                Log.d("FirestoreSuccess", "Amitié ajoutée avec ID : " + documentReference.getId());

                                                                delete.setVisibility(View.GONE);
                                                                add.setVisibility(View.GONE);
                                                                message.setText("You are friends");
                                                                message.setVisibility(View.VISIBLE);
                                                                notifyDataSetChanged();
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Log.e("FirestoreError", "Erreur lors de l'ajout de l'amitié", e);
                                                            });
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e("FirestoreError", "Erreur lors de la suppression de l'invitation", e);
                                                });
                                    } else {
                                        Log.e("FirestoreError", "Le document avec ID : " + invitationId + " n'existe pas.");
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("FirestoreError", "Erreur lors de la récupération de l'invitation", e);
                                });
                    });
        });

        delete.setOnClickListener(v -> {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseUser currentUser = auth.getCurrentUser();
            assert currentUser != null;
            String senderId = currentUser.getUid(); // L'ID de l'utilisateur actuel
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Récupérer l'ID de l'utilisateur ciblé
            String targetUserId = currentFriend.getId();
            Log.e("FirestoreError", targetUserId);

            // Rechercher l'invitation dans la collection "Invitations"
            db.collection("Invitations")
                    .whereEqualTo("senderId", senderId)
                    .whereEqualTo("receiverId", targetUserId)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {

                        db.collection("Invitations").document(targetUserId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("FirestoreSuccess", "Invitation supprimée avec succès");

                                    // Mettre à jour la liste après la suppression
                                    ar.remove(i); // Supprimer l'élément de la liste
                                    notifyDataSetChanged(); // Mettre à jour l'adaptateur
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("FirestoreError", "Erreur lors de la suppression de l'invitation", e);
                                });

                    })
                    .addOnFailureListener(e -> {
                        Log.e("FirestoreError", "Erreur lors de la récupération des invitations", e);
                    });
        });

        return view;
    }
}
