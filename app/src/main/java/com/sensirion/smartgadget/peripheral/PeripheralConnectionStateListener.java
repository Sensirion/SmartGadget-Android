package com.sensirion.smartgadget.peripheral;

import android.support.annotation.NonNull;

public interface PeripheralConnectionStateListener {

    /**
     * Informs the listeners of the connection state change of a device.
     *
     * @param deviceAddress     of the device.
     * @param deviceIsConnected <code>true</code> if the device is connected - <code>false</code> otherwise.
     */
    void onGadgetConnectionChanged(@NonNull String deviceAddress, boolean deviceIsConnected);
}
