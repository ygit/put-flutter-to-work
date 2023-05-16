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

#if __has_include(<path_provider_foundation/PathProviderPlugin.h>)
#import <path_provider_foundation/PathProviderPlugin.h>
#else
@import path_provider_foundation;
#endif

@implementation GeneratedPluginRegistrant

+ (void)registerWithRegistry:(NSObject<FlutterPluginRegistry>*)registry {
  [HmssdkFlutterPlugin registerWithRegistrar:[registry registrarForPlugin:@"HmssdkFlutterPlugin"]];
  [PathProviderPlugin registerWithRegistrar:[registry registrarForPlugin:@"PathProviderPlugin"]];
}

@end

