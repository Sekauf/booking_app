import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'dart:js' as js;
import '../models/reservation.dart';

/// StorageManager - zentrale Klasse zur Verwaltung der Datenspeicherung
class StorageManager {
  static final StorageManager _instance = StorageManager._internal();
  factory StorageManager() => _instance;
  StorageManager._internal();
  
  // Konstante für Speicherung
  static const String _reservationsKey = 'booking_app_reservations';
  
  // Initialisierung
  Future<void> initialize() async {
    debugPrint('StorageManager: Initialisierung');
    
    if (kIsWeb) {
      // Test, ob localStorage funktioniert
      try {
        _saveToLocalStorage('test_key', 'Test ${DateTime.now().toIso8601String()}');
        final testValue = _getFromLocalStorage('test_key');
        debugPrint('localStorage Test: $testValue');
      } catch (e) {
        debugPrint('Fehler bei Web-Storage Initialisierung: $e');
      }
    }
  }
  
  // Web-Speichermethoden
  void _saveToLocalStorage(String key, String value) {
    if (kIsWeb) {
      try {
        js.context['localStorage'].callMethod('setItem', [key, value]);
      } catch (e) {
        debugPrint('Fehler beim Speichern in localStorage: $e');
      }
    }
  }
  
  String? _getFromLocalStorage(String key) {
    if (kIsWeb) {
      try {
        final value = js.context['localStorage'].callMethod('getItem', [key]);
        return value?.toString();
      } catch (e) {
        debugPrint('Fehler beim Laden aus localStorage: $e');
      }
    }
    return null;
  }
  

  /// Lädt alle Reservierungen
  Future<List<Reservation>> getAllReservations() async {
    try {
      if (kIsWeb) {
        // Laden aus localStorage
        final jsonString = _getFromLocalStorage(_reservationsKey);
        
        if (jsonString == null || jsonString.isEmpty) {
          debugPrint('Keine Reservierungen in localStorage gefunden');
          return [];
        }
        
        final List<dynamic> decodedList = jsonDecode(jsonString);
        final reservations = <Reservation>[];
        
        for (final item in decodedList) {
          try {
            if (item is Map<String, dynamic>) {
              reservations.add(Reservation.fromMap(item));
            } else {
              debugPrint('Ungültiges Format für Reservierung: $item');
            }
          } catch (e) {
            debugPrint('Fehler beim Deserialisieren: $e');
          }
        }
        
        debugPrint('Aus localStorage geladen: ${reservations.length} Reservierungen');
        return reservations;
      } else {
        // Mobile-Implementierung hier (falls benötigt)
        debugPrint('getAllReservations() für Mobile noch nicht implementiert');
        return [];
      }
    } catch (e) {
      debugPrint('Fehler beim Laden der Reservierungen: $e');
      return [];
    }
  }

  /// Fügt eine Reservierung hinzu
  Future<bool> addReservation(Reservation reservation) async {
    try {
      // Alle Reservierungen laden
      final reservations = await getAllReservations();
      
      // Neue ID generieren, falls nötig
      if (reservation.id == null) {
        int maxId = 0;
        if (reservations.isNotEmpty) {
          for (var r in reservations) {
            if ((r.id ?? 0) > maxId) maxId = r.id ?? 0;
          }
        }
        reservation = Reservation(
          id: maxId + 1,
          name: reservation.name,
          tableNumber: reservation.tableNumber,
          dateTime: reservation.dateTime,
          start: reservation.start,
          end: reservation.end,
        );
      }
      
      // Zur Liste hinzufügen
      reservations.add(reservation);
      debugPrint('Reservierung hinzugefügt: ${reservation.id}');
      
      // Alles speichern
      return _saveAllReservations(reservations);
    } catch (e) {
      debugPrint('Fehler beim Hinzufügen der Reservierung: $e');
      return false;
    }
  }

  /// Aktualisiert eine Reservierung
  Future<bool> updateReservation(Reservation reservation) async {
    try {
      if (reservation.id == null) {
        debugPrint('Fehler: Reservierung hat keine ID');
        return false;
      }
      
      // Alle Reservierungen laden
      final reservations = await getAllReservations();
      final index = reservations.indexWhere((r) => r.id == reservation.id);
      
      if (index != -1) {
        // Reservierung aktualisieren
        reservations[index] = reservation;
        debugPrint('Reservierung aktualisiert: ${reservation.id}');
        
        // Alles speichern
        return _saveAllReservations(reservations);
      } else {
        debugPrint('Fehler: Reservierung mit ID ${reservation.id} nicht gefunden');
        return false;
      }
    } catch (e) {
      debugPrint('Fehler beim Aktualisieren der Reservierung: $e');
      return false;
    }
  }

  /// Löscht eine Reservierung
  Future<bool> deleteReservation(int id) async {
    try {
      // Alle Reservierungen laden
      final reservations = await getAllReservations();
      final vorherAnzahl = reservations.length;
      
      // Reservierung mit der angegebenen ID entfernen
      reservations.removeWhere((r) => r.id == id);
      
      if (reservations.length == vorherAnzahl) {
        debugPrint('Fehler: Reservierung mit ID $id nicht gefunden');
        return false;
      }
      
      debugPrint('Reservierung gelöscht: $id');
      
      // Alles speichern
      return _saveAllReservations(reservations);
    } catch (e) {
      debugPrint('Fehler beim Löschen der Reservierung: $e');
      return false;
    }
  }

  /// Speichert alle Reservierungen
  Future<bool> _saveAllReservations(List<Reservation> reservations) async {
    try {
      if (kIsWeb) {
        // Zu JSON-String konvertieren
        final List<Map<String, dynamic>> jsonList = 
            reservations.map((r) => r.toMap()).toList();
        final jsonString = jsonEncode(jsonList);
        
        // In localStorage speichern
        _saveToLocalStorage(_reservationsKey, jsonString);
        
        // Überprüfen, ob gespeichert wurde
        final saved = _getFromLocalStorage(_reservationsKey);
        final success = saved != null && saved.isNotEmpty;
        
        debugPrint('Alle Reservierungen gespeichert: $success (${reservations.length} Einträge)');
        return success;
      } else {
        // Mobile-Implementierung hier (falls benötigt)
        debugPrint('_saveAllReservations() für Mobile noch nicht implementiert');
        return false;
      }
    } catch (e) {
      debugPrint('Fehler beim Speichern aller Reservierungen: $e');
      return false;
    }
  }

  /// Prüft auf Konflikte mit anderen Reservierungen
  Future<bool> hasConflictingReservation(Reservation reservation) async {
    try {
      // Alle Reservierungen laden
      final reservations = await getAllReservations();
      
      // Nach Konflikten suchen
      final conflicts = reservations.where((r) {
        // Gleicher Tisch
        if (r.tableNumber != reservation.tableNumber) return false;
        
        // Ausschluss der eigenen Reservierung bei Updates
        if (r.id == reservation.id) return false;
        
        // Zeitüberlappung prüfen: Überlappung, wenn:
        // Start A <= Ende B UND Ende A >= Start B
        return r.start.isBefore(reservation.end) && 
               r.end.isAfter(reservation.start);
      }).toList();
      
      final hasConflict = conflicts.isNotEmpty;
      debugPrint('Konfliktprüfung: $hasConflict');
      return hasConflict;
    } catch (e) {
      debugPrint('Fehler bei Konfliktprüfung: $e');
      return false;
    }
  }
}