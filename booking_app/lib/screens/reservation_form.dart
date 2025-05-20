import 'package:flutter/material.dart';
import '../models/reservation.dart';
import '../services/database_service.dart';

class ReservationForm extends StatefulWidget {
  final Reservation? reservation;

  const ReservationForm({Key? key, this.reservation}) : super(key: key);

  @override
  State<ReservationForm> createState() => _ReservationFormState();
}

class _ReservationFormState extends State<ReservationForm> {
  final _formKey = GlobalKey<FormState>();
  final TextEditingController _nameController = TextEditingController();
  int? _selectedTable;
  DateTime? _selectedDate;
  TimeOfDay? _selectedTime;

  @override
  void initState() {
    super.initState();
    // Wenn eine Reservierung übergeben wurde, Formular mit bestehenden Werten befüllen
    if (widget.reservation != null) {
      _nameController.text = widget.reservation!.name;
      _selectedTable = widget.reservation!.table;
      final dt = widget.reservation!.dateTime;
      _selectedDate = DateTime(dt.year, dt.month, dt.day);
      _selectedTime = TimeOfDay(hour: dt.hour, minute: dt.minute);
    }
  }

  @override
  void dispose() {
    _nameController.dispose();
    super.dispose();
  }

  // Funktion zum Speichern (Hinzufügen oder Aktualisieren)
  Future<void> _saveReservation() async {
    if (!_formKey.currentState!.validate() ||
        _selectedDate == null ||
        _selectedTime == null ||
        _selectedTable == null) {
      // Falls Felder fehlen
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Bitte alle Felder ausfüllen!')),
      );
      return;
    }
    // Datum und Uhrzeit kombinieren
    final dateTime = DateTime(
      _selectedDate!.year,
      _selectedDate!.month,
      _selectedDate!.day,
      _selectedTime!.hour,
      _selectedTime!.minute,
    );
    // Neues Reservierungsobjekt
    final newRes = Reservation(
      id: widget.reservation?.id,
      name: _nameController.text,
      table: _selectedTable!,
      dateTime: dateTime,
    );
    // Prüfen auf Doppelbuchung
    final hasConflict = await DatabaseService().hasConflictingReservation(newRes);
    if (hasConflict) {
      // Fehlerdialog bei Konflikt
      showDialog(
        context: context,
        builder: (context) => AlertDialog(
          title: const Text('Doppelbuchung'),
          content: const Text('Für diesen Tisch/Uhrzeit existiert bereits eine Reservierung.'),
          actions: [
            TextButton(
              child: const Text('OK'),
              onPressed: () => Navigator.of(context).pop(),
            ),
          ],
        ),
      );
      return;
    }
    // Je nach Modus neu anlegen oder aktualisieren
    if (widget.reservation == null) {
      await DatabaseService().addReservation(newRes);
    } else {
      await DatabaseService().updateReservation(newRes);
    }
    // Zurück zur Liste
    Navigator.of(context).pop();
  }

  // Hilfsmethode für Datumsauswahl
  Future<void> _pickDate() async {
    final now = DateTime.now();
    final picked = await showDatePicker(
      context: context,
      initialDate: _selectedDate ?? now,
      firstDate: now,
      lastDate: DateTime(now.year + 1),
    );
    if (picked != null) {
      setState(() {
        _selectedDate = picked;
      });
    }
  }

  // Hilfsmethode für Uhrzeitauswahl
  Future<void> _pickTime() async {
    final picked = await showTimePicker(
      context: context,
      initialTime: _selectedTime ?? TimeOfDay.now(),
    );
    if (picked != null) {
      setState(() {
        _selectedTime = picked;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final isEditing = widget.reservation != null;
    return Scaffold(
      appBar: AppBar(
        title: Text(isEditing ? 'Reservierung bearbeiten' : 'Neue Reservierung'),
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Form(
          key: _formKey,
          child: Column(
            children: [
              // Name-Eingabe
              TextFormField(
                controller: _nameController,
                decoration: const InputDecoration(labelText: 'Name'),
                validator: (value) => value == null || value.isEmpty ? 'Name benötigt' : null,
              ),
              const SizedBox(height: 16),
              // Auswahl Tischnummer (1-10)
              DropdownButtonFormField<int>(
                decoration: const InputDecoration(labelText: 'Tischnummer'),
                value: _selectedTable,
                items: List<DropdownMenuItem<int>>.generate(
                  10,
                      (index) => DropdownMenuItem(
                    value: index + 1,
                    child: Text('Tisch ${index + 1}'),
                  ),
                ),
                onChanged: (value) => setState(() => _selectedTable = value),
                validator: (value) => value == null ? 'Bitte wählen' : null,
              ),
              const SizedBox(height: 16),
              // Datum wählen
              Row(
                children: [
                  Expanded(
                    child: Text(
                      _selectedDate == null
                          ? 'Kein Datum gewählt'
                          : 'Datum: ${_selectedDate!.day}.${_selectedDate!.month}.${_selectedDate!.year}',
                    ),
                  ),
                  TextButton(
                    onPressed: _pickDate,
                    child: const Text('Datum wählen'),
                  ),
                ],
              ),
              // Uhrzeit wählen
              Row(
                children: [
                  Expanded(
                    child: Text(
                      _selectedTime == null
                          ? 'Keine Uhrzeit gewählt'
                          : 'Uhrzeit: ${_selectedTime!.format(context)}',
                    ),
                  ),
                  TextButton(
                    onPressed: _pickTime,
                    child: const Text('Uhrzeit wählen'),
                  ),
                ],
              ),
              const Spacer(),
              // Speichern-Button
              ElevatedButton(
                onPressed: _saveReservation,
                child: Text(isEditing ? 'Aktualisieren' : 'Speichern'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
