package com.sensirion.smartgadget.view.history.graph.value_formatter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

/**
 * Formatter for showing nothing.
 */
public class ShowNothingFormat extends Format {

    @NonNull
    @Override
    public StringBuffer format(final Object timestamp, @NonNull final StringBuffer buffer, @Nullable final FieldPosition field) {
        return buffer;
    }

    @Nullable
    @Override
    public Object parseObject(final String string, @Nullable final ParsePosition position) {
        return null;
    }
}