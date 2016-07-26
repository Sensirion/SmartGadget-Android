package com.sensirion.smartgadget.persistence.history_database;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.sensirion.database_library.DatabaseFacade;
import com.sensirion.smartgadget.peripheral.rht_utils.RHTDataPoint;
import com.sensirion.smartgadget.persistence.history_database.table.HistoryDataTable;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class DatapointHandler {
    private static final String TAG = DatapointHandler.class.getSimpleName();
    private static final byte SECONDS_MINIMUM_HISTORY_RESOLUTION = 10;

    private final String mDeviceAddress;
    private final List<RHTDataPoint> mLastNotificationDataPoints = new LinkedList<>();
    private final ExecutorService mBackgroundExecutor;

    private boolean mIsFirstReceivedValue = true;

    DatapointHandler(@NonNull final String deviceAddress) {
        mDeviceAddress = deviceAddress;
        mBackgroundExecutor = Executors.newSingleThreadExecutor(Executors.defaultThreadFactory());
    }

    private static long obtainMeanTimestamp(@NonNull final List<RHTDataPoint> dataPointList) {
        long timestampSum = 0;
        for (final RHTDataPoint dataPoint : dataPointList) {
            timestampSum += dataPoint.getTimestamp();
        }
        return timestampSum / dataPointList.size();
    }

    private static float obtainMeanTemperature(@NonNull final List<RHTDataPoint> dataPointList) {
        double temperatureSum = 0.0d;
        for (final RHTDataPoint datapoint : dataPointList) {
            temperatureSum += datapoint.getTemperatureCelsius();
        }
        return (float) (temperatureSum / dataPointList.size());
    }

    private static float obtainMeanHumidity(@NonNull final List<RHTDataPoint> dataPointList) {
        double humiditySum = 0.0d;
        for (final RHTDataPoint datapoint : dataPointList) {
            humiditySum += datapoint.getRelativeHumidity();
        }
        return (float) (humiditySum / dataPointList.size());
    }

    /**
     * This methods adds a datapoint coming from the database logging functionality to the history database.
     *
     * @param logDataPoint that will be put inside the database table.
     */
    synchronized void addHistoryDatapoint(@NonNull final RHTDataPoint logDataPoint) {
        Log.d(TAG, String.format("addHistoryDatapoint -> Adding logging datapoint to history database: %s.", logDataPoint));
        insertDatapointDatabase(logDataPoint.getTimestamp(), logDataPoint.getTemperatureCelsius(), logDataPoint.getRelativeHumidity(), true);
    }

    /**
     * This method adds a datapoint coming from live notifications to the list.
     *
     * @param dataPoint with the data that will be parsed in order to be put in the history data table.
     */
    synchronized void addLiveDataDatapoint(@NonNull final RHTDataPoint dataPoint) {
        mLastNotificationDataPoints.add(dataPoint);
        if (hasPermissionToWriteNotificationDatapoint()) {
            final RHTDataPoint datapoint = prepareNotificationDatapoint();
            insertDatapointDatabase(datapoint.getTimestamp(), datapoint.getTemperatureCelsius(), datapoint.getRelativeHumidity(), false);
            mLastNotificationDataPoints.clear();
            Log.d(TAG, String.format("writeDatapointToDatabase -> Adding logging datapoint to history database: %s.", datapoint));
        }
    }

    private boolean hasPermissionToWriteNotificationDatapoint() {
        if (mIsFirstReceivedValue) {
            return true;
        }
        final long initialTime = mLastNotificationDataPoints.get(0).getTimestamp();
        final long finalTime = mLastNotificationDataPoints.get(mLastNotificationDataPoints.size() - 1).getTimestamp();
        final int timeDiff = (int) ((finalTime - initialTime) / 1000);
        return timeDiff > SECONDS_MINIMUM_HISTORY_RESOLUTION;
    }

    @NonNull
    private RHTDataPoint prepareNotificationDatapoint() {
        mIsFirstReceivedValue = false;
        final long timestamp = obtainMeanTimestamp(mLastNotificationDataPoints);
        final float temperature = obtainMeanTemperature(mLastNotificationDataPoints);
        final float humidity = obtainMeanHumidity(mLastNotificationDataPoints);
        return new RHTDataPoint(temperature, humidity, timestamp);
    }

    private void insertDatapointDatabase(final long timestamp, final float temperature, final float humidity, final boolean comesFromLog) {
        mBackgroundExecutor.submit(new Runnable() {
            @Override
            public void run() {
                final String sql = HistoryDataTable.getInstance().insertValueSql(mDeviceAddress, timestamp, temperature, humidity, comesFromLog);
                final DatabaseFacade databaseFacade = HistoryDatabaseManager.getInstance().getDatabaseFacade();
                databaseFacade.rawDatabaseQuery(sql);
                databaseFacade.commit();
                Log.d(TAG, String.format("insertDatapointDatabase: DeviceAddress: %s, Timestamp: %d, Temperature: %f, humidity: %f, comesFromLog: %b",
                        mDeviceAddress, timestamp, temperature, humidity, comesFromLog));
            }
        });
    }

    @Override
    @SuppressWarnings("SimplifiableIfStatement")
    public boolean equals(@Nullable final Object o) {
        if (o != null && o instanceof DatapointHandler) {
            return ((DatapointHandler) o).mDeviceAddress.equals(mDeviceAddress);
        }
        return false;
    }
}