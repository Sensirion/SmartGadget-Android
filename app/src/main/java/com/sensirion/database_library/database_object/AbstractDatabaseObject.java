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
package com.sensirion.database_library.database_object;

import android.database.sqlite.SQLiteQueryBuilder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.text.NumberFormat;
import java.util.Locale;

public abstract class AbstractDatabaseObject {

    protected static final String COLUMN_ID = "_id";
    protected static final String NULL = "NULL";
    protected static final int FALSE = 0;
    protected static final int TRUE = 1;
    protected final String TAG = this.getClass().getSimpleName();
    @NonNull
    protected final SQLiteQueryBuilder mQueryBuilder;
    @NonNull
    private final String mTableName;
    @NonNull
    private final DatabaseObjectType mObjectType;

    protected AbstractDatabaseObject(@NonNull final String tableName, @NonNull final DatabaseObjectType objectType) {
        mTableName = tableName;
        mObjectType = objectType;
        mQueryBuilder = new SQLiteQueryBuilder();
        mQueryBuilder.setTables(mTableName);
    }

    /**
     * Converts a boolean to the SQL equivalent
     *
     * @param value that wants to be converted.
     * @return <code>int</code> with the boolean conversion.
     */
    protected static int convertBooleanSqlite(final boolean value) {
        return value ? TRUE : FALSE;
    }

    /**
     * Converts an object to a string that can be inserted inside an SQL sentence.
     *
     * @param value that wants to be inserted in a sql sentence.
     * @return {@link java.lang.String} with a valid SQLString
     */
    @NonNull
    protected static String convertToSqlString(@Nullable final Object value) {
        if (value == null) {
            return NULL;
        }
        return String.format("'%s'", value);
    }

    /**
     * Converts an Integer to a SQL equivalent.
     *
     * @param value that wants to be converted.
     * @return {@link java.lang.String} with a valid SQLInteger
     */
    @NonNull
    protected static String convertToSqlInteger(@Nullable final Integer value) {
        if (value == null) {
            return NULL;
        }
        return convertToSqlInteger((long) value);
    }

    /**
     * Converts a Long to a SQL equivalent.
     *
     * @param value that wants to be converted.
     * @return {@link java.lang.String} with a valid SQLInteger
     */
    @NonNull
    protected static String convertToSqlInteger(@Nullable final Long value) {
        if (value == null) {
            return NULL;
        }
        return value.toString();
    }

    /**
     * Converts a Double to a valid SQL float String.
     *
     * @param value that wants to be converted in a SQLiteFloat
     * @return {@link java.lang.String} with a valid SQLString.
     */
    @NonNull
    protected static String convertToSqlFloat(@Nullable final Float value) {
        if (value == null) {
            return NULL;
        }
        return convertToSqlFloat((double) value);
    }

    /**
     * Converts a Double to a valid SQL float String.
     *
     * @param value that wants to be converted in a SQLiteFloat
     * @return {@link java.lang.String} with a valid SQLString.
     */
    @NonNull
    protected static String convertToSqlFloat(@Nullable final Double value) {
        if (value == null) {
            return NULL;
        }
        return NumberFormat.getNumberInstance(Locale.ENGLISH).format(value).replaceAll(",", "");
    }

    /**
     * Obtains the type of database object. (Table or view)
     *
     * @return {@link DatabaseObjectType} with the type.
     */
    @NonNull
    public DatabaseObjectType getType() {
        return mObjectType;
    }

    /**
     * Obtains the SQL sentence for obtaining the last id inserted.
     *
     * @return {@link java.lang.String} with the sql sentence for obtaining the last id.
     */
    @NonNull
    public String getLastIdSql() {
        return String.format(Locale.ENGLISH, "SELECT MAX (%s) FROM %s;", COLUMN_ID, getName());
    }

    /**
     * Gets the name of the table.
     *
     * @return {@link java.lang.String} with the table name.
     */
    @NonNull
    public String getName() {
        return mTableName;
    }

    /**
     * Gets the SQL sentence with the total number of rows in a table.
     *
     * @return {@link java.lang.String} with the query for looking the total number of rows in the table.
     */
    @NonNull
    public String getNumberRowsSql() {
        final String[] projectionIn = new String[]{"COUNT (*)"};
        return mQueryBuilder.buildQuery(projectionIn, null, null, null, null, null);
    }

    /**
     * Returns the SQL sentence for creating the table.
     *
     * @return {@link java.lang.String} with the create table sentence.
     */
    @NonNull
    public abstract String createSqlStatement();
}
