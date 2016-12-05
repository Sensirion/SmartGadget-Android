package com.sensirion.smartgadget.utils.view;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.sensirion.smartgadget.R;
import com.sensirion.smartgadget.utils.Settings;

/**
 * Displays a {@link Dialog} showing a notice indicating that a Sensirion Smartgadget
 * is needed in order to use the application.
 */
public class SmartGadgetRequirementDialog extends Dialog implements View.OnClickListener {

    public SmartGadgetRequirementDialog(@NonNull final Activity activity) {
        super(activity);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
        setTitle(R.string.requirements_title);
        setContentView(R.layout.smartgadget_notice_dialog);
        final Button smartgadgetRequiredButton = (Button) findViewById(R.id.requirements_button);
        smartgadgetRequiredButton.setOnClickListener(this);
        final CheckBox mShowAgainCheckbox = (CheckBox) findViewById(R.id.requirements_checkbox);
        final boolean checked = Settings.getInstance().isSmartGadgetRequirementDisplayed();
        mShowAgainCheckbox.setChecked(checked);
        mShowAgainCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull final CompoundButton buttonView,
                                         final boolean isChecked) {
                Settings.getInstance().setSmartGadgetWarningDisplayed(!isChecked);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.requirements_button:
                dismiss();
                break;
            default:
                break;
        }
    }
}
