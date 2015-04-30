package com.sensirion.smartgadget.view.preference.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sensirion.smartgadget.R;

import java.util.ArrayList;

public class PreferenceAdapter extends BaseAdapter {

    @NonNull
    private final Typeface mTypeface;

    @NonNull
    private final ArrayList<PreferenceObject> mPreferenceList = new ArrayList<>();

    public PreferenceAdapter(@NonNull final Context context) {
        super();
        mTypeface = Typeface.createFromAsset(context.getAssets(), "HelveticaNeueLTStd-Cn.otf");
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
                        @Nullable final View convertView,
                        @NonNull final ViewGroup parent) {
        final View view;
        if (convertView == null) {
            view = View.inflate(parent.getContext(), R.layout.listitem_preference, null);
        } else {
            view = convertView;
        }
        final PreferenceObject preference = mPreferenceList.get(position);
        final TextView titleTextView = (TextView) view.findViewById(R.id.preference_title);
        titleTextView.setTypeface(mTypeface);
        titleTextView.setText(preference.title.trim());
        final TextView summaryTextView = (TextView) view.findViewById(R.id.preference_summary);
        summaryTextView.setTypeface(mTypeface);
        if (preference.summary == null) {
            summaryTextView.setText("");
        } else {
            summaryTextView.setText(preference.summary.trim());
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
     * @param title of the preference.
     * @param summary of the preference.
     * @param clickListener that will be executed when the preference item is clicked.
     */
    public void addPreference(@NonNull final String title,
                              @Nullable final String summary,
                              @NonNull final View.OnClickListener clickListener) {
        mPreferenceList.add(new PreferenceObject(title, summary, clickListener));
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