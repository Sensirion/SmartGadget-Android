package com.sensirion.smartgadget.view.comfort_zone.graph;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Shader;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.sensirion.smartgadget.R;
import com.sensirion.smartgadget.utils.Converter;

import static com.sensirion.smartgadget.utils.XmlFloatExtractor.getFloatValueFromId;

public class GraphView extends View {

    private static final String TAG = GraphView.class.getSimpleName();
    protected int mWidth;
    @Nullable
    private GestureDetector mGestureDetector;
    @Nullable
    private ScaleGestureDetector mScaleDetector;
    private float mGridCornerRadius;
    private int mCustomLeftPaddingPx = -1;
    private int mCustomRightPaddingPx = -1;
    private int mCustomTopPaddingPx = -1;
    private int mCustomBottomPaddingPx = -1;
    private int mTextOnPathVoffsetYAxis = 0;
    private Paint mBackgroundPaint;
    private Paint mBorderPaint;
    private Paint mGridPaint;
    private Paint mAxisGridPaint;
    private Paint mLabelPaint;
    private Path mGridArea;
    private int mHeight;
    private int mLeftPadding;
    private int mRightPadding;
    private int mTopPadding;
    private int mBottomPadding;
    private int mGraphWidth;
    private int mGraphHeight;
    private int mX0;
    private int mY0;
    private String mLabelYAxis;
    private float mYAxisDefaultMin;
    private float mYAxisDefaultMax;
    private float mYAxisMin;
    private float mYAxisMax;
    private float mYAxisGridSize;
    private final GestureDetector.SimpleOnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            setYAxisScaleInternal(mYAxisDefaultMin, mYAxisDefaultMax, mYAxisGridSize);
            return true;
        }
    };
    private Path mLabelPath;
    private Paint mValuePaint;
    private String mLabelXAxis;
    private float mXAxisMin;
    private float mXAxisMax;
    private float mXAxisGridSize;
    private boolean mUseBackgroundGradient = false;
    private int mBackgroundGradientTop = 0x00000000;
    private int mBackgroundGradientBottom = 0x00000000;
    @Nullable
    private Bitmap mBackgroundBitmap = null;
    private RectF mGridRect;
    private boolean mTouchEnabled = false;
    private boolean mMotionEventOngoing = false;
    private final ScaleGestureDetector.OnScaleGestureListener mScaleGestureListener = new ScaleGestureDetector.OnScaleGestureListener() {
        @SuppressLint("NewApi")
        @Override
        public boolean onScale(@NonNull ScaleGestureDetector detector) {
            float factorY = detector.getScaleFactor();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                float prevSpanY = Math.abs(detector.getPreviousSpanY());
                float currSpanY = Math.abs(detector.getCurrentSpanY());

                if (prevSpanY > getResources().getInteger(R.integer.comfort_zone_span_threshold_y_px)
                        && currSpanY > getResources().getInteger(R.integer.comfort_zone_span_threshold_y_px)
                        && Math.abs(prevSpanY - currSpanY) > getResources().getInteger(R.integer.comfort_zone_diff_threshold_y_px)) {
                    factorY = currSpanY / prevSpanY;
                } else {
                    factorY = 1;
                }
            }

            float graphSpan2 = (mYAxisMax - mYAxisMin) / 2;
            float middle = graphSpan2 + mYAxisMin;

            graphSpan2 /= factorY;

            float newMin = middle - graphSpan2;
            float newMax = middle + graphSpan2;

            if (newMin > mYAxisDefaultMin) {
                mYAxisMin = newMin;
            }
            if (newMax < mYAxisDefaultMax) {
                mYAxisMax = newMax;
            }

            // call setYAxisScaleInternal to recalculate grid
            setYAxisScaleInternal(mYAxisMin, mYAxisMax, -1);
            invalidate();

            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            mMotionEventOngoing = true;
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            mMotionEventOngoing = false;
        }
    };
    private float mMotionEventLastY = 0;

    public GraphView(@NonNull final Context context) {
        super(context);
        init(context);
    }

    public GraphView(@NonNull final Context context, @NonNull final AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GraphView(@NonNull final Context context, @NonNull final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(@NonNull final Context context) {
        mGridCornerRadius = getFloatValueFromId(context, R.dimen.comfort_zone_graph_view_grid_corner_radius);

        mBorderPaint = new Paint();
        mBorderPaint.setColor(0xff999999);
        setBorderPaint(mBorderPaint);

        mBackgroundPaint = new Paint();
        setBackgroundPaint(mBackgroundPaint);

        mGridPaint = new Paint();
        mGridPaint.setColor(Color.WHITE);
        setGridPaint(mGridPaint);

        mAxisGridPaint = new Paint();
        mAxisGridPaint.setColor(Color.WHITE);
        setAxisGridPaint(mAxisGridPaint);

        mLabelPaint = new Paint();
        mLabelPaint.setColor(Color.WHITE);
        setAxisLabelPaint(mLabelPaint);

        mValuePaint = new Paint();
        mValuePaint.setColor(Color.WHITE);
        setAxisValuePaint(mValuePaint);

        mLabelPath = new Path();
        mGridArea = new Path();

        mGridRect = new RectF();

        // default values for axis scales
        setXAxisScale(0, 100, 20);
        setYAxisScale(0, 100, 20);

        mGestureDetector = new GestureDetector(context, mGestureListener);
        mScaleDetector = new ScaleGestureDetector(context, mScaleGestureListener);
    }

    // This axis is always going to be time. All values are assumed to be in
    // minutes. maxValue is always assumed to be "now"
    public void setXAxisScale(float minValue, float maxValue, float gridSize) {
        mXAxisMin = minValue;
        mXAxisMax = maxValue;
        mXAxisGridSize = gridSize;
    }

    public void setYAxisScale(float minValue, float maxValue, float gridSize) {
        mYAxisDefaultMin = minValue;
        mYAxisDefaultMax = maxValue;

        setYAxisScaleInternal(minValue, maxValue, gridSize);
    }

    @SuppressWarnings("unused")
    private void setYAxisScaleInternal(float minValue, float maxValue, float gridSize) {
        mYAxisMin = minValue;
        mYAxisMax = maxValue;

        float delta = (mYAxisMax - mYAxisMin) / 2;
        mYAxisGridSize = (float) Math.pow(10, (int) Math.log10(delta));
    }

    public Paint getAxisLabelPaint() {
        return mLabelPaint;
    }

    /**
     * set the paint used to draw the label text
     */
    public void setAxisLabelPaint(final Paint p) {
        mLabelPaint = p;
        //Writes the labels with a size depending of the size of the mobile device.

        mLabelPaint.setTextSize(getResources().getInteger(R.integer.comfort_zone_label_text_size_in_graph));
        mLabelPaint.setAntiAlias(true);
        mLabelPaint.setFakeBoldText(true);
        mLabelPaint.setTextAlign(Align.CENTER);
    }

    /**
     * set the text size used to draw the label text
     */
    public void setAxisLabelTextSize(final float textSize) {
        mLabelPaint.setTextSize(getSpFor(textSize));
    }

    private float getSpFor(float px) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                px, getResources().getDisplayMetrics());
    }

    @SuppressWarnings("unused")
    public Paint getBackgroundPaint() {
        return mBackgroundPaint;
    }

    /**
     * set the paint used to draw the inner graph background
     */
    public void setBackgroundPaint(Paint p) {
        mBackgroundPaint = p;
        mBackgroundPaint.setAntiAlias(true);
        mBackgroundPaint.setStyle(Paint.Style.FILL);
    }

    public Paint getBorderPaint() {
        return mBorderPaint;
    }

    /**
     * set the paint used to draw the graph border
     */
    public void setBorderPaint(final Paint p) {
        mBorderPaint = p;
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setStrokeWidth(getFloatValueFromId(getContext(), R.dimen.comfort_zone_graph_view_border_stroke_width));
    }

    public Paint getGridPaint() {
        return mGridPaint;
    }

    /**
     * set the paint used to draw the (inner) grid
     */
    public void setGridPaint(final Paint p) {
        mGridPaint = p;
        mGridPaint.setStrokeWidth(getFloatValueFromId(getContext(), R.dimen.comfort_zone_graph_view_grid_stroke_width));
    }

    public Paint getAxisGridPaint() {
        return mAxisGridPaint;
    }

    /**
     * set the paint used to draw the grid lines next to the graph (axis)
     */
    public void setAxisGridPaint(final Paint p) {
        mAxisGridPaint = p;
        mAxisGridPaint.setStrokeWidth(getFloatValueFromId(getContext(), R.dimen.comfort_zone_graph_view_grid_stroke_width));
    }

    // -- end of styling

    public Paint getAxisValuePaint() {
        return mValuePaint;
    }

    /**
     * set the paint used to draw the axis values
     */
    public void setAxisValuePaint(final Paint p) {
        mValuePaint = p;
        mValuePaint.setTextSize(R.integer.comfort_zone_label_text_size_in_graph);
        mValuePaint.setAntiAlias(true);
        mValuePaint.setTextAlign(Align.LEFT);
    }

    /**
     * set the text size used to draw the axis values
     */
    public void setAxisValueTextSize(final float textSize) {
        mValuePaint.setTextSize(getSpFor(textSize));
    }

    /**
     * set the graph area corner radius
     */
    public void setGridCornerRadius(final float r) {
        mGridCornerRadius = r;
    }

    /**
     * set a two color gradient for the inner background
     */
    @SuppressWarnings("unused")
    public void setBackgroundGradient(final int colorTop, final int colorBottom) {
        mBackgroundGradientTop = colorTop;
        mBackgroundGradientBottom = colorBottom;
        mUseBackgroundGradient = true;
    }

    /**
     * set a background image inside grid
     *
     * @param resId Resource Id of the image
     */
    public void setBackgroundImage(@DrawableRes final int resId) {
        mBackgroundBitmap = BitmapFactory.decodeResource(getContext().getResources(), resId);
        mBackgroundBitmap.setHasAlpha(true);
        mBackgroundPaint.setColor(Color.TRANSPARENT); // keep bitmap clean
    }

    public void setCustomLeftPaddingPx(final int leftPadding) {
        mCustomLeftPaddingPx = leftPadding;
    }

    public void setCustomRightPaddingPx(final int rightPadding) {
        mCustomRightPaddingPx = rightPadding;
    }

    @SuppressWarnings("unused")
    public void setCustomTopPaddingPx(final int topPadding) {
        mCustomTopPaddingPx = topPadding;
    }

    public void setCustomBottomPaddingPx(final int bottomPadding) {
        mCustomBottomPaddingPx = bottomPadding;
    }

    @SuppressWarnings("unused")
    public void setTextOnPathVoffsetYAxis(final int vOffset) {
        mTextOnPathVoffsetYAxis = vOffset;
    }

    @NonNull
    public PointF mapCanvasCoordinatesFor(@NonNull final PointF pos) {
        float px = mX0 + ((pos.x - mXAxisMin) / (mXAxisMax - mXAxisMin)) * mGraphWidth;
        float py = mY0 - ((pos.y - mYAxisMin) / (mYAxisMax - mYAxisMin)) * mGraphHeight;
        return new PointF(px, py);
    }

    //Solution for comfort zone wrong temperature unit problem.
    @NonNull
    public PointF mapCanvasCoordinatesForComfortZone(@NonNull final PointF pos) {
        float positionX = pos.x;
        float px;

        if (mXAxisMin < getResources().getInteger(R.integer.comfort_zone_limit_celsius_fahrenheit)) {
            Log.d(TAG, "mapCanvasCoordinatesForComfortZone -> Preparing the comfort zone in Celsius.");
            px = mX0 + ((positionX - getResources().getInteger(R.integer.comfort_zone_graph_width_celsius)) / getResources().getInteger(R.integer.comfort_zone_temperature_difference_celsius)) * mGraphWidth;
        } else {
            Log.d(TAG, "mapCanvasCoordinatesForComfortZone -> Preparing the comfort zone in Fahrenheit.");
            if (positionX < getResources().getInteger(R.integer.comfort_zone_limit_celsius_fahrenheit)) {
                positionX = Converter.convertToF(positionX);
            }
            px = mX0 + ((positionX - getResources().getInteger(R.integer.comfort_zone_graph_width_fahrenheit)) / getResources().getInteger(R.integer.comfort_zone_temperature_difference_fahrenheit)) * mGraphWidth;
        }

        float py = mY0 - ((pos.y - mYAxisMin) / (mYAxisMax - mYAxisMin)) * mGraphHeight;

        return new PointF(px, py);
    }

    @SuppressWarnings("unused")
    public String getLabelYAxis() {
        return mLabelYAxis;
    }

    public float getYAxisMin() {
        return mYAxisMin;
    }

    public float getYAxisMax() {
        return mYAxisMax;
    }

    @SuppressWarnings("unused")
    public float getYAxisGridSize() {
        return mYAxisGridSize;
    }

    @SuppressWarnings("unused")
    public String getLabelXAxis() {
        return mLabelXAxis;
    }

    public float getXAxisMin() {
        return mXAxisMin;
    }

    public float getXAxisMax() {
        return mXAxisMax;
    }

    @SuppressWarnings("unused")
    public float getXAxisGridSize() {
        return mXAxisGridSize;
    }

    public void setYAxisLabel(final String label) {
        mLabelYAxis = label;
    }

    // -------------------------------------------------------------------------
    // the scaling is lifted from libgraph

    public void setXAxisLabel(final String label) {
        mLabelXAxis = label;
    }

    @SuppressWarnings("unused")
    public void setTouchEnabled(final boolean enabled) {
        mTouchEnabled = enabled;
    }

    @Override
    public boolean onTouchEvent(@NonNull final MotionEvent event) {
        if (!mTouchEnabled) {
            return super.onTouchEvent(event);
        }
        if (mGestureDetector == null) {
            Log.w(TAG, "onTouchEvent -> Gesture detector is null.");
        } else if (mGestureDetector.onTouchEvent(event)) {
            return true;
        }

        if (mScaleDetector == null) {
            Log.w(TAG, "onTouchEvent -> Scale detector is null.");
        } else {
            mScaleDetector.onTouchEvent(event);
            if (mScaleDetector.isInProgress()) {
                return true;
            }
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mMotionEventOngoing = true;
                mMotionEventLastY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                mMotionEventOngoing = false;
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                if (mMotionEventOngoing) {
                    float delta = (mMotionEventLastY - event.getY()) / (float) mGraphHeight
                            * (mYAxisMax - mYAxisMin);
                    if (mYAxisMin - delta >= mYAxisDefaultMin
                            && mYAxisMax - delta <= mYAxisDefaultMax) {
                        setYAxisScaleInternal(mYAxisMin - delta, mYAxisMax - delta, -1);
                        invalidate();
                    }
                }
                mMotionEventLastY = event.getY();
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    protected void onSizeChanged(final int width, final int height, final int oldw, final int oldh) {
        super.onSizeChanged(width, height, oldw, oldh);

        mWidth = width;
        mHeight = height;

        if (mCustomLeftPaddingPx >= 0) {
            mLeftPadding = (int) ((mCustomLeftPaddingPx / (float) getResources().getInteger(R.integer.comfort_zone_design_width_px)) * mWidth);
        } else {
            mLeftPadding = (int) ((getResources().getInteger(R.integer.comfort_zone_design_left_padding_px) / (float) getResources().getInteger(R.integer.comfort_zone_design_width_px)) * mWidth);
        }
        if (mCustomRightPaddingPx >= 0) {
            mRightPadding = (int) ((mCustomRightPaddingPx / (float) getResources().getInteger(R.integer.comfort_zone_design_width_px)) * mWidth);
        } else {
            mRightPadding = (int) ((getResources().getInteger(R.integer.comfort_zone_design_right_padding_px) / (float) getResources().getInteger(R.integer.comfort_zone_design_width_px)) * mWidth);
        }
        if (mCustomTopPaddingPx >= 0) {
            mTopPadding = (int) ((mCustomTopPaddingPx / (float) getResources().getInteger(R.integer.comfort_zone_design_height_px)) * mHeight);
        } else {
            mTopPadding = (int) ((getResources().getInteger(R.integer.comfort_zone_top_padding_px) / (float) getResources().getInteger(R.integer.comfort_zone_design_height_px)) * mHeight);
        }
        if (mCustomBottomPaddingPx >= 0) {
            mBottomPadding = (int) (((getResources().getInteger(R.integer.extra_padding_bottom_graph) + mCustomBottomPaddingPx) / (float) getResources().getInteger(R.integer.comfort_zone_design_height_px)) * mHeight);
        } else {
            mBottomPadding = (int) ((getResources().getInteger(R.integer.comfort_zone_bottom_padding_px) / (float) getResources().getInteger(R.integer.comfort_zone_design_height_px)) * mHeight);
        }

        mGraphWidth = mWidth - mLeftPadding - mRightPadding;

        mGraphHeight = mHeight - mTopPadding - mBottomPadding;

        mX0 = mLeftPadding;
        mY0 = mHeight - mBottomPadding;

        if (mUseBackgroundGradient) {
            Shader gradientShader = new LinearGradient(mX0, mY0, mX0, mY0 - mGraphHeight,
                    mBackgroundGradientBottom, mBackgroundGradientTop, Shader.TileMode.CLAMP);
            mBackgroundPaint.setShader(gradientShader);
        }

        float labelXPos = ((66 / (float) 1280) * mWidth);

        mLabelPath.reset();
        mLabelPath.moveTo(labelXPos, height);
        mLabelPath.lineTo(labelXPos, 0);

        mGridArea.reset();
        mGridRect.set(mLeftPadding, mTopPadding, mWidth - mRightPadding, mHeight - mBottomPadding);
        mGridArea.addRoundRect(mGridRect, mGridCornerRadius, mGridCornerRadius, Path.Direction.CW);
    }

    @Override
    protected void onDraw(@NonNull final Canvas canvas) {
        drawBackground(canvas);

        drawYAxis(canvas);
        drawXAxis(canvas);

        // draw border over the axis, otherwise we'll have $bordercolor pixels
        // on top of the border
        drawBorder(canvas);
    }

    private void drawBackground(@NonNull final Canvas canvas) {
        canvas.drawPath(mGridArea, mBackgroundPaint);
        canvas.save();

        if (mBackgroundBitmap != null) {
            canvas.clipPath(mGridArea, Region.Op.INTERSECT);
            canvas.drawBitmap(mBackgroundBitmap, null, mGridRect, null);
            canvas.restore();
        }
    }

    private void drawYAxis(@NonNull final Canvas canvas) {
        // label
        if (mLabelYAxis != null && mLabelYAxis.length() > 0) {
            canvas.drawTextOnPath(mLabelYAxis, mLabelPath, 0, mTextOnPathVoffsetYAxis, mLabelPaint);
        }

        // grid
        int segments = Math.round((mYAxisMax - mYAxisMin) / mYAxisGridSize);
        float gridDistance = mGraphHeight / (float) segments;
        for (int i = 0; i <= segments; ++i) {
            float yOffset = i * gridDistance;

            canvas.drawLine(mX0 - getResources().getInteger(R.integer.comfort_zone_graph_view_y_value_indicator_line_offset), mY0 - yOffset, mX0
                    - getResources().getInteger(R.integer.comfort_zone_graph_view_y_value_indicator_line_gap), mY0 - yOffset, mAxisGridPaint);


            canvas.drawText(String.format(getResources().getString(R.string.comfort_zone_unit_text_format), mYAxisMin + i * mYAxisGridSize), mX0
                    - getResources().getInteger(R.integer.comfort_zone_graph_view_y_value_indicator_line_offset), mY0 - yOffset - getResources().getInteger(R.integer.comfort_zone_graph_view_y_text_offset), mValuePaint);

            // don't draw for first and last
            if (i > 0 && i < segments) {
                canvas.drawLine(mX0, mY0 - yOffset, mX0 + mGraphWidth, mY0 - yOffset, mGridPaint);
            }
        }
    }

    private void drawXAxis(@NonNull final Canvas canvas) {

        // label
        if (mLabelXAxis != null && mLabelXAxis.length() > 0) {
            if (getResources().getBoolean(R.bool.is_tablet)) { //Dirty solution for solving X axis bug.
                canvas.drawText(mLabelXAxis, mWidth / 2f, mHeight - (mBottomPadding + 46) / 4f, mLabelPaint);
            } else {
                canvas.drawText(mLabelXAxis, mWidth / 2f, mHeight - mBottomPadding / 4f, mLabelPaint);
            }
        }

        int segments = (int) ((mXAxisMax - mXAxisMin) / mXAxisGridSize);
        boolean drawHalfSteps = segments < 10;
        if (drawHalfSteps)
            segments *= 2;

        float gridDistance = mGraphWidth / (float) segments;
        for (int i = 0; i <= segments; ++i) {
            float xOffset = i * gridDistance;

            // don't draw for first and last
            if (i > 0 && i < segments) {
                canvas.drawLine(mX0 + xOffset, mY0, mX0 + xOffset, mY0 - mGraphHeight, mGridPaint);
            }

            if (drawHalfSteps && i % 2 != 0)
                continue;

            canvas.drawLine(mX0 + xOffset, mY0 + getResources().getInteger(R.integer.comfort_zone_graph_view_x_value_indicator_line_offset), mX0 + xOffset, mY0
                    + getResources().getInteger(R.integer.comfort_zone_graph_view_x_value_indicator_line_gap), mAxisGridPaint);
            float labelValue = drawHalfSteps ? (i / 2f) * mXAxisGridSize : (float) i * mXAxisGridSize;

            canvas.drawText(String.format(getResources().getString(R.string.comfort_zone_unit_text_format), mXAxisMin + labelValue), mX0 + xOffset
                    + getResources().getInteger(R.integer.comfort_zone_graph_view_x_text_offset), mY0 + getResources().getInteger(R.integer.comfort_zone_graph_view_x_value_indicator_line_offset), mValuePaint);
        }
    }

    private void drawBorder(@NonNull final Canvas canvas) {
        canvas.save();
        canvas.clipPath(mGridArea, Region.Op.DIFFERENCE);
        canvas.drawPath(mGridArea, mBorderPaint);
        canvas.restore();
    }
}
