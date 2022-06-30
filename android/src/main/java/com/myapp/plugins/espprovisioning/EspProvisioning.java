package com.myapp.plugins.espprovisioning;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.ACCESS_WIFI_STATE;
import static android.Manifest.permission.BLUETOOTH;
import static android.Manifest.permission.BLUETOOTH_ADMIN;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.CHANGE_WIFI_STATE;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.content.pm.PackageManager;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;
import com.espressif.provisioning.DeviceConnectionEvent;
import com.espressif.provisioning.ESPConstants;
import com.espressif.provisioning.ESPDevice;
import com.espressif.provisioning.ESPProvisionManager;
import com.espressif.provisioning.WiFiAccessPoint;
import com.espressif.provisioning.listeners.BleScanListener;
import com.espressif.provisioning.listeners.ProvisionListener;
import com.espressif.provisioning.listeners.WiFiScanListener;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Objects;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;

@NativePlugin(
    permissions = { BLUETOOTH, BLUETOOTH_ADMIN, ACCESS_FINE_LOCATION, ACCESS_WIFI_STATE, CHANGE_WIFI_STATE, ACCESS_NETWORK_STATE },
    requestCodes = { EspProvisioning.REQUEST_ACCESS_FINE_LOCATION }
)
public class EspProvisioning extends Plugin {

    private ESPProvisionManager espProvisionManager;
    private Hashtable<String, ScanResult> scanResults;
    private Hashtable<Integer, ESPDevice> espDevices = new Hashtable<>();
    static final int REQUEST_ACCESS_FINE_LOCATION = 8000;
    private boolean isScanned = false;
    private static final String TAG = "EspProvisioningActivity";

    private Integer createDeviceID(ESPDevice espDevice) {
        Integer deviceID = espDevices.size();
        espDevices.put(deviceID, espDevice);
        return deviceID;
    }

    private boolean validateDeviceID(Integer deviceID) {
        Log.v(TAG, "log_validateDeviceID_espDevices=" + espDevices);
        if (espDevices.containsKey(deviceID)) {
            return true;
        }
        return false;
    }

    public void load() {
        espProvisionManager = ESPProvisionManager.getInstance(getContext().getApplicationContext());
    }

    @PluginMethod
    public void checkLocationPermissions(PluginCall call) {
        JSObject ret = new JSObject();
        if (
            ActivityCompat.checkSelfPermission(getContext().getApplicationContext(), ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            ret.put("permissionStatus", "granted");
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this.getActivity(), ACCESS_FINE_LOCATION)) {
                ret.put("permissionStatus", "denied");
            } else {
                ret.put("permissionStatus", "neverAsked");
            }
        }
        call.resolve(ret);
    }

    @PluginMethod
    public void requestLocationPermissions(PluginCall call) {
        if (
            ActivityCompat.checkSelfPermission(getContext().getApplicationContext(), ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            call.resolve();
        } else {
            saveCall(call);
            //            call.setKeepAlive(true);
            pluginRequestPermission(ACCESS_FINE_LOCATION, REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    private ESPConstants.TransportType transportTypeFromNumber(@NonNull Integer transportNumber) {
        switch (transportNumber) {
            case 0:
                return ESPConstants.TransportType.TRANSPORT_BLE;
            case 1:
                return ESPConstants.TransportType.TRANSPORT_SOFTAP;
            default:
                throw new Error("Unknown transport type: " + transportNumber);
        }
    }

    private ESPConstants.SecurityType securityTypeFromNumber(@NonNull Integer securityNumber) {
        switch (securityNumber) {
            case 0:
                return ESPConstants.SecurityType.SECURITY_0;
            case 1:
                return ESPConstants.SecurityType.SECURITY_1;
            default:
                throw new Error("Unknown security type: " + securityNumber);
        }
    }

    @PluginMethod
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public void createESPDevice(final PluginCall call) {
        if (
            ActivityCompat.checkSelfPermission(getContext().getApplicationContext(), ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            scanResults = new Hashtable<>();

            ESPConstants.TransportType transportType = this.transportTypeFromNumber(Objects.requireNonNull(call.getInt("transportType")));
            ESPConstants.SecurityType securityType = this.securityTypeFromNumber(Objects.requireNonNull(call.getInt("securityType")));
            JSObject tpType2 = call.getData();

            final String name = call.getString("name");
            final String pop = call.getString("pop");

            Log.v(TAG, "log_callPlugin_tpType2=" + tpType2);
            Log.v(TAG, "log_callPlugin_tpType=" + transportType);
            Log.v(TAG, "log_callPlugin_secType=" + securityType);
            Log.v(TAG, "log_callPlugin_name=" + name);
            Log.v(TAG, "log_callPlugin_pop=" + pop);

            if (Objects.equals(transportType, ESPConstants.TransportType.TRANSPORT_BLE)) {
                Log.v(TAG, "log_AAAAA");

                BleScanListener bleScanListener = new BleScanListener() {
                    @Override
                    public void scanStartFailed() {
                        call.reject("ScanStartFailed");
                    }

                    @Override
                    public void onPeripheralFound(BluetoothDevice device, ScanResult scanResult) {
                        if (!scanResults.containsKey(scanResult.getDevice().getName())) {
                            scanResults.put(scanResult.getDevice().getName(), scanResult);
                        }
                    }

                    @Override
                    public void scanCompleted() {
                        Log.v(TAG, "log_scanCompleted=" + scanResults);
                        Log.v(TAG, "log_name=" + name);

                        if (scanResults.containsKey(name)) {
                            ESPDevice espDevice = espProvisionManager.createESPDevice(transportType, securityType);
                            Log.v(TAG, "log_goes_here_1" + espDevice);
                            espDevice.setDeviceName(name);
                            espDevice.setProofOfPossession(pop);
                            espDevice.setBluetoothDevice(Objects.requireNonNull(scanResults.get(name)).getDevice());
                            Log.v(TAG, "log_goes_here_2=" + scanResults.get(name));
                            espDevice.setPrimaryServiceUuid(
                                Objects.requireNonNull(scanResults.get(name)).getScanRecord().getServiceUuids().get(0).getUuid().toString()
                            );
                            int deviceID = createDeviceID(espDevice);
                            JSObject ret = new JSObject();
                            JSObject device = new JSObject();
                            ret.put("id", deviceID);
                            device.put("name", espDevice.getDeviceName());
                            device.put("transport_type", espDevice.getTransportType().toString());
                            device.put("security_type", espDevice.getSecurityType().toString());
                            device.put("proof_of_possesion", espDevice.getProofOfPossession());
                            device.put("primary_service_uuid", espDevice.getPrimaryServiceUuid());
                            ret.put("device", device);
                            Log.v(TAG, "log_goes_here_3=" + ret);
                            call.resolve(ret);
                        } else {
                            call.reject("Device not found");
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        call.reject("Failure", e);
                    }
                };
                Log.v(TAG, "log_goes_here_4=");
                espProvisionManager.searchBleEspDevices(name, bleScanListener);
            } else {
                Log.v(TAG, "log_BBBBB");

                WiFiScanListener wifiScanListener = new WiFiScanListener() {
                    @Override
                    public void onWifiListReceived(ArrayList<WiFiAccessPoint> wifiList) {
                        ESPDevice espDevice = espProvisionManager.createESPDevice(transportType, securityType);
                        espDevice.setDeviceName(name);
                        espDevice.setProofOfPossession(pop);

                        int deviceID = createDeviceID(espDevice);
                        JSObject ret = new JSObject();
                        JSObject device = new JSObject();

                        for (WiFiAccessPoint accessPoint : wifiList) {
                            Log.v(TAG, "log_wifi_list=" + ret);
                            if (Objects.equals(accessPoint.getWifiName(), name)) {
                                ret.put("id", deviceID);
                                device.put("name", espDevice.getDeviceName());
                                device.put("transport_type", espDevice.getTransportType().toString());
                                device.put("security_type", espDevice.getSecurityType().toString());
                                device.put("proof_of_possesion", espDevice.getProofOfPossession());
                                ret.put("device", device);
                                ret.put("object", accessPoint);
                            }
                        }
                        call.resolve(ret);
                    }

                    @Override
                    public void onWiFiScanFailed(Exception e) {
                        call.reject("WifiScanFailed", e);
                    }
                };
                espProvisionManager.searchWiFiEspDevices(name, wifiScanListener);
            }
        } else {
            call.reject("Requires Permission: Location");
        }
    }

    @PluginMethod
    @RequiresPermission(CAMERA)
    public void scanQRCode(PluginCall call) {
        call.reject("Not yet implemented");
    }

    @PluginMethod
    @RequiresPermission(ACCESS_FINE_LOCATION)
    public void searchBleEspDevices(final PluginCall call) {
        if (
            ActivityCompat.checkSelfPermission(getContext().getApplicationContext(), ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            scanResults = new Hashtable<>();
            BleScanListener bleScanListener = new BleScanListener() {
                @Override
                public void scanStartFailed() {
                    call.reject("ScanStartFailed");
                }

                @Override
                public void onPeripheralFound(BluetoothDevice device, ScanResult scanResult) {
                    Log.v(TAG, "log_scanResultssss=" + scanResult.getScanRecord());
                    if (!scanResults.containsKey(scanResult.getDevice().getName())) {
                        scanResults.put(scanResult.getDevice().getName(), scanResult);
                    }
                }

                @Override
                public void scanCompleted() {
                    final JSObject ret = new JSObject();
                    for (ScanResult sr : scanResults.values()) {
                        ret.put(sr.getDevice().getName(), sr);
                    }
                    Log.v(TAG, "log_scanResults=" + ret);
                    call.resolve(ret);
                }

                @Override
                public void onFailure(Exception e) {
                    call.reject("Failure", e);
                }
            };
            if (call.hasOption("prefix")) {
                String prefix = call.getString("prefix");
                Log.v(TAG, "log_prefix=" + prefix);
                Log.v(TAG, "log_bleScanListener=" + bleScanListener);
                espProvisionManager.searchBleEspDevices(prefix, bleScanListener);
            } else {
                espProvisionManager.searchBleEspDevices(bleScanListener);
            }
        } else {
            call.reject("Requires Permission: Location");
        }
    }

    @PluginMethod
    @RequiresPermission(allOf = { BLUETOOTH_ADMIN, BLUETOOTH, ACCESS_FINE_LOCATION })
    public void stopBleScan(PluginCall call) {
        espProvisionManager.stopBleScan();
        call.resolve();
    }

    @PluginMethod
    @RequiresPermission(allOf = { CHANGE_WIFI_STATE, ACCESS_WIFI_STATE })
    public void searchWifiEspDevices(final PluginCall call) {
        WiFiScanListener wiFiScanListener = new WiFiScanListener() {
            @Override
            public void onWifiListReceived(ArrayList<WiFiAccessPoint> wifiList) {
                JSObject ret = new JSObject();
                for (WiFiAccessPoint accessPoint : wifiList) {
                    ret.put(accessPoint.getWifiName(), accessPoint);
                }
                call.resolve(ret);
            }

            @Override
            public void onWiFiScanFailed(Exception e) {
                call.reject("WiFi Scan failed", e);
            }
        };

        if (call.hasOption("prefix")) {
            String prefix = call.getString("prefix");
            espProvisionManager.searchWiFiEspDevices(prefix, wiFiScanListener);
        } else {
            espProvisionManager.searchWiFiEspDevices(wiFiScanListener);
        }
    }

    @PluginMethod
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public void connectToDevice(PluginCall call) throws InterruptedException {
        Integer deviceID = call.getInt("device");
        Log.v(TAG, "log_deviceID=" + deviceID);
        Log.v(TAG, "log_validateDeviceID=" + validateDeviceID(deviceID));
        if (validateDeviceID(deviceID)) {
            ESPDevice device = espDevices.get(deviceID);

            WiFiAccessPoint wiFiAccessPoint = new WiFiAccessPoint();
            wiFiAccessPoint.setWifiName(device.getDeviceName());
            wiFiAccessPoint.setPassword(device.getProofOfPossession());

            device.setWifiDevice(wiFiAccessPoint);
            saveCall(call);
//            call.setKeepAlive(true);
            EventBus.getDefault().register(this);
            try {
                device.connectToDevice();

                JSObject status = new JSObject();
                status.put("status", "connected");
                call.resolve(status);
            } catch (Exception e) {
                Log.v(TAG, "log_Exception=" + e);
            }
//            finally {
//                JSObject status = new JSObject();
//                status.put("status", "connected");
//                call.resolve(status);
//            }
        } else {
            call.reject("Invalid Device ID provided");
        }
    }

    @PluginMethod
    @RequiresPermission(allOf = { BLUETOOTH_ADMIN, BLUETOOTH, ACCESS_FINE_LOCATION })
    public void disconnectDevice(PluginCall call) {

    }

    @PluginMethod
    public void scanWifiList(final PluginCall call) {
        Integer deviceID = call.getInt("device");
        Log.v(TAG, "log_scanWifiList_deviceID=" + deviceID);
        if (validateDeviceID(deviceID)) {
            ESPDevice device = espDevices.get(deviceID);
            Log.v(TAG, "log_scanWifiList_device=" + device);
            WiFiScanListener wifiScanListener = new WiFiScanListener() {
                @Override
                public void onWifiListReceived(ArrayList<WiFiAccessPoint> wifiList) {
                    JSObject ret = new JSObject();
                    ret.put("count", wifiList.size());
                    JSArray networks = new JSArray();
                    JSObject network;
                    for (WiFiAccessPoint ap : wifiList) {
                        network = new JSObject();
                        network.put("ssid", ap.getWifiName());
                        network.put("rssi", ap.getRssi());
                        if (ap.getSecurity() == 0) {
                            network.put("security", false);
                        } else {
                            network.put("security", true);
                        }
                        networks.put(network);
                    }
                    ret.put("networks", networks);
                    call.resolve(ret);
                }

                @Override
                public void onWiFiScanFailed(Exception e) {
                    call.reject("WiFi Scan Failed", e);
                }
            };
            device.scanNetworks(wifiScanListener);
        } else {
            call.reject("Invalid Device ID provided");
        }
    }

    @PluginMethod
    public void provision(final PluginCall call) {
        Integer deviceID = call.getInt("device");
        String ssid = call.getString("ssid");
        String passphrase = call.getString("passphrase");
        if (validateDeviceID(deviceID)) {
            ESPDevice device = espDevices.get(deviceID);
            ProvisionListener provisionListener = new ProvisionListener() {
                @Override
                public void createSessionFailed(Exception e) {
                    call.reject("Create Session Failed", e);
                }

                @Override
                public void wifiConfigSent() {}

                @Override
                public void wifiConfigFailed(Exception e) {
                    call.reject("WiFi Config Failed", e);
                }

                @Override
                public void wifiConfigApplied() {}

                @Override
                public void wifiConfigApplyFailed(Exception e) {
                    call.reject("WiFi Config Apply Failed", e);
                }

                @Override
                public void provisioningFailedFromDevice(ESPConstants.ProvisionFailureReason failureReason) {
                    call.reject(failureReason.toString());
                }

                @Override
                public void deviceProvisioningSuccess() {
                    JSObject status = new JSObject();
                    status.put("status", "Success");
                    call.resolve(status);
                }

                @Override
                public void onProvisioningFailed(Exception e) {
                    call.reject("Provisioning Failed", e);
                }
            };
            device.provision(ssid, passphrase, provisionListener);
        } else {
            call.reject("Invalid Device ID provided");
        }
    }

    @Subscribe
    public void onDeviceConnectionEvent(DeviceConnectionEvent deviceConnectionEvent) {
        EventBus.getDefault().unregister(this);
        PluginCall savedCall = getSavedCall();
        Log.v(TAG, "log_savedCall=" + savedCall);
        if (deviceConnectionEvent.getEventType() == ESPConstants.EVENT_DEVICE_CONNECTED) {
            savedCall.resolve();
        } else {
            savedCall.reject("Couldn't connect to device");
        }
    }

    @Override
    protected void handleRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.handleRequestPermissionsResult(requestCode, permissions, grantResults);
        PluginCall savedCall = getSavedCall();
        if (savedCall == null) {
            return;
        }
        for (int result : grantResults) {
            if (result == PackageManager.PERMISSION_DENIED) {
                savedCall.reject("Permission Denied by User");
                return;
            }
        }
        if (requestCode == REQUEST_ACCESS_FINE_LOCATION) {
            savedCall.resolve();
        }
    }
}
