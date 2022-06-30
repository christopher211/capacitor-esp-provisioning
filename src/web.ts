import { WebPlugin } from '@capacitor/core';
import { EspProvisioningPlugin } from './definitions';

enum TransportType {
  TRANSPORT_BLE,
  TRANSPORT_SOFTAP,
}

enum SecurityType {
  SECURITY_0,
  SECURITY_1,
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

export class EspProvisioningWeb
  extends WebPlugin
  implements EspProvisioningPlugin
{
  constructor() {
    super({
      name: 'EspProvisioning',
      platforms: ['web'],
    });
  }

  async requestLocationPermissions(): Promise<unknown> {
    console.log('[Web]: requestLocationPermissions');
    return Promise.resolve();
  }

  async checkLocationPermissions(
    data: unknown,
  ): Promise<{ permissionStatus: string }> {
    console.log('[Web]: checkLocationPermissions');
    return data as {} as { permissionStatus: string };
  }

  async createESPDevice(data: {
    transportType: TransportType;
    securityType: SecurityType;
    pop: string;
  }): Promise<ESPDevice> {
    console.log('[Web]: createEspDevice', data);
    return data as {} as ESPDevice;
  }

  async scanQRCode(data: unknown): Promise<ESPDevice> {
    console.log('[Web]: scanQRCode', data);
    return data as ESPDevice;
  }

  async stopScan(): Promise<void> {
    console.log('[Web]: stopScan');
    return Promise.resolve();
  }

  async searchBleEspDevices(data?: {
    prefix: string;
  }): Promise<{ count: number; devices: ESPDevice[] }> {
    console.log('[Web]: searchBleEspDevices', data);
    return data as {} as { count: number; devices: ESPDevice[] };
  }

  async stopBleScan(): Promise<void> {
    console.log('[Web]: stopBleScane');
    return Promise.resolve();
  }

  async searchWifiEspDevices(data?: {
    prefix: string;
  }): Promise<{ count: number; devices: ESPDevice[] }> {
    console.log('[Web]: searchWifiEspDevices', data);
    return data as {} as { count: number; devices: ESPDevice[] };
  }

  async connectToDevice(data: {
    device: number;
  }): Promise<Record<string, string>> {
    console.log('[Web]: connectToDevice', data);
    return data as {};
  }

  async disconnectDevice(): Promise<void> {
    console.log('[Web]: disconnectDevice');
    return Promise.resolve();
  }

  async scanWifiList(data: {
    device: number;
  }): Promise<{ count: number; networks: WifiNetwork[] }> {
    console.log('[Web]: scanWifiList', data);
    return data as {} as { count: number; networks: WifiNetwork[] };
  }

  async provision(data: { device: number }): Promise<Record<string, string>> {
    console.log('[Web]: provision', data);
    return data as {};
  }
}

// const EspProvisioning = new EspProvisioningWeb();

// export { EspProvisioning };

// import { registerWebPlugin } from '@capacitor/core';
// registerWebPlugin(EspProvisioning);
