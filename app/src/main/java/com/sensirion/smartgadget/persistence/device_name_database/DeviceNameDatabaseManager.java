package com.sensirion.smartgadget.persistence.device_name_database;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.sensirion.database_library.DatabaseFacade;
import com.sensirion.database_library.attributes.DatabaseAttributes;
import com.sensirion.database_library.database_object.AbstractDatabaseObject;
import com.sensirion.database_library.parser.QueryResult;
import com.sensirion.smartgadget.R;
import com.sensirion.smartgadget.persistence.device_name_database.table.DeviceNameTable;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

@SuppressWarnings("WeakerAccess")
public class DeviceNameDatabaseManager {

    private static final String TAG = DeviceNameDatabaseManager.class.getSimpleName();

    private static final int DATABASE_VERSION = 6;

    @Nullable
    private static DeviceNameDatabaseManager mInstance = null;
    protected final boolean mTestInProgress;
    @NonNull
    private final DatabaseFacade mDatabase;
    private final Map<String, String> mKnownDeviceNames = Collections.synchronizedMap(new HashMap<String, String>());

    protected DeviceNameDatabaseManager(@NonNull final Context context, final boolean isTest) {
        mTestInProgress = isTest;
        mDatabase = new DatabaseFacade(context, getPermanentDatabaseAttributes(context.getApplicationContext()));
    }

    @NonNull
    public synchronized static DeviceNameDatabaseManager getInstance() {
        if (mInstance == null) {
            throw new IllegalStateException(String.format("%s -> %s has not been initialized yet!", TAG, TAG));
        }
        return mInstance;
    }

    /**
     * Initializes the {@link DeviceNameDatabaseManager} with a Context. This method needs to be called once per application.
     *
     * @param context needed for initialize the manager
     * @return <code>true</code> if the databases has been initialized - <code>false</code> if the databases where already initialized.
     */
    public synchronized static boolean init(@NonNull final Context context) {
        return init(context, false);
    }

    /**
     * Initializes the {@link DeviceNameDatabaseManager} with a Context. This method needs to be called once for application.
     *
     * @param context needed for initialize the manager
     * @param isTest  <code>true</code> if it's a test and the database should be deleted at the end of the execution - <code>false</code> if it's a permanent database.
     * @return <code>true</code> if the databases has been initialized - <code>false</code> if the databases where already initialized.
     */
    public synchronized static boolean init(@Nullable final Context context, final boolean isTest) {
        if (mInstance == null) {
            if (context == null) {
                throw new IllegalArgumentException(String.format("%s: init -> Received null context.", TAG));
            }
            mInstance = new DeviceNameDatabaseManager(context.getApplicationContext(), isTest);
            return true;
        }
        return false;
    }

    @NonNull
    private Queue<AbstractDatabaseObject> getListPermanentTables() {
        final Queue<AbstractDatabaseObject> tableList = new LinkedList<>();
        tableList.add(DeviceNameTable.getInstance());
        return tableList;
    }

    @NonNull
    private DatabaseAttributes getPermanentDatabaseAttributes(@NonNull final Context context) {
        final String permanentDatabaseName = (mTestInProgress) ? context.getString(R.string.user_name_database_test) : context.getResources().getString(R.string.user_name_database);
        return new DatabaseAttributes(permanentDatabaseName, DATABASE_VERSION, getListPermanentTables(), true);
    }

    /**
     * Checks inside the database in case the user has a name assigned for a device.
     *
     * @param deviceAddress that we want to check in the database.
     * @return the user device name if the device has already a name inside the database - the mac address if not.
     */
    public String readDeviceName(@NonNull final String deviceAddress) {
        if (mKnownDeviceNames.containsKey(deviceAddress)) {
            return mKnownDeviceNames.get(deviceAddress);
        }
        final String sqlQuery = DeviceNameTable.getInstance().readUserDeviceNameSql(deviceAddress);
        final QueryResult queryResult = mDatabase.rawDatabaseQuery(sqlQuery);
        if (queryResult == null) {
            mKnownDeviceNames.put(deviceAddress, deviceAddress);
            return deviceAddress;
        }
        final String deviceName = queryResult.getFirstQueryResult().getString(DeviceNameTable.COLUMN_USER_DEVICE_NAME);
        mKnownDeviceNames.put(deviceAddress, deviceName);
        return deviceName;
    }

    /**
     * Adds, updates or deletes a device name in the database.
     *
     * @param deviceAddress Device we want to remember it's new name. Cannot be <code>null</code>
     * @param deviceName    Device new name. Cannot be <code>null</code>
     */
    public void updateDeviceName(@NonNull final String deviceAddress, @NonNull final String deviceName) {
        final DeviceNameTable deviceNameTable = DeviceNameTable.getInstance();
        final String oldDeviceName = readDeviceName(deviceAddress);
        final String sqlQuery;

        String deviceNameToStore = deviceName.trim();

        if (deviceNameToStore.isEmpty() || deviceNameToStore.equals(deviceAddress)) {
            sqlQuery = deviceNameTable.deleteDeviceNameSql(deviceAddress);
            deviceNameToStore = deviceAddress;
        } else if (!deviceAddress.equals(oldDeviceName)) {
            // We already have a device name, so we update the database with a new value.
            sqlQuery = deviceNameTable.updateDeviceNameSql(deviceAddress, deviceNameToStore);
        } else {
            sqlQuery = deviceNameTable.insertUserDeviceNameSql(deviceAddress, deviceNameToStore);
        }
        mDatabase.executeSQL(sqlQuery);
        mDatabase.commit();
        mKnownDeviceNames.put(deviceAddress, deviceNameToStore);
    }

    /**
     * Closes database connection.
     */
    public void closeDatabaseConnection() {
        mDatabase.closeDatabaseConnection();
    }

    /**
     * Gets the database used for storing permanent values.
     *
     * @return {@link  com.sensirion.database_library.DatabaseFacade} with the permanent database.
     */
    @NonNull
    public DatabaseFacade getDatabaseFacade() {
        return mDatabase;
    }
}
