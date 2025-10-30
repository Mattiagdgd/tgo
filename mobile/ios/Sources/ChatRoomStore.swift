import Foundation
import Combine

final class ChatRoomStore: ObservableObject {
    @Published private(set) var rooms: [ChatRoom]
    @Published private(set) var messages: [UUID: [ChatMessage]]
    private let storageKey = "chatRooms"

    init() {
        if let data = UserDefaults.standard.data(forKey: storageKey),
           let decoded = try? JSONDecoder().decode([ChatRoom].self, from: data) {
            self.rooms = decoded
        } else {
            self.rooms = [ChatRoom(name: "General"), ChatRoom(name: "Ideas", requiresPassword: true, passwordHint: "Ask the host for the code")]
        }
        self.messages = [:]
    }

    func addRoom(name: String, password: String?) {
        var room = ChatRoom(name: name, requiresPassword: password?.isEmpty == false)
        if let password, !password.isEmpty {
            room.passwordHint = String(password.prefix(2)) + String(repeating: "*", count: max(0, password.count - 2))
            PasswordStore.shared.store(password: password, for: room.id)
        }
        rooms.append(room)
        persist()
    }

    func canJoin(room: ChatRoom, password: String?) -> Bool {
        guard room.requiresPassword else { return true }
        guard let password else { return false }
        return PasswordStore.shared.matches(password: password, for: room.id)
    }

    func addMessage(_ message: ChatMessage) {
        messages[message.roomID, default: []].append(message)
    }

    private func persist() {
        guard let data = try? JSONEncoder().encode(rooms) else { return }
        UserDefaults.standard.set(data, forKey: storageKey)
    }
}

final class PasswordStore {
    static let shared = PasswordStore()
    private let keychain = SimpleKeychain()

    func store(password: String, for roomID: UUID) {
        try? keychain.store(value: password, forKey: roomID.uuidString)
    }

    func matches(password: String, for roomID: UUID) -> Bool {
        (try? keychain.fetchValue(forKey: roomID.uuidString)) == password
    }
}

final class SimpleKeychain {
    func store(value: String, forKey key: String) throws {
        UserDefaults.standard.set(value, forKey: "kc_\(key)")
    }

    func fetchValue(forKey key: String) throws -> String {
        if let value = UserDefaults.standard.string(forKey: "kc_\(key)") {
            return value
        }
        throw NSError(domain: "SimpleKeychain", code: -1)
    }
}
