import 'dart:convert';
import 'package:flutter/foundation.dart' show kIsWeb, debugPrint;
import 'package:flutter/services.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../models/reservation.dart';

class DatabaseService {
  static final DatabaseService _instance = DatabaseService._internal();
  factory DatabaseService() => _instance;
  DatabaseService._internal();

  // Name des MethodChannels für Mobile
  static const MethodChannel _channel = MethodChannel('restaurant_booking_db');
  
  // Temporäre Lösung: Immer Web-Modus verwenden, bis native Implementierung vollständig ist
  static const bool _forceWebMode = true;

  /// Lädt alle Reservierungen
  Future<List<Reservation>> getAllReservations() async {
    if (_forceWebMode || kIsWeb) {
      // === Web-Fallback via SharedPreferences ===
      final prefs = await SharedPreferences.getInstance();
      final list = prefs.getStringList('reservations') ?? <String>[];
      
      debugPrint('Geladene Reservierungen (Web): ${list.length}');
      
      try {
        return list
            .map((e) => Reservation.fromMap(jsonDecode(e) as Map<String, dynamic>))
            .toList();
      } catch (e) {
        debugPrint('Fehler beim Deserialisieren: $e');
        return [];
      }
    }

    // === Mobile (Android/iOS) via MethodChannel ===
    try {
      final List<dynamic>? raw = await _channel.invokeMethod<List>('getAllReservations');
      if (raw == null) return <Reservation>[];
      return raw
          .map((item) => Reservation.fromMap(Map<String, dynamic>.from(item)))
          .toList();
    } on PlatformException catch (e) {
      debugPrint('DB-Error: ${e.message}');
      return <Reservation>[];
    }
  }

  /// Speichert alle Reservierungen (nur für Web)
  Future<void> _saveAllWeb(List<Reservation> all) async {
    final prefs = await SharedPreferences.getInstance();
    final encoded = all.map((r) => jsonEncode(r.toMap())).toList();
    await prefs.setStringList('reservations', encoded);
    debugPrint('Reservierungen gespeichert (Web): ${all.length}');
  }

  Future<void> addReservation(Reservation reservation) async {
    if (_forceWebMode || kIsWeb) {
      final all = await getAllReservations();
      // ID generieren, falls keine vorhanden
      if (reservation.id == null) {
        final newId = all.isEmpty ? 1 : (all.map((r) => r.id ?? 0).reduce((a, b) => a > b ? a : b) + 1);
        reservation = Reservation(
          id: newId,
          name: reservation.name,
          tableNumber: reservation.tableNumber,
          dateTime: reservation.dateTime,
          start: reservation.start,
          end: reservation.end,
        );
      }
      
      all.add(reservation);
      await _saveAllWeb(all);
      debugPrint('Reservierung hinzugefügt (Web): ${reservation.id}');
      return;
    }

    try {
      await _channel.invokeMethod('addReservation', reservation.toMap());
    } catch (e) {
      debugPrint('Fehler beim Hinzufügen der Reservierung: $e');
      rethrow;
    }
  }

  Future<void> updateReservation(Reservation reservation) async {
    if (_forceWebMode || kIsWeb) {
      final all = await getAllReservations();
      final idx = all.indexWhere((r) => r.id == reservation.id);
      if (idx != -1) {
        all[idx] = reservation;
        await _saveAllWeb(all);
        debugPrint('Reservierung aktualisiert (Web): ${reservation.id}');
      }
      return;
    }

    try {
      await _channel.invokeMethod('updateReservation', reservation.toMap());
    } catch (e) {
      debugPrint('Fehler beim Aktualisieren der Reservierung: $e');
      rethrow;
    }
  }

  Future<void> deleteReservation(int id) async {
    if (_forceWebMode || kIsWeb) {
      final all = await getAllReservations();
      all.removeWhere((r) => r.id == id);
      await _saveAllWeb(all);
      debugPrint('Reservierung gelöscht (Web): $id');
      return;
    }
    
    await _channel.invokeMethod('deleteReservation', {'id': id});
  }

  Future<bool> hasConflictingReservation(Reservation reservation) async {
    if (_forceWebMode || kIsWeb) {
      final all = await getAllReservations();
      
      // Prüfe auf Überschneidungen
      final conflicts = all.where((r) {
        // Gleicher Tisch
        if (r.tableNumber != reservation.tableNumber) return false;
        // ID ausschließen, falls es dieselbe Reservierung ist
        if (r.id == reservation.id) return false;
        
        // Überprüfen ob die Zeiten überlappen
        final startA = r.start;
        final endA = r.end;
        final startB = reservation.start;
        final endB = reservation.end;
        
        // Überlappung wenn: (StartA <= EndB) und (EndA >= StartB)
        return startA.isBefore(endB) && endA.isAfter(startB);
      }).toList();
      
      final hasConflict = conflicts.isNotEmpty;
      debugPrint('Konfliktprüfung (Web): $hasConflict');
      return hasConflict;
    }
    
    final bool? result =
      await _channel.invokeMethod<bool>('checkConflict', reservation.toMap());
    return result ?? false;
  }
}