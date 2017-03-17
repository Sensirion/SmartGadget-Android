package com.sensirion.smartgadget.utils.view;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.sensirion.smartgadget.R;
import com.sensirion.smartgadget.utils.Settings;

/**
 * Displays a {@link Dialog} showing a notice indicating that a Sensirion Smartgadget
 * is needed in order to use the application.
 */
public class SmartGadgetRequirementDialog extends GenericDialog {

    public SmartGadgetRequirementDialog(@NonNull final Activity activity) {
        super(activity, R.layout.smartgadget_notice_dialog, R.string.requirements_title);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
}
