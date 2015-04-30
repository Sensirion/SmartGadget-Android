package com.sensirion.smartgadget.view.glossary.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sensirion.smartgadget.R;
import com.sensirion.smartgadget.view.glossary.adapter.glossary_items.GlossaryDescriptorItem;
import com.sensirion.smartgadget.view.glossary.adapter.glossary_items.GlossaryItem;
import com.sensirion.smartgadget.view.glossary.adapter.glossary_items.GlossarySourceItem;
import com.sensirion.smartgadget.view.glossary.adapter.view_holders.GlossaryDescriptorViewHolder;
import com.sensirion.smartgadget.view.glossary.adapter.view_holders.GlossarySourceViewHolder;

import java.util.ArrayList;

public class GlossaryAdapter extends BaseAdapter {

    private final Typeface mTypefaceNormal;
    private final Typeface mTypefaceBold;
    private final LayoutInflater mInflater;
    @NonNull
    private final ArrayList<GlossaryItem> mGlossaryDatabase;

    public GlossaryAdapter(final LayoutInflater inflater, @NonNull final Context context) {
        super();
        mGlossaryDatabase = new ArrayList<>();
        mInflater = inflater;

        mTypefaceNormal = Typeface.createFromAsset(context.getAssets(), "HelveticaNeueLTStd-Cn.otf");
        mTypefaceBold = Typeface.createFromAsset(context.getAssets(), "HelveticaNeueLTStd-Bd.otf");
    }

    @Override
    public int getCount() {
        return mGlossaryDatabase.size();
    }

    @Override
    public Object getItem(final int i) {
        return mGlossaryDatabase.get(i);
    }

    @Override
    public long getItemId(final int i) {
        return i;
    }

    @Override
    public View getView(final int i, final View view, final ViewGroup viewGroup) {
        if (mGlossaryDatabase.get(i) instanceof GlossaryDescriptorItem) {
            return getDescriptorView(i, viewGroup);
        }
        return getSourceView(i, viewGroup);
    }

    @NonNull
    private View getDescriptorView(final int i, final ViewGroup viewGroup) {
        final GlossaryDescriptorViewHolder viewHolder;

        final View view = mInflater.inflate(R.layout.listitem_glossary_item, viewGroup, false);
        final TextView itemTitle = (TextView) view.findViewById(R.id.glossary_item_title);
        final TextView itemDescription = (TextView) view.findViewById(R.id.glossary_item_description);
        final ImageView itemIcon = (ImageView) view.findViewById(R.id.glossary_icon);
        viewHolder = new GlossaryDescriptorViewHolder(itemTitle, itemDescription, itemIcon);
        view.setTag(viewHolder);

        final GlossaryDescriptorItem descriptor = ((GlossaryDescriptorItem) mGlossaryDatabase.get(i));

        //Sets the title.
        viewHolder.itemTitle.setText(descriptor.title);
        viewHolder.itemTitle.setTypeface(mTypefaceBold);

        //Sets the description.
        final String description = descriptor.description;
        viewHolder.itemDescription.setText(description);
        viewHolder.itemDescription.setTypeface(mTypefaceNormal);

        //Sets the icon
        viewHolder.itemIcon.setImageDrawable(descriptor.icon);

        return view;
    }

    @NonNull
    private View getSourceView(final int i, final ViewGroup viewGroup) {
        final GlossarySourceViewHolder viewHolder;

        final View sourceView = mInflater.inflate(R.layout.listitem_glossary_source_item, viewGroup, false);
        final TextView itemSourceName = (TextView) sourceView.findViewById(R.id.glossary_soure_source_name);
        final ImageView itemIcon = (ImageView) sourceView.findViewById(R.id.glossary_source_icon);
        viewHolder = new GlossarySourceViewHolder(itemSourceName, itemIcon);
        sourceView.setTag(viewHolder);

        final GlossarySourceItem sourceItem = ((GlossarySourceItem) mGlossaryDatabase.get(i));

        //Sets the source name.
        viewHolder.itemSourceText.setText(sourceItem.sourceName);
        viewHolder.itemSourceText.setTypeface(mTypefaceNormal);

        //Sets the icon
        viewHolder.itemSourceIcon.setImageDrawable(sourceItem.icon);

        return sourceView;
    }

    public void addDevice(@NonNull final String title, @NonNull final String description, @NonNull final Drawable icon) {
        mGlossaryDatabase.add(new GlossaryDescriptorItem(title, description, icon));
    }

    public void addSource(@NonNull final String sourceName, @NonNull final Drawable icon) {
        mGlossaryDatabase.add(new GlossarySourceItem(sourceName, icon));
    }
}