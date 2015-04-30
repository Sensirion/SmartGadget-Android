package com.sensirion.smartgadget.persistence.history_database.table;

import android.support.annotation.NonNull;

import com.sensirion.smartgadget.utils.Interval;

public class HistoryDataLast1DayView extends AbstractHistoryDataView {

    static final String VIEW_NAME = "history_data_last_1_day";

    private static final byte BIN_SIZE = 4;

    private static HistoryDataLast1DayView mInstance;

    private HistoryDataLast1DayView() {
        super(VIEW_NAME);
    }

    public synchronized static HistoryDataLast1DayView getInstance() {
        if (mInstance == null) {
            mInstance = new HistoryDataLast1DayView();
        }
        return mInstance;
    }

    @Override
    @NonNull
    public String createSqlStatement() {
        /*  CREATE VIEW IF NOT EXISTS history_data_last_1_day
            AS SELECT device_mac, timestamp, temperature, humidity
            FROM history_data
            WHERE bin_size = 4
            UNION
            SELECT device_mac, AVG(timestamp), AVG(temperature), AVG(humidity)
            FROM history_data_last_10_min
            GROUP BY device_mac, ROUND ((DATE('now') - timestamp) / MILLISECONDS_IN_ONE_DAY_RESOLUTION);
        */

        final String oneHourBeanSqlSelect = String.format("SELECT %s, %s, %s, %s FROM %s WHERE %s = %d",
                HistoryDataTable.COLUMN_DEVICE_ADDRESS, HistoryDataTable.COLUMN_TIMESTAMP, HistoryDataTable.COLUMN_TEMPERATURE, HistoryDataTable.COLUMN_HUMIDITY,
                HistoryDataTable.TABLE_NAME, HistoryDataTable.COLUMN_BIN_SIZE, BIN_SIZE);

        final String previousBeanSql = String.format("SELECT %s, AVG(%s), AVG(%s), AVG(%s) FROM %s GROUP BY %s, ROUND((%s - %s) / %d)",
                HistoryDataTable.COLUMN_DEVICE_ADDRESS, HistoryDataTable.COLUMN_TIMESTAMP, HistoryDataTable.COLUMN_TEMPERATURE, HistoryDataTable.COLUMN_HUMIDITY,
                HistoryDataLast6HoursView.VIEW_NAME, HistoryDataTable.COLUMN_DEVICE_ADDRESS, System.currentTimeMillis(), HistoryDataTable.COLUMN_TIMESTAMP, getResolution());

        return String.format("CREATE VIEW IF NOT EXISTS %s AS %s UNION %s;", VIEW_NAME, oneHourBeanSqlSelect, previousBeanSql);
    }

    @Override
    public int getNumberMilliseconds() {
        return Interval.ONE_DAY.getNumberMilliseconds();
    }

    public int getResolution() {
        return HistoryDataTable.RESOLUTION_ONE_DAY_MS;
    }
}