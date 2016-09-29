package com.sensirion.smartgadget.peripheral.rht_sensor.external;

import android.support.annotation.NonNull;

import com.sensirion.libsmartgadget.GadgetValue;
import com.sensirion.smartgadget.peripheral.rht_utils.RHTDataPoint;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to aggregates individual RH or T values until the other reading arrives and the
 * RHTDataPoint is complete. The key according to which the aggregation occurs is configurable
 * and can contain the device address and the value timestamp.
 */
abstract class RHTValueAggregator<AggregationKey> {
    private final Map<AggregationKey, RHTValue> mAggregatedValues = Collections.synchronizedMap(new HashMap<AggregationKey, RHTValue>());

    protected abstract AggregationKey getAggregationKey(String deviceAddress, long timestamp);

    private RHTDataPoint aggregateValues(@NonNull String deviceAddress, GadgetValue temperature, GadgetValue humidity) {
        if (! (temperature == null ^ humidity == null)) {
            throw new IllegalArgumentException("can only aggregate either temperature or humidity");
        }

        final GadgetValue value = temperature != null ? temperature : humidity;
        final long timestamp = value.getTimestamp().getTime();

        final AggregationKey aggregationKey = getAggregationKey(deviceAddress, timestamp);
        RHTValue rhtValue = mAggregatedValues.get(aggregationKey);
        if (rhtValue == null) {
            rhtValue = new RHTValue(null, null);
            mAggregatedValues.put(aggregationKey, rhtValue);
        }
        if (temperature != null) {
            rhtValue.setTemperature(value.getValue().floatValue());
        } else {
            rhtValue.setHumidity(value.getValue().floatValue());
        }

        if (rhtValue.isComplete()) {
            mAggregatedValues.remove(aggregationKey);
            return new RHTDataPoint(rhtValue.getTemperature(), rhtValue.getHumidity(), timestamp);
        }
        return null;
    }

    public RHTDataPoint aggregateHumidityValue(@NonNull String deviceAddress,
                                        @NonNull GadgetValue value) {
        return aggregateValues(deviceAddress, null, value);
    }

    public RHTDataPoint aggregateTemperatureValue(@NonNull String deviceAddress,
                                           @NonNull GadgetValue value) {
        return aggregateValues(deviceAddress, value, null);
    }

    protected static class RHTValue {
        private Float mTemperature;
        private Float mHumidity;

        public RHTValue(Float temperature, Float humidity) {
            mTemperature = temperature;
            mHumidity = humidity;
        }

        public boolean isComplete() {
            return mTemperature != null && mHumidity != null;
        }

        public Float getTemperature() {
            return mTemperature;
        }

        public void setTemperature(Float temperature) {
            mTemperature = temperature;
        }

        public Float getHumidity() {
            return mHumidity;
        }

        public void setHumidity(Float humidity) {
            mHumidity = humidity;
        }
    }
}
