package com.sensirion.smartgadget.view.history.graph.plot_formatter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;

import com.androidplot.Plot;
import com.androidplot.ui.XLayoutStyle;
import com.androidplot.ui.YLayoutStyle;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYStepMode;
import com.sensirion.smartgadget.R;

import java.text.Format;

public abstract class LinearPlotFormatter {

    public static void formatPlot(@NonNull final Context context, @NonNull final XYPlot plot, final String domainLabel, final String rangeLabel, final Format xAxisFormat, final Format yAxisFormat) {
        configurePlotLookAndFeel(plot);
        setPlotPadding(context, plot);
        preparePlotGrid(context, plot);
        setPlotMargins(context, plot);
        configurePlotTitle(context, plot);
        preparePlotRange(context, plot, rangeLabel, yAxisFormat);
        preparePlotDomain(context, plot, domainLabel, xAxisFormat);
    }

    private static void configurePlotLookAndFeel(@NonNull final XYPlot plot) {
        plot.setMarkupEnabled(false);
        plot.setBorderStyle(Plot.BorderStyle.NONE, null, null);
        plot.getLayoutManager().remove(plot.getLegendWidget());
    }

    private static void configurePlotTitle(@NonNull final Context context, @NonNull final XYPlot plot) {
        plot.getTitleWidget().getLabelPaint().setTextSize(context.getResources().getDimensionPixelSize(R.dimen.history_fragment_axis_text_size));
    }

    private static void setPlotMargins(@NonNull final Context context, @NonNull final XYPlot plot) {
        final int marginLeft = context.getResources().getInteger(R.integer.history_fragment_margin_left_graph);
        plot.getGraphWidget().setMarginLeft(marginLeft);
    }

    private static void setPlotPadding(@NonNull final Context context, @NonNull final XYPlot plot) {
        final int leftPadding = context.getResources().getInteger(R.integer.history_fragment_plot_left_padding);
        final int topPadding = context.getResources().getInteger(R.integer.history_fragment_plot_top_padding);
        final int rightPadding = context.getResources().getInteger(R.integer.history_fragment_plot_right_padding);
        final int bottomPadding = context.getResources().getInteger(R.integer.history_fragment_plot_bottom_padding);

        plot.getGraphWidget().setPadding(leftPadding, topPadding, rightPadding, bottomPadding);
    }

    private static void preparePlotGrid(@NonNull final Context context, @NonNull final XYPlot plot) {
        plot.getGraphWidget().getBackgroundPaint().setColor(Color.WHITE);
        plot.getGraphWidget().getGridBackgroundPaint().setColor(Color.WHITE);
        plot.getGraphWidget().getGridBackgroundPaint().setColor(Color.WHITE);
        plot.getGraphWidget().getRangeLabelPaint().setColor(context.getResources().getColor(R.color.font_dark));
        plot.getGraphWidget().getRangeLabelPaint().setTextSize(context.getResources().getDimensionPixelSize(R.dimen.history_fragment_axis_text_size));
        plot.getGraphWidget().getDomainLabelPaint().setColor(context.getResources().getColor(R.color.font_dark));
        plot.getGraphWidget().getDomainLabelPaint().setTextSize(context.getResources().getDimensionPixelSize(R.dimen.history_fragment_axis_text_size));
        plot.getGraphWidget().getDomainOriginLinePaint().setColor(Color.TRANSPARENT);
        plot.getGraphWidget().getRangeOriginLabelPaint().setTextSize(context.getResources().getDimensionPixelSize(R.dimen.history_fragment_axis_text_size));
        plot.getGraphWidget().getRangeOriginLabelPaint().setColor(context.getResources().getColor(R.color.font_dark));
        plot.getGraphWidget().getDomainGridLinePaint().setColor(Color.TRANSPARENT);
    }

    private static void preparePlotRange(@NonNull final Context context, @NonNull final XYPlot plot, final String rangeLabel, final Format yAxisFormat) {
        plot.getRangeLabelWidget().getLabelPaint().setColor(context.getResources().getColor(R.color.font_dark));
        plot.getRangeLabelWidget().getLabelPaint().setTextSize(context.getResources().getDimensionPixelSize(R.dimen.history_fragment_axis_text_size));
        plot.getRangeLabelWidget().setText(rangeLabel);
        plot.setRangeBoundaries(0, 100, BoundaryMode.FIXED);
        plot.setRangeValueFormat(yAxisFormat);
        plot.setRangeStep(XYStepMode.SUBDIVIDE, context.getResources().getInteger(R.integer.history_graph_view_num_default_range_labels));
    }

    private static void preparePlotDomain(@NonNull final Context context, @NonNull final XYPlot plot, final String domainLabel, final Format xAxisFormat) {
        plot.getDomainLabelWidget().getLabelPaint().setColor(context.getResources().getColor(R.color.font_dark));
        plot.getDomainLabelWidget().getLabelPaint().setTextSize(context.getResources().getDimensionPixelSize(R.dimen.history_fragment_axis_text_size));
        plot.setDomainValueFormat(xAxisFormat);
        final float domainLabelWidth = plot.getDomainLabelWidget().getLabelPaint().measureText(domainLabel);
        plot.getDomainLabelWidget().setText(domainLabel);
        plot.getDomainLabelWidget().position(-domainLabelWidth / 2, XLayoutStyle.ABSOLUTE_FROM_CENTER, 20, YLayoutStyle.ABSOLUTE_FROM_BOTTOM);
        plot.setDomainStep(XYStepMode.SUBDIVIDE, context.getResources().getInteger(R.integer.history_graph_view_default_num_domain_labels));
    }
}