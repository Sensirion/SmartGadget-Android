package com.sensirion.smartgadget.peripheral.rht_sensor;

import android.support.annotation.Nullable;

import com.sensirion.smartgadget.peripheral.PeripheralConnectionStateListener;

public interface RHTSensorListener extends PeripheralConnectionStateListener {

    /**
     * Informs the listeners of the new sensor data.
     *
     * @param temperature      of the sample.
     * @param relativeHumidity of the sample.
     * @param deviceAddress    of the device - <code>null</code> in case the sensor is an internal sensor
     */
    void onNewRHTSensorData(float temperature, float relativeHumidity, @Nullable String deviceAddress);
}