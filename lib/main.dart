import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'TODO Net Meter',
      theme: ThemeData(
        primarySwatch: Colors.blue,
        visualDensity: VisualDensity.adaptivePlatformDensity,
      ),
      home: MyHomePage(title: 'TODO Net Meter'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  MyHomePage({Key key, this.title}) : super(key: key);

  final String title;

  @override
  _MyHomePageState createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  static const platform = const MethodChannel('todonetmeter_android');
  String _platformVersion = '';
  bool _drawPermission = false;

  @override
  initState() {
    super.initState();

    _getDrawPermissionState();
  }

  Future<void> _getPlatformVersion() async {
    String platformVersion;
    try {
      final String result = await platform.invokeMethod("getPlatformVersion");
      platformVersion = result;
    } catch (e) {
      platformVersion = e.message;
    }

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  Future<bool> _getDrawPermissionState() async {
    bool drawPermission;

    try {
      final result = await platform.invokeMethod("getDrawPermissionState");
      drawPermission = result;
    } catch (e) {
      drawPermission = false;
    }

    setState(() {
      _drawPermission = drawPermission;
    });

    return drawPermission;
  }

  Future<bool> _reqDrawPermission() async {
    try {
      await platform.invokeMethod("reqDrawPermission");
    } catch (e) {
      print(e.message);
    }

    return _getDrawPermissionState();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        elevation: 0,
        title: Text(widget.title),
      ),
      body: Center(
        child: Column(
          children: <Widget>[
            Padding(
              padding: EdgeInsets.all(10),
              child: Text("Platform Version: "),
            ),
            Padding(
              padding: EdgeInsets.all(10),
              child: Text(
                _platformVersion,
                style: TextStyle(color: Colors.red),
              ),
            ),
            Padding(
              padding: EdgeInsets.all(10),
              child: Text("Draw over other apps permission"),
            ),
            Padding(
              padding: EdgeInsets.all(8),
              child: Icon(
                (_drawPermission ? Icons.check : Icons.block),
                size: 64,
                color: (_drawPermission ? Colors.green : Colors.red),
              ),
            ),
            MaterialButton(
              child: Text("Test Flutter Channel",
                  style: TextStyle(color: Colors.white)),
              color: Theme.of(context).primaryColor,
              onPressed: (Platform.isAndroid ? _getPlatformVersion : () {}),
            ),
            MaterialButton(
              child: Text("Request 'Draw over other apps' permission",
                  style: TextStyle(color: Colors.white)),
              color: Theme.of(context).primaryColor,
              onPressed: (Platform.isAndroid
                  ? () {
                      showReqDrawDialog(context);
                    }
                  : () {}),
            ),
            MaterialButton(
              child: Text("Show Traffic Stats",
                  style: TextStyle(color: Colors.white)),
              color: Theme.of(context).primaryColor,
              onPressed: (Platform.isAndroid
                  ? () async {
                      await platform.invokeMethod("showWidget");
                    }
                  : () {}),
            ),
          ],
        ),
      ),
    );
  }

  showReqDrawDialog(BuildContext context) async {
    if (await _getDrawPermissionState()) {
      return;
    }

    // set up the buttons
    Widget cancelButton = FlatButton(
      child: Text("No"),
      onPressed: () {
        Navigator.pop(context);
      },
    );
    Widget continueButton = FlatButton(
      child: Text("Aceptar"),
      onPressed: () async {
        await _reqDrawPermission();
        Navigator.pop(context);
      },
    );

    // set up the AlertDialog
    AlertDialog alert = AlertDialog(
      title: Text("Solicitud de permiso"),
      content:
          Text("Esta aplicación necesita permiso de superposición de pantalla"),
      actions: [
        cancelButton,
        continueButton,
      ],
    );

    // show the dialog
    await showDialog(
      context: context,
      builder: (BuildContext context) {
        return alert;
      },
    );
  }
}
