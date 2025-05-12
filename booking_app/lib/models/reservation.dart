class Reservation {
  int? id;
  int resourceId;
  DateTime date;
  String? description;
  String? resourceName;  // Name of the associated resource (for display)

  Reservation({
    this.id,
    required this.resourceId,
    required this.date,
    this.description,
    this.resourceName,
  });

  factory Reservation.fromMap(Map<dynamic, dynamic> map) {
    return Reservation(
      id: map['id'] as int?,
      resourceId: map['resource_id'] as int,
      date: DateTime.fromMillisecondsSinceEpoch(map['date'] as int),
      description: map['description'] as String?,
      resourceName: map['resource_name'] as String?,
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'id': id,
      'resource_id': resourceId,
      'date': date.millisecondsSinceEpoch,
      'description': description ?? '',
    };
  }
}
