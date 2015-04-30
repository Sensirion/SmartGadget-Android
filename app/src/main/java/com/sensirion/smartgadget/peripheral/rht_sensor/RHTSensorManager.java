package com.sensirion.smartgadget.peripheral.rht_sensor;

import android.support.annotation.NonNull;

import com.sensirion.smartgadget.peripheral.PeripheralConnectionStateListener;
import com.sensirion.smartgadget.utils.DeviceModel;

import java.util.List;

public interface RHTSensorManager extends PeripheralConnectionStateListener {
    void onNewRHTData(float temperature, float humidity, String deviceAddress);

    void onConnectedRHTDevice(DeviceModel model);

    @NonNull
    List<DeviceModel> getConnectedSensors();
}