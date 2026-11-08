import SwiftUI
import MapboxCommon

@main
struct iOSApp: App {
    init() {
        LogConfiguration.setLoggingLevelForUpTo(NSNumber(value: LoggingLevel.warning.rawValue))
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}