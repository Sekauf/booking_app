import 'package:flutter/material.dart';
import '../services/db_service.dart';
import '../models/resource.dart';

class ResourcesScreen extends StatefulWidget {
  @override
  _ResourcesScreenState createState() => _ResourcesScreenState();
}

class _ResourcesScreenState extends State<ResourcesScreen> {
  List<Resource> _resources = [];
  bool _loading = true;
  bool _changesMade = false;
  final TextEditingController _nameController = TextEditingController();

  @override
  void initState() {
    super.initState();
    _loadResources();
  }

  Future<void> _loadResources() async {
    setState(() {
      _loading = true;
    });
    List<Resource> resList = await DatabaseService.getResources();
    setState(() {
      _resources = resList;
      _loading = false;
    });
  }

  void _addResource() async {
    String name = _nameController.text.trim();
    if (name.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Resource name cannot be empty')),
      );
      return;
    }
    int? newId = await DatabaseService.insertResource(name);
    if (newId != null && newId > 0) {
      setState(() {
        _resources.add(Resource(id: newId, name: name));
      });
      _nameController.clear();
      _changesMade = true;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Resource added')),
      );
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error: Could not add resource')),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return WillPopScope(
      onWillPop: () async {
        // Return whether changes were made when popping
        Navigator.pop(context, _changesMade);
        return false;
      },
      child: Scaffold(
        appBar: AppBar(
          title: Text('Resources'),
        ),
        body: _loading
            ? Center(child: CircularProgressIndicator())
            : Padding(
          padding: EdgeInsets.all(16.0),
          child: Column(
            children: [
              TextField(
                controller: _nameController,
                decoration: InputDecoration(labelText: 'Resource name'),
              ),
              SizedBox(height: 8),
              ElevatedButton(
                onPressed: _addResource,
                child: Text('Add Resource'),
              ),
              SizedBox(height: 16),
              Expanded(
                child: _resources.isEmpty
                    ? Center(child: Text('No resources added yet.'))
                    : ListView.builder(
                  itemCount: _resources.length,
                  itemBuilder: (context, index) {
                    final res = _resources[index];
                    return Dismissible(
                      key: Key(res.id.toString()),
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
                        final removedRes = res;
                        setState(() {
                          _resources.removeAt(index);
                        });
                        bool success = await DatabaseService.deleteResource(removedRes.id!);
                        if (!success) {
                          // Deletion failed (e.g., constraint issue); restore item
                          setState(() {
                            _resources.insert(index, removedRes);
                          });
                          ScaffoldMessenger.of(context).showSnackBar(
                            SnackBar(content: Text('Error: Could not delete resource')),
                          );
                        } else {
                          _changesMade = true;
                          ScaffoldMessenger.of(context).showSnackBar(
                            SnackBar(content: Text('Resource deleted')),
                          );
                        }
                      },
                      child: ListTile(
                        title: Text(res.name),
                      ),
                    );
                  },
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  @override
  void dispose() {
    _nameController.dispose();
    super.dispose();
  }
}
