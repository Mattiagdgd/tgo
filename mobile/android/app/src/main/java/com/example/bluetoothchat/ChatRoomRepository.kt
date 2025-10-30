package com.example.bluetoothchat

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore(name = "chatrooms")

class ChatRoomRepository(private val context: Context) {
    private val json = Json { encodeDefaults = true }

    fun rooms(): Flow<List<ChatRoom>> = context.dataStore.data.map { prefs ->
        prefs[ROOMS_KEY]?.let { json.decodeFromString(it) } ?: defaultRooms()
    }

    suspend fun addRoom(room: ChatRoom) {
        context.dataStore.edit { prefs ->
            val current = prefs[ROOMS_KEY]?.let { json.decodeFromString<List<ChatRoom>>(it) } ?: defaultRooms()
            prefs[ROOMS_KEY] = json.encodeToString(current + room)
        }
    }

    suspend fun storePassword(roomId: String, password: String) {
        context.dataStore.edit { prefs ->
            val key = stringPreferencesKey("pw_$roomId")
            prefs[key] = password
        }
    }

    suspend fun validatePassword(roomId: String, password: String): Boolean {
        val key = stringPreferencesKey("pw_$roomId")
        val saved = context.dataStore.data.map { it[key] }.first()
        return saved == password
    }

    private fun defaultRooms(): List<ChatRoom> = listOf(
        ChatRoom(id = "general", name = "General"),
        ChatRoom(id = "ideas", name = "Ideas", requiresPassword = true, passwordHint = "Ask the host")
    )

    private companion object {
        val ROOMS_KEY: Preferences.Key<String> = stringPreferencesKey("rooms")
    }
}
