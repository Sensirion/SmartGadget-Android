package com.sensirion.smartgadget.peripheral.rht_sensor.external;

import android.support.annotation.NonNull;

import java.util.AbstractMap;

/**
 * Aggregates Logged values according to their time stamps.
 */
public class HistoryValueAggregator extends RHTValueAggregator<HistoryValueAggregator.HistoryAggregationKey> {

    protected HistoryAggregationKey getAggregationKey(@NonNull String deviceAddress, long timestamp) {
        return new HistoryAggregationKey(deviceAddress, timestamp);
    }

    protected static class HistoryAggregationKey extends AbstractMap.SimpleImmutableEntry<String, Long> {
        public HistoryAggregationKey(String deviceAddress, long timestamp) {
            super(deviceAddress, timestamp);
        }
    }
}
