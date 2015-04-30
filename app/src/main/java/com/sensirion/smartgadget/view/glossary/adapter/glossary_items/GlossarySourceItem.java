package com.sensirion.smartgadget.view.glossary.adapter.glossary_items;

import android.graphics.drawable.Drawable;

public class GlossarySourceItem implements GlossaryItem {
    public final String sourceName;
    public final Drawable icon;

    public GlossarySourceItem(final String sourceName, final Drawable icon) {
        this.sourceName = sourceName;
        this.icon = icon;
    }
}