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