package com.sensirion.smartgadget.peripheral.rht_sensor.external;

import android.support.annotation.NonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Aggregates live values, i.e. ignores time stamps for aggregation.
 */
public class LiveValueAggregator extends RHTValueAggregator<String> {

    protected String getAggregationKey(@NonNull String deviceAddress, long timestamp) {
        return deviceAddress;
    }
}
