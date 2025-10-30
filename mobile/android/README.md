# Android Anonymous Bluetooth Chat

This module contains Kotlin/Jetpack Compose samples that implement the anonymous Bluetooth chat application.

## Project setup

1. Create a new **Empty Compose Activity** project in Android Studio (Giraffe or newer) named `BluetoothChat` with the package `com.example.bluetoothchat` and minimum SDK 26.
2. Enable Kotlin serialization in your `build.gradle` files and add the following dependencies:
   ```kotlin
   implementation("androidx.datastore:datastore-preferences:1.0.0")
   implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
   implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
   implementation("androidx.compose.material3:material3:1.2.1")
   ```
3. Add the `kotlinx-serialization` Gradle plugin to the module-level `build.gradle.kts` file:
   ```kotlin
   plugins {
       id("com.android.application")
       kotlin("android")
       kotlin("plugin.serialization")
   }
   ```
4. Replace the generated Kotlin files with the sources contained in `mobile/android/app/src/main/java/com/example/bluetoothchat`.
5. Update `AndroidManifest.xml` to include the following permissions:
   ```xml
   <uses-permission android:name="android.permission.BLUETOOTH" />
   <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
   <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
   <uses-permission android:name="android.permission.BLUETOOTH_ADVERTISE" />
   <uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
   <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
   ```
   On Android 12+, declare the Bluetooth permissions with `android:usesPermissionFlags="neverForLocation"` when appropriate.
6. Declare `BluetoothChatApplication` in the manifest:
   ```xml
   <application
       android:name=".BluetoothChatApplication"
       ... >
   </application>
   ```
7. Opt-in to the `BLUETOOTH_ADVERTISE`, `BLUETOOTH_SCAN`, and `BLUETOOTH_CONNECT` runtime permissions at startup (already handled in `MainActivity`).

## Running on device

1. Pair at least two Android 8.0+ devices.
2. Install and run the app on each device from Android Studio using **Run > Run 'app'**.
3. Register a display name on each device, then create or join a room.
4. Use the **Send** button to publish messages. Messages are propagated via BLE characteristics to nearby subscribers.

## Notes

- Android emulators do not support Bluetooth; physical hardware is required.
- Production-ready apps should encrypt BLE payloads and implement reliable messaging with acknowledgements and chunking.
- The included repositories store passwords using DataStore and should be replaced with stronger storage in real deployments.
