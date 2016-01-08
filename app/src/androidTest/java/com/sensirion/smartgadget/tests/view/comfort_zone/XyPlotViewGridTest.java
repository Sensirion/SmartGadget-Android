package com.sensirion.smartgadget.tests.view.comfort_zone;

import android.graphics.PointF;
import android.test.suitebuilder.annotation.SmallTest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class XyPlotViewGridTest extends XyPlotViewUnderTest {

    protected static final float OFFSET_ERROR = 0.1f;
    private Method reflectionIsOutsideGrids;

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
        reflectionIsOutsideGrids = mPlotView.getClass().getDeclaredMethod("isOutsideGrid", PointF.class);
        reflectionIsOutsideGrids.setAccessible(true);
    }

    @SmallTest
    public void testCorners() throws NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        assertFalse((Boolean) reflectionIsOutsideGrids.invoke(mPlotView, mTopLeft));
        assertFalse((Boolean) reflectionIsOutsideGrids.invoke(mPlotView, mTopRight));
        assertFalse((Boolean) reflectionIsOutsideGrids.invoke(mPlotView, mBottomRight));
        assertFalse((Boolean) reflectionIsOutsideGrids.invoke(mPlotView, mBottomLeft));
    }

    @SmallTest
    public void testOutsideGrid() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        PointF outsideTopLeft = new PointF(mPlotView.getXAxisMin() - OFFSET_ERROR, mPlotView.getYAxisMax());
        PointF outsideTopRight = new PointF(mPlotView.getXAxisMax(), mPlotView.getYAxisMax() + OFFSET_ERROR);
        PointF outsideBottomRight = new PointF(mPlotView.getXAxisMax() - OFFSET_ERROR, mPlotView.getYAxisMin() - OFFSET_ERROR);
        PointF outsideBottomLeft = new PointF(mPlotView.getXAxisMin() + OFFSET_ERROR, mPlotView.getYAxisMin() - OFFSET_ERROR);

        assertTrue((Boolean) reflectionIsOutsideGrids.invoke(mPlotView, outsideTopLeft));
        assertTrue((Boolean) reflectionIsOutsideGrids.invoke(mPlotView, outsideTopRight));
        assertTrue((Boolean) reflectionIsOutsideGrids.invoke(mPlotView, outsideBottomRight));
        assertTrue((Boolean) reflectionIsOutsideGrids.invoke(mPlotView, outsideBottomLeft));
    }
}
