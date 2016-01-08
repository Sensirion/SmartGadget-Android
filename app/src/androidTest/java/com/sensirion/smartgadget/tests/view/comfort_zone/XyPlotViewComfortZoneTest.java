package com.sensirion.smartgadget.tests.view.comfort_zone;

import android.graphics.PointF;
import android.test.suitebuilder.annotation.SmallTest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class XyPlotViewComfortZoneTest extends XyPlotViewUnderTest {

    private Method refectionIsOutsideComfortZone;

    public XyPlotViewComfortZoneTest() {
        super();
    }

    protected void setUp() throws Exception {
        super.setUp();

        //TODO: keep in mind to change this string aswell if method-name has changed in XyPlotView
        refectionIsOutsideComfortZone = mPlotView.getClass().getDeclaredMethod("isOutsideComfortZone", PointF.class);
        refectionIsOutsideComfortZone.setAccessible(true);
    }

    @SmallTest
    public void testComfortZoneCorners() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        assertFalse((Boolean) refectionIsOutsideComfortZone.invoke(mPlotView, mBottomLeft));
        assertFalse((Boolean) refectionIsOutsideComfortZone.invoke(mPlotView, mTopLeft));
        assertFalse((Boolean) refectionIsOutsideComfortZone.invoke(mPlotView, mTopRight));
        assertFalse((Boolean) refectionIsOutsideComfortZone.invoke(mPlotView, mBottomRight));
    }

    @SmallTest
    public void testComfortZoneOutsideTopLeft() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        float x = mTopLeft.x;
        float y = mTopLeft.y;
        assertTrue((Boolean) refectionIsOutsideComfortZone.invoke(mPlotView, new PointF(x - 0.1f, y)));
        assertTrue((Boolean) refectionIsOutsideComfortZone.invoke(mPlotView, new PointF(x + 0.1f, y)));
        assertTrue((Boolean) refectionIsOutsideComfortZone.invoke(mPlotView, new PointF(x, y + 0.1f)));
        assertTrue((Boolean) refectionIsOutsideComfortZone.invoke(mPlotView, new PointF(x, y - 0.1f)));
    }

    @SmallTest
    public void testComfortZoneOutsideBottomRight() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        float x = mBottomRight.x;
        float y = mBottomRight.y;
        assertTrue((Boolean) refectionIsOutsideComfortZone.invoke(mPlotView, new PointF(x + 0.1f, y)));
        assertTrue((Boolean) refectionIsOutsideComfortZone.invoke(mPlotView, new PointF(x - 0.1f, y)));
        assertTrue((Boolean) refectionIsOutsideComfortZone.invoke(mPlotView, new PointF(x, y - 0.1f)));
        assertTrue((Boolean) refectionIsOutsideComfortZone.invoke(mPlotView, new PointF(x, y + 0.1f)));
    }

    @SmallTest
    public void testComfortZoneInsideBottomLeft() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        assertFalse((Boolean) refectionIsOutsideComfortZone.invoke(mPlotView, new PointF(mBottomLeft.x + 0.1f, mBottomLeft.y)));
        assertFalse((Boolean) refectionIsOutsideComfortZone.invoke(mPlotView, new PointF(mBottomLeft.x, mBottomLeft.y + 0.1f)));
    }

    @SmallTest
    public void testComfortZoneInsideTopRight() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        assertFalse((Boolean) refectionIsOutsideComfortZone.invoke(mPlotView, new PointF(mTopRight.x - 0.1f, mTopRight.y)));
        assertFalse((Boolean) refectionIsOutsideComfortZone.invoke(mPlotView, new PointF(mTopRight.x, mTopRight.y - 0.1f)));
    }

    @SmallTest
    public void testComfortZoneInsideMid() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        float xMid = (mBottomRight.x + mBottomLeft.x) / 2;
        float yMid = (mTopLeft.y + mBottomLeft.y) / 2;
        assertFalse((Boolean) refectionIsOutsideComfortZone.invoke(mPlotView, new PointF(xMid, yMid)));
    }

    @SmallTest
    public void testComfortZoneOutsideZero() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        assertTrue((Boolean) refectionIsOutsideComfortZone.invoke(mPlotView, new PointF(0f, 0f)));
    }
}
