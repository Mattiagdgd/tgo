package com.example.bluetoothchat

import android.content.Context

class ServiceRegistry(context: Context) {
    val registrationRepository = RegistrationRepository(context)
    val chatRoomRepository = ChatRoomRepository(context)
    val bluetoothService = BluetoothService(context, registrationRepository)
}
