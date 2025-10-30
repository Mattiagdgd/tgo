import Foundation

struct ChatRoom: Identifiable, Codable, Hashable {
    let id: UUID
    var name: String
    var requiresPassword: Bool
    var passwordHint: String?

    init(id: UUID = UUID(), name: String, requiresPassword: Bool = false, passwordHint: String? = nil) {
        self.id = id
        self.name = name
        self.requiresPassword = requiresPassword
        self.passwordHint = passwordHint
    }
}

struct ChatMessage: Identifiable, Codable {
    let id: UUID
    let sender: String
    let roomID: UUID
    let body: String
    let timestamp: Date

    init(id: UUID = UUID(), sender: String, roomID: UUID, body: String, timestamp: Date = Date()) {
        self.id = id
        self.sender = sender
        self.roomID = roomID
        self.body = body
        self.timestamp = timestamp
    }
}
