import 'package:flutter/material.dart';
import '../models/reservation.dart';

class StatisticsScreen extends StatelessWidget {
  final List<Reservation> reservations;

  const StatisticsScreen({Key? key, required this.reservations}) : super(key: key);

  Map<int, int> _getTableUsageStats() {
    final Map<int, int> usage = {};
    for (var reservation in reservations) {
      usage[reservation.tableNumber] = (usage[reservation.tableNumber] ?? 0) + 1;
    }
    return usage;
  }

  @override
  Widget build(BuildContext context) {
    final stats = _getTableUsageStats();
    
    return Scaffold(
      appBar: AppBar(
        title: const Text('Tischstatistiken'),
      ),
      body: ListView.builder(
        itemCount: stats.length,
        itemBuilder: (context, index) {
          final tableNumber = stats.keys.elementAt(index);
          final usageCount = stats[tableNumber]!;
          
          return ListTile(
            leading: const Icon(Icons.table_bar),
            title: Text('Tisch $tableNumber'),
            trailing: Text('$usageCount Reservierungen'),
          );
        },
      ),
    );
  }
}