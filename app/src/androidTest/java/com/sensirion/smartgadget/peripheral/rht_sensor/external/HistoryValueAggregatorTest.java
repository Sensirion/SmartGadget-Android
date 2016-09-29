package com.sensirion.smartgadget.peripheral.rht_sensor.external;

import android.support.annotation.NonNull;

import com.sensirion.libsmartgadget.GadgetValue;
import com.sensirion.smartgadget.peripheral.rht_utils.RHTDataPoint;

import junit.framework.TestCase;

public class HistoryValueAggregatorTest extends TestCase {

    private RHTValueAggregator mAggregator;

    public void setUp() throws Exception {
        mAggregator = new HistoryValueAggregator();
    }

    public void testAggregateHumidityOnce() throws Exception {
        assertNull(mAggregator.aggregateHumidityValue("abc", new GadgetTestValue(23.5, 1)));
    }

    public void testAggregateTemperatureOnce() throws Exception {
        assertNull(mAggregator.aggregateTemperatureValue("abc", new GadgetTestValue(23.5, 1)));
    }

    public void testAggregateNull() throws Exception {
        try {
            mAggregator.aggregateHumidityValue("abc", null);
            fail();
        } catch (IllegalArgumentException ex) {
            assertNotNull(ex);
        }
    }

    public void testAggregateOnlyHumidity() throws Exception {
        mAggregator.aggregateHumidityValue("abc", new GadgetTestValue(23.5, 1));
        assertNull(mAggregator.aggregateHumidityValue("abc", new GadgetTestValue(25, 1)));
        assertNull(mAggregator.aggregateHumidityValue("abc", new GadgetTestValue(25, 2)));
        assertNull(mAggregator.aggregateHumidityValue("abd", new GadgetTestValue(23.5, 1)));
    }

    public void testAggregateOnlyTemperature() throws Exception {
        mAggregator.aggregateTemperatureValue("abc", new GadgetTestValue(23.5, 1));
        assertNull(mAggregator.aggregateTemperatureValue("abc", new GadgetTestValue(25, 1)));
        assertNull(mAggregator.aggregateTemperatureValue("abc", new GadgetTestValue(25, 2)));
        assertNull(mAggregator.aggregateTemperatureValue("abd", new GadgetTestValue(23.5, 1)));
    }

    public void testAggregateDifferentTimestamps() throws Exception {
        mAggregator.aggregateHumidityValue("abc", new GadgetTestValue(23.5, 1));
        assertNull(mAggregator.aggregateTemperatureValue("abc", new GadgetTestValue(25, 2)));
    }

    public void testAggregateDifferentAddress() throws Exception {
        mAggregator.aggregateHumidityValue("abc", new GadgetTestValue(23.5, 1));
        assertNull(mAggregator.aggregateTemperatureValue("abe", new GadgetTestValue(25, 1)));
    }

    public void testAggregateOne() throws Exception {
        mAggregator.aggregateHumidityValue("abc", new GadgetTestValue(23.5, 1));
        RHTDataPoint data = mAggregator.aggregateTemperatureValue("abc", new GadgetTestValue(25, 1));
        assertNotNull(data);
        assertEquals(23.5f, data.getRelativeHumidity());
        assertEquals(25f, data.getTemperatureCelsius());
        assertEquals(1L, data.getTimestamp());
    }

    public void testAggregateOutOfOrder() throws Exception {
        mAggregator.aggregateHumidityValue("abc", new GadgetTestValue(23.5, 4));
        mAggregator.aggregateHumidityValue("abc", new GadgetTestValue(24.5, 3));
        mAggregator.aggregateHumidityValue("abc", new GadgetTestValue(25.5, 2));
        mAggregator.aggregateHumidityValue("abc", new GadgetTestValue(26.5, 1));
        assertNotNull(mAggregator.aggregateTemperatureValue("abc", new GadgetTestValue(25, 4)));
        assertNotNull(mAggregator.aggregateTemperatureValue("abc", new GadgetTestValue(26, 3)));
        assertNotNull(mAggregator.aggregateTemperatureValue("abc", new GadgetTestValue(27, 2)));
        RHTDataPoint data = mAggregator.aggregateTemperatureValue("abc", new GadgetTestValue(28, 1));
        assertNotNull(data);
        assertEquals(26.5f, data.getRelativeHumidity());
        assertEquals(28f, data.getTemperatureCelsius());
        assertEquals(1L, data.getTimestamp());
    }
}