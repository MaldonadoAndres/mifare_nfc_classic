import 'package:carousel_slider/carousel_slider.dart';
import 'package:flutter/material.dart';
import 'package:nfc_classic_mifare/nfc_classic_mifare.dart';
import 'utils.dart';

class ExamplePage extends StatefulWidget {
  @override
  _ExamplePageState createState() => _ExamplePageState();
}

class _ExamplePageState extends State<ExamplePage> {
  final GlobalKey<FormState> _formKey = GlobalKey<FormState>();
  var hasInformation = false;
  var listInformation = [0, 0];
  var _cardSectorsInfo = [
    ["A", "B", "C", "D"]
  ];
  var _selectedSector = 0;
  var _selectedBlock;
  String message = "";

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Plugin example app'),
      ),
      body: Center(
        child: SingleChildScrollView(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.start,
            children: [
              Visibility(
                visible: hasInformation,
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    CarouselSlider.builder(
                      itemCount: _cardSectorsInfo.length,
                      options: CarouselOptions(height: 200.0),
                      itemBuilder: (context, index, realIndex) {
                        return Container(
                          child: Column(
                            mainAxisAlignment: MainAxisAlignment.center,
                            children: [
                              Text('Sector $index'),
                              Text(
                                  '${_cardSectorsInfo[index][0]}\n${_cardSectorsInfo[index][1]}\n${_cardSectorsInfo[index][2]}\n${_cardSectorsInfo[index][3]}'),
                            ],
                          ),
                        );
                      },
                    ),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                      children: [
                        Text('Sector'),
                        DropdownButton<int>(
                          hint: Text('Select a Sector'),
                          value: _selectedSector,
                          items: generateSectorList(listInformation[0])
                              .map((int value) {
                            return DropdownMenuItem<int>(
                              value: value,
                              child: Text(value.toString()),
                            );
                          }).toList(),
                          onChanged: (value) {
                            setState(() {
                              _selectedSector = value as int;
                              _selectedBlock = generateBlockList(
                                  _selectedSector, listInformation[1])[0];
                            });
                          },
                        ),
                        Text('Block'),
                        DropdownButton<int>(
                          hint: Text('Select a Block'),
                          value: _selectedBlock,
                          items: generateBlockList(
                                  _selectedSector, listInformation[1])
                              .map((int value) {
                            return DropdownMenuItem<int>(
                              value: value,
                              child: Text(value.toString()),
                            );
                          }).toList(),
                          onChanged: (value) {
                            setState(() {
                              _selectedBlock = value;
                            });
                          },
                        ),
                      ],
                    ),
                    SizedBox(height: 50.0),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                      children: [
                        TextButton(
                          style: ButtonStyle(
                            foregroundColor:
                                MaterialStateProperty.all(Colors.blue),
                          ),
                          onPressed: () async {
                            final message = await NfcClassicMifare.readBlock(
                              blockIndex: _selectedBlock,
                            );
                            await showToast(message: message as String);
                          },
                          child: Text('Read X Block Of Y Sector'),
                        ),
                        TextButton(
                          style: ButtonStyle(
                            foregroundColor:
                                MaterialStateProperty.all(Colors.blue),
                          ),
                          onPressed: () async {
                            final message = await NfcClassicMifare.readSector(
                              sectorIndex: _selectedSector,
                              passwordA: this.message,
                            );
                            await showToast(
                                message:
                                    '${message[0]}\n${message[1]}\n${message[2]}\n${message[3]}');
                          },
                          child: Text('Read X Sector'),
                        ),
                        TextButton(
                          style: ButtonStyle(
                            foregroundColor:
                                MaterialStateProperty.all(Colors.blue),
                          ),
                          onPressed: () async {
                            _cardSectorsInfo = await NfcClassicMifare.readAll();
                            setState(() {});
                          },
                          child: Text('Read All'),
                        ),
                      ],
                    ),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                      children: [
                        TextButton(
                          style: ButtonStyle(
                            foregroundColor:
                                MaterialStateProperty.all(Colors.blue),
                          ),
                          onPressed: () async => showToast(
                              message: (await NfcClassicMifare.blockCount)
                                  .toString()),
                          child: Text('Get Block Count'),
                        ),
                        TextButton(
                          style: ButtonStyle(
                            foregroundColor:
                                MaterialStateProperty.all(Colors.blue),
                          ),
                          onPressed: () async => showToast(
                              message: (await NfcClassicMifare.sectorCount)
                                  .toString()),
                          child: Text('Get Sector Count'),
                        ),
                      ],
                    ),
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 30.0),
                      margin: const EdgeInsets.symmetric(vertical: 15.0),
                      child: Form(
                        key: _formKey,
                        child: TextFormField(
                          initialValue: 'af0910ceff69'.toUpperCase(),
                          onSaved: (newValue) => message = newValue as String,
                          decoration: InputDecoration(
                            border: const OutlineInputBorder(),
                            labelText: 'Message',
                          ),
                        ),
                      ),
                    ),
                    TextButton(
                      style: ButtonStyle(
                        foregroundColor: MaterialStateProperty.all(Colors.blue),
                      ),
                      onPressed: () async {
                        _formKey.currentState?.save();
                        if (_selectedSector == 0 ||
                            (_selectedBlock + 1) % 4 == 0) {
                          showToast(
                              message: "Don't Write in this sector or block");
                        } else if (message.isEmpty) {
                          showToast(message: "Write Something");
                        } else {
                          await NfcClassicMifare.writeBlock(
                              blockIndex: _selectedBlock, message: message);
                        }
                      },
                      child: Text('Write X Block'),
                    ),
                    TextButton(
                      style: ButtonStyle(
                        foregroundColor: MaterialStateProperty.all(Colors.teal),
                      ),
                      onPressed: () async {
                        _formKey.currentState?.save();
                        if (message.isEmpty) {
                          showToast(message: "Write Something");
                        } else {
                          await NfcClassicMifare.writeRawHexToBlock(
                              blockIndex: _selectedBlock, message: message);
                        }
                      },
                      child: Text('Write X Block (Raw)'),
                    ),
                    TextButton(
                      style: ButtonStyle(
                        foregroundColor: MaterialStateProperty.all(Colors.teal),
                      ),
                      onPressed: () async {
                        _formKey.currentState?.save();
                        if (message.isEmpty) {
                          showToast(message: "Write Something");
                        } else {
                          await NfcClassicMifare.changePasswordOfSector(
                            sectorIndex: 1,
                            newPasswordA: message,
                          );
                        }
                      },
                      child: Text('Change Password'),
                    ),
                  ],
                ),
              ),
              Visibility(
                visible: !hasInformation,
                child: TextButton(
                  onPressed: () async {
                    listInformation.clear();
                    listInformation.addAll(await buildInitialAlert(context));
                    setState(() {
                      hasInformation = !hasInformation;
                    });
                  },
                  child: Text('Read Card Information'),
                  style: ButtonStyle(
                    foregroundColor: MaterialStateProperty.all(Colors.blue),
                  ),
                ),
              )
            ],
          ),
        ),
      ),
    );
  }
}
