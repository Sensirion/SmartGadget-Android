package com.sensirion.smartgadget.view.glossary.adapter.view_holders;

import android.widget.ImageView;
import android.widget.TextView;

public class GlossaryDescriptorViewHolder {
    public final TextView itemTitle;
    public final TextView itemDescription;
    public final ImageView itemIcon;

    public GlossaryDescriptorViewHolder(final TextView itemTitle, final TextView itemDescription, final ImageView itemIcon) {
        this.itemTitle = itemTitle;
        this.itemDescription = itemDescription;
        this.itemIcon = itemIcon;
    }
}