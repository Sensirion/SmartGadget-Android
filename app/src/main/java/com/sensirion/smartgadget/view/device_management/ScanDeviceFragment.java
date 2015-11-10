package com.sensirion.smartgadget.view.device_management;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.sensirion.libble.BleManager;
import com.sensirion.libble.devices.BleDevice;
import com.sensirion.libble.devices.KnownDevices;
import com.sensirion.libble.listeners.devices.DeviceStateListener;
import com.sensirion.libble.listeners.devices.ScanListener;
import com.sensirion.smartgadget.R;
import com.sensirion.smartgadget.peripheral.rht_sensor.external.RHTHumigadgetSensorManager;
import com.sensirion.smartgadget.persistence.device_name_database.DeviceNameDatabaseManager;
import com.sensirion.smartgadget.utils.Interval;
import com.sensirion.smartgadget.utils.view.IndeterminateProgressDialog;
import com.sensirion.smartgadget.utils.view.ParentListFragment;
import com.sensirion.smartgadget.utils.view.SectionAdapter;
import com.sensirion.smartgadget.view.MainActivity;
import com.sensirion.smartgadget.view.device_management.utils.HumigadgetListItemAdapter;

import java.util.concurrent.Executors;

/**
 * Holds all the UI elements needed for showing the devices in range in a list
 */
public class ScanDeviceFragment extends ParentListFragment implements ScanListener, DeviceStateListener {

    //CLASS TAG
    private static final String TAG = ScanDeviceFragment.class.getSimpleName();

    //TIMEOUT ATTRIBUTES
    private static final byte DEVICE_SYNCHRONIZATION_TIMEOUT_SECONDS = 3;
    private static final int DEVICE_SYNCHRONIZATION_TIMEOUT_MILLISECONDS = DEVICE_SYNCHRONIZATION_TIMEOUT_SECONDS * Interval.ONE_SECOND.getNumberMilliseconds();
    private static final byte DEVICE_SYNCHRONIZATION_MAX_NUMBER_SYNCHRONIZATION_TRIES = 10;

    private static final byte DEVICE_TIMEOUT_SECONDS = 8; // Libble timeout -> 7.5 seconds.
    private static final int DEVICE_TIMEOUT_MILLISECONDS = DEVICE_TIMEOUT_SECONDS * Interval.ONE_SECOND.getNumberMilliseconds();

    //UPDATE LIST UPDATE ATTRIBUTES
    private static final int MINIMUM_TIME_UPDATE_LIST_DEVICES = Interval.ONE_SECOND.getNumberMilliseconds();
    private long mTimestampLastListUpdate = 0;
    private boolean mNeedUpdateList = true;

    //BLOCK DIALOGS
    @Nullable
    private IndeterminateProgressDialog mIndeterminateProgressDialog;
    private String mConnectionDialogDeviceAddress;

    //SECTION MANAGERS
    @Nullable
    private SectionAdapter mSectionAdapter;
    private HumigadgetListItemAdapter mConnectedDevicesAdapter;
    private HumigadgetListItemAdapter mDiscoveredDevicesAdapter;

    //STATE ATTRIBUTES
    private Menu mOptionsMenu;
    private volatile boolean mIsRequestingCharacteristicsFromPeripheral = false;

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView()");
        final View rootView = inflater.inflate(R.layout.fragment_device_scan, container, false);
        initListAdapter();
        initToggleButton(rootView);
        setHasOptionsMenu(true);
        initBackgroundListenerTablet(rootView);
        return rootView;
    }

    private void initBackgroundListenerTablet(@NonNull final View rootView) {
        if (getResources().getBoolean(R.bool.is_tablet)) {
            rootView.findViewById(R.id.scan_background).setOnTouchListener(
                    new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {
                            if (getResources().getBoolean(R.bool.is_tablet)) {
                                final MainActivity parent = (MainActivity) getParent();
                                if (parent != null && isVisible()) {
                                    parent.toggleTabletMenu();
                                }
                            }
                            return true;
                        }
                    }
            );
        }
    }

    private void initListAdapter() {
        mSectionAdapter = new SectionAdapter() {
            @Nullable
            @Override
            protected View getHeaderView(final String caption,
                                         final int itemIndex,
                                         @Nullable final View convertView,
                                         final ViewGroup parent) {
                TextView headerTextView = (TextView) convertView;
                if (convertView == null) {
                    final Typeface typefaceBold = Typeface.createFromAsset(getContext().getAssets(), "HelveticaNeueLTStd-Bd.otf");
                    headerTextView = (TextView) View.inflate(getParent(), R.layout.listitem_scan_header, null);
                    headerTextView.setTypeface(typefaceBold);
                }
                headerTextView.setText(caption);
                return headerTextView;
            }
        };
        mConnectedDevicesAdapter = new HumigadgetListItemAdapter(getContext());
        mDiscoveredDevicesAdapter = new HumigadgetListItemAdapter(getContext());

        mSectionAdapter.addSectionToAdapter(getContext().getString(R.string.label_connected), mConnectedDevicesAdapter);
        mSectionAdapter.addSectionToAdapter(getContext().getString(R.string.label_discovered), mDiscoveredDevicesAdapter);

        setListAdapter(mSectionAdapter);
    }

    private void initToggleButton(@NonNull final View rootView) {
        final ToggleButton toggleButton = (ToggleButton) rootView.findViewById(R.id.togglebutton_scan);

        toggleButton.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "HelveticaNeueLTStd-Bd.otf"));

        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (BleManager.getInstance().isBluetoothEnabled()) {
                    final ToggleButton toggleButton = (ToggleButton) rootView.findViewById(R.id.togglebutton_scan);
                    if (isChecked) {
                        BleManager.getInstance().registerNotificationListener(ScanDeviceFragment.this);
                        BleManager.getInstance().startScanning();
                        toggleButton.setBackgroundColor(getResources().getColor(R.color.sensirion_grey_dark));
                        setRefreshActionButtonState(true);
                    } else {
                        BleManager.getInstance().stopScanning();
                        toggleButton.setBackgroundColor(getResources().getColor(R.color.sensirion_green));
                        setRefreshActionButtonState(false);
                    }
                } else {
                    BleManager.getInstance().requestEnableBluetooth(getContext());
                }
            }
        });
        // start scanning immediately when fragment is active
        toggleButton.performClick();
    }

    @Override
    public void onListItemClick(final ListView l, final View v, final int position, final long id) {
        super.onListItemClick(l, v, position, id);
        final BleManager bleManager = BleManager.getInstance();
        if (bleManager.isBluetoothEnabled()) {
            Log.d(TAG, String.format("onListItemClick -> Clicked on position %s.", position));
        } else {
            bleManager.requestEnableBluetooth(getContext());
            return;
        }
        bleManager.stopScanning();
        final Object device = mSectionAdapter.getItem(position);
        if (device instanceof BleDevice) {
            onDeviceClick((BleDevice) device);
        } else {
            Log.w(TAG, "onListItemClick -> The selected device is not a BleDevice.");
            updateList();
        }
    }

    private synchronized void onDeviceClick(@Nullable final BleDevice device) {
        if (device == null) {
            Log.e(TAG, "onDeviceClick -> Device clicked on an empty BleDevice.");
            updateList();
        } else if (device.isConnected()) {
            Log.d(TAG, "onDeviceClick -> Opening manage device fragment.");
            openManageDeviceFragment(device);
        } else {
            Log.d(TAG, String.format("onDeviceClick -> Connecting to device with address %s.", device.getAddress()));
            connectDevice(device);
        }
    }

    private void openManageDeviceFragment(@NonNull final BleDevice device) {
        if (RHTHumigadgetSensorManager.getInstance().isDeviceReady(device)) {
            final ManageDeviceFragment fragment = new ManageDeviceFragment();
            fragment.init(device.getAddress());
            ((MainActivity) getParent()).changeFragment(fragment);
        } else {
            showRetrievingCharacteristicsFromDeviceProgressDialog(device);
        }
    }

    private void connectDevice(@NonNull final BleDevice device) {
        mConnectionDialogDeviceAddress = device.getAddress();
        Log.i(TAG, String.format(getString(R.string.trying_connect_device), mConnectionDialogDeviceAddress));
        showConnectionInProgressDialog(device.getAddress());
        RHTHumigadgetSensorManager.getInstance().connectPeripheral(mConnectionDialogDeviceAddress);
    }

    private void showDeviceNotReadyAlert(@NonNull final BleDevice device) {
        getParent().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getParent());
                builder.setTitle(getResources().getString(R.string.device_not_ready));
                builder
                        .setMessage(String.format(getResources().getString(R.string.device_not_ready_message), DeviceNameDatabaseManager.getInstance().readDeviceName(device.getAddress())))
                        .setCancelable(false)
                        .setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                                    public void onClick(@NonNull DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                        dialog.cancel();
                                    }
                                }
                        );
                final AlertDialog timeoutDialog = builder.create();
                timeoutDialog.show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        RHTHumigadgetSensorManager.getInstance().requestEnableBluetooth(getParent());
        BleManager.getInstance().registerNotificationListener(this);
        updateList();
        BleManager.getInstance().startScanning();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (BleManager.getInstance().isScanning()) {
            BleManager.getInstance().stopScanning();
        }
        BleManager.getInstance().unregisterNotificationListener(this);
    }

    /**
     * Method called when trying to connect to a discovered device. Blocks the window until a device
     * was received or a timeout was produced.
     *
     * @param device that wants to retrieve its characteristics from
     */
    private void showRetrievingCharacteristicsFromDeviceProgressDialog(@NonNull final BleDevice device) {
        showRetrievingCharacteristicsProgressDialog(device);
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                byte numberTries = 0;
                while (++numberTries < DEVICE_SYNCHRONIZATION_MAX_NUMBER_SYNCHRONIZATION_TRIES) {
                    try {
                        RHTHumigadgetSensorManager.getInstance().synchronizeDeviceServices(device);
                        Thread.sleep(DEVICE_SYNCHRONIZATION_TIMEOUT_MILLISECONDS);
                        break;
                    } catch (@NonNull final InterruptedException e) {
                        Log.e(TAG, "showConnectionInProgressDialog -> An interrupted exception was produced when connecting to peripheral -> ", e);
                    }
                }
                if (mIndeterminateProgressDialog != null && mIndeterminateProgressDialog.isShowing()) {
                    mIndeterminateProgressDialog.dismiss();
                    mIndeterminateProgressDialog = null;
                }
                if (RHTHumigadgetSensorManager.getInstance().isDeviceReady(device)) {
                    openManageDeviceFragment(device);
                } else {
                    showDeviceNotReadyAlert(device);
                }
            }
        });
    }

    private void showRetrievingCharacteristicsProgressDialog(@NonNull final BleDevice device) {
        final String title = getString(R.string.please_wait);
        final String deviceName = DeviceNameDatabaseManager.getInstance().readDeviceName(device.getAddress());
        final String message = String.format(getString(R.string.asking_for_unknown_device_characteristics), deviceName);
        final boolean isCancelable = false;
        mIndeterminateProgressDialog = new IndeterminateProgressDialog(getParent(), title, message, isCancelable);
        mIndeterminateProgressDialog.show(getParent());
    }

    /**
     * Method called when trying to connect to a discovered device. Blocks the window until a device
     * was received or a timeout was produced.
     *
     * @param deviceAddress of the device.
     */
    private void showConnectionInProgressDialog(@NonNull final String deviceAddress) {
        final String title = getString(R.string.please_wait);
        final String deviceName = DeviceNameDatabaseManager.getInstance().readDeviceName(deviceAddress);
        final String message = String.format(getString(R.string.connecting_to_device), deviceName);
        final boolean isCancelable = false;

        mIndeterminateProgressDialog = new IndeterminateProgressDialog(getParent(), title, message, isCancelable);
        mIndeterminateProgressDialog.show(getParent());

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                int timeWaited = 0;
                final int timePerCheck = 40;
                while (isStillConnecting(timeWaited)) {
                    try {
                        Thread.sleep(timePerCheck);
                        timeWaited += timePerCheck;
                    } catch (@NonNull final InterruptedException e) {
                        Log.e(TAG, "showConnectionInProgressDialog -> An interrupted exception was produced when connecting to peripheral -> ", e);
                    }
                    if (mIsRequestingCharacteristicsFromPeripheral) {
                        if (mIndeterminateProgressDialog == null) {
                            Log.e(TAG, "showConnectionInProgressDialog -> Progress dialog is already canceled.");
                        } else {
                            final String message = String.format(getString(R.string.asking_for_unknown_device_characteristics), deviceName);
                            mIndeterminateProgressDialog.setMessage(message, getParent());
                        }
                    }
                }
                dismissConnectingProgressDialog(deviceAddress);
            }
        });
    }

    private boolean isStillConnecting(final int timeWaited) {
        if (mIndeterminateProgressDialog != null && mIndeterminateProgressDialog.isShowing()) {
            if (timeWaited < DEVICE_TIMEOUT_MILLISECONDS) {
                return true;
            } else if (BleManager.getInstance().isDeviceConnected(mConnectionDialogDeviceAddress)) {
                return mIsRequestingCharacteristicsFromPeripheral;
            } else {
                mIsRequestingCharacteristicsFromPeripheral = false;
            }
        }
        return false;
    }

    private void dismissConnectingProgressDialog(@NonNull final String deviceAddress) {
        if (mIndeterminateProgressDialog != null) {
            if (mIndeterminateProgressDialog.isShowing() && BleManager.getInstance().getConnectedDevice(deviceAddress) == null) {
                onConnectionTimeout(deviceAddress);
            } else {
                mIndeterminateProgressDialog.dismiss();
                mIndeterminateProgressDialog = null;
            }
        }
    }

    /**
     * In case of timeout when connecting to a device this dialog will appear.
     *
     * @param deviceAddress of the device.
     */
    private void onConnectionTimeout(@NonNull final String deviceAddress) {
        if (mIsRequestingCharacteristicsFromPeripheral) {
            return;
        }
        if (mIndeterminateProgressDialog == null) {
            Log.w(TAG, "onConnectionTimeout -> mIndeterminateProgressDialog is null. Not showing the connection timeout dialog.");
            return;
        }
        mIndeterminateProgressDialog.dismiss();
        mIndeterminateProgressDialog = null;

        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getParent());
                builder.setTitle(getResources().getString(R.string.connection_timeout));
                final String deviceName = DeviceNameDatabaseManager.getInstance().readDeviceName(deviceAddress);
                builder
                        .setMessage(String.format("%s %s", getResources().getString(R.string.timeout_produced), deviceName))
                        .setCancelable(false)
                        .setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                                    public void onClick(@NonNull DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                        dialog.cancel();
                                    }
                                }
                        );
                final AlertDialog timeoutDialog = builder.create();
                timeoutDialog.show();
                updateList();
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull final Menu menu, @NonNull final MenuInflater inflater) {
        menu.clear();
        mOptionsMenu = menu;
        inflater.inflate(R.menu.refresh_action_bar, menu);
        setRefreshActionButtonState(true);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.scan_device_refresh:
                final BleManager bleManager = BleManager.getInstance();
                if (bleManager.isBluetoothEnabled()) {
                    if (bleManager.isScanning()) {
                        bleManager.stopScanning();
                        setRefreshActionButtonState(false);
                    } else {
                        bleManager.startScanning();
                        setRefreshActionButtonState(true);
                    }
                    return true;
                } else {
                    bleManager.requestEnableBluetooth(getContext());
                }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Shows or hides the refresh button in dependence if it's scanning or not.
     *
     * @param refreshing <code>true</code> for making it visible - <code>false</code> otherwise.
     */
    public void setRefreshActionButtonState(final boolean refreshing) {
        if (mOptionsMenu != null) {
            final MenuItem refreshItem = mOptionsMenu.findItem(R.id.scan_device_refresh);
            if (refreshItem != null) {
                if (refreshing) {
                    refreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
                } else {
                    refreshItem.setActionView(null);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDeviceConnected(@NonNull final BleDevice device) {
        Log.i(TAG, String.format("onDeviceConnected -> Received connected device with address: %s.", device.getAddress()));
        onDeviceConnectionStateChange(device.getAddress());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDeviceDisconnected(@NonNull final BleDevice device) {
        final String deviceAddress = device.getAddress();
        Log.i(TAG, String.format("onDeviceConnected -> %s has lost the connected with the device: %s ", TAG, deviceAddress));
        onDeviceConnectionStateChange(deviceAddress);
        if (mConnectionDialogDeviceAddress != null && mConnectionDialogDeviceAddress.equals(deviceAddress)) {
            dismissConnectingProgressDialog(deviceAddress);
        }
    }

    private void onDeviceConnectionStateChange(@NonNull final String deviceAddress) {
        Log.i(TAG, "mDeviceConnectionReceiver.onDeviceConnectionStateChange() -> refreshing discovered and connected list!");

        if (mConnectionDialogDeviceAddress != null && mConnectionDialogDeviceAddress.equals(deviceAddress)) {
            if (BleManager.getInstance().isDeviceConnected(deviceAddress)) {
                mIsRequestingCharacteristicsFromPeripheral = true;
            } else if (mIsRequestingCharacteristicsFromPeripheral) {
                onConnectionTimeout(deviceAddress);
                mIsRequestingCharacteristicsFromPeripheral = false;
            }
        }
        mNeedUpdateList = true;
        updateList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDeviceDiscovered(@NonNull final BleDevice device) {
        Log.i(TAG, String.format("onDeviceDiscovered -> Received discovered device with address %s and advertise name %s.", device.getAddress(), device.getAdvertisedName()));
        updateList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDeviceAllServicesDiscovered(@NonNull final BleDevice device) {
        Log.i(TAG, String.format("onDeviceAllServiceDiscovered -> Device %s has discovered all its services.", device.getAddress()));
        if (mIndeterminateProgressDialog != null && mIndeterminateProgressDialog.isShowing()) {
            if (device.isConnected()) {
                Executors.newSingleThreadExecutor().execute(
                        new Runnable() {
                            @Override
                            public void run() {
                                waitForServiceSynchronization(device);
                                mIsRequestingCharacteristicsFromPeripheral = false;
                            }
                        }
                );
            }
        }
    }

    private void waitForServiceSynchronization(@NonNull final BleDevice device) {
        int tryNumber = 0;
        while (!RHTHumigadgetSensorManager.getInstance().isDeviceReady(device) && tryNumber++ < DEVICE_SYNCHRONIZATION_MAX_NUMBER_SYNCHRONIZATION_TRIES) {
            RHTHumigadgetSensorManager.getInstance().synchronizeDeviceServices(device);
            try {
                Thread.sleep(DEVICE_SYNCHRONIZATION_TIMEOUT_MILLISECONDS);
            } catch (@NonNull final InterruptedException ignored) {
            }
            if (RHTHumigadgetSensorManager.getInstance().isDeviceReady(device)) {
                Log.i(TAG, String.format("onDeviceAllServiceDiscovered -> Device %s is synchronized.", device.getAddress()));
            } else {
                Log.w(TAG, String.format("onDeviceAllServiceDiscovered -> Device %s is not synchronized yet.", device.getAddress()));
            }
        }
    }

    private synchronized void updateList() {
        if (mNeedUpdateList || mTimestampLastListUpdate + MINIMUM_TIME_UPDATE_LIST_DEVICES > System.currentTimeMillis()) {
            Log.d(TAG, "updateList -> Updating device list.");
            mNeedUpdateList = false;
            mTimestampLastListUpdate = System.currentTimeMillis();
            final Iterable<? extends BleDevice> connectedDevices = BleManager.getInstance().getConnectedBleDevices();
            final Iterable<? extends BleDevice> discoveredDevices = BleManager.getInstance().getDiscoveredBleDevices(KnownDevices.RHT_GADGETS.getAdvertisedNames());

            getParent().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mConnectedDevicesAdapter.clear();
                    mConnectedDevicesAdapter.addAll(connectedDevices);
                    mDiscoveredDevicesAdapter.clear();
                    mDiscoveredDevicesAdapter.addAll(discoveredDevices);
                    setListAdapter(mSectionAdapter);
                }
            });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onScanStateChanged(final boolean isScanEnabled) {
        if (getView() == null) {
            Log.w(TAG, "mScanStateReceiver.onReceive -> getView() produced a null value.");
            return;
        } else if (getView().findViewById(R.id.togglebutton_scan) == null) {
            Log.w(TAG, "mScanStateReceiver.onReceive -> getView().findViewById(R.id.toggleButton_scan) produced a null value.");
            return;
        }

        final ToggleButton scanButton = (ToggleButton) getView().findViewById(R.id.togglebutton_scan);

        if (isScanEnabled) {
            Log.i(TAG, "mScanStateReceiver.onReceive() -> scanning STARTED.");
            scanButton.setChecked(true);
            setRefreshActionButtonState(true);
        } else {
            Log.i(TAG, "mScanStateReceiver.onReceive() -> scanning STOPPED.");
            scanButton.setChecked(false);
            setRefreshActionButtonState(false);
        }
        mNeedUpdateList = true;
    }
}