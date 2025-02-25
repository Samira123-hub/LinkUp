package com.example.homepage2;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class CommentAdapter extends ArrayAdapter<Commentaire> {

    public CommentAdapter(Context context, List<Commentaire> commentaires) {
        super(context, R.layout.list_item_comment, commentaires);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            // Charger la mise en page de l'élément de liste
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_comment, parent, false);
        }

        // Récupérer l'élément de commentaire actuel
        Commentaire commentaire = getItem(position);

        // Lier les vues
        ImageView userImageView = convertView.findViewById(R.id.imageView2);
        TextView userNameTextView = convertView.findViewById(R.id.userComment);
        TextView commentTextView = convertView.findViewById(R.id.textComment);
        TextView commentdate = convertView.findViewById(R.id.textdate);

        if (commentaire != null) {
            // Remplir les données
            // Si l'image est un Bitmap, utilisez setImageBitmap()
            Bitmap userImageBitmap = commentaire.getProfileBitmap();  // Assurez-vous que getUserImage() renvoie un Bitmap
            if (userImageBitmap != null) {
                userImageView.setImageBitmap(userImageBitmap);
            } else {
                // Si l'image est nulle, vous pouvez mettre une image par défaut
                userImageView.setImageResource(R.drawable.default_profile); // Utilisez une image par défaut si nécessaire
            }

            userNameTextView.setText(commentaire.getUserName());
            commentTextView.setText(commentaire.getContent());
            commentdate.setText(commentaire.getDate());
        }

        return convertView;
    }
}
