package com.example.bluetoothchat

import android.app.Application

class BluetoothChatApplication : Application() {
    lateinit var registry: ServiceRegistry
        private set

    override fun onCreate() {
        super.onCreate()
        registry = ServiceRegistry(applicationContext)
    }
}
