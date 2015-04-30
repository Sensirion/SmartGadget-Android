package com.sensirion.smartgadget.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.TypedValue;


public abstract class XmlFloatExtractor {

    public static float getFloatValueFromId(@NonNull final Context context, final int resourceId) {
        final TypedValue outValue = new TypedValue();
        context.getResources().getValue(resourceId, outValue, true);
        return outValue.getFloat();
    }
}