package com.sensirion.smartgadget.persistence.history_database;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.sensirion.database_library.DatabaseFacade;
import com.sensirion.database_library.attributes.DatabaseAttributes;
import com.sensirion.database_library.database_object.AbstractDatabaseObject;
import com.sensirion.database_library.parser.QueryResult;
import com.sensirion.database_library.parser.QueryResultRow;
import com.sensirion.smartgadget.R;
import com.sensirion.smartgadget.peripheral.rht_sensor.RHTSensorFacade;
import com.sensirion.smartgadget.peripheral.rht_utils.RHTDataPoint;
import com.sensirion.smartgadget.persistence.history_database.table.HistoryDataLast10MinutesView;
import com.sensirion.smartgadget.persistence.history_database.table.HistoryDataLast1DayView;
import com.sensirion.smartgadget.persistence.history_database.table.HistoryDataLast1HourView;
import com.sensirion.smartgadget.persistence.history_database.table.HistoryDataLast1WeekView;
import com.sensirion.smartgadget.persistence.history_database.table.HistoryDataLast6HoursView;
import com.sensirion.smartgadget.persistence.history_database.table.HistoryDataTable;
import com.sensirion.smartgadget.utils.DeviceModel;
import com.sensirion.smartgadget.view.history.HistoryResult;
import com.sensirion.smartgadget.view.history.type.HistoryIntervalType;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class HistoryDatabaseManager {

    private static final String TAG = HistoryDatabaseManager.class.getSimpleName();

    private static final int DATABASE_VERSION = 3;

    @Nullable
    private static HistoryDatabaseManager mInstance = null;

    @NonNull
    private final DatabaseFacade mDatabase;

    private final Map<String, DatapointHandler> mDatapointHandlers = Collections.synchronizedMap(new HashMap<String, DatapointHandler>());

    private final boolean mTestInProgress;

    private HistoryDatabaseManager(@NonNull final Context context, final boolean isTest) {
        mTestInProgress = isTest;
        mDatabase = new DatabaseFacade(context, getPermanentDatabaseAttributes(context));
    }

    @NonNull
    public synchronized static HistoryDatabaseManager getInstance() {
        if (mInstance == null) {
            throw new IllegalStateException(String.format("%s: getInstance() -> DatabaseManager has not been initialized yet.", TAG));
        }
        return mInstance;
    }

    /**
     * Initializes the {@link com.sensirion.smartgadget.persistence.device_name_database.DeviceNameDatabaseManager} with a Context. This method needs to be called once per application.
     *
     * @param context needed for initializing the manager
     * @return <code>true</code> if the databases has been initialized - <code>false</code> if the databases where already initialized.
     */
    public synchronized static boolean init(@NonNull final Context context) {
        return init(context, false);
    }

    /**
     * Initializes the {@link com.sensirion.smartgadget.persistence.history_database.HistoryDatabaseManager} with a Context. This method needs to be called once for application.
     *
     * @param context needed for initializing the manager
     * @param isTest  <code>true</code> if it's a test and the database should be deleted at the end of the execution - <code>false</code> if it's a permanent database.
     * @return <code>true</code> if the databases has been initialized - <code>false</code> if the databases where already initialized.
     */
    public synchronized static boolean init(@NonNull final Context context, final boolean isTest) {
        if (mInstance == null) {
            mInstance = new HistoryDatabaseManager(context.getApplicationContext(), isTest);
            return true;
        }
        return false;
    }

    @NonNull
    private DatabaseAttributes getPermanentDatabaseAttributes(@NonNull final Context context) {
        final String permanentDatabaseName = (mTestInProgress) ? context.getString(R.string.history_database_test) : context.getResources().getString(R.string.history_database);
        return new DatabaseAttributes(permanentDatabaseName, DATABASE_VERSION, getDatabaseObjects(), true);
    }

    @NonNull
    private Queue<AbstractDatabaseObject> getDatabaseObjects() {
        final Queue<AbstractDatabaseObject> databaseObject = new LinkedList<>();
        databaseObject.add(HistoryDataTable.getInstance());
        databaseObject.add(HistoryDataLast10MinutesView.getInstance());
        databaseObject.add(HistoryDataLast1HourView.getInstance());
        databaseObject.add(HistoryDataLast6HoursView.getInstance());
        databaseObject.add(HistoryDataLast1DayView.getInstance());
        databaseObject.add(HistoryDataLast1WeekView.getInstance());
        return databaseObject;
    }

    /**
     * Adds the introduced datapoint to the history, if it's necessary.
     *
     * @param deviceAddress of the device.
     * @param datapoint     that wants to be added to the database.
     */
    public void addRHTData(@NonNull final String deviceAddress, @NonNull final RHTDataPoint datapoint, final boolean isFromHistory) {
        DatapointHandler handler = mDatapointHandlers.get(deviceAddress);
        if (handler == null) {
            handler = new DatapointHandler(deviceAddress);
            mDatapointHandlers.put(deviceAddress, handler);
        }

        if (isFromHistory) {
            handler.addHistoryDatapoint(datapoint);
        } else {
            handler.addLiveDataDatapoint(datapoint);
        }
    }

    /**
     * Obtains the history data from the database.
     *
     * @param interval      that the user wants to retrieve.
     * @param deviceAddress of the device.
     * @return {@link com.sensirion.smartgadget.view.history.HistoryResult} with the found datapoints.
     */
    @Nullable
    @SuppressWarnings("unused")
    public HistoryResult getHistoryPoints(@NonNull final HistoryIntervalType interval, @NonNull final String deviceAddress) {
        return getHistoryPoints(interval, Collections.singletonList(deviceAddress));
    }

    /**
     * Obtains the history data from the database.
     *
     * @param interval    that the user wants to retrieve.
     * @param devicesList list of devices needed by the user.
     * @return {@link com.sensirion.smartgadget.view.history.HistoryResult} with the found datapoints.
     */
    @Nullable
    public synchronized HistoryResult getHistoryPoints(@NonNull final HistoryIntervalType interval, @NonNull final List<String> devicesList) {
        final String sql = interval.getIntervalView().getHistoryDataSql(devicesList);
        final QueryResult queryResult = mDatabase.rawDatabaseQuery(sql);
        if (queryResult == null) {
            Log.e(TAG, "getHistoryPoints -> No results where found in the database.");
            return null;
        }
        final List<QueryResultRow> listOfDatapoints = queryResult.getQueryResults();
        final HistoryResult result = new HistoryResult(devicesList);
        for (final QueryResultRow row : listOfDatapoints) {
            final String deviceAddress = row.getString(HistoryDataTable.COLUMN_DEVICE_ADDRESS);
            result.addResult(deviceAddress, obtainDatapointFromRow(row));
        }
        Log.i(TAG, String.format("getHistoryPoints -> Obtained %d datapoints from the database.", result.size()));
        return result;
    }

    @NonNull
    private RHTDataPoint obtainDatapointFromRow(@NonNull final QueryResultRow row) {
        final float humidity = row.getFloat(HistoryDataTable.COLUMN_HUMIDITY);
        final float temperature = row.getFloat(HistoryDataTable.COLUMN_TEMPERATURE);
        final long timestamp = row.getLong(HistoryDataTable.COLUMN_TIMESTAMP);
        return new RHTDataPoint(temperature, humidity, timestamp);
    }

    /**
     * Obtains the list of devices connected in the selected interval.
     *
     * @param interval that wants to be retrieved. Cannot be <code>null</code>
     * @return {@link java.util.List} with the addresses of the devices in {@link java.lang.String}. Returns an empty list if they are no devices.
     */
    @NonNull
    public List<String> getConnectedDeviceListInterval(@NonNull final HistoryIntervalType interval) {
        Log.d(TAG, String.format("getConnectedDeviceListInterval -> Interval %s was selected.", interval));
        purgeOldDatabaseData();
        final String sql = interval.getIntervalView().getListOfDevicesSql();
        final QueryResult queryResult = mDatabase.rawDatabaseQuery(sql);
        if (queryResult == null) {
            Log.e(TAG, String.format("getConnectedDeviceListInterval -> No results where found in the database on interval %s.", interval.getPosition()));
            return new LinkedList<>();
        }
        final List<QueryResultRow> listOfDatapoints = queryResult.getQueryResults();
        final List<String> unsortedListDevices = new LinkedList<>();
        for (final QueryResultRow device : listOfDatapoints) {
            unsortedListDevices.add(device.getString(0));
        }
        Log.i(TAG, String.format("getConnectedDeviceListInterval -> The device retrieved from the database %d devices.", unsortedListDevices.size()));
        return sortListDevices(unsortedListDevices);
    }

    /**
     * This methods sorts the list of devices conserving the order from the other sections of the application.
     *
     * @param historyDeviceList that has to be ordered.
     * @return {@link java.util.List} of {@link java.lang.String} with the sorted device list.
     */
    @NonNull
    private List<String> sortListDevices(@NonNull final List<String> historyDeviceList) {
        final List<DeviceModel> connectedDevices = RHTSensorFacade.getInstance().getConnectedSensors();
        final List<String> unsortedDeviceList = new LinkedList<>(historyDeviceList);
        final List<String> sortedDeviceList = new LinkedList<>();
        for (final DeviceModel connectedDevice : connectedDevices) {
            final String connectedDeviceAddress = connectedDevice.getAddress();
            sortedDeviceList.add(connectedDeviceAddress);
            unsortedDeviceList.remove(connectedDeviceAddress);
        }
        Collections.sort(unsortedDeviceList);
        sortedDeviceList.addAll(unsortedDeviceList);
        return sortedDeviceList;
    }

    /**
     * Deletes all the unnecessary data from the device.
     */
    public synchronized void purgeOldDatabaseData() {
        final long initialTime = System.currentTimeMillis();
        final long numberDataPoints = mDatabase.rawDatabaseQuery(HistoryDataTable.getInstance().getNumberRowsSql()).getFirstQueryResult().getInt(0);
        mDatabase.executeSQL(HistoryDataTable.getInstance().purgeOldDataSql());
        final long numberDataPointsAfterPurge = mDatabase.rawDatabaseQuery(HistoryDataTable.getInstance().getNumberRowsSql()).getFirstQueryResult().getInt(0);
        Log.d(TAG, String.format("purgeOldDatabaseData -> Cleaned old elements: Elements at the beginning: %d - Elements at the end: %d  - Time: %d milliseconds.", numberDataPoints, numberDataPointsAfterPurge, (System.currentTimeMillis() - initialTime)));
    }

    /**
     * Gets the database facade used for storing history values.
     *
     * @return {@link com.sensirion.database_library.DatabaseFacade} with the permanent database.
     */
    @NonNull
    DatabaseFacade getDatabaseFacade() {
        return mDatabase;
    }
}