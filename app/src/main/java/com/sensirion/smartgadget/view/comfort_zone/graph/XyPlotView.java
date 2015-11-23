package com.sensirion.smartgadget.view.comfort_zone.graph;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;

import com.sensirion.smartgadget.R;

import butterknife.BindColor;
import butterknife.BindInt;

public class XyPlotView extends GraphView {

    private static final String TAG = XyPlotView.class.getSimpleName();

    // Points that describe the comfort-zone according to:
    // http://www.sensirion.com/nc/en/products/humidity-temperature/download-center/?cid=882&did=121&sechash=355082bc
    @NonNull
    private static final PointF[] COMFORT_ZONE_WINTER_POSITIONS = {
            new PointF(19.5f, 86.5f),
            new PointF(23.5f, 58.3f),
            new PointF(24.5f, 23.0f),
            new PointF(20.5f, 29.3f)
    };
    @NonNull
    private static final PointF[] COMFORT_ZONE_SUMMER_POSITIONS = {
            new PointF(22.5f, 79.5f),
            new PointF(26.0f, 57.3f),
            new PointF(27.0f, 19.8f),
            new PointF(23.5f, 24.4f)
    };
    //Attributes stores the correct map coordinates for the comfort zone in each station.
    @Nullable
    private static PointF[] mapCoordinatesWinter = null;

    @Nullable
    private static PointF[] mapCoordinatesSummer = null;

    @BindInt(R.integer.comfort_zone_axis_label_text_size)
    int AXIS_LABEL_TEXT_SIZE;

    @BindInt(R.integer.comfort_zone_stroke_size_boundary)
    int STROKE_SIZE_BOUNDARY;

    @BindColor(R.color.sensirion_green)
    int SENSIRION_GREEN;

    @Nullable
    private PointF[] mCzActive = new PointF[4];

    @Nullable
    private Paint mPaintComfortZone = null;

    @Nullable
    private Path mPathComfortZone = null;

    @Nullable
    private PointF mClippedPoint = null;

    private boolean mIsCzWinter = true;

    public XyPlotView(@NonNull final Context context) {
        super(context);
        init();
    }

    public XyPlotView(@NonNull final Context context,
                      @NonNull final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public XyPlotView(@NonNull final Context context,
                      @NonNull final AttributeSet attrs,
                      final int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mClippedPoint = new PointF();

        mPathComfortZone = new Path();
        mPaintComfortZone = new Paint();
        mPaintComfortZone.setStyle(Paint.Style.STROKE);

        super.setAxisLabelTextSize(AXIS_LABEL_TEXT_SIZE);

        //Sets the size of the point
        mPaintComfortZone.setStrokeWidth(STROKE_SIZE_BOUNDARY);

        mPaintComfortZone.setAntiAlias(true);
        mPaintComfortZone.setColor(SENSIRION_GREEN);

        updatePath();
    }

    //In case it's necessary, it updates the path of the comfort zone.
    private void updatePath() {
        if (mIsCzWinter) {
            updateWinterCoordinates();
        } else {
            updateSummerCoordinates();
        }
    }

    private void updateWinterCoordinates() {
        if (mapCoordinatesWinter == null) {
            mapCoordinatesWinter = obtainCoordinatesPathPoints();
            if (mapCoordinatesWinter == null) {
                return; //Not a valid path.
            }
        } else {
            Log.i(TAG, "updateWinterCoordinates -> Found winter map coordinates.");
        }
        mCzActive = mapCoordinatesWinter;

        if (mapCoordinatesWinter != null) {
            resetComfortZone();
        }
    }

    private void updateSummerCoordinates() {
        if (mapCoordinatesSummer == null) {
            mapCoordinatesSummer = obtainCoordinatesPathPoints();
            if (mapCoordinatesSummer == null) {
                return; //Not a valid path.
            }
        } else {
            Log.i(TAG, "updateSummerCoordinates -> Found summer map coordinates.");
        }
        mCzActive = mapCoordinatesSummer;
        if (mapCoordinatesSummer != null) {
            resetComfortZone();
        }
    }

    private void resetComfortZone() {
        if (mCzActive[0].x == 0) {
            Log.e(TAG, "resetComfortZone -> The graph representation is not valid.");
            return;
        }
        mPathComfortZone.reset();
        mPathComfortZone.moveTo(mCzActive[0].x, mCzActive[0].y);
        for (int i = 1; i < mCzActive.length; i++) {
            mPathComfortZone.lineTo(mCzActive[i].x, mCzActive[i].y);
        }
        mPathComfortZone.close();
        super.invalidate();
    }

    private PointF[] obtainCoordinatesPathPoints() {
        if (mIsCzWinter) {
            mCzActive = COMFORT_ZONE_WINTER_POSITIONS;
            if (mapCoordinatesWinter == null) {
                Log.v(TAG, "obtainCoordinatesPathPoints -> Obtaining the coordinates of winter.");
            } else {
                return null;
            }
        } else {
            mCzActive = COMFORT_ZONE_SUMMER_POSITIONS;
            if (mapCoordinatesSummer == null) {
                Log.v(TAG, "obtainCoordinatesPathPoints -> Obtaining the coordinates of summer");
            } else {
                return null;
            }
        }
        final PointF[] validPath = new PointF[4];
        final PointF analysedPoint = super.mapCanvasCoordinatesForComfortZone(mCzActive[0]);
        if (analysedPoint.x > 0) {
            validPath[0] = analysedPoint;
            for (int i = 1; i < 4; i++) {
                validPath[i] = super.mapCanvasCoordinatesForComfortZone(mCzActive[i]);
            }
            return validPath;
        } else {
            return null;
        }
    }

    @Override
    protected void onSizeChanged(final int width,
                                 final int height,
                                 final int oldWidth,
                                 final int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        updatePath();
    }

    @Override
    protected void onDraw(@NonNull final Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(mPathComfortZone, mPaintComfortZone);
    }

    /**
     * Toggles between the two Comfort Zones
     *
     * @param season true=winter, false=summer
     */
    public void setComfortZoneWinter(final boolean season) {
        mIsCzWinter = season;
        updatePath();
    }

    @Nullable
    public PointF getClippedPoint() {
        return mClippedPoint;
    }

    /**
     * Checks if the given PointF is inside the given
     * ComfortZone. This will only work for polygons
     * with 4 corners.
     *
     * @param p PointF with vertical and horizontal pos.
     * @return Point is in ComfortZone flag.
     */
    public boolean isOutsideComfortZone(@NonNull final PointF p) {
        /* NOTE JWI: the divisions should be performed at compile time,
                     thus this should be only 4 mul, 4 add, and 4 sub

             the numbering of the corners must look like this:
             x0,y0 -------------- x1,y1
               |                    |
               |                    |
             x3,y3 -------------- x2,y2

             unlike in this drawing, the sides need not be perpendicular
        */

        float xLine, yLine;

        // y = y1 + [(y2 - y1) / (x2 - x1)] * (x - x1)
        yLine = mCzActive[0].y + ((mCzActive[1].y - mCzActive[0].y) / (mCzActive[1].x - mCzActive[0].x)) * (p.x - mCzActive[0].x);
        if (p.y > yLine) { // above top line
            Log.v(TAG, String.format("isOutsideComfortZone -> y is above the top line of the comfort zone. | p.y = %s.", p.y));
            return true;
        }
        yLine = mCzActive[3].y + ((mCzActive[2].y - mCzActive[3].y) / (mCzActive[2].x - mCzActive[3].x)) * (p.x - mCzActive[3].x);
        if (p.y < yLine) { // below bottom line
            Log.v(TAG, String.format("isOutsideComfortZone -> y is below the bottom line of the comfort zone. | p.y = %s.", p.y));
            return true;
        }

        // x = x1 + [(x2 - x1) / (y2 - y1)] * (y - y1),
        xLine = mCzActive[0].x + ((mCzActive[3].x - mCzActive[0].x) / (mCzActive[3].y - mCzActive[0].y)) * (p.y - mCzActive[0].y);
        if (p.x < xLine) { // left of left line
            Log.v(TAG, String.format("isOutsideComfortZone -> x is on the left of the left line of the comfort zone. | p.x = %s.", p.x));
            return true;
        }
        xLine = mCzActive[1].x + ((mCzActive[2].x - mCzActive[1].x) / (mCzActive[2].y - mCzActive[1].y)) * (p.y - mCzActive[1].y);
        if (p.x > xLine) { // right of right line
            Log.v(TAG, String.format("isOutsideComfortZone -> x is on the right of the right line of the comfort zone. | p.x = %s.", p.x));
            return true;
        }
        return false;
    }

    /**
     * Checks if the given PointF is inside the given
     * Grid provided by GraphView.
     *
     * @param p PointF with vertical and horizontal pos.
     * @return Point is in Grid flag.
     */
    public boolean isOutsideGrid(@NonNull final PointF p) {
        boolean isOutsideTheGrid = false;

        if (p.x > getXAxisMax()) {
            isOutsideTheGrid = true;
            mClippedPoint.x = getXAxisMax();
            mClippedPoint.y = p.y;
            Log.v(TAG, String.format("isOutsideGrid -> Clipped outside right end at p.x = %s.", p.x));
        } else if (p.x < getXAxisMin()) {
            isOutsideTheGrid = true;
            mClippedPoint.x = getXAxisMin();
            mClippedPoint.y = p.y;
            Log.v(TAG, String.format("isOutsideGrid -> Clipped outside left end at p.x = %s.", p.x));
        }

        if (p.y > getYAxisMax()) {
            isOutsideTheGrid = true;
            mClippedPoint.x = p.x;
            mClippedPoint.y = getYAxisMax();
            Log.v(TAG, String.format("isOutsideGrid -> Clipped outside top end at p.y = %s.", p.y));
        } else if (p.y < getYAxisMin()) {
            isOutsideTheGrid = true;
            mClippedPoint.x = p.x;
            mClippedPoint.y = getYAxisMin();
            Log.v(TAG, String.format("isOutsideGrid -> Clipped outside right end at p.y = %s.", p.y));
        }
        return isOutsideTheGrid;
    }
}