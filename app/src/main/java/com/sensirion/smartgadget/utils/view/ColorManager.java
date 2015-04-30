package com.sensirion.smartgadget.utils.view;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.Log;

import com.sensirion.smartgadget.R;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ColorManager {

    private static final String TAG = ColorManager.class.getSimpleName();
    private static ColorManager mInstance;
    @NonNull
    private final List<Integer> mDefinedColorArray = new LinkedList<>();
    @NonNull
    private final Map<String, Integer> mColorsInUse = new HashMap<>();

    private ColorManager(@NonNull final Context context) {
        initColorArray(context);
    }

    @NonNull
    public static ColorManager getInstance() {
        if (mInstance == null) {
            throw new IllegalStateException(String.format("%s: getInstance -> Has not been initialized yet.", TAG));
        }
        return mInstance;
    }

    public static void init(@NonNull final Context context) {
        if (mInstance == null) {
            mInstance = new ColorManager(context);
        }
    }

    private void initColorArray(@NonNull final Context context) {
        mDefinedColorArray.add(context.getResources().getColor(R.color.green));
        mDefinedColorArray.add(context.getResources().getColor(R.color.red));
        mDefinedColorArray.add(context.getResources().getColor(R.color.orange));
        mDefinedColorArray.add(context.getResources().getColor(R.color.cyan));
        mDefinedColorArray.add(context.getResources().getColor(R.color.violet));
        mDefinedColorArray.add(context.getResources().getColor(R.color.royalblue));
        mDefinedColorArray.add(Color.MAGENTA);
        mDefinedColorArray.add(context.getResources().getColor(R.color.lila));
    }

    /**
     * Checks if the device knows the color value.
     *
     * @param deviceAddress of the device.
     * @return <code>true</code> if the color is known - <code>false</code> otherwise.
     */
    public boolean hasDeviceColor(@NonNull final String deviceAddress) {
        return mColorsInUse.containsKey(deviceAddress);
    }

    /**
     * Obtains the color of a device. If the device doesn't have a color it assigns it.
     *
     * @param deviceAddress of the device.
     * @return {@link java.lang.Integer} with the color.
     */
    public int getDeviceColor(@NonNull final String deviceAddress) {
        if (hasDeviceColor(deviceAddress)) {
            return mColorsInUse.get(deviceAddress);
        }

        for (final int color : mDefinedColorArray) {
            if (mColorsInUse.values().contains(color)) {
                continue;
            }
            mColorsInUse.put(deviceAddress, color);
            Log.i(TAG, String.format("getDeviceColor() -> new color assigned: %d", color));
            return color;
        }
        final int randomColor = getRandomHsvColor();
        mColorsInUse.put(deviceAddress, randomColor);
        Log.i(TAG, String.format("getDeviceColor -> RANDOM color used: %d", randomColor));
        return randomColor;
    }

    private int getRandomHsvColor() {
        Integer color;
        do {
            final float[] hsv = new float[]{(new Random()).nextInt(360), 1, 1};
            color = Color.HSVToColor(255, hsv);
        } while (mColorsInUse.values().contains(color));
        return color;
    }
}