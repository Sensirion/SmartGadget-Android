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
import android.util.Log;

import com.sensirion.database_library.database_object.AbstractDatabaseObject;
import com.sensirion.database_library.database_object.DatabaseObjectType;
import com.sensirion.smartgadget.utils.Interval;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;

/**
 * This table controls the historical data of the device.
 */
public class HistoryDataTable extends AbstractDatabaseObject {

    public static final String COLUMN_DEVICE_ADDRESS = "device_address";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_TEMPERATURE = "temperature";
    public static final String COLUMN_HUMIDITY = "humidity";
    public static final String COLUMN_COMES_FROM_LOG = "comes_from_log";
    public static final String COLUMN_BIN_SIZE = "bin_size";

    static final String TABLE_NAME = "history_data";

    static final int RESOLUTION_TEN_MINUTES_MS = Interval.ONE_SECOND.getNumberMilliseconds() * 10;
    static final int RESOLUTION_ONE_HOUR_MS = RESOLUTION_TEN_MINUTES_MS * 6;
    static final int RESOLUTION_SIX_HOURS_MS = RESOLUTION_ONE_HOUR_MS * 6;
    static final int RESOLUTION_ONE_DAY_MS = RESOLUTION_ONE_HOUR_MS * 24;
    static final int RESOLUTION_ONE_WEEK_MS = RESOLUTION_ONE_DAY_MS * 7;

    private static HistoryDataTable mInstance;

    private HistoryDataTable() {
        super(TABLE_NAME, DatabaseObjectType.TABLE);
    }

    public synchronized static HistoryDataTable getInstance() {
        if (mInstance == null) {
            mInstance = new HistoryDataTable();
        }
        return mInstance;
    }

    @NonNull
    @Override
    public String createSqlStatement() {
        return "CREATE TABLE IF NOT EXISTS " + getName() + " ("
                + COLUMN_DEVICE_ADDRESS + " VARCHAR NOT NULL, "
                + COLUMN_TIMESTAMP + " INTEGER NOT NULL, "
                + COLUMN_TEMPERATURE + " FLOAT NOT NULL, "
                + COLUMN_HUMIDITY + " FLOAT NOT NULL, "
                + COLUMN_COMES_FROM_LOG + " TINYINT NOT NULL, "
                + COLUMN_BIN_SIZE + " TINYINT DEFAULT 1"
                + ");";
    }

    /**
     * Obtains the timestamp of the last downloaded data of a device.
     *
     * @param deviceAddress of the device we want to obtain the timestamp from.
     * @return {@link java.lang.String} with the SQL sentence.
     */
    @SuppressWarnings("unused")
    public String obtainLastLoggedValueTimestampSql(final String deviceAddress) {
        return String.format("SELECT MAX(%s) FROM %s WHERE %s = %s AND %s = %d", COLUMN_TIMESTAMP, getName(),
                COLUMN_DEVICE_ADDRESS, convertToSqlString(deviceAddress), COLUMN_COMES_FROM_LOG, TRUE);
    }

    /**
     * Inserts a value inside the history list.
     *
     * @param comesFromLog <code>true</code> if the incoming value comes from logging - <code>false</code> otherwise.
     * @return {@link java.lang.String} with the SQL sentence.
     */
    public String insertValueSql(final String deviceAddress, final long timestamp, final float temperature, final float humidity, final boolean comesFromLog) {
        return String.format(Locale.ENGLISH, "INSERT INTO %s (%s, %s, %s, %s, %s) VALUES (%s, %s, %s, %s, %d)", getName()
                , COLUMN_DEVICE_ADDRESS, COLUMN_TIMESTAMP, COLUMN_TEMPERATURE, COLUMN_HUMIDITY, COLUMN_COMES_FROM_LOG,
                convertToSqlString(deviceAddress), convertToSqlInteger(timestamp), convertToSqlFloat(temperature), convertToSqlFloat(humidity), convertBooleanSqlite(comesFromLog));
    }

 /*
     STORAGE SCALES:

    SCALE    |     RESOLUTION    |     BIN SIZE
    ----------------------------------------------------
	10 min	 |    10 seconds     |        1
    ----------------------------------------------------
	1 hour	 |    60 seconds     |        2
    ----------------------------------------------------
	6 hours	 |    6  minutes     |        3
    ----------------------------------------------------
	1 day	 |    24 minutes     |	      4
    ----------------------------------------------------
	1 Week	 |   168 minutes     |	      5
    ----------------------------------------------------
*/

    /**
     * Purges the database from the data that the application is not going to use anymore.
     *
     * @return {@link java.util.List <{java.lang.String}>} with the SQL sentences needed for the purge.+
     */
    @NonNull
    public List<String> purgeOldDataSql() {

        final long initialTime = System.currentTimeMillis();
        final List<String> purgeOldDataSql = new LinkedList<>();

        purgeOldDataSql.add(deleteRecordsOlderThan(Interval.ONE_WEEK.getNumberMilliseconds()));
        purgeOldDataSql.addAll(updateBin(Interval.TEN_MINUTES.getNumberMilliseconds(), RESOLUTION_TEN_MINUTES_MS, 1));
        purgeOldDataSql.addAll(updateBin(Interval.ONE_HOUR.getNumberMilliseconds(), RESOLUTION_ONE_HOUR_MS, 2));
        purgeOldDataSql.addAll(updateBin(Interval.SIX_HOURS.getNumberMilliseconds(), RESOLUTION_SIX_HOURS_MS, 3));
        purgeOldDataSql.addAll(updateBin(Interval.ONE_DAY.getNumberMilliseconds(), RESOLUTION_ONE_DAY_MS, 4));

        Log.d(TAG, String.format("purgeOldDataSql -> has %d sentences.", purgeOldDataSql.size()));

        for (final String purgeSqlSentence : purgeOldDataSql) {
            Log.d(TAG, String.format("purgeOldDataSql -> The following sentence was created: %s", purgeSqlSentence));
        }

        Log.d(TAG, String.format("purgeOldDataSql -> Creation of the sentence last for %d milliseconds", (System.currentTimeMillis() - initialTime)));

        return purgeOldDataSql;
    }

    @NonNull
    private Queue<String> updateBin(final int numSecondsScale, final int numSecondsResolution, final int initialBinSize) {
        final Queue<String> updateBeanSql = new LinkedList<>();

        final String beanParserSql = String.format("INSERT INTO %s (%s, %s, %s, %s, %s, %s)",
                getName(), COLUMN_DEVICE_ADDRESS, COLUMN_TIMESTAMP, COLUMN_TEMPERATURE, COLUMN_HUMIDITY, COLUMN_COMES_FROM_LOG, COLUMN_BIN_SIZE)
                + String.format(" SELECT hd.%s, ROUND(AVG(hd.%s)), AVG(hd.%s), AVG(hd.%s), hd.%s, %s ",
                COLUMN_DEVICE_ADDRESS, COLUMN_TIMESTAMP, COLUMN_TEMPERATURE, COLUMN_HUMIDITY, COLUMN_COMES_FROM_LOG, (initialBinSize + 1))
                + String.format(" FROM %s hd", getName())
                + String.format(" WHERE hd.%s = %d AND %s - hd.%s > %d",
                COLUMN_BIN_SIZE, initialBinSize, System.currentTimeMillis(), COLUMN_TIMESTAMP, numSecondsScale)
                + String.format(" GROUP BY %s, %s, ((%s - %s)/%d);", COLUMN_DEVICE_ADDRESS, COLUMN_COMES_FROM_LOG, System.currentTimeMillis(),
                COLUMN_TIMESTAMP, numSecondsResolution);

        updateBeanSql.add(beanParserSql);

        updateBeanSql.add(String.format("DELETE FROM %s WHERE %s = %d AND %s - %s > %s;",
                getName(), COLUMN_BIN_SIZE, initialBinSize, System.currentTimeMillis(), COLUMN_TIMESTAMP, numSecondsScale));

        return updateBeanSql;
    }

    private String deleteRecordsOlderThan(final int numberOfMilliseconds) {
        return String.format("DELETE FROM %s WHERE %s - %s > %s;",
                TABLE_NAME, System.currentTimeMillis(), COLUMN_TIMESTAMP, convertToSqlInteger(numberOfMilliseconds));
    }
}
