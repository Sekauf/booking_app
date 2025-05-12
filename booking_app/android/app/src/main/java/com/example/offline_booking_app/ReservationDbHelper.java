package com.example.offline_booking_app;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReservationDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "reservations.db";
    private static final int DATABASE_VERSION = 1;

    // Table names
    public static final String TABLE_RESOURCES = "resources";
    public static final String TABLE_RESERVATIONS = "reservations";
    // Columns for resources
    public static final String COL_RES_ID = "id";
    public static final String COL_RES_NAME = "name";
    // Columns for reservations
    public static final String COL_RESERV_ID = "id";
    public static final String COL_RESERV_RESOURCE_ID = "resource_id";
    public static final String COL_RESERV_DATE = "date";
    public static final String COL_RESERV_DESC = "description";

    public ReservationDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // Enable foreign key constraints
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create resources table
        String createResources = "CREATE TABLE " + TABLE_RESOURCES + " (" +
                COL_RES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_RES_NAME + " TEXT NOT NULL" +
                ")";
        // Create reservations table with foreign key reference to resources
        String createReservations = "CREATE TABLE " + TABLE_RESERVATIONS + " (" +
                COL_RESERV_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_RESERV_RESOURCE_ID + " INTEGER NOT NULL, " +
                COL_RESERV_DATE + " INTEGER NOT NULL, " +
                COL_RESERV_DESC + " TEXT, " +
                "FOREIGN KEY(" + COL_RESERV_RESOURCE_ID + ") REFERENCES " + TABLE_RESOURCES + "(" + COL_RES_ID + ") ON DELETE CASCADE" +
                ")";
        db.execSQL(createResources);
        db.execSQL(createReservations);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop existing tables and recreate (simple strategy for now)
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RESERVATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RESOURCES);
        onCreate(db);
    }

    public long insertResource(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_RES_NAME, name);
        return db.insert(TABLE_RESOURCES, null, values);
    }

    public long insertReservation(int resourceId, long dateMillis, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_RESERV_RESOURCE_ID, resourceId);
        values.put(COL_RESERV_DATE, dateMillis);
        values.put(COL_RESERV_DESC, description);
        return db.insert(TABLE_RESERVATIONS, null, values);
    }

    public boolean deleteReservation(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_RESERVATIONS, COL_RESERV_ID + " = ?", new String[]{String.valueOf(id)});
        return rows > 0;
    }

    public boolean deleteResource(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_RESOURCES, COL_RES_ID + " = ?", new String[]{String.valueOf(id)});
        return rows > 0;
    }

    public List<Map<String, Object>> getAllResources() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Map<String, Object>> resultList = new ArrayList<>();
        Cursor cursor = db.query(TABLE_RESOURCES, new String[]{COL_RES_ID, COL_RES_NAME},
                null, null, null, null, COL_RES_NAME + " ASC");
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    Map<String, Object> map = new HashMap<>();
                    int idIndex = cursor.getColumnIndexOrThrow(COL_RES_ID);
                    int nameIndex = cursor.getColumnIndexOrThrow(COL_RES_NAME);
                    map.put("id", cursor.getInt(idIndex));
                    map.put("name", cursor.getString(nameIndex));
                    resultList.add(map);
                }
            } finally {
                cursor.close();
            }
        }
        return resultList;
    }

    public List<Map<String, Object>> getAllReservations() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Map<String, Object>> resultList = new ArrayList<>();
        String query = "SELECT res." + COL_RESERV_ID + " AS id, " +
                "res." + COL_RESERV_RESOURCE_ID + " AS resource_id, " +
                "res." + COL_RESERV_DATE + " AS date, " +
                "res." + COL_RESERV_DESC + " AS description, " +
                "r." + COL_RES_NAME + " AS resource_name " +
                "FROM " + TABLE_RESERVATIONS + " res " +
                "JOIN " + TABLE_RESOURCES + " r " +
                "ON res." + COL_RESERV_RESOURCE_ID + " = r." + COL_RES_ID + " " +
                "ORDER BY res." + COL_RESERV_DATE + " ASC";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor != null) {
            try {
                int idIndex = cursor.getColumnIndexOrThrow("id");
                int resIdIndex = cursor.getColumnIndexOrThrow("resource_id");
                int dateIndex = cursor.getColumnIndexOrThrow("date");
                int descIndex = cursor.getColumnIndexOrThrow("description");
                int nameIndex = cursor.getColumnIndexOrThrow("resource_name");
                while (cursor.moveToNext()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", cursor.getInt(idIndex));
                    map.put("resource_id", cursor.getInt(resIdIndex));
                    map.put("date", cursor.getLong(dateIndex));
                    // Handle description which may be null
                    if (!cursor.isNull(descIndex)) {
                        map.put("description", cursor.getString(descIndex));
                    } else {
                        map.put("description", "");
                    }
                    // Resource name from joined table
                    map.put("resource_name", cursor.getString(nameIndex));
                    resultList.add(map);
                }
            } finally {
                cursor.close();
            }
        }
        return resultList;
    }
}
