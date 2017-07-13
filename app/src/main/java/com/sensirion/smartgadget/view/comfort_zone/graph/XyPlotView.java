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
package com.sensirion.smartgadget.view.comfort_zone.graph;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Region;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import com.sensirion.smartgadget.R;
import com.sensirion.smartgadget.utils.Converter;
import com.sensirion.smartgadget.utils.Settings;

public class XyPlotView extends View {

    private static final String TAG = XyPlotView.class.getSimpleName();

    private static final PointF[] COMFORT_ZONE_WINTER_POSITIONS = {
            new PointF(19.5f, 86.5f),
            new PointF(23.5f, 58.3f),
            new PointF(24.5f, 23.0f),
            new PointF(20.5f, 29.3f)
    };
    private static final PointF[] COMFORT_ZONE_SUMMER_POSITIONS = {
            new PointF(22.5f, 79.5f),
            new PointF(26.0f, 57.3f),
            new PointF(27.0f, 19.8f),
            new PointF(23.5f, 24.4f)
    };

    private int mWidth;
    private int mHeight;
    private int mGraphWidth;
    private int mGraphHeight;

    private int mGridCornerRadius;

    private int mXAxisHeight;
    private int mYAxisWidth;

    private int mLeftPadding;
    private int mRightPadding;
    private int mTopPadding;
    private int mBottomPadding;

    private Paint mBorderPaint;
    private Paint mGridPaint;
    private Paint mAxisGridPaint;
    private Paint mLabelPaint;
    private Paint mValuePaint;
    private Paint mComfortZonePaint;

    private Path mLabelPath;
    private Path mGridArea;
    private Path mComfortZone;
    private RectF mGridRect;

    private int mX0;
    private int mY0;
    private String mLabelYAxis;
    private float mYAxisMin;
    private float mYAxisMax;
    private float mYAxisGridSize;
    private String mLabelXAxis;
    private float mXAxisMin;
    private float mXAxisMax;
    private float mXAxisGridSize;
    @Nullable
    private Bitmap mBackgroundBitmap = null;
    private PointF mClippedPoint = null;

    public XyPlotView(@NonNull final Context context) {
        super(context);
        init(context);
    }

    public XyPlotView(@NonNull final Context context, @NonNull final AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public XyPlotView(@NonNull final Context context, @NonNull final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(@NonNull final Context context) {

        // Padding
        mLeftPadding = context.getResources().getInteger(R.integer.comfort_zone_plot_view_left_padding);
        mRightPadding = context.getResources().getInteger(R.integer.comfort_zone_plot_view_right_padding);
        mBottomPadding = context.getResources().getInteger(R.integer.comfort_zone_plot_view_bottom_padding);
        mTopPadding = context.getResources().getInteger(R.integer.comfort_zone_plot_view_top_padding);

        // Axis
        mXAxisHeight = context.getResources().getInteger(R.integer.comfort_zone_plot_view_x_axis_height);
        mYAxisWidth = context.getResources().getInteger(R.integer.comfort_zone_plot_view_y_axis_width);

        // Border Radius
        mGridCornerRadius = context.getResources().getInteger(R.integer.comfort_zone_grid_corner_radius);

        // Border Paint
        mBorderPaint = new Paint();
        mBorderPaint.setColor(Color.GRAY);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setStrokeWidth(getContext().getResources().getInteger(R.integer.comfort_zone_plot_stroke_width));

        // Grid Paint
        mGridPaint = new Paint();
        mGridPaint.setColor(Color.GRAY);
        mGridPaint.setStrokeWidth(getContext().getResources().getInteger(R.integer.comfort_zone_graph_view_grid_stroke_width));

        // Axis Paint
        mAxisGridPaint = new Paint();
        mAxisGridPaint.setColor(Color.GRAY);
        mAxisGridPaint.setStrokeWidth(getContext().getResources().getInteger(R.integer.comfort_zone_graph_view_grid_stroke_width));

        // Label Paint
        mLabelPaint = new Paint();
        mLabelPaint.setColor(Color.WHITE);
        mLabelPaint.setShadowLayer(3, 1, 1, Color.GRAY);
        mLabelPaint.setTextSize(getSpFor(getResources().getInteger(R.integer.comfort_zone_temperature_humidity_value_text_size_graph)));
        mLabelPaint.setAntiAlias(true);
        mLabelPaint.setFakeBoldText(true);
        mLabelPaint.setTextAlign(Align.CENTER);

        // Value Paint
        mValuePaint = new Paint();
        mValuePaint.setColor(Color.WHITE);
        mValuePaint.setShadowLayer(1, 1, 1, Color.GRAY);
        mValuePaint.setTextSize(getSpFor(getResources().getInteger(R.integer.comfort_zone_values_text_size)));
        mValuePaint.setAntiAlias(true);
        mValuePaint.setTextAlign(Align.LEFT);

        // Comfort Zone Paint
        mComfortZonePaint = new Paint();
        mComfortZonePaint.setStyle(Paint.Style.STROKE);
        mComfortZonePaint.setStrokeWidth(getResources().getInteger(R.integer.comfort_zone_stroke_size_boundary));
        mComfortZonePaint.setAntiAlias(true);
        mComfortZonePaint.setColor(getResources().getColor(R.color.sensirion_green));

        // Background
        mBackgroundBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.img_background_overlay);
        mBackgroundBitmap.setHasAlpha(true);

        // Axis
        mLabelYAxis = getResources().getString(R.string.graph_label_relative_humidity);

        // Misc.
        mLabelPath = new Path();
        mGridArea = new Path();
        mComfortZone = new Path();
        mGridRect = new RectF();
        mClippedPoint = new PointF();
    }

    public void setXAxisScale(float minValue, float maxValue, float gridSize) {
        mXAxisMin = minValue;
        mXAxisMax = maxValue;
        mXAxisGridSize = gridSize;
    }

    public void setYAxisScale(float minValue, float maxValue, float gridSize) {
        mYAxisMin = minValue;
        mYAxisMax = maxValue;
        mYAxisGridSize = gridSize;
    }

    private float getSpFor(float px) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                px, getResources().getDisplayMetrics());
    }

    public PointF coordinates(final PointF pos) {
        float posX = mX0 + ((pos.x - mXAxisMin) / (mXAxisMax - mXAxisMin)) * mGraphWidth;
        float posY = mY0 - ((pos.y - mYAxisMin) / (mYAxisMax - mYAxisMin)) * mGraphHeight;
        return new PointF(posX, posY);
    }

    public void updateComfortZone() {
        PointF[] points;
        PointF[] converted_points = new PointF[4];
        PointF[] final_points = new PointF[4];

        // Get correct points in Celsius
        if (Settings.getInstance().isSeasonWinter()) {
            points = COMFORT_ZONE_WINTER_POSITIONS;
        } else {
            points = COMFORT_ZONE_SUMMER_POSITIONS;
        }

        // Convert points to Fahrenheit if needed
        if (Settings.getInstance().isTemperatureUnitFahrenheit()) {
            for (int i = 0; i < points.length; i++) {
                float posX = Converter.convertToF(points[i].x);
                converted_points[i] = new PointF(posX, points[i].y);
            }
        } else {
            converted_points = points;
        }

        // Get coordinates for points
        for (int i = 0; i < converted_points.length; i++) {
            final_points[i] = coordinates(converted_points[i]);
        }

        // Make Path
        mComfortZone.reset();
        mComfortZone.moveTo(final_points[0].x, final_points[0].y);
        for (int i = 1; i < final_points.length; i++) {
            mComfortZone.lineTo(final_points[i].x, final_points[i].y);
        }
        mComfortZone.close();
        invalidate();
    }

    @Override
    protected void onSizeChanged(final int width, final int height, final int oldw, final int oldh) {
        super.onSizeChanged(width, height, oldw, oldh);

        mWidth = width;
        mHeight = height;

        mGraphWidth = mWidth - mLeftPadding - mRightPadding - mYAxisWidth;

        mGraphHeight = mHeight - mTopPadding - mBottomPadding - mXAxisHeight;

        mX0 = mLeftPadding + mYAxisWidth;
        mY0 = mHeight - mBottomPadding - mXAxisHeight;

        float labelXPos = (mLeftPadding - getResources().getInteger(R.integer.comfort_zone_graph_view_y_value_indicator_line_offset)) / 2f + mLabelPaint.getTextSize();
        mLabelPath.reset();
        mLabelPath.moveTo(labelXPos, height);
        mLabelPath.lineTo(labelXPos, 0);

        mGridArea.reset();
        mGridRect.set(mX0, mTopPadding, mX0 + mGraphWidth, mTopPadding + mGraphHeight);
        mGridArea.addRoundRect(mGridRect, mGridCornerRadius, mGridCornerRadius, Path.Direction.CW);

        updateComfortZone();
    }

    /*
     * Drawing
     */

    @Override
    protected void onDraw(@NonNull final Canvas canvas) {
        drawBackground(canvas);

        drawYAxis(canvas);
        drawXAxis(canvas);

        // draw border over the axis, otherwise we'll have $bordercolor pixels
        // on top of the border
        drawBorder(canvas);

        canvas.drawPath(mComfortZone, mComfortZonePaint);
    }

    private void drawBackground(@NonNull final Canvas canvas) {
        canvas.save();

        if (mBackgroundBitmap != null) {
            canvas.clipPath(mGridArea, Region.Op.INTERSECT);
            canvas.drawBitmap(mBackgroundBitmap, null, mGridRect, null);
            canvas.restore();
        }
    }

    private void drawYAxis(@NonNull final Canvas canvas) {
        int segments = Math.round((mYAxisMax - mYAxisMin) / mYAxisGridSize);
        float gridDistance = mGraphHeight / (float) segments;
        for (int i = 0; i <= segments; ++i) {
            float yOffset = i * gridDistance;

            // Grid (don't draw first and last)
            if (i > 0 && i < segments) {
                canvas.drawLine(mX0, mY0 - yOffset, mX0 + mGraphWidth, mY0 - yOffset, mGridPaint);
            }

            // Line label
            canvas.drawLine(mX0 - getResources().getInteger(R.integer.comfort_zone_graph_view_y_value_indicator_line_offset), mY0 - yOffset, mX0
                    - getResources().getInteger(R.integer.comfort_zone_graph_view_y_value_indicator_line_gap), mY0 - yOffset, mAxisGridPaint);
            float labelValue = mYAxisMin + i * mYAxisGridSize;
            canvas.drawText(String.format(getResources().getString(R.string.comfort_zone_unit_text_format), labelValue), mX0
                    - getResources().getInteger(R.integer.comfort_zone_graph_view_y_value_indicator_line_offset), mY0 - yOffset - getResources().getInteger(R.integer.comfort_zone_graph_view_y_text_offset), mValuePaint);
        }

        // Axis label
        if (mLabelYAxis != null && mLabelYAxis.length() > 0) {
            canvas.drawTextOnPath(mLabelYAxis, mLabelPath, 0, 0, mLabelPaint);
        }
    }

    private void drawXAxis(@NonNull final Canvas canvas) {
        int segments = (int) ((mXAxisMax - mXAxisMin) / mXAxisGridSize);
        boolean drawHalfSteps = segments < 10;
        if (drawHalfSteps)
            segments *= 2;

        float yOffset = mY0 + getResources().getInteger(R.integer.comfort_zone_graph_view_x_value_indicator_line_offset);
        float gridDistance = mGraphWidth / (float) segments;
        for (int i = 0; i <= segments; ++i) {
            float xOffset = i * gridDistance;

            // Grid (don't draw first and last)
            if (i > 0 && i < segments) {
                canvas.drawLine(mX0 + xOffset, mY0, mX0 + xOffset, mY0 - mGraphHeight, mGridPaint);
            }

            if (drawHalfSteps && i % 2 != 0)
                continue;

            // Line label
            canvas.drawLine(mX0 + xOffset, yOffset, mX0 + xOffset, mY0
                    + getResources().getInteger(R.integer.comfort_zone_graph_view_x_value_indicator_line_gap), mAxisGridPaint);
            float labelValue = drawHalfSteps ? (i / 2f) * mXAxisGridSize : (float) i * mXAxisGridSize;
            canvas.drawText(String.format(getResources().getString(R.string.comfort_zone_unit_text_format), mXAxisMin + labelValue), mX0 + xOffset
                    + getResources().getInteger(R.integer.comfort_zone_graph_view_x_text_offset), yOffset, mValuePaint);
        }

        // Axis label
        if (mLabelXAxis != null && mLabelXAxis.length() > 0) {
            canvas.drawText(mLabelXAxis, mWidth / 2f, (mHeight + yOffset + mLabelPaint.getTextSize()) / 2f, mLabelPaint);
        }
    }

    private void drawBorder(@NonNull final Canvas canvas) {
        canvas.save();
        canvas.clipPath(mGridArea, Region.Op.DIFFERENCE);
        canvas.drawPath(mGridArea, mBorderPaint);
        canvas.restore();
    }

    /*
     * Helper
     */

    /**
     * Checks if the given PointF is inside the given
     * Grid provided by GraphView.
     *
     * @param p PointF with vertical and horizontal pos.
     * @return Point is in Grid flag.
     */
    public boolean isOutsideGrid(@NonNull final PointF p) {
        boolean isOutsideTheGrid = false;

        if (p.x > mXAxisMax) {
            isOutsideTheGrid = true;
            mClippedPoint.x = mXAxisMax;
            mClippedPoint.y = p.y;
            Log.v(TAG, String.format("isOutsideGrid -> Clipped outside right end at p.x = %s.", p.x));
        } else if (p.x < mXAxisMin) {
            isOutsideTheGrid = true;
            mClippedPoint.x = mXAxisMin;
            mClippedPoint.y = p.y;
            Log.v(TAG, String.format("isOutsideGrid -> Clipped outside left end at p.x = %s.", p.x));
        }

        if (p.y > mYAxisMax) {
            isOutsideTheGrid = true;
            mClippedPoint.x = p.x;
            mClippedPoint.y = mYAxisMax;
            Log.v(TAG, String.format("isOutsideGrid -> Clipped outside top end at p.y = %s.", p.y));
        } else if (p.y < mYAxisMin) {
            isOutsideTheGrid = true;
            mClippedPoint.x = p.x;
            mClippedPoint.y = mYAxisMin;
            Log.v(TAG, String.format("isOutsideGrid -> Clipped outside right end at p.y = %s.", p.y));
        }
        return isOutsideTheGrid;
    }

    public float getYAxisMin() {
        return mYAxisMin;
    }

    public float getYAxisMax() {
        return mYAxisMax;
    }

    public float getXAxisMin() {
        return mXAxisMin;
    }

    public float getXAxisMax() {
        return mXAxisMax;
    }

    public void setXAxisLabel(final String label) {
        mLabelXAxis = label;
    }

    @Nullable
    public PointF getClippedPoint() {
        return mClippedPoint;
    }

}
