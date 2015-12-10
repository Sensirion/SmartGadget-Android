package com.sensirion.smartgadget.view.dashboard.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sensirion.smartgadget.R;
import com.sensirion.smartgadget.utils.DeviceModel;
import com.sensirion.smartgadget.utils.Settings;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ConnectedDeviceAdapter extends ArrayAdapter<DeviceModel> {

    public ConnectedDeviceAdapter(@NonNull final Context context) {
        super(context, R.layout.listitem_dashboard);
    }

    @UiThread
    public void update(@NonNull final List<DeviceModel> connectedDevices) {
        clear();
        addAll(connectedDevices);
    }

    @Override
    public View getView(final int position,
                        @Nullable View view,
                        @NonNull final ViewGroup parent) {

        final DashboardViewHolder holder;
        if (view != null) {
            holder = (DashboardViewHolder) view.getTag();
        } else {
            view = View.inflate(parent.getContext(), R.layout.listitem_dashboard, null);
            holder = new DashboardViewHolder(view);
            view.setTag(holder);
        }

        final DeviceModel item = getItem(position);
        holder.titleView.setText(item.getUserDeviceName());
        holder.colorView.setBackgroundColor(item.getColor());

        if (item.getAddress().equals(Settings.getInstance().getSelectedAddress())) {
            holder.itemIcon.setImageResource(R.drawable.ic_action_accept);
        } else {
            holder.itemIcon.setImageResource(0);
        }

        return view;
    }

    static class DashboardViewHolder {
        @Bind(R.id.item_gadget_displayname)
        TextView titleView;
        @Bind(R.id.item_gadget_color)
        ImageView colorView;
        @Bind(R.id.item_icon)
        ImageView itemIcon;

        DashboardViewHolder(@NonNull final View view) {
            ButterKnife.bind(this, view);
        }
    }
}