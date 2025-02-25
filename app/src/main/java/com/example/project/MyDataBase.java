package com.example.project;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.Base64;


import com.example.homepage2.Commentaire;
import com.example.homepage2.Post;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MyDataBase {

    // Nom et version de la base de données
    private static final String DATABASE_NAME = "bublication_data.db";
    private static final int DATABASE_VERSION = 2;

    // Table des images des utilisateurs
    private static final String TABLE_USER_IMAGES = "user_images";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_TYPE = "type"; // 'profile' ou 'cover'
    private static final String COLUMN_IMAGE = "image"; // Contient l'image en BLOB

    // Table des posts
    private static final String TABLE_POSTS = "posts";
    private static final String COLUMN_POST_ID = "id_user";
    private static final String COLUMN_USER_NAME = "user_name";
    private static final String COLUMN_CONTEXT = "context"; // Contenu du post
    private static final String COLUMN_POST_IMAGE = "image"; // Image du post
    private static final String COLUMN_DATE = "date";
    public static final String COLUMN_IS_LIKED = "is_liked";
    private final Context context;
    private DatabaseHelper DBHelper;
    private SQLiteDatabase database;

    // tables des likes :
    private static final String TABLE_LIKES = "likes";
    //table notifications:
    private static final String TABLE_NOTIFICATIONS = "notifications";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_FROM_USER_ID = "from_user_id";
    private static final String COLUMN_FROM_USER_NAME = "from_user_name";
    private static final String COLUMN_TIMESTAMP = "timestamp";

    //table comments :
//    private static final String TABLE_COMMENTS = "comments";
//    private static final String COLUMN_COMMENT_CONTENT = "commentContent";
//


    // Constructeur
    public MyDataBase(Context context) {
        this.context = context;
        DBHelper = new DatabaseHelper(context);
        open();
    }

    // Ouvre la base de données en écriture
    public void open() throws SQLException {
        database = DBHelper.getWritableDatabase();
    }

    // Ferme la base de données
    public void close() {
        DBHelper.close();
    }

    // Méthodes pour la table user_images

    public void insertImage(String userId, String type, byte[] imageBytes) {
        boolean imageExists = checkIfImageExists(userId, type);

        if (imageExists) {
            updateImage(userId, type, imageBytes);
        } else {
            ContentValues values = new ContentValues();
            values.put(COLUMN_USER_ID, userId);
            values.put(COLUMN_TYPE, type);
            values.put(COLUMN_IMAGE, imageBytes);
            database.insert(TABLE_USER_IMAGES, null, values);
        }
    }

    private boolean checkIfImageExists(String userId, String type) {
        Cursor cursor = database.query(TABLE_USER_IMAGES,
                new String[]{COLUMN_IMAGE},
                COLUMN_USER_ID + "=? AND " + COLUMN_TYPE + "=?",
                new String[]{userId, type},
                null, null, null);

        if (cursor != null) {
            boolean exists = cursor.getCount() > 0;
            cursor.close();
            return exists;
        }
        return false;
    }

    public List<Post> getAllPosts() {
        List<Post> posts = new ArrayList<>();
        SQLiteDatabase db = DBHelper.getReadableDatabase(); // Ensure this is correct
        Cursor cursor = db.query(
                TABLE_POSTS,
                new String[]{COLUMN_POST_ID, COLUMN_USER_NAME, COLUMN_CONTEXT, COLUMN_POST_IMAGE, COLUMN_DATE},
                null, // Selection
                null, // Selection args
                null, // Group by
                null, // Having
                COLUMN_DATE + " DESC" // Order by (tri par date décroissante)
        );


        if (cursor != null) {
            // Check if cursor has data
            if (cursor.moveToFirst()) {
                do {
                    // Ensure column index is valid
                    int userIdIndex = cursor.getColumnIndex(COLUMN_POST_ID);
                    int userNameIndex = cursor.getColumnIndex(COLUMN_USER_NAME);
                    int contextIndex = cursor.getColumnIndex(COLUMN_CONTEXT);
                    int imageIndex = cursor.getColumnIndex(COLUMN_POST_IMAGE);
                    int dateIndex = cursor.getColumnIndex(COLUMN_DATE);

                    // Check if indices are valid
                    if (userIdIndex != -1 && userNameIndex != -1 && contextIndex != -1 && imageIndex != -1 && dateIndex != -1) {
                        String userId = cursor.getString(userIdIndex);
                        String userName = cursor.getString(userNameIndex);
                        String context = cursor.getString(contextIndex);
                        byte[] imageBytes = cursor.getBlob(imageIndex);
                        String date = cursor.getString(dateIndex);

                        // Create Post object and add it to the list
                        Post post = new Post(userId, userName, context, imageBytes, date);
                        posts.add(post);
                    } else {
                        Log.e("Database", "Column index is invalid, check your database schema.");
                    }

                } while (cursor.moveToNext());
            } else {
                Log.e("Database", "No posts found.");
            }
            cursor.close();
        } else {
            Log.e("Database", "Cursor is null.");
        }

        return posts;
    }


    private void updateImage(String userId, String type, byte[] imageBytes) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_IMAGE, imageBytes);

        String whereClause = COLUMN_USER_ID + "=? AND " + COLUMN_TYPE + "=?";
        String[] whereArgs = new String[]{userId, type};

        database.update(TABLE_USER_IMAGES, values, whereClause, whereArgs);
    }

    @SuppressLint("Range")
    public byte[] retrieveImageByUserId(String userId, String type) {
        Cursor cursor = null;
        byte[] imageBytes = null;

        try {
            Log.d("MyDataBase", "Querying image for user: " + userId + " with type: " + type);
            cursor = database.query(TABLE_USER_IMAGES,
                    new String[]{COLUMN_IMAGE},
                    COLUMN_USER_ID + "=? AND " + COLUMN_TYPE + "=?",
                    new String[]{userId, type},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                imageBytes = cursor.getBlob(cursor.getColumnIndex(COLUMN_IMAGE));
                Log.d("MyDataBase", "Image found for user: " + userId);
            } else {
                Log.d("MyDataBase", "No image found for user: " + userId);
            }
        } catch (Exception e) {
            Log.e("MyDataBase", "Error retrieving image: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return imageBytes;
    }


    public void deleteImage(String userId, String type) {
        database.delete(TABLE_USER_IMAGES,
                COLUMN_USER_ID + "=? AND " + COLUMN_TYPE + "=?",
                new String[]{userId, type});
    }

    private byte[] convertBitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        return outputStream.toByteArray();
    }

    // Méthodes pour la table posts
    public boolean insertPost(String iduser, String userName, String context, Bitmap imageBytes, String date) {
        byte[] s = convertBitmapToByteArray(imageBytes);
        ContentValues values = new ContentValues();
        values.put(COLUMN_POST_ID, iduser);  // Assurez-vous que l'ID utilisateur est correct ici
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_USER_NAME, userName);
        values.put(COLUMN_CONTEXT, context);
        values.put(COLUMN_POST_IMAGE, s);
        Log.e("Database", "Inserting post with values: " +
                "ID=" + iduser + ", " +
                "Date=" + date + ", " +
                "UserName=" + userName + ", " +
                "Context=" + context + ", " +
                "Image=" + s);

        long result = database.insert(TABLE_POSTS, null, values);
        Log.d("Database", "Insert result: " + result);

        if (result == -1) {
            // Si result == -1, l'insertion a échoué
            return false;
        }
        return true;  // L'insertion a réussi
    }

    public void updatePost(int postId, String context, byte[] imageBytes) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_CONTEXT, context);
        values.put(COLUMN_POST_IMAGE, imageBytes);

        String whereClause = COLUMN_POST_ID + "=?";
        String[] whereArgs = new String[]{String.valueOf(postId)};

        database.update(TABLE_POSTS, values, whereClause, whereArgs);
    }

    public Cursor getPostById(int postId) {
        return database.query(TABLE_POSTS,
                null,
                COLUMN_POST_ID + "=?",
                new String[]{String.valueOf(postId)},
                null, null, null);
    }

    public void deletePostByUserAndDate(String userId, String date) {
        SQLiteDatabase db = DBHelper.getWritableDatabase();
        db.delete(TABLE_POSTS, COLUMN_POST_ID + "=? AND " + COLUMN_DATE + "=?",
                new String[]{userId, date});
    }


    public List<Post> getUserPosts(String userId) {
        List<Post> posts = new ArrayList<>();
        Cursor cursor = database.query(TABLE_POSTS,
                new String[]{COLUMN_POST_ID, COLUMN_USER_NAME, COLUMN_CONTEXT, COLUMN_POST_IMAGE, COLUMN_DATE},
                COLUMN_POST_ID + "=?",
                new String[]{userId},
                null, null, COLUMN_DATE + " DESC");

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") String userName = cursor.getString(cursor.getColumnIndex(COLUMN_USER_NAME));
                    @SuppressLint("Range") String context = cursor.getString(cursor.getColumnIndex(COLUMN_CONTEXT));
                    @SuppressLint("Range") byte[] imageBytes = cursor.getBlob(cursor.getColumnIndex(COLUMN_POST_IMAGE));
                    @SuppressLint("Range") String date = cursor.getString(cursor.getColumnIndex(COLUMN_DATE));

                    Post post = new Post(userId, userName, context, imageBytes, date);
                    posts.add(post);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return posts;
    }

    public List<String> getUserImages(String userId) {
        List<String> imageUrls = new ArrayList<>();
        SQLiteDatabase db = DBHelper.getReadableDatabase();
        Cursor cursor = db.query(TABLE_POSTS,
                new String[]{COLUMN_POST_IMAGE},
                COLUMN_POST_ID + "=?", // Sélectionner uniquement les posts de l'utilisateur
                new String[]{userId},
                null, null, COLUMN_DATE + " DESC"); // Trier par date décroissante

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") byte[] imageBytes = cursor.getBlob(cursor.getColumnIndex(COLUMN_POST_IMAGE));
                    // Convertir les octets de l'image en une URI ou en une chaîne (si les images sont enregistrées sous forme d'URL)
                    String imageUrl = convertImageBytesToUrl(imageBytes);
                    imageUrls.add(imageUrl);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return imageUrls;
    }


    private String convertImageBytesToUrl(byte[] imageBytes) {
        // Ta logique pour convertir les octets d'image en URL ou en chemin local
        return "data:image/jpeg;base64," + Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    //Methodes pour la table likes:
    public void addLike(String userId, String postContent) {
        SQLiteDatabase db = DBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_CONTEXT, postContent);
        db.insertWithOnConflict(TABLE_LIKES, null, values, SQLiteDatabase.CONFLICT_IGNORE); // Utilise CONFLICT_IGNORE pour ignorer les conflits
    }

    public void removeLike(String userId, String postContent) {
        SQLiteDatabase db = DBHelper.getWritableDatabase();
        db.delete(TABLE_LIKES, COLUMN_USER_ID + "=? AND " + COLUMN_CONTEXT + "=?", new String[]{userId, String.valueOf(postContent)});
    }


    public boolean isLiked(String userId, String postContent) {
        SQLiteDatabase db = DBHelper.getReadableDatabase();
        Cursor cursor = db.query(TABLE_LIKES, null, COLUMN_USER_ID + "=? AND " + COLUMN_CONTEXT + "=?", new String[]{userId, String.valueOf(postContent)}, null, null, null);
        boolean liked = cursor.moveToFirst();
        cursor.close();
        return liked;
    }
    public int getCurrentLikes(String postContent) {
        SQLiteDatabase db = DBHelper.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_LIKES + " WHERE "+COLUMN_CONTEXT +"=?";
        Cursor cursor = db.rawQuery(query, new String[]{postContent});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }

    /////////////////////methodes pour la table notifications:
    public void addNotification(String userId, String type, String postContent, String fromUserId, String fromUserName) {

        SQLiteDatabase db = DBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_TYPE, type);
        values.put(COLUMN_CONTEXT, postContent);
        values.put(COLUMN_FROM_USER_ID, fromUserId);
        values.put(COLUMN_FROM_USER_NAME, fromUserName);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String formattedDate = dateFormat.format(new Date());
        values.put(COLUMN_TIMESTAMP, formattedDate);
        db.insert(TABLE_NOTIFICATIONS, null, values);
        db.close();

    }


    public Cursor getNotifications(String userId) {
        SQLiteDatabase db = DBHelper.getReadableDatabase();
        return db.query(TABLE_NOTIFICATIONS, null, COLUMN_USER_ID + "=?", new String[]{userId}, null, null, COLUMN_TIMESTAMP + " DESC");
    }

    @SuppressLint("Range")
    public String getUserProfileImage(String userId) {
        SQLiteDatabase db = DBHelper.getReadableDatabase();
        String profileImage = null;
        Cursor cursor = db.query(TABLE_USER_IMAGES, new String[]{COLUMN_IMAGE}, COLUMN_USER_ID + "=?", new String[]{userId}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            profileImage = cursor.getString(cursor.getColumnIndex(COLUMN_IMAGE));
            cursor.close();
        }
        return profileImage;
    }

    @SuppressLint("Range")
    public String getUserIdByPostContent(String postContent) {
        SQLiteDatabase db = DBHelper.getReadableDatabase();
        Cursor cursor = db.query(TABLE_POSTS,
                new String[]{COLUMN_POST_ID},
                COLUMN_CONTEXT + "=?", // Sélectionner uniquement les posts de l'utilisateur
                new String[]{postContent},
                null, null, null); // Trier par date décroissante

        String userId = null;
        if (cursor != null && cursor.moveToFirst()) {
            userId = cursor.getString(cursor.getColumnIndex(COLUMN_POST_ID));
            cursor.close(); }
        return userId;

    }

    public void deleteNotificationByUserId(String userId) {
        database.delete(TABLE_NOTIFICATIONS,
                COLUMN_USER_ID + " =?",
                new String[]{userId});
    }

//methode pour la table comments:
//public boolean addComment(String userId, String postContent,String commentContent) {
//
//    SQLiteDatabase db = DBHelper.getWritableDatabase();
//    ContentValues values = new ContentValues();
//    values.put(COLUMN_USER_ID, userId);
//
//    values.put(COLUMN_CONTEXT, postContent);
//    values.put(COLUMN_COMMENT_CONTENT, commentContent);
//    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//    String formattedDate = dateFormat.format(new Date());
//    values.put(COLUMN_TIMESTAMP, formattedDate);
//    long result = db.insert(TABLE_COMMENTS, null, values);
//    if (result == -1) {
//        // Si result == -1, l'insertion a échoué
//        return false;
//    }
//    return true;
//}


//    @SuppressLint("Range")
//    public List<Commentaire> getAllCommentsByPostContent(String postContent) {
//        List<Commentaire> comments = new ArrayList<>();
//        SQLiteDatabase db = DBHelper.getReadableDatabase();
//        String query = "SELECT * FROM " + TABLE_COMMENTS + " WHERE " + COLUMN_CONTEXT + " = ?"; // Change COLUMN_POST_ID to COLUMN_POST_CONTENT if needed
//        Cursor cursor = db.rawQuery(query, new String[]{postContent});
//
//        if (cursor.moveToFirst()) {
//            do {
//                Commentaire comment = new Commentaire();
//                comment.setUserId(cursor.getString(cursor.getColumnIndex(COLUMN_USER_ID)));
//                comment.setPost(cursor.getString(cursor.getColumnIndex(COLUMN_CONTEXT))); // Change to COLUMN_POST_CONTENT if needed
//                comment.setContent(cursor.getString(cursor.getColumnIndex(COLUMN_COMMENT_CONTENT)));
//                comment.setDate(cursor.getString(cursor.getColumnIndex(COLUMN_TIMESTAMP)));
//                comments.add(comment);
//            } while (cursor.moveToNext());
//        }
//        cursor.close();
//        db.close();
//
//        return comments;
//    }







    // Classe interne pour gérer la création de la base de données
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d("DatabaseHelper", "Creating tables...");
            // Création de la table des images des utilisateurs
            String CREATE_TABLE_USER_IMAGES = "CREATE TABLE " + TABLE_USER_IMAGES + " ("
                    + COLUMN_USER_ID + " TEXT NOT NULL, "
                    + COLUMN_TYPE + " TEXT NOT NULL, "
                    + COLUMN_IMAGE + " BLOB, "
                    + "PRIMARY KEY (" + COLUMN_USER_ID + ", " + COLUMN_TYPE + "))";

            // Création de la table des posts
            String CREATE_TABLE_POSTS = "CREATE TABLE " + TABLE_POSTS + " ("
                    + COLUMN_POST_ID + " TEXT NOT NULL, "
                    + COLUMN_USER_NAME + " TEXT NOT NULL, "
                    + COLUMN_CONTEXT + " TEXT NOT NULL, " // Permettre les valeurs nulles pour context
                    + COLUMN_DATE + " TEXT NOT NULL, "   // Permettre les valeurs nulles pour date
                    + COLUMN_POST_IMAGE + " BLOB DEFAULT NULL)"; // Permettre les valeurs nulles pour image


            String CREATE_TABLE_LIKES = "CREATE TABLE "+ TABLE_LIKES + " ("
                    + COLUMN_USER_ID+ " TEXT NOT NULL, "
                    +COLUMN_CONTEXT + " TEXT NOT NULL, "
                    +"PRIMARY KEY (" + COLUMN_USER_ID + ", " + COLUMN_CONTEXT + "))";
            String CREATE_NOTIFICATIONS_TABLE = "CREATE TABLE " + TABLE_NOTIFICATIONS + "(" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_USER_ID + " TEXT NOT NULL, " +
                    COLUMN_TYPE + " TEXT NOT NULL, " +
                    COLUMN_CONTEXT + " TEXT NOT NULL, " +
                    COLUMN_FROM_USER_ID + " TEXT NOT NULL, " +
                    COLUMN_FROM_USER_NAME + " TEXT NOT NULL, " +
                    COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP" + ")";


//            String CREATE_COMMENTS_TABLE = "CREATE TABLE " + TABLE_COMMENTS + "(" +
//                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
//                    COLUMN_USER_ID + " TEXT NOT NULL, " +
//                    COLUMN_CONTEXT + " TEXT NOT NULL, " +
//                    COLUMN_COMMENT_CONTENT + " TEXT NOT NULL, "+
//                    COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP" + ")";
//

            db.execSQL(CREATE_TABLE_USER_IMAGES);
            db.execSQL(CREATE_TABLE_POSTS);
            db.execSQL(CREATE_TABLE_LIKES);
            db.execSQL(CREATE_NOTIFICATIONS_TABLE);
//            db.execSQL(CREATE_COMMENTS_TABLE);

            Log.d("DatabaseHelper", "Tables created successfully.");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.d("DatabaseHelper", "Upgrading database...");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_IMAGES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_POSTS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_LIKES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATIONS);
//            db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMMENTS);

            onCreate(db);

            Log.d("DatabaseHelper", "Database upgraded successfully.");
        }
    }
}