/*
 * Copyright (c) 2017, Sensirion AG
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of Sensirion AG nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
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
