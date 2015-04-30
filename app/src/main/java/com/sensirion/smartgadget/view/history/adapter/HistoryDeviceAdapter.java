package com.sensirion.smartgadget.view.history.adapter;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sensirion.smartgadget.R;
import com.sensirion.smartgadget.utils.DeviceModel;
import com.sensirion.smartgadget.utils.view.ColorManager;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class HistoryDeviceAdapter extends ArrayAdapter<DeviceModel> {

    private static final String TAG = HistoryDeviceAdapter.class.getSimpleName();

    @NonNull
    private List<DeviceModel> mSelectedItems = Collections.synchronizedList(new LinkedList<DeviceModel>());

    public HistoryDeviceAdapter(@NonNull final Context context) {
        super(context, R.layout.listitem_dashboard);
    }

    /**
     * Update the device adapter device list.
     *
     * @param handler           needed to update the GUI.
     * @param updatedDeviceList new gadgets with history data available to select.
     */
    public synchronized void update(@NonNull final Handler handler, @NonNull final List<DeviceModel> updatedDeviceList) {
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
        final List<DeviceModel> newSelectedList = Collections.synchronizedList(new LinkedList<DeviceModel>());
        for (final DeviceModel model : mSelectedItems) {
            if (updatedDeviceList.contains(model)) {
                newSelectedList.add(model);
            }
        }
        mSelectedItems = newSelectedList;
    }

    private void checkAreGadgetsSelected(@NonNull final List<DeviceModel> updatedDeviceList) {
        if (mSelectedItems.isEmpty()) {
            if (updatedDeviceList.size() > 0) {
                mSelectedItems.add(updatedDeviceList.get(0));
            }
        }
    }

    @Override
    public View getView(final int position, final View convertView, @NonNull final ViewGroup parent) {
        final View rowView = View.inflate(parent.getContext(), R.layout.listitem_dashboard, null);
        final DeviceModel item = getItem(position);

        final TextView titleView = (TextView) rowView.findViewById(R.id.item_gadget_displayname);
        titleView.setText(item.getUserDeviceName());

        setDeviceColor(rowView, item);

        final ImageView iconView = (ImageView) rowView.findViewById(R.id.item_icon);

        if (mSelectedItems.contains(item)) {
            iconView.setImageResource(R.drawable.ic_action_accept);
        } else {
            iconView.setImageResource(0);
        }

        return rowView;
    }

    private void setDeviceColor(@NonNull final View rowView, @NonNull final DeviceModel item) {
        final ImageView colorView = (ImageView) rowView.findViewById(R.id.item_gadget_color);
        final int color = ColorManager.getInstance().getDeviceColor(item.getAddress());
        colorView.setBackgroundColor(color);
    }

    /**
     * @param position of the selected item.
     */
    public void itemSelected(final int position) {
        final DeviceModel model = getItem(position);
        if (mSelectedItems.contains(model)) {
            if (mSelectedItems.size() > 1) {
                mSelectedItems.remove(model);
            }
        } else {
            mSelectedItems.add(model);
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
}