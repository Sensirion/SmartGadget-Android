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
package com.sensirion.database_library;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.sensirion.database_library.attributes.DatabaseAttributes;
import com.sensirion.database_library.database_object.AbstractDatabaseObject;
import com.sensirion.database_library.parser.CursorParser;
import com.sensirion.database_library.parser.QueryResult;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class DatabaseFacade {

    private static final String TAG = DatabaseFacade.class.getSimpleName();

    @NonNull
    private final DatabaseAttributes mDatabaseAttributes;
    private final Semaphore mWritingPermits = new Semaphore(1, true);
    private InternalSQLiteOpenHelper mDatabase;
    private boolean mIsAutocommitEnabled;

    /**
     * This constructor has to be called in order to use the library.
     *
     * @param context            needed for opening the database. Cannot be <code>null</code>
     * @param databaseAttributes wrapper class with the attributes needed in order to create the database.
     */
    public DatabaseFacade(@NonNull final Context context, @NonNull final DatabaseAttributes databaseAttributes) {
        mDatabaseAttributes = databaseAttributes;
        mDatabase = new InternalSQLiteOpenHelper(context, mDatabaseAttributes);
        setAutoCommit(databaseAttributes.getAutocommit());
    }

    /**
     * Change the parameter that controls the autocommit after each query.
     *
     * @param autoCommit autocommit in case of <code>true</code>, manual commit in case it's <code>false</code>.
     */
    public void setAutoCommit(final boolean autoCommit) {
        mIsAutocommitEnabled = autoCommit;
    }

    /**
     * Checks if the database is connected.
     *
     * @return <code>true</code> if database is connected, <code>false</code> otherwise.
     */
    public boolean isDatabaseOpenForWrite() {
        return mDatabase.getWritableDatabase().isOpen();
    }

    /**
     * In case the database connection is close it opens it.
     */
    public void openClosedDatabaseConnection() {
        mDatabase.getWritableDatabase();
    }

    /**
     * Constructs and executes a database query.
     *
     * @param table         Name of the queried table. Cannot be <code>null</code>
     * @param columns       List of columns to return. Passing <code>null</code>  will return all columns.
     * @param selection     Where-clause, i.e. filter for the selection of data, <code>null</code>  will select all data.
     * @param selectionArgs You may include ?s in the "whereClause"". These placeholders will get replaced by the values from the selectionArgs array.
     * @param groupBy       A filter declaring how to group rows, <code>null</code> will cause the rows to not be grouped.
     * @param having        Filter for the groups, <code>null</code> means no filter.
     * @param orderBy       Table columns which will be used to order the data, <code>null</code> means no ordering.
     * @param limit         Limits the result of the query.
     * @return {@link  com.sensirion.database_library.parser.QueryResult} with the query result.
     */
    @Nullable
    public QueryResult query(@NonNull final String table, @Nullable final String[] columns, @Nullable final String selection,
                             @Nullable final String[] selectionArgs, @Nullable final String groupBy, @Nullable final String having,
                             @Nullable final String orderBy, @Nullable final String limit) {
        return CursorParser.parseCursor(mDatabase.getReadableDatabase().query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit));
    }

    /**
     * Executes an standard rawDatabaseQuery with no attributes.
     *
     * @param sql sentence.
     * @return {@link com.sensirion.database_library.parser.QueryResult} with the query result.
     */
    @Nullable
    public QueryResult rawDatabaseQuery(@NonNull final String sql) {
        return rawDatabaseQuery(sql, null);
    }

    /**
     * Executes an standard rawDatabaseQuery.
     *
     * @param sql        - SQL Sentence. All 'dynamic' values should be '?'.
     * @param attributes - SQL attributes, we need one per '?' in SQL sentence.
     * @return {@link  com.sensirion.database_library.parser.QueryResult} with the query result.
     */
    @Nullable
    public QueryResult rawDatabaseQuery(@NonNull final String sql, @Nullable final String[] attributes) {
        if (attributes == null) {
            Log.d(TAG, String.format("rawDatabaseQuery -> Database %s received the following SQL: %s", getDatabaseName(), sql));
        } else {
            Log.d(TAG, String.format("rawDatabaseQuery -> Database %s received the following SQL: %s with the following attributes: %s", getDatabaseName(), sql, Arrays.toString(attributes)));
        }
        final SQLiteDatabase db = mDatabase.getWritableDatabase();

        if (!db.inTransaction()) {
            beginTransaction(db);
        }

        Cursor c = null;
        try {
            c = db.rawQuery(sql, attributes);
            if (c == null) {
                return null;
            }
            return CursorParser.parseCursor(c);
        } catch (@NonNull final SQLiteException e) {
            Log.e(TAG, String.format("rawDatabaseQuery -> The following error was produced when looking into the database with the following sentence: %s  -> ", sql), e);
        } finally {
            if (mIsAutocommitEnabled) {
                commit(db);
            }
            if (c != null) {
                c.close();
            }
        }
        return null;
    }


    /**
     * Executes a database query.
     *
     * @param sql for execute.
     */
    public void executeSQL(@NonNull final String sql) {
        final List<String> listWithSQLSentence = new LinkedList<>();
        listWithSQLSentence.add(sql);
        executeSQL(listWithSQLSentence);
    }

    /**
     * Executes a database query for each statement in the list.
     *
     * @param sqlStatements that the user wants to execute.
     */
    public void executeSQL(@NonNull final List<String> sqlStatements) {
        final SQLiteDatabase db = mDatabase.getWritableDatabase();
        if (!db.inTransaction()) {
            beginTransaction(db);
        }
        try {
            for (final String sql : sqlStatements) {
                Log.d(TAG, String.format("executeSQL -> The database received the following SQL sentence: %s", sql));
                db.execSQL(sql);
            }
        } catch (@NonNull final SQLiteException e) {
            Log.e(TAG, "executeSQL -> The following exception was thrown -> ", e);
            rollbackTransaction();
        }
        if (mIsAutocommitEnabled) {
            commit(db);
        }
    }

    /**
     * Close the database connection.
     */
    public void closeDatabaseConnection() {
        mDatabase.close();
    }

    /**
     * Begins a transaction.
     */
    @SuppressWarnings("unused")
    public void beginTransaction() {
        beginTransaction(mDatabase.getWritableDatabase());
    }

    /**
     * Begins a transaction.
     *
     * @param db {@link android.database.sqlite.SQLiteDatabase} with of the database that wants the transaction.
     */
    public void beginTransaction(@NonNull final SQLiteDatabase db) {
        askWritingPermit();
        db.beginTransaction();
    }

    private void askWritingPermit() {
        while (true) {
            try {
                mWritingPermits.acquire(1);
                break;
            } catch (@NonNull final InterruptedException e) {
                Log.w(TAG, "askWritingPermit -> The following InterruptedException was thrown -> ", e);
            }
        }
    }


    /**
     * Begins a non exclusive transaction in the database.
     */
    @SuppressWarnings("unused")
    public void beginTransactionNonExclusive() {
        beginTransactionNonExclusive(mDatabase.getWritableDatabase());
    }

    /**
     * Begins a non exclusive transaction in the database.
     *
     * @param db {@link android.database.sqlite.SQLiteDatabase} with of the database that wants the transaction.
     */
    public void beginTransactionNonExclusive(@NonNull final SQLiteDatabase db) {
        askWritingPermit();
        db.beginTransactionNonExclusive();
    }

    /**
     * Commits the last transaction.
     */
    public void commit() {
        finishTransaction(true);
    }

    /**
     * Commits the last transaction.
     *
     * @param db {@link android.database.sqlite.SQLiteDatabase} with of the database that wants to close the transaction.
     */
    public void commit(@NonNull final SQLiteDatabase db) {
        finishTransaction(db, true);
    }


    /**
     * Rollbacks the last transaction.
     */
    public void rollbackTransaction() {
        finishTransaction(false);
    }


    /**
     * Rollbacks the last transaction.
     *
     * @param db database that is going to rollback the last transaction.
     */
    @SuppressWarnings("unused")
    public void rollbackTransaction(@NonNull final SQLiteDatabase db) {
        finishTransaction(db, false);
    }

    /**
     * Ends a transaction in progress.
     *
     * @param transactionSuccessful <code>true</code> if it's a commit - <code>false</code> if it's a rollback.
     */
    private void finishTransaction(final boolean transactionSuccessful) {
        finishTransaction(mDatabase.getWritableDatabase(), transactionSuccessful);
    }

    /**
     * Ends a transaction in progress.
     *
     * @param db                    that wants to close the transaction.
     * @param transactionSuccessful <code>true</code> if it's a commit - <code>false</code> if it's a rollback.
     */
    private void finishTransaction(@NonNull final SQLiteDatabase db, final boolean transactionSuccessful) {
        if (db.inTransaction()) {
            if (transactionSuccessful) {
                db.setTransactionSuccessful();
            }
            db.endTransaction();
        }
        mWritingPermits.release(1);
    }

    /**
     * Gets the last inserted id of a table
     *
     * @param table that wants to be checked for the last selected ID.
     * @return {@link java.lang.Integer} with the last inserted id.
     */
    @Nullable
    @SuppressWarnings("unused")
    public Long getLastId(@NonNull final AbstractDatabaseObject table) {
        final QueryResult queryResult = rawDatabaseQuery(table.getLastIdSql());
        if (queryResult == null) {
            return null;
        }
        return queryResult.getFirstQueryResult().getLong(0);
    }

    /**
     * Gets the database name.
     *
     * @return {@link java.lang.String} with the database name.
     */
    public String getDatabaseName() {
        return mDatabase.getDatabaseName();
    }

    /**
     * Deletes all the data from the database.
     */
    @SuppressWarnings("unused")
    public void deleteAllDatabaseData(@NonNull final Context context) {
        askWritingPermit();
        mDatabase.getWritableDatabase().close();
        context.deleteDatabase(mDatabaseAttributes.getDatabaseName());
        mDatabase = new InternalSQLiteOpenHelper(context, mDatabaseAttributes);
        setAutoCommit(mDatabaseAttributes.getAutocommit());
        mWritingPermits.release(1);
    }
}
