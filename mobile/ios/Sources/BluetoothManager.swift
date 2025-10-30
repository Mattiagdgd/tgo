import Foundation
import CoreBluetooth

final class BluetoothManager: NSObject, ObservableObject {
    enum Mode {
        case peripheral
        case central
    }

    @Published private(set) var discoveredPeers: [UUID: CBPeripheral] = [:]
    @Published private(set) var receivedMessages: [ChatMessage] = []

    private var centralManager: CBCentralManager!
    private var peripheralManager: CBPeripheralManager!
    private var chatCharacteristic: CBMutableCharacteristic?
    private var chatService: CBMutableService?

    private let serviceUUID = CBUUID(string: "8BA0E088-973C-4E2F-995D-945233AE1A9F")
    private let characteristicUUID = CBUUID(string: "F2CD5B4D-9596-4D74-8134-4C83C611B1D0")

    private var subscriptions: [CBCentral] = []
    private var messageBuffer = Data()

    override init() {
        super.init()
        centralManager = CBCentralManager(delegate: self, queue: DispatchQueue(label: "bluetooth-central"))
        peripheralManager = CBPeripheralManager(delegate: self, queue: DispatchQueue(label: "bluetooth-peripheral"))
    }

    func startAdvertising(displayName: String) {
        guard peripheralManager.state == .poweredOn else { return }
        let data = [CBAdvertisementDataLocalNameKey: displayName,
                    CBAdvertisementDataServiceUUIDsKey: [serviceUUID]] as [String: Any]
        peripheralManager.startAdvertising(data)
    }

    func stopAdvertising() {
        peripheralManager.stopAdvertising()
    }

    func startScanning() {
        guard centralManager.state == .poweredOn else { return }
        centralManager.scanForPeripherals(withServices: [serviceUUID])
    }

    func stopScanning() {
        centralManager.stopScan()
    }

    func send(_ message: ChatMessage) {
        guard let data = try? JSONEncoder().encode(message) else { return }
        subscriptions.forEach { central in
            peripheralManager.updateValue(data, for: chatCharacteristic!, onSubscribedCentrals: [central])
        }
    }
}

extension BluetoothManager: CBCentralManagerDelegate {
    func centralManagerDidUpdateState(_ central: CBCentralManager) {
        if central.state == .poweredOn {
            startScanning()
        }
    }

    func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral,
                        advertisementData: [String : Any], rssi RSSI: NSNumber) {
        discoveredPeers[peripheral.identifier] = peripheral
        central.connect(peripheral)
    }

    func centralManager(_ central: CBCentralManager, didConnect peripheral: CBPeripheral) {
        peripheral.delegate = self
        peripheral.discoverServices([serviceUUID])
    }
}

extension BluetoothManager: CBPeripheralDelegate {
    func peripheral(_ peripheral: CBPeripheral, didDiscoverServices error: Error?) {
        peripheral.services?.forEach { service in
            peripheral.discoverCharacteristics([characteristicUUID], for: service)
        }
    }

    func peripheral(_ peripheral: CBPeripheral, didUpdateValueFor characteristic: CBCharacteristic, error: Error?) {
        guard let data = characteristic.value else { return }
        messageBuffer.append(data)
        if let message = try? JSONDecoder().decode(ChatMessage.self, from: messageBuffer) {
            receivedMessages.append(message)
            messageBuffer.removeAll(keepingCapacity: true)
        }
    }
}

extension BluetoothManager: CBPeripheralManagerDelegate {
    func peripheralManagerDidUpdateState(_ peripheral: CBPeripheralManager) {
        if peripheral.state == .poweredOn {
            chatCharacteristic = CBMutableCharacteristic(type: characteristicUUID,
                                                         properties: [.notify, .writeWithoutResponse],
                                                         value: nil,
                                                         permissions: [.readable, .writeable])
            chatService = CBMutableService(type: serviceUUID, primary: true)
            chatService?.characteristics = [chatCharacteristic!]
            peripheralManager.add(chatService!)
        }
    }

    func peripheralManager(_ peripheral: CBPeripheralManager, central: CBCentral,
                           didSubscribeTo characteristic: CBCharacteristic) {
        if let central = central as? CBCentral {
            subscriptions.append(central)
        }
    }
}
