import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import '../models/reservation.dart';
import '../services/storage_manager.dart';
import 'reservation_form_screen.dart';

class ReservationListScreen extends StatefulWidget {
  final VoidCallback? onReservationChanged;
  
  const ReservationListScreen({
    Key? key, 
    this.onReservationChanged,
  }) : super(key: key);

  @override
  ReservationListScreenState createState() => ReservationListScreenState();
}

class ReservationListScreenState extends State<ReservationListScreen> {
  late Future<List<Reservation>> _reservationsFuture;
  bool _isLoading = false;

  @override
  void initState() {
    super.initState();
    _loadReservations();
  }

  void _loadReservations() {
    setState(() => _isLoading = true);
    _reservationsFuture = StorageManager().getAllReservations().whenComplete(() {
      if (mounted) setState(() => _isLoading = false);
    });
  }

  Future<void> _deleteReservation(int id) async {
    setState(() => _isLoading = true);
    
    try {
      final success = await StorageManager().deleteReservation(id);
      
      if (!mounted) return;
      
      if (success) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Reservierung gelöscht')),
        );
        _loadReservations();
        if (widget.onReservationChanged != null) {
          widget.onReservationChanged!();
        }
      } else {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Fehler beim Löschen der Reservierung')),
        );
      }
    } catch (e) {
      debugPrint('Fehler beim Löschen: $e');
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Ein Fehler ist aufgetreten')),
      );
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }
  
  // Test-Speicherung zum Debugging
  Future<void> _testSaveReservation() async {
    try {
      final now = DateTime.now();
      final testReservation = Reservation(
        id: 999,
        name: 'Test ${now.hour}:${now.minute}',
        tableNumber: 5,
        dateTime: now,
        start: now,
        end: now.add(const Duration(hours: 2)),
      );
      
      final success = await StorageManager().addReservation(testReservation);
      
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Test-Speicherung: ${success ? "Erfolgreich" : "Fehlgeschlagen"}')),
      );
      
      _loadReservations();
    } catch (e) {
      debugPrint('Test-Speicherung fehlgeschlagen: $e');
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Reservierungen'),
        actions: [
          // Test-Button für Debug-Zwecke
          if (kDebugMode)
            IconButton(
              icon: const Icon(Icons.bug_report),
              onPressed: _testSaveReservation,
              tooltip: 'Test-Reservierung erstellen',
            ),
        ],
      ),
      body: _isLoading 
        ? const Center(child: CircularProgressIndicator()) 
        : FutureBuilder<List<Reservation>>(
            future: _reservationsFuture,
            builder: (context, snapshot) {
              if (snapshot.connectionState == ConnectionState.waiting) {
                return const Center(child: CircularProgressIndicator());
              }
              
              if (snapshot.hasError) {
                debugPrint('Fehler beim Laden: ${snapshot.error}');
                return Center(child: Text('Fehler: ${snapshot.error}'));
              }
              
              // Debug-Print für leere Listen
              debugPrint('Reservierungen geladen: ${snapshot.data?.length ?? 0}');
              
              final list = snapshot.data ?? [];
              if (list.isEmpty) {
                return const Center(child: Text('Keine Reservierungen vorhanden'));
              }
              
              return Padding(
                padding: const EdgeInsets.all(16),
                child: ListView.builder(
                  itemCount: list.length,
                  itemBuilder: (context, index) {
                    final res = list[index];
                    final name = res.name;
                    final id = res.id;
                    final table = res.tableNumber.toString();
                    final dateTime = res.dateTime.toLocal();

                    return Card(
                      margin: const EdgeInsets.symmetric(vertical: 8),
                      child: ListTile(
                        title: Text(name),
                        subtitle: Text('Tisch $table - ${_formatDateTime(dateTime)}'),
                        onTap: () {
                          Navigator.push(
                            context,
                            MaterialPageRoute(
                              builder: (context) => ReservationFormScreen(reservation: res),
                            ),
                          ).then((result) {
                            if (result == true) {
                              _loadReservations();
                              if (widget.onReservationChanged != null) {
                                widget.onReservationChanged!();
                              }
                            }
                          });
                        },
                        trailing: IconButton(
                          icon: const Icon(Icons.delete, color: Colors.red),
                          onPressed: id != null
                              ? () => _deleteReservation(id)
                              : null,
                        ),
                      ),
                    );
                  },
                ),
              );
            },
          ),
      floatingActionButton: FloatingActionButton(
        onPressed: () {
          Navigator.push(
            context,
            MaterialPageRoute(builder: (context) => const ReservationFormScreen()),
          ).then((result) {
            if (result == true) {
              // Nach Rückkehr die Reservierungsliste aktualisieren, wenn erfolgreich
              _loadReservations();
              if (widget.onReservationChanged != null) {
                widget.onReservationChanged!();
              }
            }
          });
        },
        child: const Icon(Icons.add),
      ),
    );
  }
  
  // Hilfsfunktion zum Formatieren des Datums/der Uhrzeit
  String _formatDateTime(DateTime dateTime) {
    return '${dateTime.day}.${dateTime.month}.${dateTime.year} um '
        '${dateTime.hour.toString().padLeft(2, '0')}:'
        '${dateTime.minute.toString().padLeft(2, '0')} Uhr';
  }
}