
package com.example.homepage2;
import android.graphics.Bitmap;
public class Commentaire {
    private Bitmap profileBitmap;
    private String userName;
    private String content;
    private String date;

    public Commentaire(Bitmap profileBitmap, String userName, String content, String date) {
        this.profileBitmap = profileBitmap;
        this.userName = userName;
        this.content = content;
        this.date = date;
    }

    public Bitmap getProfileBitmap() {
        return profileBitmap;
    }

    public String getUserName() {
        return userName;
    }

    public String getContent() {
        return content;
    }

    public String getDate() {
        return date;
    }
}
