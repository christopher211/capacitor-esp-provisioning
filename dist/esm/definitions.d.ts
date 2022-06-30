export declare enum TransportType {
    TRANSPORT_BLE = 0,
    TRANSPORT_SOFTAP = 1
}
export declare enum SecurityType {
    SECURITY_0 = 0,
    SECURITY_1 = 1
}
export interface ESPDevice {
    id: number;
    device: {
        name: string;
        transport_type: TransportType;
        security_type: SecurityType;
        proof_of_possesion: string;
        primary_service_uuid: string;
        capabilities: unknown;
        version: string;
    };
}
export interface WifiNetwork {
    ssid: string;
    channel: string;
    rssi: string;
    security: boolean;
}
export interface EspProvisioningPlugin {
    requestLocationPermissions(): Promise<unknown>;
    checkLocationPermissions(data?: unknown): Promise<{
        permissionStatus: string;
    }>;
    createESPDevice(data: {
        name: string;
        transportType: TransportType;
        securityType: SecurityType;
        pop: string;
    }): Promise<ESPDevice>;
    scanQRCode(data?: unknown): Promise<ESPDevice>;
    stopScan(): Promise<void>;
    searchBleEspDevices(data?: {
        prefix: string;
    }): Promise<{
        count: number;
        devices: ESPDevice[];
    }>;
    searchWifiEspDevices(data?: {
        prefix: string;
    }): Promise<{
        count: number;
        devices: ESPDevice[];
    }>;
    stopBleScan(): Promise<void>;
    connectToDevice(data: {
        device: number;
    }): Promise<Record<string, string>>;
    disconnectDevice(): Promise<void>;
    scanWifiList(data: {
        device: number;
    }): Promise<{
        count: number;
        networks: WifiNetwork[];
    }>;
    provision(data: {
        device: number;
        ssid: string;
        passphrase: string;
    }): Promise<Record<string, string>>;
}
