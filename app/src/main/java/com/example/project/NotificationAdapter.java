package com.example.project;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.homepage2.R;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<Notification> notifications;
    private OnItemClickListener listener;
    private final AppCompatActivity activity;
    private MyDataBase myDataBase ;

    public interface OnItemClickListener {
        void onItemClick(Notification notification);
    }

    public NotificationAdapter(List<Notification> notifications,AppCompatActivity activity) {
        this.activity = activity;
        this.notifications = notifications;
        myDataBase = new MyDataBase(activity);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.userName.setText(notification.getFromUserName());
        holder.notificationText.setText(notification.getMessage());
        try{
            String originalTimestamp = notification.getTimestamp(); // Par exemple, "2024-12-30 19:17:30"
            SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm"); // Format sans secondes
            Date date = originalFormat.parse(originalTimestamp);
            String formattedDate = targetFormat.format(date);
            holder.date.setText(formattedDate);
        }catch (Exception e){
            e.printStackTrace();
            holder.date.setText(notification.getTimestamp());
        }

        // Load profile picture (if you have a method to load images)
        // For example: holder.profilePic.setImageResource(R.drawable.default_profile_pic);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = user.getUid();
        if (user != null) {
            String notifUserId = notification.getFromUserId();
            Log.d("NotificationAdapter", notifUserId);


            // Set image bytes for post image
            if (notifUserId != null) {
                MyDataBase db = new MyDataBase(activity);
                byte[] imageBytesuser = db.retrieveImageByUserId(notifUserId, "profile");
                Log.d("NotificationAdapter", "Image bytes for user " + notifUserId + ": " + Arrays.toString(imageBytesuser));
//                myDataBase.close(); // Assure-toi de fermer la base de données après utilisation

                if (imageBytesuser != null) {
                    Bitmap notifBitmap = BitmapFactory.decodeByteArray(imageBytesuser, 0, imageBytesuser.length);
                    if (notifBitmap != null) {
                        holder.profilePic.setImageBitmap(notifBitmap);
                        Log.d("NotificationAdapter", "Image set for user: " + notifUserId);
                    } else {
                        Log.e("NotificationAdapter", "Failed to decode image for user: " + notifUserId);
                    }
                }else{
                    Log.d("NotificationAdapter", "No Image found for user: " + notifUserId);

                }
            }
        }

        holder.more.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                PopupMenu popupMenu = new PopupMenu(activity,holder.more);
                popupMenu.getMenuInflater().inflate(R.menu.menu_deletenotification,popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if(menuItem.getItemId() == R.id.action_delete){
                            myDataBase.deleteNotificationByUserId(currentUserId);
                            notifications.remove(holder.getAdapterPosition());
                            notifyItemRemoved(holder.getAdapterPosition());
                            notifyItemRangeChanged(holder.getAdapterPosition(), notifications.size());
                            Toast.makeText(activity,"notification deleted!", Toast.LENGTH_SHORT).show();
                            return true;
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(notification);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ShapeableImageView profilePic;
        public TextView userName;
        public TextView notificationText;
        public TextView date;
        public ImageView more;

        public ViewHolder(View itemView) {
            super(itemView);
            profilePic = itemView.findViewById(R.id.profilpic);
            userName = itemView.findViewById(R.id.user);
            notificationText = itemView.findViewById(R.id.notification);
            date = itemView.findViewById(R.id.date);
            more = itemView.findViewById(R.id.more);
        }
    }
}