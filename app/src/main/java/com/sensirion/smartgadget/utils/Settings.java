package com.sensirion.smartgadget.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.sensirion.smartgadget.R;

public class Settings {

    private static final String TAG = Settings.class.getSimpleName();
    private static final String PREFIX = Settings.class.getName();
    public static final String KEY_SELECTED_SENSOR = PREFIX + ".SELECTED_SENSOR";
    public static final String KEY_SELECTED_TEMPERATURE_UNIT = PREFIX + ".SELECTED_TEMPERATURE_UNIT";
    public static final String KEY_SELECTED_SEASON = PREFIX + ".SELECTED_SEASON";
    public static final String KEY_SMART_GADGET_REQUIRED = PREFIX + ".SMART_GADGET_REQUIRED";

    public static final String SELECTED_NONE = PREFIX + ".SELECTED_NONE";

    private static Settings mInstance;
    private final SharedPreferences mPreferences;
    private final String mDefaultTemperatureUnitString;
    private final String mFahrenheitUnitString;
    private final String mDefaultSeasonString;
    private final String mWinterSeasonString;
    private final String mNoString;
    private final String mYesString;

    private Settings(@NonNull final Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mDefaultTemperatureUnitString = context.getString(R.string.pref_temp_unit_default);
        mFahrenheitUnitString = context.getString(R.string.unit_fahrenheit);
        mDefaultSeasonString = context.getString(R.string.pref_season_default);
        mWinterSeasonString = context.getString(R.string.label_season_winter);
        mNoString = context.getString(R.string.no);
        mYesString = context.getString(R.string.yes);
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

    @NonNull
    public String getSelectedAddress() {
        return mPreferences.getString(KEY_SELECTED_SENSOR, SELECTED_NONE);
    }

    @SuppressLint("CommitPrefEdits")
    public void setSelectedAddress(@NonNull final String deviceAddress) {
        mPreferences.edit().putString(KEY_SELECTED_SENSOR, deviceAddress).commit();
    }

    public void unselectCurrentAddress() {
        setSelectedAddress(SELECTED_NONE);
    }

    @NonNull
    public String getSelectedTemperatureUnit() {
        return mPreferences.getString(KEY_SELECTED_TEMPERATURE_UNIT, mDefaultTemperatureUnitString);
    }

    @SuppressLint("CommitPrefEdits")
    public void setSelectedTemperatureUnit(@NonNull final String unitString) {
        mPreferences.edit().putString(KEY_SELECTED_TEMPERATURE_UNIT, unitString).commit();
    }

    /**
     * Checks if the user has select Fahrenheit as the temperature unit.
     *
     * @return <code>true</code> if the user has selected Fahrenheit as the temperature unit - <code>false</code> otherwise.
     */
    public boolean isTemperatureUnitFahrenheit() {
        return getSelectedTemperatureUnit().equals(mFahrenheitUnitString);
    }

    @NonNull
    public String getSelectedSeason() {
        return mPreferences.getString(KEY_SELECTED_SEASON, mDefaultSeasonString);
    }

    @SuppressLint("CommitPrefEdits")
    public void setSelectedSeason(@NonNull final String seasonString) {
        mPreferences.edit().putString(KEY_SELECTED_SEASON, seasonString).commit();
    }

    /**
     * Checks if the user has select Winter as the season.
     *
     * @return <code>true</code> if the user has selected Winter as the season - <code>false</code> otherwise.
     */
    public boolean isSeasonWinter() {
        return getSelectedSeason().equals(mWinterSeasonString);
    }

    /**
     * Checks if a Smart Gadget Requirements dialog needs to be shown on App Start.
     *
     * @return <code>true</code> if the dialog is going to be displayed - <code>false</code> otherwise.
     */
    public boolean isSmartGadgetRequirementDisplayed() {
        final String isDisplayed = mPreferences.getString(KEY_SMART_GADGET_REQUIRED, mNoString);
        return isDisplayed.equals(mNoString);
    }

    /**
     * Sets if a Smart Gadget Requirements dialog needs to be displayed on App Start.
     *
     * @param isSmartGadgetRequirementDisplayed if the dialog is going to be displayed - <code>false</code> otherwise.
     */
    public void setSmartGadgetWarningDisplayed(final boolean isSmartGadgetRequirementDisplayed) {
        mPreferences.edit().putString(KEY_SMART_GADGET_REQUIRED,
                (isSmartGadgetRequirementDisplayed) ? mYesString : mNoString).apply();
    }

    /**
     * Registers a Shared preference listener.
     *
     * @param listener The {@link android.content.SharedPreferences.OnSharedPreferenceChangeListener} to register.
     */
    public void registerOnSharedPreferenceChangeListener(@NonNull final SharedPreferences.OnSharedPreferenceChangeListener listener) {
        mPreferences.registerOnSharedPreferenceChangeListener(listener);
    }

    /**
     * Unregisters a Shared preference listener.
     *
     * @param listener The {@link android.content.SharedPreferences.OnSharedPreferenceChangeListener} to unregister.
     */
    public void unregisterOnSharedPreferenceChangeListener(@NonNull final SharedPreferences.OnSharedPreferenceChangeListener listener) {
        mPreferences.unregisterOnSharedPreferenceChangeListener(listener);
    }
}
