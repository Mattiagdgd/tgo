import Foundation

final class UserSession: ObservableObject {
    @Published var displayName: String = UserDefaults.standard.string(forKey: "displayName") ?? ""
    @Published var isRegistered: Bool = UserDefaults.standard.bool(forKey: "isRegistered")

    func register(name: String) {
        displayName = name
        isRegistered = true
        UserDefaults.standard.set(name, forKey: "displayName")
        UserDefaults.standard.set(true, forKey: "isRegistered")
    }

    func reset() {
        displayName = ""
        isRegistered = false
        UserDefaults.standard.removeObject(forKey: "displayName")
        UserDefaults.standard.set(false, forKey: "isRegistered")
    }
}
