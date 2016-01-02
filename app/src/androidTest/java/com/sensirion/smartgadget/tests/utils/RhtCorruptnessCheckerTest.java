package com.sensirion.smartgadget.tests.utils;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.sensirion.libble.utils.RHTDataPoint;

import static com.sensirion.smartgadget.utils.RhtCorruptnessChecker.HUMIDITY_LOWER_BOUND;
import static com.sensirion.smartgadget.utils.RhtCorruptnessChecker.HUMIDITY_UPPER_BOUND;
import static com.sensirion.smartgadget.utils.RhtCorruptnessChecker.TEMPERATURE_LOWER_BOUND;
import static com.sensirion.smartgadget.utils.RhtCorruptnessChecker.TEMPERATURE_UPPER_BOUND;
import static com.sensirion.smartgadget.utils.RhtCorruptnessChecker.isDatapointCorrupted;

public class RhtCorruptnessCheckerTest extends AndroidTestCase {

    @SmallTest
    public void testDataPointInsideBounds() {
        final RHTDataPoint dataPoint = new RHTDataPoint(
                TEMPERATURE_LOWER_BOUND + 1f, HUMIDITY_LOWER_BOUND + 1f, System.currentTimeMillis()
        );
        assertTrue(isDatapointCorrupted(dataPoint));
    }

    @SmallTest
    public void testDataPointCornerCaseLowerBounds() {
        final RHTDataPoint dataPoint = new RHTDataPoint(
                TEMPERATURE_LOWER_BOUND, HUMIDITY_LOWER_BOUND, System.currentTimeMillis()
        );
        assertTrue(isDatapointCorrupted(dataPoint));
    }

    @SmallTest
    public void testDataPointCornerCaseUpperBounds() {
        final RHTDataPoint dataPoint = new RHTDataPoint(
                TEMPERATURE_UPPER_BOUND, HUMIDITY_UPPER_BOUND, System.currentTimeMillis()
        );
        assertTrue(isDatapointCorrupted(dataPoint));
    }

    @SmallTest
    public void testDataPointTemperatureLowerThanLowerBound() {
        final float validHumidity = HUMIDITY_UPPER_BOUND - 1f;
        final RHTDataPoint dataPoint = new RHTDataPoint(
                TEMPERATURE_LOWER_BOUND - 1f, validHumidity, System.currentTimeMillis()
        );
        assertFalse(isDatapointCorrupted(dataPoint));
    }

    @SmallTest
    public void testDataPointTemperatureUpperThanUpperBound() {
        final float validHumidity = HUMIDITY_UPPER_BOUND - 1f;
        final RHTDataPoint dataPoint = new RHTDataPoint(
                TEMPERATURE_UPPER_BOUND + 1f, validHumidity, System.currentTimeMillis()
        );
        assertFalse(isDatapointCorrupted(dataPoint));
    }

    @SmallTest
    public void testDataPointHumidityLowerThanLowerBound() {
        final float validTemperature = TEMPERATURE_UPPER_BOUND - 1f;
        final RHTDataPoint dataPoint = new RHTDataPoint(
                validTemperature, HUMIDITY_LOWER_BOUND - 1f, System.currentTimeMillis()
        );
        assertFalse(isDatapointCorrupted(dataPoint));
    }

    @SmallTest
    public void testDataPointHumidityUpperThanUpperBound() {
        final float validTemperature = TEMPERATURE_UPPER_BOUND - 1f;
        final RHTDataPoint dataPoint = new RHTDataPoint(
                validTemperature, HUMIDITY_UPPER_BOUND + 1f, System.currentTimeMillis()
        );
        assertFalse(isDatapointCorrupted(dataPoint));
    }
}
