#import "ArcfaceandroidPlugin.h"
#if __has_include(<arcfaceandroid/arcfaceandroid-Swift.h>)
#import <arcfaceandroid/arcfaceandroid-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "arcfaceandroid-Swift.h"
#endif

@implementation ArcfaceandroidPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftArcfaceandroidPlugin registerWithRegistrar:registrar];
}
@end
