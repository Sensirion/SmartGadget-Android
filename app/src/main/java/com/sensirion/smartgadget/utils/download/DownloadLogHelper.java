package com.sensirion.smartgadget.utils.download;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.sensirion.libble.devices.BleDevice;
import com.sensirion.libble.listeners.history.HistoryListener;
import com.sensirion.libble.services.AbstractHistoryService;
import com.sensirion.smartgadget.R;
import com.sensirion.smartgadget.persistence.device_name_database.DeviceNameDatabaseManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

public class DownloadLogHelper implements HistoryListener {

    private static final String TAG = DownloadLogHelper.class.getSimpleName();
    @Nullable
    private static DownloadLogHelper mInstance = null;
    @NonNull
    private final Map<String, DownloadNotificationCenter> mDownloadCenters = Collections.synchronizedMap(new HashMap<String, DownloadNotificationCenter>());
    @Nullable
    private ProgressDialog mProgressLogDownloadDialog;
    @Nullable
    private String mProgressDialogAddress;
    private boolean mFirstProgressDialogNotification = false;
    @NonNull
    private Handler mHandler;
    @NonNull
    private Context mAppContext;

    private DownloadLogHelper() {
    }

    @Nullable
    public synchronized static DownloadLogHelper getInstance() {
        if (mInstance == null) {
            mInstance = new DownloadLogHelper();
        }
        return mInstance;
    }

    /**
     * Starts to download data of a device.
     *
     * @param loggingService of the download service we want to add.
     * @param activity       {@link android.app.Activity} that is going to show the data.
     */
    public void downloadLoggedData(@NonNull final Activity activity, @NonNull final AbstractHistoryService loggingService, @NonNull final Handler handler) {
        mHandler = handler;
        mAppContext = activity.getApplicationContext();

        activity.findViewById(R.id.manage_device_button_download_log).setEnabled(false);

        final Integer numberElementsToLog = loggingService.getNumberLoggedElements();

        Log.i(TAG, String.format("downloadLoggedData -> The user has to download %d elements.", numberElementsToLog));

        if (numberElementsToLog == null || numberElementsToLog <= 0) {
            Log.e(TAG, "downloadLoggedData -> The device does not have elements to download.");
            return;
        }

        mProgressDialogAddress = loggingService.getDeviceAddress();

        //Initializes the progress dialog.
        prepareProgressDialog(activity, mProgressDialogAddress, numberElementsToLog);

        //Registers the listener.
        loggingService.registerNotificationListener(DownloadLogHelper.this);

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "downloadLoggedData -> Start to download data all the data from the device.");
                loggingService.startDataDownload();
            }
        });
    }

    private void prepareProgressDialog(@NonNull final Activity context, @NonNull final String deviceAddress, final int numberElementsLog) {
        final String deviceName = DeviceNameDatabaseManager.getInstance().readDeviceName(deviceAddress);

        mProgressLogDownloadDialog = new ProgressDialog(context);
        mProgressLogDownloadDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressLogDownloadDialog.setTitle(context.getResources().getString(R.string.please_wait));
        mProgressLogDownloadDialog.setMessage(String.format("%s: %s", context.getResources().getString(R.string.downloading_data), deviceName));
        mProgressLogDownloadDialog.setMax(numberElementsLog);
        mProgressLogDownloadDialog.setProgress(0);
        mProgressLogDownloadDialog.setCancelable(false);
        mProgressLogDownloadDialog.setIndeterminate(false);
        mProgressLogDownloadDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "HIDE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(@NonNull DialogInterface dialog, int which) {
                dialog.dismiss();
                dialog.cancel();
                final DownloadNotificationCenter center = new DownloadNotificationCenter(context, context.getString(R.string.log_download_notification_title), deviceAddress, numberElementsLog);
                mDownloadCenters.put(deviceAddress, center);
            }
        });
        mFirstProgressDialogNotification = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDownloadProgress(@NonNull final BleDevice device, final int downloadProgress) {
        if (mProgressLogDownloadDialog == null) {
            Log.w(TAG, "setDownloadProgress -> Progress dialog it's not initialized yet.");
            return;
        }

        if (mFirstProgressDialogNotification && device.getAddress().equals(mProgressDialogAddress)) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mProgressLogDownloadDialog.show();
                }
            });
            mFirstProgressDialogNotification = false;
        }

        if (mProgressLogDownloadDialog.isShowing()) {
            updateDownloadDialog(device, downloadProgress);
        } else {
            updateNotification(device.getAddress(), downloadProgress);
        }
    }

    private void updateDownloadDialog(@NonNull final BleDevice device, final int downloadProgress) {
        if (mProgressLogDownloadDialog != null && device.getAddress().equals(mProgressDialogAddress)) {
            Log.i(TAG, String.format("updateDownloadDialog -> Device %s has a progress of %d out of %d.", device.getAddress(), downloadProgress, mProgressLogDownloadDialog.getMax()));
            mProgressLogDownloadDialog.setProgress(downloadProgress);
            if (device.isConnected() && mProgressLogDownloadDialog.getProgress() < mProgressLogDownloadDialog.getMax()) {
                return;
            }
            mProgressLogDownloadDialog.cancel();
        }
    }

    private void updateNotification(@NonNull final String deviceAddress, final int downloadProgress) {
        if (mDownloadCenters.get(deviceAddress) == null) {
            Log.w(TAG, "updateNotification -> Can't update a notification because the download center is null.");
            return;
        }
        mDownloadCenters.get(deviceAddress).updateNotification(mAppContext, downloadProgress);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAmountElementsToDownload(@NonNull final BleDevice device, final int amount) {
        if (mProgressLogDownloadDialog == null) {
            Log.w(TAG, "setAmountElementsToDownload() -> Progress dialog it's not initialized yet.");
            return;
        }
        if (mProgressLogDownloadDialog.isShowing()) {
            Log.i(TAG, String.format("setAmountElementsToDownload() -> Changed download max for %d", amount));
            mProgressLogDownloadDialog.setMax(amount);
            mProgressLogDownloadDialog.setProgress(0);
        }
        final DownloadNotificationCenter center = mDownloadCenters.get(device.getAddress());
        if (center != null) {
            center.setNumberValuesToDownload(amount);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLogDownloadFailure(@NonNull final BleDevice device) {
        closeNotifications(device);
        showToast(device.getAddress(), R.string.download_complete_failure);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLogDownloadCompleted(@NonNull final BleDevice device) {
        closeNotifications(device);
        showToast(device.getAddress(), R.string.download_complete_success);
    }

    private void closeNotifications(@NonNull final BleDevice device) {
        if (device.getAddress().equals(mProgressDialogAddress)) {
            if (mProgressLogDownloadDialog != null) {
                mProgressLogDownloadDialog.cancel();
            }
        }
        final DownloadNotificationCenter center = mDownloadCenters.get(device.getAddress());
        if (center != null) {
            center.cancel();
        }
    }

    private void showToast(@NonNull final String deviceAddress, final int toastTextId) {
        final String deviceName = DeviceNameDatabaseManager.getInstance().readDeviceName(deviceAddress);
        final String toastText = mAppContext.getResources().getString(toastTextId, deviceName);

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mAppContext, toastText, Toast.LENGTH_LONG).show();
            }
        });
    }
}