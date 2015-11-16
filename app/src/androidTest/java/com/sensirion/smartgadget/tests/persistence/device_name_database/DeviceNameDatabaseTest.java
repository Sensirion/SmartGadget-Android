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