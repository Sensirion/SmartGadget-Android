package com.sensirion.smartgadget.peripheral.rht_sensor.external;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.sensirion.libsmartgadget.Gadget;
import com.sensirion.libsmartgadget.GadgetDownloadService;
import com.sensirion.libsmartgadget.GadgetListener;
import com.sensirion.libsmartgadget.GadgetManager;
import com.sensirion.libsmartgadget.GadgetManagerCallback;
import com.sensirion.libsmartgadget.GadgetService;
import com.sensirion.libsmartgadget.GadgetValue;
import com.sensirion.libsmartgadget.smartgadget.GadgetManagerFactory;
import com.sensirion.libsmartgadget.smartgadget.SHT3xHumidityService;
import com.sensirion.libsmartgadget.smartgadget.SHT3xTemperatureService;
import com.sensirion.libsmartgadget.smartgadget.SHTC1TemperatureAndHumidityService;
import com.sensirion.libsmartgadget.smartgadget.SensorTagTemperatureAndHumidityService;
import com.sensirion.libsmartgadget.utils.BLEUtility;
import com.sensirion.smartgadget.peripheral.rht_sensor.HumiSensorListener;
import com.sensirion.smartgadget.peripheral.rht_utils.RHTDataPoint;
import com.sensirion.smartgadget.persistence.device_name_database.DeviceNameDatabaseManager;
import com.sensirion.smartgadget.persistence.history_database.HistoryDatabaseManager;
import com.sensirion.smartgadget.utils.DeviceModel;
import com.sensirion.smartgadget.utils.view.ColorManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RHTHumigadgetSensorManager implements GadgetManagerCallback, GadgetListener {

    private static final String TAG = RHTHumigadgetSensorManager.class.getSimpleName();

    @Nullable
    private static RHTHumigadgetSensorManager mInstance = null;
    @NonNull
    private final GadgetManager mGadgetManager;
    private final Set<HumiSensorListener> mSensorListeners = Collections.synchronizedSet(new HashSet<HumiSensorListener>());
    private final Map<String, Gadget> mDiscoveredGadgets = Collections.synchronizedMap(new HashMap<String, Gadget>());
    private final Map<String, Gadget> mConnectedGadgets = Collections.synchronizedMap(new HashMap<String, Gadget>());
    private final Map<AggregatorType, RHTValueAggregator> mAggregators = new HashMap<>();

    private HumiGadgetConnectionStateListener mConnectionStateListener;
    // TODO: find a better way to filter devices, right now we have to register the service UUID
    // and then still enter the exact device names here. Better would be to keep everything within
    // libsmartgadget.
    private String[] mHumiGadgetNameFilter = new String[]{"SHTC1 smart gadget",
            "SHTC1 smart gadget\u0002", "Smart Humigadget", "SensorTag"};
    private String[] mHumiGadgetServiceUUIDFilter = new String[]{SHT3xTemperatureService.SERVICE_UUID,
            SHT3xHumidityService.SERVICE_UUID,
            SHTC1TemperatureAndHumidityService.SERVICE_UUID,
            SensorTagTemperatureAndHumidityService.SERVICE_UUID};

    public enum AggregatorType {
        LIVE,
        HISTORY
    }

    private RHTHumigadgetSensorManager(@NonNull final Context context) {
        mGadgetManager = GadgetManagerFactory.create(this);
        mGadgetManager.initialize(context.getApplicationContext());
        mAggregators.put(AggregatorType.LIVE, new LiveValueAggregator());
        mAggregators.put(AggregatorType.HISTORY, new HistoryValueAggregator());
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
        disconnectAllGadgets();
        mGadgetManager.release(context.getApplicationContext());
        mInstance = null;
    }

    /**
     * Registers a listener of the humigadget notifications.
     *
     * @param listener of the humigadget notifications.
     */
    public void registerHumigadgetListener(@NonNull final HumiSensorListener listener) {
        mSensorListeners.add(listener);
    }

    /**
     * Unregisters a listener of the notifications.
     *
     * @param listener that doesn't want to hear more notifications.
     */
    @SuppressWarnings("unused")
    public void unregisterHumigadgetListener(@NonNull final HumiSensorListener listener) {
        mSensorListeners.remove(listener);
    }

    public void setConnectionStateListener(@Nullable HumiGadgetConnectionStateListener connectionStateListener) {
        mConnectionStateListener = connectionStateListener;
    }

    /**
     * Tries to establish a connection to a selected peripheral (by address)
     *
     * @param deviceAddress of the device that should be connected
     */
    public synchronized void connectToGadget(@NonNull final String deviceAddress) {
        Log.d(TAG, String.format("connectDevice -> Bluetooth: Trying to connect device with address: %s", deviceAddress));
        final Gadget gadget = mDiscoveredGadgets.get(deviceAddress);
        if (gadget == null) {
            Log.w(TAG, String.format("connectDevice -> device with address: %s not in discovered gadgets list", deviceAddress));
            // TODO: should we notify about a disconnection without having sent a connect?
            // (we don't have way to report a failure)
            notifyGadgetConnectionChanged(deviceAddress, null, false);
            return;
        }
        gadget.addListener(this);
        gadget.connect();
    }

    /**
     * Disconnects all connected devices.
     */
    public synchronized void disconnectAllGadgets() {
        for (final Gadget gadget : mConnectedGadgets.values()) {
            gadget.disconnect();
        }
    }

    public Set<GadgetModel> getConnectedDevices() {
        Set<GadgetModel> connectedDevices = new HashSet<>(mConnectedGadgets.size());
        synchronized (mConnectedGadgets) {
            for (final Gadget gadget : mConnectedGadgets.values()) {
                connectedDevices.add(new GadgetModel(gadget.getAddress(), true, gadget.getName()));
            }
        }
        return connectedDevices;
    }

    public int getConnectedDevicesCount() {
        return mConnectedGadgets.size();
    }

    @Nullable
    public DeviceModel getConnectedDevice(@NonNull final String deviceAddress) {
        final Gadget gadget = mConnectedGadgets.get(deviceAddress);
        if (gadget == null) return null;
        return createDeviceModel(deviceAddress);
    }

    public Gadget getConnectedGadget(@NonNull final String deviceAddress) {
        return mConnectedGadgets.get(deviceAddress);
    }

    /**
     * Checks if bluetooth connection is enabled on the device.
     *
     * @param context The android context.
     * @return <code>true</code> if it's enabled (and context is valid) - <code>false</code> otherwise.
     */
    public boolean bluetoothIsEnabled(@NonNull final Context context) {
        return BLEUtility.isBLEEnabled(context.getApplicationContext());
    }

    /**
     * Runtime request for ACCESS_FINE_LOCATION. This is required on Android 6.0 and higher in order
     * to perform BLE scans.
     *
     * @param requestingActivity The activity requesting the permission.
     * @param requestCode        The request code used to deliver the user feedback to the calling
     *                           activity.
     */
    @SuppressWarnings("unused")
    public void requestScanningPermission(@NonNull final Activity requestingActivity,
                                          final int requestCode) {
        BLEUtility.requestScanningPermission(requestingActivity, requestCode);
    }

    /**
     * Enables bluetooth in case it's disconnected.
     *
     * @param activity of the activity. Can't be <code>null</code>
     */
    public void requestEnableBluetooth(@NonNull final Activity activity) {
        BLEUtility.requestEnableBluetooth(activity);
    }

    /*
     * Implementation of {@link GadgetManagerCallback}
     */

    @Override
    public void onGadgetManagerInitialized() {
        // Yeaiii.... Ready to go
    }

    @Override
    public void onGadgetManagerInitializationFailed() {
        throw new IllegalStateException("Failed to initialize libSmartGadget");
    }

    /**
     * This method is called when the library discovers a new device.
     *
     * @param gadget that was discovered.
     * @param rssi   the received signal strength
     */
    @Override
    public void onGadgetDiscovered(final Gadget gadget, final int rssi) {
        mDiscoveredGadgets.put(gadget.getAddress(), gadget);
        if (mConnectionStateListener != null) {
            final GadgetModel gadgetModel = new GadgetModel(gadget.getAddress(), false, gadget.getName());
            gadgetModel.setRssi(rssi);
            mConnectionStateListener.onGadgetDiscovered(gadgetModel);
        }
    }

    @Override
    public void onGadgetDiscoveryFailed() {
        if (mConnectionStateListener != null) {
            mConnectionStateListener.onGadgetDiscoveryFailed();
        }
    }

    @Override
    public void onGadgetDiscoveryFinished() {
        if (mConnectionStateListener != null) {
            mConnectionStateListener.onGadgetDiscoveryFinished();
        }
    }

    /*
     * Implementation of {@link GadgetListener}
     */

    /**
     * The services and characteristics of this device are connected and ready to use.
     * NOTE: The connected device is removed from the discovered list.
     * This method is called when a device is connected and all the services and characteristics
     * were discovered.
     *
     * @param gadget that was connected.
     */
    @Override
    public void onGadgetConnected(@NonNull final Gadget gadget) {
        mDiscoveredGadgets.remove(gadget.getAddress());
        mConnectedGadgets.put(gadget.getAddress(), gadget);

        gadget.subscribeAll();

        notifyGadgetConnectionChanged(gadget, true);
    }

    /**
     * This method is called when a device becomes disconnected.
     *
     * @param gadget that was disconnected.
     */
    @Override
    public void onGadgetDisconnected(@NonNull final Gadget gadget) {
        mConnectedGadgets.remove(gadget.getAddress());

        notifyGadgetConnectionChanged(gadget, false);
    }

    @Override
    public void onGadgetValuesReceived(@NonNull final Gadget gadget,
                                       @NonNull final GadgetService service,
                                       @NonNull final GadgetValue[] values) {
        aggregateRHTAndNotify(gadget.getAddress(), values, AggregatorType.LIVE);
    }

    @Override
    public void onGadgetDownloadDataReceived(@NonNull final Gadget gadget,
                                             @NonNull final GadgetDownloadService service,
                                             @NonNull final GadgetValue[] values,
                                             final int progress) {
        aggregateRHTAndNotify(gadget.getAddress(), values, AggregatorType.HISTORY);
    }

    @Override
    public void onSetGadgetLoggingEnabledFailed(@NonNull final Gadget gadget,
                                                @NonNull final GadgetDownloadService service) {
        Log.w(TAG, String.format("Failed to set logging state in gadget %s", gadget.getAddress()));
    }

    @Override
    public void onSetLoggerIntervalFailed(@NonNull final Gadget gadget,
                                          @NonNull final GadgetDownloadService service) {
        Log.w(TAG, String.format("Failed to set logger interval in gadget %s", gadget.getAddress()));
    }

    @Override
    public void onDownloadFailed(@NonNull final Gadget gadget,
                                 @NonNull final GadgetDownloadService service) {
        Log.w(TAG, String.format("Failed to perform download from gadget %s", gadget.getAddress()));
    }

    /*
     * Private Helpers
     */

    private DeviceModel createDeviceModel(final String deviceAddress) {
        final int color = ColorManager.getInstance().getDeviceColor(deviceAddress);
        final String deviceName = DeviceNameDatabaseManager.getInstance().readDeviceName(deviceAddress);

        return new DeviceModel(deviceAddress, color, deviceName, false);
    }

    private void notifyGadgetConnectionChanged(@NonNull Gadget gadget, boolean isConnected) {
        notifyGadgetConnectionChanged(gadget.getAddress(), gadget.getName(), isConnected);
    }

    private void notifyGadgetConnectionChanged(@NonNull final String address,
                                               @Nullable final String name,
                                               final boolean isConnected) {
        synchronized (mSensorListeners) {
            for (HumiSensorListener listener : mSensorListeners) {
                listener.onGadgetConnectionChanged(createDeviceModel(address), isConnected);
            }
        }

        if (mConnectionStateListener != null) {
            mConnectionStateListener.onConnectionStateChanged(new GadgetModel(address, isConnected, name), isConnected);
        }
    }

    private void aggregateRHTAndNotify(@NonNull final String deviceAddress,
                                       @NonNull final GadgetValue[] values,
                                       AggregatorType aggregatorType) {
        for (GadgetValue value : values) {
            RHTDataPoint rhtDataPoint;
            if (isTemperatureValue(value)) {
                rhtDataPoint = mAggregators.get(aggregatorType).aggregateTemperatureValue(deviceAddress, value);
            } else if (isHumidityValue(value)) {
                rhtDataPoint = mAggregators.get(aggregatorType).aggregateHumidityValue(deviceAddress, value);
            } else {
                Log.w(TAG, "Can not aggregate RHT data for value that isn't RH or T");
                continue;
            }

            if (rhtDataPoint != null) {
                notifyRHTDataPoint(deviceAddress, rhtDataPoint, aggregatorType == AggregatorType.HISTORY);
            }
        }
    }

    private void notifyRHTDataPoint(@NonNull String deviceAddress, @NonNull RHTDataPoint dataPoint,
                                    final boolean isHistory) {
        if (!isHistory) {
            synchronized (mSensorListeners) {
                for (final HumiSensorListener listener : mSensorListeners) {
                    listener.onNewRHTData(dataPoint.getTemperatureCelsius(), dataPoint.getRelativeHumidity(), deviceAddress);
                }
            }
        }
        HistoryDatabaseManager.getInstance().addRHTData(deviceAddress, dataPoint, isHistory);
    }

    private boolean isHumidityValue(@NonNull final GadgetValue value) {
        return value.getUnit().equals(SHTC1TemperatureAndHumidityService.UNIT_RH) ||
                value.getUnit().equals(SHT3xHumidityService.UNIT) ||
                value.getUnit().equals(SensorTagTemperatureAndHumidityService.UNIT_RH);
    }

    private boolean isTemperatureValue(@NonNull final GadgetValue value) {
        return value.getUnit().equals(SHTC1TemperatureAndHumidityService.UNIT_T) ||
                value.getUnit().equals(SHT3xTemperatureService.UNIT) ||
                value.getUnit().equals(SensorTagTemperatureAndHumidityService.UNIT_T);
    }

    public boolean startDiscovery(final int durationMs) {
        mDiscoveredGadgets.clear();
        return mGadgetManager.startGadgetDiscovery(durationMs, mHumiGadgetNameFilter,
                mHumiGadgetServiceUUIDFilter);
    }

    public void stopDiscovery() {
        mGadgetManager.stopGadgetDiscovery();
    }

}