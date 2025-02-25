package com.example.project;

public class Notification {
    private String user_id;
    private String type;
    private String post_content;
    private String from_user_id;
    private String from_user_name;
    private String timestamp;

    public Notification() {
        // Default constructor required for calls to DataSnapshot.getValue(Notification.class)
    }

    public Notification(String user_id, String type, String post_content, String from_user_id, String from_user_name, String timestamp) {
        this.user_id = user_id;
        this.type = type;
        this.post_content = post_content;
        this.from_user_id = from_user_id;
        this.from_user_name = from_user_name;
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return user_id;
    }

    public void setUserId(String user_id) {
        this.user_id = user_id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPostContent() {
        return post_content;
    }

    public void setPostContent(String post_content) {
        this.post_content = post_content;
    }

    public String getFromUserId() {
        return from_user_id;
    }

    public void setFromUserId(String from_user_id) {
        this.from_user_id = from_user_id;
    }

    public String getFromUserName() {
        return from_user_name;
    }

    public void setFromUserName(String from_user_name) {
        this.from_user_name = from_user_name;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        switch (type) {
            case "like":
                return "liked your post.";
            case "comment":
                return from_user_name + " commented on your post.";
            case "follow":
                return "started following you.";
            default:
                return "You have a new notification.";
        }
    }
}
