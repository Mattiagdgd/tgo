import SwiftUI

struct ChatRoomListView: View {
    @StateObject private var store = ChatRoomStore()
    @EnvironmentObject private var session: UserSession
    @EnvironmentObject private var bluetooth: BluetoothManager
    @State private var presentingCreateSheet = false

    var body: some View {
        NavigationView {
            List {
                Section(header: Text("Nearby")) {
                    ForEach(Array(bluetooth.discoveredPeers.values), id: \.identifier) { peripheral in
                        Text(peripheral.name ?? peripheral.identifier.uuidString)
                    }
                }

                Section(header: Text("Chat Rooms")) {
                    ForEach(store.rooms) { room in
                        NavigationLink(destination: ChatRoomView(room: room).environmentObject(store)) {
                            HStack {
                                Text(room.name)
                                if room.requiresPassword {
                                    Image(systemName: "lock.fill")
                                }
                            }
                        }
                    }
                }
            }
            .navigationTitle("Rooms")
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Broadcast") {
                        bluetooth.startAdvertising(displayName: session.displayName)
                    }
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: { presentingCreateSheet = true }) {
                        Image(systemName: "plus")
                    }
                }
            }
            .sheet(isPresented: $presentingCreateSheet) {
                CreateRoomView { name, password in
                    store.addRoom(name: name, password: password)
                }
            }
            .task {
                bluetooth.startScanning()
            }
        }
    }
}

struct CreateRoomView: View {
    var onSubmit: (String, String?) -> Void
    @Environment(\.dismiss) private var dismiss
    @State private var name: String = ""
    @State private var password: String = ""

    var body: some View {
        NavigationView {
            Form {
                Section(header: Text("Room name")) {
                    TextField("Name", text: $name)
                }

                Section(header: Text("Password (optional)")) {
                    SecureField("Password", text: $password)
                }
            }
            .navigationTitle("New Room")
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel", action: dismiss.callAsFunction)
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Create") {
                        onSubmit(name, password.isEmpty ? nil : password)
                        dismiss()
                    }
                    .disabled(name.isEmpty)
                }
            }
        }
    }
}
