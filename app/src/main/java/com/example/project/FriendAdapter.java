package com.example.project;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatlinkup.frindes;
import com.example.homepage2.R;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.MyViewHolder> {
    private List<frindes> friendList;
    private Context context;

    public FriendAdapter(List<frindes> friendList, Context context) {
        this.friendList = friendList;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_friend, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        frindes friend = friendList.get(position);

        // Conversion de l'image Base64 en Bitmap
        Bitmap decodedBitmap = base64ToBitmap(friend.getImage());
        if (decodedBitmap != null) {
            holder.profilPic.setImageBitmap(decodedBitmap);
        } else {
            holder.profilPic.setImageResource(R.drawable.default_profile);
        }

        // Définir le nom de l'utilisateur
        holder.user.setText(friend.getPrenom());

        // Définir l'écouteur pour le bouton "more"
        holder.more.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, holder.more);
            popupMenu.getMenuInflater().inflate(R.menu.menu_friends, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {

                        if (item.getItemId() == R.id.action_voirprofil) {
                            Toast.makeText(context, "See Profile selected", Toast.LENGTH_SHORT).show();

                            return true;
                        }

                        Toast.makeText(context, "Block selected", Toast.LENGTH_SHORT).show();

                        return false;

        });
            popupMenu.show();
        });
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    public void updateData(List<frindes> newFriendList) {
        this.friendList = newFriendList;
        notifyDataSetChanged();
    }

    // Méthode utilitaire pour convertir Base64 en Bitmap
    public Bitmap base64ToBitmap(String base64Image) {
        if (base64Image == null || base64Image.isEmpty()) {
            return null;
        }
        byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }

    // Méthode pour bloquer l'utilisateur (à implémenter selon votre logique)
    private void blockUser(String userId) {
        // Ajoutez ici la logique pour bloquer l'utilisateur
        // Par exemple, vous pouvez appeler une méthode dans la base de données pour bloquer l'utilisateur
        Toast.makeText(context, "User " + userId + " blocked", Toast.LENGTH_SHORT).show();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView profilPic;
        TextView user;
        ImageView more;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            profilPic = itemView.findViewById(R.id.profilpic);
            user = itemView.findViewById(R.id.friend);
            more = itemView.findViewById(R.id.more);
        }
    }
}