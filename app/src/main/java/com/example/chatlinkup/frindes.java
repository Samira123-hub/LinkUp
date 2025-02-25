package com.example.chatlinkup;

import java.util.HashMap;
import java.util.Map;

public class frindes {
    public String id, nom, prenom, enligne, messaj, image;
    public int nbmessaj;

    public frindes(String n, String b, String en, String sms, String img, int a, String id  ) {
        this.nom = n;
        this.prenom = b;
        this.nbmessaj = a;
        this.enligne = en;
        this.messaj = sms;
        this.image = img;
        this.id=id;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("nom", this.nom);
        map.put("prenom", this.prenom);
        map.put("profileImage", this.image);

        return map;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }
}

