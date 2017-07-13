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
package com.sensirion.database_library.parser;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.LinkedList;
import java.util.List;

public class QueryResult {

    private static final String TAG = QueryResult.class.getSimpleName();

    @NonNull
    private final String[] mColumnNames;
    private final List<QueryResultRow> mResultRows = new LinkedList<>();

    public QueryResult(@NonNull final String[] columnNames) {
        mColumnNames = columnNames;
    }

    /**
     * Gets the number of columns that have each row.
     *
     * @return <code>int</code> with the number of columns.
     */
    public int getNumberOfColumns() {
        return mColumnNames.length;
    }

    /**
     * Gets the number of generated rows.
     *
     * @return <code>int</code> with the number of rows.
     */
    @SuppressWarnings("unused")
    public int getNumberOfRows() {
        return mResultRows.size();
    }

    /**
     * Obtains the column name of a column.
     *
     * @param index with the column index.
     * @return {@link java.lang.String} with the column name.
     */
    @SuppressWarnings("unused")
    public String getColumnName(final int index) {
        if (index < 0 || index >= getNumberOfColumns()) {
            throw new IllegalArgumentException(String.format("%s: getColumnName -> The index %d is out of bounds because the result haves %d columns.", TAG, index, mColumnNames.length));
        }
        return mColumnNames[index];
    }

    /**
     * Returns the names of the result columns.
     *
     * @return array of {@link java.lang.String} with the column names.
     */
    @NonNull
    @SuppressWarnings("unused")
    public String[] getColumnsNames() {
        return mColumnNames;
    }

    /**
     * Gets the column index of a column.
     *
     * @param columnName of the column we are looking for.
     * @return {@link java.lang.Integer with the column index} - <code>null</code> in case the column index was not found.
     */
    @Nullable
    public Integer getIndexColumn(@NonNull final String columnName) {
        for (int i = 0; i < mColumnNames.length; i++) {
            if (mColumnNames[i].equals(columnName)) {
                return i;
            }
        }
        return null;
    }

    /**
     * Adds a row to the result.
     *
     * @param cursorRowElements with the cursor elements.
     */
    public void addRow(@NonNull final Object[] cursorRowElements) {
        mResultRows.add(new QueryResultRow(this, cursorRowElements));
    }

    /**
     * Obtains the first result of the query.
     *
     * @return {@link QueryResultRow} with the first result.
     */
    public QueryResultRow getFirstQueryResult() {
        return mResultRows.get(0);
    }

    /**
     * Obtains the result of the query.
     *
     * @return {@link java.util.List} of {@link QueryResultRow} with the query results.
     */
    @NonNull
    public List<QueryResultRow> getQueryResults() {
        return mResultRows;
    }
}
