package com.sensirion.smartgadget.utils.view;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;
import android.util.TypedValue;

/**
 * Style a {@link Spannable} with a custom {@link Typeface}.
 */
public class ApplicationHeaderGenerator extends MetricAffectingSpan {

    private final float mTextSizeSP;
    private final int mShadowColor;
    @Nullable
    private Typeface mTypeface = null;

    /**
     * Load the {@link Typeface} and apply to a {@link Spannable}.
     */
    public ApplicationHeaderGenerator(@NonNull final Context context, @NonNull final String typefaceName, final float textSize, final int shadowColor) {
        mTypeface = Typeface.createFromAsset(context.getApplicationContext().getAssets(), typefaceName);
        mTextSizeSP = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSize, context.getResources().getDisplayMetrics());
        mShadowColor = shadowColor;
    }

    @Override
    public void updateMeasureState(@NonNull final TextPaint textPaint) {
        textPaint.setTypeface(mTypeface);
        textPaint.setTextSize(mTextSizeSP);
        textPaint.setShadowLayer(1.5f, -1f, -1f, mShadowColor);
    }

    @Override
    public void updateDrawState(@NonNull final TextPaint textPaint) {
        textPaint.setTypeface(mTypeface);
        textPaint.setTextSize(mTextSizeSP);
        textPaint.setShadowLayer(1.5f, -1f, -1f, mShadowColor);
    }
}