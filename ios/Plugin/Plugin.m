#import <Foundation/Foundation.h>
#import <Capacitor/Capacitor.h>

// Define the plugin using the CAP_PLUGIN Macro, and
// each method the plugin supports using the CAP_PLUGIN_METHOD macro.
CAP_PLUGIN(EspProvisioning, "EspProvisioning",
           CAP_PLUGIN_METHOD(createESPDevice, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(searchBleEspDevices, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(connectToDevice, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(disconnectDevice, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(scanWifiList, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(provision, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(scanQRCode, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(stopScan, CAPPluginReturnPromise);
)
