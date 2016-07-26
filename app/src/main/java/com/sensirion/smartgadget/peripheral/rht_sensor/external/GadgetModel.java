package com.sensirion.smartgadget.peripheral.rht_sensor.external;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class GadgetModel {
    public static final String UNKNOWN_DEVICE_NAME = "UNKNOWN";
    @NonNull
    private final String mDeviceAddress;
    private String mUserDeviceName;
    private int rssi;

    private boolean mConnected;

    public GadgetModel(@NonNull final String deviceAddress, final boolean connected,
                       @Nullable final String userDeviceName) {
        mDeviceAddress = deviceAddress;
        mConnected = connected;
        mUserDeviceName = (userDeviceName != null) ? userDeviceName : UNKNOWN_DEVICE_NAME;
    }

    @NonNull
    public String getAddress() {
        return mDeviceAddress;
    }

    public boolean isConnected() {
        return mConnected;
    }

    public void setConnected(boolean connected) {
        mConnected = connected;
    }

    @NonNull
    public String getName() {
        return mUserDeviceName;
    }

    public void setUserDeviceName(@NonNull String userDeviceName) {
        mUserDeviceName = userDeviceName;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GadgetModel)) return false;

        GadgetModel that = (GadgetModel) o;

        return mDeviceAddress.equals(that.mDeviceAddress);

    }

    @Override
    public int hashCode() {
        return mDeviceAddress.hashCode();
    }
}
