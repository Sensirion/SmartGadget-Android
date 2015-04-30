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