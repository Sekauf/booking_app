package com.example.booking_app;  // Geändertes Package

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ReservationDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "reservations.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_RESERVATIONS = "reservations";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_TABLE = "tableNumber";
    private static final String COLUMN_DATETIME = "dateTime";
    private static final String COLUMN_START = "start";
    private static final String COLUMN_END = "end";

    public ReservationDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_RESERVATIONS_TABLE = "CREATE TABLE " + TABLE_RESERVATIONS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAME + " TEXT NOT NULL,"
                + COLUMN_TABLE + " INTEGER NOT NULL,"
                + COLUMN_DATETIME + " INTEGER NOT NULL,"
                + COLUMN_START + " INTEGER NOT NULL,"
                + COLUMN_END + " INTEGER NOT NULL"
                + ")";
        db.execSQL(CREATE_RESERVATIONS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RESERVATIONS);
        onCreate(db);
    }

    public long addReservation(Map<String, Object> reservation) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COLUMN_NAME, (String) reservation.get("name"));
        values.put(COLUMN_TABLE, (Integer) reservation.get("tableNumber"));
        values.put(COLUMN_DATETIME, (Long) reservation.get("dateTime"));
        values.put(COLUMN_START, (Long) reservation.get("start"));
        values.put(COLUMN_END, (Long) reservation.get("end"));

        long id = db.insert(TABLE_RESERVATIONS, null, values);
        db.close();
        return id;
    }

    public ArrayList<Map<String, Object>> getAllReservations() {
        ArrayList<Map<String, Object>> reservations = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_RESERVATIONS;
        
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Map<String, Object> reservation = new HashMap<>();
                reservation.put(COLUMN_ID, cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
                reservation.put(COLUMN_NAME, cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
                reservation.put(COLUMN_TABLE, cursor.getInt(cursor.getColumnIndex(COLUMN_TABLE)));
                reservation.put(COLUMN_DATETIME, cursor.getLong(cursor.getColumnIndex(COLUMN_DATETIME)));
                reservation.put(COLUMN_START, cursor.getLong(cursor.getColumnIndex(COLUMN_START)));
                reservation.put(COLUMN_END, cursor.getLong(cursor.getColumnIndex(COLUMN_END)));
                reservations.add(reservation);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return reservations;
    }

    public int updateReservation(Map<String, Object> reservation) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_NAME, (String) reservation.get("name"));
        values.put(COLUMN_TABLE, (Integer) reservation.get("tableNumber"));
        values.put(COLUMN_DATETIME, (Long) reservation.get("dateTime"));
        values.put(COLUMN_START, (Long) reservation.get("start"));
        values.put(COLUMN_END, (Long) reservation.get("end"));

        int rowsAffected = db.update(TABLE_RESERVATIONS, values,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(reservation.get("id"))});
        db.close();
        return rowsAffected;
    }

    public void deleteReservation(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_RESERVATIONS, COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();
    }

    public boolean hasConflict(Map<String, Object> reservation) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_RESERVATIONS +
                " WHERE " + COLUMN_TABLE + " = ? AND " +
                COLUMN_DATETIME + " = ? AND " +
                COLUMN_ID + " != ?";

        Cursor cursor = db.rawQuery(query, new String[]{
                String.valueOf(reservation.get("tableNumber")),
                String.valueOf(reservation.get("dateTime")),
                String.valueOf(reservation.get("id") != null ? reservation.get("id") : -1)
        });

        boolean hasConflict = false;
        if (cursor.moveToFirst()) {
            hasConflict = cursor.getInt(0) > 0;
        }

        cursor.close();
        db.close();
        return hasConflict;
    }
}