package com.sensirion.smartgadget.view.glossary;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.sensirion.smartgadget.R;
import com.sensirion.smartgadget.utils.view.ParentListFragment;
import com.sensirion.smartgadget.view.MainActivity;
import com.sensirion.smartgadget.view.glossary.adapter.GlossaryAdapter;


public class GlossaryFragment extends ParentListFragment {

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.preferences_list, container, false);

        //Creates a new adapter
        final GlossaryAdapter adapter = new GlossaryAdapter(getActivity().getLayoutInflater(), getActivity());

        //Adds glossary elements to the adapter.
        addTemperature(adapter);
        addHumidity(adapter);
        addDewPoint(adapter);
        addHeatIndex(adapter);
        addSource(adapter);

        //Sets the list adapter.
        setListAdapter(adapter);

        return rootView;
    }

    private void addTemperature(@NonNull final GlossaryAdapter adapter) {
        final String title = getString(R.string.glossary_temperature_title);
        final String description = getString(R.string.glossary_temperature_description);
        final Drawable icon = getResources().getDrawable(R.drawable.temperature_icon);
        adapter.addDevice(title, description, icon);
    }

    private void addHumidity(@NonNull final GlossaryAdapter adapter) {
        final String title = getString(R.string.glossary_humidity_title);
        final String description = getString(R.string.glossary_humidity_description);
        final Drawable icon = getResources().getDrawable(R.drawable.humidity_icon);
        adapter.addDevice(title, description, icon);
    }

    private void addDewPoint(@NonNull final GlossaryAdapter adapter) {
        final String title = getString(R.string.glossary_dewpoint_title);
        final String description = getString(R.string.glossary_dewpoint_description);
        final Drawable icon = getResources().getDrawable(R.drawable.dew_point_icon);
        adapter.addDevice(title, description, icon);
    }

    private void addHeatIndex(@NonNull final GlossaryAdapter adapter) {
        final String title = getString(R.string.glossary_heatindex_title);
        final String description = getString(R.string.glossary_heatindex_description);
        final Drawable icon = getResources().getDrawable(R.drawable.heat_index_icon);
        adapter.addDevice(title, description, icon);
    }

    private void addSource(@NonNull final GlossaryAdapter adapter) {
        final String title = getResources().getString(R.string.glossary_source);
        final Drawable icon = getResources().getDrawable(R.drawable.empty_icon);
        adapter.addSource(title, icon);
    }

    @Override
    public void onListItemClick(final ListView l, final View v, final int position, final long id) {
        if (getParent().getResources().getBoolean(R.bool.is_tablet)) {
            ((MainActivity) getParent()).toggleTabletMenu();
        }
    }
}