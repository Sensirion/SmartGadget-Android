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
        mPlotView.setYAxisScale(MIN_Y_AXIS_VALUE, MAX_Y_AXIS_VALUE, Y_AXIS_GRID_SIZE);
    }
}
