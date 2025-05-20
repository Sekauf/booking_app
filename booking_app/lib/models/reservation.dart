// lib/models/reservation.dart
import 'package:flutter/foundation.dart';

class Reservation {
  int? id;
  final String name;
  final int tableNumber;
  final DateTime dateTime;
  final DateTime start;
  final DateTime end;

  Reservation({
    this.id,
    required this.name,
    required this.tableNumber,
    required this.dateTime,
    required this.start,
    required this.end,
  });

  Map<String, dynamic> toMap() {
    return {
      'id': id,
      'name': name,
      'tableNumber': tableNumber,
      'dateTime': dateTime.millisecondsSinceEpoch,
      'start': start.millisecondsSinceEpoch,
      'end': end.millisecondsSinceEpoch,
    };
  }

  factory Reservation.fromMap(Map<String, dynamic> map) {
    // Debug-Ausgabe für Deserialierungs-Probleme
    try {
      // Überprüfen, ob Werte unter verschiedenen Spaltennamen vorhanden sind
      final int tableNumber;
      if (map.containsKey('tableNumber')) {
        tableNumber = map['tableNumber'] as int;
      } else if (map.containsKey('table')) {
        tableNumber = map['table'] as int;
      } else {
        debugPrint('Warnung: Weder tableNumber noch table gefunden in $map');
        tableNumber = 0;
      }

      // Zeitstempel verarbeiten
      int dateTimeMs;
      if (map['dateTime'] is int) {
        dateTimeMs = map['dateTime'] as int;
      } else if (map['dateTime'] is String) {
        dateTimeMs = DateTime.parse(map['dateTime'] as String).millisecondsSinceEpoch;
      } else {
        debugPrint('Warnung: dateTime hat unerwarteten Typ in $map');
        dateTimeMs = DateTime.now().millisecondsSinceEpoch;
      }

      // Start/End-Zeit
      int startMs = dateTimeMs;
      int endMs = dateTimeMs + const Duration(hours: 2).inMilliseconds;
      
      if (map.containsKey('start')) {
        if (map['start'] is int) {
          startMs = map['start'] as int;
        } else if (map['start'] is String) {
          startMs = DateTime.parse(map['start'] as String).millisecondsSinceEpoch;
        }
      }
      
      if (map.containsKey('end')) {
        if (map['end'] is int) {
          endMs = map['end'] as int;
        } else if (map['end'] is String) {
          endMs = DateTime.parse(map['end'] as String).millisecondsSinceEpoch;
        }
      }

      return Reservation(
        id: map['id'] as int?,
        name: map['name'] as String? ?? 'Unbekannt',
        tableNumber: tableNumber,
        dateTime: DateTime.fromMillisecondsSinceEpoch(dateTimeMs),
        start: DateTime.fromMillisecondsSinceEpoch(startMs),
        end: DateTime.fromMillisecondsSinceEpoch(endMs),
      );
    } catch (e) {
      debugPrint('Fehler beim Deserialisieren der Reservierung: $e');
      // Fallback-Objekt erstellen
      return Reservation(
        id: 0,
        name: 'Fehlerhafte Daten',
        tableNumber: 0,
        dateTime: DateTime.now(),
        start: DateTime.now(),
        end: DateTime.now().add(const Duration(hours: 2)),
      );
    }
  }
}