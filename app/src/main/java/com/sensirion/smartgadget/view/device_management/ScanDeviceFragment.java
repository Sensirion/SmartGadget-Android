/*
 * Copyright (c) 2017, Sensirion AG
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of Sensirion AG nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.sensirion.smartgadget.view.device_management;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.sensirion.smartgadget.R;
import com.sensirion.smartgadget.peripheral.rht_sensor.external.GadgetModel;
import com.sensirion.smartgadget.peripheral.rht_sensor.external.HumiGadgetConnectionStateListener;
import com.sensirion.smartgadget.peripheral.rht_sensor.external.RHTHumigadgetSensorManager;
import com.sensirion.smartgadget.persistence.device_name_database.DeviceNameDatabaseManager;
import com.sensirion.smartgadget.utils.PhoneSettingsSetup;
import com.sensirion.smartgadget.utils.view.ParentListFragment;
import com.sensirion.smartgadget.utils.view.SectionAdapter;
import com.sensirion.smartgadget.view.MainActivity;
import com.sensirion.smartgadget.view.device_management.utils.HumiGadgetListAdapter;

import java.lang.reflect.Method;
import java.util.Set;

import butterknife.BindBool;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Holds all the UI elements needed for showing the devices in range in a list
 */
public class ScanDeviceFragment extends ParentListFragment implements HumiGadgetConnectionStateListener {
    private static final String TAG = ScanDeviceFragment.class.getSimpleName();

    private static final int SCAN_DISCOVERY_TIME_MS = 10000;
    private static final int CONNECTING_DIALOG_DISMISS_TIME_MS = 2000;

    // Resources extracted from the resources folder
    @BindString(R.string.label_connected)
    String CONNECTED_STRING;
    @BindString(R.string.label_discovered)
    String DISCOVERED_STRING;
    @BindString(R.string.typeface_condensed)
    String TYPEFACE_CONDENSED_LOCATION;
    @BindString(R.string.typeface_bold)
    String TYPEFACE_BOLD_LOCATION;
    @BindString(R.string.connecting_title)
    String PLEASE_WAIT_STRING;
    @BindString(R.string.connecting_to_device_prefix)
    String CONNECTING_TO_DEVICE_PREFIX;
    @BindBool(R.bool.is_tablet)
    boolean IS_TABLET;

    // Injected views
    @BindView(R.id.scan_background)
    FrameLayout mBackground;
    @BindView(R.id.togglebutton_scan)
    ToggleButton mScanToggleButton;

    @BindView(R.id.scanning_info_container)
    ScrollView mScanInfoContainer;

    // Block Dialogs
    @Nullable
    private ProgressDialog mBlockingProgressDialog; // TODO remove and use a normal dialog instead

    // Section Managers
    private SectionAdapter mSectionAdapter;
    private HumiGadgetListAdapter mConnectedDevicesAdapter;
    private HumiGadgetListAdapter mDiscoveredDevicesAdapter;

    // Fragment state attributes
    private Menu mOptionsMenu;
    private RHTHumigadgetSensorManager mHumiGadgetSensorManager;

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_device_scan, container, false);

        unbinder = ButterKnife.bind(this, rootView);

        final AssetManager assets = getContext().getAssets();
        final Typeface typefaceNormal = Typeface.createFromAsset(assets, TYPEFACE_CONDENSED_LOCATION);
        final Typeface typefaceBold = Typeface.createFromAsset(assets, TYPEFACE_BOLD_LOCATION);

        initListAdapter(typefaceNormal, typefaceBold);
        initToggleButton(typefaceBold);

        setHasOptionsMenu(true);

        initBackgroundListenerTablet();
        return rootView;
    }

    private void initBackgroundListenerTablet() {
        if (IS_TABLET) {
            mBackground.setOnTouchListener(
                    new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(@NonNull final View view,
                                               @NonNull final MotionEvent motionEvent) {
                            final MainActivity parent = (MainActivity) getParent();
                            if (parent != null && isVisible()) {
                                parent.toggleTabletMenu();
                                return true;
                            }
                            return false;
                        }
                    }
            );
        }
    }

    private void initListAdapter(@NonNull final Typeface typefaceNormal,
                                 @NonNull final Typeface typefaceBold) {
        mSectionAdapter = new SectionAdapter() {
            @Nullable
            @Override
            protected View getHeaderView(final String caption,
                                         final int itemIndex,
                                         @Nullable final View convertView,
                                         final ViewGroup parent) {
                TextView headerTextView = (TextView) convertView;
                if (convertView == null) {
                    headerTextView = (TextView) View.inflate(getParent(), R.layout.listitem_scan_header, null);
                    headerTextView.setTypeface(typefaceBold);
                }
                headerTextView.setText(caption);
                return headerTextView;
            }
        };

        mConnectedDevicesAdapter = new HumiGadgetListAdapter(typefaceNormal, typefaceBold);
        mDiscoveredDevicesAdapter = new HumiGadgetListAdapter(typefaceNormal, typefaceBold);

        mSectionAdapter.addSectionToAdapter(CONNECTED_STRING, mConnectedDevicesAdapter);
        mSectionAdapter.addSectionToAdapter(DISCOVERED_STRING, mDiscoveredDevicesAdapter);

        setListAdapter(mSectionAdapter);

        mHumiGadgetSensorManager = RHTHumigadgetSensorManager.getInstance();
    }

    private void initToggleButton(@NonNull final Typeface typefaceBold) {

        mScanToggleButton.setTypeface(typefaceBold);

        mScanToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startDeviceDiscovery();
                } else {
                    stopDeviceDiscovery();
                }
            }
        });
        // start scanning immediately when fragment is active
        mScanToggleButton.performClick();
    }

    /*
     * Device Discovery and related UI state handling
     */
    private void startDeviceDiscovery() {
        mHumiGadgetSensorManager.startDiscovery(SCAN_DISCOVERY_TIME_MS);
        updateScanButtonsState(true);
    }

    private void stopDeviceDiscovery() {
        mHumiGadgetSensorManager.stopDiscovery();
        updateScanButtonsState(false);
    }

    private void updateScanButtonsState(final boolean scanning) {
        mScanToggleButton.setBackgroundResource(R.drawable.toggle_button_scan);
        mScanToggleButton.setChecked(scanning);
    }

    /*
     * Implementation of {@link RHTHumigadgetDiscoveryListener}
     */

    @Override
    public void onGadgetDiscovered(@NonNull final GadgetModel gadget) {
        mDiscoveredDevicesAdapter.add(gadget);
        mDiscoveredDevicesAdapter.sortForRssi();
        mSectionAdapter.notifyDataSetChanged();

        handleScanningInfoVisibility(false);
    }

    private boolean unbindGadgetIfBoundByDevice(final GadgetModel gadget) {
        final BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
        final Set<BluetoothDevice> bondedDevices = defaultAdapter.getBondedDevices();
        for (final BluetoothDevice device : bondedDevices) {
            if (gadget.getAddress().equals(device.getAddress())) {
                Log.d(TAG, device.getAddress() + " bound in device settings... will unbind");
                try {
                    Method m = device.getClass()
                            .getMethod("removeBond", (Class[]) null);
                    m.invoke(device, (Object[]) null);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to unbind from gadget");
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onGadgetDiscoveryFailed() {
        updateScanButtonsState(false);
        mScanToggleButton.setBackgroundResource(R.color.red);
    }

    @Override
    public void onGadgetDiscoveryFinished() {
        updateScanButtonsState(false);
    }


    @Override
    public void onConnectionStateChanged(@NonNull GadgetModel gadget, boolean isConnected) {
        mDiscoveredDevicesAdapter.remove(gadget);
        if (isConnected) {
            mConnectedDevicesAdapter.add(gadget);
        } else {
            mConnectedDevicesAdapter.remove(gadget);
        }
        mSectionAdapter.notifyDataSetChanged();

        handleScanningInfoVisibility(false);
    }

    private void handleScanningInfoVisibility(final boolean forceToggle) {
        if (forceToggle) {
            mScanInfoContainer.setVisibility(isScanInfoContainerVisible() ? View.GONE : View.VISIBLE);
            return;
        }

        if (mDiscoveredDevicesAdapter.isEmpty() && !isScanInfoContainerVisible()) {
            mScanInfoContainer.setVisibility(View.VISIBLE);
        } else if (!mDiscoveredDevicesAdapter.isEmpty() && isScanInfoContainerVisible()) {
            mScanInfoContainer.setVisibility(View.GONE);
        }
    }

    private boolean isScanInfoContainerVisible() {
        return mScanInfoContainer.getVisibility() == View.VISIBLE;
    }

    @Override
    public void onListItemClick(final ListView l, final View v, final int position, final long id) {
        super.onListItemClick(l, v, position, id);
        stopDeviceDiscovery();

        final Object item = mSectionAdapter.getItem(position);
        if (GadgetModel.class.isInstance(item)) {
            onDeviceClick((GadgetModel) item);
        } else {
            Log.e(TAG, "FIXME: The selected device is not a GadgetModel.");
            Thread.dumpStack();
        }
    }

    private void onDeviceClick(@NonNull final GadgetModel gadget) {
        if (gadget.isConnected()) {
            openManageDeviceFragment(gadget);
        } else {
            if (!unbindGadgetIfBoundByDevice(gadget)) {
                final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
                dialogBuilder.setTitle(R.string.dialog_unbind_gadget_title);
                dialogBuilder.setMessage(R.string.dialog_unbind_gadget_message);
                dialogBuilder.create().show();
                return;
            }

            mDiscoveredDevicesAdapter.remove(gadget);
            mSectionAdapter.notifyDataSetChanged();
            handleScanningInfoVisibility(false);

            showConnectionInProgressDialog(gadget.getAddress());
            RHTHumigadgetSensorManager.getInstance().connectToGadget(gadget.getAddress());
        }
    }

    private void openManageDeviceFragment(@NonNull final GadgetModel device) {
        final MainActivity mainActivity = (MainActivity) getParent();
        if (mainActivity == null) {
            Log.e(TAG, "openManageDeviceFragment -> Cannot obtain main activity");
            return;
        }

        final ManageDeviceFragment fragment = new ManageDeviceFragment();
        fragment.init(device.getAddress());
        mainActivity.changeFragment(fragment);
    }

    @Override
    public void onResume() {
        super.onResume();
        PhoneSettingsSetup.lazyRequestPhoneSetup(getParent());

        mConnectedDevicesAdapter.clear();
        mConnectedDevicesAdapter.addAll(mHumiGadgetSensorManager.getConnectedDevices());
        mSectionAdapter.notifyDataSetChanged();
        handleScanningInfoVisibility(false);
        mHumiGadgetSensorManager.setConnectionStateListener(this);
        startDeviceDiscovery();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopDeviceDiscovery();
        mHumiGadgetSensorManager.setConnectionStateListener(null);
    }

    /**
     * Method called when trying to connect to a discovered device. Blocks the window until a device
     * was received or a timeout was produced.
     *
     * @param deviceAddress of the device.
     */
    private void showConnectionInProgressDialog(@NonNull final String deviceAddress) {
        final Context parent = getParent();
        if (parent == null) {
            Log.e(TAG, "showConnectionInProgressDialog -> Received a null parent.");
            return;
        }

        if (mBlockingProgressDialog != null) {
            Log.w(TAG, "Connecting ProgressDialog already in use... will not create a new one");
            return;
        }

        final String deviceName = DeviceNameDatabaseManager.getInstance().readDeviceName(deviceAddress);

        mBlockingProgressDialog = new ProgressDialog(parent);
        mBlockingProgressDialog.setTitle(PLEASE_WAIT_STRING);
        mBlockingProgressDialog.setMessage(String.format(CONNECTING_TO_DEVICE_PREFIX, deviceName));
        mBlockingProgressDialog.setCancelable(false);
        mBlockingProgressDialog.show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mBlockingProgressDialog != null) {
                    mBlockingProgressDialog.dismiss();
                    mBlockingProgressDialog = null;
                }
            }
        }, CONNECTING_DIALOG_DISMISS_TIME_MS);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull final Menu menu, @NonNull final MenuInflater inflater) {
        menu.clear();
        mOptionsMenu = menu;
        inflater.inflate(R.menu.info_action_bar, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.scan_device_info:
                if (isScanInfoContainerVisible()) {
                    handleScanningInfoVisibility(true);
                    startDeviceDiscovery();
                } else {
                    stopDeviceDiscovery();
                    mDiscoveredDevicesAdapter.clear();
                    mSectionAdapter.notifyDataSetChanged();
                    handleScanningInfoVisibility(true);
                }

                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
