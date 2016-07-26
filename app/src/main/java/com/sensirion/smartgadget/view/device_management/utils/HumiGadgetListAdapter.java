package com.sensirion.smartgadget.view.device_management.utils;

import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sensirion.smartgadget.R;
import com.sensirion.smartgadget.peripheral.rht_sensor.external.GadgetModel;
import com.sensirion.smartgadget.persistence.device_name_database.DeviceNameDatabaseManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Represents discovered or connected peripherals in a ListView
 */
public class HumiGadgetListAdapter extends BaseAdapter {

    // List of the adapter {@link BleDevice}
    @NonNull
    private final List<GadgetModel> mGadgets = Collections.synchronizedList(new ArrayList<GadgetModel>());

    @NonNull
    private final Comparator<GadgetModel> mRssiComparator = new Comparator<GadgetModel>() {
        public int compare(@NonNull final GadgetModel device1, @NonNull final GadgetModel device2) {
            return device2.getRssi() - device1.getRssi();
        }
    };

    @NonNull
    private final Typeface mTypefaceNormal;
    @NonNull
    private final Typeface mTypefaceBold;

    public HumiGadgetListAdapter(@NonNull final Typeface typefaceNormal,
                                 @NonNull final Typeface typefaceBold) {
        mTypefaceNormal = typefaceNormal;
        mTypefaceBold = typefaceBold;
    }

    @Override
    public int getCount() {
        return mGadgets.size();
    }

    @Override
    public GadgetModel getItem(final int position) {
        return mGadgets.get(position);
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

        HumiGadgetViewHolder holder = (HumiGadgetViewHolder) view.getTag();

        if (holder == null) {
            holder = new HumiGadgetViewHolder(view);
            view.setTag(holder);
        }
        final GadgetModel gadget = mGadgets.get(position);

        holder.advertisedName.setTypeface(mTypefaceNormal);
        holder.advertisedName.setText(gadget.getName());

        final String deviceName = DeviceNameDatabaseManager.getInstance().readDeviceName(gadget.getAddress());
        holder.deviceAddress.setText(deviceName);
        holder.deviceAddress.setTypeface(mTypefaceBold);
        if (gadget.getRssi() >= 0) {
            holder.rssiLabelTextView.setVisibility(View.GONE);
        } else {
            holder.rssiLabelTextView.setVisibility(View.VISIBLE);
            holder.rssiLabelTextView.setTypeface(mTypefaceNormal);
            holder.rssiValueTextView.setText(String.format("%d dBm", gadget.getRssi()));
            holder.rssiValueTextView.setTypeface(mTypefaceNormal);
        }
        holder.settingsIcon.setVisibility((gadget.isConnected()) ? View.VISIBLE : View.INVISIBLE);

        return view;
    }

    /**
     * Removes all elements from the list of {@link com.sensirion.libsmartgadget.Gadget}
     * and asks the GUI to update it to the new state.
     */
    public void clear() {
        mGadgets.clear();
        notifyDataSetChanged();
    }


    public void add(final GadgetModel gadget) {
        synchronized (mGadgets) {
            for (final GadgetModel knownGadget : mGadgets) {
                if (knownGadget.equals(gadget)) {
                    knownGadget.setRssi(gadget.getRssi());
                    return;
                }
            }
            mGadgets.add(gadget);
        }
        notifyDataSetChanged();
    }

    public void addAll(Set<GadgetModel> gadgets) {
        synchronized (mGadgets) {
            for (GadgetModel gadget : gadgets) {
                add(gadget);
            }
        }
        notifyDataSetChanged();
    }

    public void remove(GadgetModel gadget) {
        mGadgets.remove(gadget);
        notifyDataSetChanged();
    }

    public void sortForRssi() {
        Collections.sort(mGadgets, mRssiComparator);
    }

    static class HumiGadgetViewHolder {
        @BindView(R.id.listitem_advertised_name)
        TextView advertisedName;
        @BindView(R.id.device_address)
        TextView deviceAddress;
        @BindView(R.id.listitem_label_rssi)
        TextView rssiLabelTextView;
        @BindView(R.id.listitem_value_rssi)
        TextView rssiValueTextView;
        @BindView(R.id.listitem_icon)
        ImageView settingsIcon;

        public HumiGadgetViewHolder(@NonNull final View view) {
            ButterKnife.bind(this, view);
        }
    }
}
