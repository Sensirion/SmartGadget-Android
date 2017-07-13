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
package com.sensirion.smartgadget.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.sensirion.libsmartgadget.utils.BLEUtility;
import com.sensirion.smartgadget.R;

public final class PhoneSettingsSetup {
    private static final int PERMISSION_REQUEST_CODE = 123;
    private static AlertDialog mDialog;

    private PhoneSettingsSetup() {
    }

    public static boolean isLocationSetupMissing(final Context context) {
        return !isLocationPermitted(context) || !isLocationEnabled(context);
    }

    public static boolean isLocationPermitted(final Context context) {
        return BLEUtility.hasScanningPermission(context);
    }

    public static boolean isLocationEnabled(final Context context) {
        try {
            return Settings.Secure.getInt(context.getContentResolver(),
                    Settings.Secure.LOCATION_MODE) != Settings.Secure.LOCATION_MODE_OFF;
        } catch (Settings.SettingNotFoundException e) {
            return !TextUtils.isEmpty(Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.LOCATION_PROVIDERS_ALLOWED));
        }
    }

    public static boolean isBluetoothEnabled(final Context context) {
        return BLEUtility.isBLEEnabled(context);
    }

    public static boolean lazyRequestPhoneSetup(final Activity activity) {
        if (activity == null) return false;

        if (isLocationSetupMissing(activity.getApplicationContext())) {
            showPermissionRequestWarning(activity,
                    activity.getString(R.string.dialog_phone_setup_location_title),
                    activity.getString(R.string.dialog_phone_setup_location_message),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            BLEUtility.requestScanningPermission(activity, PERMISSION_REQUEST_CODE);
                            if (!isLocationEnabled(activity.getApplicationContext())) {
                                activity.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                            }
                        }
                    });
            return true;
        }

        if (!isBluetoothEnabled(activity.getApplicationContext())) {
            BLEUtility.requestEnableBluetooth(activity);
            return true;
        }

        return false;
    }

    private static void showPermissionRequestWarning(final Activity activity, final String title,
                                                     final String contentMessage,
                                                     final DialogInterface.OnClickListener onClickListener) {
        if (mDialog != null) {
            mDialog.dismiss();
        }
        mDialog = new AlertDialog.Builder(activity)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        activity.moveTaskToBack(true);
                    }
                })
                .setPositiveButton("OK", onClickListener)
                .setTitle(title)
                .setMessage(contentMessage)
                .create();
        mDialog.show();
    }
}
