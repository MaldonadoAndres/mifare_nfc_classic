import 'dart:async';
import 'package:flutter/services.dart';
import 'package:logger/logger.dart';

class NfcClassicMifare {
  static const MethodChannel _channel =
      const MethodChannel('nfc_classic_mifare');

  static Future<String?> readBlock({
    required int blockIndex,
    String? passwordA,
    String? passwordB,
  }) async {
    final response = await _channel.invokeMethod('readBlock', {
      'blockIndex': blockIndex,
      'passwordA': passwordA,
      'passwordB': passwordB,
    });
    Logger().i(response);
    return response as String?;
  }

  static Future<bool> writeBlock({
    required int blockIndex,
    required String message,
    String? passwordA,
    String? passwordB,
  }) async {
    final response = await _channel.invokeMethod('writeBlock', {
      'blockIndex': blockIndex,
      'message': message,
      'passwordA': passwordA,
      'passwordB': passwordB,
    });
    Logger().i(response);
    return response as bool;
  }

  static Future<Map<String, dynamic>> overwriteBlock({
    required int blockIndex,
    required String message,
    String? passwordA,
    String? passwordB,
  }) async {
    final response = await _channel.invokeMethod('overwriteBlock', {
      'blockIndex': blockIndex,
      'message': message,
      'passwordA': passwordA,
      'passwordB': passwordB,
    });
    Logger().i(response);
    return Map<String, dynamic>.from(response);
  }

  static Future<bool> changePasswordOfSector({
    required int sectorIndex,
    required String newPasswordA,
    String? newPasswordB,
    String? passwordA,
    String? passwordB,
  }) async {
    final response = await _channel.invokeMethod('changePasswordOfSector', {
      'sectorIndex': sectorIndex,
      'newPasswordA': newPasswordA,
      'newPasswordB': newPasswordB,
      'passwordA': passwordA,
      'passwordB': passwordB,
    });
    Logger().i(response);
    return response as bool;
  }

  static Future<bool> writeRawHexToBlock({
    required int blockIndex,
    required String message,
    String? passwordA,
    String? passwordB,
  }) async {
    final response = await _channel.invokeMethod(
      'writeRawHexToBlock',
      {
        'blockIndex': blockIndex,
        'message': message,
        'passwordA': passwordA,
        'passwordB': passwordB,
      },
    );
    Logger().i(response);
    return response as bool;
  }

  static Future<List<String>> readSector(
      {required int sectorIndex, String? passwordA, String? passwordB}) async {
    final response = await _channel.invokeMethod('readSector', {
      'sectorIndex': sectorIndex,
      'passwordA': passwordA,
      'passwordB': passwordB,
    });
    Logger().i(response);

    return List<String>.from(response);
  }

  static Future<int?> get sectorCount async {
    final count = await _channel.invokeMethod('sectorCount');
    Logger().i(count);
    return count;
  }

  static Future<List<List<String>>> readAll({
    String? passwordA,
    String? passwordB,
  }) async {
    final response = await _channel.invokeMethod('readAll', {
      'passwordA': passwordA,
      'passwordB': passwordB,
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
