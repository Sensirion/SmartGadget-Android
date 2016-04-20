package com.sensirion.smartgadget.view.comfort_zone;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sensirion.smartgadget.R;
import com.sensirion.smartgadget.peripheral.rht_sensor.RHTSensorFacade;
import com.sensirion.smartgadget.peripheral.rht_sensor.RHTSensorListener;
import com.sensirion.smartgadget.peripheral.rht_sensor.internal.RHTInternalSensorManager;
import com.sensirion.smartgadget.utils.Converter;
import com.sensirion.smartgadget.utils.DeviceModel;
import com.sensirion.smartgadget.utils.Settings;
import com.sensirion.smartgadget.utils.view.ParentFragment;
import com.sensirion.smartgadget.view.comfort_zone.graph.XyPlotView;
import com.sensirion.smartgadget.view.comfort_zone.graph.XyPoint;

import java.text.NumberFormat;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.BindInt;
import butterknife.BindString;
import butterknife.ButterKnife;

import static com.sensirion.smartgadget.utils.XmlFloatExtractor.getFloatValueFromId;

/**
 * A fragment representing the ComfortZone view
 */
public class ComfortZoneFragment extends ParentFragment implements OnTouchListener, RHTSensorListener {

    private static final String TAG = ComfortZoneFragment.class.getSimpleName();

    @BindString(R.string.graph_label_relative_humidity)
    String GRAPH_LABEL_RELATIVE_HUMIDITY;

    @BindInt(R.integer.comfort_zone_min_y_axis_value)
    int GRAPH_MIN_Y_VALUE;

    @BindInt(R.integer.comfort_zone_max_y_axis_value)
    int GRAPH_MAX_Y_VALUE;

    @BindInt(R.integer.comfort_zone_y_axis_grid_size)
    int GRAPH_Y_GRID_SIZE;

    @BindInt(R.integer.comfort_zone_min_x_axis_value)
    int GRAPH_MIN_X_VALUE;

    @BindInt(R.integer.comfort_zone_max_x_axis_value)
    int GRAPH_MAX_X_VALUE;

    @BindInt(R.integer.comfort_zone_x_axis_grid_size_celsius)
    int GRAPH_X_GRID_SIZE_CELSIUS;

    @BindString(R.string.graph_label_temperature_celsius)
    String GRAPH_X_LABEL_CELSIUS;

    @BindString(R.string.graph_label_temperature_fahrenheit)
    String GRAPH_X_LABEL_FAHRENHEIT;

    @BindInt(R.integer.comfort_zone_plot_view_left_padding)
    int GRAPH_LEFT_PADDING;

    @BindInt(R.integer.comfort_zone_plot_view_right_padding)
    int GRAPH_RIGHT_PADDING;

    @BindInt(R.integer.comfort_zone_plot_view_bottom_padding)
    int GRAPH_BOTTOM_PADDING;

    @BindColor(R.color.sensirion_grey_dark)
    int SENSIRION_GREY_DARK;

    @Bind(R.id.plotview)
    XyPlotView mPlotView;

    @Bind(R.id.textview_left)
    TextView mTextViewLeft;

    @Bind(R.id.textview_top)
    TextView mTextViewTop;

    @Bind(R.id.textview_right)
    TextView mTextViewRight;

    @Bind(R.id.textview_bottom)
    TextView mTextViewBottom;

    @BindInt(R.integer.comfort_zone_temperature_humidity_value_text_size_graph)
    int TEMPERATURE_HUMIDITY_TEXT_SIZE_GRAPH;

    @BindInt(R.integer.comfort_zone_temperature_humidity_label_text_size)
    int TEMPERATURE_HUMIDITY_TEXT_SIZE;

    @BindInt(R.integer.comfort_zone_values_text_size)
    int GRAPH_LABEL_TEXT_SIZE;

    @Bind(R.id.tv_sensor_name)
    TextView mSensorNameTextView;

    @Bind(R.id.text_amb_temp)
    TextView mSensorAmbientTemperatureTextView;

    @Bind(R.id.text_rh)
    TextView mSensorRelativeHumidity;

    @BindString(R.string.text_sensor_name_default)
    String DEFAULT_SENSOR_NAME;

    @BindString(R.string.label_empty_t)
    String EMPTY_TEMPERATURE_STRING;

    @BindString(R.string.label_empty_rh)
    String EMPTY_RELATIVE_HUMIDITY_STRING;

    @Bind(R.id.parentframe)
    ViewGroup mParentFrame;

    @BindString(R.string.char_percent)
    String PERCENTAGE_CHARACTER;

    @BindInt(R.integer.comfort_zone_x_axis_grid_size_fahrenheit)
    int GRAPH_X_GRID_SIZE_FAHRENHEIT;

    @BindInt(R.integer.comfort_zone_plot_stroke_width)
    int GRAPH_STROKE_WIDTH;

    private Map<String, XyPoint> mActiveSensorViews;

    private boolean mIsFahrenheit;

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_comfortzone, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mActiveSensorViews = Collections.synchronizedMap(new LinkedHashMap<String, XyPoint>());
        final Activity parent = getParent();
        if (parent == null) {
            Log.e(TAG, "onViewCreated -> Received null activity");
        } else {
            initXyPlotView();
        }
    }

    private void initXyPlotView() {
        mPlotView.setYAxisLabel(GRAPH_LABEL_RELATIVE_HUMIDITY);
        mPlotView.setYAxisScale(GRAPH_MIN_Y_VALUE, GRAPH_MAX_Y_VALUE, GRAPH_Y_GRID_SIZE);
        mPlotView.setXAxisScale(GRAPH_MIN_X_VALUE, GRAPH_MAX_X_VALUE, GRAPH_X_GRID_SIZE_CELSIUS);
        mPlotView.setXAxisLabel(GRAPH_X_LABEL_CELSIUS);
        mPlotView.setCustomLeftPaddingPx(GRAPH_LEFT_PADDING);
        mPlotView.setCustomRightPaddingPx(GRAPH_RIGHT_PADDING);
        mPlotView.setCustomBottomPaddingPx(GRAPH_BOTTOM_PADDING);
        mPlotView.getBorderPaint().setShadowLayer(7, 3, 3, SENSIRION_GREY_DARK);
        mPlotView.getBorderPaint().setColor(Color.DKGRAY);
        mPlotView.getBorderPaint().setStrokeWidth(GRAPH_STROKE_WIDTH);
        mPlotView.getGridPaint().setColor(Color.GRAY);
        mPlotView.getAxisGridPaint().setColor(Color.DKGRAY);
        mPlotView.getAxisLabelPaint().setColor(Color.WHITE);
        mPlotView.getAxisLabelPaint().setShadowLayer(3, 1, 1, Color.DKGRAY);
        mPlotView.getAxisValuePaint().setColor(Color.WHITE);
        mPlotView.getAxisValuePaint().setShadowLayer(1, 1, 1, Color.DKGRAY);

        mPlotView.setAxisLabelTextSize(TEMPERATURE_HUMIDITY_TEXT_SIZE_GRAPH);
        mPlotView.getAxisValuePaint().setTextSize(TEMPERATURE_HUMIDITY_TEXT_SIZE);
        mPlotView.setAxisValueTextSize(GRAPH_LABEL_TEXT_SIZE);

        mPlotView.setBackgroundImage(R.drawable.img_background_overlay);
        final float cornerRadius = getFloatValueFromId(getContext(), R.dimen.comfort_zone_grid_corner_radius);
        mPlotView.setGridCornerRadius(getDipFor(cornerRadius));

        mTextViewLeft.bringToFront();
        mTextViewTop.bringToFront();
        mTextViewRight.bringToFront();
        mTextViewBottom.bringToFront();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume()");
        RHTSensorFacade.getInstance().registerListener(this);

        updateSensorViews();

        updateViewForSelectedSeason();
        updateViewForSelectedTemperatureUnit();
        updateTextViewName();
        touchSelectedSensorView();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "onPause()");
        RHTSensorFacade.getInstance().unregisterListener(this);
    }

    private void updateSensorViews() {
        final Activity parent = getParent();
        if (parent == null) {
            Log.e(TAG, "updateViewForSelectedSeason -> obtained null activity when calling parent.");
            return;
        }
        getParent().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                synchronized (mActiveSensorViews) {
                    for (final String key : mActiveSensorViews.keySet()) {
                        mParentFrame.removeView(mActiveSensorViews.get(key));
                    }
                    mActiveSensorViews.clear();
                }
            }
        });

        final Iterable<DeviceModel> connectedModels = RHTSensorFacade.getInstance().getConnectedSensors();

        for (final DeviceModel model : connectedModels) {
            createNewSensorViewFor(model);
        }
    }

    private void createNewSensorViewFor(@NonNull final DeviceModel model) {
        final Activity parent = getParent();
        if (parent == null) {
            Log.e(TAG, "updateViewForSelectedSeason -> obtained null activity when calling parent.");
            return;
        }
        final String address = model.getAddress();
        try {
            Log.d(TAG, String.format("createNewSensorViewFor() -> TRY address %s", address));
            final XyPoint sensorPoint = new XyPoint(getContext().getApplicationContext());
            sensorPoint.setVisibility(View.INVISIBLE);
            sensorPoint.setTag(address);
            sensorPoint.setRadius(
                    getDipFor(
                            getFloatValueFromId(getContext(), R.dimen.comfort_zone_radius_sensor_point)
                    )
            );
            sensorPoint.setOutlineRadius(
                    getDipFor(
                            getFloatValueFromId(getContext(), R.dimen.comfort_zone_radius_sensor_point) +
                                    getFloatValueFromId(getContext(), R.dimen.comfort_zone_outline_radius_offset)
                    )
            );
            sensorPoint.setInnerColor(model.getColor());
            sensorPoint.setOnTouchListener(this);
            synchronized (mActiveSensorViews) {
                mActiveSensorViews.put(address, sensorPoint);
            }
            parent.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mParentFrame.addView(sensorPoint);
                }
            });
        } catch (final IllegalArgumentException e) {
            Log.e(TAG, "createNewSensorViewFor -> The following exception was thrown: ", e);
        }
    }

    private void updateViewForSelectedSeason() {
        final Activity parent = getParent();
        if (parent == null) {
            Log.e(TAG, "updateViewForSelectedSeason -> obtained null activity when calling parent.");
            return;
        }
        final boolean isSeasonWinter = Settings.getInstance().isSeasonWinter(getContext());
        Log.i(TAG,
                String.format(
                        "updateViewForSelectedSeason(): Season %s was selected.",
                        isSeasonWinter ? "Winter" : "Summer"
                )
        );
        getParent().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPlotView.setComfortZoneWinter(isSeasonWinter);
            }
        });
    }

    @UiThread
    private void updateViewForSelectedTemperatureUnit() {
        final Activity parent = getParent();
        if (parent == null) {
            Log.e(TAG, "updateViewForSelectedTemperatureUnit -> obtained null activity when calling parent.");
            return;
        }
        final String mAxisLabel;
        final int gridSize;
        float minXAxisValue = GRAPH_MIN_X_VALUE;
        float maxXAxisValue = GRAPH_MAX_X_VALUE;

        mIsFahrenheit = Settings.getInstance().isTemperatureUnitFahrenheit(getContext());

        if (mIsFahrenheit) {
            minXAxisValue = Converter.convertToF(minXAxisValue);
            maxXAxisValue = Converter.convertToF(maxXAxisValue);
            gridSize = GRAPH_X_GRID_SIZE_FAHRENHEIT;
            mAxisLabel = GRAPH_X_LABEL_FAHRENHEIT;
            Log.d(TAG, "updateViewForSelectedTemperatureUnit -> Updating temperature unit to Fahrenheit.");
        } else {
            gridSize = GRAPH_X_GRID_SIZE_CELSIUS;
            mAxisLabel = GRAPH_X_LABEL_CELSIUS;
            Log.d(TAG, "updateViewForSelectedTemperatureUnit -> Updating temperature unit to Celsius.");
        }

        mPlotView.setXAxisLabel(mAxisLabel);
        mPlotView.setXAxisScale(minXAxisValue, maxXAxisValue, gridSize);

        parent.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPlotView.invalidate();
            }
        });
    }

    private void touchSelectedSensorView() {
        if (isAdded()) {
            final String selectedAddress = Settings.getInstance().getSelectedAddress();
            if (selectedAddress == Settings.SELECTED_NONE) {
                return;
            }
            final XyPoint point = mActiveSensorViews.get(selectedAddress);
            if (point == null) {
                Log.e(TAG,
                        String.format(
                                "touchSelectedSensorView() -> could not find XyPoint for address: %s",
                                selectedAddress
                        )
                );
            } else {
                selectSensor(selectedAddress);
                final Activity parent = getParent();
                if (parent == null) {
                    Log.e(TAG, "touchSelectedSensorView -> obtained null activity when calling parent.");
                    return;
                }
                getParent().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        point.animateTouch();
                    }
                });
            }
        }
    }

    @Override
    public boolean onTouch(@NonNull final View view, @NonNull final MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (view instanceof XyPoint) {
                selectSensor(view.getTag().toString());
                final Activity parent = getParent();
                if (parent == null) {
                    Log.e(TAG, "onTouch -> obtained null activity when calling parent.");
                    return false;
                }
                getParent().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((XyPoint) view).animateTouch();
                    }
                });
            }
        }
        return view.performClick();
    }

    private void selectSensor(final String selectedAddress) {
        for (final XyPoint point : mActiveSensorViews.values()) {
            point.setOutlineColor(Color.TRANSPARENT);
            point.postInvalidate();
        }
        XyPoint selectedPoint = mActiveSensorViews.get(selectedAddress);
        if (selectedPoint != null) {
            Settings.getInstance().setSelectedAddress(selectedAddress);
            selectedPoint.setOutlineColor(Color.WHITE);
            selectedPoint.postInvalidate();
            updateTextViewName();
        } else {
            Log.e(TAG, "selectSensor(): no selected address found: " + selectedAddress);
        }
    }

    private void updateTextViewName() {
        try {
            final String selectedAddress = Settings.getInstance().getSelectedAddress();
            final DeviceModel model;
            if (selectedAddress == null) {
                model = null;
            } else {
                model = RHTSensorFacade.getInstance().getDeviceModel(selectedAddress);
            }
            if (model == null) {
                synchronized (mActiveSensorViews) {
                    mActiveSensorViews.remove(selectedAddress);
                }
                return;
            }
            final Activity parent = getParent();
            if (parent == null) {
                Log.e(TAG, "updateTextViewName -> obtained null activity when calling parent.");
                return;
            }
            parent.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    XyPoint selectedPoint = mActiveSensorViews.get(selectedAddress);
                    if (selectedPoint != null) {
                        mSensorNameTextView.setTextColor(selectedPoint.getInnerColor());
                    } else {
                        Log.e(TAG,
                                String.format(
                                        "updateTextViewName() -> mActiveSensorViews does not contain selected address: %s",
                                        selectedAddress
                                )
                        );
                        mSensorNameTextView.setTextColor(model.getColor());
                    }
                    mSensorNameTextView.setText(model.getUserDeviceName());
                }
            });
        } catch (final IllegalArgumentException e) {
            Log.e(TAG, "updateTextViewName(): The following exception was produced -> ", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onGadgetConnectionChanged(@NonNull final String deviceAddress,
                                          final boolean deviceIsConnected) {
        if (isAdded()) {
            if (deviceIsConnected) {
                Log.d(TAG,
                        String.format(
                                "onGadgetConnectionChanged() -> Device %s was connected.",
                                deviceAddress
                        )
                );
            } else {
                Log.d(TAG,
                        String.format(
                                "onGadgetConnectionChanged() -> Device %s was disconnected. ",
                                deviceAddress
                        )
                );
                if (getView() == null) {
                    throw new NullPointerException(
                            String.format(
                                    "%s: onGadgetConnectionChanged -> It was impossible to obtain the view.",
                                    TAG
                            )
                    );
                }
                removeSensorView(deviceAddress);
                if (RHTSensorFacade.getInstance().hasConnectedDevices()) {
                    touchSelectedSensorView();
                } else {
                    final Activity parent = getParent();
                    if (parent == null) {
                        Log.e(TAG, "onGadgetConnectionChanged -> Received null activity.");
                        return;
                    }
                    parent.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mSensorNameTextView.setText(DEFAULT_SENSOR_NAME);
                            mSensorAmbientTemperatureTextView.setText(EMPTY_TEMPERATURE_STRING);
                            mSensorRelativeHumidity.setText(EMPTY_RELATIVE_HUMIDITY_STRING);
                        }
                    });
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onNewRHTSensorData(final float temperature,
                                   final float relativeHumidity,
                                   @Nullable final String deviceAddress) {
        if (deviceAddress == null) {
            updateViewValues(
                    RHTInternalSensorManager.INTERNAL_SENSOR_ADDRESS,
                    temperature,
                    relativeHumidity
            );
        } else {
            updateViewValues(deviceAddress, temperature, relativeHumidity);
        }
    }

    private void removeSensorView(final String deviceAddress) {
        synchronized (mActiveSensorViews) {
            if (mActiveSensorViews.containsKey(deviceAddress)) {
                if (getView() == null) {
                    throw new NullPointerException(
                            String.format(
                                    "%s: removeSensorView -> It was impossible to obtain the view.",
                                    TAG
                            )
                    );
                }
                Log.i(TAG,
                        String.format(
                                "removeSensorView() -> The view from address %s was removed.",
                                deviceAddress
                        )
                );

                final Activity parent = getParent();
                if (parent == null) {
                    Log.e(TAG, "removeSensorView() -> Obtained null when calling the activity.");
                    return;
                }
                final XyPoint stalePoint = mActiveSensorViews.get(deviceAddress);
                parent.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mParentFrame.removeView(stalePoint);
                    }
                });
                mActiveSensorViews.remove(deviceAddress);
            }
        }
    }

    public void updateViewValues(@NonNull final String address,
                                 final float temperature,
                                 final float relativeHumidity) {
        if (isAdded()) {
            final Activity parent = getParent();
            if (parent == null) {
                Log.e(TAG, "updateViewValues() -> Obtained null when calling the activity.");
                return;
            }

            if (address != RHTInternalSensorManager.INTERNAL_SENSOR_ADDRESS &&
                !mActiveSensorViews.containsKey(address)) {
                Log.w(TAG, String.format(
                                "updateViewValues() -> Received value from inactive device %s. Updating views.",
                                address
                        )
                );
                updateSensorViews();
            }
            parent.runOnUiThread(new Runnable() {
                float newTemperature = temperature;
                String unit;

                @Override
                public void run() {
                    Log.v(TAG,
                            String.format(
                                    "updateViewValues(): address = %s | temperature = %f | relativeHumidity = %f",
                                    address,
                                    temperature,
                                    relativeHumidity
                            )
                    );
                    if (mIsFahrenheit) {
                        newTemperature = Converter.convertToF(temperature);
                        unit = getString(R.string.unit_fahrenheit);
                    } else {
                        newTemperature = temperature;
                        unit = getString(R.string.unit_celsius);
                    }
                    updateTextViewRHT(address, newTemperature, relativeHumidity, unit);

                    final PointF newPos = new PointF(newTemperature, relativeHumidity);
                    boolean isClipped = false;
                    if (mPlotView.isOutsideComfortZone(newPos)) {
                        if (mPlotView.isOutsideGrid(newPos)) {
                            isClipped = true;
                        }
                    }
                    final XyPoint selectedPoint = mActiveSensorViews.get(address);
                    if (selectedPoint != null) {
                        updateViewPositionFor(selectedPoint, newPos, isClipped);
                    }
                }
            });
        }
    }

    private void updateTextViewRHT(@NonNull final String address,
                                   final float temperature,
                                   final float humidity,
                                   final String unit) {
        if (address.equals(Settings.getInstance().getSelectedAddress())) {
            if (getView() == null) {
                throw new NullPointerException(
                        String.format("%s: updateTextViewRHT -> It was impossible to obtain the view.",
                                TAG
                        )
                );
            }
            final NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
            nf.setMaximumFractionDigits(1);
            nf.setMinimumFractionDigits(1);
            mSensorAmbientTemperatureTextView.setText(nf.format(temperature) + unit);
            mSensorRelativeHumidity.setText(String.format("%s%sRH", nf.format(humidity), PERCENTAGE_CHARACTER));
        }
    }

    private void updateViewPositionFor(@NonNull final XyPoint selectedPoint,
                                       @NonNull final PointF p,
                                       final boolean isClipped) {
        final PointF canvasPosition;
        if (isClipped) {
            final PointF clippedPoint = mPlotView.getClippedPoint();
            if (clippedPoint == null) {
                Log.e(TAG, "updateViewPositionFor -> Cannot obtain the clipped point");
                return;
            } else {
                canvasPosition = mPlotView.mapCanvasCoordinatesFor(mPlotView.getClippedPoint());
            }
        } else {
            canvasPosition = mPlotView.mapCanvasCoordinatesFor(p);
        }
        animateSensorViewPointTo(selectedPoint, canvasPosition.x, canvasPosition.y);
    }

    private void animateSensorViewPointTo(@NonNull final XyPoint selectedPoint, final float x, final float y) {
        final Activity parent = getParent();
        if (parent == null) {
            Log.e(TAG, "animateSensorViewPointTo() -> Obtained null when calling the activity.");
            return;
        }
        final float relativeX =
                x - (getDipFor(getFloatValueFromId(parent, R.dimen.comfort_zone_radius_sensor_point) +
                        getFloatValueFromId(parent, R.dimen.comfort_zone_outline_radius_offset)));
        final float relativeY =
                y - (getDipFor(getFloatValueFromId(parent, R.dimen.comfort_zone_radius_sensor_point) +
                        getFloatValueFromId(parent, R.dimen.comfort_zone_outline_radius_offset)));
        parent.runOnUiThread(new Runnable() {
            @Override
            @UiThread
            public void run() {
                selectedPoint.animateMove(relativeX, relativeY);
            }
        });
    }

    private float getDipFor(final float px) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, getResources().getDisplayMetrics());
    }
}