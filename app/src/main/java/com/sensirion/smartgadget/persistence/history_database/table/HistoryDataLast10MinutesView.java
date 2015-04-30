package com.sensirion.smartgadget.persistence.history_database.table;

import android.support.annotation.NonNull;

import com.sensirion.smartgadget.utils.Interval;

public class HistoryDataLast10MinutesView extends AbstractHistoryDataView {

    static final String VIEW_NAME = "history_data_last_10_min";
    private static final byte BIN_SIZE = 1;

    private static HistoryDataLast10MinutesView mInstance;

    private HistoryDataLast10MinutesView() {
        super(VIEW_NAME);
    }

    public synchronized static HistoryDataLast10MinutesView getInstance() {
        if (mInstance == null) {
            mInstance = new HistoryDataLast10MinutesView();
        }
        return mInstance;
    }

    @Override
    @NonNull
    public String createSqlStatement() {
        /*
        CREATE VIEW IF NOT EXISTS history_data_last_10_min
        AS SELECT device_mac, timestamp, temperature, humidity
        FROM history_data
        WHERE bin_size = 1;
        */
        return String.format("CREATE VIEW IF NOT EXISTS %s AS SELECT %s, %s, %s, %s FROM %s WHERE %s = %d;"
                , getName(), HistoryDataTable.COLUMN_DEVICE_ADDRESS, HistoryDataTable.COLUMN_TIMESTAMP, HistoryDataTable.COLUMN_TEMPERATURE, HistoryDataTable.COLUMN_HUMIDITY,
                HistoryDataTable.TABLE_NAME, HistoryDataTable.COLUMN_BIN_SIZE, BIN_SIZE);
    }

    @Override
    public int getNumberMilliseconds() {
        return Interval.TEN_MINUTES.getNumberMilliseconds();
    }

    @Override
    public int getResolution() {
        return HistoryDataTable.RESOLUTION_TEN_MINUTES_MS;
    }
}