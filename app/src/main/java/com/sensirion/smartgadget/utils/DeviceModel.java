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
package com.sensirion.smartgadget.utils;

import android.support.annotation.NonNull;

public class DeviceModel {

    @NonNull
    private final String mDeviceAddress;
    private final int mColor;
    private final boolean mIsInternal;
    @NonNull
    private String mUserDeviceName;

    public DeviceModel(@NonNull final String deviceAddress, final int color, @NonNull final String userDeviceName, final boolean isInternal) {
        mDeviceAddress = deviceAddress;
        mColor = color;
        mUserDeviceName = userDeviceName;
        mIsInternal = isInternal;
    }

    /**
     * Obtains the device address of the device.
     *
     * @return {@link java.lang.String} with the device address.
     */
    @NonNull
    public String getAddress() {
        return mDeviceAddress;
    }

    /**
     * Obtains the color of the device.
     *
     * @return {@link java.lang.Integer} with the color of the device.
     */
    public int getColor() {
        return mColor;
    }

    /**
     * Obtains the user name of the device.
     *
     * @return {@link java.lang.String} with the device name.
     */
    @NonNull
    public String getUserDeviceName() {
        return mUserDeviceName;
    }

    /**
     * Returns if the device it's an internal device.
     *
     * @return <code>true</code> if the device is internal - <code>false</code> if it's external.
     */
    public boolean isInternal() {
        return mIsInternal;
    }

    /**
     * Modifies the device user name.
     *
     * @param displayName that the user has choose for the device.
     */
    public void setDisplayName(@NonNull final String displayName) {
        mUserDeviceName = displayName;
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof DeviceModel) {
            return ((DeviceModel) o).getAddress().equals(mDeviceAddress);
        } else if (o instanceof String) {
            return o.toString().equals(mDeviceAddress);
        }
        return false;
    }

    @NonNull
    @Override
    @SuppressWarnings("StringBufferReplaceableByString")
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Device address = ").append(mDeviceAddress).append(" ");
        sb.append("color = ").append(mColor).append(" ");
        sb.append("name = ").append(mUserDeviceName).append(" ");
        sb.append("Is internal = ").append(mIsInternal);
        return sb.toString();
    }
}
