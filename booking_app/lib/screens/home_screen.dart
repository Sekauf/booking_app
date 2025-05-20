// lib/screens/home_screen.dart
import 'package:flutter/material.dart';
import '../models/reservation.dart';
import '../services/database_service.dart';
import '../screens/statistics_screen.dart';
import '../screens/reservation_list_screen.dart';
import '../screens/reservation_form_screen.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({Key? key}) : super(key: key);

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

// In home_screen.dart
class _HomeScreenState extends State<HomeScreen> {
  int _selectedIndex = 0;

  // Methode zum Aktualisieren der Daten (wenn nötig)
  void _refreshData() {
    setState(() {});
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
            future: DatabaseService().getAllReservations(),
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
              ),
              child: const Icon(Icons.add),
            )
          : null,
    );
  }
}