package com.sensirion.smartgadget.utils.view;

import android.app.Activity;
import android.app.Dialog;
import android.support.annotation.NonNull;

import com.sensirion.smartgadget.R;

/**
 * Displays a {@link Dialog} showing a notice explaining the app's privacy policy.
 */
public class PrivacyPolicyDialog extends GenericDialog {
    public PrivacyPolicyDialog(@NonNull final Activity activity) {
        super(activity, R.layout.privacy_policy_dialog, R.string.policy_title);
    }
}
