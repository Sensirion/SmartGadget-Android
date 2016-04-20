package com.sensirion.smartgadget.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.sensirion.smartgadget.R;

public class Settings {
    private static final String TAG = Settings.class.getSimpleName();
    private static final String PREFIX = Settings.class.getName();
    private static final String KEY_SELECTED_SENSOR = PREFIX + ".KEY_SELECTED_SENSOR";

    public static final String UNKNOWN_VALUE = "";
    public static final String SELECTED_NONE = PREFIX + ".SELECTED_NONE";

    private static Settings mInstance;
    private final SharedPreferences mPreferences;

    private Settings(@NonNull final Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void init(@NonNull final Context context) {
        if (mInstance == null) {
            mInstance = new Settings(context);
        }
    }

    public static Settings getInstance() {
        if (mInstance == null) {
            throw new IllegalStateException(String.format("%s: getInstance -> Has not been initialized with context yet.", TAG));
        }
        return mInstance;
    }

    @Nullable
    public String getSelectedAddress() {
        return mPreferences.getString(KEY_SELECTED_SENSOR, SELECTED_NONE);
    }

    @SuppressLint("CommitPrefEdits")
    public void setSelectedAddress(@Nullable final String deviceAddress) {
        mPreferences.edit()
                .putString(KEY_SELECTED_SENSOR, deviceAddress)
                .commit();
        Log.i(TAG, String.format("setSelectedAddress -> Address %s was selected.", deviceAddress));
    }

    public void unselectCurrentAddress() {
        setSelectedAddress(SELECTED_NONE);
    }

    /**
     * Checks if the user has select Fahrenheit as the temperature unit.
     *
     * @param context needed for obtaining the temperature preference.
     * @return <code>true</code> if the user has selected Fahrenheit as the temperature unit - <code>false</code> otherwise.
     */
    public boolean isTemperatureUnitFahrenheit(@NonNull final Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        final String selectedTemperatureUnit = prefs.getString(context.getString(R.string.key_pref_temp_unit), context.getString(R.string.pref_temp_unit_default));
        return selectedTemperatureUnit.equals(context.getString(R.string.unit_fahrenheit));
    }

    /**
     * Checks if the user has select Winter as the season.
     *
     * @param context needed for obtaining the season preference.
     * @return <code>true</code> if the user has selected Winter as the season - <code>false</code> otherwise.
     */
    public boolean isSeasonWinter(@NonNull final Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        final String selectedSeason = prefs.getString(context.getString(R.string.key_pref_season), context.getString(R.string.pref_season_default));
        return selectedSeason.equals(context.getString(R.string.label_season_winter));
    }
}