package com.sensirion.smartgadget.peripheral.rht_sensor.external;

import android.support.annotation.NonNull;

import com.sensirion.libsmartgadget.Gadget;

public interface HumiGadgetConnectionStateListener {
    /**
     * Callback to report found gadgets.
     *
     * @param gadget The discovered {@link Gadget} instance.
     */
    void onGadgetDiscovered(@NonNull GadgetModel gadget);

    /**
     * Callback when gadget discovery could not be started.
     */
    void onGadgetDiscoveryFailed();

    /**
     * Called when the discovery has stopped.
     */
    void onGadgetDiscoveryFinished();

    void onConnectionStateChanged(@NonNull GadgetModel gadgetModel, boolean isConnected);
}
