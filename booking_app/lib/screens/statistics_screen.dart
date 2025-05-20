import 'package:flutter/material.dart';
import '../services/database_service.dart';
import '../models/reservation.dart';

class StatisticsScreen extends StatelessWidget {
  const StatisticsScreen({Key? key}) : super(key: key);

  // Berechnet Anzahl Reservierungen pro Tisch
  Map<int, int> _countByTable(List<Reservation> reservations) {
    final Map<int, int> counts = {};
    for (var res in reservations) {
      counts[res.table] = (counts[res.table] ?? 0) + 1;
    }
    return counts;
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Statistiken'),
      ),
      body: FutureBuilder<List<Reservation>>(
        future: DatabaseService().getAllReservations(),
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const Center(child: CircularProgressIndicator());
          } else if (snapshot.hasError) {
            return Center(child: Text('Fehler: ${snapshot.error}'));
          } else {
            final reservations = snapshot.data ?? <Reservation>[];
            final total = reservations.length;
            final counts = _countByTable(reservations);
            if (total == 0) {
              return const Center(child: Text('Keine Daten für Statistik.'));
            }
            return Padding(
              padding: const EdgeInsets.all(16.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text('Gesamtreservierungen: $total',
                      style: const TextStyle(fontSize: 18)),
                  const SizedBox(height: 16),
                  const Text('Reservierungen pro Tisch:',
                      style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
                  const SizedBox(height: 8),
                  // Liste der Reservierungen pro Tisch
                  Expanded(
                    child: ListView(
                      children: counts.entries.map((entry) {
                        return Text('Tisch ${entry.key}: ${entry.value}');
                      }).toList(),
                    ),
                  ),
                ],
              ),
            );
          }
        },
      ),
    );
  }
}
