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
package com.sensirion.smartgadget.persistence.device_name_database.table;

import android.support.annotation.NonNull;

import com.sensirion.database_library.database_object.AbstractDatabaseObject;
import com.sensirion.database_library.database_object.DatabaseObjectType;

/**
 * This table store the names given to the user to his/her devices.
 */
public class DeviceNameTable extends AbstractDatabaseObject {

    public static final String COLUMN_USER_DEVICE_NAME = "name";
    private static final String COLUMN_DEVICE_MAC = "mac_address";
    private static final String TABLE_NAME = "device_names";
    private static final int LENGTH_MAC_ADDRESS = 17; // AA:BB:CC:DD:EE:FF --> AA:(3) BB:(6) CC:(9) DD:(12) EE:(15) FF(17)

    private static DeviceNameTable mInstance;

    private DeviceNameTable() {
        super(TABLE_NAME, DatabaseObjectType.TABLE);
    }

    @NonNull
    public synchronized static DeviceNameTable getInstance() {
        if (mInstance == null) {
            mInstance = new DeviceNameTable();
        }
        return mInstance;
    }

    /**
     * SQL sentence for retrieving a device name from the database.
     *
     * @param deviceAddress of the device we want to know the name.
     * @return {@link java.lang.String} with the SQL sentence for looking for the name of the device.
     */
    @NonNull
    public String readUserDeviceNameSql(@NonNull final String deviceAddress) {
        final String[] projectionIn = new String[]{COLUMN_USER_DEVICE_NAME};
        final String selection = COLUMN_DEVICE_MAC + " = '" + deviceAddress + "'";
        return mQueryBuilder.buildQuery(projectionIn, selection, null, null, null, null);
    }

    /**
     * SQL sentence for inserting a device name in the database.
     *
     * @param deviceAddress of the device we want to insert in the database.
     * @param deviceName    that the user has given to the device.
     * @return {@link java.lang.String} with the SQL sentence for inserting the name of the device.
     */
    @NonNull
    public String insertUserDeviceNameSql(@NonNull final String deviceAddress, @NonNull final String deviceName) {
        return String.format("INSERT INTO %s (%s, %s) VALUES (%s, %s);",
                getName(), COLUMN_DEVICE_MAC, COLUMN_USER_DEVICE_NAME, convertToSqlString(deviceAddress), convertToSqlString(deviceName));
    }

    /**
     * SQL sentence for update a device name in the database.
     *
     * @param deviceAddress of the device we want to update in the database.
     * @param deviceName    that the user has given to the device.
     * @return {@link java.lang.String} with the SQL sentence for updating the name of the device.
     */
    @NonNull
    public String updateDeviceNameSql(@NonNull final String deviceAddress, @NonNull final String deviceName) {
        return String.format("UPDATE %s SET %s = %s WHERE %s = %s;",
                getName(), COLUMN_USER_DEVICE_NAME, convertToSqlString(deviceName), COLUMN_DEVICE_MAC, convertToSqlString(deviceAddress));
    }

    /**
     * SQL sentence for deleting a device from the database.
     *
     * @param deviceAddress of the device we want to delete from the database.
     * @return {@link java.lang.String} with the SQL sentence of the device we want to delete from the database.
     */
    @NonNull
    public String deleteDeviceNameSql(@NonNull final String deviceAddress) {
        return String.format("DELETE FROM %s WHERE %s = %s;", getName(), COLUMN_DEVICE_MAC, convertToSqlString(deviceAddress));
    }

    @NonNull
    @Override
    public String createSqlStatement() {
        return "CREATE TABLE " + getName() + " ("
                + COLUMN_DEVICE_MAC + " CHARACTER(" + LENGTH_MAC_ADDRESS + ") PRIMARY KEY, "
                + COLUMN_USER_DEVICE_NAME + " VARCHAR);";
    }
}
