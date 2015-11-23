package com.sensirion.smartgadget.utils;

import android.content.Context;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.util.TypedValue;

public final class XmlFloatExtractor {

    private XmlFloatExtractor(){}

    /**
     * Extracts a dimension value from the XML resources into a float.
     * @param context needed to access the XML resources.
     * @param resourceId that will be extracted into a float.
     * @return {@link float} with the extracted float value.
     */
    public static float getFloatValueFromId(@NonNull final Context context,
                                            @DimenRes final int resourceId) {
        final TypedValue value = new TypedValue();
        context.getResources().getValue(resourceId, value, true);
        return value.getFloat();
    }
}