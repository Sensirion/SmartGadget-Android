package com.sensirion.smartgadget.view.device_management.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

/**
 * Represents a discovered or connected peripheral in a ListView
 */
public class HumigadgetListItemAdapter extends BaseAdapter {

    private final List<BleDevice> mBleDevices = Collections.synchronizedList(new ArrayList<BleDevice>());

    private final Typeface mTypefaceNormal;
    private final Typeface mTypefaceBold;

    private final Comparator<BleDevice> mRssiComparator = new Comparator<BleDevice>() {
        public int compare(@NonNull final BleDevice device1, @NonNull final BleDevice device2) {
            return device2.getRSSI() - device1.getRSSI();
        }
    };

    public HumigadgetListItemAdapter(@NonNull final Context context) {
        mTypefaceNormal = Typeface.createFromAsset(context.getAssets(), "HelveticaNeueLTStd-Cn.otf");
        mTypefaceBold = Typeface.createFromAsset(context.getAssets(), "HelveticaNeueLTStd-Bd.otf");
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

    @Nullable
    @Override
    public View getView(final int position, @Nullable final View convertView, @NonNull final ViewGroup parent) {

        final View view;
        if (convertView == null) {
            view = View.inflate(parent.getContext(), R.layout.listitem_scan_result, null);
        } else {
            view = convertView;
        }

        final BleDevice bleDevice = mBleDevices.get(position);

        final TextView advertisedName = (TextView) view.findViewById(R.id.listitem_advertised_name);
        advertisedName.setTypeface(mTypefaceNormal);
        advertisedName.setText(bleDevice.getAdvertisedName());

        final TextView deviceNameView = (TextView) view.findViewById(R.id.device_address);
        final String deviceName = DeviceNameDatabaseManager.getInstance().readDeviceName(bleDevice.getAddress());
        deviceNameView.setText(deviceName);
        deviceNameView.setTypeface(mTypefaceBold);

        final TextView rssiLabelTextView = (TextView) view.findViewById(R.id.listitem_label_rssi);
        rssiLabelTextView.setTypeface(mTypefaceNormal);

        final TextView rssiTextView = (TextView) view.findViewById(R.id.listitem_value_rssi);
        rssiTextView.setText(Integer.toString(bleDevice.getRSSI()));
        rssiTextView.setTypeface(mTypefaceNormal);

        return view;
    }

    public synchronized void clear() {
        mBleDevices.clear();
        notifyDataSetChanged();
    }

    public synchronized void addAll(@NonNull final Iterable<? extends BleDevice> devices) {
        for (final BleDevice device : devices) {
            if (mBleDevices.contains(device)) {
                continue;
            }
            mBleDevices.add(device);
        }
        Collections.sort(mBleDevices, mRssiComparator);
        notifyDataSetChanged();
    }
}