package com.example.chatlinkup;

import com.example.chatlinkup.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.homepage2.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Message> messages;
    private String currentUserId;

    public MessageAdapter(List<Message> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        if (viewType == 0) { // Message reçu
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recever_message, parent, false);
        } else { // Message envoyé
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.send_messege, parent, false);
        }
        return new MessageViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        Message message = messages.get(position);

        holder.messageText.setText(message.getContent());
        String formattedDate = formatTimestamp(message.getTimestamp());
        holder.timestamp.setText(formattedDate);
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        return message.getSenderId().equals(currentUserId) ? 1 : 0; // 1 = envoyé, 0 = reçu
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, timestamp;

        public MessageViewHolder(View view) {
            super(view);
            messageText = view.findViewById(R.id.message);
            timestamp = view.findViewById(R.id.time);
        }
    }

    private String formatTimestamp(long timestamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return dateFormat.format(new Date(timestamp));
    }
}
