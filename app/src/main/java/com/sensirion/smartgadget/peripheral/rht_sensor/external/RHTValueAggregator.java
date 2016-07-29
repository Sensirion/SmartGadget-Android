package com.sensirion.smartgadget.peripheral.rht_sensor.external;

import android.support.annotation.NonNull;

import com.sensirion.libsmartgadget.GadgetValue;
import com.sensirion.smartgadget.peripheral.rht_utils.RHTDataPoint;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to aggregates individual RH or T values until the other reading arrives and the
 * RHTDataPoint is complete.
 */
class RHTValueAggregator {
    private final Map<String, RHTValue> mLiveDataPointAggregator = Collections.synchronizedMap(new HashMap<String, RHTValue>());
    private final Map<String, RHTValue> mHistoryDataPointAggregator = Collections.synchronizedMap(new HashMap<String, RHTValue>());

    RHTDataPoint aggregateHumidityValue(@NonNull AggregatorType aggregatorType,
                                        @NonNull String deviceAddress,
                                        @NonNull GadgetValue value) {
        final Map<String, RHTValue> aggregator = getAggregatorMapForType(aggregatorType);
        final float humidity = value.getValue().floatValue();
        final long timestamp = value.getTimestamp().getTime();
        final RHTValue rhtValue = aggregator.get(deviceAddress);

        if (rhtValue == null) {
            aggregator.put(deviceAddress, new RHTValue(null, humidity));
        } else {
            rhtValue.setHumidity(humidity);
            if (rhtValue.getTemperature() != null) {
                aggregator.remove(deviceAddress);
                return new RHTDataPoint(rhtValue.getTemperature(), rhtValue.getHumidity(), timestamp);
            }
        }
        return null;
    }

    RHTDataPoint aggregateTemperatureValue(@NonNull AggregatorType aggregatorType,
                                           @NonNull String deviceAddress,
                                           @NonNull GadgetValue value) {
        final Map<String, RHTValue> aggregator = getAggregatorMapForType(aggregatorType);
        final float temperature = value.getValue().floatValue();
        final long timestamp = value.getTimestamp().getTime();
        final RHTValue rhtValue = aggregator.get(deviceAddress);

        if (rhtValue == null) {
            aggregator.put(deviceAddress, new RHTValue(temperature, null));
        } else {
            rhtValue.setTemperature(temperature);
            if (rhtValue.getHumidity() != null) {
                aggregator.remove(deviceAddress);
                return new RHTDataPoint(rhtValue.getTemperature(), rhtValue.getHumidity(), timestamp);
            }
        }
        return null;
    }

    private Map<String, RHTValue> getAggregatorMapForType(AggregatorType aggregatorType) {
        switch (aggregatorType) {
            case LIVE:
                return mLiveDataPointAggregator;
            case HISTORY:
                return mHistoryDataPointAggregator;
        }
        return null;
    }

    public enum AggregatorType {
        LIVE,
        HISTORY
    }

    public class RHTValue {
        private Float mTemperature;
        private Float mHumidity;

        public RHTValue(Float temperature, Float humidity) {
            mTemperature = temperature;
            mHumidity = humidity;
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
