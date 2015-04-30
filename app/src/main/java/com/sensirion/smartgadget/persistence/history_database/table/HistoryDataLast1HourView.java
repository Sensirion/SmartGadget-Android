package com.sensirion.smartgadget.persistence.history_database.table;

import android.support.annotation.NonNull;

import com.sensirion.smartgadget.utils.Interval;

public class HistoryDataLast1HourView extends AbstractHistoryDataView {

    static final String VIEW_NAME = "history_data_last_1_hour";

    private static final byte BIN_SIZE = 2;

    private static HistoryDataLast1HourView mInstance;

    private HistoryDataLast1HourView() {
        super(VIEW_NAME);
    }

    public synchronized static HistoryDataLast1HourView getInstance() {
        if (mInstance == null) {
            mInstance = new HistoryDataLast1HourView();
        }
        return mInstance;
    }

    @Override
    @NonNull
    public String createSqlStatement() {
        /*  CREATE VIEW IF NOT EXISTS  history_data_last_1_hour
            AS SELECT device_mac, timestamp, temperature, humidity
            FROM history_data
            WHERE bin_size = 2
            UNION
            SELECT device_mac, MID(timestamp), MID(temperature), MID(humidity)
            FROM history_data_last_10_min
            GROUP BY device_mac, ROUND ((DATE('now') - timestamp) / 60);
        */
        final String oneHourBeanSqlSelect = String.format("SELECT %s, %s, %s, %s FROM %s WHERE %s = %d",
                HistoryDataTable.COLUMN_DEVICE_ADDRESS, HistoryDataTable.COLUMN_TIMESTAMP, HistoryDataTable.COLUMN_TEMPERATURE, HistoryDataTable.COLUMN_HUMIDITY,
                HistoryDataTable.TABLE_NAME, HistoryDataTable.COLUMN_BIN_SIZE, BIN_SIZE);

        final String previousBeanSql = String.format("SELECT %s, AVG(%s), AVG(%s), AVG(%s) FROM %s GROUP BY %s, ROUND((%s - %s) / %d)",
                HistoryDataTable.COLUMN_DEVICE_ADDRESS, HistoryDataTable.COLUMN_TIMESTAMP, HistoryDataTable.COLUMN_TEMPERATURE, HistoryDataTable.COLUMN_HUMIDITY,
                HistoryDataLast10MinutesView.VIEW_NAME, HistoryDataTable.COLUMN_DEVICE_ADDRESS, System.currentTimeMillis(), HistoryDataTable.COLUMN_TIMESTAMP, getResolution());

        return String.format("CREATE VIEW IF NOT EXISTS %s AS %s UNION %s;", VIEW_NAME, oneHourBeanSqlSelect, previousBeanSql);
    }

    @Override
    public int getNumberMilliseconds() {
        return Interval.ONE_HOUR.getNumberMilliseconds();
    }

    @Override
    public int getResolution() {
        return HistoryDataTable.RESOLUTION_ONE_HOUR_MS;
    }
}