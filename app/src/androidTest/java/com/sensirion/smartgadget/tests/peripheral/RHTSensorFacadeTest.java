/*
 * Copyright (c) 2017, Sensirion AG
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of Sensirion AG nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.sensirion.smartgadget.tests.peripheral;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.sensirion.smartgadget.peripheral.rht_sensor.RHTSensorFacade;
import com.sensirion.smartgadget.peripheral.rht_sensor.RHTSensorListener;

public class RHTSensorFacadeTest extends AndroidTestCase {

    @NonNull
    private static final String TEST_DEVICE_ADDRESS_1 = "AA:BB:CC:DD:EE:F1";
    @NonNull
    private static final String TEST_DEVICE_ADDRESS_2 = "AA:BB:CC:DD:EE:F2";

    private static final float TEST_TEMPERATURE = 25f;
    private static final float TEST_HUMIDITY = 54f;

    @Nullable
    private RHTSensorFacade mSensorFacade;

    /**
     * {@inheritDoc}
     */
    public void setUp() throws Exception {
        super.setUp();
        final Context context = getContext();
        RHTSensorFacade.init(context);
        mSensorFacade = RHTSensorFacade.getInstance();
    }

    @SmallTest
    public void testPrerequisites() {
        assertNotNull("testPrerequisites: mSensorFacade is needed", mSensorFacade);
    }

    /**
     * Test that when the user calls {@link RHTSensorFacade#registerListener(RHTSensorListener)}
     * the last DataPoint registered is properly notified.
     */
    @SmallTest
    public void testReceiveSingleDeviceLiveValuesOnListenerRegistration() {
        assertNotNull(
                "testReceiveLiveValuesOnRegistration: mSensorFacade is needed",
                mSensorFacade
        );
        mSensorFacade.onNewRHTData(TEST_TEMPERATURE, TEST_HUMIDITY, TEST_DEVICE_ADDRESS_1);
        final TestRHTListener listener = new TestRHTListener();
        mSensorFacade.registerListener(listener);

        // Test if the device humidity is properly notified.
        assertNotNull(listener.lastHumidity);
        assertTrue(listener.lastHumidity == TEST_HUMIDITY);

        // Test if the device temperature is properly notified.
        assertNotNull(listener.lastTemperature);
        assertTrue(listener.lastTemperature == TEST_TEMPERATURE);

        // Test if the device address is properly notified
        assertNotNull(listener.lastDeviceAddress);
        assertEquals(listener.lastDeviceAddress, TEST_DEVICE_ADDRESS_1);
    }

    /**
     * Test that when the user calls {@link RHTSensorFacade#registerListener(RHTSensorListener)}
     * it receives values from all devices that sent live values to the {@link RHTSensorFacade}
     */
    @SmallTest
    public void testReceiveMultipleDeviceLiveValuesOnListenerRegistration() {
        assertNotNull(
                "testReceiveLiveValuesOnRegistration: mSensorFacade is needed",
                mSensorFacade
        );
        mSensorFacade.onNewRHTData(TEST_TEMPERATURE, TEST_HUMIDITY, TEST_DEVICE_ADDRESS_1);
        mSensorFacade.onNewRHTData(TEST_TEMPERATURE, TEST_HUMIDITY, TEST_DEVICE_ADDRESS_2);
        final TestRHTListener listener = new TestRHTListener();
        mSensorFacade.registerListener(listener);

        // The device must receive notifications from the two devices with live values.
        assertTrue(listener.numberOfNotifications == 2);
    }

    private static class TestRHTListener implements RHTSensorListener {

        @Nullable
        private Float lastTemperature = null;
        @Nullable
        private Float lastHumidity = null;
        @Nullable
        private String lastDeviceAddress = null;

        private int numberOfNotifications = 0;

        /**
         * {@inheritDoc}
         */
        @Override
        public void onNewRHTSensorData(final float temperature,
                                       final float relativeHumidity,
                                       @Nullable final String deviceAddress) {
            this.lastTemperature = temperature;
            this.lastHumidity = relativeHumidity;
            this.lastDeviceAddress = deviceAddress;
            this.numberOfNotifications += 1;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onGadgetConnectionChanged(@NonNull final String deviceAddress,
                                              final boolean deviceIsConnected) {
            // DO NOTHING
        }
    }
}
