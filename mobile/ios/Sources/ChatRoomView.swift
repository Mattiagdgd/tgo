import SwiftUI

struct ChatRoomView: View {
    let room: ChatRoom
    @EnvironmentObject private var session: UserSession
    @EnvironmentObject private var bluetooth: BluetoothManager
    @EnvironmentObject private var store: ChatRoomStore
    @State private var draft: String = ""
    @State private var password: String = ""
    @State private var showPasswordPrompt = false

    var body: some View {
        VStack {
            List(messages) { message in
                VStack(alignment: .leading, spacing: 4) {
                    HStack {
                        Text(message.sender).bold()
                        Spacer()
                        Text(message.timestamp, style: .time)
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                    Text(message.body)
                }
            }

            HStack {
                TextField("Message", text: $draft)
                Button(action: send) {
                    Image(systemName: "paperplane.fill")
                }
                .disabled(draft.isEmpty)
            }
            .padding()
        }
        .navigationTitle(room.name)
        .toolbar {
            if room.requiresPassword {
                Button("Join") { showPasswordPrompt = true }
            }
        }
        .alert("Enter password", isPresented: $showPasswordPrompt) {
            SecureField("Password", text: $password)
            Button("Join") {
                if store.canJoin(room: room, password: password) {
                    password = ""
                }
            }
            Button("Cancel", role: .cancel) { password = "" }
        }
    }

    private var messages: [ChatMessage] {
        store.messages[room.id] ?? bluetooth.receivedMessages.filter { $0.roomID == room.id }
    }

    private func send() {
        let message = ChatMessage(sender: session.displayName, roomID: room.id, body: draft)
        bluetooth.send(message)
        store.addMessage(message)
        draft = ""
    }
}
