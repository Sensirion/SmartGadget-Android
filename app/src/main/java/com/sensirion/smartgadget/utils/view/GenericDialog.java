package com.sensirion.smartgadget.utils.view;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.WindowManager;

import com.sensirion.smartgadget.R;

/**
 * Displays a {@link Dialog} showing a notice explaining the app's privacy policy.
 */
public class GenericDialog extends Dialog implements View.OnClickListener {

    private final int mLayoutResId;
    private final int mTitleResId;

    public GenericDialog(@NonNull final Activity activity, final int layoutResId, final int titleResId) {
        super(activity);
        mLayoutResId = layoutResId;
        mTitleResId = titleResId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
        setTitle(mTitleResId);
        setContentView(mLayoutResId);
        findViewById(R.id.close_button).setOnClickListener(this);

        adjustWidth();
    }

    private void adjustWidth() {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(lp);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.close_button:
                dismiss();
                break;
            default:
                break;
        }
    }
}
