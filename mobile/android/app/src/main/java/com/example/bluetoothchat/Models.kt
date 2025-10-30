package com.example.bluetoothchat

import kotlinx.serialization.Serializable

@Serializable
data class ChatRoom(
    val id: String,
    val name: String,
    val requiresPassword: Boolean = false,
    val passwordHint: String? = null
)

@Serializable
data class ChatMessage(
    val id: String,
    val sender: String,
    val roomId: String,
    val body: String,
    val timestamp: Long
)
