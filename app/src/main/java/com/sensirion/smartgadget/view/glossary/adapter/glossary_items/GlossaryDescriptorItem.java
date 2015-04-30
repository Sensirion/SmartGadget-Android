package com.sensirion.smartgadget.view.glossary.adapter.glossary_items;

import android.graphics.drawable.Drawable;

public class GlossaryDescriptorItem implements GlossaryItem {
    public final String title;
    public final String description;
    public final Drawable icon;

    public GlossaryDescriptorItem(final String title, final String description, final Drawable icon) {
        this.title = title;
        this.description = description;
        this.icon = icon;
    }
}