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
    private static final String LOCATION_PERMISSION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int PERMISSION_REQUEST_CODE = 123;
    private static AlertDialog mDialog;

    private PhoneSettingsSetup() {
    }

    public static boolean isLocationSetupMissing(final Context context) {
        return !isLocationPermitted(context) || !isLocationEnabled(context);
    }

    public static boolean isLocationPermitted(final Context context) {
        return ContextCompat.checkSelfPermission(context, LOCATION_PERMISSION)
                == PackageManager.PERMISSION_GRANTED;
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
                            if (!isLocationPermitted(activity.getApplicationContext())) {
                                ActivityCompat.requestPermissions(activity,
                                        new String[]{LOCATION_PERMISSION}, PERMISSION_REQUEST_CODE);
                            }
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
