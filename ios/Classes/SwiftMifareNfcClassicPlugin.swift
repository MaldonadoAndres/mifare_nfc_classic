import Flutter
import UIKit

public class SwiftNfcClassicMifarePlugin: NSObject, FlutterPlugin {
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "nfc_classic_mifare", binaryMessenger: registrar.messenger())
    let instance = SwiftNfcClassicMifarePlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    result("iOS " + UIDevice.current.systemVersion)
  }
}
