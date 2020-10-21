# mifare_nfc_classic

A Flutter plugin for Android for reading and writing NFC cards

## Notice

- Only Mifare Classic
- Only NfcA technology
- Only works with default password

## Setup

- Requires Android API level 19 or later.
- Add [android.permission.NFC ](https://developer.android.com/reference/android/Manifest.permission.html#NFC) to your AndroidManifest.xml.

## Notes

- Remember that you shouln't write in the sector 0 of any card
- If you write in the 4th block of any sector write down what you write this is the new password for write and read in this sector
- In all cases the password parameter is optional, is only in the case the sector has a custom password

## Usage

**Check NFC Availability**

```dart
// Check availability
AVAILABILITY isAvailable = await MifareNfcClassic.availability;
switch(isAvailable){
    case(AVAILABILITY.AVAILABLE):
        //NFC is enabled.
        break;
    case(AVAILABILITY.NOT_ENABLED):
        //The phone support NFC but user has to enable it.
        break;
    case(AVAILABILITY.NOT_SUPPORTED):
        //The phone doesn't support NFC.
        break;
}
```

**Get Sector or Block Count**

```dart
int _sectorCount = await MifareNfcClassic.sectorCount;
int _blockCount = await MifareNfcClassic.blockCount;
```

**Read a specific Sector**

```dart
List<String> _sector = await MifareNfcClassic.readSector(sectorIndex:index,password:password)
/*
["FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
"FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
"FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
"FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"]
*/
```

**Read a specific Block of a specific Sector**

```dart
String _block = await MifareNfcClassic.readBlock(blockIndex: _blockIndex,password:password);
/*
"FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"
*/
```

**Read all sectors of a card**

Note: This operation take some seconds so leave the card close to the phone like for 2 seconds.

```dart
List<List<String>> _card = await MifareNfcClassic.readAll(password:password);
/*
[["FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
"FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
"FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
"FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"],
...
...
...
["FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
"FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
"FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
"FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"]]
*/
```

**Write a specific Block of a specific Sector**

```dart
bool didWrite = await  MifareNfcClassic.writeBlock(blockIndex: _blockIndex,message: _message,password:password);
/*
didWrite indicates if the operation completed successfully or not.
*/
```

**Change Password of a Sector**

```dart
await  MifareNfcClassic.changePasswordOfSector(sectorIndex: _sectorIndex,newPassword: _newPassword,password:password);
```

**Write Raw Hexadecimal**

```dart
await  MifareNfcClassic.writeRawHexToBlock(blockIndex: _blockIndex,message: rawHex,password:password);
```
