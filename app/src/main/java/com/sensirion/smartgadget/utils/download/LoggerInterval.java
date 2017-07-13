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
package com.sensirion.smartgadget.utils.download;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.sensirion.smartgadget.R;
import com.sensirion.smartgadget.utils.Interval;

public enum LoggerInterval {
    ONE_SECOND(0, R.string.label_interval_1s, Interval.ONE_SECOND.getNumberSeconds()),
    TEN_SECONDS(1, R.string.label_interval_10s, Interval.TEN_SECONDS.getNumberSeconds()),
    ONE_MINUTE(2, R.string.label_interval_1min, Interval.ONE_MINUTE.getNumberSeconds()),
    FIVE_MINUTES(3, R.string.label_interval_5min, Interval.FIVE_MINUTES.getNumberSeconds()),
    TEN_MINUTES(4, R.string.label_interval_10min, Interval.TEN_MINUTES.getNumberSeconds()),
    ONE_HOUR(5, R.string.label_interval_1h, Interval.ONE_HOUR.getNumberSeconds()),
    THREE_HOURS(6, R.string.label_interval_3h, Interval.THREE_HOURS.getNumberSeconds());

    private static final String TAG = LoggerInterval.class.getSimpleName();

    private final int mPosition;
    private final int mLabelId;
    private final int mSeconds;

    LoggerInterval(final int numberElement, final int labelId, final int seconds) {
        mPosition = numberElement;
        mLabelId = labelId;
        mSeconds = seconds;
    }

    @Nullable
    public static LoggerInterval fromNumberElement(final int position) {
        for (LoggerInterval l : values()) {
            if (position == l.mPosition) {
                return l;
            }
        }
        Log.e(TAG, String.format("fromValue -> Cannot create %s from position: %d", LoggerInterval.class.getSimpleName(), position));
        return null;
    }

    /**
     * Obtains the interval in seconds.
     *
     * @return <code>int</code> with the interval in seconds.
     */
    public int getValueInSeconds() {
        return mSeconds;
    }

    /**
     * Obtains the interval in milliseconds.
     *
     * @return <code>int</code> with the interval in milliseconds.
     */
    public int getValueInMilliseconds() {
        return getValueInSeconds() * 1000;
    }

    /**
     * Obtains the string text of a selected interval.
     *
     * @param context of the requesting activity. Cannot be null.
     * @return {@link java.lang.String} with the String label.
     */
    public String toStringLabel(@NonNull final Context context) {
        return context.getString(mLabelId);
    }
}
