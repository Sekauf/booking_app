class Reservation {
  final int? id;
  final String name;
  final int table;
  final DateTime dateTime;

  Reservation({
    this.id,
    required this.name,
    required this.table,
    required this.dateTime,
  });

  // Umwandlung der Reservierung in eine Map für die DB (z.B. via MethodChannel)
  Map<String, dynamic> toMap() {
    return {
      'id': id,
      'name': name,
      'table': table,
      'datetime': dateTime.toIso8601String(),
    };
  }

  // Erzeugung einer Reservierung aus einer Map (z.B. Rückgabe aus der DB)
  factory Reservation.fromMap(Map<String, dynamic> map) {
    return Reservation(
      id: map['id'] as int?,
      name: map['name'] as String,
      table: map['table'] as int,
      dateTime: DateTime.parse(map['datetime'] as String),
    );
  }
}
