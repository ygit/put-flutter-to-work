//
//  NewsfeedApp.swift
//  newsfeed_app
//


//import FlutterPluginRegistrant
import SwiftUI

//import Flutter

//class FlutterDependencies: ObservableObject {
////  let npsFlutterEngine = FlutterEngine(name: "flus_engine")
//
//  init() {
//    // Prepare a Flutter engine in advance.
////    npsFlutterEngine.run(withEntrypoint: nil, libraryURI: nil, initialRoute: "/test-route")
//    GeneratedPluginRegistrant.register(with: npsFlutterEngine)
//  }
//}

@main
struct NewsfeedApp: App {

  // flutterDependencies will be injected using EnvironmentObject
//  @StateObject var flutterDependencies = FlutterDependencies()

  var body: some Scene {
    WindowGroup {
        
        ZStack {
            Color.yellow
            
            VStack {
                
                TextView()
                    .frame(width: 500, height: 500)
                
                testRoute()
                    .frame(width: 500, height: 500)
            }
        }
        
        
        
//        .environmentObject(flutterDependencies)
    }
  }
}

extension String {
  func localized() -> String {
    return NSLocalizedString(
      self, tableName: "Localizable", bundle: .main, value: self, comment: self)
  }
}
