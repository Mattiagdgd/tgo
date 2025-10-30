package com.example.bluetoothchat

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.charset.Charset
import java.util.UUID

class BluetoothService(context: Context, private val registrationRepository: RegistrationRepository) {
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val adapter: BluetoothAdapter = bluetoothManager.adapter
    private val advertiser: BluetoothLeAdvertiser? = adapter.bluetoothLeAdvertiser
    private val scope = CoroutineScope(Dispatchers.IO)
    private val json = Json { encodeDefaults = true }

    private val _messages = MutableSharedFlow<ChatMessage>(extraBufferCapacity = 32)
    val messages = _messages.asSharedFlow()

    private val gattServerCallback = object : BluetoothGattServerCallback() {
        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice?,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic?,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            if (characteristic?.uuid == CHAT_CHARACTERISTIC_UUID && value != null) {
                val decoded = json.decodeFromString<ChatMessage>(value.toString(Charset.defaultCharset()))
                scope.launch { _messages.emit(decoded) }
            }
        }
    }

    private val gattServer: BluetoothGattServer? = bluetoothManager.openGattServer(context, gattServerCallback)
    private val chatCharacteristic = BluetoothGattCharacteristic(
        CHAT_CHARACTERISTIC_UUID,
        BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_NOTIFY,
        BluetoothGattCharacteristic.PERMISSION_WRITE
    )

    private val chatService = BluetoothGattService(CHAT_SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY).apply {
        addCharacteristic(chatCharacteristic)
    }

    init {
        gattServer?.addService(chatService)
    }

    fun startAdvertising() {
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setConnectable(true)
            .build()

        val displayName = registrationRepository.displayName.ifBlank { "Anonymous" }
        val data = AdvertiseData.Builder()
            .addServiceUuid(ParcelUuid(CHAT_SERVICE_UUID))
            .addServiceData(ParcelUuid(CHAT_SERVICE_UUID), displayName.toByteArray(Charset.defaultCharset()))
            .build()

        advertiser?.startAdvertising(settings, data, advertiseCallback)
    }

    fun stopAdvertising() {
        advertiser?.stopAdvertising(advertiseCallback)
    }

    fun startScanning(callback: ScanCallback) {
        val filters = listOf(ScanFilter.Builder().setServiceUuid(ParcelUuid(CHAT_SERVICE_UUID)).build())
        val settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
        adapter.bluetoothLeScanner.startScan(filters, settings, callback)
    }

    fun stopScanning(callback: ScanCallback) {
        adapter.bluetoothLeScanner.stopScan(callback)
    }

    fun send(message: ChatMessage) {
        val payload = json.encodeToString(message).toByteArray(Charset.defaultCharset())
        chatCharacteristic.value = payload
        gattServer?.connectedDevices?.forEach { device ->
            gattServer.notifyCharacteristicChanged(device, chatCharacteristic, false)
        }
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
        }
    }

    companion object {
        val CHAT_SERVICE_UUID: UUID = UUID.fromString("8BA0E088-973C-4E2F-995D-945233AE1A9F")
        val CHAT_CHARACTERISTIC_UUID: UUID = UUID.fromString("F2CD5B4D-9596-4D74-8134-4C83C611B1D0")
    }
}
