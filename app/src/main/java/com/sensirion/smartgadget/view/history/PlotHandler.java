package com.sensirion.smartgadget.view.history;

import android.content.Context;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYStepMode;
import com.sensirion.smartgadget.R;
import com.sensirion.smartgadget.utils.Converter;
import com.sensirion.smartgadget.utils.Settings;
import com.sensirion.smartgadget.utils.view.ColorManager;
import com.sensirion.smartgadget.view.history.graph.HistoryPlot;
import com.sensirion.smartgadget.view.history.graph.value_formatter.ShowNothingFormat;
import com.sensirion.smartgadget.view.history.type.HistoryIntervalType;
import com.sensirion.smartgadget.view.history.type.HistoryUnitType;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindInt;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

public class PlotHandler {

    // Class TAG
    @NonNull
    private static final String TAG = PlotHandler.class.getSimpleName();

    // Injected XML views
    @BindView(R.id.history_fragment_plot)
    HistoryPlot mViewPlot;

    // Extracted constants from the XML resources
    @BindString(R.string.graph_label_elapsed_time)
    String ELAPSED_TIME_STRING;
    @BindString(R.string.graph_label_temperature)
    String TEMPERATURE_STRING;
    @BindInt(R.integer.history_graph_view_num_default_range_labels)
    int DEFAULT_NUMBER_RANGE_LABELS;
    @BindString(R.string.graph_label_temperature_fahrenheit)
    String TEMPERATURE_LABEL_IN_FAHRENHEIT;
    @BindString(R.string.graph_label_temperature_celsius)
    String TEMPERATURE_LABEL_IN_CELSIUS;
    @BindString(R.string.graph_label_relative_humidity)
    String RELATIVE_HUMIDITY_LABEL;
    @BindString(R.string.graph_label_temperature)
    String TEMPERATURE_LABEL;
    @BindInt(R.integer.history_graph_min_humidity_range)
    int MIN_PLOT_HUMIDITY;
    @BindInt(R.integer.history_graph_max_humidity_range)
    int MAX_PLOT_HUMIDITY;
    @BindInt(R.integer.history_graph_min_separation)
    int MIN_PLOT_SEPARATION;
    @BindInt(R.integer.history_graph_threshold_time_representation_ms)
    int THRESHOLD_TIME_REPRESENTATION_MS;
    @BindInt(R.integer.history_graph_gap_resolution_multiplier)
    int GAP_THRESHOLD_RESOLUTION_MULTIPLIER;

    // Plot State
    @NonNull
    private List<SimpleXYSeries> mDeviceSeries = new LinkedList<>();
    private boolean mShouldResetRangeBoundaries = true;
    private double mRangeValueMin;
    private double mRangeValueMax;
    private boolean mIsFahrenheit;

    // Type of data displayed.
    @NonNull
    private HistoryUnitType mLastUnit;
    @NonNull
    private HistoryIntervalType mLastInterval;

    public PlotHandler(@NonNull final View historyView,
                       @NonNull final HistoryIntervalType defaultInterval,
                       @NonNull final HistoryUnitType defaultUnitType) {

        ButterKnife.bind(this, historyView);

        mViewPlot.setDomainStep(XYStepMode.SUBDIVIDE, defaultInterval.getNumberDomainElements());
        mIsFahrenheit = Settings.getInstance().isTemperatureUnitFahrenheit();

        mLastInterval = defaultInterval;
        mLastUnit = defaultUnitType;

        final String defaultDomainText = ELAPSED_TIME_STRING;
        final String defaultRangeText = TEMPERATURE_STRING;

        mViewPlot.format(defaultDomainText, defaultRangeText,
                new ShowNothingFormat(), new ShowNothingFormat());
    }

    public synchronized void updateSeries(@NonNull final Context context,
                                          @NonNull final List<SimpleXYSeries> series,
                                          @NonNull final HistoryIntervalType interval,
                                          @NonNull final HistoryUnitType type) {

        mShouldResetRangeBoundaries = true;
        cleanSeries();
        mDeviceSeries = series;
        updatePlotRangeFormat(context, type);
        updatePlotDomainFormat(context, interval);
        updatePlot();
        Log.i(TAG, "updateSeries -> Series where updated, graph was updated.");
    }

    private void updatePlotDomainFormat(@NonNull final Context context,
                                        @NonNull final HistoryIntervalType interval) {
        mLastInterval = interval;
        mViewPlot.setDomainStep(XYStepMode.SUBDIVIDE, mLastInterval.getNumberDomainElements());
        mViewPlot.setDomainValueFormat(mLastInterval.getTimeFormat());
        mViewPlot.setDomainLabel(mLastInterval.getGraphLabel(context));
    }

    private void updatePlotRangeFormat(@NonNull final Context context, @NonNull final HistoryUnitType type) {
        mLastUnit = type;
        mViewPlot.setRangeValueFormat(type.getValueFormat(context));
        mViewPlot.setRangeStep(XYStepMode.SUBDIVIDE, DEFAULT_NUMBER_RANGE_LABELS);
        if (type == HistoryUnitType.TEMPERATURE) {
            updateRangeFormatToTemperature();
        } else {
            updateRangeFormatToHumidity();
        }
    }

    private void updateRangeFormatToTemperature() {
        mIsFahrenheit = Settings.getInstance().isTemperatureUnitFahrenheit();
        if (mIsFahrenheit) {
            mViewPlot.setRangeLabel(TEMPERATURE_LABEL_IN_FAHRENHEIT);
        } else {
            mViewPlot.setRangeLabel(TEMPERATURE_LABEL_IN_CELSIUS);
        }
    }

    private void updateRangeFormatToHumidity() {
        //The white space solves a bug in the graph
        final String fixedHumidityString = String.format(" %s", RELATIVE_HUMIDITY_LABEL);
        mViewPlot.setRangeLabel(fixedHumidityString);
    }

    private void updatePlot() {
        boolean validSeriesFound = false;
        long biggestTimestampSeries = 0;
        for (final SimpleXYSeries deviceSeries : mDeviceSeries) {
            checkSeriesRange(deviceSeries);
            final long biggestSeriesTimestamp = obtainBiggestTimestampSeries(deviceSeries);
            if (biggestSeriesTimestamp > biggestTimestampSeries) {
                biggestTimestampSeries = biggestSeriesTimestamp;
            }
            final LineAndPointFormatter deviceFormatter = getDeviceFormatterFromSeries(deviceSeries);

            List<SimpleXYSeries> deviceSeriesNoGaps = handleGapsForSeries(deviceSeries);
            for (final SimpleXYSeries series : deviceSeriesNoGaps) {
                mViewPlot.addSeries(prepareSeriesToShow(series), deviceFormatter);
            }

            validSeriesFound = true;
        }
        adjustGraphFormat(biggestTimestampSeries, validSeriesFound);
    }

    private void adjustGraphFormat(final long biggestTimestampSeries,
                                   final boolean validSeriesFound) {
        if (validSeriesFound) {
            adjustGraphBoundaries(biggestTimestampSeries);
        } else {
            mViewPlot.setRangeValueFormat(new ShowNothingFormat());
            mViewPlot.setDomainValueFormat(new ShowNothingFormat());
            mViewPlot.setRangeLabel(TEMPERATURE_LABEL);
            mViewPlot.redraw();
        }
    }

    /**
     * Separates a series in several series so the graph will have separated lines for very separated datapoints.
     *
     * @param deviceSeries that is going to be slitted.
     * @return {@link java.util.List} with the different series.
     */
    @NonNull
    private List<SimpleXYSeries> handleGapsForSeries(@NonNull final SimpleXYSeries deviceSeries) {
        final List<SimpleXYSeries> listOfSeriesOfDevice = new LinkedList<>();

        SimpleXYSeries analysedSeries = new SimpleXYSeries(deviceSeries.getTitle());

        final long lastIntervalResolution = mLastInterval.getIntervalView().getResolution();
        final long maximumResolutionBetweenGraphPoints = lastIntervalResolution * GAP_THRESHOLD_RESOLUTION_MULTIPLIER;

        for (int i = deviceSeries.size() - 1; i >= 0; i--) {
            if (analysedSeries.size() > 0) {
                final long timestampDeviceSeries = deviceSeries.getX(i).longValue();
                final long lastTimestampSeriesToShow = analysedSeries.getX(analysedSeries.size() - 1).longValue();
                if (timestampDeviceSeries > lastTimestampSeriesToShow + maximumResolutionBetweenGraphPoints) {
                    listOfSeriesOfDevice.add(analysedSeries);
                    analysedSeries = new SimpleXYSeries(deviceSeries.getTitle());
                }
            }
            analysedSeries.addLast(deviceSeries.getX(i), deviceSeries.getY(i));
        }
        listOfSeriesOfDevice.add(analysedSeries);
        return listOfSeriesOfDevice;
    }

    @NonNull
    private SimpleXYSeries prepareSeriesToShow(@NonNull final SimpleXYSeries series) {
        final SimpleXYSeries fixedDeviceSeries;
        if (mIsFahrenheit && mLastUnit == HistoryUnitType.TEMPERATURE) {
            fixedDeviceSeries = convertSeriesToFahrenheit(series);
        } else {
            fixedDeviceSeries = series;
        }
        if (fixedDeviceSeries.size() == 1) {
            prepare1ValueSeries(fixedDeviceSeries);
        }
        return fixedDeviceSeries;
    }

    @NonNull
    private SimpleXYSeries convertSeriesToFahrenheit(@NonNull final SimpleXYSeries seriesInCelsius) {
        final SimpleXYSeries seriesInFahrenheit = new SimpleXYSeries(seriesInCelsius.getTitle());
        for (int i = 0; i < seriesInCelsius.size(); i++) {
            final Number x = seriesInCelsius.getX(i);
            final Number y = Converter.convertToF(seriesInCelsius.getY(i).floatValue());
            seriesInFahrenheit.addFirst(x, y);
        }
        return seriesInFahrenheit;
    }

    private long obtainBiggestTimestampSeries(@NonNull final SimpleXYSeries series) {
        final long firstValue = series.getX(0).longValue();
        final long lastValue = series.getX(series.size() - 1).longValue();

        if (firstValue > lastValue) {
            return firstValue;
        }
        return lastValue;
    }

    /**
     * Checks if the graph range is modified in some way when.
     *
     * @param series that has to be checked.
     */
    private void checkSeriesRange(@NonNull final SimpleXYSeries series) {
        for (int i = 0; i < series.size(); i++) {
            final float rangeValue = series.getY(i).floatValue();
            if (mIsFahrenheit && mLastUnit == HistoryUnitType.TEMPERATURE) {
                final float rangeValueFahrenheit = Converter.convertToF(rangeValue);
                recalculateRangeBoundaries(rangeValueFahrenheit);
            } else {
                recalculateRangeBoundaries(rangeValue);
            }
        }
    }

    /**
     * By definition a linear format only shows the deference between two values. In this way when we have
     * 1 single value the graph is shown. It extends the value to show a tiny line to display the value.
     *
     * @param series that is going to be prepared to be shown.
     */
    private void prepare1ValueSeries(@NonNull final SimpleXYSeries series) {
        final long resolutionTime = mLastInterval.getIntervalView().getResolution();
        final long displayedLineLength = resolutionTime / 3;
        series.addFirst((series.getX(0).longValue() - displayedLineLength), series.getY(0));
    }

    /**
     * Obtains and prepares the formatter of a series, using the specific color of a device.
     *
     * @param deviceSeries that is to be formatted.
     * @return {@link com.androidplot.xy.LineAndPointFormatter} of a device series.
     * NOTE: The name of a series MUST be the device address of the device.
     */
    @Nullable
    private LineAndPointFormatter getDeviceFormatterFromSeries(@NonNull final SimpleXYSeries deviceSeries) {
        final int lineColor = ColorManager.getInstance().getDeviceColor(deviceSeries.getTitle());
        final Paint fillColor = new Paint();
        fillColor.setColor(lineColor);
        fillColor.setAlpha(45);
        return new LineAndPointFormatter(lineColor, null, fillColor.getColor(), null);
    }

    /**
     * Deletes all the graph data displayed in the plot.
     */
    private void cleanSeries() {
        mDeviceSeries.clear();

        /* FIXME Change plot update approach.
         *
         * Memory leak in the XYPlot element is caused by incorrect SimpleXYSeries handling.
         * The solution below is a workaround, not a fix, since it solves the OOM problem, but
         * generates performance issues.
         *
         * In the current solution, every time the plot is updated, all data
         * is removed from the plot, the series are updated and re-added to the plot.
         *
         * A much better (in terms of performance) solution should be to remove old elements
         * only (if at all) and add new elements (if needed).
         *
         * From android tutorial (http://androidplot.com/docs/dynamically-plotting-sensor-data/):
         * SimpleXYPlot was meant for use in plots that are static or comprised of a small number
         * of samples that change infrequently. This is a convenience class and should only be used
         * for static data models; it is not suitable for representing dynamically changing data.
         *
         * What caused OOM fault were some remaining references to series data in the XYPlot, even
         * though mViewPlot.removeSeries(setElement) was called. This prohibits GC from removing
         * old data series. Therefore, the code below (workaround) removes elements from series
         * explicitely one by one.
         *
         * For plotting dynamic sensor data in real time, please see:
         * http://androidplot.com/docs/dynamically-plotting-sensor-data/
         * http://androidplot.com/docs/a-dynamic-xy-plot/
         */
        for (XYSeries xySeries : mViewPlot.getSeriesSet()) {
            SimpleXYSeries setElement = (SimpleXYSeries) xySeries;
            while (setElement.size() > 0) {
                setElement.removeLast();
            }
            mViewPlot.removeSeries(setElement);
        }

        mViewPlot.clear();
        mViewPlot.setDomainValueFormat(null);
        mViewPlot.setRangeValueFormat(null);
        mViewPlot.getRendererList().clear();
    }

    /**
     * Constantly recalculates the range boundaries
     *
     * @param rangeValue the current range value
     * @return <code>true</code> if upper and/or lower range boundary has been changed
     */
    private boolean recalculateRangeBoundaries(final double rangeValue) {
        if (mShouldResetRangeBoundaries) {
            mRangeValueMax = rangeValue;
            mRangeValueMin = rangeValue;
            mShouldResetRangeBoundaries = false;
        } else if (mRangeValueMax < rangeValue) {
            mRangeValueMax = rangeValue;
        } else if (mRangeValueMin > rangeValue) {
            mRangeValueMin = rangeValue;
        } else {
            return false;
        }
        return true;
    }

    private void adjustGraphBoundaries(final long timestampMillis) {
        adjustRangeBoundaries();
        adjustDomainBoundaries(timestampMillis);
        mViewPlot.redraw();
    }

    private void adjustRangeBoundaries() {

        double delta = mRangeValueMax - mRangeValueMin;
        int roundTo = 5;
        if (delta < 1) {
            roundTo = 1;
        } else if (delta < 2) {
            roundTo = 2;
        }

        int minValueRounded = roundDown(mRangeValueMin, roundTo);
        int maxValueRounded = roundUp(mRangeValueMax, roundTo);

        mViewPlot.setRangeLowerBoundary(minValueRounded, BoundaryMode.FIXED);
        mViewPlot.setRangeUpperBoundary(maxValueRounded, BoundaryMode.FIXED);
    }

    /**
     * Rounds a double to the next lower multiple of a specified integer
     *
     * @param n number to be rounded
     * @param r multiple
     * @return The next lower multiple of r
     */
    private int roundDown(double n, int r) {
        int rounded = (int) Math.floor(n);
        return rounded / r * r;
    }

    /**
     * Rounds a double to the next higher multiple of a specified integer
     *
     * @param n number to be rounded
     * @param r multiple
     * @return The next higher multiple of r
     */
    private int roundUp(double n, int r) {
        int rounded = (int) Math.ceil(n);
        return rounded / r * r + ((rounded % r) > 0 ? r : 0);
    }

    /**
     * Updates the domain boundary.
     *
     * @param lastBoundary that it's going to be put in the
     */
    private void adjustDomainBoundaries(long lastBoundary) {
        if (lastBoundary < System.currentTimeMillis() - THRESHOLD_TIME_REPRESENTATION_MS) {
            lastBoundary = System.currentTimeMillis();
        }
        final long firstBoundary = lastBoundary - mLastInterval.getNumberMilliseconds();
        Log.d(TAG,
                String.format(
                        "%s -> Domain boundaries has been updated from %d to %d.",
                        "adjustDomainBoundaries()",
                        firstBoundary,
                        lastBoundary
                )
        );

        mViewPlot.setDomainBoundaries(firstBoundary, lastBoundary, BoundaryMode.FIXED);
    }
}