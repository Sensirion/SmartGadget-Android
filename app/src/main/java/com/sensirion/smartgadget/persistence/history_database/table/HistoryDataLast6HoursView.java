package com.sensirion.smartgadget.persistence.history_database.table;

import android.support.annotation.NonNull;

import com.sensirion.smartgadget.utils.Interval;

public class HistoryDataLast6HoursView extends AbstractHistoryDataView {

    static final String VIEW_NAME = "history_data_last_6_hours";

    private static final byte BIN_SIZE = 3;

    private static HistoryDataLast6HoursView mInstance;

    private HistoryDataLast6HoursView() {
        super(VIEW_NAME);
    }

    public synchronized static HistoryDataLast6HoursView getInstance() {
        if (mInstance == null) {
            mInstance = new HistoryDataLast6HoursView();
        }
        return mInstance;
    }

    @Override
    @NonNull
    public String createSqlStatement() {
        /*  CREATE VIEW IF NOT EXISTS  history_data_last_6_hours
            AS (SELECT device_mac, timestamp, temperature, humidity
            FROM history_data
            WHERE bin_size = 3
            UNION
            SELECT device_mac, AVG(timestamp), AVG(temperature), AVG(humidity)
            FROM history_data_last_10_min
            GROUP BY device_mac, ROUND ((now() - timestamp) / 360);
        */

        final String oneHourBeanSqlSelect = String.format("SELECT %s, %s, %s, %s FROM %s WHERE %s = %d",
                HistoryDataTable.COLUMN_DEVICE_ADDRESS, HistoryDataTable.COLUMN_TIMESTAMP, HistoryDataTable.COLUMN_TEMPERATURE, HistoryDataTable.COLUMN_HUMIDITY,
                HistoryDataTable.TABLE_NAME, HistoryDataTable.COLUMN_BIN_SIZE, BIN_SIZE);

        final String previousBeanSql = String.format("SELECT %s, AVG(%s), AVG(%s), AVG(%s) FROM %s GROUP BY %s, ROUND((%s - %s) / %d)",
                HistoryDataTable.COLUMN_DEVICE_ADDRESS, HistoryDataTable.COLUMN_TIMESTAMP, HistoryDataTable.COLUMN_TEMPERATURE, HistoryDataTable.COLUMN_HUMIDITY,
                HistoryDataLast1HourView.VIEW_NAME, HistoryDataTable.COLUMN_DEVICE_ADDRESS, System.currentTimeMillis(), HistoryDataTable.COLUMN_TIMESTAMP, getResolution());

        return String.format("CREATE VIEW IF NOT EXISTS %s AS %s UNION %s;", VIEW_NAME, oneHourBeanSqlSelect, previousBeanSql);
    }

    @Override
    public int getNumberMilliseconds() {
        return Interval.SIX_HOURS.getNumberMilliseconds();
    }

    @Override
    public int getResolution() {
        return HistoryDataTable.RESOLUTION_SIX_HOURS_MS;
    }
}