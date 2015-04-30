package com.sensirion.smartgadget.persistence.history_database.table;

import android.support.annotation.NonNull;

import com.sensirion.smartgadget.utils.Interval;

public class HistoryDataLast1WeekView extends AbstractHistoryDataView {

    private static final String VIEW_NAME = "history_data_last_1_week";

    private static final byte BIN_SIZE = 5;

    private static HistoryDataLast1WeekView mInstance;

    private HistoryDataLast1WeekView() {
        super(VIEW_NAME);
    }

    public synchronized static HistoryDataLast1WeekView getInstance() {
        if (mInstance == null) {
            mInstance = new HistoryDataLast1WeekView();
        }
        return mInstance;
    }

    @Override
    @NonNull
    public String createSqlStatement() {
        /*  CREATE VIEW IF NOT EXISTS history_data_last_1_week
            AS (SELECT device_mac, timestamp, temperature, humidity
            FROM history_data
            WHERE bin_size = 5
            UNION
            SELECT device_mac, AVG(timestamp), AVG(temperature), AVG(humidity)
            FROM history_data_1_day
            GROUP BY device_mac, ROUND ((now() - timestamp) / SECONDS_IN_ONE_DAY_RESOLUTION);
        */

        final String oneWeekSqlSelect = String.format("SELECT %s, %s, %s, %s FROM %s WHERE %s = %d",
                HistoryDataTable.COLUMN_DEVICE_ADDRESS, HistoryDataTable.COLUMN_TIMESTAMP, HistoryDataTable.COLUMN_TEMPERATURE, HistoryDataTable.COLUMN_HUMIDITY,
                HistoryDataTable.TABLE_NAME, HistoryDataTable.COLUMN_BIN_SIZE, BIN_SIZE);

        final String previousBeanSql = String.format("SELECT %s, %s, %s, %s FROM %s",
                HistoryDataTable.COLUMN_DEVICE_ADDRESS, HistoryDataTable.COLUMN_TIMESTAMP, HistoryDataTable.COLUMN_TEMPERATURE, HistoryDataTable.COLUMN_HUMIDITY, HistoryDataLast1DayView.VIEW_NAME);

        return String.format("CREATE VIEW IF NOT EXISTS %s AS %s UNION %s;", VIEW_NAME, oneWeekSqlSelect, previousBeanSql);
    }

    @Override
    public int getNumberMilliseconds() {
        return Interval.ONE_WEEK.getNumberMilliseconds();
    }

    @Override
    public int getResolution() {
        return HistoryDataTable.RESOLUTION_ONE_WEEK_MS;
    }
}