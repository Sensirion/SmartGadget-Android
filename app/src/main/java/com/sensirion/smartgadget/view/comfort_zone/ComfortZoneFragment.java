package com.sensirion.smartgadget.view.comfort_zone;

import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import static com.sensirion.smartgadget.utils.XmlFloatExtractor.getFloatValueFromId;

/**
 * A fragment representing the ComfortZone view
 */
public class ComfortZoneFragment extends ParentFragment implements OnTouchListener, RHTSensorListener {

    private static final String TAG = ComfortZoneFragment.class.getSimpleName();

    private XyPlotView mPlotView;

    private Map<String, XyPoint> mActiveSensorViews;
    private boolean mIsFahrenheit;

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_comfortzone, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mActiveSensorViews = new LinkedHashMap<>();
        initXyPlotView();
    }

    private void initXyPlotView() {
        if (getView() == null) {
            throw new NullPointerException(String.format("%s: initXyPlotView -> It was impossible to obtain the view.", TAG));
        }
        getParent().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPlotView = (XyPlotView) getView().findViewById(R.id.plotview);
                mPlotView.setYAxisLabel(getString(R.string.graph_label_relative_humidity));
                mPlotView.setYAxisScale(getResources().getInteger(R.integer.comfort_zone_min_y_axis_value), getResources().getInteger(R.integer.comfort_zone_max_y_axis_value), getResources().getInteger(R.integer.comfort_zone_y_axis_grid_size));
                mPlotView.setXAxisScale(getResources().getInteger(R.integer.comfort_zone_min_x_axis_value), getResources().getInteger(R.integer.comfort_zone_max_x_axis_value), getResources().getInteger(R.integer.comfort_zone_x_axis_grid_size_celsius));
                mPlotView.setXAxisLabel(getString(R.string.graph_label_temperature_celsius));
                mPlotView.setCustomLeftPaddingPx(getResources().getInteger(R.integer.comfort_zone_plot_view_left_padding));
                mPlotView.setCustomRightPaddingPx(getResources().getInteger(R.integer.comfort_zone_plot_view_right_padding));
                mPlotView.setCustomBottomPaddingPx(getResources().getInteger(R.integer.comfort_zone_plot_view_bottom_padding));
                mPlotView.getBorderPaint().setShadowLayer(7, 3, 3, getResources().getColor(R.color.sensirion_grey_dark));
                mPlotView.getBorderPaint().setColor(Color.DKGRAY);
                mPlotView.getBorderPaint().setStrokeWidth(getResources().getInteger(R.integer.comfort_zone_plot_stroke_width));
                mPlotView.getGridPaint().setColor(Color.GRAY);
                mPlotView.getAxisGridPaint().setColor(Color.DKGRAY);
                mPlotView.getAxisLabelPaint().setColor(Color.WHITE);
                mPlotView.getAxisLabelPaint().setShadowLayer(3, 1, 1, Color.DKGRAY);
                mPlotView.getAxisValuePaint().setColor(Color.WHITE);
                mPlotView.getAxisValuePaint().setShadowLayer(1, 1, 1, Color.DKGRAY);

                mPlotView.setAxisLabelTextSize(getResources().getInteger(R.integer.comfort_zone_temperature_and_humidity_text_size_graph));
                mPlotView.getAxisValuePaint().setTextSize(getResources().getInteger(R.integer.comfort_zone_temperature_humidity_text_size_graph));
                mPlotView.setAxisValueTextSize(getResources().getInteger(R.integer.comfort_zone_values_text_size));

                mPlotView.setBackgroundImage(R.drawable.img_background_overlay);
                mPlotView.setGridCornerRadius(getDipFor(getFloatValueFromId(getParent(), R.dimen.comfort_zone_grid_corner_radius)));

                getView().findViewById(R.id.textview_left).bringToFront();
                getView().findViewById(R.id.textview_top).bringToFront();
                getView().findViewById(R.id.textview_right).bringToFront();
                getView().findViewById(R.id.textview_bottom).bringToFront();
            }
        });
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
        if (getView() == null) {
            throw new NullPointerException(String.format("%s: updateSensorViews -> It was impossible to obtain the view.", TAG));
        }

        getParent().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (final String key : mActiveSensorViews.keySet()) {
                    ((ViewGroup) getView().findViewById(R.id.parentframe)).removeView(mActiveSensorViews.get(key));
                }
            }
        });

        mActiveSensorViews.clear();

        final Iterable<DeviceModel> connectedModels = RHTSensorFacade.getInstance().getConnectedSensors();

        for (final DeviceModel model : connectedModels) {
            createNewSensorViewFor(model);
        }
    }

    private void createNewSensorViewFor(@NonNull final DeviceModel model) {
        final String address = model.getAddress();

        if (getView() == null) {
            throw new NullPointerException(String.format("%s: createNewSensorViewFor -> It was impossible to obtain the view.", TAG));
        }

        try {
            Log.d(TAG, String.format("createNewSensorViewFor() -> TRY address %s", address));
            final XyPoint sensorPoint = new XyPoint(getParent().getApplicationContext());
            sensorPoint.setVisibility(View.INVISIBLE);
            sensorPoint.setTag(address);
            sensorPoint.setRadius(getDipFor(getFloatValueFromId(getParent(), R.dimen.comfort_zone_radius_sensor_point)));
            sensorPoint.setOutlineRadius(getDipFor(getFloatValueFromId(getParent(), R.dimen.comfort_zone_radius_sensor_point) + getFloatValueFromId(getParent(), R.dimen.comfort_zone_outline_radius_offset)));
            sensorPoint.setInnerColor(model.getColor());
            sensorPoint.setOnTouchListener(this);
            mActiveSensorViews.put(address, sensorPoint);
            getParent().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((ViewGroup) getView().findViewById(R.id.parentframe)).addView(sensorPoint);
                }
            });
        } catch (@NonNull final IllegalArgumentException e) {
            Log.e(TAG, String.format("createNewSensorViewFor() -> The following problem was produces when trying address %s -> ", address), e);
        }
    }

    private void updateViewForSelectedSeason() {
        final boolean isSeasonWinter = Settings.getInstance().isSeasonWinter(getParent());
        Log.i(TAG, String.format("updateViewForSelectedSeason(): Season %s was selected.", isSeasonWinter ? "Winter" : "Summer"));
        getParent().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPlotView.setComfortZoneWinter(isSeasonWinter);
            }
        });
    }

    private void updateViewForSelectedTemperatureUnit() {
        final String mAxisLabel;
        final int gridSize;
        float minXAxisValue = getResources().getInteger(R.integer.comfort_zone_min_x_axis_value);
        float maxXAxisValue = getResources().getInteger(R.integer.comfort_zone_max_x_axis_value);

        mIsFahrenheit = Settings.getInstance().isTemperatureUnitFahrenheit(getParent());

        if (mIsFahrenheit) {
            minXAxisValue = Converter.convertToF(minXAxisValue);
            maxXAxisValue = Converter.convertToF(maxXAxisValue);
            gridSize = getResources().getInteger(R.integer.comfort_zone_x_axis_grid_size_fahrenheit);
            mAxisLabel = getString(R.string.graph_label_temperature_fahrenheit);
            Log.d(TAG, "updateViewForSelectedTemperatureUnit -> Updating temperature unit to Fahrenheit.");
        } else {
            gridSize = getResources().getInteger(R.integer.comfort_zone_x_axis_grid_size_celsius);
            mAxisLabel = getString(R.string.graph_label_temperature_celsius);
            Log.d(TAG, "updateViewForSelectedTemperatureUnit -> Updating temperature unit to Celsius.");
        }

        mPlotView.setXAxisLabel(mAxisLabel);
        mPlotView.setXAxisScale(minXAxisValue, maxXAxisValue, gridSize);

        getParent().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPlotView.invalidate();
            }
        });
    }

    private void touchSelectedSensorView() {
        if (isAdded()) {
            final String selectedAddress = Settings.getInstance().getSelectedAddress();
            final XyPoint point = mActiveSensorViews.get(selectedAddress);
            if (point == null) {
                Log.e(TAG, "touchSelectedSensorView() -> could not find XyPoint for address: " + selectedAddress);
            } else {
                selectSensor(selectedAddress);
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
            getParent().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    point.invalidate();
                }
            });
        }
        if (mActiveSensorViews.containsKey(selectedAddress)) {
            Settings.getInstance().setSelectedAddress(selectedAddress);
            mActiveSensorViews.get(selectedAddress).setOutlineColor(Color.WHITE);
            getParent().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mActiveSensorViews.get(selectedAddress).invalidate();
                }
            });
            updateTextViewName();
        } else {
            Log.e(TAG, "selectSensor(): no selected address found: " + selectedAddress);
        }
    }

    private void updateTextViewName() {
        try {
            final String selectedAddress = Settings.getInstance().getSelectedAddress();
            final DeviceModel model = RHTSensorFacade.getInstance().getDeviceModel(selectedAddress);
            if (model == null) {
                mActiveSensorViews.remove(selectedAddress);
                return;
            }

            if (getView() == null) {
                throw new NullPointerException(String.format("%s: updateTextViewName -> It was impossible to obtain the view.", TAG));
            }

            final TextView textSensorName = (TextView) getView().findViewById(R.id.tv_sensor_name);

            getParent().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mActiveSensorViews.containsKey(selectedAddress)) {
                        textSensorName.setTextColor(mActiveSensorViews.get(selectedAddress).getInnerColor());
                    } else {
                        Log.e(TAG, String.format("updateTextViewName() -> mActiveSensorViews does not selected address: %s", selectedAddress));
                        textSensorName.setTextColor(model.getColor());
                    }
                    textSensorName.setText(model.getUserDeviceName());
                }
            });
        } catch (@NonNull final IllegalArgumentException e) {
            Log.e(TAG, "updateTextViewName(): The following exception was produced -> ", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onGadgetConnectionChanged(@NonNull final String deviceAddress, final boolean deviceIsConnected) {
        if (isAdded()) {
            if (deviceIsConnected) {
                Log.d(TAG, String.format("onGadgetConnectionChanged() -> Device %s was connected.", deviceAddress));
            } else {
                Log.d(TAG, String.format("onGadgetConnectionChanged() -> Device %s was disconnected. ", deviceAddress));
                if (getView() == null) {
                    throw new NullPointerException(String.format("%s: onGadgetConnectionChanged -> It was impossible to obtain the view.", TAG));
                }
                removeSensorView(deviceAddress);
                if (RHTSensorFacade.getInstance().hasConnectedDevices()) {
                    touchSelectedSensorView();
                } else {
                    getParent().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((TextView) getView().findViewById(R.id.tv_sensor_name)).setText(getResources().getString(R.string.text_sensor_name_default));
                            ((TextView) getView().findViewById(R.id.text_amb_temp)).setText(getResources().getString(R.string.label_empty_t));
                            ((TextView) getView().findViewById(R.id.text_rh)).setText(getResources().getString(R.string.label_empty_rh));
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
    public void onNewRHTSensorData(final float temperature, final float relativeHumidity, @Nullable final String deviceAddress) {
        if (deviceAddress == null) {
            updateViewValues(RHTInternalSensorManager.INTERNAL_SENSOR_ADDRESS, temperature, relativeHumidity);
        } else {
            updateViewValues(deviceAddress, temperature, relativeHumidity);
        }
    }

    private void removeSensorView(final String deviceAddress) {
        if (mActiveSensorViews.containsKey(deviceAddress)) {
            if (getView() == null) {
                throw new NullPointerException(String.format("%s: removeSensorView -> It was impossible to obtain the view.", TAG));
            }
            Log.i(TAG, String.format("removeSensorView() -> The view from address %s was removed.", deviceAddress));
            getParent().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((ViewGroup) getView().findViewById(R.id.parentframe)).removeView(mActiveSensorViews.get(deviceAddress));
                }
            });
            mActiveSensorViews.remove(deviceAddress);
        }
    }

    public void updateViewValues(@NonNull final String address, final float temperature, final float relativeHumidity) {
        if (isAdded()) {
            getParent().runOnUiThread(new Runnable() {
                float newTemperature = temperature;
                String unit;

                @Override
                public void run() {
                    Log.v(TAG, String.format("updateViewValues(): address = %s | temperature = %f | relativeHumidity = %f", address, temperature, relativeHumidity));
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
                    updateViewPositionFor(address, newPos, isClipped);
                }
            });
        }
    }

    private void updateTextViewRHT(@NonNull final String address, final float temperature, final float humidity, final String unit) {
        if (address.equals(Settings.getInstance().getSelectedAddress())) {
            if (getView() == null) {
                throw new NullPointerException(String.format("%s: updateTextViewRHT -> It was impossible to obtain the view.", TAG));
            }
            final TextView temperatureTextView = (TextView) getView().findViewById(R.id.text_amb_temp);
            final TextView humidityTextView = (TextView) getView().findViewById(R.id.text_rh);
            final NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
            nf.setMaximumFractionDigits(1);
            nf.setMinimumFractionDigits(1);
            temperatureTextView.setText(nf.format(temperature) + unit);
            humidityTextView.setText(String.format("%s%sRH", nf.format(humidity), getString(R.string.char_percent)));
        }
    }

    private void updateViewPositionFor(@NonNull final String address, @NonNull final PointF p, final boolean isClipped) {
        final PointF canvasPosition;
        if (isClipped) {
            canvasPosition = mPlotView.mapCanvasCoordinatesFor(mPlotView.getClippedPoint());
        } else {
            canvasPosition = mPlotView.mapCanvasCoordinatesFor(p);
        }
        animateSensorViewPointTo(address, canvasPosition.x, canvasPosition.y);
    }

    private void animateSensorViewPointTo(@NonNull final String address, final float x, final float y) {
        if (mActiveSensorViews.containsKey(address)) {
            final float relativeX = x - (getDipFor(getFloatValueFromId(getParent(), R.dimen.comfort_zone_radius_sensor_point) + getFloatValueFromId(getParent(), R.dimen.comfort_zone_outline_radius_offset)));
            final float relativeY = y - (getDipFor(getFloatValueFromId(getParent(), R.dimen.comfort_zone_radius_sensor_point) + getFloatValueFromId(getParent(), R.dimen.comfort_zone_outline_radius_offset)));
            getParent().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mActiveSensorViews.get(address).animateMove(relativeX, relativeY);
                }
            });
        } else {
            Log.w(TAG, "animateSensorViewPointTo() -> mActiveSensorViews does not contain key: ".concat(address));
        }
    }

    private float getDipFor(final float px) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, getResources().getDisplayMetrics());
    }
}