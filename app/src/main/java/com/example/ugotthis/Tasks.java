//SQLite code, now abandoned in favour of Firebase
//Code is saved for posterity and is unused anywhere
/*package com.example.ugotthis;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Tasks extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "GotThis";
    private static final String TABLE_TASKS = "task";
    private static final String KEY_TID = "tid";
    private static final String KEY_UID = "uid";
    private static final String KEY_NAME = "name";
    private static final String KEY_DESCP = "description";
    private static final String KEY_COMP = "complete";

    public Tasks(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    public void addTask(Task task)
    {
        SQLiteDatabase db = this.getReadableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TID, task.gettId());
        values.put(KEY_UID, task.getuId());
        values.put(KEY_NAME, task.getName());
        values.put(KEY_DESCP, task.getDescp());
        values.put(KEY_COMP, false);

        db.insert(TABLE_TASKS, null, values);
        db.close();
    }

    Task getTask(String id)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TASKS, new String[]{KEY_TID, KEY_UID, KEY_NAME, KEY_DESCP, KEY_COMP},
                KEY_TID + "=?", new String[] {String.valueOf(id)}, null, null, null, null);
        if(cursor.getCount() == 0)
        {
            Task task = new Task("-1", "-1", "", "", false);
            return task;
        }
        else
        {
            cursor.moveToFirst();
            Task task = new Task(cursor.getString(0), cursor.getString(1), cursor.getString(2),
                    cursor.getString(3), cursor.getInt(4) > 0);
            return task;
        }
    }

    public List<Task> getAllTasks(String id)
    {
        List<Task> taskList = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_TASKS + " WHERE " + KEY_UID + " = ?";
        String[] args = {id};

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, args);

        if(cursor.moveToFirst())
        {
            do{
                Task task = new Task(cursor.getString(0), cursor.getString(1), cursor.getString(2),
                        cursor.getString(3), cursor.getInt(4) > 0);
                taskList.add(task);
            }while(cursor.moveToNext());
            Collections.sort(taskList, new Comparator<Task>() {
                @Override
                public int compare(Task task, Task t1) {
                    return Boolean.compare(task.getComp(), t1.getComp());
                }
            });
            return taskList;
        }
        else
        {
            Task task = new Task("-1", "-1", "Nice", "No new tasks!", false);
            taskList.add(task);
            return taskList;
        }
    }

    public int updateTask(Task task)
    {
        SQLiteDatabase db = this.getReadableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, task.getName());
        values.put(KEY_DESCP, task.getDescp());

        return db.update(TABLE_TASKS, values, KEY_TID + " =?",
                new String[] { task.gettId() });
    }

    public void updateTaskComp(Boolean comp, String tid)
    {
        SQLiteDatabase db = this.getReadableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_COMP, comp);
        db.update(TABLE_TASKS, values, KEY_TID + " =?", new String[] {tid});
    }

    public void deleteTask(Task task)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        db.delete(TABLE_TASKS, KEY_TID + " =?",
                new String[] { task.gettId() });

    }
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}*/
