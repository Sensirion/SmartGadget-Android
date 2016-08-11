package com.sensirion.smartgadget.view.history.graph.plot_formatter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;

import com.androidplot.Plot;
import com.androidplot.ui.AnchorPosition;
import com.androidplot.ui.SizeLayoutType;
import com.androidplot.ui.SizeMetrics;
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
        preparePlotGrid(context, plot);
        configurePlotTitle(context, plot);
        preparePlotRange(context, plot, rangeLabel, yAxisFormat);
        preparePlotDomain(context, plot, domainLabel, xAxisFormat);
        layout(context, plot);
    }

    private static void configurePlotLookAndFeel(@NonNull final XYPlot plot) {
        plot.setMarkupEnabled(false);
        plot.setBorderStyle(Plot.BorderStyle.NONE, null, null);
        plot.getLayoutManager().remove(plot.getLegendWidget());
        plot.getLayoutManager().remove(plot.getTitleWidget());
    }

    private static void configurePlotTitle(@NonNull final Context context, @NonNull final XYPlot plot) {
        plot.getTitleWidget().getLabelPaint().setTextSize(context.getResources().getDimensionPixelSize(R.dimen.history_fragment_axis_text_size));
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
        plot.setRangeLabel(rangeLabel);
        plot.setRangeBoundaries(0, 100, BoundaryMode.FIXED);
        plot.setRangeValueFormat(yAxisFormat);
        plot.setRangeStep(XYStepMode.SUBDIVIDE, context.getResources().getInteger(R.integer.history_graph_view_num_default_range_labels));
        plot.getGraphWidget().setRangeAxisPosition(true, false, 0, context.getString(R.string.history_graph_range_label));
    }

    private static void preparePlotDomain(@NonNull final Context context, @NonNull final XYPlot plot, final String domainLabel, final Format xAxisFormat) {
        plot.getDomainLabelWidget().getLabelPaint().setColor(context.getResources().getColor(R.color.font_dark));
        plot.getDomainLabelWidget().getLabelPaint().setTextSize(context.getResources().getDimensionPixelSize(R.dimen.history_fragment_axis_text_size));
        plot.setDomainLabel(domainLabel);
        plot.setDomainValueFormat(xAxisFormat);
        plot.setDomainStep(XYStepMode.SUBDIVIDE, context.getResources().getInteger(R.integer.history_graph_view_default_num_domain_labels));
        plot.getGraphWidget().setDomainAxisPosition(true, false, 0, context.getString(R.string.history_graph_domain_label));
    }

    private static void layout(@NonNull Context context, @NonNull final XYPlot plot) {
        // center the x-axis label
        plot.getDomainLabelWidget().position(
                -0, XLayoutStyle.ABSOLUTE_FROM_CENTER,
                0, YLayoutStyle.ABSOLUTE_FROM_BOTTOM, AnchorPosition.BOTTOM_MIDDLE);

        // position graph at the top right
        plot.getGraphWidget().position(
                0, XLayoutStyle.ABSOLUTE_FROM_RIGHT,
                0, YLayoutStyle.ABSOLUTE_FROM_TOP,
                AnchorPosition.RIGHT_TOP);
        // fill all space except for axis labels
        float domainHeight = plot.getDomainLabelWidget().getHeightPix(0);
        float rangeWidth = plot.getRangeLabelWidget().getWidthPix(0);
        plot.getGraphWidget().setSize(new SizeMetrics(
                domainHeight, SizeLayoutType.FILL,
                rangeWidth, SizeLayoutType.FILL));

        // we need a top padding because the y-axis labels are cut of otherwise
        final int topPadding = context.getResources().getInteger(R.integer.history_fragment_plot_top_padding);
        plot.getGraphWidget().setPaddingTop(topPadding);
    }
}