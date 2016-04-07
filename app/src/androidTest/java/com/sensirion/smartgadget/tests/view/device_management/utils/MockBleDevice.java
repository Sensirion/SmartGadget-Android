package com.sensirion.smartgadget.tests.view.device_management.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.sensirion.libble.devices.BleDevice;
import com.sensirion.libble.devices.DeviceBluetoothType;
import com.sensirion.libble.listeners.NotificationListener;
import com.sensirion.libble.services.AbstractBleService;
import com.sensirion.libble.services.AbstractHistoryService;
import com.sensirion.libble.services.BleService;

import java.util.LinkedList;

class MockBleDevice implements BleDevice {

    private final int rssi;

    MockBleDevice(final int rssi) {
        this.rssi = rssi;
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public void connect(@NonNull final Context context) {
        //DO NOTHING
    }

    @Override
    public boolean reconnect() {
        return false;
    }

    @Override
    public void disconnect() {
        //DO NOTHING
    }

    @NonNull
    @Override
    public String getAddress() {
        return "AA:BB:CC:DD:EE:FF";
    }

    @Nullable
    @Override
    public String getAdvertisedName() {
        return null;
    }

    @NonNull
    @Override
    public DeviceBluetoothType getBluetoothType() {
        return DeviceBluetoothType.DEVICE_TYPE_UNKNOWN;
    }

    @Override
    public int getRSSI() {
        return this.rssi;
    }

    @Nullable
    @Override
    public <T extends AbstractBleService> T getDeviceService(@NonNull Class<T> aClass) {
        return null;
    }

    @Nullable
    @Override
    public BleService getDeviceService(@NonNull String s) {
        return null;
    }

    @NonNull
    @Override
    public Iterable<BleService> getDiscoveredServices() {
        return new LinkedList<>();
    }

    @NonNull
    @Override
    public Iterable<String> getDiscoveredServicesNames() {
        return new LinkedList<>();
    }

    @Override
    public int getNumberServices() {
        return 0;
    }

    @Override
    public void setAllNotificationsEnabled(boolean b) {
        //Do Nothing
    }

    @Override
    public boolean registerDeviceListener(@NonNull NotificationListener notificationListener) {
        return false;
    }

    @Override
    public void unregisterDeviceListener(@NonNull NotificationListener notificationListener) {
        //Do Nothing
    }

    @Nullable
    @Override
    public AbstractHistoryService getHistoryService() {
        return null;
    }

    @Override
    public boolean areAllServicesReady() {
        return false;
    }
}
