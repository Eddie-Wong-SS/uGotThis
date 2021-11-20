//package com.example.ugotthis;
//Legacy code, SQLite database code for storing user data, abandoned in favour of Firebase
//Code is saved for posterity
/*import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

class Users extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "GotThis";
    private static final String TABLE_USER = "users";
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_PASS = "password";
    private static final String TABLE_TASKS = "task";
    private static final String KEY_TID = "tid";
    private static final String KEY_UID = "uid";
    private static final String KEY_DESCP = "description";
    private static final String KEY_COMP = "complete";

    public Users(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_USERS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_USER + "("
                + KEY_ID + " TEXT PRIMARY KEY  NOT NULL," + KEY_NAME + " TEXT NOT NULL,"
                + KEY_PASS + " TEXT NOT NULL" + ")";
        sqLiteDatabase.execSQL(CREATE_USERS_TABLE);

        String CREATE_TASKS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_TASKS + " ( "
                + KEY_TID + " TEXT PRIMARY KEY NOT NULL," + KEY_UID + " TEXT ," + KEY_NAME + " TEXT NOT NULL,"
                + KEY_DESCP + " TEXT NOT NULL," +  KEY_COMP + " BOOLEAN " + ")";
        Log.w("SQL", CREATE_TASKS_TABLE);
        sqLiteDatabase.execSQL(CREATE_TASKS_TABLE);

    }

    // code to add the new contact
    void addUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, user.getId());
        values.put(KEY_NAME, user.getUser());
        values.put(KEY_PASS, user.getPassword());

        // Inserting Row
        db.insert(TABLE_USER, null, values);
        //2nd argument is String containing nullColumnHack
        db.close(); // Closing database connection
    }

    User getUser(String id, String pass)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USER, new String[] { KEY_ID,
                        KEY_NAME, KEY_PASS }, KEY_NAME + "=? AND " +KEY_PASS + "=?",
                new String[] { id, pass }, null, null, null, null);
        if (cursor.getCount() != 0)
        {
            cursor.moveToFirst();
            User user = new User(cursor.getString(0),cursor.getString(1), cursor.getString(2));
            return user;
        }
        else
        {
            User user = new User("-1", "", "");
            return user;
        }

    }

    public List<User> getAllUserss() {
        List<User> userList = new ArrayList<User>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_USER;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                User user = new User();
                user.setUser(cursor.getString(0));
                user.setUser(cursor.getString(1));
                user.setPass(cursor.getString(2));
                userList.add(user);
            } while (cursor.moveToNext());
        }
        return userList;
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
} */
