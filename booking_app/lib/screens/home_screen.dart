// lib/screens/home_screen.dart
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import '../models/reservation.dart';
import '../services/storage_manager.dart';
import '../screens/statistics_screen.dart';
import '../screens/reservation_list_screen.dart';
import '../screens/reservation_form_screen.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({Key? key}) : super(key: key);

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  int _selectedIndex = 0;

  // Methode zum Aktualisieren der Daten (wenn nötig)
  void _refreshData() {
    setState(() {});
  }

  // Hilfsmethode zum Testen und Anzeigen von Debug-Informationen
  void _showDebugInfo() async {
    try {
      final reservations = await StorageManager().getAllReservations();
      
      if (!mounted) return;
      
      showDialog(
        context: context,
        builder: (context) => AlertDialog(
          title: const Text('Debug-Info'),
          content: SizedBox(
            width: double.maxFinite,
            child: SingleChildScrollView(
              child: Column(
                mainAxisSize: MainAxisSize.min,
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text('Plattform: ${kIsWeb ? "Web" : "Mobile"}'),
                  Text('Anzahl Reservierungen: ${reservations.length}'),
                  const Divider(),
                  if (reservations.isNotEmpty)
                    ...reservations.map((r) => Card(
                      margin: const EdgeInsets.only(bottom: 8),
                      child: Padding(
                        padding: const EdgeInsets.all(8.0),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text('ID: ${r.id}'),
                            Text('Name: ${r.name}'),
                            Text('Tisch: ${r.tableNumber}'),
                            Text('Datum: ${r.dateTime.toLocal()}'),
                          ],
                        ),
                      ),
                    )).toList()
                  else
                    const Text('Keine Reservierungen vorhanden'),
                ],
              ),
            ),
          ),
          actions: [
            TextButton(
              child: const Text('Test-Reservierung'),
              onPressed: () async {
                final now = DateTime.now();
                final testRes = Reservation(
                  id: null, // Neue ID generieren lassen
                  name: 'Test ${now.hour}:${now.minute}:${now.second}',
                  tableNumber: (now.second % 10) + 1,
                  dateTime: now,
                  start: now,
                  end: now.add(const Duration(hours: 2)),
                );
                
                final success = await StorageManager().addReservation(testRes);
                
                if (!context.mounted) return;
                
                ScaffoldMessenger.of(context).showSnackBar(
                  SnackBar(content: Text('Test-Speicherung: ${success ? "Erfolgreich" : "Fehlgeschlagen"}')),
                );
                
                Navigator.of(context).pop();
                setState(() {});
              },
            ),
            TextButton(
              child: const Text('Schließen'),
              onPressed: () => Navigator.of(context).pop(),
            ),
          ],
        ),
      );
    } catch (e) {
      debugPrint('Fehler bei Debug-Info: $e');
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: IndexedStack(
        index: _selectedIndex,
        children: [
          ReservationListScreen(
            onReservationChanged: _refreshData,
          ),
          FutureBuilder<List<Reservation>>(
            future: StorageManager().getAllReservations(),
            builder: (context, snapshot) {
              if (snapshot.connectionState == ConnectionState.waiting) {
                return const Center(child: CircularProgressIndicator());
              }
              final reservations = snapshot.data ?? [];
              return StatisticsScreen(reservations: reservations);
            },
          ),
        ],
      ),
      bottomNavigationBar: BottomNavigationBar(
        currentIndex: _selectedIndex,
        onTap: (index) => setState(() => _selectedIndex = index),
        items: const [
          BottomNavigationBarItem(
            icon: Icon(Icons.list),
            label: 'Reservierungen',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.analytics),
            label: 'Statistiken',
          ),
        ],
      ),
      floatingActionButton: _selectedIndex == 0
          ? FloatingActionButton(
              onPressed: () => Navigator.of(context).push(
                MaterialPageRoute(
                  builder: (_) => const ReservationFormScreen(),
                ),
              ).then((result) {
                if (result == true) {
                  _refreshData();
                }
              }),
              child: const Icon(Icons.add),
            )
          : FloatingActionButton(
              onPressed: _showDebugInfo,
              child: const Icon(Icons.bug_report),
            ),
    );
  }
}