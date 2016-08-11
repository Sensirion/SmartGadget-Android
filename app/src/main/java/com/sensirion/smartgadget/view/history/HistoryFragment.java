package com.sensirion.smartgadget.view.history;

import android.content.Context;
import android.content.res.AssetManager;
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
import com.sensirion.smartgadget.R;
import com.sensirion.smartgadget.peripheral.rht_sensor.RHTSensorFacade;
import com.sensirion.smartgadget.peripheral.rht_sensor.RHTSensorListener;
import com.sensirion.smartgadget.peripheral.rht_utils.RHTDataPoint;
import com.sensirion.smartgadget.persistence.device_name_database.DeviceNameDatabaseManager;
import com.sensirion.smartgadget.persistence.history_database.HistoryDatabaseManager;
import com.sensirion.smartgadget.utils.DeviceModel;
import com.sensirion.smartgadget.utils.Interval;
import com.sensirion.smartgadget.utils.view.ColorManager;
import com.sensirion.smartgadget.utils.view.ParentFragment;
import com.sensirion.smartgadget.view.MainActivity;
import com.sensirion.smartgadget.view.history.adapter.HistoryDeviceAdapter;
import com.sensirion.smartgadget.view.history.graph.HistoryPlot;
import com.sensirion.smartgadget.view.history.type.HistoryIntervalType;
import com.sensirion.smartgadget.view.history.type.HistoryUnitType;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindInt;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

public class HistoryFragment extends ParentFragment implements RHTSensorListener {

    // Class TAG
    @NonNull
    private static final String TAG = HistoryFragment.class.getSimpleName();

    // Default plot parameters
    @NonNull
    private static final HistoryUnitType DEFAULT_UNIT_TYPE = HistoryUnitType.TEMPERATURE;
    @NonNull
    private static final HistoryIntervalType DEFAULT_TIME_INTERVAL = HistoryIntervalType.INTERVAL_OF_10_MINUTES;

    // Injected application Views
    @BindView(R.id.history_device_nested_list_view)
    ListView mDeviceListView;
    @BindView(R.id.history_interval_tabs)
    LinearLayout mIntervalTabs;
    @BindView(R.id.history_type_of_value_tabs)
    LinearLayout mValueTabs;
    @BindView(R.id.history_fragment_plot)
    HistoryPlot mPlot;

    // Extracted constants from the XML resources
    @BindInt(R.integer.history_fragment_value_tabs_text_size)
    int VALUE_TABS_TEXT_SIZE;
    @BindInt(R.integer.history_fragment_interval_tabs_text_size)
    int INTERVAL_TABS_TEXT_SIZE;
    @BindString(R.string.typeface_bold)
    String TYPEFACE_BOLD_LOCATION;

    // Selected plot parameters
    @NonNull
    private HistoryUnitType mUnitTypeSelected = DEFAULT_UNIT_TYPE;
    @NonNull
    private HistoryIntervalType mIntervalSelected = DEFAULT_TIME_INTERVAL;

    // Plot handler
    @Nullable
    private PlotHandler mPlotHandler;

    // Device adapter
    @Nullable
    private HistoryDeviceAdapter mHistoryDeviceAdapter;

    // Fragment state controllers
    private long mLastDatabaseQuery = System.currentTimeMillis();
    private int mLastIntervalPosition = 0;
    private int mLastUnitPosition = 0;

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        final View historyView = inflater.inflate(R.layout.fragment_history, container, false);
        unbinder = ButterKnife.bind(this, historyView);
        init(historyView);
        mPlot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(@NonNull final View v) {
                final MainActivity parent = (MainActivity) getParent();
                if (parent == null) {
                    Log.e(TAG, "onClick -> Can't obtain the MainActivity");
                } else {
                    parent.toggleTabletMenu();
                }
            }
        });
        return historyView;
    }

    @Override
    public void onResume() {
        super.onResume();
        RHTSensorFacade.getInstance().registerListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        RHTSensorFacade.getInstance().unregisterListener(this);
    }

    public void init(@NonNull final View historyView) {
        refreshIntervalTabs();
        initHistoryDeviceListView();
        refreshTypeValueTabs();
        updateDeviceView();
        mPlotHandler = new PlotHandler(getContext(), historyView, DEFAULT_TIME_INTERVAL, DEFAULT_UNIT_TYPE);
        updateGraph();
    }

    public void updateGraph() {
        if (mHistoryDeviceAdapter == null) {
            Log.e(TAG, "updateGraph -> Graph is not initialized yet.");
            return;
        }
        updateDeviceView();
        final List<String> selectedItems = mHistoryDeviceAdapter.getListOfSelectedItems();

        if (selectedItems.isEmpty()) {
            Log.d(TAG, "updateGraph -> No values to display.");
            return;
        }
        if (mPlotHandler == null) {
            Log.e(TAG, "updateGraph -> Don't have a plot handler for managing the plot");
            return;
        }
        mPlotHandler.updateSeries(
                getContext(),
                obtainPlotSeries(selectedItems),
                mIntervalSelected,
                mUnitTypeSelected
        );
    }

    private void initHistoryDeviceListView() {
        HistoryDatabaseManager.getInstance().purgeOldDatabaseData();
        mHistoryDeviceAdapter = new HistoryDeviceAdapter(getContext().getApplicationContext());
        mDeviceListView.setAdapter(mHistoryDeviceAdapter);

        mDeviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> adapterView, View arg1, final int position, long arg3) {
                mHistoryDeviceAdapter.itemSelected(position);
                mHistoryDeviceAdapter.notifyDataSetChanged();

                if (mPlotHandler == null) {
                    Log.e(TAG, "updateGraph -> Don't have a plot handler for managing the plot");
                    return;
                }
                mPlotHandler.updateSeries(
                        getContext(),
                        obtainPlotSeries(mHistoryDeviceAdapter.getListOfSelectedItems()),
                        mIntervalSelected,
                        mUnitTypeSelected
                );
            }
        });
        updateDeviceView();
    }

    private void updateDeviceView() {
        if (isAdded()) {
            final HistoryDatabaseManager historyDb = HistoryDatabaseManager.getInstance();
            final List<String> connectedDevicesAddresses =
                    historyDb.getConnectedDeviceListInterval(mIntervalSelected);

            final List<DeviceModel> deviceModels = new LinkedList<>();
            for (final String deviceAddress : connectedDevicesAddresses) {
                DeviceModel model = RHTSensorFacade.getInstance().getDeviceModel(deviceAddress);
                if (model == null) {
                    model = obtainDeviceModelDisconnectedDevice(deviceAddress);
                }
                deviceModels.add(model);
            }

            final HistoryDeviceAdapter adapter = (HistoryDeviceAdapter) mDeviceListView.getAdapter();

            final Handler viewHandler = mDeviceListView.getHandler();
            if (viewHandler == null) {
                adapter.update(new Handler(Looper.myLooper()), deviceModels);
            } else {
                Log.d(TAG, String.format("updateDeviceView() -> Added %d devices.", deviceModels.size()));
                adapter.update(viewHandler, deviceModels);
            }
        }
    }

    private DeviceModel obtainDeviceModelDisconnectedDevice(@NonNull final String deviceAddress) {
        final String deviceName = DeviceNameDatabaseManager.getInstance().readDeviceName(deviceAddress);
        final int color = ColorManager.getInstance().getDeviceColor(deviceAddress);
        return new DeviceModel(deviceAddress, color, deviceName, false);
    }

    private void refreshIntervalTabs() {
        mIntervalTabs.removeAllViews();
        for (int i = 0; i < HistoryIntervalType.values().length; i++) {
            final String tabDisplayName = HistoryIntervalType.getInterval(i).getDisplayName(getContext());
            final float textSize = INTERVAL_TABS_TEXT_SIZE;
            final View intervalTab =
                    createTab(
                            mIntervalTabs,
                            null,
                            tabDisplayName,
                            textSize, i == mLastIntervalPosition
                    );
            final int position = i;
            intervalTab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onIntervalTabSelected(position);
                }
            });
            mIntervalTabs.addView(intervalTab);
        }
    }

    private void refreshTypeValueTabs() {
        mValueTabs.removeAllViews();
        for (int i = 0; i < HistoryUnitType.values().length; i++) {
            final HistoryUnitType unitType = HistoryUnitType.getUnitType(i);
            final String tabDisplayName = unitType.getDisplayName(getContext());
            final Drawable icon = unitType.getIcon(getContext());
            final int textSize = VALUE_TABS_TEXT_SIZE;
            final int position = i;

            final View unitTypeView =
                    createTab(
                            mValueTabs,
                            icon,
                            tabDisplayName,
                            textSize,
                            position == mLastUnitPosition
                    );

            unitTypeView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onTypeOfValueTabSelected(position);
                }
            });
            mValueTabs.addView(unitTypeView);
        }
    }

    @NonNull
    private View createTab(@NonNull final ViewGroup root,
                           @Nullable final Drawable icon,
                           @NonNull final String text,
                           final float textSize,
                           final boolean marked) {

        final Button view;

        final LayoutInflater inflater = getActivity().getLayoutInflater();
        if (marked) {
            view = (Button) inflater.inflate(R.layout.history_preference_tab_marked, root, false);
        } else {
            view = (Button) inflater.inflate(R.layout.history_preference_tab, root, false);
        }

        if (icon != null) {
            view.setPadding(0, view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom());
            view.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            view.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null);
        }

        view.setTextSize(textSize);
        view.setText(text);

        final AssetManager assets = getContext().getAssets();
        final Typeface typefaceBold = Typeface.createFromAsset(assets, TYPEFACE_BOLD_LOCATION);
        view.setTypeface(typefaceBold, Typeface.BOLD);

        return view;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onGadgetConnectionChanged(@NonNull final String deviceAddress,
                                          final boolean deviceIsConnected) {
        // Do nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onNewRHTSensorData(final float temperature,
                                   final float relativeHumidity,
                                   @Nullable final String deviceAddress) {

        if (mLastDatabaseQuery + Interval.FIVE_SECONDS.getNumberMilliseconds()
                < System.currentTimeMillis()) {

            if (mHistoryDeviceAdapter == null) {
                Log.e(TAG, "onNewRHTSensorData -> mHistoryDeviceAdapter can't be null.");
                return;
            }
            if (deviceAddress == null
                    || mHistoryDeviceAdapter.getListOfSelectedItems().contains(deviceAddress)) {

                if (mPlotHandler == null) {
                    Log.e(TAG, "onNewRHTSensorData -> mPlotHandler can't be null.");
                    return;
                }
                final Context context = getContext();
                if (context != null) {
                    mPlotHandler.updateSeries(
                            context,
                            obtainPlotSeries(mHistoryDeviceAdapter.getListOfSelectedItems()),
                            mIntervalSelected,
                            mUnitTypeSelected
                    );
                } else {
                    Log.e(TAG, "onNewRHTSensorData -> plot view not updated.");
                }
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

        if (mHistoryDeviceAdapter == null) {
            Log.e(TAG, "onIntervalTabSelected -> mHistoryDeviceAdapter can't be null.");
            return;
        }
        if (mPlotHandler == null) {
            Log.e(TAG, "onIntervalTabSelected -> mPlotHandler can't be null.");
            return;
        }

        mLastIntervalPosition = position;

        mIntervalSelected = HistoryIntervalType.getInterval(position);

        updateDeviceView();

        final List<String> selectedItems = mHistoryDeviceAdapter.getListOfSelectedItems();
        final List<SimpleXYSeries> plotSeries = obtainPlotSeries(selectedItems);
        mPlotHandler.updateSeries(getContext(), plotSeries, mIntervalSelected, mUnitTypeSelected);
        refreshIntervalTabs();
    }

    /**
     * This method is called when a value type tab is pressed by the user.
     *
     * @param position of the tab pressed by the user.
     */
    private void onTypeOfValueTabSelected(final int position) {

        if (mHistoryDeviceAdapter == null) {
            Log.e(TAG, "onTypeOfValueTabSelected -> mHistoryDeviceAdapter can't be null.");
            return;
        }
        if (mPlotHandler == null) {
            Log.e(TAG, "onTypeOfValueTabSelected -> mPlotHandler can't be null.");
            return;
        }

        mLastUnitPosition = position;

        mUnitTypeSelected = HistoryUnitType.getUnitType(position);

        final List<String> selectedItems = mHistoryDeviceAdapter.getListOfSelectedItems();
        final List<SimpleXYSeries> plotSeries = obtainPlotSeries(selectedItems);
        mPlotHandler.updateSeries(getContext(), plotSeries, mIntervalSelected, mUnitTypeSelected);
        refreshTypeValueTabs();
    }

    /**
     * Obtain the list of series from the database.
     *
     * @param deviceAddressList with the devices that will be used in order to display data.
     * @return {@link java.util.List} with the {@link com.androidplot.xy.SimpleXYSeries} that will be displayed in the graph.
     */
    @NonNull
    private List<SimpleXYSeries> obtainPlotSeries(@NonNull final List<String> deviceAddressList) {

        final HistoryDatabaseManager historyDb = HistoryDatabaseManager.getInstance();
        final HistoryResult databaseResults =
                historyDb.getHistoryPoints(mIntervalSelected, deviceAddressList);

        final List<SimpleXYSeries> listOfDataPoints = new LinkedList<>();

        if (databaseResults == null) {
            return listOfDataPoints;
        }
        for (final String deviceAddress : databaseResults.getResults().keySet()) {
            final List<RHTDataPoint> deviceDataPoints = databaseResults.getResults().get(deviceAddress);
            if (deviceDataPoints.isEmpty()) {
                continue;
            }
            final SimpleXYSeries newSeries = obtainGraphSeriesFromDataPointList(deviceAddress, deviceDataPoints);
            listOfDataPoints.add(newSeries);
        }
        return listOfDataPoints;
    }

    /**
     * Obtains a SimpleXYSeries from a datapoint list.
     *
     * @param dataPoints that haves to be converted into a graph series.
     * @return {@link com.androidplot.xy.SimpleXYSeries} with the device data.
     */
    @NonNull
    private SimpleXYSeries obtainGraphSeriesFromDataPointList(@NonNull final String deviceAddress,
                                                              @NonNull final List<RHTDataPoint> dataPoints) {
        if (dataPoints.isEmpty()) {
            throw new IllegalArgumentException(
                    String.format(
                            "%s: %s -> In order to obtain data from a list it cannot be empty.",
                            "obtainGraphSeriesFromDataPointList",
                            TAG
                    )
            );
        }
        sortDataPointListByTimestamp(dataPoints);
        final SimpleXYSeries deviceSeries = new SimpleXYSeries(deviceAddress);
        for (final RHTDataPoint dataPoint : dataPoints) {
            final Float value = getRequiredValueFromDatapoint(dataPoint);
            deviceSeries.addFirst(dataPoint.getTimestamp(), value);
        }
        return deviceSeries;
    }

    /**
     * Sorts the datapoint List by timestamps.
     * Uses {@link RHTDataPoint#compareTo} for sorting.
     *
     * @param deviceDataPoints is to be sorted.
     */
    private void sortDataPointListByTimestamp(@NonNull final List<RHTDataPoint> deviceDataPoints) {
        Collections.sort(deviceDataPoints);
    }

    /**
     * Obtains the required value from a data point.
     *
     * @param dataPoint that is going to be used in order to obtain the required value.
     * @return {@link Float} with the requiredValue. <code>null</code> if the value is corrupted.
     */
    private float getRequiredValueFromDatapoint(@NonNull final RHTDataPoint dataPoint) {
        if (mUnitTypeSelected == HistoryUnitType.TEMPERATURE) {
            return dataPoint.getTemperatureCelsius();
        }
        return dataPoint.getRelativeHumidity();
    }
}