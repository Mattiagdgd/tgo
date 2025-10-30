import SwiftUI

struct RegistrationView: View {
    @EnvironmentObject private var session: UserSession
    @State private var name: String = ""

    var body: some View {
        NavigationView {
            Form {
                Section(header: Text("Choose a display name")) {
                    TextField("Display Name", text: $name)
                        .autocapitalization(.words)
                }

                Section {
                    Button(action: register) {
                        Label("Continue", systemImage: "checkmark")
                    }
                    .disabled(name.trimmingCharacters(in: .whitespaces).isEmpty)
                }
            }
            .navigationTitle("Register")
        }
    }

    private func register() {
        session.register(name: name.trimmingCharacters(in: .whitespaces))
    }
}
