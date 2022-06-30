import { WebPlugin } from '@capacitor/core';
var TransportType;
(function (TransportType) {
    TransportType[TransportType["TRANSPORT_BLE"] = 0] = "TRANSPORT_BLE";
    TransportType[TransportType["TRANSPORT_SOFTAP"] = 1] = "TRANSPORT_SOFTAP";
})(TransportType || (TransportType = {}));
var SecurityType;
(function (SecurityType) {
    SecurityType[SecurityType["SECURITY_0"] = 0] = "SECURITY_0";
    SecurityType[SecurityType["SECURITY_1"] = 1] = "SECURITY_1";
})(SecurityType || (SecurityType = {}));
export class EspProvisioningWeb extends WebPlugin {
    constructor() {
        super({
            name: 'EspProvisioning',
            platforms: ['web'],
        });
    }
    async requestLocationPermissions() {
        console.log('[Web]: requestLocationPermissions');
        return Promise.resolve();
    }
    async checkLocationPermissions(data) {
        console.log('[Web]: checkLocationPermissions');
        return data;
    }
    async createESPDevice(data) {
        console.log('[Web]: createEspDevice', data);
        return data;
    }
    async scanQRCode(data) {
        console.log('[Web]: scanQRCode', data);
        return data;
    }
    async stopScan() {
        console.log('[Web]: stopScan');
        return Promise.resolve();
    }
    async searchBleEspDevices(data) {
        console.log('[Web]: searchBleEspDevices', data);
        return data;
    }
    async stopBleScan() {
        console.log('[Web]: stopBleScane');
        return Promise.resolve();
    }
    async searchWifiEspDevices(data) {
        console.log('[Web]: searchWifiEspDevices', data);
        return data;
    }
    async connectToDevice(data) {
        console.log('[Web]: connectToDevice', data);
        return data;
    }
    async disconnectDevice() {
        console.log('[Web]: disconnectDevice');
        return Promise.resolve();
    }
    async scanWifiList(data) {
        console.log('[Web]: scanWifiList', data);
        return data;
    }
    async provision(data) {
        console.log('[Web]: provision', data);
        return data;
    }
}
// const EspProvisioning = new EspProvisioningWeb();
// export { EspProvisioning };
// import { registerWebPlugin } from '@capacitor/core';
// registerWebPlugin(EspProvisioning);
//# sourceMappingURL=web.js.map