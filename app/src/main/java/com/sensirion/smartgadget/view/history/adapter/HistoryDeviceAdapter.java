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
package com.sensirion.smartgadget.view.history.adapter;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sensirion.smartgadget.R;
import com.sensirion.smartgadget.utils.DeviceModel;
import com.sensirion.smartgadget.utils.view.ColorManager;

import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HistoryDeviceAdapter extends ArrayAdapter<DeviceModel> {

    private static final String TAG = HistoryDeviceAdapter.class.getSimpleName();

    @NonNull
    private final List<DeviceModel> mSelectedItems = new LinkedList<>();

    public HistoryDeviceAdapter(@NonNull final Context context) {
        super(context, R.layout.listitem_dashboard);
    }

    /**
     * Update the device adapter device list.
     *
     * @param handler           needed to update the GUI.
     * @param updatedDeviceList new gadgets with history data available to select.
     */
    public synchronized void update(@NonNull final Handler handler,
                                    @NonNull final List<DeviceModel> updatedDeviceList) {
        if (isListTheSame(updatedDeviceList)) {
            return;
        }
        Log.i(TAG, String.format("update -> New device list with %d devices.", updatedDeviceList.size()));
        removeUnavailableGadgetsFromSelectedList(updatedDeviceList);
        checkAreGadgetsSelected(updatedDeviceList);

        handler.post(new Runnable() {
            @Override
            public void run() {
                clear();
                addAll(updatedDeviceList);
            }
        });
    }

    private boolean isListTheSame(@NonNull final List<DeviceModel> updatedDeviceList) {
        if (updatedDeviceList.size() != getCount()) {
            return false;
        }
        for (int i = 0; i < getCount(); i++) {
            if (updatedDeviceList.contains(getItem(i))) {
                continue;
            }
            return false;
        }
        return true;
    }

    private void removeUnavailableGadgetsFromSelectedList(@NonNull final List<DeviceModel> updatedDeviceList) {
        final List<DeviceModel> newSelectedList = new LinkedList<>();
        for (final DeviceModel model : mSelectedItems) {
            if (updatedDeviceList.contains(model)) {
                newSelectedList.add(model);
            }
        }
        synchronized (mSelectedItems) {
            mSelectedItems.clear();
            mSelectedItems.addAll(newSelectedList);
        }
    }

    private void checkAreGadgetsSelected(@NonNull final List<DeviceModel> updatedDeviceList) {
        synchronized (mSelectedItems) {
            if (mSelectedItems.isEmpty()) {
                if (updatedDeviceList.size() > 0) {
                    mSelectedItems.add(updatedDeviceList.get(0));
                }
            }
        }
    }

    @Override
    public View getView(final int position,
                        @Nullable final View convertView,
                        @NonNull final ViewGroup parent) {

        final View view;
        if (convertView == null) {
            view = View.inflate(parent.getContext(), R.layout.listitem_dashboard, null);
        } else {
            view = convertView;
        }

        DeviceViewHolder holder = (DeviceViewHolder) view.getTag();

        if (holder == null) {
            holder = new DeviceViewHolder(view);
            view.setTag(holder);
        }

        final DeviceModel item = getItem(position);

        holder.title.setText(item.getUserDeviceName());

        final int color = ColorManager.getInstance().getDeviceColor(item.getAddress());
        holder.color.setBackgroundColor(color);

        if (mSelectedItems.contains(item)) {
            holder.icon.setImageResource(R.drawable.ic_action_accept);
        } else {
            holder.icon.setImageResource(0);
        }

        return view;
    }

    /**
     * Marks an 'unselected' element or removes a mark of a 'selected' element.
     *
     * @param position of the selected item.
     */
    public void itemSelected(final int position) {
        final DeviceModel model = getItem(position);
        synchronized (mSelectedItems) {
            if (mSelectedItems.contains(model)) {
                mSelectedItems.remove(model);
            } else {
                mSelectedItems.add(model);
            }
        }
    }

    /**
     * Obtains the list of marked items.
     *
     * @return {@link java.util.List} with the marked items.
     */
    @NonNull
    public List<String> getListOfSelectedItems() {
        final LinkedList<String> listOfItems = new LinkedList<>();
        for (final DeviceModel model : mSelectedItems) {
            listOfItems.add(model.getAddress());
        }
        return listOfItems;
    }

    static class DeviceViewHolder {

        @BindView(R.id.item_gadget_displayname)
        TextView title;

        @BindView(R.id.item_icon)
        ImageView icon;

        @BindView(R.id.item_gadget_color)
        ImageView color;

        public DeviceViewHolder(@NonNull final View view) {
            ButterKnife.bind(this, view);
        }
    }
}
