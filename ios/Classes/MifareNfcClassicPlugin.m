#import "NfcClassicMifarePlugin.h"
#if __has_include(<nfc_classic_mifare/nfc_classic_mifare-Swift.h>)
#import <nfc_classic_mifare/nfc_classic_mifare-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "nfc_classic_mifare-Swift.h"
#endif

@implementation NfcClassicMifarePlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftNfcClassicMifarePlugin registerWithRegistrar:registrar];
}
@end
