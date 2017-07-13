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
package com.sensirion.smartgadget.view.history.type;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.sensirion.smartgadget.R;
import com.sensirion.smartgadget.utils.Settings;
import com.sensirion.smartgadget.utils.XmlFloatExtractor;

import java.text.DecimalFormat;
import java.text.Format;

public enum HistoryUnitType {

    TEMPERATURE(0, R.string.label_t, R.drawable.temperature_icon),
    HUMIDITY(1, R.string.label_rh, R.drawable.humidity_icon);

    private static final String TAG = HistoryUnitType.class.getSimpleName();

    private final int mUnitPosition;
    @StringRes
    private final int mDisplayNameId;
    @DrawableRes
    private final int mIconId;

    HistoryUnitType(final int position,
                    @StringRes final int displayNameId,
                    @DrawableRes final int iconId) {
        mUnitPosition = position;
        mDisplayNameId = displayNameId;
        mIconId = iconId;
    }

    /**
     * Obtains the unit stored in a particular position of the view.
     *
     * @param position of the unit.
     * @return {@link com.sensirion.smartgadget.view.history.type.HistoryUnitType} with the UnitType.
     */
    @NonNull
    public static HistoryUnitType getUnitType(final int position) {
        for (final HistoryUnitType interval : values()) {
            if (interval.mUnitPosition == position) {
                return interval;
            }
        }
        throw new IllegalArgumentException(String.format("%s: getUnitType -> There is no interval in position %d.", TAG, position));
    }

    /**
     * Gets the position of a unit.
     *
     * @return {@link java.lang.Integer} with the position of the unit in the view.
     */
    @SuppressWarnings("unused")
    public int getUnitPosition() {
        return mUnitPosition;
    }

    /**
     * Obtains the name of the unit.
     *
     * @param context cannot be <code>null</code>
     * @return {@link java.lang.String} with the name of the unit.
     */
    @NonNull
    public String getDisplayName(@NonNull final Context context) {
        return context.getResources().getString(mDisplayNameId);
    }

    /**
     * Obtains the format of the unit.
     *
     * @return {@link java.text.Format} on how the unit values are going to be displayed.
     */
    @NonNull
    public Format getValueFormat(@NonNull final Context context) {
        if (this == TEMPERATURE) {
            if (Settings.getInstance().isTemperatureUnitFahrenheit()) {
                return new DecimalFormat(context.getResources().getString(R.string.history_temperature_decimal_format_fahrenheit));
            }
            return new DecimalFormat(context.getResources().getString(R.string.history_temperature_decimal_format_celsius));
        }

        if (this == HUMIDITY) {
            return new DecimalFormat(context.getResources().getString(R.string.history_humidity_decimal_format));
        }
        throw new IllegalStateException(String.format("%s: getValueFormat -> Enum is not from a valid format.", TAG));
    }

    /**
     * Obtains the minimum graph resolution.
     *
     * @return {@link java.lang.Float} with the minimum graph resolution.
     */
    public float getMinimumGraphResolution(@NonNull final Context context) {
        if (this == TEMPERATURE) {
            if (Settings.getInstance().isTemperatureUnitFahrenheit()) {
                return XmlFloatExtractor.getFloatValueFromId(context, R.dimen.history_temperature_separation_fahrenheit);
            }
            return XmlFloatExtractor.getFloatValueFromId(context, R.dimen.history_temperature_separation_celsius);
        }
        if (this == HUMIDITY) {
            return XmlFloatExtractor.getFloatValueFromId(context, R.dimen.history_humidity_separation);
        }
        throw new IllegalStateException(String.format("%s: getValueFormat -> Enum is not from a valid format.", TAG));
    }

    /**
     * Get the icon of the Unit Type.
     *
     * @param context cannot be <code>null</code>
     * @return {@link android.graphics.drawable.Drawable} with the icon.
     */
    @Nullable
    public Drawable getIcon(@NonNull final Context context) {
        return context.getResources().getDrawable(mIconId);
    }
}
