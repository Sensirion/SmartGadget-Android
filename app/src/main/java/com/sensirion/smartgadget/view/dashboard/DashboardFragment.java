package com.sensirion.smartgadget.view.dashboard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.sensirion.smartgadget.R;
import com.sensirion.smartgadget.peripheral.rht_sensor.RHTSensorFacade;
import com.sensirion.smartgadget.peripheral.rht_sensor.RHTSensorListener;
import com.sensirion.smartgadget.peripheral.rht_sensor.internal.RHTInternalSensorManager;
import com.sensirion.smartgadget.utils.Converter;
import com.sensirion.smartgadget.utils.DeviceModel;
import com.sensirion.smartgadget.utils.Settings;
import com.sensirion.smartgadget.utils.view.ParentFragment;
import com.sensirion.smartgadget.view.MainActivity;
import com.sensirion.smartgadget.view.dashboard.adapter.ConnectedDeviceAdapter;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class DashboardFragment extends ParentFragment implements RHTSensorListener {

    private static final String TAG = DashboardFragment.class.getSimpleName();

    private static final int BUTTON_STATE_TEMPERATURE = 0;
    private static final int DEFAULT_STATE_BUTTON = BUTTON_STATE_TEMPERATURE;
    private static final int BUTTON_STATE_HUMIDITY = 1;
    private static final int BUTTON_STATE_DEW_POINT = 2;
    private static final int BUTTON_STATE_HEAT_INDEX = 3;
    private boolean mIsFahrenheit;

    private ConnectedDeviceAdapter mConnectedDeviceAdapter;

    private void updateViewForSelectedTemperatureUnit() {
        mIsFahrenheit = Settings.getInstance().isTemperatureUnitFahrenheit(getParent());
        Log.i(TAG, String.format("updateViewForSelectedTemperatureUnit(): The temperature unit it's %s.", (mIsFahrenheit) ? "Fahrenheit" : "Celsius"));
    }

    private void updateListView() {
        if (isAdded()) {
            Log.i(TAG, "updateListView()");
            final List<DeviceModel> connectedDevices = RHTSensorFacade.getInstance().getConnectedSensors();
            final ListView listView = (ListView) getParent().findViewById(R.id.dashboard_connected_device_nested_list_view);
            final ConnectedDeviceAdapter adapter = ((ConnectedDeviceAdapter) listView.getAdapter());
            getParent().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.update(connectedDevices);
                }
            });
        }
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        Log.i(TAG, "OnCreateView()");
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.i(TAG, "onViewCreated()");
        final Typeface typefaceNormal = Typeface.createFromAsset(getParent().getAssets(), "HelveticaNeueLTStd-Cn.otf");
        final Typeface typefaceBold = Typeface.createFromAsset(getParent().getAssets(), "HelveticaNeueLTStd-Bd.otf");
        initButtons(typefaceNormal, typefaceBold);

        initListView();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume()");
        RHTSensorFacade.getInstance().registerListener(this);

        updateViewForSelectedTemperatureUnit();
        updateListView();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "onPause()");
        RHTSensorFacade.getInstance().unregisterListener(this);
        resetViewValues();
    }

    @SuppressLint("CommitPrefEdits")
    //Using apply instead of commit can cause a Parallel writing Exception.
    private void initButtons(final Typeface typefaceNormal, final Typeface typefaceBold) {
        ((TextView) getParent().findViewById(R.id.dashboard_temperature_value)).setTypeface(typefaceBold);
        ((TextView) getParent().findViewById(R.id.dashboard_humidity_value)).setTypeface(typefaceBold);
        ((TextView) getParent().findViewById(R.id.dashboard_dew_point_value)).setTypeface(typefaceBold);
        ((TextView) getParent().findViewById(R.id.dashboard_heat_index_value)).setTypeface(typefaceBold);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getParent().getApplicationContext());

        final Button button1 = (Button) getParent().findViewById(R.id.dashboard_temperature_button);
        button1.setTypeface(typefaceNormal);
        prefs.edit().putInt(String.valueOf(button1.getId()), BUTTON_STATE_TEMPERATURE).commit();
        addButtonListener(button1);

        final Button button2 = (Button) getParent().findViewById(R.id.dashboard_humidity_button);
        button2.setTypeface(typefaceNormal);
        prefs.edit().putInt(String.valueOf(button2.getId()), BUTTON_STATE_HUMIDITY).commit();
        addButtonListener(button2);

        final Button button3 = (Button) getParent().findViewById(R.id.dashboard_dew_point_button);
        button3.setTypeface(typefaceNormal);
        prefs.edit().putInt(String.valueOf(button3.getId()), BUTTON_STATE_DEW_POINT).commit();
        addButtonListener(button3);

        final Button button4 = (Button) getParent().findViewById(R.id.dashboard_heat_index_button);
        button4.setTypeface(typefaceNormal);
        prefs.edit().putInt(String.valueOf(button4.getId()), BUTTON_STATE_HEAT_INDEX).commit();
        addButtonListener(button4);
    }

    private void initListView() {
        mConnectedDeviceAdapter = new ConnectedDeviceAdapter(getParent().getApplicationContext());

        final ListView listView = (ListView) getParent().findViewById(R.id.dashboard_connected_device_nested_list_view);
        listView.setAdapter(mConnectedDeviceAdapter);
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> adapterView, View arg1, final int position, long arg3) {
                final String selectedAddress = mConnectedDeviceAdapter.getItem(position).getAddress();
                if (Settings.getInstance().getSelectedAddress().equals(selectedAddress)) {
                    return;
                }
                Settings.getInstance().setSelectedAddress(selectedAddress);
                mConnectedDeviceAdapter.notifyDataSetChanged();
                resetViewValues();
            }
        });
        updateListView();
    }

    private void addButtonListener(@NonNull final Button button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getParent().getResources().getBoolean(R.bool.is_tablet)) {
                    ((MainActivity) getParent()).toggleTabletMenu();
                }
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onGadgetConnectionChanged(@Nullable final String deviceAddress, final boolean deviceIsConnected) {
        if (isAdded()) {
            updateListView();
            if (deviceIsConnected) {
                Log.i(TAG, String.format("onGadgetConnectionChanged() -> Sensor with address %s was connected.", deviceAddress));
            } else {
                resetViewValues();
                Log.i(TAG, String.format("onGadgetConnectionChanged() -> Sensor with address %s was disconnected.", deviceAddress));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onNewRHTSensorData(final float temperature, final float relativeHumidity, @Nullable final String deviceAddress) {
        if (deviceAddress == null) {
            updateViewValues(temperature, relativeHumidity, RHTInternalSensorManager.INTERNAL_SENSOR_ADDRESS);
        } else {
            updateViewValues(temperature, relativeHumidity, deviceAddress);
        }
    }

    private void resetViewValues() {
        getParent().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final TextView temperatureTextView = ((TextView) getParent().findViewById(R.id.dashboard_temperature_value));
                if (temperatureTextView != null) {
                    temperatureTextView.setText(getString(R.string.label_empty_t));
                }
                final TextView humidityTextView = ((TextView) getParent().findViewById(R.id.dashboard_humidity_value));
                if (humidityTextView != null) {
                    humidityTextView.setText(getString(R.string.label_empty_rh));
                }
                final TextView dewPointTextView = ((TextView) getParent().findViewById(R.id.dashboard_dew_point_value));
                if (dewPointTextView != null) {
                    dewPointTextView.setText(getString(R.string.label_empty_t));
                }
                final TextView heatIndexTextView = ((TextView) getParent().findViewById(R.id.dashboard_heat_index_value));
                if (heatIndexTextView != null) {
                    heatIndexTextView.setText(getString(R.string.label_empty_t));
                }
            }
        });
    }

    private void updateViewValues(final float temperature, final float humidity, @NonNull final String deviceAddress) {
        if (isAdded()) {
            getParent().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final String selectedAddress = Settings.getInstance().getSelectedAddress();
                    if (deviceAddress.equals(selectedAddress)) {

                        changeButtonValue((Button) getParent().findViewById(R.id.dashboard_temperature_button),
                                (TextView) getParent().findViewById(R.id.dashboard_temperature_value));
                        changeButtonValue((Button) getParent().findViewById(R.id.dashboard_humidity_button),
                                (TextView) getParent().findViewById(R.id.dashboard_humidity_value));
                        changeButtonValue((Button) getParent().findViewById(R.id.dashboard_dew_point_button),
                                (TextView) getParent().findViewById(R.id.dashboard_dew_point_value));
                        changeButtonValue((Button) getParent().findViewById(R.id.dashboard_heat_index_button),
                                (TextView) getParent().findViewById(R.id.dashboard_heat_index_value));
                    }
                }

                private void changeButtonValue(@NonNull final Button button, @NonNull final TextView textView) {
                    final String unit;
                    if (mIsFahrenheit) {
                        unit = getString(R.string.unit_fahrenheit);
                    } else {
                        unit = getString(R.string.unit_celsius);
                    }

                    final Context appContext = getParent().getApplicationContext();
                    final String buttonId = String.valueOf(button.getId());
                    final int buttonState = PreferenceManager.getDefaultSharedPreferences(appContext).getInt(buttonId, DEFAULT_STATE_BUTTON);

                    final NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
                    nf.setMaximumFractionDigits(1);
                    nf.setMinimumFractionDigits(1);

                    final String signGap;

                    switch (buttonState) {
                        case BUTTON_STATE_HUMIDITY:
                            signGap = " ";
                            textView.setText(String.format("%s%s%s", signGap, nf.format(humidity), getString(R.string.unit_humidity)));
                            break;
                        case BUTTON_STATE_TEMPERATURE:
                            signGap = temperature < 0 ? "" : " ";
                            final String fixedTemperature = nf.format(mIsFahrenheit ? Converter.convertToF(temperature) : temperature);
                            textView.setText(String.format("%s%s%s", signGap, fixedTemperature, unit));
                            break;
                        case BUTTON_STATE_DEW_POINT:
                            final float dewPoint = Converter.calcDewPoint(humidity, temperature);
                            final float fixedDewPoint = mIsFahrenheit ? Converter.convertToF(dewPoint) : dewPoint;
                            signGap = fixedDewPoint < 0 ? "" : " ";
                            textView.setText(String.format("%s%s%s", signGap, nf.format(fixedDewPoint), unit));
                            break;
                        case BUTTON_STATE_HEAT_INDEX:
                            final float heatIndex;
                            if (mIsFahrenheit) {
                                heatIndex = Converter.calculateHeatIndexFahrenheit(humidity, Converter.convertToF(temperature));
                            } else {
                                heatIndex = Converter.calculateHeatIndexCelsius(humidity, temperature);
                            }
                            if (Float.isNaN(heatIndex)) {
                                textView.setText(String.format(" %s", getString(R.string.label_empty_heat_index)));
                            } else {
                                signGap = heatIndex < 0 ? "" : " ";
                                textView.setText(String.format("%s%s%s", signGap, nf.format(heatIndex), unit));
                            }
                            break;
                        default:
                            Log.e(TAG, String.format("changeButtonValue() -> Unknown button state: %s", buttonState));
                            break;
                    }
                }
            });
        }
    }
}