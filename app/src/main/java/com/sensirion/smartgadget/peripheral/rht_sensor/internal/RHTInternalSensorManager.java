package com.sensirion.smartgadget.peripheral.rht_sensor.internal;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.sensirion.libble.utils.RHTDataPoint;
import com.sensirion.smartgadget.R;
import com.sensirion.smartgadget.peripheral.rht_sensor.RHTSensorManager;
import com.sensirion.smartgadget.persistence.device_name_database.DeviceNameDatabaseManager;
import com.sensirion.smartgadget.persistence.history_database.HistoryDatabaseManager;
import com.sensirion.smartgadget.utils.DeviceModel;
import com.sensirion.smartgadget.utils.view.ColorManager;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class RHTInternalSensorManager {

    private static final String TAG = RHTInternalSensorManager.class.getSimpleName();
    private static final String PREFIX = RHTInternalSensorManager.class.getName();

    public static final String INTERNAL_SENSOR_ADDRESS = String.format("%s/InternalRHTSensor", PREFIX);

    private static RHTInternalSensorManager mInstance;
    private final String mInternalRHTSensorName;
    @NonNull
    private final SensorManager mSensorManager;
    private final Set<RHTSensorManager> mRHTInternalSensorListeners = Collections.synchronizedSet(new HashSet<RHTSensorManager>());
    @Nullable
    private final SensorEventListener mInternalSensorEventListener = new SensorEventListener() {

        @Nullable
        private Float mLastTemperature = null;
        @Nullable
        private Float mLastHumidity = null;

        @Override
        public void onSensorChanged(@NonNull final SensorEvent event) {
            synchronized (this) {
                switch (event.sensor.getType()) {
                    case Sensor.TYPE_AMBIENT_TEMPERATURE:
                        if (event.values[0] == 0f) {
                            Log.e(TAG, "onSensorChanged -> The sensor returned a corrupt temperature.");
                            return;
                        }
                        mLastTemperature = event.values[0];
                        if (mLastHumidity != null) {
                            notifyListeners();
                        }
                        break;
                    case Sensor.TYPE_RELATIVE_HUMIDITY:
                        if (event.values[0] == 0f) {
                            Log.e(TAG, "onSensorChanged -> The sensor returned a corrupt humidity.");
                            return;
                        }
                        mLastHumidity = event.values[0];
                        if (mLastTemperature != null) {
                            notifyListeners();
                        }
                        break;
                }
            }
        }

        @Override
        public void onAccuracyChanged(@NonNull final Sensor sensor, final int accuracy) {
            Log.w(TAG, String.format("onAccuracyChanged -> The accuracy of sensor %s has change to %d.", sensor.getName(), accuracy));
        }

        private void notifyListeners() {
            for (final RHTSensorManager listener : mRHTInternalSensorListeners) {
                listener.onNewRHTData(mLastTemperature, mLastHumidity, INTERNAL_SENSOR_ADDRESS);
            }
            final RHTDataPoint dataPoint = new RHTDataPoint(mLastTemperature, mLastHumidity, System.currentTimeMillis());
            HistoryDatabaseManager.getInstance().addRHTData(INTERNAL_SENSOR_ADDRESS, dataPoint, false);
            mLastTemperature = null;
            mLastHumidity = null;
        }
    };
    @Nullable
    private DeviceModel mSensorModel = null;
    private boolean mIsStarted;

    private RHTInternalSensorManager(@NonNull final Context context) {
        mSensorManager = (SensorManager) context.getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        mInternalRHTSensorName = context.getString(R.string.inphone_rht_sensor);
    }

    @NonNull
    public static RHTInternalSensorManager getInstance() {
        if (mInstance == null) {
            throw new IllegalStateException(String.format("%s: getInstance -> %s has not been initialized yet.", TAG, TAG));
        }
        return mInstance;
    }

    public synchronized static void init(@NonNull final Context context) {
        if (mInstance == null) {
            mInstance = new RHTInternalSensorManager(context);
        } else {
            throw new IllegalStateException(String.format("%s: init -> %s have been already initialized.", TAG, TAG));
        }
    }

    /**
     * Registers a listener in the internal sensor listener list.
     *
     * @param listener that wants to be informed on the internal sensor news.
     */
    public void registerInternalSensorListener(@NonNull final RHTSensorManager listener) {
        if (mRHTInternalSensorListeners.contains(listener)) {
            Log.w(TAG, String.format("registerInternalSensorListener -> Listener %s it's already in the listener list", listener));
        }
        if (hasInternalSensor()) {
            mRHTInternalSensorListeners.add(listener);
            startInternalSensor();
        }
    }

    /**
     * Unregisters a listener from the internal sensor listener list.
     *
     * @param listener that wants to be remove.
     */
    @SuppressWarnings("unused")
    public void unregisterInternalSensorListener(@NonNull final RHTSensorManager listener) {
        mRHTInternalSensorListeners.remove(listener);
        if (mRHTInternalSensorListeners.isEmpty()) {
            stopInternalSensor();
        }
    }

    /**
     * Removes all the listeners from the internal listener list.
     */
    @SuppressWarnings("unused")
    public void unregisterAllInternalSensorListeners() {
        mRHTInternalSensorListeners.clear();
        stopInternalSensor();
    }

    private void startInternalSensor() {
        if (mIsStarted) {
            Log.w(TAG, "startInternalSensor -> The internal sensor was already initialized by the user.");
            return;
        }

        if (hasInternalSensor()) {
            if (initializeTemperatureSensor()) {
                Log.d(TAG, "startInternalSensor -> Temperature Sensor has been initialized.");
                if (initializeHumiditySensor()) {
                    Log.d(TAG, "startInternalSensor -> Humidity Sensor has been initialized.");
                    mIsStarted = true;
                    notifyAllListenersNewSensor();
                } else {
                    stopInternalSensor();
                }
            }
        }
        Log.w(TAG, "startInternalSensor -> The device doesn't have a valid internal Sensor.");
    }

    private void notifyListenerNewSensor(@NonNull final RHTSensorManager listener) {
        listener.onConnectedRHTDevice(getSensorModel());
    }

    private void notifyAllListenersNewSensor() {
        for (final RHTSensorManager listener : mRHTInternalSensorListeners) {
            notifyListenerNewSensor(listener);
        }
    }

    @NonNull
    private DeviceModel getSensorModel() {
        if (mSensorModel == null) {
            if (!DeviceNameDatabaseManager.getInstance().readDeviceName(INTERNAL_SENSOR_ADDRESS).equals(getInternalRHTSensorName())) {
                DeviceNameDatabaseManager.getInstance().updateDeviceName(INTERNAL_SENSOR_ADDRESS, getInternalRHTSensorName());
            }
            final int deviceColor = ColorManager.getInstance().getDeviceColor(INTERNAL_SENSOR_ADDRESS);
            mSensorModel = new DeviceModel(INTERNAL_SENSOR_ADDRESS, deviceColor, getInternalRHTSensorName(), true);
        }
        return mSensorModel;
    }

    /**
     * Checks if the device has a RHT internal sensor.
     *
     * @return <code>true</code> if the device has a valid internal sensor for the application. <code>false</code> otherwise.
     */
    public boolean hasInternalSensor() {
        return hasTemperatureSensor() && hasRelativeHumiditySensor();
    }

    /**
     * Checks if the device has a humidity sensor.
     *
     * @return <code>true</code> if the device has a temperature internal sensor. <code>false</code> otherwise.
     */
    private boolean hasTemperatureSensor() {
        return mSensorManager.getSensorList(Sensor.TYPE_AMBIENT_TEMPERATURE).size() > 0;
    }

    /**
     * Initializes the internal temperature sensor and adding a listener to it.
     *
     * @return <code>true</code> if the temperature sensor was initialized correctly. <code>false</code> otherwise.
     */
    private boolean initializeTemperatureSensor() {
        return mSensorManager.registerListener(mInternalSensorEventListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE),
                android.hardware.SensorManager.SENSOR_DELAY_NORMAL);
    }

    /**
     * Initializes the internal humidity sensor and adding a listener to it.
     *
     * @return <code>true</code> if the temperature sensor was initialized correctly. <code>false</code> otherwise.
     */
    private boolean initializeHumiditySensor() {
        return mSensorManager.registerListener(mInternalSensorEventListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY),
                android.hardware.SensorManager.SENSOR_DELAY_NORMAL);
    }

    /**
     * Checks if the device has a humidity sensor.
     *
     * @return <code>true</code> if the device has a relative humidity internal sensor. <code>false</code> otherwise.
     */
    private boolean hasRelativeHumiditySensor() {
        return mSensorManager.getSensorList(Sensor.TYPE_RELATIVE_HUMIDITY).size() > 0;
    }

    /**
     * Stops the internal Sensor.
     */
    private void stopInternalSensor() {
        mSensorManager.unregisterListener(mInternalSensorEventListener);
        mIsStarted = false;
    }

    /**
     * Obtains the default sensor name for the internal sensor.
     *
     * @return {@link java.lang.String} with the sensor name.
     */
    @NonNull
    public String getInternalRHTSensorName() {
        return mInternalRHTSensorName;
    }
}