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
      title: 'Flutter Demo',
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
              child: Text(_platformVersion),
            ),
            MaterialButton(
              child: Text("Test Flutter Channel",
                  style: TextStyle(color: Colors.white)),
              color: Theme.of(context).primaryColor,
              onPressed: (Platform.isAndroid ? _getPlatformVersion : () {}),
            ),
          ],
        ),
      ),
    );
  }
}
