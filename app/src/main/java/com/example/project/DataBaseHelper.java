//package com.example.project;
//
//import android.content.ContentValues;
//import android.content.Context;
//import android.database.Cursor;
//import android.database.sqlite.SQLiteDatabase;
//import android.database.sqlite.SQLiteOpenHelper;
//
//import androidx.annotation.Nullable;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class DataBaseHelper extends SQLiteOpenHelper {
//
//    public static final String DATABASE_NAME = "Profil.db";
//    public static final int DATABASE_VERSION = 1;
//    public static final String TABLE_NAME = "publications";
//    public static final String ID = "id";
//    public static final String TEXT = "text";
//    public static final String IMAGE = "image";
//    public static final String DATE = "postedDate";
//
//    public DataBaseHelper(@Nullable Context context) {
//        super(context, DATABASE_NAME, null, DATABASE_VERSION);
//    }
//
//    @Override
//    public void onCreate(SQLiteDatabase db) {
//        // Create the publications table
//        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
//                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
//                TEXT + " TEXT, " +
//                IMAGE + " BLOB, " +
//                DATE + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
//                ")";
//        db.execSQL(CREATE_TABLE);
//    }
//
//    @Override
//    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        // Drop the existing table and recreate it
//        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
//        onCreate(db);
//    }
//
//    public void insertPub(PubModel model) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put(TEXT, model.getText());
//        values.put(IMAGE, model.getImage()); // Store as byte[] (BLOB)
//        values.put(DATE, model.getPostedDate());
//        db.insert(TABLE_NAME, null, values);
//        db.close(); // Close database connection
//    }
//
//    public void updatePub(int id, byte[] image, String text) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put(IMAGE, image);
//        values.put(TEXT, text);
//        db.update(TABLE_NAME, values, "id=?", new String[]{String.valueOf(id)});
//        db.close(); // Close database connection
//    }
//
//    public void deletePub(int id) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        db.delete(TABLE_NAME, "id=?", new String[]{String.valueOf(id)});
//        db.close(); // Close database connection
//    }
//    public byte[] retrieveImageByUserId(String userId, String type) {
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor cursor = null;
//        byte[] imageBytes = null;
//
//        try {
//            // Requête pour récupérer l'image en fonction de l'ID utilisateur et du type
//            cursor = db.rawQuery(
//                    "SELECT image FROM images WHERE user_id = ? AND type = ?",
//                    new String[]{userId, type}
//            );
//
//            // Vérifiez si un résultat est disponible
//            if (cursor != null && cursor.moveToFirst()) {
//                // Récupérez les octets de l'image
//                imageBytes = cursor.getBlob(cursor.getColumnIndex("image"));
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (cursor != null) {
//                cursor.close();
//            }
//        }
//
//        return imageBytes;
//    }
//
//
//    public List<PubModel> getAllPublication() {
//        SQLiteDatabase db = this.getWritableDatabase();
//        List<PubModel> modelList = new ArrayList<>();
//        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
//
//        // Ensure the cursor has data before accessing it
//        if (cursor != null && cursor.moveToFirst()) {
//            do {
//                PubModel pub = new PubModel();
//
//                // Use getColumnIndex to safely fetch the column index
//                int idIndex = cursor.getColumnIndex(ID);
//                int textIndex = cursor.getColumnIndex(TEXT);
//                int imageIndex = cursor.getColumnIndex(IMAGE);
//                int dateIndex = cursor.getColumnIndex(DATE);
//
//                if (idIndex != -1) pub.setId(cursor.getInt(idIndex));
//                if (textIndex != -1) pub.setText(cursor.getString(textIndex));
//                if (imageIndex != -1) pub.setImage(cursor.getString(imageIndex));
//                if (dateIndex != -1) pub.setPostedDate(cursor.getString(dateIndex));
//
//                modelList.add(pub);
//            } while (cursor.moveToNext());
//            cursor.close(); // Close the cursor after use
//        }
//        db.close(); // Close database connection
//        return modelList;
//    }
//
//}
//
//
