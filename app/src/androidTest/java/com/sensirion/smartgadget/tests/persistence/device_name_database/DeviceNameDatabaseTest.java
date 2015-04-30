package com.sensirion.smartgadget.tests.persistence.device_name_database;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;

import com.sensirion.database_library.DatabaseFacade;
import com.sensirion.database_library.parser.QueryResult;
import com.sensirion.smartgadget.persistence.device_name_database.DeviceNameDatabaseManager;
import com.sensirion.smartgadget.view.MainActivity;

public class DeviceNameDatabaseTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private static final String mGadgetAddress001 = "AA:BB:CC:DD:EE:FF";
    private static final String mGadgetName001 = "Kitchen";
    private static final String mGadgetName002 = "Laundry";
    private static final Long mTestNumber001 = 10l;

    @Nullable
    private final DeviceNameDatabaseManager mDeviceNameDatabaseManager;
    @NonNull
    private final DatabaseFacade mDatabaseFacade;

    public DeviceNameDatabaseTest() {
        super(MainActivity.class);
        DeviceNameDatabaseManager.init(getActivity().getApplicationContext(), true);
        mDeviceNameDatabaseManager = DeviceNameDatabaseManager.getInstance();
        mDatabaseFacade = mDeviceNameDatabaseManager.getDatabaseFacade();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        mDatabaseFacade.closeDatabaseConnection();
    }

    @SmallTest
    public void testCheckSelectDatabaseStandardQuery() {
        final QueryResult result = mDatabaseFacade.query("DUAL", new String[]{mTestNumber001.toString()}, null, null, null, null, null, null);
        assertEquals(result.getFirstQueryResult().getLong(0), mTestNumber001);
    }

    @SmallTest
    public void testCheckSelectRawQueryWithoutAttributes() {
        final QueryResult result = mDatabaseFacade.rawDatabaseQuery("SELECT " + mTestNumber001 + " FROM DUAL");
        assertEquals(result.getFirstQueryResult().getLong(0), mTestNumber001);
    }

    @SmallTest
    public void testCheckSelectRawQueryWithAttributes() {
        final QueryResult result = mDatabaseFacade.rawDatabaseQuery("SELECT ? FROM DUAL", new String[]{mTestNumber001.toString()});
        assertEquals(result.getFirstQueryResult().getLong(0), mTestNumber001);
    }

    @SmallTest
    public void testAddDevice() {
        mDeviceNameDatabaseManager.updateDeviceName(mGadgetAddress001, mGadgetName001);
        String retrievedString = DeviceNameDatabaseManager.getInstance().readDeviceName(mGadgetAddress001);
        assertEquals(retrievedString, mGadgetName001);
    }

    @SmallTest
    public void testUpdateDevice() {
        mDeviceNameDatabaseManager.updateDeviceName(mGadgetAddress001, mGadgetName002);
        String retrievedString = DeviceNameDatabaseManager.getInstance().readDeviceName(mGadgetAddress001);
        assertEquals(retrievedString, mGadgetName002);
    }

    @SmallTest
    public void testDatabaseConnectivity() {
        mDatabaseFacade.openClosedDatabaseConnection();
        assertTrue(mDatabaseFacade.isDatabaseOpenForWrite());
        mDatabaseFacade.closeDatabaseConnection();
        assertFalse(mDatabaseFacade.isDatabaseOpenForWrite());
        mDatabaseFacade.openClosedDatabaseConnection();
        assertTrue(mDatabaseFacade.isDatabaseOpenForWrite());
    }
}