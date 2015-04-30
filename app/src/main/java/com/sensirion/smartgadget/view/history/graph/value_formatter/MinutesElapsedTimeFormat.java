package com.sensirion.smartgadget.view.history.graph.value_formatter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

/**
 * Formatter for the domain (time) axis, uses timestamps of events to determine how much time has elapsed since then.
 */
public class MinutesElapsedTimeFormat extends Format {

    private static final int ONE_SECOND_MS = 1000;

    @NonNull
    @Override
    public StringBuffer format(@NonNull final Object timestamp, @NonNull final StringBuffer buffer, @Nullable final FieldPosition field) {
        final Long objectTimestamp = ((Double) timestamp).longValue();
        final long elapsedMinutes = -((objectTimestamp - System.currentTimeMillis()) / ONE_SECOND_MS) / 60;
        buffer.append(elapsedMinutes);
        return buffer;
    }

    @Nullable
    @Override
    public Object parseObject(final String string, @Nullable final ParsePosition position) {
        return null;
    }
}