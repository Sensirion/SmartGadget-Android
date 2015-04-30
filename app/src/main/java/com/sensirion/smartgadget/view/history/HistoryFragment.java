package com.sensirion.smartgadget.view.history;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.androidplot.xy.SimpleXYSeries;
import com.sensirion.libble.utils.RHTDataPoint;
import com.sensirion.smartgadget.R;
import com.sensirion.smartgadget.peripheral.rht_sensor.RHTSensorFacade;
import com.sensirion.smartgadget.peripheral.rht_sensor.RHTSensorListener;
import com.sensirion.smartgadget.persistence.device_name_database.DeviceNameDatabaseManager;
import com.sensirion.smartgadget.persistence.history_database.HistoryDatabaseManager;
import com.sensirion.smartgadget.utils.DeviceModel;
import com.sensirion.smartgadget.utils.Interval;
import com.sensirion.smartgadget.utils.view.ColorManager;
import com.sensirion.smartgadget.utils.view.ParentFragment;
import com.sensirion.smartgadget.view.MainActivity;
import com.sensirion.smartgadget.view.history.adapter.HistoryDeviceAdapter;
import com.sensirion.smartgadget.view.history.type.HistoryIntervalType;
import com.sensirion.smartgadget.view.history.type.HistoryUnitType;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class HistoryFragment extends ParentFragment implements RHTSensorListener {

    private static final String TAG = HistoryFragment.class.getSimpleName();

    private static final HistoryUnitType DEFAULT_UNIT_TYPE = HistoryUnitType.TEMPERATURE;
    private static final HistoryIntervalType DEFAULT_TIME_INTERVAL = HistoryIntervalType.INTERVAL_OF_10_MINUTES;
    private static final Map<String, DeviceModel> mDisconnectedDevicesModel = Collections.synchronizedMap(new HashMap<String, DeviceModel>());
    @NonNull
    private HistoryUnitType mUnitTypeSelected = DEFAULT_UNIT_TYPE;
    @NonNull
    private HistoryIntervalType mIntervalSelected = DEFAULT_TIME_INTERVAL;
    private PlotHandler mPlotHandler;

    private HistoryDeviceAdapter mHistoryDeviceAdapter;

    private long mLastDatabaseQuery = System.currentTimeMillis();

    private int mLastIntervalPosition = 0;
    private int mLastUnitPosition = 0;

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        final View historyView = inflater.inflate(R.layout.fragment_history, container, false);
        init(historyView);
        RHTSensorFacade.getInstance().registerListener(this);
        historyView.findViewById(R.id.history_plot_temperature).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                ((MainActivity) getParent()).toggleTabletMenu();
            }
        });
        return historyView;
    }

    public void init(@NonNull final View historyView) {
        refreshIntervalTabs(historyView);
        initHistoryDeviceListView(historyView);
        refreshTypeValueTabs(historyView);
        updateConnectedDeviceView();
        mPlotHandler = new PlotHandler(getParent(), historyView, DEFAULT_TIME_INTERVAL, DEFAULT_UNIT_TYPE);
        updateGraph();
    }

    public void updateGraph() {
        if (mHistoryDeviceAdapter == null) {
            Log.e(TAG, "updateGraph -> Graph is not initialized yet.");
            return;
        }
        updateConnectedDeviceView();
        final List<String> selectedItems = mHistoryDeviceAdapter.getListOfSelectedItems();

        if (selectedItems.isEmpty()) {
            Log.i(TAG, "init -> No values to display.");
            return;
        }
        mPlotHandler.updateSeries(getParent(), obtainPlotSeries(selectedItems), mIntervalSelected, mUnitTypeSelected);
    }

    private void initHistoryDeviceListView(@NonNull final View historyView) {
        HistoryDatabaseManager.getInstance().purgeOldDatabaseData();
        mHistoryDeviceAdapter = new HistoryDeviceAdapter(getParent().getApplicationContext());
        final ListView listView = (ListView) historyView.findViewById(R.id.history_device_nested_list_view);
        listView.setAdapter(mHistoryDeviceAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> adapterView, View arg1, final int position, long arg3) {
                mHistoryDeviceAdapter.itemSelected(position);
                mHistoryDeviceAdapter.notifyDataSetChanged();
                mPlotHandler.updateSeries(getParent(), obtainPlotSeries(mHistoryDeviceAdapter.getListOfSelectedItems()), mIntervalSelected, mUnitTypeSelected);
            }
        });
        updateDeviceView(historyView);
    }

    private void updateConnectedDeviceView() {
        final View historyView = super.getView();
        if (historyView != null) {
            updateDeviceView(historyView);
        }
    }

    private void updateDeviceView(@NonNull final View historyView) {
        if (isAdded()) {
            final List<String> connectedDevicesAddresses = HistoryDatabaseManager.getInstance().getConnectedDeviceListInterval(mIntervalSelected);
            final List<DeviceModel> deviceModels = new LinkedList<>();
            for (final String deviceAddress : connectedDevicesAddresses) {
                DeviceModel model = RHTSensorFacade.getInstance().getDeviceModel(deviceAddress);
                if (model == null) {
                    model = obtainDeviceModelDisconnectedDevice(deviceAddress);
                }
                deviceModels.add(model);
            }
            final ListView listView = (ListView) historyView.findViewById(R.id.history_device_nested_list_view);
            final HistoryDeviceAdapter adapter = (HistoryDeviceAdapter) listView.getAdapter();

            final Handler viewHandler = listView.getHandler();
            if (viewHandler == null) {
                adapter.update(new Handler(Looper.myLooper()), deviceModels);
                Log.d(TAG, "updateDeviceView -> No devices in selected interval.");
            } else {
                Log.d(TAG, String.format("updateDeviceView() -> Added %d devices.", deviceModels.size()));
                adapter.update(viewHandler, deviceModels);
            }
        }
    }

    private DeviceModel obtainDeviceModelDisconnectedDevice(@NonNull final String deviceAddress) {
        DeviceModel model = mDisconnectedDevicesModel.get(deviceAddress);
        if (model == null) {
            final String deviceName = DeviceNameDatabaseManager.getInstance().readDeviceName(deviceAddress);
            final int color = ColorManager.getInstance().getDeviceColor(deviceAddress);
            model = new DeviceModel(deviceAddress, color, deviceName, false);
        }
        return model;
    }

    private void refreshIntervalTabs(@NonNull final View historyView) {
        final LinearLayout tabLayout = (LinearLayout) historyView.findViewById(R.id.history_interval_tabs);
        tabLayout.removeAllViews();
        for (int i = 0; i < HistoryIntervalType.values().length; i++) {
            final String tabDisplayName = HistoryIntervalType.getInterval(i).getDisplayName(getParent());
            final float textSize = historyView.getContext().getResources().getInteger(R.integer.history_fragment_interval_tabs_text_size);
            final View intervalTab = createTab(tabLayout, null, tabDisplayName, textSize, i == mLastIntervalPosition);
            final int position = i;
            intervalTab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onIntervalTabSelected(position);
                }
            });
            tabLayout.addView(intervalTab);
        }
    }

    private void refreshTypeValueTabs(@NonNull final View historyView) {
        final LinearLayout tabLayout = (LinearLayout) historyView.findViewById(R.id.history_type_of_value_tabs);
        tabLayout.removeAllViews();
        for (int i = 0; i < HistoryUnitType.values().length; i++) {
            final HistoryUnitType unitType = HistoryUnitType.getUnitType(i);
            final String tabDisplayName = unitType.getDisplayName(getParent());
            final Drawable icon = unitType.getIcon(getParent());
            final int textSize = historyView.getContext().getResources().getInteger(R.integer.history_fragment_value_tabs_text_size);
            final View unitTypeView = createTab(tabLayout, icon, tabDisplayName, textSize, i == mLastUnitPosition);
            final int position = i;
            unitTypeView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onTypeOfValueTabSelected(position);
                }
            });
            tabLayout.addView(unitTypeView);
        }
    }

    @NonNull
    private View createTab(@NonNull final ViewGroup root,
                           @Nullable final Drawable icon,
                           @NonNull final String text,
                           final float textSize,
                           final boolean marked) {

        final Button view;
        if (marked) {
            view = (Button) getParent().getLayoutInflater().inflate(R.layout.history_preference_tab_marked, root, false);
        } else {
            view = (Button) getParent().getLayoutInflater().inflate(R.layout.history_preference_tab, root, false);
        }

        if (icon != null) {
            view.setPadding(0, view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom());
            view.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            view.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null);
        }

        view.setTextSize(textSize);
        view.setText(text);

        final Typeface typefaceBold = Typeface.createFromAsset(getParent().getAssets(), "HelveticaNeueLTStd-Bd.otf");
        view.setTypeface(typefaceBold, Typeface.BOLD);

        return view;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onGadgetConnectionChanged(@NonNull final String deviceAddress, final boolean deviceIsConnected) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onNewRHTSensorData(final float temperature, final float relativeHumidity, @Nullable final String deviceAddress) {
        if (mLastDatabaseQuery + Interval.FIVE_SECONDS.getNumberMilliseconds() < System.currentTimeMillis()) {
            if (deviceAddress == null || mHistoryDeviceAdapter.getListOfSelectedItems().contains(deviceAddress)) {
                Log.i(TAG, "onNewRHTSensorData -> plot view has been updated.");
                mPlotHandler.updateSeries(getParent(), obtainPlotSeries(mHistoryDeviceAdapter.getListOfSelectedItems()), mIntervalSelected, mUnitTypeSelected);
            }
            mLastDatabaseQuery = System.currentTimeMillis();
        }
    }

    /**
     * This method is called when a interval tab is pressed by the user.
     *
     * @param position of the tab pressed by the user.
     */
    private void onIntervalTabSelected(final int position) {
        mLastIntervalPosition = position;

        Log.d(TAG, String.format("onIntervalTabSelected -> Position %d was selected.", position));
        mIntervalSelected = HistoryIntervalType.getInterval(position);

        updateConnectedDeviceView();

        final List<SimpleXYSeries> plotSeries = obtainPlotSeries(mHistoryDeviceAdapter.getListOfSelectedItems());
        mPlotHandler.updateSeries(getParent(), plotSeries, mIntervalSelected, mUnitTypeSelected);
        refreshIntervalTabs(getView());
    }

    /**
     * This method is called when a value type tab is pressed by the user.
     *
     * @param position of the tab pressed by the user.
     */
    private void onTypeOfValueTabSelected(final int position) {
        mLastUnitPosition = position;

        Log.d(TAG, String.format("onTypeOfValueTabSelected -> Position %d was selected.", position));
        mUnitTypeSelected = HistoryUnitType.getUnitType(position);
        final List<SimpleXYSeries> plotSeries = obtainPlotSeries(mHistoryDeviceAdapter.getListOfSelectedItems());
        mPlotHandler.updateSeries(getParent(), plotSeries, mIntervalSelected, mUnitTypeSelected);
        refreshTypeValueTabs(getView());
    }

    /**
     * Obtain the list of series from the database.
     *
     * @param deviceAddressList with the devices that will be used in order to display data.
     * @return {@link java.util.List} with the {@link com.androidplot.xy.SimpleXYSeries} that will be displayed in the graph.
     */
    @NonNull
    private List<SimpleXYSeries> obtainPlotSeries(@NonNull final List<String> deviceAddressList) {
        final List<SimpleXYSeries> listOfDataPoints = new LinkedList<>();
        final HistoryResult databaseResults = HistoryDatabaseManager.getInstance().getHistoryPoints(mIntervalSelected, deviceAddressList);
        if (databaseResults == null) {
            Log.w(TAG, "obtainPlotSeries -> No results where found when updating the plot.");
            return listOfDataPoints;
        }
        for (final String deviceAddress : databaseResults.getResults().keySet()) {
            final List<RHTDataPoint> deviceDatapoints = databaseResults.getResults().get(deviceAddress);
            if (deviceDatapoints.isEmpty()) {
                continue;
            }
            final SimpleXYSeries newSeries = obtainGraphSeriesFromDatapointList(deviceAddress, deviceDatapoints);
            listOfDataPoints.add(newSeries);
        }
        Log.i(TAG, String.format("obtainPlotSeries -> Prepared %d graph series.", listOfDataPoints.size()));
        return listOfDataPoints;
    }

    /**
     * Obtains a SimpleXYSeries from a datapoint list.
     *
     * @param deviceDatapoints that haves to be converted into a graph series.
     * @return {@link com.androidplot.xy.SimpleXYSeries} with the device data.
     */
    @NonNull
    private SimpleXYSeries obtainGraphSeriesFromDatapointList(@NonNull final String deviceAddress, @NonNull final List<RHTDataPoint> deviceDatapoints) {
        if (deviceDatapoints.isEmpty()) {
            throw new IllegalArgumentException(String.format("%s: obtainGraphSeriesFromDatapointList -> In order to obtain data from a list it cannot be empty.", TAG));
        }
        sortDatapointListByTimestamp(deviceDatapoints);
        final SimpleXYSeries deviceSeries = new SimpleXYSeries(deviceAddress);
        for (final RHTDataPoint datapoint : deviceDatapoints) {
            final Float value = obtainValueFromDatapoint(datapoint);
            deviceSeries.addFirst(datapoint.getTimestamp(), value);
        }
        return deviceSeries;
    }

    /**
     * Sorts the datapoint List by timestamps.
     * Uses {@link com.sensirion.libble.utils.RHTDataPoint} compareTo for sorting.
     *
     * @param deviceDatapoints that wants to be sorted.
     */
    private void sortDatapointListByTimestamp(@NonNull final List<RHTDataPoint> deviceDatapoints) {
        Collections.sort(deviceDatapoints);
    }

    /**
     * Obtains the required value from a datapoint.
     *
     * @param datapoint that is going to be used in order to obtain the required value.
     * @return {@link java.lang.Float} with the requiredValue. <code>null</code> if the value is corrupted.
     */
    private float obtainValueFromDatapoint(@NonNull final RHTDataPoint datapoint) {
        if (mUnitTypeSelected == HistoryUnitType.TEMPERATURE) {
            return datapoint.getTemperatureCelsius();
        }
        return datapoint.getRelativeHumidity();
    }
}