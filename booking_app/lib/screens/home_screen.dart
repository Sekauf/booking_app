import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../services/db_service.dart';
import '../models/reservation.dart';

class HomeScreen extends StatefulWidget {
  @override
  _HomeScreenState createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  bool _loading = true;
  List<Reservation> _reservations = [];

  @override
  void initState() {
    super.initState();
    _loadReservations();
  }

  Future<void> _loadReservations() async {
    setState(() {
      _loading = true;
    });
    List<Reservation> resList = await DatabaseService.getReservations();
    setState(() {
      _reservations = resList;
      _loading = false;
    });
  }

  void _addReservation() async {
    final result = await Navigator.pushNamed(context, '/addReservation');
    if (result == true) {
      _loadReservations();
    }
  }

  void _manageResources() async {
    final result = await Navigator.pushNamed(context, '/resources');
    if (result == true) {
      // If resources were changed (added/deleted), reload reservations in case it affects list
      _loadReservations();
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Reservations'),
        actions: [
          IconButton(
            icon: Icon(Icons.settings),
            tooltip: 'Manage Resources',
            onPressed: _manageResources,
          ),
        ],
      ),
      body: _loading
          ? Center(child: CircularProgressIndicator())
          : _reservations.isEmpty
          ? Center(child: Text('No reservations available.'))
          : RefreshIndicator(
        onRefresh: () async {
          await _loadReservations();
        },
        child: ListView.builder(
          itemCount: _reservations.length,
          itemBuilder: (context, index) {
            final reservation = _reservations[index];
            final resourceName = reservation.resourceName ?? 'Resource ${reservation.resourceId}';
            final formattedDate = DateFormat('dd.MM.yyyy').format(reservation.date);
            return Dismissible(
              key: Key(reservation.id.toString()),
              background: Container(
                color: Colors.red,
                alignment: Alignment.centerLeft,
                padding: EdgeInsets.symmetric(horizontal: 16),
                child: Icon(Icons.delete, color: Colors.white),
              ),
              secondaryBackground: Container(
                color: Colors.red,
                alignment: Alignment.centerRight,
                padding: EdgeInsets.symmetric(horizontal: 16),
                child: Icon(Icons.delete, color: Colors.white),
              ),
              onDismissed: (direction) async {
                // Remove from list optimistically
                final removedRes = reservation;
                setState(() {
                  _reservations.removeAt(index);
                });
                // Delete from database
                await DatabaseService.deleteReservation(removedRes.id!);
                // Show feedback
                ScaffoldMessenger.of(context).showSnackBar(
                  SnackBar(content: Text('Reservation deleted')),
                );
              },
              child: ListTile(
                title: Text(resourceName),
                subtitle: Text(formattedDate +
                    (reservation.description != null && reservation.description!.isNotEmpty
                        ? " - ${reservation.description}"
                        : "")),
              ),
            );
          },
        ),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: _addReservation,
        tooltip: 'Add Reservation',
        child: Icon(Icons.add),
      ),
    );
  }
}
