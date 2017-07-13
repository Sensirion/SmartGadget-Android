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
