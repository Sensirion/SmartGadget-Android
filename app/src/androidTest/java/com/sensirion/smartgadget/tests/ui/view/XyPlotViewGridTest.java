package com.sensirion.smartgadget.tests.ui.view;

import android.graphics.PointF;
import android.test.suitebuilder.annotation.SmallTest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class XyPlotViewGridTest extends XyPlotViewUnderTest {

    protected static final float OFFSET_ERROR = 0.1f;
    private Method reflection_isWithinGrids;

    public XyPlotViewGridTest() {
        super();
    }

    protected void setUp() throws Exception {
        super.setUp();

        mTopLeft = new PointF(mPlotView.getXAxisMin(), mPlotView.getYAxisMax());
        mTopRight = new PointF(mPlotView.getXAxisMax(), mPlotView.getYAxisMax());
        mBottomRight = new PointF(mPlotView.getXAxisMax(), mPlotView.getYAxisMin());
        mBottomLeft = new PointF(mPlotView.getXAxisMin(), mPlotView.getYAxisMin());

        //TODO: keep in mind to change this string aswell if method-name has changed in XyPlotView
        reflection_isWithinGrids = mPlotView.getClass().getDeclaredMethod("isOutsideGrid", PointF.class);
        reflection_isWithinGrids.setAccessible(true);
    }

    @SmallTest
    public void testCorners() throws NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        assertTrue((Boolean) reflection_isWithinGrids.invoke(mPlotView, mTopLeft));
        assertTrue((Boolean) reflection_isWithinGrids.invoke(mPlotView, mTopRight));
        assertTrue((Boolean) reflection_isWithinGrids.invoke(mPlotView, mBottomRight));
        assertTrue((Boolean) reflection_isWithinGrids.invoke(mPlotView, mBottomLeft));
    }

    @SmallTest
    public void testOutsideGrid() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        PointF outsideTopLeft = new PointF(mPlotView.getXAxisMin() - OFFSET_ERROR, mPlotView.getYAxisMax());
        PointF outsideTopRight = new PointF(mPlotView.getXAxisMax(), mPlotView.getYAxisMax() + OFFSET_ERROR);
        PointF outsideBottomRight = new PointF(mPlotView.getXAxisMax() - OFFSET_ERROR, mPlotView.getYAxisMin() - OFFSET_ERROR);
        PointF outsideBottomLeft = new PointF(mPlotView.getXAxisMin() + OFFSET_ERROR, mPlotView.getYAxisMin() - OFFSET_ERROR);

        assertFalse((Boolean) reflection_isWithinGrids.invoke(mPlotView, outsideTopLeft));
        assertFalse((Boolean) reflection_isWithinGrids.invoke(mPlotView, outsideTopRight));
        assertFalse((Boolean) reflection_isWithinGrids.invoke(mPlotView, outsideBottomRight));
        assertFalse((Boolean) reflection_isWithinGrids.invoke(mPlotView, outsideBottomLeft));
    }

}
