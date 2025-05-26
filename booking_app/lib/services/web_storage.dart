// lib/services/web_storage_service.dart
import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:universal_html/html.dart' as html;

/// WebStorageService - Klasse zum Zugriff auf localStorage im Browser
class WebStorageService {
  static final WebStorageService _instance = WebStorageService._internal();
  factory WebStorageService() => _instance;
  WebStorageService._internal();
  
  // Speichert Daten im localStorage
  void saveData(String key, String value) {
    if (kIsWeb) {
      try {
        html.window.localStorage[key] = value;
        debugPrint('WebStorageService: Daten für Schlüssel "$key" gespeichert');
      } catch (e) {
        debugPrint('Fehler beim Speichern in localStorage: $e');
      }
    }
  }
  
  // Lädt Daten aus localStorage
  String? getData(String key) {
    if (kIsWeb) {
      try {
        final value = html.window.localStorage[key];
        return value;
      } catch (e) {
        debugPrint('Fehler beim Laden aus localStorage: $e');
      }
    }
    return null;
  }
  
  // Löscht Daten aus localStorage
  void removeData(String key) {
    if (kIsWeb) {
      try {
        html.window.localStorage.remove(key);
        debugPrint('WebStorageService: Daten für Schlüssel "$key" gelöscht');
      } catch (e) {
        debugPrint('Fehler beim Entfernen aus localStorage: $e');
      }
    }
  }
  
  // Speichert eine Liste als JSON-String
  void saveList<T>(String key, List<Map<String, dynamic>> items) {
    final jsonString = jsonEncode(items);
    saveData(key, jsonString);
    debugPrint('WebStorageService: Liste mit ${items.length} Elementen gespeichert');
  }
  
  // Lädt eine Liste aus einem JSON-String
  List<Map<String, dynamic>> getList(String key) {
    try {
      final jsonString = getData(key);
      if (jsonString == null || jsonString.isEmpty) {
        return [];
      }
      
      final decoded = jsonDecode(jsonString) as List<dynamic>;
      return decoded.map<Map<String, dynamic>>((item) {
        if (item is Map) {
          return Map<String, dynamic>.from(item);
        }
        return <String, dynamic>{};
      }).toList();
    } catch (e) {
      debugPrint('Fehler beim Laden der Liste aus localStorage: $e');
      return [];
    }
  }
}