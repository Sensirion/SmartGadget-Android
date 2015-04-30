package com.sensirion.smartgadget.view.history.type;

import android.content.Context;
import android.support.annotation.NonNull;

import com.sensirion.smartgadget.R;
import com.sensirion.smartgadget.persistence.history_database.table.AbstractHistoryDataView;
import com.sensirion.smartgadget.persistence.history_database.table.HistoryDataLast10MinutesView;
import com.sensirion.smartgadget.persistence.history_database.table.HistoryDataLast1DayView;
import com.sensirion.smartgadget.persistence.history_database.table.HistoryDataLast1HourView;
import com.sensirion.smartgadget.persistence.history_database.table.HistoryDataLast1WeekView;
import com.sensirion.smartgadget.persistence.history_database.table.HistoryDataLast6HoursView;
import com.sensirion.smartgadget.view.history.graph.value_formatter.DaysElapsedTimeFormat;
import com.sensirion.smartgadget.view.history.graph.value_formatter.HourElapsedTimeFormat;
import com.sensirion.smartgadget.view.history.graph.value_formatter.MinutesElapsedTimeFormat;

import java.text.Format;

public enum HistoryIntervalType {

    INTERVAL_OF_10_MINUTES(0, R.string.label_10_minutes, HistoryDataLast10MinutesView.getInstance(), 10, new MinutesElapsedTimeFormat(), R.string.graph_label_domain_min),
    INTERVAL_OF_1_HOUR(1, R.string.label_1_hour, HistoryDataLast1HourView.getInstance(), 10, new MinutesElapsedTimeFormat(), R.string.graph_label_domain_min),
    INTERVAL_OF_6_HOUR(2, R.string.label_6_hours, HistoryDataLast6HoursView.getInstance(), 7, new HourElapsedTimeFormat(), R.string.graph_label_domain_hours),
    INTERVAL_OF_1_DAY(3, R.string.label_1_day, HistoryDataLast1DayView.getInstance(), 7, new HourElapsedTimeFormat(), R.string.graph_label_domain_hours),
    INTERVAL_OF_1_WEEK(4, R.string.label_1_week, HistoryDataLast1WeekView.getInstance(), 7, new DaysElapsedTimeFormat(), R.string.graph_label_domain_days);

    private static final String TAG = HistoryIntervalType.class.getSimpleName();

    private final int mPosition;
    private final int mDisplayNameId;
    private final AbstractHistoryDataView mDatabaseView;
    private final int mNumberDomainElements;
    private final Format mTimeFormat;
    private final int mGraphLabelId;

    HistoryIntervalType(final int position, final int displayNameId, final AbstractHistoryDataView view, final int numberDomainElements, final Format timeFormat, final int graphLabelId) {
        mPosition = position;
        mDisplayNameId = displayNameId;
        mDatabaseView = view;
        mNumberDomainElements = numberDomainElements;
        mTimeFormat = timeFormat;
        mGraphLabelId = graphLabelId;
    }

    /**
     * Obtains the time interval that corresponds to a position.
     *
     * @param position of the time interval.
     * @return {@link HistoryIntervalType} with the TimeInterval in the selected position.
     */
    @NonNull
    public static HistoryIntervalType getInterval(final int position) {
        for (final HistoryIntervalType interval : values()) {
            if (interval.getPosition() == position) {
                return interval;
            }
        }
        throw new IllegalArgumentException(String.format("%s: getInterval -> Position %d it's not a valid %s.", TAG, position, TAG));
    }

    /**
     * Return the number of intervals.
     *
     * @return <code>int</code> with the number of intervals.
     */
    public static int intervalCount() {
        return values().length;
    }

    public int getPosition() {
        return mPosition;
    }

    public String getDisplayName(@NonNull final Context context) {
        return context.getResources().getString(mDisplayNameId);
    }

    public AbstractHistoryDataView getIntervalView() {
        return mDatabaseView;
    }

    public int getNumberDomainElements() {
        return mNumberDomainElements;
    }

    public Format getTimeFormat() {
        return mTimeFormat;
    }

    public String getGraphLabel(@NonNull final Context context) {
        return context.getResources().getString(mGraphLabelId);
    }

    /**
     * Obtains the number of milliseconds of the view.
     *
     * @return <code>int</code> with the number of milliseconds.
     */
    public int getNumberMilliseconds() {
        return mDatabaseView.getNumberMilliseconds();
    }
}