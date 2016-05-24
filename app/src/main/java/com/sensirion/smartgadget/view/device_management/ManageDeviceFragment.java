package com.sensirion.smartgadget.view.device_management;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.sensirion.libble.BleManager;
import com.sensirion.libble.devices.BleDevice;
import com.sensirion.libble.devices.BlePeripheralService;
import com.sensirion.libble.listeners.history.HistoryListener;
import com.sensirion.libble.services.AbstractHistoryService;
import com.sensirion.libble.services.generic.BatteryService;
import com.sensirion.smartgadget.R;
import com.sensirion.smartgadget.peripheral.rht_sensor.RHTSensorFacade;
import com.sensirion.smartgadget.persistence.device_name_database.DeviceNameDatabaseManager;
import com.sensirion.smartgadget.utils.DeviceModel;
import com.sensirion.smartgadget.utils.Interval;
import com.sensirion.smartgadget.utils.TimeFormatter;
import com.sensirion.smartgadget.utils.download.DownloadLogHelper;
import com.sensirion.smartgadget.utils.download.LoggerInterval;
import com.sensirion.smartgadget.utils.view.ParentFragment;
import com.sensirion.smartgadget.view.MainActivity;

import butterknife.BindBool;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

public class ManageDeviceFragment extends ParentFragment implements HistoryListener {

    // Class TAG used for testing
    @NonNull
    private static final String TAG = ManageDeviceFragment.class.getSimpleName();

    // Broadcast Views
    @Nullable
    private final BroadcastReceiver mDeviceConnectionReceiver = new ConnectionStateReceiver();

    // XML Resources
    @BindBool(R.bool.is_tablet)
    boolean IS_TABLET;
    @BindString(R.string.enable_logging)
    String ENABLE_LOGGING_STRING;
    @BindString(R.string.label_advice_logging_enable)
    String GADGET_ENABLE_ADVICE_STRING;
    @BindString(R.string.interval_modification)
    String INTERVAL_MODIFICATION_TITLE;
    @BindString(R.string.interval_modification_message)
    String INTERVAL_MODIFICATION_MESSAGE;
    @BindString(R.string.yes)
    String YES_STRING;
    @BindString(R.string.no)
    String NO_STRING;
    @BindString(R.string.typeface_condensed)
    String TYPEFACE_CONDENSED_LOCATION;
    @BindString(R.string.typeface_bold)
    String TYPEFACE_BOLD_LOCATION;

    // XML Views
    @BindView(R.id.manage_device_gadget_name_label)
    TextView mGadgetNameLabel;
    @BindView(R.id.manage_device_label_logging_interval)
    TextView mLoggingIntervalLabel;
    @BindView(R.id.manage_device_label_gadget_logging)
    TextView mGadgetLoggingLabel;
    @BindView(R.id.manage_device_gadget_name_edit_field)
    EditText mGadgetNameEditText;
    @BindView(R.id.manage_device_button_disconnect)
    Button mDisconnectButton;
    @BindView(R.id.manage_device_button_download_log)
    Button mDownloadLogButton;
    @BindView(R.id.manage_device_button_logging_interval)
    Button mLoggingIntervalButton;
    @BindView(R.id.manage_device_layout_gadgetlogging_switch_layout)
    LinearLayout mGadgetLoggingSwitch;
    @BindView(R.id.manage_device_battery_level_layout)
    RelativeLayout mBatteryLevelLayout;
    @BindView(R.id.manage_device_battery_level_value)
    TextView mBatteryLevelValue;
    @BindView(R.id.manage_device_battery_seek_bar)
    SeekBar mBatterySeekBar;
    @BindView(R.id.manage_device_switch_toggle_logger)
    Switch mLoggingToggle;
    // BleDevice connectors
    @Nullable
    private BleDevice mSelectedDevice;
    @Nullable
    private DeviceModel mDeviceModel;
    @Nullable
    private AbstractHistoryService mHistoryService = null;
    // Fragment states
    private volatile boolean mCheckingDeviceState = false;
    private volatile boolean mFirstTimeRead = true;

    /**
     * This method should be called before starting the class.
     *
     * @param deviceAddress device of the device that the users wants to manage.
     */
    public void init(@NonNull final String deviceAddress) {
        mSelectedDevice = BleManager.getInstance().getConnectedDevice(deviceAddress);
        mDeviceModel = RHTSensorFacade.getInstance().getDeviceModel(deviceAddress);
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_manage_device, container, false);
        ButterKnife.bind(this, view);
        view.setOnTouchListener(new OnTouchOpenTabletMenuListener());
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isSelectedDeviceAvailable()) {
            BleManager.getInstance().registerNotificationListener(this);
            checkInterval();
            checkLoggingIsEnabled();
            checkDownloadPermission();
        } else {
            closeScreen();
        }
        if (mFirstTimeRead) {
            Log.i(TAG, "onResume -> First time -> trying to get connected gadget from BluetoothLeService");
            mFirstTimeRead = false;
            synchronizeSelectedDeviceSettings();
            initUiElements();
        }
    }

    @Override
    public void onStop() {
        BleManager.getInstance().unregisterNotificationListener(this);
        super.onStop();
    }

    private boolean isSelectedDeviceAvailable() {
        if (mSelectedDevice == null) {
            Log.e(TAG, "isSelectedDeviceAvailable -> Could not find Humigadget from Intent.");
            return false;
        }
        if (mDeviceModel == null) {
            Log.e(TAG, "isSelectedDeviceAvailable -> Sensor facade does not obtain values from the device.");
            return false;
        }
        if (mSelectedDevice.isConnected()) {
            return true;
        }
        Log.e(TAG, "isSelectedDeviceAvailable -> Device is disconnected.");
        return false;
    }

    private void synchronizeSelectedDeviceSettings() {
        initLoggingService();
        checkDeviceState();
        updateBatteryLevel();
        if (mHistoryService == null) {
            Log.i(TAG, "onResume -> The device is not compatible with logging service.");
            mGadgetLoggingLabel.setVisibility(View.GONE);
        } else {
            checkInterval();
            checkLoggingIsEnabled();
            checkDownloadPermission();
        }
        enableUiElementsForConnectedGadget();
        registerDeviceStateListener();
    }

    private void closeScreen() {
        final Activity activity = getParent();
        if (activity instanceof MainActivity) {
            ((MainActivity) activity).changeFragment(new ScanDeviceFragment());
        }
    }

    private void registerDeviceStateListener() {
        final IntentFilter filterDeviceState = new IntentFilter();
        filterDeviceState.addAction(BlePeripheralService.ACTION_PERIPHERAL_DISCOVERY);
        filterDeviceState.addAction(BlePeripheralService.ACTION_PERIPHERAL_CONNECTION_CHANGED);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mDeviceConnectionReceiver, filterDeviceState);
    }

    private void onGadgetDisconnected() {
        Log.d(TAG, "onGadgetDisconnected -> was called");
        closeScreen();
    }

    private void unregisterDeviceState() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mDeviceConnectionReceiver);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterDeviceState();
    }

    private void enableUiElementsForConnectedGadget() {
        initButtonDisconnect();
        initGadgetName();
        mGadgetNameEditText.setEnabled(true);
        mDisconnectButton.setEnabled(true);
    }

    private void initButtonDisconnect() {
        mDisconnectButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(@NonNull final View v) {
                if (mSelectedDevice == null) {
                    Log.e(TAG, "initButtonDisconnect -> Selected device cannot be null");
                    closeScreen();
                    return;
                }
                BleManager.getInstance().disconnectDevice(mSelectedDevice.getAddress());
                closeScreen();
            }
        });
    }

    private void initGadgetName() {
        if (mSelectedDevice == null) {
            Log.e(TAG, "initGadget -> The selected device cannot be null");
            return;
        }
        if (mDeviceModel == null) {
            Log.e(TAG, "initGadget -> The device model cannot be null");
            return;
        }
        final String deviceAddress = mSelectedDevice.getAddress();
        final String deviceName = DeviceNameDatabaseManager.getInstance().readDeviceName(deviceAddress);
        mGadgetNameEditText.setText(deviceName);
        mDeviceModel.setDisplayName(deviceName);
        mGadgetNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                final String displayName = mGadgetNameEditText.getText().toString();
                mDeviceModel.setDisplayName(displayName);
                DeviceNameDatabaseManager.getInstance().updateDeviceName(deviceAddress, displayName);
            }
        });
    }

    private void initUiElements() {
        initCustomFonts();
        initIntervalChooser();
        initLoggingSwitch();
        initBatteryBar();
        initDownloadButton();
    }

    private void initCustomFonts() {
        final AssetManager assets = getContext().getAssets();
        final Typeface typefaceNormal = Typeface.createFromAsset(assets, TYPEFACE_CONDENSED_LOCATION);
        final Typeface typefaceBold = Typeface.createFromAsset(assets, TYPEFACE_BOLD_LOCATION);
        mGadgetNameLabel.setTypeface(typefaceBold);
        mBatteryLevelValue.setTypeface(typefaceBold);
        mLoggingIntervalLabel.setTypeface(typefaceBold);
        mGadgetLoggingLabel.setTypeface(typefaceBold);
        mGadgetNameEditText.setTypeface(typefaceNormal);
        mDisconnectButton.setTypeface(typefaceNormal);
        mDownloadLogButton.setTypeface(typefaceNormal);
        mLoggingIntervalButton.setTypeface(typefaceBold);
    }

    private void initIntervalChooser() {
        mLoggingIntervalButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(@NonNull final View v) {
                initLoggingService();
                if (mHistoryService == null) {
                    Log.e(TAG, "initIntervalChooser.onClick -> device cannot find the logging service.");
                }
                checkDeviceState();
                final Integer numberElements = mHistoryService.getNumberLoggedElements();
                if (numberElements != null && numberElements > 0) {
                    showAdviceWhenModifyingInterval();
                } else {
                    showIntervalSelector(mLoggingIntervalButton);
                }
            }

            /**
             * In case the user wants to modify the interval when the device has data, it sends an advice
             * to the user informing that the data will be deleted.
             */
            private void showAdviceWhenModifyingInterval() {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(INTERVAL_MODIFICATION_TITLE);
                builder.setCancelable(false);
                builder.setMessage(INTERVAL_MODIFICATION_MESSAGE);
                builder.setPositiveButton(YES_STRING, new DialogInterface.OnClickListener() {
                    public void onClick(@NonNull final DialogInterface dialog, final int which) {
                        if (mHistoryService != null && mHistoryService.isLoggingStateEditable()) {
                            mHistoryService.resetDeviceData();
                        }
                        dialog.cancel();
                        showIntervalSelector(mLoggingIntervalButton);
                    }
                });
                builder.setNegativeButton(NO_STRING, new DialogInterface.OnClickListener() {
                    public void onClick(@NonNull final DialogInterface dialog, final int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }

            private void showIntervalSelector(@NonNull final Button button) {
                final AlertDialog.Builder builder = new Builder(getContext());
                builder.setCancelable(false)
                        .setTitle(R.string.title_button_choice)
                        .setItems(R.array.array_interval_choices, new DialogInterface.OnClickListener() {
                            public void onClick(@NonNull DialogInterface dialog, int which) {
                                if (mHistoryService == null) {
                                    Log.e(TAG, "showIntervalSelected -> History service is null");
                                    dialog.dismiss();
                                    return;
                                }
                                checkDeviceState();
                                final LoggerInterval interval = LoggerInterval.fromNumberElement(which);
                                if (interval == null) {
                                    Log.e(TAG, "showIntervalSelected.onClick -> Interval is null");
                                    dialog.dismiss();
                                    return;
                                }
                                Log.i(TAG,
                                        String.format(
                                                "%s -> User selected interval %s in seconds.",
                                                "showIntervalSelector.onClickListener",
                                                interval.getValueInSeconds()
                                        )
                                );
                                mHistoryService.setDownloadInterval(interval.getValueInMilliseconds());
                                button.setText(interval.toStringLabel(getContext().getApplicationContext()));
                                PreferenceManager.
                                        getDefaultSharedPreferences(getContext().getApplicationContext()).
                                        edit().
                                        putInt(String.valueOf(button.getId()), which).
                                        commit();
                                dialog.dismiss();
                            }
                        });
                final AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    private void initLoggingSwitch() {
        initLoggingService();
        if (mHistoryService == null) {
            Log.e(TAG, "initLoggingSwitch -> Logging service is not enabled yet.");
            return;
        }
        if (mHistoryService.isLoggingStateEditable()) {
            mLoggingToggle.setChecked(mHistoryService.isGadgetLoggingEnabled());
            if (mHistoryService.isDownloadInProgress()) {
                mLoggingToggle.setEnabled(false);
            } else {
                mLoggingToggle.setEnabled(true);
            }
            mLoggingToggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                    if (isChecked) {
                        initLoggingAdviceAlert();
                        mLoggingIntervalButton.setEnabled(false);
                        mDownloadLogButton.setEnabled(true);
                    } else {
                        mHistoryService.setLoggingState(false);
                        mLoggingIntervalButton.setEnabled(true);
                        checkDownloadPermission();
                    }
                }
            });
        } else {
            mGadgetLoggingSwitch.setVisibility(View.GONE);
        }
    }

    private void initLoggingAdviceAlert() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getParent());

        builder.setTitle(ENABLE_LOGGING_STRING);
        builder.setCancelable(false);
        builder.setMessage(GADGET_ENABLE_ADVICE_STRING);
        builder.setPositiveButton(YES_STRING, new DialogInterface.OnClickListener() {
            public void onClick(@NonNull final DialogInterface dialog, final int which) {
                dialog.cancel();
                if (mHistoryService != null) {
                    mHistoryService.setLoggingState(true);
                }
                mLoggingIntervalButton.setEnabled(false);
            }
        });
        builder.setNegativeButton(NO_STRING, new DialogInterface.OnClickListener() {
            public void onClick(@NonNull final DialogInterface dialog, final int which) {
                dialog.cancel();
                mLoggingToggle.setChecked(false);
            }
        });
        builder.show();
    }

    private void initBatteryBar() {
        updateBatteryLevel();
        mBatterySeekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(@NonNull View view, MotionEvent motionEvent) {
                checkDeviceState();
                updateBatteryLevel();
                return view.performClick();
            }
        });
    }

    private void initDownloadButton() {
        if (mHistoryService == null) {
            Log.e(TAG, "initDownloadButton -> History service is null");
            mDownloadLogButton.setEnabled(false);
            return;
        }
        mDownloadLogButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                checkDeviceState();
                final Activity parent = getParent();
                if (parent == null) {
                    Log.e(TAG, "initDownloadButton -> Parent is null");
                    return;
                }
                parent.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLoggingToggle.setEnabled(false);
                        mLoggingIntervalButton.setEnabled(false);
                        final Activity parent = getParent();
                        if (parent == null) {
                            Log.e(TAG, "initDownloadButton -> Parent is null");
                            return;
                        }
                        DownloadLogHelper.getInstance().downloadLoggedData(parent, mHistoryService, new Handler());
                    }
                });
            }
        });
        mDownloadLogButton.setEnabled(hasValuesToDownload());
    }

    private void updateBatteryLevel() {
        if (mSelectedDevice == null) {
            Log.e(TAG, "updateBatteryLevel -> Cannot obtain the battery level.");
            closeScreen();
            return;
        }
        final Integer batteryLevel = getBatteryLevel();
        if (batteryLevel == null) {
            mBatteryLevelLayout.setVisibility(View.GONE);
        } else {
            mBatteryLevelLayout.setVisibility(View.VISIBLE);
            Log.i(TAG, String.format("updateBatteryLevel -> Battery it's at %d%%", batteryLevel));
            mBatteryLevelValue.setText(String.format("%d%%", batteryLevel));
            mBatterySeekBar.setProgress(batteryLevel);
            mBatterySeekBar.setEnabled(false);
        }
    }

    @Nullable
    private Integer getBatteryLevel() {
        if (mSelectedDevice == null) {
            Log.e(TAG, "getBatteryLevel -> Selected device is null.");
            closeScreen();
            return null;
        }
        final BatteryService service = mSelectedDevice.getDeviceService(BatteryService.class);
        if (service == null) {
            Log.e(TAG, "getBatteryLevel -> Battery Service was not found.");
            return null;
        }
        return service.getBatteryLevel();
    }

    private void checkLoggingIsEnabled() {
        if (mSelectedDevice == null) {
            Log.e(TAG, "checkLoggingIsEnabled -> Selected Device cannot be null");
            closeScreen();
            return;
        }
        initLoggingService();
        if (mHistoryService == null) {
            mHistoryService = BleManager.getInstance().getConnectedDevice(mSelectedDevice.getAddress()).getHistoryService();
            if (mHistoryService == null) {
                Log.e(TAG, "checkLoggingIsEnabled -> Logging service can't be initialized.");
                return;
            }
        }
        if (mHistoryService.isLoggingStateEditable()) {
            final boolean isLoggingEnabled = mHistoryService.isGadgetLoggingEnabled();
            Log.i(TAG,
                    String.format(
                            "checkLoggingIsEnabled -> Logging in device %s is %s.",
                            mSelectedDevice.getAddress(),
                            (isLoggingEnabled) ? "enabled" : "disabled")
            );
            mLoggingToggle.setChecked(isLoggingEnabled);
            mLoggingToggle.setEnabled(!mHistoryService.isDownloadInProgress());
        }
    }

    private void checkInterval() {
        if (mHistoryService == null) {
            Log.i(TAG, "checkInterval -> The device doesn't have a valid logging service.");
            return;
        }
        final Integer intervalMs = mHistoryService.getLoggingIntervalMs();
        if (intervalMs == null) {
            Log.e(TAG, "checkInterval -> Logged data interval is not known yet. (HINT -> Synchronize the history service)");
            return;
        }
        final int intervalSeconds = intervalMs / Interval.ONE_SECOND.getNumberMilliseconds();
        Log.i(TAG, String.format("checkInterval() ->  Interval is at %d seconds.", intervalSeconds));
        mLoggingIntervalButton.setText(new TimeFormatter(intervalSeconds).getShortTime(getContext().getApplicationContext()));
        if (mHistoryService.isDownloadInProgress()) {
            Log.d(TAG, "checkInterval -> During a download interval can't be enabled.");
            mLoggingIntervalButton.setEnabled(false);
        } else if (mHistoryService.isLoggingStateEditable() && mHistoryService.isGadgetLoggingEnabled()) {
            Log.d(TAG, "checkInterval -> Device is logging data, interval can't be modified.");
            mLoggingIntervalButton.setEnabled(false);
        } else {
            mLoggingIntervalButton.setEnabled(true);
        }
    }

    /**
     * Checks if a device is connected, in case it's not connected it close the activity.
     */
    private void checkDeviceState() {
        if (mCheckingDeviceState) {
            return;
        }
        mCheckingDeviceState = true;
        if (mSelectedDevice != null && mSelectedDevice.isConnected()) {
            Log.i(TAG,
                    String.format(
                            "checkDeviceState -> Device with address %s is connected.",
                            mSelectedDevice.getAddress()
                    )
            );
        } else {
            Log.e(TAG, "checkDeviceState -> Device is not connected, closing activity.");
            closeScreen();
        }
    }

    private boolean initLoggingService() {
        if (mSelectedDevice == null) {
            Log.e(TAG, "initLoggingService -> Device cannot be null.");
            closeScreen();
            return false;
        }
        try {
            if (mHistoryService == null) {
                mHistoryService = mSelectedDevice.getHistoryService();
                if (mHistoryService == null) {
                    Log.e(TAG, "initLoggingService -> Logging service was not found.");
                } else {
                    return true;
                }
            } else {
                Log.i(TAG, "initLoggingService -> Logging service initialized correctly.");
            }
        } catch (@NonNull final Exception e) {
            Log.e(TAG, "initLoggingService -> The following error was thrown -> ", e);
        }
        return false;
    }

    /**
     * Checks if the user has permission to download. (Has values to log)
     */
    private void checkDownloadPermission() {
        if (mHistoryService == null) {
            Log.d(TAG, "checkDownloadPermission -> The device do not have logging capabilities");
        } else {
            Log.d(TAG, "checkDownloadPermission -> Checking download permission.");
            if (mHistoryService.isDownloadInProgress()) {
                mDownloadLogButton.setEnabled(false);
            } else {
                mDownloadLogButton.setEnabled(hasValuesToDownload());
            }
        }
    }

    /**
     * Checks if they are values to log.
     *
     * @return <code>true</code> if there values - <code>false</code> otherwise.
     */
    private boolean hasValuesToDownload() {
        if (mSelectedDevice == null) {
            Log.e(TAG, "hasValuesToDownload -> Selected device is null");
            closeScreen();
            return false;
        }
        if (mHistoryService == null) {
            Log.e(TAG, "hasValuesToDownload -> Logging service it's not initialized yet.");
            return false;
        }
        if (mHistoryService.isDownloadInProgress()) {
            Log.d(TAG, "hasValuesToDownload -> User is downloading data from the device.");
            return false;
        }
        final Integer numberLoggedElements = mHistoryService.getNumberLoggedElements();
        if (numberLoggedElements == null) {
            Log.e(TAG, "hasValuesToDownload -> It was impossible to retrieve the number of elements.");
            return false;
        } else {
            Log.i(TAG,
                    String.format(
                            "hasValuesToDownload -> The device %s has %d elements to download.",
                            mSelectedDevice.getAddress(),
                            numberLoggedElements
                    )
            );
        }
        return numberLoggedElements > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDownloadProgress(@NonNull final BleDevice device, final int downloadProgress) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAmountElementsToDownload(@NonNull final BleDevice device, final int amount) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLogDownloadFailure(@NonNull final BleDevice device) {
        if (mSelectedDevice != null && device.equals(mSelectedDevice)) {
            onLogDownloadFinished();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLogDownloadCompleted(@NonNull final BleDevice device) {
        if (mSelectedDevice != null && device.equals(mSelectedDevice)) {
            onLogDownloadFinished();
        }
    }

    private void onLogDownloadFinished() {
        final Activity parent = getParent();
        if (parent == null) {
            Log.e(TAG, "onLogDownloadFinished -> cannot obtain the parent activity.");
            return;
        }
        parent.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mHistoryService != null && mHistoryService.isLoggingStateEditable()) {
                    mLoggingToggle.setEnabled(true);
                }
                mLoggingIntervalButton.setEnabled(true);
            }
        });
    }

    private class ConnectionStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(@NonNull final Context context, @NonNull final Intent intent) {
            Log.i(TAG, "mDeviceConnectionReceiver.onReceive() -> refreshing discovered and connected list");
            if (mSelectedDevice == null) {
                Log.e(TAG, "mDeviceConnectionReceiver.onReceive() -> Selected device cannot be null.");
                return;
            }
            if (BleManager.getInstance().getConnectedDevice(mSelectedDevice.getAddress()) == null) {
                Log.e(TAG,
                        String.format(
                                "%s -> The device with address %s has been disconnected.",
                                "mDeviceConnectionReceiver.onReceive()",
                                mSelectedDevice.getAddress()
                        )
                );
                onGadgetDisconnected();
            }
        }
    }

    public class OnTouchOpenTabletMenuListener implements View.OnTouchListener {
        public boolean onTouch(@NonNull final View v, @NonNull final MotionEvent event) {
            if (IS_TABLET) {
                final MainActivity parent = (MainActivity) getParent();
                if (parent == null) {
                    final String mainActivityName = MainActivity.class.getSimpleName();
                    Log.e(TAG, String.format("onCreateView -> Cannot obtain the %s", mainActivityName));
                } else {
                    parent.toggleTabletMenu();
                }
            }
            return true;
        }
    }
}