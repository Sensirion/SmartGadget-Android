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
package com.sensirion.smartgadget.utils.section_manager;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.sensirion.smartgadget.R;
import com.sensirion.smartgadget.view.comfort_zone.ComfortZoneFragment;
import com.sensirion.smartgadget.view.dashboard.DashboardFragment;
import com.sensirion.smartgadget.view.device_management.ScanDeviceFragment;
import com.sensirion.smartgadget.view.history.HistoryFragment;
import com.sensirion.smartgadget.view.preference.SmartgadgetPreferenceFragment;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class SectionManagerTablet extends SectionManager {

    private static final String TAG = SectionManagerTablet.class.getSimpleName();
    private static final int POSITION_DASHBOARD = 0;
    private static final int POSITION_COMFORT_ZONE = 1;
    private static final int POSITION_HISTORY = 2;
    //TODO: UNCOMMENT WHEN GLOSSARY BECOMES ENABLED AGAIN
    // private static final int POSITION_GLOSSARY = 3;
    private static final int POSITION_DEVICE_SCAN = 3;
    private static final int POSITION_SETTINGS = 4;
    private static final int NUMBER_PAGES = 5;

    public SectionManagerTablet(@NonNull final FragmentManager fm) {
        super(fm);
    }

    /**
     * Obtains the fragment ID of an incoming fragment.
     *
     * @param destinationFragment that will be used for obtaining the ID.
     * @return {@link java.lang.Integer} with the incoming fragment ID, if available.
     */
    @Nullable
    @AvailableSections
    public static Integer getFragmentId(@NonNull final Fragment destinationFragment) {
        final String fragmentName = ((Object) destinationFragment).getClass().getSimpleName();
        if (fragmentName.equals(DashboardFragment.class.getSimpleName())) {
            return POSITION_DASHBOARD;
        }
        if (fragmentName.equals(ComfortZoneFragment.class.getSimpleName())) {
            return POSITION_COMFORT_ZONE;
        }
        if (fragmentName.equals(HistoryFragment.class.getSimpleName())) {
            return POSITION_HISTORY;
        }
        //  if (fragmentName.equals(GlossaryFragment.class.getSimpleName())) {
        //    return POSITION_GLOSSARY;
        //}
        if (fragmentName.equals(ScanDeviceFragment.class.getSimpleName())) {
            return POSITION_DEVICE_SCAN;
        }
        if (fragmentName.equals(SmartgadgetPreferenceFragment.class.getSimpleName())) {
            return POSITION_SETTINGS;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPageId(@AvailableSections final int position) {
        switch (position) {
            case POSITION_DASHBOARD:
                return R.string.title_page_dashboard;
            case POSITION_COMFORT_ZONE:
                return R.string.title_page_comfort_zone;
            case POSITION_HISTORY:
                return R.string.title_page_history;
            case POSITION_DEVICE_SCAN:
                return R.string.title_page_device_management;
            // TODO: UNCOMMENT WHEN GLOSSARY BECOMES ENABLED AGAIN
            //     case POSITION_GLOSSARY:
            //       return R.string.title_page_glossary;
            case POSITION_SETTINGS:
                return R.string.title_page_settings;
            default:
                throw new IllegalArgumentException(String.format("%s: getPageTitle -> Position %d it's not implemented.", TAG, position));
        }
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public Fragment getItem(@AvailableSections final int position) {
        switch (position) {
            case POSITION_DASHBOARD:
                return new DashboardFragment();
            case POSITION_COMFORT_ZONE:
                return new ComfortZoneFragment();
            case POSITION_HISTORY:
                return new HistoryFragment();
            // TODO: UNCOMMENT WHEN GLOSSARY BECOMES ENABLED AGAIN
            //   case POSITION_GLOSSARY:
            //     return new GlossaryFragment();
            case POSITION_DEVICE_SCAN:
                return new ScanDeviceFragment();
            case POSITION_SETTINGS:
                return new SmartgadgetPreferenceFragment();
            default:
                throw new IllegalArgumentException(String.format("%s: getItem -> Position %d it's not implemented", TAG, position));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCount() {
        return NUMBER_PAGES;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPosition(@NonNull final Context context, @NonNull final String name) {
        if (name.equals(context.getString(R.string.title_page_dashboard))) {
            return POSITION_DASHBOARD;
        }
        if (name.equals(context.getString(R.string.title_page_comfort_zone))) {
            return POSITION_COMFORT_ZONE;
        }
        // TODO: UNCOMMENT WHEN GLOSSARY BECOMES ENABLED AGAIN
        //    if (name.equals(context.getString(R.string.title_page_glossary))) {
        //      return POSITION_GLOSSARY;
        //  }
        if (name.equals(context.getString(R.string.title_page_history))) {
            return POSITION_HISTORY;
        }
        if (name.equals(context.getString(R.string.title_page_device_management))) {
            return POSITION_DEVICE_SCAN;
        }
        if (name.equals(context.getString(R.string.title_page_settings))) {
            return POSITION_SETTINGS;
        }
        throw new IllegalArgumentException(String.format("%s: getPosition -> %s it's not implemented.", TAG, name));
    }

    // TODO: UNCOMMENT 'POSITION_GLOSSARY' WHEN GLOSSARY BECOMES ENABLED AGAIN
    @IntDef(flag = false,
            value = {POSITION_DASHBOARD, POSITION_COMFORT_ZONE, POSITION_HISTORY,
             /* POSITION_GLOSSARY,*/ POSITION_DEVICE_SCAN, POSITION_SETTINGS})
    @Retention(RetentionPolicy.SOURCE)
    public @interface AvailableSections {
    }
}
