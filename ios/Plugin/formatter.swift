import Foundation
import ESPProvision

public class Formatter {
    
    public static func espSecurityStringToEnum(str: String?) -> ESPSecurity? {
        switch str?.lowercased() {
            case "1":
                return ESPSecurity.secure
            case "0":
                return ESPSecurity.unsecure
            default:
                return nil
        }
    }
    
    public static func espTransportStringToEnum(str: String?) -> ESPTransport? {
        switch str?.lowercased() {
            case "ble":
                return ESPTransport.ble
            case "softap":
                return ESPTransport.softap
            default:
                return nil
        }
    }
    
    public static func espSecurityEnumToString(enumVal: ESPSecurity) -> String {
        switch enumVal {
            case .secure:
                return "secure"
            case .unsecure:
                return "unsecure"
        }
    }
    
    public static func espTransportEnumToString(enumVal: ESPTransport) -> String {
        switch enumVal {
            case .ble:
                return "ble"
            case .softap:
                return "softap"
        }
    }
    
    public static func wifiAuthToString(auth: Espressif_WifiAuthMode) -> String {
        if (auth == .open) {
            return "open"
        } else if (auth == .wep) {
            return "wep"
        } else if (auth == .wpaPsk) {
            return "wpaPsk"
        } else if (auth == .wpa2Psk) {
            return "wpa2Psk"
        } else if (auth == .wpaWpa2Psk) {
            return "wpaWpa2Psk"
        } else {
            return "wpa2Enterprise"
        }
    }
    
    public static func serialiseEspDevice(device: ESPDevice) -> [String: String] {
        return [
            "name": device.name,
            "transport": Formatter.espTransportEnumToString(enumVal: device.transport),
            "security": Formatter.espSecurityEnumToString(enumVal: device.security)
        ]
    }
    
    public static func serialiseWifiList(wifiNetworkList: [ESPWifiNetwork]) -> [[String: String]] {
        var serialisedList = [[String:String]]()
        for wifiNetwork in wifiNetworkList {
            serialisedList.append([
                "ssid": wifiNetwork.ssid,
                "channel": String(wifiNetwork.channel),
                "rssi": String(wifiNetwork.rssi),
                "auth": self.wifiAuthToString(auth: wifiNetwork.auth)
//                public var bssid: Data = SwiftProtobuf.Internal.emptyData
//                public var auth: Espressif_WifiAuthMode = .open
//                public var unknownFields = SwiftProtobuf.UnknownStorage()
            ])
        }
        return serialisedList
    }
    
    public static func provisionStatusToString(status: ESPProvisionStatus) -> String {
        switch status {
            case ESPProvisionStatus.configApplied:
                return "Config Applied"
            case ESPProvisionStatus.success:
                return "Success"
            case ESPProvisionStatus.failure(.configurationError):
                return "The attempt to apply network configuration in ESPDevice failed with associated error."
            case ESPProvisionStatus.failure(.sessionError):
                return "Session needed for communication is not maintained with ESPDevice."
            case ESPProvisionStatus.failure(.wifiStatusAuthenticationError):
                return "Wrong Wi-Fi credentials applied to ESPDevice."
            case ESPProvisionStatus.failure(.wifiStatusDisconnected):
                return "Unable to apply Wi-Fi settings to ESPDevice with status disconnected."
            case ESPProvisionStatus.failure(.wifiStatusError):
                return "The attempt to fetch Wi-Fi status of ESPDevice failed with underlying error."
            case ESPProvisionStatus.failure(.wifiStatusNetworkNotFound):
                return "Wi-Fi network not found."
            case ESPProvisionStatus.failure(.wifiStatusUnknownError):
                return "Wi-Fi status of ESPDevice is unknown."
            default:
                return "Unknown error"
        }
    }

}
