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
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

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

    INTERVAL_OF_10_MINUTES(
            0,
            R.string.label_10_minutes,
            HistoryDataLast10MinutesView.getInstance(),
            11,
            new MinutesElapsedTimeFormat(),
            R.string.graph_label_domain_min
    ),
    INTERVAL_OF_1_HOUR(
            1,
            R.string.label_1_hour,
            HistoryDataLast1HourView.getInstance(),
            7,
            new MinutesElapsedTimeFormat(),
            R.string.graph_label_domain_min
    ),
    INTERVAL_OF_6_HOUR(
            2,
            R.string.label_6_hours,
            HistoryDataLast6HoursView.getInstance(),
            7,
            new HourElapsedTimeFormat(),
            R.string.graph_label_domain_hours
    ),
    INTERVAL_OF_1_DAY(
            3,
            R.string.label_1_day,
            HistoryDataLast1DayView.getInstance(),
            7,
            new HourElapsedTimeFormat(),
            R.string.graph_label_domain_hours
    ),
    INTERVAL_OF_1_WEEK(
            4,
            R.string.label_1_week,
            HistoryDataLast1WeekView.getInstance(),
            8,
            new DaysElapsedTimeFormat(),
            R.string.graph_label_domain_days
    );

    @NonNull
    private static final String TAG = HistoryIntervalType.class.getSimpleName();

    private final int mPosition;
    private final int mDisplayNameId;
    @NonNull
    private final AbstractHistoryDataView mDatabaseView;
    private final int mNumberDomainElements;
    @NonNull
    private final Format mTimeFormat;
    private final int mGraphLabelId;

    HistoryIntervalType(final int position,
                        @StringRes final int displayNameId,
                        @NonNull final AbstractHistoryDataView view,
                        final int numberDomainElements,
                        @NonNull final Format timeFormat,
                        @StringRes final int graphLabelId) {
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
        throw new IllegalArgumentException(
                String.format(
                        "%s: getInterval -> Position %d it's not a valid %s.",
                        TAG,
                        position,
                        TAG
                )
        );
    }

    /**
     * Obtains the position of the interval in the screen
     *
     * @return <code>int</code> with the position number
     */
    public int getPosition() {
        return mPosition;
    }

    /**
     * Obtains the display name from the database.
     *
     * @param context needed for extracting the label from the XML.
     * @return {@link String} with the display name
     */
    @NonNull
    public String getDisplayName(@NonNull final Context context) {
        return context.getResources().getString(mDisplayNameId);
    }

    /**
     * Obtains the database {@link AbstractHistoryDataView} from the interval
     *
     * @return {@link AbstractHistoryDataView}
     */
    @NonNull
    public AbstractHistoryDataView getIntervalView() {
        return mDatabaseView;
    }

    /**
     * Obtains the number of domain elements in the graph.
     *
     * @return <code>int</code> with the number of domain elements.
     */
    public int getNumberDomainElements() {
        return mNumberDomainElements;
    }

    /**
     * Obtains the time format of the graph.
     *
     * @return {@link Format}
     */
    @NonNull
    public Format getTimeFormat() {
        return mTimeFormat;
    }

    /**
     * Obtains the label of the graph related to a time interval.
     *
     * @param context needed for extracting the graph label of the lime interval.
     * @return {@link String} with the interval.
     */
    @NonNull
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
