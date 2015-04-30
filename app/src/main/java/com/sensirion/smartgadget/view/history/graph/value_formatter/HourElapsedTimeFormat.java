package com.sensirion.smartgadget.view.history.graph.value_formatter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

/**
 * Formatter for the domain (time) axis, uses timestamps of events to determine how much time has elapsed since then.
 */
public class HourElapsedTimeFormat extends Format {

    private static final int ONE_SECOND_MS = 1000;

    @NonNull
    @Override
    public StringBuffer format(@NonNull final Object timestamp, @NonNull final StringBuffer buffer, @Nullable final FieldPosition field) {
        final Long objectTimestamp = ((Double) timestamp).longValue();
        final long elapsedMinutes = -((objectTimestamp - System.currentTimeMillis()) / ONE_SECOND_MS) / 60;
        final float elapsedHours = ((float) elapsedMinutes) / 60f;

        buffer.append(new DecimalFormat("0.#").format(elapsedHours));
        return buffer;
    }

    @Nullable
    @Override
    public Object parseObject(final String string, @Nullable final ParsePosition position) {
        return null;
    }
}