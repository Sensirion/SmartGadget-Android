package com.sensirion.smartgadget.utils.view;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;

import com.sensirion.smartgadget.R;

/**
 * Displays a {@link Dialog} showing a notice explaining the app's privacy policy.
 */
public class PrivacyPolicyDialog extends Dialog implements View.OnClickListener {

    public PrivacyPolicyDialog(@NonNull final Activity activity) {
        super(activity);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
        setTitle(R.string.policy_title);
        setContentView(R.layout.privacy_policy_dialog);
        final Button gotItbutton = (Button) findViewById(R.id.privacy_policy_button);
        gotItbutton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.privacy_policy_button:
                dismiss();
                break;
            default:
                break;
        }
    }
}
