package com.example.project;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.homepage2.CommentBottomSheetFragment;
import com.example.homepage2.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class PubAdapter extends ArrayAdapter<PubModel> {
    private final AppCompatActivity activity;
    private final FirebaseFirestore db;

    public PubAdapter(AppCompatActivity activity, List<PubModel> posts) {
        super(activity, R.layout.list_item_post, posts);
        this.activity = activity;
        this.db = FirebaseFirestore.getInstance();
    }

    public static class ViewHolder {
        ImageView userImageView;
        TextView userNameTextView;
        TextView postDateTextView;
        TextView contentTextView;
        ImageView postImageView;
        LinearLayout likeLayout;
        TextView likeText;
        ImageView likeIcon;
        LinearLayout commentButton;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
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

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        PubModel post = getItem(position);
        if (post != null) {
            String userId = post.getUserId();

            // Set user image
            MyDataBase myDataBase = new MyDataBase(getContext());
            myDataBase.open();
            byte[] imageBytesuser = myDataBase.retrieveImageByUserId(userId, "profile");
            myDataBase.close(); // N'oublie pas de fermer la base de données après utilisation

            if (imageBytesuser != null) {
                Bitmap postBitmap = BitmapFactory.decodeByteArray(imageBytesuser, 0, imageBytesuser.length);
                viewHolder.userImageView.setImageBitmap(postBitmap);
            }

            // Retrieve and set user name from Firestore
            db.collection("Users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String userName = documentSnapshot.getString("name");
                            viewHolder.userNameTextView.setText(userName);
                        } else {
                            viewHolder.userNameTextView.setText("Unknown");
                        }
                    })
                    .addOnFailureListener(e -> {
                        viewHolder.userNameTextView.setText("Error");
                        Log.e("PubAdapter", "Error fetching user name", e);
                    });

            // Set post image
            byte[] imageBytes = post.getImageBytes();
            if (imageBytes != null) {
                Bitmap postBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                viewHolder.postImageView.setImageBitmap(postBitmap);
            }

            viewHolder.postDateTextView.setText(post.getPostDate());
            viewHolder.contentTextView.setText(post.getContext());

            // Handle Like Button State
            final boolean[] isLiked = {false};
            viewHolder.likeLayout.setOnClickListener(v -> {
                Drawable background = ContextCompat.getDrawable(activity, R.drawable.rounded_corners_like);
                if (background instanceof GradientDrawable) {
                    GradientDrawable drawable = (GradientDrawable) background;
                    if (isLiked[0]) {
                        drawable.setColor(Color.parseColor("#FFFFFF"));
                        viewHolder.likeText.setText("Like");
                        viewHolder.likeText.setTextColor(Color.parseColor("#a0a29e"));
                        viewHolder.likeIcon.setImageResource(R.drawable.grislike);
                    } else {
                        viewHolder.likeText.setText("Like");
                        drawable.setColor(Color.parseColor("#FF7F7F"));
                        viewHolder.likeText.setTextColor(Color.parseColor("#FFFFFF"));
                        viewHolder.likeIcon.setImageResource(R.drawable.aimant);
                    }
                    viewHolder.likeLayout.setBackground(drawable);
                    isLiked[0] = !isLiked[0];
                }
            });

            // Handle Comment Button Click
            viewHolder.commentButton.setOnClickListener(v -> {
                CommentBottomSheetFragment bottomSheetFragment = new CommentBottomSheetFragment();
                bottomSheetFragment.show(activity.getSupportFragmentManager(), bottomSheetFragment.getTag());
            });
        }

        return convertView;
    }

}
