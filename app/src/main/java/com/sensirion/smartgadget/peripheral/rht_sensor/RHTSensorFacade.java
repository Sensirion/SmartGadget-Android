package com.sensirion.smartgadget.peripheral.rht_sensor;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.sensirion.libble.utils.RHTDataPoint;
import com.sensirion.smartgadget.peripheral.rht_sensor.external.RHTHumigadgetSensorManager;
import com.sensirion.smartgadget.peripheral.rht_sensor.internal.RHTInternalSensorManager;
import com.sensirion.smartgadget.utils.DeviceModel;
import com.sensirion.smartgadget.utils.Settings;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class RHTSensorFacade implements RHTSensorManager {

    private static final String TAG = RHTSensorFacade.class.getSimpleName();

    @Nullable
    private static RHTSensorFacade mInstance;
    private final List<DeviceModel> mConnectedDeviceListModels = Collections.synchronizedList(new LinkedList<DeviceModel>());
    private final Set<RHTSensorListener> mListeners = Collections.synchronizedSet(new HashSet<RHTSensorListener>());

    @NonNull
    private final Map<String, RHTDataPoint> mLastDataPoint = Collections.synchronizedMap(new HashMap<String, RHTDataPoint>());

    private RHTSensorFacade() {
    }

    @NonNull
    public synchronized static RHTSensorFacade getInstance() {
        if (mInstance == null) {
            throw new IllegalStateException(String.format("%s: getInstance -> The manager has already been initialized.", TAG));
        }
        return mInstance;
    }

    /**
     * This init method haves to be called at the beginning of the code execution.
     */
    public synchronized static void init(@NonNull final Context context) {
        if (mInstance == null) {
            mInstance = new RHTSensorFacade();
            RHTHumigadgetSensorManager.init(context);
            RHTInternalSensorManager.init(context);
            RHTHumigadgetSensorManager.getInstance().registerHumigadgetListener(mInstance);
            RHTInternalSensorManager.getInstance().registerInternalSensorListener(mInstance);
        }
    }

    /**
     * This method has to be called at the end of the code execution.
     *
     * @param context cannot be <code>null</code>
     */
    public void release(@NonNull final Context context) {
        RHTHumigadgetSensorManager.getInstance().release(context);
        RHTInternalSensorManager.getInstance().unregisterAllInternalSensorListeners();
        mConnectedDeviceListModels.clear();
        mInstance = null;
    }

    /**
     * Checks if they are connected devices.
     *
     * @return <code>true</code> if they are connected devices - <code>false</code> otherwise.
     */
    public boolean hasConnectedDevices() {
        return !mConnectedDeviceListModels.isEmpty();
    }

    /**
     * Obtains the list of the connected devices.
     *
     * @return {@link java.util.List} with  {@link com.sensirion.smartgadget.utils.DeviceModel} with the connected devices.
     */
    @NonNull
    public List<DeviceModel> getConnectedSensors() {
        return new LinkedList<>(mConnectedDeviceListModels);
    }

    /**
     * Checks if a device is connected.
     *
     * @param deviceAddress of the device we want to check.
     * @return <code>true</code> if connected - <code>false</code> if it's disconnected.
     */
    public boolean isDeviceConnected(@NonNull final String deviceAddress) {
        synchronized (mConnectedDeviceListModels) {
            for (final DeviceModel model : mConnectedDeviceListModels) {
                if (model.getAddress().equals(deviceAddress)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Gets a specific device model.
     *
     * @param deviceAddress of the device we want to retrieve.
     * @return {@link com.sensirion.smartgadget.utils.DeviceModel} of the requested device - <code>null</code> if it was not found.
     */
    @Nullable
    public DeviceModel getDeviceModel(@NonNull final String deviceAddress) {
        synchronized (mConnectedDeviceListModels) {
            for (final DeviceModel model : mConnectedDeviceListModels) {
                if (model.getAddress().equals(deviceAddress)) {
                    return model;
                }
            }
        }
        return null;
    }

    /**
     * Registers a listener to connected sensor notifications.
     *
     * @param listener for all the sensors.
     */
    public void registerListener(@NonNull final RHTSensorListener listener) {
        if (mListeners.contains(listener)) {
            Log.w(TAG, String.format("registerNotificationListener -> already contains listener: %s.", listener));
        }
        mListeners.add(listener);
        RHTInternalSensorManager.getInstance().registerInternalSensorListener(this);
        RHTHumigadgetSensorManager.getInstance().registerHumigadgetListener(this);
        notifyCachedSensorData(listener);
    }

    /**
     * Unregisters a listener to connected sensor notifications.
     *
     * @param listener for all the sensors.
     */
    public void unregisterListener(@NonNull final RHTSensorListener listener) {
        if (mListeners.contains(listener)) {
            mListeners.remove(listener);
        } else {
            Log.w(TAG, String.format("unregisterNotificationListener -> Has not found: %s", listener));
        }
    }

    private void selectFallback(@NonNull final String deviceAddress) {
        if (deviceAddress.equals(Settings.getInstance().getSelectedAddress())) {
            try {
                final String nextAddress = getAddressLastConnectedGadget();
                Settings.getInstance().setSelectedAddress(nextAddress);
            } catch (NoSuchElementException e) {
                Log.w(TAG, "removeDevice() -> selectFallback(): no more devices connected");
                Settings.getInstance().unselectCurrentAddress();
            }
        }
    }

    /**
     * Gets the address of the last connected gadget.
     *
     * @return {@link java.lang.String} with the address of the next connected gadget. <code>null</code> if no gadgets are available.
     */
    @Nullable
    public String getAddressLastConnectedGadget() {
        synchronized (mConnectedDeviceListModels) {
            if (mConnectedDeviceListModels.isEmpty()) {
                return null;
            }
            return mConnectedDeviceListModels.get(mConnectedDeviceListModels.size() - 1).getAddress();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onNewRHTData(final float temperature,
                             final float humidity,
                             @Nullable final String deviceAddress) {
        this.mLastDataPoint.put(deviceAddress,
                new RHTDataPoint(temperature, humidity, System.currentTimeMillis())
        );
        notifySensorData(temperature, humidity, deviceAddress);
    }

    @Override
    public void onGadgetConnectionChanged(@NonNull final DeviceModel model, final boolean isConnected) {
        final String deviceAddress = model.getAddress();
        Log.d(TAG, String.format("onGadgetConnectionChanged -> Device %s has been %s.", deviceAddress, (isConnected) ? "connected" : "disconnected"));

        if (isConnected) {
            if (isDeviceConnected(deviceAddress)) {
                Log.w(TAG, String.format("onGadgetConnectionChanged -> Device with address %s was already in the connected device list.", deviceAddress));
                return;
            }
            mConnectedDeviceListModels.add(model);
            Settings.getInstance().setSelectedAddress(model.getAddress());
        } else {
            mConnectedDeviceListModels.remove(model);
            selectFallback(deviceAddress);
        }

        synchronized (mListeners) {
            for (RHTSensorListener listener : mListeners) {
                listener.onGadgetConnectionChanged(deviceAddress, isConnected);
            }
        }
    }

    /**
     * Notifies to the listeners the new sensor data.
     *
     * @param temperature      of the sample.
     * @param relativeHumidity of the sample.
     * @param deviceAddress    of the device.
     */
    public void notifySensorData(final float temperature,
                                 final float relativeHumidity, @Nullable final String deviceAddress) {
        synchronized (mListeners) {
            for (final RHTSensorListener listener : mListeners) {
                listener.onNewRHTSensorData(temperature, relativeHumidity, deviceAddress);
            }
        }
    }

    /**
     * Notifies a new listener of the last value for each device
     *
     * @param listener that will be notified
     */
    public void notifyCachedSensorData(@NonNull final RHTSensorListener listener) {
        synchronized (mLastDataPoint) {
            for (final String humigadgetsWithDatapoints : mLastDataPoint.keySet()) {
                final RHTDataPoint dataPoint = mLastDataPoint.get(humigadgetsWithDatapoints);
                listener.onNewRHTSensorData(
                        dataPoint.getTemperatureCelsius(),
                        dataPoint.getRelativeHumidity(),
                        humigadgetsWithDatapoints
                );
            }
        }
    }
}
