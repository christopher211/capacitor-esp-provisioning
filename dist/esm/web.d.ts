import { WebPlugin } from '@capacitor/core';
import { EspProvisioningPlugin } from './definitions';
declare enum TransportType {
    TRANSPORT_BLE = 0,
    TRANSPORT_SOFTAP = 1
}
declare enum SecurityType {
    SECURITY_0 = 0,
    SECURITY_1 = 1
}
interface ESPDevice {
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
interface WifiNetwork {
    ssid: string;
    channel: string;
    rssi: string;
    security: boolean;
}
export declare class EspProvisioningWeb extends WebPlugin implements EspProvisioningPlugin {
    constructor();
    requestLocationPermissions(): Promise<unknown>;
    checkLocationPermissions(data: unknown): Promise<{
        permissionStatus: string;
    }>;
    createESPDevice(data: {
        transportType: TransportType;
        securityType: SecurityType;
        pop: string;
    }): Promise<ESPDevice>;
    scanQRCode(data: unknown): Promise<ESPDevice>;
    stopScan(): Promise<void>;
    searchBleEspDevices(data?: {
        prefix: string;
    }): Promise<{
        count: number;
        devices: ESPDevice[];
    }>;
    stopBleScan(): Promise<void>;
    searchWifiEspDevices(data?: {
        prefix: string;
    }): Promise<{
        count: number;
        devices: ESPDevice[];
    }>;
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
    }): Promise<Record<string, string>>;
}
export {};
