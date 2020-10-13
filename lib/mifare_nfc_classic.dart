import 'dart:async';

import 'package:flutter/services.dart';
import 'package:logger/logger.dart';
import 'package:flutter/material.dart';

class MifareNfcClassic {
  static const MethodChannel _channel =
      const MethodChannel('mifare_nfc_classic');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<void> read() async {
    final response = await _channel.invokeMethod('read');
    Logger().i(response);
  }

  static Future<void> readBlockOfSector({
    @required int blockIndex,
    @required int sectorIndex,
  }) async {
    final response = await _channel.invokeMethod('readBlockOfSector', {
      'blockIndex': blockIndex,
      'sectorIndex': sectorIndex,
    });
    Logger().i(response);
  }

  static Future<void> readSector({
    @required int sectorIndex,
  }) async {
    final response = await _channel.invokeMethod('readSector', {
      'sectorIndex': sectorIndex,
    });
    Logger().i(response);
  }

  static Future<void> write() async {
    final response = await _channel.invokeMethod('write');
    Logger().i(response);
  }
}
