package com.sensirion.smartgadget.utils;

import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import com.sensirion.libble.utils.RHTDataPoint;

/**
 * Checks if the humidity and temperature outputted by a humidity and
 * temperature sensor is feasible.
 */
@WorkerThread
public final class RhtCorruptnessChecker {

    /**
     * Minimum accepted temperature.
     */
    public static final float TEMPERATURE_LOWER_BOUND = -25f;
    /**
     * Maximum accepted temperature.
     */
    public static final float TEMPERATURE_UPPER_BOUND = 120f;

    /**
     * Minimum accepted relative humidity.
     */
    public static final float HUMIDITY_LOWER_BOUND = 0f;
    /**
     * Maximum accepted relative humidity.
     */
    public static final float HUMIDITY_UPPER_BOUND = 100f;

    /**
     * Use the public static method:
     * {@link RhtCorruptnessChecker#isDatapointCorrupted(RHTDataPoint)}
     */
    private RhtCorruptnessChecker() {
    }

    /**
     * Checks if the incoming dataPoint values are feasible.
     *
     * @param dataPoint that will be analyzed
     * @return <code>true</code> if the incoming values are feasible - <code>false</code> otherwise.
     */
    public static boolean isDatapointCorrupted(@NonNull final RHTDataPoint dataPoint) {
        return isDataPointTemperatureFeasible(dataPoint) && isDataPointHumidityFeasible(dataPoint);
    }

    /**
     * Checks if the incoming dataPoint temperature value is feasible.
     *
     * @param dataPoint that will be analyzed.
     * @return <code>true</code> if the temperature is between (included)
     * {@link #TEMPERATURE_LOWER_BOUND} and {@link #TEMPERATURE_UPPER_BOUND}
     * return <code>false</code> otherwise.
     */
    private static boolean isDataPointTemperatureFeasible(@NonNull final RHTDataPoint dataPoint) {
        final float dataPointTemperatureCelsius = dataPoint.getTemperatureCelsius();
        return dataPointTemperatureCelsius >= TEMPERATURE_LOWER_BOUND &&
                dataPointTemperatureCelsius <= TEMPERATURE_UPPER_BOUND;
    }

    /**
     * Checks if the incoming dataPoint relative humidity value is feasible.
     *
     * @param dataPoint that will be analyzed.
     * @return <code>true</code> if the humidity is between (included) {@link #HUMIDITY_LOWER_BOUND}
     * and {@link #HUMIDITY_UPPER_BOUND} - return <code>false</code> otherwise.
     */
    private static boolean isDataPointHumidityFeasible(@NonNull final RHTDataPoint dataPoint) {
        final float dataPointRelativeHumidity = dataPoint.getRelativeHumidity();
        return dataPointRelativeHumidity >= HUMIDITY_LOWER_BOUND &&
                dataPointRelativeHumidity <= HUMIDITY_UPPER_BOUND;
    }
}
