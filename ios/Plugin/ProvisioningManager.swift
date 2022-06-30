import Foundation
import Capacitor
import ESPProvision

public class ProvisioningManager: NSObject {
    
    private var rootViewController: UIViewController
    private var deviceList:[ESPDevice] = []
    
    init(viewController: UIViewController) {
        rootViewController = viewController
    }
    
    private func createDeviceID(espDevice: ESPDevice) -> Int {
        let deviceID = self.deviceList.count
        self.deviceList.insert(espDevice, at: deviceID)
        
        return deviceID
    }
    
    private func validateDeviceID(deviceID: Int?) -> Bool {
        if (deviceID == nil || (deviceID != nil && !self.deviceList.indices.contains(deviceID!))) {
            return false
        }
        return true
    }
    
    private func formatDeviceList(deviceList: [ESPDevice]) -> [[String:Any]] {
        var serialisedDevices = [[String:Any]]();
        for device in deviceList {
            serialisedDevices.append([
                "id": createDeviceID(espDevice: device),
                "device": Formatter.serialiseEspDevice(device: device)
            ])
        }
        return serialisedDevices
    }

    public func createESPDevice(call: CAPPluginCall) {
        let name = call.getString("name")!
        let tpType = Formatter.espTransportStringToEnum(str: call.getString("transport")) ?? .ble
        let secType = Formatter.espSecurityStringToEnum(str: call.getString("security")) ?? .secure
        let pop = call.getString("pop")!
        
        ESPProvision.ESPProvisionManager.shared.createESPDevice(deviceName: name, transport: tpType, security: secType, proofOfPossession: pop){ espDevice, error in
            if (error != nil) {
                call.reject("Device could not be created", nil, error)
                return
            }
            call.resolve([
                "id": self.createDeviceID(espDevice: espDevice!),
                "device": Formatter.serialiseEspDevice(device: espDevice!)
            ])
        }
    }
    
    public func searchBleEspDevices(call: CAPPluginCall) {
        let prefix = call.getString("prefix") ?? "";
        let tpType = Formatter.espTransportStringToEnum(str: call.getString("transport")) ?? .ble;
        let secType = Formatter.espSecurityStringToEnum(str: call.getString("security")) ?? .unsecure;
        
        ESPProvision.ESPProvisionManager.shared.searchESPDevices(devicePrefix: prefix, transport:tpType, security:secType) { deviceList, error in
            if (error != nil) {
                call.reject("No devices found", nil, error)
                return
            }
            call.resolve([
                "count": deviceList!.count,
                "devices": self.formatDeviceList(deviceList: deviceList!)
            ])
        }
    }
    
    public func scanQRCode(call: CAPPluginCall) {
        DispatchQueue.main.async {
            let qrScanViewController = QRScanViewController()
            self.rootViewController.present(qrScanViewController, animated: true, completion: nil)
            
            ESPProvisionManager.shared.scanQRCode(scanView: qrScanViewController.CameraView) { espDevice, error in
                if (error != nil) {
                    call.reject("Device could not be found", nil, error);
                    return
                }
                call.resolve([
                    "id": self.createDeviceID(espDevice: espDevice!),
                    "device": Formatter.serialiseEspDevice(device: espDevice!)
                ])
            }
        }
    }
    
    public func stopScan() {
        ESPProvisionManager.shared.stopScan();
    }
    
    public func connectToDevice(call: CAPPluginCall) {
        let deviceID = call.getInt("device")
        guard self.validateDeviceID(deviceID: deviceID) else {
            call.reject("Invalid Device ID provided")
            return
        }
        
        let device = self.deviceList[deviceID!]
        device.connect() { status in
            if case ESPSessionStatus.connected = status {
                call.resolve([
                    "status": "connected"
                ])
            } else {
                call.reject("Unable to connect to device")
            }
        }
    }
    
    public func disconnectDevice() {
        
    }
    
    public func scanWifiList(call: CAPPluginCall) {
        let deviceID = call.getInt("device")
        guard self.validateDeviceID(deviceID: deviceID) else {
            call.reject("Invalid Device ID provided")
            return
        }
        
        let device = self.deviceList[deviceID!]
        device.scanWifiList { wifiList, error in
            if (error != nil) {
                call.reject("Unable to find any Wifi Networks.", nil, error)
            }
            call.resolve([
                "count": wifiList!.count,
                "networks": Formatter.serialiseWifiList(wifiNetworkList: wifiList!)
            ])
        }
    }
    
    public func provision(call: CAPPluginCall) {
        let deviceID = call.getInt("device")
        let ssid = call.getString("ssid")
        let passphrase = call.getString("passphrase") ?? ""
        
        guard self.validateDeviceID(deviceID: deviceID) else {
            call.reject("Invalid Device ID provided")
            return
        }
        guard ssid != nil else {
            call.reject("Invalid SSID provided.")
            return
        }
        
        let device = self.deviceList[deviceID!]
        device.provision(ssid: ssid!, passPhrase: passphrase) { status in
            if case ESPProvisionStatus.failure = status {
                call.reject(Formatter.provisionStatusToString(status: status))
            } else if case ESPProvisionStatus.success = status {
                call.resolve([
                    "status": Formatter.provisionStatusToString(status: status)
                ])
            }
        }
    }

}
