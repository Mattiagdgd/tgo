import SwiftUI

@main
struct BluetoothChatApp: App {
    @StateObject private var session = UserSession()
    @StateObject private var bluetoothManager = BluetoothManager()

    var body: some Scene {
        WindowGroup {
            if session.isRegistered {
                ChatRoomListView()
                    .environmentObject(session)
                    .environmentObject(bluetoothManager)
            } else {
                RegistrationView()
                    .environmentObject(session)
            }
        }
    }
}
