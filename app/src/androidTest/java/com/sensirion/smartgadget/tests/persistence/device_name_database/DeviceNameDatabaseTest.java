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
package com.sensirion.smartgadget.tests.persistence.device_name_database;

import android.content.Context;
import android.support.annotation.Nullable;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.sensirion.database_library.DatabaseFacade;
import com.sensirion.smartgadget.persistence.device_name_database.DeviceNameDatabaseManager;

public class DeviceNameDatabaseTest extends AndroidTestCase {

    private static final String mGadgetAddress001 = "AA:BB:CC:DD:EE:FF";
    private static final String mGadgetName001 = "Kitchen";
    private static final String mGadgetName002 = "Laundry";

    @Nullable
    private DeviceNameDatabaseManager mDeviceNameDatabaseManager;

    /**
     * {@inheritDoc}
     */
    public void setUp() throws Exception {
        super.setUp();
        final Context context = getContext();
        DeviceNameDatabaseManager.init(context.getApplicationContext(), true);
        mDeviceNameDatabaseManager = DeviceNameDatabaseManager.getInstance();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (mDeviceNameDatabaseManager != null) {
            final DatabaseFacade dbFacade = mDeviceNameDatabaseManager.getDatabaseFacade();
            dbFacade.closeDatabaseConnection();
        }
    }

    @SmallTest
    public void testPrerequisites() {
        assertNotNull("testPrerequisites: mDeviceNameDatabaseManager is needed", mDeviceNameDatabaseManager);
    }

    @SmallTest
    public void testAddDevice() {
        assertNotNull("testAddDevice: Device name manager is needed", mDeviceNameDatabaseManager);
        mDeviceNameDatabaseManager.updateDeviceName(mGadgetAddress001, mGadgetName001);
        final String retrievedString = mDeviceNameDatabaseManager.readDeviceName(mGadgetAddress001);
        assertEquals("testAddDevice: Inserted device was not retrieved successfully", retrievedString, mGadgetName001);
    }

    @SmallTest
    public void testUpdateDevice() {
        assertNotNull("testUpdateDevice: Device name manager is needed", mDeviceNameDatabaseManager);
        this.testAddDevice();
        mDeviceNameDatabaseManager.updateDeviceName(mGadgetAddress001, mGadgetName002);
        final String updatedString = mDeviceNameDatabaseManager.readDeviceName(mGadgetAddress001);
        assertEquals("testUpdateDevice: Device was not updated successfully", updatedString, mGadgetName002);
    }
}
