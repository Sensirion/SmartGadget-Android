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
