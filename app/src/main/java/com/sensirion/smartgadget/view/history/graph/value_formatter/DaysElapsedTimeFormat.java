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