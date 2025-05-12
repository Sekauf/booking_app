class Resource {
  int? id;
  String name;

  Resource({this.id, required this.name});

  factory Resource.fromMap(Map<dynamic, dynamic> map) {
    return Resource(
      id: map['id'] as int?,
      name: map['name'] as String,
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'id': id,
      'name': name,
    };
  }
}
