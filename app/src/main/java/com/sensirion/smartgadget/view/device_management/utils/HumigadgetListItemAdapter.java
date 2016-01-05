package com.sensirion.smartgadget.view.device_management.utils;

import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sensirion.libble.devices.BleDevice;
import com.sensirion.smartgadget.R;
import com.sensirion.smartgadget.persistence.device_name_database.DeviceNameDatabaseManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Represents a discovered or connected peripheral in a ListView
 */
public class HumigadgetListItemAdapter extends BaseAdapter {

    // List of the adapter {@link BleDevice}
    @NonNull
    private final List<BleDevice> mBleDevices = new ArrayList<>();

    @NonNull
    private final Comparator<BleDevice> mRssiComparator = new Comparator<BleDevice>() {
        public int compare(@NonNull final BleDevice device1, @NonNull final BleDevice device2) {
            return device2.getRSSI() - device1.getRSSI();
        }
    };

    @NonNull
    private final Typeface mTypefaceNormal;
    @NonNull
    private final Typeface mTypefaceBold;

    public HumigadgetListItemAdapter(@NonNull final Typeface typefaceNormal,
                                     @NonNull final Typeface typefaceBold) {
        mTypefaceNormal = typefaceNormal;
        mTypefaceBold = typefaceBold;
    }

    @Override
    public int getCount() {
        return mBleDevices.size();
    }

    @Override
    public Object getItem(final int position) {
        return mBleDevices.get(position);
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(final int position,
                        @Nullable View view,
                        @NonNull final ViewGroup parent) {
        if (view == null) {
            view = View.inflate(parent.getContext(), R.layout.listitem_scan_result, null);
        }

        HumigadgetViewHolder holder = (HumigadgetViewHolder) view.getTag();

        if (holder == null) {
            holder = new HumigadgetViewHolder(view);
            view.setTag(holder);
        }
        final BleDevice bleDevice = mBleDevices.get(position);

        holder.advertisedName.setTypeface(mTypefaceNormal);
        holder.advertisedName.setText(bleDevice.getAdvertisedName());

        final String deviceName = DeviceNameDatabaseManager.getInstance().readDeviceName(bleDevice.getAddress());
        holder.deviceAddress.setText(deviceName);
        holder.deviceAddress.setTypeface(mTypefaceBold);

        holder.rssiLabelTextView.setTypeface(mTypefaceNormal);

        holder.rssiValueTextView.setText(String.format("%d", bleDevice.getRSSI()));
        holder.rssiValueTextView.setTypeface(mTypefaceNormal);
        return view;
    }

    /**
     * Removes all elements from the list of {@link BleDevice}
     * and asks the GUI to update it to the new state.
     */
    @UiThread
    public void clear() {
        synchronized (mBleDevices) {
            mBleDevices.clear();
        }
        notifyDataSetChanged();
    }

    @UiThread
    public void addAll(@NonNull final Iterable<? extends BleDevice> devices) {
        synchronized (mBleDevices) {
            for (final BleDevice device : devices) {
                if (mBleDevices.contains(device)) {
                    continue;
                }
                mBleDevices.add(device);
            }
            Collections.sort(mBleDevices, mRssiComparator);
        }
        notifyDataSetChanged();
    }

    static class HumigadgetViewHolder {
        @Bind(R.id.listitem_advertised_name)
        TextView advertisedName;
        @Bind(R.id.device_address)
        TextView deviceAddress;
        @Bind(R.id.listitem_label_rssi)
        TextView rssiLabelTextView;
        @Bind(R.id.listitem_value_rssi)
        TextView rssiValueTextView;

        public HumigadgetViewHolder(@NonNull final View view) {
            ButterKnife.bind(this, view);
        }
    }
}