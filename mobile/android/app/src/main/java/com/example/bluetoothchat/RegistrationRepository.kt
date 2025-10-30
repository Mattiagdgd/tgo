package com.example.bluetoothchat

import android.content.Context
import android.content.SharedPreferences

class RegistrationRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("registration", Context.MODE_PRIVATE)

    var displayName: String
        get() = prefs.getString(KEY_DISPLAY_NAME, "").orEmpty()
        set(value) {
            prefs.edit().putString(KEY_DISPLAY_NAME, value).apply()
        }

    val isRegistered: Boolean
        get() = prefs.getBoolean(KEY_REGISTERED, false)

    fun register(name: String) {
        prefs.edit()
            .putString(KEY_DISPLAY_NAME, name)
            .putBoolean(KEY_REGISTERED, true)
            .apply()
    }

    fun reset() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val KEY_DISPLAY_NAME = "display_name"
        const val KEY_REGISTERED = "registered"
    }
}
