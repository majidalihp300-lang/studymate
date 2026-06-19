package com.example;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "StudyMate.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_TASKS = "tasks";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_STATUS = "status"; // "pending" or "completed"

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TASKS_TABLE = "CREATE TABLE " + TABLE_TASKS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TITLE + " TEXT NOT NULL,"
                + COLUMN_DESCRIPTION + " TEXT,"
                + COLUMN_DATE + " TEXT,"
                + COLUMN_STATUS + " TEXT NOT NULL DEFAULT 'pending'"
                + ")";
        db.execSQL(CREATE_TASKS_TABLE);

        // Prepopulate with a few sample tasks so the student app doesn't look barren on submission
        db.execSQL("INSERT INTO " + TABLE_TASKS + " (" + COLUMN_TITLE + ", " + COLUMN_DESCRIPTION + ", " + COLUMN_DATE + ", " + COLUMN_STATUS + ") VALUES ('Complete Math Assignment', 'Solve Algebra exercise 4 and Chapter 5 problems.', '2026-06-21', 'pending')");
        db.execSQL("INSERT INTO " + TABLE_TASKS + " (" + COLUMN_TITLE + ", " + COLUMN_DESCRIPTION + ", " + COLUMN_DATE + ", " + COLUMN_STATUS + ") VALUES ('Prepare Physics Lab Report', 'Write the conclusion and draw graphs for the Optics lab.', '2026-06-22', 'pending')");
        db.execSQL("INSERT INTO " + TABLE_TASKS + " (" + COLUMN_TITLE + ", " + COLUMN_DESCRIPTION + ", " + COLUMN_DATE + ", " + COLUMN_STATUS + ") VALUES ('Read History Seminar Notes', 'Review WWII European theater summaries.', '2026-06-18', 'completed')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        onCreate(db);
    }

    // --- CRUD: CREATE ---
    public long addTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, task.getTitle());
        values.put(COLUMN_DESCRIPTION, task.getDescription());
        values.put(COLUMN_DATE, task.getDate());
        values.put(COLUMN_STATUS, task.getStatus());

        long id = db.insert(TABLE_TASKS, null, values);
        db.close();
        return id;
    }

    // --- CRUD: READ (Single Task) ---
    public Task getTask(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TASKS,
                new String[]{COLUMN_ID, COLUMN_TITLE, COLUMN_DESCRIPTION, COLUMN_DATE, COLUMN_STATUS},
                COLUMN_ID + "=?", new String[]{String.valueOf(id)},
                null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            Task task = new Task(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS))
            );
            cursor.close();
            db.close();
            return task;
        }
        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return null;
    }

    // --- CRUD: READ (All or Filtered/Searched) ---
    public List<Task> getAllTasks() {
        return getFilteredTasks("", "All");
    }

    public List<Task> getFilteredTasks(String searchKeyword, String statusFilter) {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Construct query dynamic based on filters
        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM " + TABLE_TASKS + " WHERE 1=1");
        List<String> argsList = new ArrayList<>();

        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            queryBuilder.append(" AND (").append(COLUMN_TITLE).append(" LIKE ? OR ").append(COLUMN_DESCRIPTION).append(" LIKE ?)");
            argsList.add("%" + searchKeyword + "%");
            argsList.add("%" + searchKeyword + "%");
        }

        if (statusFilter != null && !statusFilter.equals("All")) {
            queryBuilder.append(" AND ").append(COLUMN_STATUS).append(" = ?");
            argsList.add(statusFilter.toLowerCase());
        }

        // Sort by date (completed tasks at bottom, pending and upcoming on top)
        queryBuilder.append(" ORDER BY CASE WHEN ").append(COLUMN_STATUS).append("='pending' THEN 0 ELSE 1 END, ").append(COLUMN_DATE).append(" ASC");

        String[] selectionArgs = argsList.toArray(new String[0]);
        Cursor cursor = db.rawQuery(queryBuilder.toString(), selectionArgs);

        if (cursor.moveToFirst()) {
            do {
                Task task = new Task(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS))
                );
                taskList.add(task);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return taskList;
    }

    // --- CRUD: UPDATE ---
    public int updateTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, task.getTitle());
        values.put(COLUMN_DESCRIPTION, task.getDescription());
        values.put(COLUMN_DATE, task.getDate());
        values.put(COLUMN_STATUS, task.getStatus());

        int result = db.update(TABLE_TASKS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(task.getId())});
        db.close();
        return result;
    }

    // --- CRUD: DELETE ---
    public int deleteTask(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_TASKS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return result;
    }

    // --- DATABASE MAINTENANCE (Reset) ---
    public void resetDatabase() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_TASKS);
        db.close();
    }

    // --- AGGREGATIONS FOR DASHBOARD ---
    public int getTasksCount(String status) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_TASKS;
        String[] selectionArgs = null;
        if (status != null && !status.equalsIgnoreCase("all")) {
            query += " WHERE " + COLUMN_STATUS + " = ?";
            selectionArgs = new String[]{status.toLowerCase()};
        }
        Cursor cursor = db.rawQuery(query, selectionArgs);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }
}
