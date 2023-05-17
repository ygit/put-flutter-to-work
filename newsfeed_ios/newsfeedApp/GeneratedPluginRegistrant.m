//
//  Generated file. Do not edit.
//

// clang-format off

#import "GeneratedPluginRegistrant.h"

#if __has_include(<hmssdk_flutter/HmssdkFlutterPlugin.h>)
#import <hmssdk_flutter/HmssdkFlutterPlugin.h>
#else
@import hmssdk_flutter;
#endif

//#if __has_include(<permission_handler_apple/PermissionHandlerPlugin.h>)
//#import <permission_handler_apple/PermissionHandlerPlugin.h>
//#else
//@import permission_handler_apple;
//#endif

@implementation GeneratedPluginRegistrant

+ (void)registerWithRegistry:(NSObject<FlutterPluginRegistry>*)registry {
  [HmssdkFlutterPlugin registerWithRegistrar:[registry registrarForPlugin:@"HmssdkFlutterPlugin"]];
//  [PermissionHandlerPlugin registerWithRegistrar:[registry registrarForPlugin:@"PermissionHandlerPlugin"]];
}

@end
