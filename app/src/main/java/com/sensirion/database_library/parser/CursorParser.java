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

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;


public abstract class CursorParser {

    private static final String TAG = CursorParser.class.getSimpleName();

    /**
     * Parse the elements of a cursor and it's close.
     *
     * @param c cursor that we want to extract the data. At the end of the reading it would be closed.
     * @return {@link com.sensirion.database_library.parser.QueryResult} with the cursor data - <code>null</code> if the cursor is empty.
     */
    public static QueryResult parseCursor(@Nullable final Cursor c) {
        try {
            if (c == null || !c.moveToFirst()) {
                return null;
            }
            final QueryResult queryResult = new QueryResult(c.getColumnNames());
            do {
                queryResult.addRow(obtainCursorRow(c));
            } while (c.moveToNext());
            return queryResult;

        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    @NonNull
    private static Object[] obtainCursorRow(@NonNull final Cursor c) {
        final int numberColumns = c.getColumnCount();
        final Object[] cursorRow = new Object[numberColumns];
        for (int i = 0; i < numberColumns; i++) {
            switch (c.getType(i)) {
                case Cursor.FIELD_TYPE_NULL:
                    cursorRow[i] = null;
                    break;
                case Cursor.FIELD_TYPE_INTEGER:
                    cursorRow[i] = c.getLong(i);
                    break;
                case Cursor.FIELD_TYPE_FLOAT:
                    cursorRow[i] = c.getDouble(i);
                    break;
                case Cursor.FIELD_TYPE_STRING:
                    cursorRow[i] = c.getString(i);
                    break;
                case Cursor.FIELD_TYPE_BLOB:
                    cursorRow[i] = c.getBlob(i);
                    break;
                default:
                    throw new IllegalStateException(String.format("%s: obtainCursorRow -> Cursor type %s cannot be parsed.", TAG, c.getType(i)));
            }
        }
        return cursorRow;
    }
}
