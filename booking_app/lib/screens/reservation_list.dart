// lib/screens/reservation_list.dart

import 'package:flutter/material.dart';
import '../models/reservation.dart';
import '../services/database_service.dart';
import 'reservation_form.dart';

/// Widget, das alle Reservierungen lädt und in einer Liste anzeigt.
/// Über den [onReservationChanged]-Callback wird die umgebende Seite informiert,
/// wenn eine Reservierung hinzugefügt, bearbeitet oder gelöscht wurde.
class ReservationList extends StatelessWidget {
  final VoidCallback onReservationChanged;

  const ReservationList({
    Key? key,
    required this.onReservationChanged,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return FutureBuilder<List<Reservation>>(
      future: DatabaseService().getAllReservations(),
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.waiting) {
          // Ladeindikator während der Datenbank-Abfrage
          return const Center(child: CircularProgressIndicator());
        } else if (snapshot.hasError) {
          // Fehleranzeige bei Problem
          return Center(child: Text('Fehler: ${snapshot.error}'));
        }

        final reservations = snapshot.data ?? <Reservation>[];
        if (reservations.isEmpty) {
          // Hinweis, wenn keine Reservierungen vorliegen
          return const Center(child: Text('Keine Reservierungen gefunden.'));
        }

        return ListView.builder(
          itemCount: reservations.length,
          itemBuilder: (context, index) {
            final res = reservations[index];
            return Card(
              margin: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
              child: ListTile(
                // Titel: Name des Gastes
                title: Text(res.name),
                // Untertitel: Tischnummer und Datum/Uhrzeit
                subtitle: Text(
                  'Tisch ${res.tableNumber} – ${res.dateTime.toLocal()}'.split('.').first,  // Geändert von table
                ),
                // Tippen öffnet das Formular zum Bearbeiten
                onTap: () async {
                  await Navigator.of(context).push(
                    MaterialPageRoute(
                      builder: (_) => ReservationForm(reservation: res),
                    ),
                  );
                  onReservationChanged();
                },
                // Löschen-Button am Zeilenende
                trailing: IconButton(
                  icon: const Icon(Icons.delete, color: Colors.red),
                  onPressed: () async {
                    // Reservierung löschen und Liste neu laden
                    await DatabaseService().deleteReservation(res.id!);
                    onReservationChanged();
                  },
                ),
              ),
            );
          },
        );
      },
    );
  }
}