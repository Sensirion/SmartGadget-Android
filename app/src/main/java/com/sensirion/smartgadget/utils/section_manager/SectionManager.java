package com.sensirion.smartgadget.utils.section_manager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;


public abstract class SectionManager extends FragmentPagerAdapter {

    protected SectionManager(@NonNull final FragmentManager fm) {
        super(fm);
    }

    abstract public int getPageId(final int position);

    public String getPageTitle(@NonNull final Context context, final int position) {
        return context.getResources().getString(getPageId(position));
    }

    @Override
    abstract public Fragment getItem(final int position);

    @Override
    abstract public int getCount();

    abstract public int getPosition(@NonNull final Context context, @NonNull final String name);
}
