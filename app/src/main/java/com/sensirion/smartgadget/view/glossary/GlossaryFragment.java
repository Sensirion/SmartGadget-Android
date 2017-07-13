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
