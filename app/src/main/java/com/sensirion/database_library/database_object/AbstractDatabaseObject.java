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