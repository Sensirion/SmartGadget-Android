/*
 * Copyright (c) 2017, Sensirion AG
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of Sensirion AG nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
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
