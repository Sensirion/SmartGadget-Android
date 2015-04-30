package com.sensirion.smartgadget.view.dashboard.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sensirion.smartgadget.R;
import com.sensirion.smartgadget.utils.DeviceModel;
import com.sensirion.smartgadget.utils.Settings;

import java.util.List;

public class ConnectedDeviceAdapter extends ArrayAdapter<DeviceModel> {

    public ConnectedDeviceAdapter(@NonNull final Context context) {
        super(context, R.layout.listitem_dashboard);
    }

    public void update(final List<DeviceModel> connectedDevices) {
        clear();
        addAll(connectedDevices);
    }

    @Override
    public View getView(final int position, final View convertView, @NonNull final ViewGroup parent) {
        final View rowView = View.inflate(parent.getContext(), R.layout.listitem_dashboard, null);
        final DeviceModel item = getItem(position);

        final TextView titleView = (TextView) rowView.findViewById(R.id.item_gadget_displayname);
        titleView.setText(item.getUserDeviceName());

        final ImageView colorView = (ImageView) rowView.findViewById(R.id.item_gadget_color);
        colorView.setBackgroundColor(item.getColor());

        final ImageView iconView = (ImageView) rowView.findViewById(R.id.item_icon);

        if (item.getAddress().equals(Settings.getInstance().getSelectedAddress())) {
            iconView.setImageResource(R.drawable.ic_action_accept);
        } else {
            iconView.setImageResource(0);
        }

        return rowView;
    }
}