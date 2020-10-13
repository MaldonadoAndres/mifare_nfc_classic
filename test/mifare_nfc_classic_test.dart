import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mifare_nfc_classic/mifare_nfc_classic.dart';

void main() {
  const MethodChannel channel = MethodChannel('mifare_nfc_classic');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await MifareNfcClassic.platformVersion, '42');
  });
}
