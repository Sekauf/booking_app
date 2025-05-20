import 'dart:convert';
import 'package:flutter/foundation.dart' show kIsWeb;
import 'package:flutter/services.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../models/reservation.dart';

class DatabaseService {
  static final DatabaseService _instance = DatabaseService._internal();
  factory DatabaseService() => _instance;
  DatabaseService._internal();

  // Name des MethodChannels für Mobile
  static const MethodChannel _channel = MethodChannel('restaurant_booking_db');

  /// Lädt alle Reservierungen
  Future<List<Reservation>> getAllReservations() async {
    if (kIsWeb) {
      // === Web-Fallback via SharedPreferences ===
      final prefs = await SharedPreferences.getInstance();
      final list = prefs.getStringList('reservations') ?? <String>[];
      return list
          .map((e) => Reservation.fromMap(jsonDecode(e) as Map<String, dynamic>))
          .toList();
    }

    // === Mobile (Android/iOS) via MethodChannel ===
    try {
      final List<dynamic>? raw = await _channel.invokeMethod<List>('getAllReservations');
      if (raw == null) return <Reservation>[];
      return raw
          .map((item) => Reservation.fromMap(Map<String, dynamic>.from(item)))
          .toList();
    } on PlatformException catch (e) {
      print('DB-Error: ${e.message}');
      return <Reservation>[];
    }
  }

  /// Speichert alle Reservierungen (nur für Web)
  Future<void> _saveAllWeb(List<Reservation> all) async {
    final prefs = await SharedPreferences.getInstance();
    final encoded = all.map((r) => jsonEncode(r.toMap())).toList();
    await prefs.setStringList('reservations', encoded);
  }

  Future<void> addReservation(Reservation reservation) async {
    if (kIsWeb) {
      final all = await getAllReservations();
      all.add(reservation);
      await _saveAllWeb(all);
      return;
    }
    await _channel.invokeMethod('addReservation', reservation.toMap());
  }

  Future<void> updateReservation(Reservation reservation) async {
    if (kIsWeb) {
      final all = await getAllReservations();
      final idx = all.indexWhere((r) => r.id == reservation.id);
      if (idx != -1) {
        all[idx] = reservation;
        await _saveAllWeb(all);
      }
      return;
    }
    await _channel.invokeMethod('updateReservation', reservation.toMap());
  }

  Future<void> deleteReservation(int id) async {
    if (kIsWeb) {
      final all = await getAllReservations();
      all.removeWhere((r) => r.id == id);
      await _saveAllWeb(all);
      return;
    }
    await _channel.invokeMethod('deleteReservation', {'id': id});
  }

  Future<bool> hasConflictingReservation(Reservation reservation) async {
    if (kIsWeb) {
      final all = await getAllReservations();
      return all.any((r) =>
      r.table == reservation.table &&
          r.dateTime == reservation.dateTime &&
          r.id != reservation.id
      );
    }
    final bool? result =
    await _channel.invokeMethod<bool>('checkConflict', reservation.toMap());
    return result ?? false;
  }
}
