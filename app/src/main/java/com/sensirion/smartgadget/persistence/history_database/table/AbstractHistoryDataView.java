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

import com.sensirion.database_library.database_object.AbstractDatabaseObject;
import com.sensirion.database_library.database_object.DatabaseObjectType;

import java.util.Arrays;
import java.util.List;

public abstract class AbstractHistoryDataView extends AbstractDatabaseObject {

    AbstractHistoryDataView(final String viewName) {
        super(viewName, DatabaseObjectType.VIEW);
    }

    /**
     * Obtains the SQL needed for obtaining the historical data of the selected view.
     *
     * @param deviceMac of he device we want to retrieve.
     * @return {@link java.lang.String} with the SQL sentence.
     */
    @SuppressWarnings("unused")
    public String getHistoryDataSql(final String deviceMac) {
        return getHistoryDataSql(Arrays.asList(new String[]{deviceMac}));
    }

    /**
     * Obtains the SQL needed for obtaining the historical data of the selected view.
     *
     * @param listOfDevices with the list of devices needed for retrieving the historical data.
     * @return {@link java.lang.String} with the SQL sentence.
     */
    public String getHistoryDataSql(@NonNull final List<String> listOfDevices) {
        final StringBuilder listOfDevicesSql = new StringBuilder();
        listOfDevicesSql.append('(');
        for (int i = listOfDevices.size() - 1; i >= 0; i--) {
            listOfDevicesSql.append(convertToSqlString(listOfDevices.get(i)));
            if (i > 0) {
                listOfDevicesSql.append(", ");
            }
        }
        listOfDevicesSql.append(")");
        return String.format("SELECT * FROM %s WHERE %s IN %s;", getName(), HistoryDataTable.COLUMN_DEVICE_ADDRESS, listOfDevicesSql);
    }

    /**
     * Obtains the SQL needed for obtaining the addresses of the devices available in the selected interval.
     *
     * @return {@link java.lang.String} with the SQL sentence.
     */
    public String getListOfDevicesSql() {
        return String.format("SELECT DISTINCT %s FROM %s;", HistoryDataTable.COLUMN_DEVICE_ADDRESS, getName());
    }

    /**
     * Obtains the maximum number of milliseconds of a view.
     *
     * @return <code>int</code> with the number of milliseconds.
     */
    public abstract int getNumberMilliseconds();

    /**
     * Returns the view resolution.
     *
     * @return <code>int</code> with the view resolution in milliseconds.
     */
    @SuppressWarnings("unused")
    public abstract int getResolution();
}
