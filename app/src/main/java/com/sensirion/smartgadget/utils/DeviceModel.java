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