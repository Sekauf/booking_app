package com.example.offline_booking_app;

import androidx.annotation.NonNull;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugins.GeneratedPluginRegistrant;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;

public class MainActivity extends FlutterActivity {
    private static final String CHANNEL = "offline_booking_app";
    private ReservationDbHelper dbHelper;

    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        // Register any generated plugins (if any)
        GeneratedPluginRegistrant.registerWith(flutterEngine);
        // Set up the MethodChannel for database operations
        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
                .setMethodCallHandler(new MethodCallHandler() {
                    @Override
                    public void onMethodCall(MethodCall call, Result result) {
                        // Initialize the database helper if not already
                        if (dbHelper == null) {
                            dbHelper = new ReservationDbHelper(MainActivity.this);
                        }
                        switch (call.method) {
                            case "insertResource": {
                                String name = call.argument("name");
                                if (name == null) {
                                    result.error("INVALID", "Resource name is null", null);
                                    return;
                                }
                                long newId = dbHelper.insertResource(name);
                                if (newId == -1) {
                                    result.error("DB_ERROR", "Failed to insert resource", null);
                                } else {
                                    result.success(newId);
                                }
                                break;
                            }
                            case "getResources": {
                                java.util.List<java.util.Map<String, Object>> resources = dbHelper.getAllResources();
                                result.success(resources);
                                break;
                            }
                            case "insertReservation": {
                                Number resIdNum = call.argument("resource_id");
                                Number dateNum = call.argument("date");
                                String description = call.argument("description");
                                if (resIdNum == null || dateNum == null) {
                                    result.error("INVALID", "Missing arguments for reservation", null);
                                    return;
                                }
                                int resourceId = resIdNum.intValue();
                                long dateMillis = dateNum.longValue();
                                long newId = dbHelper.insertReservation(resourceId, dateMillis, description != null ? description : "");
                                if (newId == -1) {
                                    result.error("DB_ERROR", "Failed to insert reservation", null);
                                } else {
                                    result.success(newId);
                                }
                                break;
                            }
                            case "getReservations": {
                                java.util.List<java.util.Map<String, Object>> reservations = dbHelper.getAllReservations();
                                result.success(reservations);
                                break;
                            }
                            case "deleteReservation": {
                                Number idNum = call.argument("id");
                                if (idNum == null) {
                                    result.error("INVALID", "Missing reservation id", null);
                                    return;
                                }
                                int id = idNum.intValue();
                                boolean success = dbHelper.deleteReservation(id);
                                result.success(success);
                                break;
                            }
                            case "deleteResource": {
                                Number idNum = call.argument("id");
                                if (idNum == null) {
                                    result.error("INVALID", "Missing resource id", null);
                                    return;
                                }
                                int id = idNum.intValue();
                                boolean success = dbHelper.deleteResource(id);
                                result.success(success);
                                break;
                            }
                            default:
                                result.notImplemented();
                                break;
                        }
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
