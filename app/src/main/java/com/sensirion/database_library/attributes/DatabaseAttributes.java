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
