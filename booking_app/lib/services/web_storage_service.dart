// lib/services/storage_service.dart
import '../models/reservation.dart';
import 'storage_manager.dart';

/// Weiterleitung zu StorageManager
class StorageService {
  static final StorageService _instance = StorageService._internal();
  factory StorageService() => _instance;
  StorageService._internal();
  
  /// Initialisierung
  Future<void> initialize() => StorageManager().initialize();
  
  /// Lädt alle Reservierungen
  Future<List<Reservation>> getAllReservations() => StorageManager().getAllReservations();
  
  /// Fügt eine Reservierung hinzu
  Future<bool> addReservation(Reservation reservation) => StorageManager().addReservation(reservation);
  
  /// Aktualisiert eine Reservierung
  Future<bool> updateReservation(Reservation reservation) => StorageManager().updateReservation(reservation);
  
  /// Löscht eine Reservierung
  Future<bool> deleteReservation(int id) => StorageManager().deleteReservation(id);
  
  /// Prüft auf Konflikte mit anderen Reservierungen
  Future<bool> hasConflictingReservation(Reservation reservation) => 
      StorageManager().hasConflictingReservation(reservation);
}