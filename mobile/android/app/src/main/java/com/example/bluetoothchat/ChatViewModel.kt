package com.example.bluetoothchat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel(private val registry: ServiceRegistry) : ViewModel() {
    private val selectedRoom = MutableStateFlow<ChatRoom?>(null)
    private val inputMessage = MutableStateFlow("")

    init {
        viewModelScope.launch {
            registry.bluetoothService.messages.collect { incoming ->
                ChatStore.append(incoming.roomId, incoming)
            }
        }
    }

    val state: StateFlow<ChatScreenState> = combine(
        registry.chatRoomRepository.rooms(),
        selectedRoom,
        inputMessage
    ) { rooms, room, draft ->
        val updatedMessages = room?.let { ChatStore.messages[it.id] } ?: emptyList()
        ChatScreenState(
            rooms = rooms,
            activeRoom = room,
            messages = updatedMessages,
            draft = draft
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChatScreenState())

    fun register(displayName: String) {
        registry.registrationRepository.register(displayName)
    }

    fun updateDraft(text: String) {
        inputMessage.value = text
    }

    fun selectRoom(room: ChatRoom) {
        selectedRoom.value = room
    }

    fun sendMessage() {
        val room = selectedRoom.value ?: return
        val sender = registry.registrationRepository.displayName
        if (sender.isBlank() || inputMessage.value.isBlank()) return
        val message = ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = sender,
            roomId = room.id,
            body = inputMessage.value,
            timestamp = System.currentTimeMillis()
        )
        ChatStore.append(roomId = room.id, message = message)
        registry.bluetoothService.send(message)
        inputMessage.value = ""
    }
}

data class ChatScreenState(
    val rooms: List<ChatRoom> = emptyList(),
    val activeRoom: ChatRoom? = null,
    val messages: List<ChatMessage> = emptyList(),
    val draft: String = ""
)

object ChatStore {
    private val cache: MutableMap<String, MutableList<ChatMessage>> = mutableMapOf()

    val messages: Map<String, List<ChatMessage>>
        get() = cache

    fun append(roomId: String, message: ChatMessage) {
        cache.getOrPut(roomId) { mutableListOf() }.add(message)
    }
}
