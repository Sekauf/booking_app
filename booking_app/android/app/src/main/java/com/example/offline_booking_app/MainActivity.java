package com.deinpaketname;  // passe das an dein Package an

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodChannel;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FlutterActivity {
    private static final String CHANNEL = "restaurant_booking_db";
    private DatabaseHelper dbHelper;

    @Override
    public void configureFlutterEngine(FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);
        dbHelper = new DatabaseHelper(getApplicationContext());

        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
                .setMethodCallHandler((call, result) -> {
                    switch (call.method) {
                        case "getAllReservations":
                            result.success(getAllReservations());
                            break;
                        case "addReservation":
                            addReservation((Map<String, Object>) call.arguments());
                            result.success(null);
                            break;
                        case "updateReservation":
                            updateReservation((Map<String, Object>) call.arguments());
                            result.success(null);
                            break;
                        case "deleteReservation":
                            deleteReservation((int) call.argument("id"));
                            result.success(null);
                            break;
                        case "checkConflict":
                            boolean conflict = checkConflict((Map<String, Object>) call.arguments());
                            result.success(conflict);
                            break;
                        default:
                            result.notImplemented();
                    }
                });
    }

    // Liest alle Reservierungen aus
    private List<Map<String,Object>> getAllReservations() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("reservations", null, null, null, null, null, "datetime");
        List<Map<String,Object>> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            Map<String,Object> m = new HashMap<>();
            m.put("id", cursor.getInt(cursor.getColumnIndex("id")));
            m.put("name", cursor.getString(cursor.getColumnIndex("name")));
            m.put("table", cursor.getInt(cursor.getColumnIndex("table")));
            m.put("datetime", cursor.getString(cursor.getColumnIndex("datetime")));
            list.add(m);
        }
        cursor.close();
        return list;
    }

    // Fügt eine neue Reservierung ein
    private void addReservation(Map<String,Object> args) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", (String) args.get("name"));
        cv.put("table", (Integer) args.get("table"));
        cv.put("datetime", (String) args.get("datetime"));
        db.insert("reservations", null, cv);
    }

    // Aktualisiert eine bestehende Reservierung
    private void updateReservation(Map<String,Object> args) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", (String) args.get("name"));
        cv.put("table", (Integer) args.get("table"));
        cv.put("datetime", (String) args.get("datetime"));
        int id = (Integer) args.get("id");
        db.update("reservations", cv, "id=?", new String[]{String.valueOf(id)});
    }

    // Löscht eine Reservierung per ID
    private void deleteReservation(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("reservations", "id=?", new String[]{String.valueOf(id)});
    }

    // Prüft auf Konflikt (Doppelbuchung)
    private boolean checkConflict(Map<String,Object> args) {
        int table = (int) args.get("table");
        String datetime = (String) args.get("datetime");
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                "reservations",
                new String[]{"id"},
                "table=? AND datetime=?",
                new String[]{String.valueOf(table), datetime},
                null, null, null
        );
        boolean conflict = cursor.getCount() > 0;
        cursor.close();
        return conflict;
    }

    // Helper-Klasse für SQLite
    private static class DatabaseHelper extends SQLiteOpenHelper {
        private static final String DB_NAME = "booking_app.db";
        private static final int DB_VERSION = 1;

        DatabaseHelper(Context ctx) {
            super(ctx, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS reservations (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "name TEXT," +
                            "table INTEGER," +
                            "datetime TEXT" +
                            ")"
            );
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
            // Hier DB-Migrationen bei Versionswechsel
        }
    }
}
