import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../services/db_service.dart';
import '../models/resource.dart';
import '../models/reservation.dart';

class AddReservationScreen extends StatefulWidget {
  @override
  _AddReservationScreenState createState() => _AddReservationScreenState();
}

class _AddReservationScreenState extends State<AddReservationScreen> {
  bool _loadingResources = false;
  List<Resource> _resources = [];
  Resource? _selectedResource;
  DateTime? _selectedDate;
  final TextEditingController _descController = TextEditingController();

  @override
  void initState() {
    super.initState();
    _loadResources();
  }

  Future<void> _loadResources() async {
    setState(() {
      _loadingResources = true;
    });
    List<Resource> resList = await DatabaseService.getResources();
    setState(() {
      _resources = resList;
      _loadingResources = false;
      if (_resources.isNotEmpty) {
        _selectedResource = _resources[0];
      }
    });
  }

  Future<void> _pickDate() async {
    DateTime now = DateTime.now();
    final DateTime? picked = await showDatePicker(
      context: context,
      initialDate: _selectedDate ?? now,
      firstDate: DateTime(2000),
      lastDate: DateTime(2100),
    );
    if (picked != null) {
      setState(() {
        // Use date only (set time to midnight for consistency)
        _selectedDate = DateTime(picked.year, picked.month, picked.day);
      });
    }
  }

  void _saveReservation() async {
    if (_selectedResource == null || _selectedDate == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Please select a resource and date')),
      );
      return;
    }
    Reservation newRes = Reservation(
      resourceId: _selectedResource!.id!,
      date: _selectedDate!,
      description: _descController.text.trim(),
    );
    int? newId = await DatabaseService.insertReservation(newRes);
    if (newId != null && newId > 0) {
      // Return true to indicate a new reservation was added
      Navigator.pop(context, true);
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error: Could not save reservation')),
      );
    }
  }

  void _goToAddResource() async {
    final result = await Navigator.pushNamed(context, '/resources');
    if (result == true) {
      // If a resource was added, reload resource list
      _loadResources();
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Add Reservation'),
        actions: [
          IconButton(
            icon: Icon(Icons.check),
            onPressed: _saveReservation,
            tooltip: 'Save',
          )
        ],
      ),
      body: _loadingResources
          ? Center(child: CircularProgressIndicator())
          : _resources.isEmpty
          ? Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Text('No resources available.'),
            SizedBox(height: 8),
            ElevatedButton(
              onPressed: _goToAddResource,
              child: Text('Add Resource'),
            ),
          ],
        ),
      )
          : SingleChildScrollView(
        padding: EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('Resource'),
            DropdownButton<Resource>(
              isExpanded: true,
              value: _selectedResource,
              items: _resources.map((Resource res) {
                return DropdownMenuItem<Resource>(
                  value: res,
                  child: Text(res.name),
                );
              }).toList(),
              onChanged: (Resource? newRes) {
                setState(() {
                  _selectedResource = newRes;
                });
              },
            ),
            SizedBox(height: 16),
            Text('Date'),
            SizedBox(
              width: double.infinity,
              child: OutlinedButton.icon(
                icon: Icon(Icons.calendar_today),
                label: Text(
                  _selectedDate == null
                      ? 'Select Date'
                      : DateFormat('dd.MM.yyyy').format(_selectedDate!),
                ),
                onPressed: _pickDate,
              ),
            ),
            SizedBox(height: 16),
            TextField(
              controller: _descController,
              decoration: InputDecoration(labelText: 'Description (optional)'),
              maxLines: 1,
            ),
          ],
        ),
      ),
    );
  }

  @override
  void dispose() {
    _descController.dispose();
    super.dispose();
  }
}
