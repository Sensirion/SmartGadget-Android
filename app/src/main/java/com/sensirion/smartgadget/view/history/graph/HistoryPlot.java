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
package com.sensirion.smartgadget.view.history.graph;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.AttributeSet;

import com.androidplot.Plot;
import com.androidplot.ui.AnchorPosition;
import com.androidplot.ui.SizeLayoutType;
import com.androidplot.ui.SizeMetrics;
import com.androidplot.ui.XLayoutStyle;
import com.androidplot.ui.YLayoutStyle;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYStepMode;
import com.sensirion.smartgadget.R;

import java.text.Format;

public class HistoryPlot extends XYPlot {

    public HistoryPlot(Context context, AttributeSet attributes) {
        super(context, attributes);
    }

    public void format(final String domainLabel, final String rangeLabel,
                       final Format xAxisFormat, final Format yAxisFormat) {
        Resources resources = getContext().getResources();
        configurePlotLookAndFeel();
        preparePlotGrid(resources);
        configurePlotTitle(resources);
        preparePlotRange(resources, rangeLabel, yAxisFormat);
        preparePlotDomain(resources, domainLabel, xAxisFormat);
        layout(resources);
    }

    private void configurePlotLookAndFeel() {
        setMarkupEnabled(false);
        setBorderStyle(Plot.BorderStyle.NONE, null, null);
        getLayoutManager().remove(getLegendWidget());
        getLayoutManager().remove(getTitleWidget());
    }

    private void configurePlotTitle(@NonNull final Resources resources) {
        getTitleWidget().getLabelPaint().setTextSize(resources.getDimensionPixelSize(R.dimen.history_fragment_axis_text_size));
    }

    private void preparePlotGrid(@NonNull final Resources resources) {
        XYGraphWidget widget = getGraphWidget();
        widget.getBackgroundPaint().setColor(Color.WHITE);
        widget.getGridBackgroundPaint().setColor(Color.WHITE);
        widget.getGridBackgroundPaint().setColor(Color.WHITE);
        widget.getRangeLabelPaint().setColor(resources.getColor(R.color.font_dark));
        widget.getRangeLabelPaint().setTextSize(resources.getDimensionPixelSize(R.dimen.history_fragment_axis_text_size));
        widget.getDomainLabelPaint().setColor(resources.getColor(R.color.font_dark));
        widget.getDomainLabelPaint().setTextSize(resources.getDimensionPixelSize(R.dimen.history_fragment_axis_text_size));
        widget.getDomainOriginLinePaint().setColor(Color.TRANSPARENT);
        widget.getRangeOriginLabelPaint().setTextSize(resources.getDimensionPixelSize(R.dimen.history_fragment_axis_text_size));
        widget.getRangeOriginLabelPaint().setColor(resources.getColor(R.color.font_dark));
        widget.getDomainGridLinePaint().setColor(Color.TRANSPARENT);
    }

    private void preparePlotRange(@NonNull final Resources resources, final String rangeLabel, final Format yAxisFormat) {
        Paint rangeLabelPaint = getRangeLabelWidget().getLabelPaint();
        rangeLabelPaint.setColor(resources.getColor(R.color.font_dark));
        rangeLabelPaint.setTextSize(resources.getDimensionPixelSize(R.dimen.history_fragment_axis_text_size));
        setRangeLabel(rangeLabel);
        setRangeBoundaries(0, 100, BoundaryMode.FIXED);
        setRangeValueFormat(yAxisFormat);
        setRangeStep(XYStepMode.SUBDIVIDE, resources.getInteger(R.integer.history_graph_view_num_default_range_labels));
        getGraphWidget().setRangeAxisPosition(true, false, 0, resources.getString(R.string.history_graph_range_label));
    }

    private void preparePlotDomain(@NonNull final Resources resources, final String domainLabel, final Format xAxisFormat) {
        Paint domainLabelPaint = getDomainLabelWidget().getLabelPaint();
        domainLabelPaint.setColor(resources.getColor(R.color.font_dark));
        domainLabelPaint.setTextSize(resources.getDimensionPixelSize(R.dimen.history_fragment_axis_text_size));
        setDomainLabel(domainLabel);
        setDomainValueFormat(xAxisFormat);
        setDomainStep(XYStepMode.SUBDIVIDE, resources.getInteger(R.integer.history_graph_view_default_num_domain_labels));
        getGraphWidget().setDomainAxisPosition(true, false, 0, resources.getString(R.string.history_graph_domain_label));
    }

    private void layout(@NonNull Resources resources) {
        // center the x-axis label
        getDomainLabelWidget().position(
                0, XLayoutStyle.ABSOLUTE_FROM_CENTER,
                0, YLayoutStyle.ABSOLUTE_FROM_BOTTOM, AnchorPosition.BOTTOM_MIDDLE);

        // position graph at the top right
        getGraphWidget().position(
                0, XLayoutStyle.ABSOLUTE_FROM_RIGHT,
                0, YLayoutStyle.ABSOLUTE_FROM_TOP,
                AnchorPosition.RIGHT_TOP);
        // fill all space except for axis labels
        float domainHeight = getDomainLabelWidget().getHeightPix(0);
        float rangeWidth = getRangeLabelWidget().getWidthPix(0);
        getGraphWidget().setSize(new SizeMetrics(
                domainHeight, SizeLayoutType.FILL,
                rangeWidth, SizeLayoutType.FILL));

        // we need a top padding because the y-axis labels are cut of otherwise
        final int topPadding = resources.getInteger(R.integer.history_fragment_plot_top_padding);
        getGraphWidget().setPaddingTop(topPadding);
    }
}
