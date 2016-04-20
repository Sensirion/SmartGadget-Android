package com.sensirion.smartgadget.utils.view;

import android.database.DataSetObserver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Based on the class found in: http://blogingtutorials.blogspot.ch/2010/11/android-listview-header-two-or-more-in.html
 */

abstract public class SectionAdapter extends BaseAdapter {

    private static final byte HEADER_POSITION = 0;

    private static final String TAG = SectionAdapter.class.getSimpleName();

    @NonNull
    private final List<SectionItem> mSectionsList = Collections.synchronizedList(new ArrayList<SectionItem>());

    @Nullable
    abstract protected View getHeaderView(final String caption, final int itemIndex, final View convertView, final ViewGroup parent);

    @Override
    public synchronized int getItemViewType(final int itemPosition) {
        int headerOffset = HEADER_POSITION + 1;
        int fixedItemPosition = itemPosition;
        for (final SectionItem section : mSectionsList) {
            if (fixedItemPosition == HEADER_POSITION) {
                return HEADER_POSITION;
            }
            final int itemCount = section.getAdapter().getCount() + 1;
            if (fixedItemPosition < itemCount) {
                return (headerOffset + section.getAdapter().getItemViewType(fixedItemPosition - 1));
            }
            fixedItemPosition -= itemCount;
            headerOffset += section.getAdapter().getViewTypeCount();
        }
        return -1;
    }

    @Override
    public synchronized int getViewTypeCount() {
        Log.d(TAG, "getViewTypeCount()");
        // one for the header, plus those from mSectionsList
        int totalItems = 1;
        for (final SectionItem section : mSectionsList) {
            totalItems += section.getAdapter().getViewTypeCount();
        }
        return totalItems;
    }

    @Nullable
    @Override
    public synchronized Object getItem(final int itemPosition) {
        Log.d(TAG, String.format("getItem() -> Position %d was retrieved. ", itemPosition));
        int fixedItemPosition = itemPosition;
        for (final SectionItem section : mSectionsList) {
            if (fixedItemPosition == 0) {
                return section;
            }
            final int size = section.getAdapter().getCount() + 1;
            if (fixedItemPosition < size) {
                return section.getAdapter().getItem(fixedItemPosition - 1);
            }
            fixedItemPosition -= size;
        }
        return null;
    }

    @Override
    public synchronized int getCount() {
        int numberItems = 0;
        for (final SectionItem sectionItem : mSectionsList) {
            numberItems += sectionItem.getAdapter().getCount() + 1;
        }
        return numberItems;
    }

    @Nullable
    @Override
    public synchronized View getView(final int itemPosition, final View convertView, final ViewGroup parent) {
        int sectionIndex = 0;
        int fixedPosition = itemPosition;
        for (final SectionItem sectionItem : mSectionsList) {
            if (fixedPosition == HEADER_POSITION) {
                return getHeaderView(sectionItem.getCaption(), sectionIndex, convertView, parent);
            }
            final int sectionSize = sectionItem.getAdapter().getCount() + 1;
            if (fixedPosition < sectionSize) {
                return sectionItem.getAdapter().getView(fixedPosition - 1, convertView, parent);
            }
            fixedPosition -= sectionSize;
            sectionIndex++;
        }
        throw new IndexOutOfBoundsException(String.format("%s: getView -> Position %d is outside the bounds of this adapter. (size: %d).", TAG, itemPosition, getCount()));
    }

    @Override
    public boolean isEnabled(final int itemPosition) {
        return (getItemViewType(itemPosition) != HEADER_POSITION);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public long getItemId(final int itemPosition) {
        return itemPosition;
    }

    public synchronized void addSectionToAdapter(final String title, @NonNull final Adapter adapter) {
        final SectionItem newSection = new SectionItem(title, adapter);
        mSectionsList.add(newSection);
    }

    private class SectionItem extends DataSetObserver {

        private final String mCaption;
        @NonNull
        private final Adapter mAdapter;

        SectionItem(final String caption, @NonNull final Adapter adapter) {
            mCaption = caption;
            mAdapter = adapter;
            adapter.registerDataSetObserver(this);
        }

        @Override
        public void onChanged() {
            SectionAdapter.this.notifyDataSetChanged();
        }

        private String getCaption() {
            return mCaption;
        }

        @NonNull
        private Adapter getAdapter() {
            return mAdapter;
        }
    }
}