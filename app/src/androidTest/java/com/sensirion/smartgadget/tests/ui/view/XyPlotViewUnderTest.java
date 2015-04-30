package com.sensirion.smartgadget.tests.ui.view;

import android.graphics.PointF;
import android.test.AndroidTestCase;

import com.sensirion.smartgadget.R;
import com.sensirion.smartgadget.view.comfort_zone.ComfortZoneFragment;
import com.sensirion.smartgadget.view.comfort_zone.graph.XyPlotView;

public abstract class XyPlotViewUnderTest extends AndroidTestCase {

    protected XyPlotView mPlotView = null;

    protected PointF mTopLeft = null;
    protected PointF mTopRight = null;
    protected PointF mBottomRight = null;
    protected PointF mBottomLeft = null;

    public XyPlotViewUnderTest() {
        super();
    }

    protected void setUp() throws Exception {
        super.setUp();

        mPlotView = new XyPlotView(getContext());

        mPlotView.setXAxisLabel(getContext().getString(R.string.graph_label_temperature_celsius));
        mPlotView.setXAxisScale(ComfortZoneFragment.MIN_X_AXIS_VALUE, ComfortZoneFragment.MAX_X_AXIS_VALUE, ComfortZoneFragment.X_AXIS_GRID_SIZE);
        mPlotView.setYAxisLabel(getContext().getString(R.string.graph_label_humidity));
        mPlotView.setYAxisScale(ComfortZoneFragment.MIN_Y_AXIS_VALUE, ComfortZoneFragment.MAX_Y_AXIS_VALUE, ComfortZoneFragment.Y_AXIS_GRID_SIZE);
    }

}
