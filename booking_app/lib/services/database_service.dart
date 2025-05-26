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
  
  // Konstanter Key für SharedPreferences
  static const String _reservationsKey = 'reservations';

  /// Lädt alle Reservierungen
  Future<List<Reservation>> getAllReservations() async {
    if (_forceWebMode || kIsWeb) {
      // === Web-Fallback via SharedPreferences ===
      try {
        final prefs = await SharedPreferences.getInstance();
        // Überprüfen, ob SharedPreferences funktionieren
        debugPrint('SharedPreferences funktionieren: ${prefs.getKeys().isNotEmpty ? 'ja' : 'unbekannt'}');
        
        final list = prefs.getStringList(_reservationsKey);
        
        if (list == null) {
          debugPrint('Keine Reservierungen gefunden (null)');
          return [];
        }
        
        debugPrint('Geladene Reservierungen (Web): ${list.length}');
        
        if (list.isEmpty) {
          return [];
        }
        
        final result = <Reservation>[];
        for (int i = 0; i < list.length; i++) {
          try {
            final jsonMap = jsonDecode(list[i]);
            if (jsonMap is! Map<String, dynamic>) {
              debugPrint('Ungültiges JSON-Format bei Eintrag $i');
              continue;
            }
            final reservierung = Reservation.fromMap(jsonMap);
            result.add(reservierung);
          } catch (e) {
            debugPrint('Fehler beim Deserialisieren von Eintrag $i: $e');
          }
        }
        return result;
      } catch (e) {
        debugPrint('Fehler beim Laden der Reservierungen: $e');
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
  Future<bool> _saveAllWeb(List<Reservation> all) async {
    try {
      final prefs = await SharedPreferences.getInstance();
      
      // Alle Reservierungen als JSON kodieren
      final encoded = all.map((r) => jsonEncode(r.toMap())).toList();
      
      // SharedPreferences speichern
      final success = await prefs.setStringList(_reservationsKey, encoded);
      
      // Überprüfen, ob das Speichern erfolgreich war
      debugPrint('Reservierungen gespeichert (Web): ${all.length}, Erfolg: $success');
      
      // Beispiel ausgeben für Debugging
      if (encoded.isNotEmpty) {
        debugPrint('Beispiel gespeicherte Daten: ${encoded.first}');
      }
      
      // Direkte Überprüfung der Speicherung
      final testList = prefs.getStringList(_reservationsKey);
      if (testList == null) {
        debugPrint('FEHLER: Nach dem Speichern konnte nichts geladen werden!');
        return false;
      }
      
      debugPrint('Nach Speichern direkt geprüft: ${testList.length} Elemente');
      
      // Manuelle Überprüfung durch Datei-Commit erzwingen
      await prefs.reload();
      final afterReload = prefs.getStringList(_reservationsKey);
      debugPrint('Nach Reload geprüft: ${afterReload?.length} Elemente');
      
      return success;
    } catch (e) {
      debugPrint('Fehler beim Speichern der Reservierungen: $e');
      return false;
    }
  }

  Future<bool> addReservation(Reservation reservation) async {
    if (_forceWebMode || kIsWeb) {
      try {
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
        
        // Zur Liste hinzufügen
        all.add(reservation);
        
        // Speichern und Ergebnis zurückgeben
        final success = await _saveAllWeb(all);
        
        // Überprüfung nach dem Speichern
        final verifyList = await getAllReservations();
        debugPrint('Nach Hinzufügen geladen: ${verifyList.length}');
        
        return success;
      } catch (e) {
        debugPrint('Fehler beim Hinzufügen der Reservierung: $e');
        return false;
      }
    }

    try {
      await _channel.invokeMethod('addReservation', reservation.toMap());
      return true;
    } catch (e) {
      debugPrint('Fehler beim Hinzufügen der Reservierung: $e');
      return false;
    }
  }

  Future<bool> updateReservation(Reservation reservation) async {
    if (_forceWebMode || kIsWeb) {
      try {
        final all = await getAllReservations();
        final idx = all.indexWhere((r) => r.id == reservation.id);
        
        if (idx != -1) {
          all[idx] = reservation;
          final success = await _saveAllWeb(all);
          debugPrint('Reservierung aktualisiert (Web): ${reservation.id}');
          
          // Überprüfung nach dem Speichern
          final verifyList = await getAllReservations();
          debugPrint('Nach Update geladen: ${verifyList.length}');
          
          return success;
        } else {
          debugPrint('Warnung: Zu aktualisierende Reservierung nicht gefunden: ${reservation.id}');
          return false;
        }
      } catch (e) {
        debugPrint('Fehler beim Aktualisieren der Reservierung: $e');
        return false;
      }
    }

    try {
      await _channel.invokeMethod('updateReservation', reservation.toMap());
      return true;
    } catch (e) {
      debugPrint('Fehler beim Aktualisieren der Reservierung: $e');
      return false;
    }
  }

  Future<bool> deleteReservation(int id) async {
    if (_forceWebMode || kIsWeb) {
      try {
        final all = await getAllReservations();
        final vorherAnzahl = all.length;
        all.removeWhere((r) => r.id == id);
        
        if (vorherAnzahl == all.length) {
          debugPrint('Warnung: Keine Reservierung mit ID $id gefunden zum Löschen');
        }
        
        final success = await _saveAllWeb(all);
        debugPrint('Reservierung gelöscht (Web): $id, Erfolg: $success');
        
        // Überprüfung nach dem Löschen
        final verifyList = await getAllReservations();
        debugPrint('Nach Löschen geladen: ${verifyList.length}');
        
        return success;
      } catch (e) {
        debugPrint('Fehler beim Löschen der Reservierung: $e');
        return false;
      }
    }
    
    try {
      await _channel.invokeMethod('deleteReservation', {'id': id});
      return true;
    } catch (e) {
      debugPrint('Fehler beim Löschen der Reservierung: $e');
      return false;
    }
  }

  Future<bool> hasConflictingReservation(Reservation reservation) async {
    if (_forceWebMode || kIsWeb) {
      try {
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
      } catch (e) {
        debugPrint('Fehler bei der Konfliktprüfung: $e');
        return false;
      }
    }
    
    try {
      final bool? result =
        await _channel.invokeMethod<bool>('checkConflict', reservation.toMap());
      return result ?? false;
    } catch (e) {
      debugPrint('Fehler bei der Konfliktprüfung: $e');
      return false;
    }
  }

  // Hilfsmethode zum Löschen aller Daten (für Tests)
  Future<void> clearAllData() async {
    try {
      if (_forceWebMode || kIsWeb) {
        final prefs = await SharedPreferences.getInstance();
        await prefs.remove(_reservationsKey);
        debugPrint('Alle Reservierungen gelöscht (Web)');
      } else {
        await _channel.invokeMethod('clearAllData');
      }
    } catch (e) {
      debugPrint('Fehler beim Löschen aller Daten: $e');
    }
  }
}