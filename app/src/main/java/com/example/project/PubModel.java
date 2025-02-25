package com.example.project;
public class PubModel {
    private String userId;
    private String userName;
    private String context;
    private String Date;
    private byte[] imageBytes;

    public PubModel(String userId, String userName, String context, byte[] imageBytes,String date) {
        this.userId = userId;
        this.userName = userName;
        this.context = context;
        this.imageBytes = imageBytes;
        this.Date=date;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getContext() {
        return context;
    }

    public byte[] getImageBytes() {
        return imageBytes;
    }

    public String getPostDate() {
        return Date;
    }
}
