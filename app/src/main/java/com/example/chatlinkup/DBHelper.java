package com.example.chatlinkup;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "chatlinkup.db";
    private static final int DATABASE_VERSION = 3;  // Incrémentez la version pour déclencher onUpgrade()

    public static final String TABLE_USERS = "users";
    public static final String TABLE_FRIENDS = "friends";
    public static final String TABLE_MESSAGES = "messages";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NOM = "nom";
    public static final String COLUMN_PRENOM = "prenom";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_ENLIGNE = "enligne";

    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_FRIEND_NOM = "friend_nom";
    public static final String COLUMN_FRIEND_PRENOM = "friend_prenom";
    public static final String COLUMN_FRIEND_IMAGE = "friend_image";
    public static final String COLUMN_FRIEND_MESSAGE = "lastmessage";
    public static final String COLUMN_SENDER_ID = "sender_id";
    public static final String COLUMN_RECEIVER_ID = "receiver_id";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Table des utilisateurs
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_NOM + " TEXT, "
                + COLUMN_PRENOM + " TEXT, "
                + COLUMN_EMAIL + " TEXT, "
                + COLUMN_ENLIGNE + " TEXT)";
        db.execSQL(CREATE_USERS_TABLE);

        // Table des amis
        String CREATE_FRIENDS_TABLE = "CREATE TABLE " + TABLE_FRIENDS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_USER_ID + " INTEGER, "
                + COLUMN_FRIEND_NOM + " TEXT, "
                + COLUMN_FRIEND_PRENOM + " TEXT, "
                + COLUMN_FRIEND_IMAGE + " TEXT, "
                + COLUMN_FRIEND_MESSAGE + " TEXT, "
                + "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + "))";
        db.execSQL(CREATE_FRIENDS_TABLE);

        // Table des messages
        String CREATE_MESSAGES_TABLE = "CREATE TABLE " + TABLE_MESSAGES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_SENDER_ID + " INTEGER, "
                + COLUMN_RECEIVER_ID + " INTEGER, "
                + COLUMN_CONTENT + " TEXT, "
                + COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP, "
                + "FOREIGN KEY(" + COLUMN_SENDER_ID + ") REFERENCES " + TABLE_FRIENDS+ "(" + COLUMN_ID + "), "
                + "FOREIGN KEY(" + COLUMN_RECEIVER_ID + ") REFERENCES " + TABLE_FRIENDS + "(" + COLUMN_ID + "))";
        db.execSQL(CREATE_MESSAGES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Supprimer les anciennes tables si elles existent
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FRIENDS);  // Supprime la table friends
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES); // Supprime la table messages
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS); // Supprime la table users

        // Créer les tables à partir de zéro
        onCreate(db);
    }

    // Ajouter un utilisateur
    public long addUser(String nom, String prenom, String email, String enligne) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOM, nom);
        values.put(COLUMN_PRENOM, prenom);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_ENLIGNE, enligne);

        long userId = db.insert(TABLE_USERS, null, values);
        db.close();
        return userId;
    }

    // Ajouter un message
    public void addMessage(int senderId, int receiverId, String content) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SENDER_ID, senderId);
        values.put(COLUMN_RECEIVER_ID, receiverId);
        values.put(COLUMN_CONTENT, content);
        values.put(COLUMN_TIMESTAMP, System.currentTimeMillis()); // Assure un timestamp en millisecondes
        db.insert(TABLE_MESSAGES, null, values);
        db.close();
    }

    // Ajouter un ami
    public void addFriend(int userId, String friendNom, String friendPrenom, String friendImage, String message) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_FRIEND_NOM, friendNom);
        values.put(COLUMN_FRIEND_PRENOM, friendPrenom);
        values.put(COLUMN_FRIEND_IMAGE, friendImage);
        values.put(COLUMN_FRIEND_MESSAGE, message);

        db.insert(TABLE_FRIENDS, null, values);
        db.close();
    }

    // Récupérer les amis d'un utilisateur
    public Cursor getFriends(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_FRIENDS + " WHERE " + COLUMN_USER_ID + " = ?";
        return db.rawQuery(query, new String[]{String.valueOf(userId)});
    }

    // Récupérer les messages entre deux utilisateurs
    public Cursor getMessages(int userId1, int userId2) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_MESSAGES + " WHERE "
                + "(" + COLUMN_SENDER_ID + " = ? AND " + COLUMN_RECEIVER_ID + " = ?) "
                + "OR (" + COLUMN_SENDER_ID + " = ? AND " + COLUMN_RECEIVER_ID + " = ?) "
                + "ORDER BY " + COLUMN_TIMESTAMP + " ASC";
        return db.rawQuery(query, new String[]{
                String.valueOf(userId1),
                String.valueOf(userId2),
                String.valueOf(userId2),
                String.valueOf(userId1)
        });
    }

    // Supprimer une table spécifique
    public void dropTable(String tableName) {
        SQLiteDatabase db = this.getWritableDatabase();
        String dropQuery = "DROP TABLE IF EXISTS " + tableName;
        db.execSQL(dropQuery);  // Exécution de la commande pour supprimer la table
        db.close();  // Fermer la base de données après la suppression
    }


}
