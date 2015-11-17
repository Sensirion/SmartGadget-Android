package com.sensirion.smartgadget.tests.ui.view;

import android.graphics.PointF;

public class XyPlotViewComfortZoneWinterTest extends XyPlotViewComfortZoneTest {

    public XyPlotViewComfortZoneWinterTest() {
        super();
    }

    protected void setUp() throws Exception {
        super.setUp();

        mTopLeft = new PointF(19.5f, 86.5f);
        mTopRight = new PointF(23.5f, 58.3f);
        mBottomRight = new PointF(24.5f, 23.0f);
        mBottomLeft = new PointF(20.5f, 29.3f);

        mPlotView.setComfortZoneWinter(true);
    }
}
