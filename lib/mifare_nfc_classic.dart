import 'dart:async';
import 'package:flutter/services.dart';
import 'package:logger/logger.dart';

class MifareNfcClassic {
  static const MethodChannel _channel =
      const MethodChannel('mifare_nfc_classic');

  static Future<String?> readBlock({
    required int blockIndex,
    String? password,
  }) async {
    final response = await _channel.invokeMethod('readBlock', {
      'blockIndex': blockIndex,
      'password': password,
    });
    Logger().i(response);
    return response as String?;
  }

  static Future<void> writeBlock({
    required int blockIndex,
    required String message,
    String? password,
  }) async {
    final response = await _channel.invokeMethod('writeBlock', {
      'blockIndex': blockIndex,
      'message': message,
      'password': password,
    });
    Logger().i(response);
  }

  static Future<Map<String, dynamic>> overwriteBlock({
    required int blockIndex,
    required String message,
    String? password,
  }) async {
    final response = await _channel.invokeMethod('overwriteBlock', {
      'blockIndex': blockIndex,
      'message': message,
      'password': password,
    });
    Logger().i(response);
    return Map<String, dynamic>.from(response);
  }

  static Future<void> changePasswordOfSector({
    required int sectorIndex,
    required String newPassword,
    String? password,
  }) async {
    final response = await _channel.invokeMethod('changePasswordOfSector', {
      'sectorIndex': sectorIndex,
      'newPassword': newPassword,
      'password': password
    });
    Logger().i(response);
  }

  static Future<void> writeRawHexToBlock({
    required int blockIndex,
    required String message,
    String? password,
  }) async {
    final response = await _channel.invokeMethod(
      'writeRawHexToBlock',
      {
        'blockIndex': blockIndex,
        'message': message,
        'password': password,
      },
    );
    Logger().i(response);
  }

  static Future<List<String>> readSector(
      {required int sectorIndex, String? password}) async {
    final response = await _channel.invokeMethod('readSector', {
      'sectorIndex': sectorIndex,
      'password': password,
    });
    Logger().i(response);

    return List<String>.from(response);
  }

  static Future<int?> get sectorCount async {
    final count = await _channel.invokeMethod('sectorCount');
    Logger().i(count);
    return count;
  }

  static Future<List<List<String>>> readAll({String? password}) async {
    final response = await _channel.invokeMethod('readAll', {
      'password': password,
    }) as Map<dynamic, dynamic>;
    final listOfSectors = List<List<String>>.empty(growable: true);
    response.forEach((_, list) => listOfSectors.add(List<String>.from(list)));
    Logger().i(listOfSectors);
    return listOfSectors;
  }

  static Future<int?> get blockCount async {
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

  static AVAILABILITY _decodeMessage(String? response) {
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
