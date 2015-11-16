package com.sensirion.smartgadget.tests.ui.view;

import android.graphics.PointF;
import android.test.AndroidTestCase;

import com.sensirion.smartgadget.R;
import com.sensirion.smartgadget.view.comfort_zone.graph.XyPlotView;

public abstract class XyPlotViewUnderTest extends AndroidTestCase {

    public static final int Y_AXIS_GRID_SIZE = 10;
    public static final int MAX_Y_AXIS_VALUE = 100;
    public static final int MIN_Y_AXIS_VALUE = 0;
    public static final int X_AXIS_GRID_SIZE = 1;
    public static final int MAX_X_AXIS_VALUE = 30;
    public static final int MIN_X_AXIS_VALUE = 15;

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
        mPlotView.setXAxisScale(MIN_X_AXIS_VALUE, MAX_X_AXIS_VALUE, X_AXIS_GRID_SIZE);
        mPlotView.setYAxisLabel(getContext().getString(R.string.graph_label_humidity));
        mPlotView.setYAxisScale(MIN_Y_AXIS_VALUE, MAX_Y_AXIS_VALUE, Y_AXIS_GRID_SIZE);
    }
}
