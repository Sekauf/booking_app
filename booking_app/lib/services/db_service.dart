import 'package:flutter/services.dart';
import '../models/resource.dart';
import '../models/reservation.dart';

class DatabaseService {
  static const MethodChannel _channel = MethodChannel('offline_booking_app');

  static Future<int?> insertResource(String name) async {
    try {
      final int? id = await _channel.invokeMethod('insertResource', {
        'name': name,
      });
      return id;
    } on PlatformException catch (e) {
      print("Error inserting resource: ${e.message}");
      return null;
    }
  }

  static Future<List<Resource>> getResources() async {
    try {
      final List<dynamic>? result = await _channel.invokeMethod('getResources');
      if (result == null) return [];
      return result.map((item) => Resource.fromMap(item)).toList();
    } on PlatformException catch (e) {
      print("Error getting resources: ${e.message}");
      return [];
    }
  }

  static Future<int?> insertReservation(Reservation reservation) async {
    try {
      final int? id = await _channel.invokeMethod('insertReservation', {
        'resource_id': reservation.resourceId,
        'date': reservation.date.millisecondsSinceEpoch,
        'description': reservation.description ?? '',
      });
      return id;
    } on PlatformException catch (e) {
      print("Error inserting reservation: ${e.message}");
      return null;
    }
  }

  static Future<List<Reservation>> getReservations() async {
    try {
      final List<dynamic>? result = await _channel.invokeMethod('getReservations');
      if (result == null) return [];
      return result.map((item) => Reservation.fromMap(item)).toList();
    } on PlatformException catch (e) {
      print("Error getting reservations: ${e.message}");
      return [];
    }
  }

  static Future<bool> deleteReservation(int id) async {
    try {
      final bool? success = await _channel.invokeMethod('deleteReservation', {
        'id': id,
      });
      return success ?? false;
    } on PlatformException catch (e) {
      print("Error deleting reservation: ${e.message}");
      return false;
    }
  }

  static Future<bool> deleteResource(int id) async {
    try {
      final bool? success = await _channel.invokeMethod('deleteResource', {
        'id': id,
      });
      return success ?? false;
    } on PlatformException catch (e) {
      print("Error deleting resource: ${e.message}");
      return false;
    }
  }
}
