package com.sensirion.smartgadget.view.device_management;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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


public class ManageDeviceFragment extends ParentFragment implements HistoryListener {

    private static final String TAG = ManageDeviceFragment.class.getSimpleName();
    private EditText mGadgetEditText;
    private BleDevice mSelectedDevice;
    @Nullable
    private final BroadcastReceiver mDeviceConnectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            Log.i(TAG, "mDeviceConnectionReceiver.onReceive() -> refreshing discovered and connected list");
            if (BleManager.getInstance().getConnectedDevice(mSelectedDevice.getAddress()) == null) {
                Log.e(TAG, String.format("mDeviceConnectionReceiver.onReceive() -> The device with address %s has been disconnected.", mSelectedDevice.getAddress()));
                onGadgetDisconnected();
            }
        }
    };
    private DeviceModel mDeviceModel;
    @Nullable
    private AbstractHistoryService mHistoryService = null;

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
    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_manage_device, container, false);
        root.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (getParent().getResources().getBoolean(R.bool.is_tablet)) {
                    ((MainActivity) getParent()).toggleTabletMenu();
                }
                return true;
            }
        });
        return root;
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
            getParent().findViewById(R.id.manage_device_layout_gadget_logging).setVisibility(View.GONE);
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
        LocalBroadcastManager.getInstance(getParent()).registerReceiver(mDeviceConnectionReceiver, filterDeviceState);
    }

    private void onGadgetDisconnected() {
        Log.d(TAG, "onGadgetDisconnected -> was called");
        closeScreen();
    }

    private void unregisterDeviceState() {
        LocalBroadcastManager.getInstance(getParent()).unregisterReceiver(mDeviceConnectionReceiver);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterDeviceState();
    }

    private void enableUiElementsForConnectedGadget() {
        initButtonDisconnect();
        initGadgetName();
        getParent().findViewById(R.id.manage_device_gadget_name_edit_field).setEnabled(true);
        getParent().findViewById(R.id.manage_device_button_disconnect).setEnabled(true);
    }

    private void initButtonDisconnect() {
        getParent().findViewById(R.id.manage_device_button_disconnect).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                BleManager.getInstance().disconnectDevice(mSelectedDevice.getAddress());
                closeScreen();
            }
        });
    }

    private void initGadgetName() {
        mGadgetEditText = (EditText) getParent().findViewById(R.id.manage_device_gadget_name_edit_field);
        final String deviceAddress = mSelectedDevice.getAddress();
        final String deviceName = DeviceNameDatabaseManager.getInstance().readDeviceName(deviceAddress);
        mGadgetEditText.setText(deviceName);
        mDeviceModel.setDisplayName(deviceName);
        mGadgetEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                final String displayName = mGadgetEditText.getText().toString();
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
        Typeface typefaceNormal = Typeface.createFromAsset(getParent().getAssets(), "HelveticaNeueLTStd-Cn.otf");
        Typeface typefaceBold = Typeface.createFromAsset(getParent().getAssets(), "HelveticaNeueLTStd-Bd.otf");
        ((TextView) getParent().findViewById(R.id.manage_device_gadget_name_label)).setTypeface(typefaceBold);
        ((TextView) getParent().findViewById(R.id.manage_device_battery_level_label)).setTypeface(typefaceBold);
        ((TextView) getParent().findViewById(R.id.manage_device_label_logging_interval)).setTypeface(typefaceBold);
        ((TextView) getParent().findViewById(R.id.manage_device_label_gadget_logging)).setTypeface(typefaceBold);
        ((EditText) getParent().findViewById(R.id.manage_device_gadget_name_edit_field)).setTypeface(typefaceNormal);
        ((Button) getParent().findViewById(R.id.manage_device_button_disconnect)).setTypeface(typefaceNormal);
        ((Button) getParent().findViewById(R.id.manage_device_button_download_log)).setTypeface(typefaceNormal);
    }

    private void initIntervalChooser() {
        final Button logInterval = (Button) getParent().findViewById(R.id.manage_device_button_logging_interval);
        logInterval.setTypeface(Typeface.createFromAsset(getParent().getAssets(), "HelveticaNeueLTStd-Cn.otf"));
        logInterval.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(@NonNull final View v) {
                initLoggingService();
                if (mHistoryService == null) {
                    Log.e(TAG, "initIntervalChooser.onClick -> device cannot find the logging service.");
                }
                checkDeviceState();
                final Button logInterval = (Button) getParent().findViewById(R.id.manage_device_button_logging_interval);
                final Integer numberElements = mHistoryService.getNumberLoggedElements();
                if (numberElements != null && numberElements > 0) {
                    showAdviceWhenModifyingInterval();
                } else {
                    showIntervalSelector(logInterval);
                }
            }

            /**
             * In case the user wants to modify the interval when the device has data, it sends an advice
             * to the user informing that the data will be deleted.
             */
            private void showAdviceWhenModifyingInterval() {
                final Button logInterval = (Button) getParent().findViewById(R.id.manage_device_button_logging_interval);
                final AlertDialog.Builder builder = new AlertDialog.Builder(getParent());
                builder.setTitle(getResources().getString(R.string.interval_modification));
                builder.setCancelable(false);
                builder.setMessage(getResources().getString(R.string.interval_modification_message));
                builder.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(@NonNull final DialogInterface dialog, final int which) {
                        if (mHistoryService.isLoggingStateEditable()) {
                            mHistoryService.resetDeviceData();
                        }
                        dialog.cancel();
                        showIntervalSelector(logInterval);
                    }
                });
                builder.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }

            private void showIntervalSelector(@NonNull final Button button) {
                final AlertDialog.Builder builder = new Builder(getParent());
                builder.setCancelable(false)
                        .setTitle(R.string.title_button_choice)
                        .setItems(R.array.array_interval_choices, new DialogInterface.OnClickListener() {

                            public void onClick(@NonNull DialogInterface dialog, int which) {
                                checkDeviceState();
                                final LoggerInterval l = LoggerInterval.fromNumberElement(which);
                                Log.i(TAG, String.format("showIntervalSelector.onClickListener -> User selected interval %s in seconds.", l.getValueInSeconds()));
                                mHistoryService.setDownloadInterval(l.getValueInMilliseconds());
                                button.setText(l.toStringLabel(getParent().getApplicationContext()));
                                PreferenceManager.getDefaultSharedPreferences(getParent().getApplicationContext()).edit().putInt(String.valueOf(button.getId()), which).commit();
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
            final Switch loggingSwitch = (Switch) getParent().findViewById(R.id.manage_device_switch_toggle_logger);
            loggingSwitch.setChecked(mHistoryService.isGadgetLoggingEnabled());
            if (mHistoryService.isDownloadInProgress()) {
                loggingSwitch.setEnabled(false);
            } else {
                loggingSwitch.setEnabled(true);
            }
            loggingSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                    if (isChecked) {
                        initLoggingAdviceAlert();
                        getParent().findViewById(R.id.manage_device_button_logging_interval).setEnabled(false);
                        getParent().findViewById((R.id.manage_device_button_download_log)).setEnabled(true);
                    } else {
                        mHistoryService.setLoggingState(false);
                        getParent().findViewById(R.id.manage_device_button_logging_interval).setEnabled(true);
                        checkDownloadPermission();
                    }
                }
            });
        } else {
            getParent().findViewById(R.id.manage_device_layout_gadgetlogging_switch).setVisibility(View.GONE);
        }
    }

    private void initLoggingAdviceAlert() {
        final Switch logging = (Switch) getParent().findViewById(R.id.manage_device_switch_toggle_logger);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getParent());

        builder.setTitle(getResources().getString(R.string.enable_logging));
        builder.setCancelable(false);
        builder.setMessage(getResources().getString(R.string.label_advice_logging_enable));
        builder.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(@NonNull DialogInterface dialog, int which) {
                dialog.cancel();
                mHistoryService.setLoggingState(true);
                getParent().findViewById(R.id.manage_device_button_logging_interval).setEnabled(false);
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(@NonNull DialogInterface dialog, int which) {
                dialog.cancel();
                logging.setChecked(false);
            }
        });
        builder.show();
    }

    private void initBatteryBar() {
        updateBatteryLevel();
        getParent().findViewById(R.id.manage_device_battery_seek_bar).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(@NonNull View view, MotionEvent motionEvent) {
                checkDeviceState();
                updateBatteryLevel();
                return view.performClick();
            }
        });
    }

    private void initDownloadButton() {
        final View downloadButton = getParent().findViewById(R.id.manage_device_button_download_log);
        downloadButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                checkDeviceState();
                getParent().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getParent().findViewById(R.id.manage_device_switch_toggle_logger).setEnabled(false);
                        getParent().findViewById(R.id.manage_device_button_logging_interval).setEnabled(false);
                        DownloadLogHelper.getInstance().downloadLoggedData(getParent(), mHistoryService, new Handler());
                    }
                });
            }
        });
        downloadButton.setEnabled(hasValuesToDownload());
    }

    private void updateBatteryLevel() {
        if (mSelectedDevice == null) {
            Log.e(TAG, "updateBatteryLevel -> Battery level is null, ");
        } else {
            final Integer batteryLevel = getBatteryLevel();
            if (batteryLevel == null) {
                getParent().findViewById(R.id.manage_device_battery_level_layout).setVisibility(View.GONE);
            } else {
                getParent().findViewById(R.id.manage_device_battery_level_layout).setVisibility(View.VISIBLE);
                Log.i(TAG, String.format("updateBatteryLevel -> Battery it's at %d%%", batteryLevel));
                ((TextView) getParent().findViewById(R.id.manage_device_battery_level_value)).setText(String.format("%d%%", batteryLevel));
                final SeekBar seekBar = (SeekBar) getParent().findViewById(R.id.manage_device_battery_seek_bar);
                seekBar.setProgress(batteryLevel);
                seekBar.setEnabled(false);
            }
        }
    }

    @Nullable
    private Integer getBatteryLevel() {
        final BatteryService service = mSelectedDevice.getDeviceService(BatteryService.class);
        if (service == null) {
            Log.e(TAG, "getBatteryLevel -> Battery Service was not found.");
            return null;
        }
        return service.getBatteryLevel();
    }

    private void checkLoggingIsEnabled() {
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
            Log.i(TAG, String.format("checkLoggingIsEnabled -> Logging in device %s is %s.", mSelectedDevice.getAddress(), (isLoggingEnabled) ? "enabled" : "disabled"));
            final Switch loggingToggle = ((Switch) getParent().findViewById(R.id.manage_device_switch_toggle_logger));
            loggingToggle.setChecked(isLoggingEnabled);
            loggingToggle.setEnabled(!mHistoryService.isDownloadInProgress());
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
        final Button logIntervalButton = (Button) getParent().findViewById(R.id.manage_device_button_logging_interval);
        logIntervalButton.setText(new TimeFormatter(intervalSeconds).getShortTime(getParent().getApplicationContext()));
        if (mHistoryService.isDownloadInProgress()) {
            Log.d(TAG, "checkInterval -> During a download interval can't be enabled.");
            logIntervalButton.setEnabled(false);
        } else if (mHistoryService.isLoggingStateEditable() && mHistoryService.isGadgetLoggingEnabled()) {
            Log.d(TAG, "checkInterval -> Device is logging data, interval can't be modified.");
            logIntervalButton.setEnabled(false);
        } else {
            logIntervalButton.setEnabled(true);
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
            Log.i(TAG, String.format("checkDeviceState -> Device with address %s is connected.", mSelectedDevice.getAddress()));
        } else {
            Log.e(TAG, String.format("checkDeviceState -> Device with address %s is not connected, closing activity.", mDeviceModel.getAddress()));
            closeScreen();
        }
    }

    private boolean initLoggingService() {
        try {
            if (mHistoryService == null) {
                mHistoryService = mSelectedDevice.getHistoryService();
                if (mHistoryService == null) {
                    Log.e(TAG, "initLoggingService -> Logging service was not found in the selected humigadget.");
                } else {
                    return true;
                }
            } else {
                Log.i(TAG, "initLoggingService -> Logging service initialized correctly.");
            }
        } catch (@NonNull final Exception e) {
            Log.e(TAG, "initLoggingService -> It was impossible to connect logging in the selected humigadget due the following error -> ", e);
        }
        return false;
    }

    /**
     * Checks if the user has permission to download. (Has values to log)
     */
    private void checkDownloadPermission() {
        Log.d(TAG, "checkDownloadPermission -> Checking download permission.");
        if (mHistoryService.isDownloadInProgress()) {
            getParent().findViewById(R.id.manage_device_button_download_log).setEnabled(false);
        } else {
            getParent().findViewById(R.id.manage_device_button_download_log).setEnabled(hasValuesToDownload());
        }
    }

    /**
     * Checks if they are values to log.
     *
     * @return <code>true</code> if there values - <code>false</code> otherwise.
     */
    private boolean hasValuesToDownload() {
        if (mHistoryService == null) {
            Log.w(TAG, "hasValuesToDownload -> Logging service it's not initialized yet.");
            return false;
        }
        if (mHistoryService.isDownloadInProgress()) {
            Log.w(TAG, "hasValuesToDownload -> User is downloading data from the device.");
            return false;
        }
        final Integer numberLoggedElements = mHistoryService.getNumberLoggedElements();
        if (numberLoggedElements == null) {
            Log.e(TAG, "hasValuesToDownload -> It was impossible to retrieve the number of elements.");
            return false;
        } else {
            Log.i(TAG, String.format("hasValuesToDownload -> The device %s has %d elements to download.", mSelectedDevice.getAddress(), numberLoggedElements));
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
        getParent().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mHistoryService.isLoggingStateEditable()) {
                    final Switch loggingToggle = ((Switch) getParent().findViewById(R.id.manage_device_switch_toggle_logger));
                    loggingToggle.setEnabled(true);
                }
                final Button logIntervalButton = (Button) getParent().findViewById(R.id.manage_device_button_logging_interval);
                logIntervalButton.setEnabled(true);
            }
        });
    }
}