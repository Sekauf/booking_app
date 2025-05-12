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
    setState(() => _loading = true);
    List<Resource> list = await DatabaseService.getResources();
    setState(() {
      _resources = list;
      _loading = false;
    });
  }

  Future<void> _addResource() async {
    String name = _nameController.text.trim();
    if (name.isEmpty) {
      ScaffoldMessenger.of(context)
          .showSnackBar(SnackBar(content: Text('Resource name cannot be empty')));
      return;
    }
    int? newId = await DatabaseService.insertResource(name);
    if (newId != null && newId > 0) {
      setState(() {
        _resources.add(Resource(id: newId, name: name));
        _changesMade = true;
        _nameController.clear();
      });
      ScaffoldMessenger.of(context)
          .showSnackBar(SnackBar(content: Text('Resource added')));
    } else {
      ScaffoldMessenger.of(context)
          .showSnackBar(SnackBar(content: Text('Error: Could not add resource')));
    }
  }

  @override
  Widget build(BuildContext context) {
    return PopScope(
      // Pop bei Zurück-Geste verhindern und selbst manuell poppen
      canPop: false,
      onPopInvoked: (didPop) {
        Navigator.pop(context, _changesMade);
      },
      child: Scaffold(
        appBar: AppBar(title: Text('Resources')),
        body: _loading
            ? Center(child: CircularProgressIndicator())
            : Padding(
          padding: EdgeInsets.all(16),
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
                  itemBuilder: (ctx, i) {
                    final res = _resources[i];
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
                        final removed = res;
                        setState(() => _resources.removeAt(i));
                        bool ok = await DatabaseService.deleteResource(removed.id!);
                        if (!ok) {
                          setState(() => _resources.insert(i, removed));
                          ScaffoldMessenger.of(context).showSnackBar(
                              SnackBar(content: Text('Could not delete resource')));
                        } else {
                          _changesMade = true;
                          ScaffoldMessenger.of(context).showSnackBar(
                              SnackBar(content: Text('Resource deleted')));
                        }
                      },
                      child: ListTile(title: Text(res.name)),
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
