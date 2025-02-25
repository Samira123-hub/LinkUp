package com.example.homepage2;

import static java.security.AccessController.getContext;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.homepage2.CommentBottomSheetFragment;
import com.example.homepage2.Post;
import com.example.homepage2.R;
import com.example.project.MyDataBase;
import com.example.project.Profil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class PostAdapter extends ArrayAdapter<Post> {
    private final AppCompatActivity activity;
    String userId;
    private MyDataBase myDataBase;

    // Constructor to initialize the adapter
    public PostAdapter(AppCompatActivity activity, List<Post> posts) {
        super(activity, R.layout.list_item_post, posts); // Correct constructor usage
        this.activity = activity;
        this.myDataBase = new MyDataBase(activity);
        this.myDataBase.open();
    }

    public class ViewHolder {
        ImageView userImageView;
        TextView userNameTextView;
        TextView postDateTextView;
        TextView contentTextView;
        ImageView postImageView;
        LinearLayout likeLayout;
        TextView likeText;
        ImageView likeIcon;
        LinearLayout commentButton;
        ImageView deletePost;
        TextView numberLike;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Use ViewHolder to optimize findViewById
        ViewHolder viewHolder;


        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_post, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.userImageView = convertView.findViewById(R.id.imageView2);
            viewHolder.userNameTextView = convertView.findViewById(R.id.postUserName);
            viewHolder.postDateTextView = convertView.findViewById(R.id.postDate);
            viewHolder.contentTextView = convertView.findViewById(R.id.postContent);
            viewHolder.postImageView = convertView.findViewById(R.id.postImage);
            viewHolder.likeLayout = convertView.findViewById(R.id.likeLayout);
            viewHolder.likeText = convertView.findViewById(R.id.likeText);
            viewHolder.likeIcon = convertView.findViewById(R.id.likeIcon);
            viewHolder.commentButton = convertView.findViewById(R.id.commentIcon);
            viewHolder.deletePost = convertView.findViewById(R.id.deletePost);
            viewHolder.numberLike = convertView.findViewById(R.id.numberLike);

            convertView.setTag(viewHolder); // Set the ViewHolder as a tag
        } else {
            viewHolder = (ViewHolder) convertView.getTag(); // Reuse ViewHolder
        }

        Post post = getItem(position);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String postUserId = post.getUserId();
            Log.d("PostAdapter","PostuserId : "+postUserId );


            // Set image bytes for post image

            myDataBase = new MyDataBase(activity);
            myDataBase.open();
            byte[] imageBytesuser = myDataBase.retrieveImageByUserId(postUserId, "profile");
            Log.d("PostAdapter","imageBytesuser : "+imageBytesuser );

//            myDataBase.close(); // Assure-toi de fermer la base de données après utilisation

            if (imageBytesuser != null) {
                Bitmap postBitmap = BitmapFactory.decodeByteArray(imageBytesuser, 0, imageBytesuser.length);
                viewHolder.userImageView.setImageBitmap(postBitmap);
            }
        } else {
            viewHolder.userImageView.setImageResource(R.drawable.default_profile); // Image par défaut si aucune image n'est trouvée
        }

        if (post != null) {
            // Set image bytes for post image
            byte[] imageBytes = post.getImageBytes();
            if (imageBytes != null) {
                Bitmap postBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                viewHolder.postImageView.setImageBitmap(postBitmap);
            }


            if (user != null) {

                String postUserId = post.getUserId();
                // Récupérer les données de l'utilisateur depuis Firestore
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("Users").document(postUserId)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    // Récupérer le nom de l'utilisateur depuis la collection
                                    String name = document.getString("name"); // Récupérer le champ "name" du document

                                    // Afficher le nom de l'utilisateur dans le TextView
                                    viewHolder.userNameTextView.setText(name);
                                } else {
                                    viewHolder.userNameTextView.setText("Utilisateur introuvable");
                                }
                            } else {
                                System.out.println("Erreur lors de la récupération des données de l'utilisateur : " + task.getException().getMessage());
                                viewHolder.userNameTextView.setText("Erreur de récupération");
                            }
                        });
            } else {
                // L'utilisateur n'est pas connecté
                viewHolder.userNameTextView.setText("Utilisateur non connecté");
            }


            // Set text fields for the post

            viewHolder.postDateTextView.setText(post.getPostDate());
            viewHolder.contentTextView.setText(post.getContext());

            String currentUserId = user.getUid();

            // Récupération de l'état actuel du like
            boolean isLiked = myDataBase.isLiked(currentUserId,viewHolder.contentTextView.getText().toString());
            int currentLikes = myDataBase.getCurrentLikes(viewHolder.contentTextView.getText().toString());

// Mise à jour de l'UI en fonction de l'état actuel du like
            Drawable background = ContextCompat.getDrawable(activity, R.drawable.rounded_corners_like);
            if (background instanceof GradientDrawable) {
                GradientDrawable drawable = (GradientDrawable) background;
                if (isLiked) {
                    drawable.setColor(Color.parseColor("#FF7F7F"));
                    viewHolder.likeText.setTextColor(Color.parseColor("#FFFFFF"));
                    viewHolder.numberLike.setTextColor(Color.parseColor("#FFFFFF"));
                    viewHolder.likeIcon.setImageResource(R.drawable.aimant);
                } else {
                    drawable.setColor(Color.parseColor("#FFFFFF"));
                    viewHolder.likeText.setText("Like");
                    viewHolder.likeText.setTextColor(Color.parseColor("#a0a29e"));
                    viewHolder.numberLike.setTextColor(Color.parseColor("#a0a29e"));
                    viewHolder.likeIcon.setImageResource(R.drawable.grislike);
                }
                viewHolder.likeLayout.setBackground(drawable);
                viewHolder.numberLike.setText(String.valueOf(currentLikes));

            }


            class LikeCounter {
                int currentLikes;
                LikeCounter(int initialLikes){
                    this.currentLikes = initialLikes;
                }
            }
            class IsLiked {
                boolean isLiked;
                IsLiked(boolean isLiked){
                    this.isLiked = isLiked;
                }
            }
            LikeCounter likeCounter = new LikeCounter(currentLikes);
            IsLiked isLiked1 = new IsLiked(isLiked);

// Gestion de l'événement de clic sur le bouton like
            viewHolder.likeLayout.setOnClickListener(v -> {
                String postContent = viewHolder.contentTextView.getText().toString();
                String postOwnerId = myDataBase.getUserIdByPostContent(viewHolder.contentTextView.getText().toString());


                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("Users").document(currentUserId)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    // Récupérer le nom de l'utilisateur depuis la collection
                                    String currentUserName = document.getString("name");

                                    if (isLiked1.isLiked) {
                                        myDataBase.removeLike(currentUserId, postContent);
                                        likeCounter.currentLikes = Math.max(0, likeCounter.currentLikes - 1); // Éviter les valeurs négatives
                                        isLiked1.isLiked = false;
                                    } else {
                                        myDataBase.addLike(currentUserId, postContent);
                                        likeCounter.currentLikes++;
                                        if(!currentUserId.equals(postOwnerId)) {
                                            myDataBase.addNotification(postOwnerId, "like", postContent, currentUserId, currentUserName);
                                        }
                                        isLiked1.isLiked = true;
                                    }

                                    if (background instanceof GradientDrawable) {
                                        GradientDrawable drawable = (GradientDrawable) background;
                                        if (isLiked1.isLiked) {
                                            drawable.setColor(Color.parseColor("#FF7F7F"));
                                            viewHolder.likeText.setTextColor(Color.parseColor("#FFFFFF"));
                                            viewHolder.numberLike.setTextColor(Color.parseColor("#FFFFFF"));
                                            viewHolder.likeIcon.setImageResource(R.drawable.aimant);
                                        } else {
                                            drawable.setColor(Color.parseColor("#FFFFFF"));
                                            viewHolder.likeText.setText("Like");
                                            viewHolder.likeText.setTextColor(Color.parseColor("#a0a29e"));
                                            viewHolder.numberLike.setTextColor(Color.parseColor("#a0a29e"));
                                            viewHolder.likeIcon.setImageResource(R.drawable.grislike);
                                        }
                                        viewHolder.likeLayout.setBackground(drawable);
                                        viewHolder.numberLike.setText(String.valueOf(likeCounter.currentLikes));

                                    }

                                }
                            }
                        });
            });


            // Handle Comment Button Click
            // viewHolder.commentButton.setOnClickListener(v -> {
            // Show comment bottom sheet
            //   CommentBottomSheetFragment bottomSheetFragment = new CommentBottomSheetFragment();
            // bottomSheetFragment.show(activity.getSupportFragmentManager(), bottomSheetFragment.getTag());
            //});
            //}
            viewHolder.commentButton.setOnClickListener(v -> {
                // Récupérez le contenu du texte du post actuel
                String postContent = viewHolder.contentTextView.getText().toString(); // Remplacez par la méthode appropriée pour obtenir le texte

                // Créez une instance de CommentBottomSheetFragment
                CommentBottomSheetFragment bottomSheetFragment = new CommentBottomSheetFragment();

                // Ajoutez le texte du post comme argument
                Bundle args = new Bundle();
                args.putString("postContent", postContent);
                bottomSheetFragment.setArguments(args);

                // Affichez le BottomSheetFragment
                bottomSheetFragment.show(activity.getSupportFragmentManager(), bottomSheetFragment.getTag());
            });


            //handle delete post:////////////////////////////////////////
            viewHolder.deletePost.setOnClickListener(new View.OnClickListener(){
                public void onClick(View v){
                    PopupMenu popupMenu = new PopupMenu(activity,viewHolder.deletePost);
                    popupMenu.getMenuInflater().inflate(R.menu.menu_deletenotification,popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            if(menuItem.getItemId() == R.id.action_delete){
                                String postDate = viewHolder.postDateTextView.getText().toString();
                                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                                Log.d("PostAdapter", "User ID: " + userId);
                                Log.d("PostAdapter", "Post Date: " + postDate);

                                try {

                                    myDataBase.deletePostByUserAndDate(userId, postDate);

                                    remove(post);
                                    notifyDataSetChanged();

                                    Toast.makeText(getContext(), "Post supprimé", Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    Log.e("PostAdapter", "Error deleting post", e);
                                }

                                return true;
                            }
                            return false;
                        }
                    });
                    popupMenu.show();
                }
            });






        }
        return convertView;
    }

}
