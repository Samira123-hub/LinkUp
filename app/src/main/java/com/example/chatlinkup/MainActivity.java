package com.example.chatlinkup;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.homepage2.R;
import com.example.project.MyDataBase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    private EditText search;
    private ArrayList<frindes> friendList;
    private ImageView homme;
    private Adapterl adapter;
    private MyDataBase myDataBase;
    private FirebaseAuth firebaseAuth;
    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_msg);

        // Initialisations
        search = findViewById(R.id.search);
        homme = findViewById(R.id.homme_button);
        friendList = new ArrayList<>();
        firebaseAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressBar);


        ListView list = findViewById(R.id.list_user);

        // Écouteur pour rechercher dans la liste des amis
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Filtrer la liste à chaque changement de texte
                adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        adapter = new Adapterl(friendList);
        list.setAdapter(adapter);

        // Gestion du clic sur les items de la liste
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                frindes clickedFriend = (frindes) adapter.getItem(position);
                String nom = clickedFriend.getPrenom();
                String idus = clickedFriend.getId();
                Intent intent = new Intent(MainActivity.this, MainActivitymessage.class);
                intent.putExtra("nom", nom);
                intent.putExtra("autherId", idus);
                startActivity(intent);
            }
        });

        // Bouton pour revenir à la page principale
        homme.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, com.example.homepage2.MainActivity.class);
            startActivity(intent);
        });

        // Charger les amis depuis Firebase
        fetchFriends();
    }

    private void fetchFriends() {
        progressBar.setVisibility(View.VISIBLE);

        String currentUserId = firebaseAuth.getCurrentUser().getUid();
        if (currentUserId == null) {
            Log.e("MainActivity", "Utilisateur non authentifié");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Friends")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        friendList.clear(); // Réinitialiser la liste des amis
                        if (task.getResult() != null) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String userId1 = document.getString("userId1");
                                String userId2 = document.getString("userId2");
                                String userName1 = document.getString("userName1");
                                String userName2 = document.getString("userName2");

                                myDataBase = new MyDataBase(this);
                                myDataBase.open();

                                // Récupérer les images pour les deux utilisateurs
                                byte[] profileBytes1 = myDataBase.retrieveImageByUserId(userId1, "profile");
                                byte[] profileBytes2 = myDataBase.retrieveImageByUserId(userId2, "profile");

                                String image1 = null;
                                if (profileBytes1 != null) {
                                    Bitmap profileBitmap1 = BitmapFactory.decodeByteArray(profileBytes1, 0, profileBytes1.length);
                                    image1 = BitmapToBase64(profileBitmap1);
                                    Log.d("Invetations", "Image récupérée avec succès pour l'utilisateur " + userId1);
                                } else {
                                    Log.d("Invetations", "Aucune image trouvée pour l'utilisateur " + userId1);
                                }

                                String image2 = null;
                                if (profileBytes2 != null) {
                                    Bitmap profileBitmap2 = BitmapFactory.decodeByteArray(profileBytes2, 0, profileBytes2.length);
                                    image2 = BitmapToBase64(profileBitmap2);
                                    Log.d("Invetations", "Image récupérée avec succès pour l'utilisateur " + userId2);
                                } else {
                                    Log.d("Invetations", "Aucune image trouvée pour l'utilisateur " + userId2);
                                }

                                // Ajouter un ami si l'utilisateur actuel est l'un des deux
                                if (currentUserId.equals(userId1)) {
                                    friendList.add(new frindes("", userName1, "", "", image2, 0, userId2));
                                } else if  (currentUserId.equals(userId2))  {
                                    friendList.add(new frindes("", userName2, "", "", image1, 0, userId1));
                                }
                            }
                            adapter.notifyDataSetChanged(); // Rafraîchir la liste
                            progressBar.setVisibility(View.GONE);

                        } else {
                            Log.e("MainActivity", "Aucun résultat trouvé dans la collection 'Friends'");
                        }
                    } else {
                        Log.e("MainActivity", "Erreur lors de la récupération des données: " + task.getException().getMessage());
                    }
                });
    }

    public String BitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }


    class Adapterl extends BaseAdapter {
        private ArrayList<frindes> ar;
        private ArrayList<frindes> originalList;

        public Adapterl(ArrayList<frindes> arr) {
            this.ar = arr;
            this.originalList = new ArrayList<>(arr); // Liste originale pour filtrage
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
            LayoutInflater li = getLayoutInflater();
            View v = li.inflate(R.layout.users, null);

            TextView nom = v.findViewById(R.id.username);
            CircleImageView profile = v.findViewById(R.id.photo);

            frindes currentFriend = ar.get(i);
            nom.setText(currentFriend.getNom() + " " + currentFriend.getPrenom()); // Afficher le nom complet

            // Charger l'image depuis Base64 si elle existe
            String base64Image = currentFriend.getImage(); // Assurez-vous que "image" contient une chaîne Base64 valide

            if (base64Image != null && !base64Image.isEmpty()) {
                // Décoder la chaîne Base64 en tableau d'octets
                byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);

                // Convertir le tableau d'octets en Bitmap
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                // Mettre l'image décodée dans le CircleImageView
                profile.setImageBitmap(decodedBitmap);
            }

            return v;
        }

        public android.widget.Filter getFilter() {
            return new android.widget.Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();
                    ArrayList<frindes> filteredList = new ArrayList<>();

                    if (constraint == null || constraint.length() == 0) {
                        filteredList.addAll(originalList); // Pas de filtre appliqué
                    } else {
                        String filterPattern = constraint.toString().toLowerCase().trim();
                        for (frindes item : originalList) {
                            // Vérification de la présence du texte dans le nom et le prénom
                            if ((item.getPrenom() != null && item.getPrenom().toLowerCase().contains(filterPattern))) {
                                filteredList.add(item);
                            }
                        }
                    }

                    results.values = filteredList;
                    results.count = filteredList.size();
                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    ar.clear(); // Vider la liste actuelle
                    if (results.values != null) {
                        ar.addAll((ArrayList<frindes>) results.values); // Ajouter les éléments filtrés
                    }
                    notifyDataSetChanged(); // Mettre à jour l'affichage de la liste
                }
            };
        }
    }
    }


