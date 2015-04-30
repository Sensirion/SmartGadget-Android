package com.sensirion.smartgadget.utils.download;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.sensirion.smartgadget.R;
import com.sensirion.smartgadget.persistence.device_name_database.DeviceNameDatabaseManager;

class DownloadNotificationCenter {

    private static final String TAG = DownloadNotificationCenter.class.getSimpleName();
    private static final String DEFAULT_NOTIFICATION_TEXT = "";

    private static int mNotificationCenterCounter = 1;
    private final int mNotificationCenterId = mNotificationCenterCounter++;
    @NonNull
    private final String mDeviceAddress;
    @NonNull
    private final String mNotificationTitle;
    @NonNull
    private final NotificationManager mNotificationManager;
    @NonNull
    private final NotificationCompat.Builder mNotificationBuilder;
    @NonNull
    private final PendingIntent mContentIntent;
    @NonNull
    private String mNotificationText;
    @Nullable
    private Integer mNumberValuesToDownload = null;
    private boolean mDownloadHasFinished = false;

    DownloadNotificationCenter(@NonNull final Context context, @NonNull final String title, @NonNull final String deviceAddress, final int numberValuesToDownload) {
        mDeviceAddress = deviceAddress;
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationTitle = title;
        mNumberValuesToDownload = numberValuesToDownload;
        mNotificationText = DEFAULT_NOTIFICATION_TEXT;
        mContentIntent = PendingIntent.getActivity(context, 0, new Intent(), 0);
        mNotificationBuilder = new NotificationCompat.Builder(context);
        createNotification();
    }

    private void createNotification() {
        mNotificationBuilder
                .setContentTitle(mNotificationTitle)
                .setContentText(mNotificationText)
                .setSmallIcon(R.drawable.download_notification_icon).setContentIntent(mContentIntent);

        Notification notification = mNotificationBuilder.build();
        mNotificationManager.notify(mNotificationCenterId, notification);
    }

    /**
     * Sets the total number of elements to download.
     *
     * @param numberValuesToDownload with the total number of elements to download.
     */
    public void setNumberValuesToDownload(final int numberValuesToDownload) {
        mNumberValuesToDownload = numberValuesToDownload;
        Log.d(TAG, String.format("setNumberValuesToDownload -> %d values will be downloaded from the device.", numberValuesToDownload));
    }

    /**
     * Updates the notification for updating the download progress.
     *
     * @param progress progress of the download.
     */
    public void updateNotification(@NonNull final Context context, final int progress) {
        if (mDownloadHasFinished || mNumberValuesToDownload == null) {
            return;
        }
        if (progress >= mNumberValuesToDownload) {
            mDownloadHasFinished = true;
            mNotificationText = context.getString(R.string.log_download_notification_download_finished);
        }

        final int downloadPercentage = Math.round(100f * progress / mNumberValuesToDownload);
        final String deviceName = DeviceNameDatabaseManager.getInstance().readDeviceName(mDeviceAddress);
        mNotificationText = String.format(context.getResources().getString(R.string.log_download_notification_progress_text), deviceName, progress, mNumberValuesToDownload, downloadPercentage);
        createNotification();
        Log.d(TAG, String.format("updateNotification -> Notification updated. %d/%d.", progress, mNumberValuesToDownload));
    }

    /**
     * Cancels the notification.
     */
    public void cancel() {
        Log.w(TAG, "cancel() --> stops notification");
        mNotificationManager.cancel(mNotificationCenterId);
    }
}