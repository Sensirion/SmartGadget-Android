package com.sensirion.smartgadget.peripheral.rht_sensor;

import com.sensirion.smartgadget.utils.DeviceModel;

public interface HumiSensorListener {
    void onNewRHTData(float temperature, float humidity, String deviceAddress);

    void onGadgetConnectionChanged(DeviceModel model, boolean isConnected);
}