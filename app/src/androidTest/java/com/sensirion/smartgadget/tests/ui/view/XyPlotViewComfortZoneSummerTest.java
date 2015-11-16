package com.sensirion.smartgadget.tests.ui.view;

import android.graphics.PointF;

public class XyPlotViewComfortZoneSummerTest extends XyPlotViewComfortZoneTest {

    public XyPlotViewComfortZoneSummerTest() {
        super();
    }

    protected void setUp() throws Exception {
        super.setUp();

        mTopLeft = new PointF(22.5f, 79.5f);
        mTopRight = new PointF(26.0f, 57.3f);
        mBottomRight = new PointF(27.0f, 19.8f);
        mBottomLeft = new PointF(23.5f, 24.4f);

        mPlotView.setComfortZoneWinter(false);
    }

}
