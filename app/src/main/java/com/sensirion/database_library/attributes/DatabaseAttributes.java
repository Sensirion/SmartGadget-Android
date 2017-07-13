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
package com.sensirion.database_library.attributes;

import android.support.annotation.NonNull;

import com.sensirion.database_library.database_object.AbstractDatabaseObject;

import java.util.Queue;

public class DatabaseAttributes {

    private static final String TAG = DatabaseAttributes.class.getSimpleName();

    @NonNull
    private final String mDatabaseName;
    private final int mDatabaseVersion;
    @NonNull
    private final Queue<AbstractDatabaseObject> mDatabaseTables;
    private final boolean mAutoCommit;

    public DatabaseAttributes(@NonNull final String databaseName, final int databaseVersionNumber, @NonNull final Queue<AbstractDatabaseObject> databaseTables, final boolean autoCommit) {
        if (databaseTables.isEmpty()) {
            throw new IllegalArgumentException(String.format("%s: Constructor -> A database should have tables in order to be created.", TAG));
        }
        if (databaseVersionNumber <= 0) {
            throw new IllegalArgumentException(String.format("%s: Constructor -> The database version number must be positive.", TAG));
        }
        mDatabaseName = databaseName;
        mDatabaseVersion = databaseVersionNumber;
        mDatabaseTables = databaseTables;
        mAutoCommit = autoCommit;
    }

    /**
     * Returns the database name.
     *
     * @return {@link java.lang.String} with the database name.
     */
    @NonNull
    public String getDatabaseName() {
        return mDatabaseName;
    }

    /**
     * Returns the database version.
     *
     * @return <code>int</code> with the database version.
     */
    public int getDatabaseVersion() {
        return mDatabaseVersion;
    }

    /**
     * Returns a list with the database user objects (Views and tables).
     *
     * @return {@link java.util.Queue} with the {@link com.sensirion.database_library.database_object.AbstractDatabaseObject}.
     */
    @NonNull
    public Queue<AbstractDatabaseObject> getDatabaseObjects() {
        return mDatabaseTables;
    }

    /**
     * Returns if the database autocommit is enabled.
     *
     * @return <code>true</code> if it's enabled. <code>false</code> otherwise.
     */
    public boolean getAutocommit() {
        return mAutoCommit;
    }
}
