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
package com.sensirion.smartgadget.view.preference.adapter;

import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sensirion.smartgadget.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PreferenceAdapter extends BaseAdapter {

    @NonNull
    private final Typeface mTypeface;

    @NonNull
    private final List<PreferenceObject> mPreferenceList =
            Collections.synchronizedList(new ArrayList<PreferenceObject>());

    public PreferenceAdapter(@NonNull final Typeface typeface) {
        super();
        mTypeface = typeface;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCount() {
        return mPreferenceList.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getItem(final int position) {
        return mPreferenceList.get(position);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getItemId(final int position) {
        return position;
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public View getView(final int position,
                        @Nullable View view,
                        @NonNull final ViewGroup parent) {

        final PreferenceViewHolder holder;
        if (view != null) {
            holder = (PreferenceViewHolder) view.getTag();
        } else {
            view = View.inflate(parent.getContext(), R.layout.listitem_preference, null);
            holder = new PreferenceViewHolder(view);
            view.setTag(holder);
        }

        final PreferenceObject preference = mPreferenceList.get(position);

        holder.summaryTextView.setText(preference.summary);

        holder.titleTextView.setTypeface(mTypeface);
        holder.titleTextView.setText(preference.title.trim());

        holder.summaryTextView.setTypeface(mTypeface);
        if (preference.summary == null) {
            holder.summaryTextView.setText("");
        } else {
            holder.summaryTextView.setText(preference.summary.trim());
        }

        view.setOnClickListener(preference.clickListener);
        return view;
    }

    /**
     * Removes all the elements from the preference list.
     */
    public void clear() {
        mPreferenceList.clear();
    }

    /**
     * Adds a preference to the preference list.
     *
     * @param title         of the preference.
     * @param summary       of the preference.
     * @param clickListener that will be executed when the preference item is clicked.
     */
    public void addPreference(@NonNull final String title,
                              @Nullable final String summary,
                              @NonNull final View.OnClickListener clickListener) {
        mPreferenceList.add(new PreferenceObject(title, summary, clickListener));
    }

    static class PreferenceViewHolder {
        @BindView(R.id.preference_summary)
        TextView summaryTextView;
        @BindView(R.id.preference_title)
        TextView titleTextView;

        PreferenceViewHolder(@NonNull final View view) {
            ButterKnife.bind(this, view);
        }
    }

    private static class PreferenceObject {
        @NonNull
        private final String title;
        @Nullable
        private final String summary;
        @NonNull
        private final View.OnClickListener clickListener;

        private PreferenceObject(@NonNull final String title,
                                 @Nullable final String summary,
                                 @NonNull final View.OnClickListener clickListener) {
            this.title = title;
            this.summary = summary;
            this.clickListener = clickListener;
        }
    }
}
