package com.sensirion.smartgadget.peripheral.rht_sensor.external;

import android.support.annotation.NonNull;

import com.sensirion.libsmartgadget.GadgetValue;

import java.util.Date;

class GadgetTestValue implements GadgetValue {

    private Number mValue;
    private Date mTimestamp;

    GadgetTestValue(Number value, long timestamp) {
        this.mValue = value;
        this.mTimestamp = new Date(timestamp);
    }

    @NonNull
    @Override
    public Number getValue() {
        return mValue;
    }

    @NonNull
    @Override
    public Date getTimestamp() {
        return mTimestamp;
    }

    @NonNull
    @Override
    public String getUnit() {
        return "";
    }
}
