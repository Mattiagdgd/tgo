package com.example.bluetoothchat

import android.Manifest
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MainActivity : ComponentActivity() {
    private val viewModel: ChatViewModel by viewModels {
        val registry = (application as BluetoothChatApplication).registry
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ChatViewModel(registry) as T
            }
        }
    }

    private val bluetoothScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            // You can surface results in the UI with additional state hooks.
        }
    }

    private val permissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermissions()
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ChatScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val registry = (application as BluetoothChatApplication).registry
        registry.bluetoothService.startAdvertising()
        registry.bluetoothService.startScanning(bluetoothScanCallback)
    }

    override fun onStop() {
        super.onStop()
        val registry = (application as BluetoothChatApplication).registry
        registry.bluetoothService.stopScanning(bluetoothScanCallback)
        registry.bluetoothService.stopAdvertising()
    }

    private fun checkPermissions() {
        val permissions = listOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION
        ).filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissions.isNotEmpty()) {
            permissionsLauncher.launch(permissions.toTypedArray())
        }
    }
}

@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        RegistrationCard(viewModel)
        Divider()
        RoomList(state = state, onRoomSelected = viewModel::selectRoom)
        state.activeRoom?.let {
            Divider()
            MessageComposer(state = state, onDraftChanged = viewModel::updateDraft) {
                viewModel.sendMessage()
            }
        }
    }
}

@Composable
fun RegistrationCard(viewModel: ChatViewModel) {
    val context = LocalContext.current
    val registry = (context.applicationContext as BluetoothChatApplication).registry
    var currentName by remember { mutableStateOf(registry.registrationRepository.displayName) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Display name", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = currentName,
            onValueChange = { currentName = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Name") }
        )
        Button(onClick = { viewModel.register(currentName) }, enabled = currentName.isNotBlank()) {
            Text("Save")
        }
    }
}

@Composable
fun RoomList(state: ChatScreenState, onRoomSelected: (ChatRoom) -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().weight(1f, fill = true)) {
        items(state.rooms) { room ->
            Card(onClick = { onRoomSelected(room) }) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(room.name, style = MaterialTheme.typography.titleMedium)
                    if (room.requiresPassword) {
                        Text("Password protected", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Light)
                        room.passwordHint?.let { Text("Hint: $it", style = MaterialTheme.typography.bodySmall) }
                    }
                }
            }
        }
    }
}

@Composable
fun MessageComposer(state: ChatScreenState, onDraftChanged: (String) -> Unit, onSend: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f, fill = true)) {
            items(state.messages) { message ->
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text("${message.sender} • ${message.timestamp}", style = MaterialTheme.typography.labelSmall)
                    Text(message.body, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = state.draft,
                onValueChange = onDraftChanged,
                modifier = Modifier.weight(1f),
                label = { Text("Message") }
            )
            Button(onClick = onSend, enabled = state.draft.isNotBlank()) {
                Text("Send")
            }
        }
    }
}
