package com.sensirion.smartgadget.view.history;

import android.support.annotation.NonNull;

import com.sensirion.libble.utils.RHTDataPoint;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class HistoryResult {

    @NonNull
    private final Map<String, List<RHTDataPoint>> mResultValues = Collections.synchronizedMap(new HashMap<String, List<RHTDataPoint>>());

    public HistoryResult(@NonNull final List<String> devices) {
        for (final String device : devices) {
            mResultValues.put(device, Collections.synchronizedList(new LinkedList<RHTDataPoint>()));
        }
    }

    /**
     * Obtains the history results.
     *
     * @return Iterable of {@link java.util.List <@link RHTDataPoint>}>} with the results
     */
    @NonNull
    public Map<String, List<RHTDataPoint>> getResults() {
        return mResultValues;
    }

    /**
     * Adds a datapoint to the result list.
     *
     * @param dataPoint that is going to be added.
     */
    public void addResult(@NonNull final String deviceAddress, @NonNull final RHTDataPoint dataPoint) {
        mResultValues.get(deviceAddress).add(dataPoint);
    }

    /**
     * Obtains the number of values retrieved by the database.
     *
     * @return <code>int</code> with the number of values.
     */
    public int size() {
        int size = 0;
        for (List<RHTDataPoint> results : mResultValues.values()) {
            size += results.size();
        }
        return size;
    }

    @NonNull
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (final String deviceAddress : mResultValues.keySet()) {
            for (final RHTDataPoint datapoint : mResultValues.get(deviceAddress)) {
                sb.append(String.format("\nDevice with address: %s - Temperature: %f - Humidity: %f, Seconds ago: %d",
                        deviceAddress, datapoint.getTemperatureCelsius(), datapoint.getRelativeHumidity(), (System.currentTimeMillis() - datapoint.getTimestamp() / 1000)));
            }
        }
        return sb.toString();
    }
}