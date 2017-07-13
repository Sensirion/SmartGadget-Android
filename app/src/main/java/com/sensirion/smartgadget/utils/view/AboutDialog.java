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

import android.app.Activity;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.TextView;

import com.sensirion.smartgadget.R;

import java.util.Calendar;

/**
 * Displays a {@link Dialog} showing a notice explaining the app's privacy policy.
 */
public class AboutDialog extends GenericDialog {

    private static final String TAG = AboutDialog.class.getSimpleName();

    public AboutDialog(@NonNull final Activity activity) {
        super(activity, R.layout.about_dialog, R.string.label_about);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final TextView aboutText = (TextView) findViewById(R.id.about_txt);
        aboutText.setText(createAboutText());
    }

    private String createAboutText() {
        String versionName = null;
        final PackageManager packageManager = getContext().getPackageManager();
        final String packageName = getContext().getPackageName();
        try {
            versionName = packageManager.getPackageInfo(packageName, 0).versionName;
        } catch (final PackageManager.NameNotFoundException e) {
            Log.e(TAG, "showAboutText -> The following error was produced " +
                    "when obtaining the version name -> ", e);
        }

        final String appName = getContext().getString(R.string.app_name);
        final String appPlatform = getContext().getString(R.string.app_platform);
        final String copyright = getContext().getString(R.string.txt_about_char_copyright);
        final String sensirionAg = getContext().getString(R.string.about_sensirion_ag);

        final StringBuilder aboutText = new StringBuilder();

        final int deviceYear = Calendar.getInstance().get(Calendar.YEAR);
        aboutText.append(appName)
                .append(" ")
                .append(appPlatform)
                .append(" ")
                .append(versionName)
                .append(System.getProperty("line.separator"))
                .append(copyright)
                .append(" ")
                .append((deviceYear <= 2016 ? 2016 : deviceYear))
                .append(" ")
                .append(sensirionAg);

        return aboutText.toString();
    }
}
