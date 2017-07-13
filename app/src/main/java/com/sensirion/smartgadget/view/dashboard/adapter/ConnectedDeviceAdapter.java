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

import butterknife.BindView;
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
        @BindView(R.id.item_gadget_displayname)
        TextView titleView;
        @BindView(R.id.item_gadget_color)
        ImageView colorView;
        @BindView(R.id.item_icon)
        ImageView itemIcon;

        DashboardViewHolder(@NonNull final View view) {
            ButterKnife.bind(this, view);
        }
    }
}
