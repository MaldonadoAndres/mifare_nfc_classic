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
    MifareNfcClassic.availability;
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
                  sectorIndex: 2,
                ),
                child: Text('Read X Sector'),
              ),
              FlatButton(
                color: Colors.blue,
                onPressed: () => MifareNfcClassic.writeBlockOfSector(
                  blockIndex: 9,
                  sectorIndex: 2,
                  message: 'Holis',
                ),
                child: Text('Write X Block'),
              ),
              FlatButton(
                color: Colors.yellow,
                onPressed: () => MifareNfcClassic.readSector(
                  sectorIndex: 2,
                ),
                child: Text('Read X Sector'),
              ),
              FlatButton(
                color: Colors.pink,
                onPressed: () => MifareNfcClassic.readAll,
                child: Text('Read All'),
              ),
              FlatButton(
                color: Colors.orange,
                onPressed: () async => await MifareNfcClassic.sectorCount,
                child: Text('Get Sector Count'),
              ),
              FlatButton(
                color: Colors.amber,
                onPressed: () async => await MifareNfcClassic.blockCount,
                child: Text('Get Block Count'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
