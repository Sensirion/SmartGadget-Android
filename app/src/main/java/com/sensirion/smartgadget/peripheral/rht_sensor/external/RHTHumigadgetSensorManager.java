package com.sensirion.smartgadget.peripheral.rht_sensor.external;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.sensirion.libble.BleManager;
import com.sensirion.libble.devices.BleDevice;
import com.sensirion.libble.listeners.devices.DeviceStateListener;
import com.sensirion.libble.listeners.services.RHTListener;
import com.sensirion.libble.services.AbstractHistoryService;
import com.sensirion.libble.services.generic.BatteryService;
import com.sensirion.libble.utils.RHTDataPoint;
import com.sensirion.smartgadget.peripheral.rht_sensor.RHTSensorManager;
import com.sensirion.smartgadget.persistence.device_name_database.DeviceNameDatabaseManager;
import com.sensirion.smartgadget.persistence.history_database.HistoryDatabaseManager;
import com.sensirion.smartgadget.utils.DeviceModel;
import com.sensirion.smartgadget.utils.view.ColorManager;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class RHTHumigadgetSensorManager implements RHTListener, DeviceStateListener {

    private static final String TAG = RHTHumigadgetSensorManager.class.getSimpleName();

    @Nullable
    private static RHTHumigadgetSensorManager mInstance = null;
    private static BleManager mBleManager;

    private final List<RHTSensorManager> mSensorManagers = Collections.synchronizedList(new LinkedList<RHTSensorManager>());

    private RHTHumigadgetSensorManager(@NonNull final Context context) {
        mBleManager = BleManager.getInstance();
        mBleManager.init(context);
        mBleManager.registerNotificationListener(this);
    }

    /**
     * Obtains the singleton object of the class.
     * The class have to be initialized with 'init()' first.
     *
     * @return the {@link com.sensirion.smartgadget.peripheral.rht_sensor.external.RHTHumigadgetSensorManager} instance.
     */
    @NonNull
    public synchronized static RHTHumigadgetSensorManager getInstance() {
        if (mInstance == null) {
            throw new IllegalStateException(String.format("%s: getInstance() -> BluetoothSensorManager has not been initialized yet.", TAG));
        }
        return mInstance;
    }

    /**
     * This method should be called before obtaining the instance.
     *
     * @param context Cannot be <code>null</code>
     */
    public static void init(@NonNull final Context context) {
        if (mInstance == null) {
            mInstance = new RHTHumigadgetSensorManager(context);
        } else {
            throw new IllegalStateException(String.format("%s: init -> %s was already initialized.", TAG, TAG));
        }
    }

    /**
     * This methods closes all bluetooth connexions and deletes it's instance.
     *
     * @param context that wants to close the bluetooth connections.
     */
    public void release(@NonNull final Context context) {
        Log.i(TAG, String.format("release -> Releasing %s", TAG));
        disconnectAllPeripherals();
        mBleManager.release(context.getApplicationContext());
        mInstance = null;
    }

    /**
     * Registers a listener of the humigadget notifications.
     *
     * @param listener of the humigadget notifications.
     */
    public synchronized void registerHumigadgetListener(@NonNull final RHTSensorManager listener) {
        if (mSensorManagers.contains(listener)) {
            Log.w(TAG, String.format("registerHumigadgetListener -> Listener %s is already in the listener list.", listener));
        } else {
            mSensorManagers.add(listener);
        }
        for (final BleDevice device : mBleManager.getConnectedBleDevices()) {
            mBleManager.registerDeviceListener(this, device.getAddress());
        }
    }

    /**
     * Unregisters a listener of the notifications.
     *
     * @param listener that doesn't want to hear more notifications.
     */
    @SuppressWarnings("unused")
    public synchronized void unregisterHumigadgetListener(@NonNull final RHTSensorManager listener) {
        mSensorManagers.remove(listener);
    }

    /**
     * Tries to establish a connection to a selected peripheral (by address)
     *
     * @param deviceAddress of the device that should be connected
     */
    public synchronized void connectPeripheral(@NonNull final String deviceAddress) {
        Log.d(TAG, String.format("connectDevice -> Bluetooth: Trying to connect device with address: %s", deviceAddress));
        if (mBleManager.isDeviceConnected(deviceAddress)) {
            Log.w(TAG, String.format("connectDevice -> Device already added: %s", deviceAddress));
            return;
        }
        mBleManager.connectDevice(deviceAddress);

        final int color = ColorManager.getInstance().getDeviceColor(deviceAddress);
        Log.i(TAG, String.format("connectDevice -> ADDING device: %s with color %d ", deviceAddress, color));

        final String deviceName = DeviceNameDatabaseManager.getInstance().readDeviceName(deviceAddress);
        final DeviceModel model = new DeviceModel(deviceAddress, color, deviceName, false);

        for (final RHTSensorManager listener : mSensorManagers) {
            listener.onConnectedRHTDevice(model);
        }
    }

    /**
     * Disconnects all the connected devices.
     */
    public synchronized void disconnectAllPeripherals() {
        final Iterable<? extends BleDevice> connectedBleDevices = mBleManager.getConnectedBleDevices();
        for (final BleDevice connectedBleDevice : connectedBleDevices) {
            mBleManager.disconnectDevice((connectedBleDevice).getAddress());
        }
    }

    /**
     * Checks if bluetooth connection is enabled on the device.
     *
     * @return <code>true</code> if it's enabled - <code>false</code> otherwise.
     */
    public boolean bluetoothIsEnabled() {
        return mBleManager.isBluetoothEnabled();
    }

    /**
     * Enables the bluetooth in case it's disconnected.
     *
     * @param activity of the activity. Can't be <code>null</code>
     */
    public void requestEnableBluetooth(@NonNull final Activity activity) {
        mBleManager.requestEnableBluetooth(activity);
    }

    /**
     * Advices the listeners that the reading of a new data point was obtained.
     *
     * @param device     {@link com.sensirion.libble.devices.BleDevice} that reported the RHT_DATA.
     * @param dataPoint  {@link com.sensirion.libble.utils.RHTDataPoint} with the RHT_DATA.
     * @param sensorName {@link java.lang.String} with the name of the sensor that reported the RHT_DATA
     */
    @Override
    public void onNewRHTValue(@NonNull final BleDevice device, @NonNull final RHTDataPoint dataPoint, @NonNull final String sensorName) {
        for (final RHTSensorManager listener : mSensorManagers) {
            listener.onNewRHTData(dataPoint.getTemperatureCelsius(), dataPoint.getRelativeHumidity(), device.getAddress());
        }
        HistoryDatabaseManager.getInstance().addRHTData(device, dataPoint, false);
    }

    /**
     * Advices the listeners that the reading of a new data point was obtained.
     *
     * @param device     {@link com.sensirion.libble.devices.BleDevice} that reported the RHT_DATA.
     * @param dataPoint  {@link com.sensirion.libble.utils.RHTDataPoint} with the historical RHT_DATA.
     * @param sensorName {@link java.lang.String} with the name of the sensor that reported the RHT_DATA
     */
    @Override
    public void onNewHistoricalRHTValue(@NonNull final BleDevice device, @NonNull final RHTDataPoint dataPoint, @NonNull final String sensorName) {
        HistoryDatabaseManager.getInstance().addRHTData(device, dataPoint, true);
    }

    /**
     * NOTE: The services and characteristics of this device are not connected yet.
     * NOTE: The connected device is removed from the library internal discovered list.
     * This method is called when a device is connected.
     *
     * @param device that was connected.
     */
    @Override
    public void onDeviceConnected(@NonNull final BleDevice device) {
        Log.i(TAG, String.format("onDeviceConnected -> Received connected device with address: %s.", device.getAddress()));
        updateConnectedDeviceList();
    }

    /**
     * This method is called when a device becomes disconnected.
     *
     * @param device that was disconnected.
     */
    @Override
    public void onDeviceDisconnected(@NonNull final BleDevice device) {
        Log.i(TAG, String.format("onDeviceConnected -> %s has lost the connected with the device: %s ", TAG, device.getAddress()));
        updateConnectedDeviceList();
    }

    /**
     * This method is called when the library discovers a new device.
     *
     * @param device that was discovered.
     */
    @Override
    public void onDeviceDiscovered(@NonNull final BleDevice device) {
        Log.i(TAG, String.format("onDeviceDiscovered -> Received discovered device with address: %s.", device.getAddress()));
        updateConnectedDeviceList();
    }

    /**
     * This method is called when all the device services are discovered.
     */
    @Override
    public void onDeviceAllServicesDiscovered(@NonNull final BleDevice device) {
        Log.i(TAG, String.format("onDeviceAllServiceDiscovered -> Device %s has discovered all its services.", device.getAddress()));
        mBleManager.registerNotificationListener(this);
    }

    /**
     * Checks that the device list doesn't have disconnected devices.
     */
    public void updateConnectedDeviceList() {
        Log.d(TAG, "updateConnectedDeviceList -> Updating connected device list.");
        for (final RHTSensorManager listener : mSensorManagers) {
            for (DeviceModel deviceModel : listener.getConnectedSensors()) {
                if (deviceModel.isInternal()) {
                    continue;
                }
                final String deviceAddress = deviceModel.getAddress();
                if (mBleManager.getConnectedDevice(deviceAddress) == null) {
                    listener.onGadgetConnectionChanged(deviceAddress, false);
                    Log.i(TAG, String.format("updateConnectedDeviceList -> Device %s was disconnected.", deviceAddress));
                }
            }
        }
    }

    /**
     * Checks if a incoming device has its elements synchronized.
     *
     * @param device to check.
     * @return <code>true</code> if the device is ready - <code>false</code> otherwise.
     */
    public boolean isDeviceReady(@NonNull final BleDevice device) {
        final boolean isSynchronized = isBatteryServiceReady(device) && isHistoryServiceReady(device);
        if (isSynchronized) {
            Log.i(TAG, String.format("isDeviceReady -> Device %s is ready.", device.getAddress()));
        } else {
            Log.w(TAG, String.format("isDeviceReady -> Device %s in not ready yet.", device.getAddress()));
        }
        return isSynchronized;
    }

    private boolean isBatteryServiceReady(@NonNull final BleDevice device) {
        final BatteryService batteryService = device.getDeviceService(BatteryService.class);
        if (batteryService == null) {
            Log.w(TAG, String.format("isBatteryServiceReady -> Battery service of %s is not ready yet.", device.getAddress()));
            return false;
        }
        final Integer batteryLevel = batteryService.getBatteryLevel();
        if (batteryLevel == null) {
            Log.i(TAG, "isBatteryServiceReady -> The battery level is unknown.");
            return false;
        }
        Log.i(TAG, String.format("isBatteryServiceReady -> The device %s has a battery level of %s%%.", device.getAddress(), batteryLevel));
        return true;
    }

    private boolean isHistoryServiceReady(@NonNull final BleDevice device) {
        final AbstractHistoryService historyService = device.getHistoryService();
        if (historyService == null) {
            Log.w(TAG, "isHistoryServiceReady -> The device does not have a history service.");
            return true; //Application is compatible with devices without history.
        }
        return historyService.isServiceReady();
    }

    /**
     * Tries to synchronize the needed device services.
     *
     * @param device that needs to be synchronized.
     */
    public void synchronizeDeviceServices(@NonNull final BleDevice device) {
        device.registerDeviceListener(this);
        final BatteryService batteryService = device.getDeviceService(BatteryService.class);
        if (batteryService != null) {
            batteryService.synchronizeService();
        }
        final AbstractHistoryService historyService = device.getHistoryService();
        if (historyService != null) {
            historyService.synchronizeService();
        }
    }
}