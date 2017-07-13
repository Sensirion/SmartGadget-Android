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
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.sensirion.database_library.attributes.DatabaseAttributes;
import com.sensirion.database_library.database_object.AbstractDatabaseObject;

import java.util.Queue;

class InternalSQLiteOpenHelper extends SQLiteOpenHelper {

    private static final String TAG = InternalSQLiteOpenHelper.class.getSimpleName();

    @NonNull
    private final Queue<AbstractDatabaseObject> mDatabaseObjectList;

    /**
     * Friendly constructor. Should only be called by DatabaseFacade.
     *
     * @param context            needed for opening the databases.
     * @param databaseAttributes wrapper class with the attributes needed in order to create the database.
     */
    InternalSQLiteOpenHelper(@NonNull final Context context, @NonNull final DatabaseAttributes databaseAttributes) {
        super(context.getApplicationContext(), databaseAttributes.getDatabaseName(), null, databaseAttributes.getDatabaseVersion());
        mDatabaseObjectList = databaseAttributes.getDatabaseObjects();
        getWritableDatabase();
    }

    /**
     * It creates a database using the tables given by the constructor.
     *
     * @param db: The database we want to create.
     */
    @Override
    public void onCreate(@NonNull final SQLiteDatabase db) {
        Log.i(TAG, "Database: onCreate()");
        for (final AbstractDatabaseObject table : mDatabaseObjectList) {
            createOrReplaceTableDatabase(db, table);
            Log.i(TAG, String.format("onCreate -> Database: Table %s has been created.", table.getName()));
        }
        Log.i(TAG, String.format("Database with name %s has been created using the version %s.", super.getDatabaseName(), db.getVersion()));
    }

    void enableForeignKeys(@NonNull final SQLiteDatabase db) {
        db.beginTransaction();
        db.execSQL("PRAGMA foreign_keys=ON");
        db.endTransaction();

        Cursor cursor = null;
        try {
            cursor = db.rawQuery("PRAGMA foreign_keys;", null);
            cursor.moveToFirst();
            Log.i(TAG, String.format("Database: enableForeignKeys -> Foreign keys are %s", (cursor.getInt(0) == 1) ? "enabled" : "disabled"));
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Override
    public void onUpgrade(@NonNull final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        Log.i(TAG, "onUpgrade()");
        for (AbstractDatabaseObject table : mDatabaseObjectList) {
            createOrReplaceTableDatabase(db, table);
            Log.i(TAG, String.format("onUpgrade() -> Database: %s %s has been upgraded to the version %d.", table.getType(), table.getName(), newVersion));
        }
        Log.i(TAG, String.format("onUpgrade -> Database has been upgraded from version %d to version %d.", oldVersion, newVersion));
        onCreate(db);
    }

    @Override
    public void onDowngrade(@NonNull final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        Log.i(TAG, "onDowngrade()");
        for (final AbstractDatabaseObject table : mDatabaseObjectList) {
            createOrReplaceTableDatabase(db, table);
            Log.i(TAG, String.format("onDowngrade() -> Database: %s %s has been downgrade to the version %d.", table.getType(), table.getName(), oldVersion));
        }
        Log.i(TAG, String.format("onDowngrade -> Database has been downgraded from version %d to version %d.", newVersion, oldVersion));
        onCreate(db);
    }

    /**
     * Creates or replaces a table in the database.
     *
     * @param databaseObject Table we want in the database.
     */
    private void createOrReplaceTableDatabase(@NonNull final SQLiteDatabase db, @NonNull final AbstractDatabaseObject databaseObject) {
        db.execSQL(String.format("DROP %s IF EXISTS %s;", databaseObject.getType(), databaseObject.getName()));
        Log.d(TAG, String.format("Replaced %s %s for a new version.", databaseObject.getType(), databaseObject.getName()));
        db.execSQL(databaseObject.createSqlStatement());
    }

    /**
     * It configures the database for using the foreign keys constrains.
     *
     * @param db that want to support foreign keys.
     */
    @Override
    public void onConfigure(@NonNull final SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
        enableForeignKeys(db);
    }
}
