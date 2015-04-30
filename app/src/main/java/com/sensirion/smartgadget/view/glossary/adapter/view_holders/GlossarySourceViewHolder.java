package com.sensirion.smartgadget.view.glossary.adapter.view_holders;

import android.widget.ImageView;
import android.widget.TextView;

public class GlossarySourceViewHolder {
    public final TextView itemSourceText;
    public final ImageView itemSourceIcon;

    public GlossarySourceViewHolder(final TextView itemSourceText, final ImageView itemSourceIcon) {
        this.itemSourceText = itemSourceText;
        this.itemSourceIcon = itemSourceIcon;
    }
}