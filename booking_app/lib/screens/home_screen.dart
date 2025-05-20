import 'package:flutter/material.dart';
import 'reservation_list.dart';
import 'reservation_form.dart';
import 'statistics_screen.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({Key? key}) : super(key: key);

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  // Callback, um die Liste nach Änderungen zu aktualisieren
  void _refresh() {
    setState(() {});
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Reservierungen'),
      ),
      // Seitenleiste für Navigation (z.B. Statistik)
      drawer: Drawer(
        child: ListView(
          children: [
            const DrawerHeader(child: Text('Menü', style: TextStyle(fontSize: 18))),
            ListTile(
              title: const Text('Statistiken'),
              onTap: () {
                Navigator.of(context).push(
                  MaterialPageRoute(builder: (_) => const StatisticsScreen()),
                );
              },
            ),
          ],
        ),
      ),
      // Hauptinhalt: Liste der Reservierungen
      body: ReservationList(onReservationChanged: _refresh),
      // FAB zum Anlegen einer neuen Reservierung
      floatingActionButton: FloatingActionButton(
        onPressed: () async {
          // Öffne Formular ohne übergebene Reservierung (Neuanlage)
          await Navigator.of(context).push(
            MaterialPageRoute(builder: (_) => const ReservationForm()),
          );
          _refresh();
        },
        child: const Icon(Icons.add),
      ),
    );
  }
}
