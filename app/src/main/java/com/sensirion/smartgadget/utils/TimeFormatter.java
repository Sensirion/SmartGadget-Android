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

import android.content.Context;
import android.support.annotation.NonNull;

import com.sensirion.smartgadget.R;


public class TimeFormatter {
    private final static byte MINUTE = 60; // seconds
    private final static short HOUR = MINUTE * 60; // seconds

    private final int mHours;
    private final byte mMinutes;
    private final byte mSeconds;
    private final int mTotalSeconds;

    public TimeFormatter(final int numberOfSeconds) {
        mTotalSeconds = numberOfSeconds;
        mHours = numberOfSeconds / HOUR;
        final int rest = numberOfSeconds % HOUR;
        mMinutes = (byte) (rest / MINUTE);
        mSeconds = (byte) (rest % MINUTE);
    }

    public int getHour() {
        return mHours;
    }

    public int getMinute() {
        return mMinutes;
    }

    public int getSeconds() {
        return mSeconds;
    }

    /**
     * Gets the time string description using all the necessary representation Strings.
     * Example 1 = 3683s --> 1 hour 1 minute 23 seconds.
     * Example 2 = 189s --> 3 minutes 9 seconds.
     *
     * @param context of the calling method.
     * @return {@link java.lang.String} with the long String representation.
     */
    @NonNull
    @SuppressWarnings("unused")
    public String getTime(@NonNull final Context context) {
        final StringBuilder sb = new StringBuilder();
        if (getHour() > 0) {
            if (getHour() == 1) {
                sb.append(context.getString(R.string.label_interval_hour_singular));
            } else {
                sb.append(getHour()).append(" ").append(context.getString(R.string.label_interval_hour_plural));
            }
        }
        if (getMinute() > 0) {
            if (getMinute() == 1) {
                sb.append(context.getString(R.string.label_interval_minute_singular));
            } else {
                sb.append(getMinute()).append(" ").append(context.getString(R.string.label_interval_hour_plural));
            }
        }
        if (getSeconds() > 0) {
            if (getSeconds() == 1) {
                sb.append(context.getString(R.string.label_interval_second_singular));
            } else {
                sb.append(getSeconds()).append(" ").append(context.getString(R.string.label_interval_second_plural));
            }
        }
        return sb.toString();
    }

    /**
     * Gets the time String using one single unit. (Hours, minutes or seconds)
     * Example 1: 7200s --> "2 hours"
     * Example 2: 5453s --> "5453 seconds"
     * Example 3: 3600s --> "1 hour"
     * Example 4: 2532s --> "2532 seconds"
     * Example 5: 180s --> "3 minutes"
     * Example 6: 60s --> "1 minute"
     * Example 7: 45s --> "45 seconds"
     * Example 8: 1s --> "1 second"
     *
     * @param context of the calling method.
     * @return {@link java.lang.String} with the short time representation.
     */
    public String getShortTime(@NonNull final Context context) {

        if (mTotalSeconds % HOUR == 0) {
            if (mTotalSeconds == HOUR) {
                return context.getString(R.string.label_interval_hour_singular);
            }
            return String.format("%d %s", mHours, context.getString(R.string.label_interval_hour_plural));
        }
        if (mTotalSeconds % MINUTE == 0) {
            if (mTotalSeconds == MINUTE) {
                return context.getString(R.string.label_interval_minute_singular);
            }
            return String.format("%d %s", mMinutes, context.getString(R.string.label_interval_minute_plural));
        }
        if (mTotalSeconds == 1) {
            return context.getString(R.string.label_interval_second_singular);
        }
        return String.format("%d %s", mTotalSeconds, context.getString(R.string.label_interval_second_plural));
    }
}
