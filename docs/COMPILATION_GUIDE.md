# Compilation Guide

This guide summarises the steps required to compile the provided anonymous Bluetooth chat clients for both iOS (Xcode) and Android (Android Studio).

## Prerequisites

- Xcode 14 or later with the iOS 16 SDK. The runtime is backward-compatible with iOS 12 when you target the appropriate deployment version.
- Android Studio Giraffe or newer with the Android 14 SDK and build tools.
- Physical iOS and Android devices capable of Bluetooth Low Energy (BLE). Simulators and emulators do not provide BLE APIs.

## iOS (Xcode)

1. Clone this repository or copy the `mobile/ios` folder onto your macOS machine.
2. Launch **Xcode** and create a new SwiftUI App project named `BluetoothChat`.
3. Set the minimum deployment target to **iOS 12.0** in the project settings.
4. Drag-and-drop the Swift source files in `mobile/ios/Sources` into the project navigator. Ensure they are included in the main target.
5. Add **CoreBluetooth.framework** to your target dependencies.
6. Update `Info.plist` with:
   - `NSBluetoothAlwaysUsageDescription`
   - `NSBluetoothPeripheralUsageDescription`
   - `NSLocationWhenInUseUsageDescription`
7. Enable the **Background Modes** capability and tick **Uses Bluetooth LE accessories**.
8. Build the project with **Product > Build**. Resolve any signing prompts by selecting your development team.
9. Deploy to a physical device with **Product > Run**.

## Android (Android Studio)

1. Copy the contents of `mobile/android/app/src/main/java/com/example/bluetoothchat` into the `app/src/main/java/com/example/bluetoothchat` folder of a new Android Studio project created with the Empty Compose template.
2. Update `app/build.gradle.kts`:
   ```kotlin
   android {
       namespace = "com.example.bluetoothchat"
       compileSdk = 34

       defaultConfig {
           applicationId = "com.example.bluetoothchat"
           minSdk = 26
           targetSdk = 34
           versionCode = 1
           versionName = "1.0"
       }
   }

   dependencies {
       implementation("androidx.datastore:datastore-preferences:1.0.0")
       implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
       implementation("androidx.compose.material3:material3:1.2.1")
       implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
   }
   ```
3. Add the Kotlin serialization plugin to the module and root build scripts.
4. Declare the Bluetooth permissions in `AndroidManifest.xml` and reference `BluetoothChatApplication` as the `<application android:name>` attribute.
5. Sync Gradle and build the app with **Build > Make Project**.
6. Deploy to a physical Android device with **Run > Run 'app'**.

## Testing Bluetooth Chat

- Launch the iOS and Android applications on separate devices. The BLE service/characteristic UUIDs are aligned, enabling cross-platform discovery.
- Ensure Bluetooth and Location services are enabled on each device.
- Use the registration view to set a display name, then create or join a room.
- Messages are broadcast as JSON payloads over a shared BLE characteristic and displayed in the chat views.

## Limitations

- The provided samples are prototypes and omit encryption, message reliability, and keychain-grade password storage.
- BLE payload size is limited; large messages should be chunked and reassembled.
- Cross-platform BLE interoperability depends on device hardware and OS-level permissions.
