//
//  EndlessList.swift
//  newsfeed_app
//

import Flutter
import SwiftUI
import UIKit

struct EndlessList: View {

//  @EnvironmentObject var flutterDependencies: FlutterDependencies

  @StateObject var dataSource = ContentDataSource()
  @State var wasOpened = false

  var body: some View {
    List {
      ForEach(dataSource.items, id: \.self) { item in
        RowItem(id: item)
          .onAppear {
            dataSource.loadMoreContentIfNeeded(currentItem: item)
            if item == 10 && wasOpened == false {
              openFlutterApp()
              wasOpened = true
            }
          }.listRowSeparator(.hidden)
      }
      if dataSource.isLoadingPage {
        HStack {
          Spacer()
          ProgressView()
          Spacer()
        }
      }
    }
  }

  func openFlutterApp() {
    // Get the RootViewController.
      
    guard
      let windowScene = UIApplication.shared.connectedScenes
        .first(where: { $0.activationState == .foregroundActive && $0 is UIWindowScene }) as? UIWindowScene,
      let window = windowScene.windows.first(where: \.isKeyWindow),
      let rootViewController = window.rootViewController
    else { return }
    
    // Create the FlutterViewController.
    let flutterViewController = FlutterViewController(project: nil, initialRoute: nil, nibName: nil, bundle: nil)
      
      let flutterViewController1 = FlutterViewController(project: nil, initialRoute: "/test-route", nibName: nil, bundle: nil)
      
      GeneratedPluginRegistrant.register(with: flutterViewController.engine!)
      GeneratedPluginRegistrant.register(with: flutterViewController1.engine!)
      
      
    flutterViewController.modalPresentationStyle = .overCurrentContext
    flutterViewController.isViewOpaque = false
      
      flutterViewController1.modalPresentationStyle = .overCurrentContext
      flutterViewController1.isViewOpaque = false
    
      
//      rootViewController.present(flutterViewController1, animated: true)
//
//      flutterViewController1.present(flutterViewController, animated: true)
      
      flutterViewController.view.frame = rootViewController.view.frame
      
      rootViewController.view.addSubview(flutterViewController.view)
      
      
//      flutterViewController.view
  }
}

struct TextView: UIViewRepresentable {

    func makeUIView(context: Context) -> UIView {
        
        let flutterViewController = FlutterViewController(project: nil, initialRoute: nil, nibName: nil, bundle: nil)
        
        flutterViewController.setFlutterViewDidRenderCallback {
            
            print("pawan: inside \(flutterViewController.view.frame)")
            
            flutterViewController.view.frame = .init(origin: .zero, size: CGSize(width: 500, height: 500))
            
            print("pawan: inside after seetin \(flutterViewController.view.frame)")
        }
        
        GeneratedPluginRegistrant.register(with: flutterViewController.engine!)
        
        
        print("pawan: initial \(flutterViewController.view.frame)")
        
        flutterViewController.view.frame = .init(origin: .zero, size: CGSize(width: 500, height: 500))
        
        print("pawan: after setitng \(flutterViewController.view.frame)")
        
        flutterViewController.isViewOpaque = false
        
        return flutterViewController.view
    }

    func updateUIView(_ uiView: UIView, context: Context) {
    }
}

struct testRoute: UIViewRepresentable {

    func makeUIView(context: Context) -> UIView {
        
        let flutterViewController = FlutterViewController(project: nil, initialRoute: "/test-route", nibName: nil, bundle: nil)
        
        flutterViewController.setFlutterViewDidRenderCallback {
            flutterViewController.view.frame = .init(origin: .zero, size: CGSize(width: 500, height: 500))
        }
        
        GeneratedPluginRegistrant.register(with: flutterViewController.engine!)
        
        flutterViewController.view.frame = .init(origin: .zero, size: CGSize(width: 500, height: 500))
        
        flutterViewController.isViewOpaque = false
        
        return flutterViewController.view
    }

    func updateUIView(_ uiView: UIView, context: Context) {
    }
}
