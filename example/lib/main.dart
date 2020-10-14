import 'package:flutter/material.dart';
import 'package:mifare_nfc_classic/mifare_nfc_classic.dart';

import 'package:mifare_nfc_classic_example/example_page.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final isAvailable = MifareNfcClassic.availability;
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: FutureBuilder(
        future: isAvailable,
        builder: (_, snapshot) {
          if (snapshot.connectionState == ConnectionState.done) {
            switch (snapshot.data) {
              case AVAILABILITY.AVAILABLE:
                return ExamplePage();
                break;
              case AVAILABILITY.NOT_ENABLED:
                return Scaffold(
                  body: Center(
                    child: Text('NFC Not Enabled.'),
                  ),
                );
                break;
              case AVAILABILITY.NOT_SUPPORTED:
                return Scaffold(
                  body: Center(
                    child: Text('NFC Not Supported.'),
                  ),
                );
                break;
              default:
                return Scaffold(
                  body: Center(
                    child: Text('How?'),
                  ),
                );
            }
          } else {
            return Container();
          }
        },
      ),
    );
  }
}
