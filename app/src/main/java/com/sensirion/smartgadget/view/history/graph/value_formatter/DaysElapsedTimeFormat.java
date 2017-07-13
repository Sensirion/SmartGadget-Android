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
package com.sensirion.smartgadget.view.history.graph.value_formatter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.Date;

/**
 * Formatter for the domain (time) axis, uses timestamps of events to determine how much time has elapsed since then.
 */
public class DaysElapsedTimeFormat extends Format {

    @NonNull
    @Override
    public StringBuffer format(@NonNull final Object timestamp, @NonNull final StringBuffer buffer, @Nullable final FieldPosition field) {
        final Long objectTimestamp = ((Double) timestamp).longValue();
        final int dayOfWeek = (new Date(objectTimestamp)).getDay();
        buffer.append(getDayOfTheWeekString(dayOfWeek));
        return buffer;
    }

    @Nullable
    @Override
    public Object parseObject(final String string, @Nullable final ParsePosition position) {
        return null;
    }

    private String getDayOfTheWeekString(final int dayOfWeek) {
        switch (dayOfWeek) {
            case 0:
                return "MON";
            case 1:
                return "TUE";
            case 2:
                return "WEN";
            case 3:
                return "THU";
            case 4:
                return "FRI";
            case 5:
                return "SAT";
            case 6:
                return "SUN";
            default:
                throw new IllegalArgumentException("getDayOfTheString -> The week only have 7 days. Value Received -> " + dayOfWeek);
        }
    }
}
