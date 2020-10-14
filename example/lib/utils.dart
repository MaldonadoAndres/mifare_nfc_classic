import 'package:flutter/material.dart';
import 'package:fluttertoast/fluttertoast.dart';
import 'package:mifare_nfc_classic/mifare_nfc_classic.dart';
import 'package:rflutter_alert/rflutter_alert.dart';

Future<List<int>> buildInitialAlert(BuildContext context) async {
  final listInfo = List<int>();
  final _alert = Alert(
    context: context,
    title: 'Example',
    desc: 'Touch the phone with the card twice.',
  );

  _alert.show();
  listInfo.add(await MifareNfcClassic.sectorCount);
  listInfo.add(await MifareNfcClassic.blockCount);
  _alert.dismiss();
  return listInfo;
}

List<int> generateSectorList(int size) {
  return [for (var i = 0; i < size; i += 1) i];
}

List<int> generateBlockList(int sector, int size) {
  return [for (var i = sector * 4; i < (sector * 4) + 4; i += 1) i];
}

Future<void> showToast({@required String message}) async {
  await Fluttertoast.showToast(
      msg: message,
      toastLength: Toast.LENGTH_SHORT,
      gravity: ToastGravity.BOTTOM,
      timeInSecForIosWeb: 1,
      backgroundColor: Colors.blue,
      textColor: Colors.white,
      fontSize: 16.0);
}
