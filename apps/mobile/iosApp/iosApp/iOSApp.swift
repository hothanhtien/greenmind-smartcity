import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
                .onOpenURL { url in
                    PaymentDeeplinkBus.shared.publish(url: url.absoluteString)
                }
        }
    }
}
