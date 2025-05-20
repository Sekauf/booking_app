package com.example.booking_app;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugins.GeneratedPluginRegistrant;
import io.flutter.plugin.common.MethodChannel;
import java.util.ArrayList;
import java.util.Map;

public class MainActivity extends FlutterActivity {
    private static final String CHANNEL = "restaurant_booking_db";

    @Override
    public void configureFlutterEngine(FlutterEngine flutterEngine) {
        GeneratedPluginRegistrant.registerWith(flutterEngine);
        
        final ReservationDbHelper dbHelper = new ReservationDbHelper(getContext());

        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
            .setMethodCallHandler((call, result) -> {
                try {
                    switch (call.method) {
                        case "getAllReservations":
                            result.success(dbHelper.getAllReservations());
                            break;
                        case "addReservation":
                            @SuppressWarnings("unchecked")
                            Map<String, Object> reservation = (Map<String, Object>) call.arguments;
                            long id = dbHelper.addReservation(reservation);
                            result.success(id);
                            break;
                        case "updateReservation":
                            @SuppressWarnings("unchecked")
                            Map<String, Object> updateRes = (Map<String, Object>) call.arguments;
                            int rows = dbHelper.updateReservation(updateRes);
                            result.success(rows > 0);
                            break;
                        case "deleteReservation":
                            int delId = call.argument("id");
                            dbHelper.deleteReservation(delId);
                            result.success(null);
                            break;
                        case "checkConflict":
                            @SuppressWarnings("unchecked")
                            Map<String, Object> checkRes = (Map<String, Object>) call.arguments;
                            boolean hasConflict = dbHelper.hasConflict(checkRes);
                            result.success(hasConflict);
                            break;
                        default:
                            result.notImplemented();
                            break;
                    }
                } catch (Exception e) {
                    result.error("ERROR", e.getMessage(), null);
                }
            });
    }
}