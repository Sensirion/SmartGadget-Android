/*
 * Copyright (c) 2017, Sensirion AG
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of Sensirion AG nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
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
