import 'package:flutter/material.dart';

import 'package:mifare_nfc_classic/mifare_nfc_classic.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.spaceAround,
            children: [
              FlatButton(
                color: Colors.red,
                onPressed: () => MifareNfcClassic.readBlockOfSector(
                  blockIndex: 9,
                  sectorIndex: 2,
                ),
                child: Text('Read'),
              ),
              FlatButton(
                color: Colors.yellow,
                onPressed: () => MifareNfcClassic.readSector(
                  sectorIndex: 5,
                ),
                child: Text('Read X Sector'),
              ),
              FlatButton(
                color: Colors.blue,
                onPressed: () => MifareNfcClassic.write(),
                child: Text('Write'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
