package com.example.slouch_patrol_app.Helpers;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "slouchpatrol.db";
    private static final int DATABASE_VERSION = 1;

    // User Table
    private static final String USER_TABLE = "users";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";

    // Posture Table
    private static final String POSTURE_TABLE = "posture_scores";
    private static final String COLUMN_POSTURE_ID = "posture_id";
    private static final String COLUMN_USER_ID_FK = "user_id_fk";  // Foreign key from user table
    private static final String COLUMN_SCORE = "score";
    private static final String COLUMN_TIMESTAMP = "timestamp";

    // Activity Log Table
    private static final String ACTIVITY_LOG_TABLE = "activity_log";
    private static final String COLUMN_ACTIVITY_ID = "activity_id";
    private static final String COLUMN_USER_ID_FK_ACTIVITY = "user_id_fk_activity";  // Foreign key from user table
    private static final String COLUMN_SERIALIZED_ACTIVITY = "serialized_activity";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static String getUserIdColumn() {
        return COLUMN_USER_ID;
    }

    public static String getUserTable() {
        return USER_TABLE;
    }

    public static String getUsernameColumn() {
        return COLUMN_USERNAME;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create User Table
        String createUserTable = "CREATE TABLE " + USER_TABLE + "(" +
                COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_USERNAME + " TEXT NOT NULL," +
                COLUMN_PASSWORD + " TEXT NOT NULL)";
        db.execSQL(createUserTable);

        // Create Posture Score Table
        String createPostureTable = "CREATE TABLE " + POSTURE_TABLE + "(" +
                COLUMN_POSTURE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_USER_ID_FK + " INTEGER," +
                COLUMN_SCORE + " REAL," +
                COLUMN_TIMESTAMP + " TEXT," +
                "FOREIGN KEY(" + COLUMN_USER_ID_FK + ") REFERENCES " + USER_TABLE + "(" + COLUMN_USER_ID + "))";
        db.execSQL(createPostureTable);

        // Create Activity Log Table
        String createActivityLogTable = "CREATE TABLE " + ACTIVITY_LOG_TABLE + "(" +
                COLUMN_ACTIVITY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_USER_ID_FK_ACTIVITY + " INTEGER," +
                COLUMN_SERIALIZED_ACTIVITY + " TEXT," +
                "FOREIGN KEY(" + COLUMN_USER_ID_FK_ACTIVITY + ") REFERENCES " + USER_TABLE + "(" + COLUMN_USER_ID + "))";
        db.execSQL(createActivityLogTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + USER_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + POSTURE_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + ACTIVITY_LOG_TABLE);
        onCreate(db);
    }

    public boolean insertUser(String username, String password) {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return false; // Return false if inputs are invalid
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_USERNAME, username);
        contentValues.put(COLUMN_PASSWORD, password);

        long result = db.insert(USER_TABLE, null, contentValues);
        return result != -1;
    }


    public boolean verifyUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        //Update the query to check against the username and password
        Cursor cursor = db.rawQuery("SELECT * FROM " + USER_TABLE + " WHERE " + COLUMN_USERNAME + "=? AND " + COLUMN_PASSWORD + "=?", new String[]{username, password});

        //If no user is found, return false
        if (cursor.getCount() > 0) {
            cursor.close();
            return true; // User found
        }

        cursor.close();
        return false; // User not found
    }

    //Add posture score
    public boolean addPostureScore(int userID, float score, String timestamp) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_USER_ID_FK, userID);
        contentValues.put(COLUMN_SCORE, score);
        contentValues.put(COLUMN_TIMESTAMP, timestamp);

        long result = db.insert(POSTURE_TABLE, null, contentValues);
        return result != -1;
    }

    public boolean addPostureScoreForCurrentUser(String username, float score, String timestamp) {
        // Get the current user ID using the username
        int userID = getUserIdByUsername(username);

        // If the user is found (userID is greater than 0), add the posture score
        if (userID > 0) {
            return addPostureScore(userID, score, timestamp);
        } else {
            return false; // Return false if user is not found
        }
    }


    //Retrieve posture scores for a user
    public Cursor getPostureScores(int userID) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(POSTURE_TABLE, null, COLUMN_USER_ID_FK + "=?",
                new String[]{String.valueOf(userID)}, null, null, COLUMN_TIMESTAMP + " DESC");
    }

    //Retrieve posture scores for a specific user by username
    public Cursor getPostureScoresByUserIDCursor(int userID) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                POSTURE_TABLE,                    //The table to query
                null,                             //The columns to return (null means all columns)
                COLUMN_USER_ID_FK + " = ?",       //The WHERE clause to filter by user_id_fk
                new String[]{String.valueOf(userID)},  //The actual value for the WHERE clause
                null,                             //Don't group the rows
                null,                             //Don't filter by row groups
                COLUMN_TIMESTAMP + " DESC");      //Order by the most recent score
    }

    public int[] getPostureScoresByUserID(int userID) {
        // Get query cursor
        Cursor cursor = getPostureScoresByUserIDCursor(userID);

        // init array of scores
        int[] postureScores = new int[cursor.getCount()];

        // populate array
        for (int i = 0; i < postureScores.length; i++) {
            postureScores[i++] = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SCORE));
        }

        // return values
        return postureScores;
    }

    @SuppressLint("Range")
    public Integer getUserIdByUsernameAndPassword(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_USER_ID + " FROM " + USER_TABLE +
                        " WHERE " + COLUMN_USERNAME + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{username, password});

        if (cursor != null && cursor.moveToFirst()) {

            int userID = 0;
            if(cursor.getColumnIndex(COLUMN_USER_ID) > -1)
            {
                userID = cursor.getInt(cursor.getColumnIndex(COLUMN_USER_ID));
            }

            cursor.close();
            return userID;
        }

        if (cursor != null) {
            cursor.close();
        }
        return 0; // Return 0 if user is not found or credentials are incorrect
    }

    @SuppressLint("Range")
    public Integer getUserIdByUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_USER_ID + " FROM " + USER_TABLE +
                        " WHERE " + COLUMN_USERNAME + "=?",
                new String[]{username});

        if (cursor != null && cursor.moveToFirst()) {

            int userID = 0;
            if (cursor.getColumnIndex(COLUMN_USER_ID) > -1) {
                userID = cursor.getInt(cursor.getColumnIndex(COLUMN_USER_ID));
            }

            cursor.close();
            return userID;
        }

        if (cursor != null) {
            cursor.close();
        }
        return 0; // Return 0 if user is not found
    }

   /* public boolean insertUser(String username, String password) {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return false; // Return false if inputs are invalid
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_USERNAME, username);
        contentValues.put(COLUMN_PASSWORD, password);

        long result = db.insert(USER_TABLE, null, contentValues);
        return result != -1;
    }
*/

    public boolean insertActivity(int userID, String sessionDataJSON) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_USER_ID_FK_ACTIVITY, userID);
        contentValues.put(COLUMN_SERIALIZED_ACTIVITY, sessionDataJSON);

        long result = db.insert(ACTIVITY_LOG_TABLE, null, contentValues);
        return result != -1;
    }



}
