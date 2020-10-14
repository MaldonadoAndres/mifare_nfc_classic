import 'dart:async';

import 'package:flutter/services.dart';
import 'package:logger/logger.dart';
import 'package:flutter/material.dart';

class MifareNfcClassic {
  static const MethodChannel _channel =
      const MethodChannel('mifare_nfc_classic');

  static Future<String> readBlockOfSector({
    @required int blockIndex,
    @required int sectorIndex,
  }) async {
    final response = await _channel.invokeMethod('readBlockOfSector', {
      'blockIndex': blockIndex,
      'sectorIndex': sectorIndex,
    });
    Logger().i(response);
    return response as String;
  }

  static Future<void> writeBlockOfSector({
    @required int blockIndex,
    @required int sectorIndex,
    @required String message,
  }) async {
    final response = await _channel.invokeMethod('writeBlockOfSector', {
      'blockIndex': blockIndex,
      'sectorIndex': sectorIndex,
      'message': message
    });
    Logger().i(response);
  }

  static Future<List<String>> readSector({@required int sectorIndex}) async {
    final response = await _channel.invokeMethod('readSector', {
      'sectorIndex': sectorIndex,
    });
    Logger().i(response);

    return List<String>.from(response);
  }

  static Future<int> get sectorCount async {
    final count = await _channel.invokeMethod('sectorCount');
    Logger().i(count);
    return count;
  }

  static Future<List<List<String>>> get readAll async {
    final response =
        await _channel.invokeMethod('readAll') as Map<dynamic, dynamic>;
    final listOfSectors = List<List<String>>();
    response.forEach((_, list) => listOfSectors.add(List<String>.from(list)));
    Logger().i(listOfSectors.runtimeType);
    Logger().i(listOfSectors);
    return listOfSectors;
  }

  static Future<int> get blockCount async {
    final count = await _channel.invokeMethod('blockCount');
    Logger().i(count);
    return count;
  }

  static Future<AVAILABILITY> get availability async {
    final response =
        _decodeMessage(await _channel.invokeMethod('isNFCEnabled'));
    Logger().i(response);
    return response;
  }

  static AVAILABILITY _decodeMessage(String response) {
    if (response == 'AVAILABLE') {
      return AVAILABILITY.AVAILABLE;
    } else if (response == 'NOT_ENABLED') {
      return AVAILABILITY.NOT_ENABLED;
    } else {
      return AVAILABILITY.NOT_SUPPORTED;
    }
  }
}

enum AVAILABILITY { AVAILABLE, NOT_ENABLED, NOT_SUPPORTED }
