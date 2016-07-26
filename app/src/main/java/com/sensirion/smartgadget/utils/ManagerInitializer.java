package com.sensirion.smartgadget.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.sensirion.smartgadget.peripheral.rht_sensor.RHTSensorFacade;
import com.sensirion.smartgadget.persistence.device_name_database.DeviceNameDatabaseManager;
import com.sensirion.smartgadget.persistence.history_database.HistoryDatabaseManager;
import com.sensirion.smartgadget.utils.view.ColorManager;

public final class ManagerInitializer {
    private ManagerInitializer() {
    }

    private static final String TAG = ManagerInitializer.class.getSimpleName();

    /**
     * Initialize the application managers. Should be called at the first execution of the application.
     *
     * @param context used for initialize the managers. Cannot be <code>null</code>
     */
    public static void initializeApplicationManagers(@NonNull final Context context) {
        final Context appContext = context.getApplicationContext();

        initColorManager(appContext);
        initSettingsManager(appContext);
        initHistoryDatabaseManager(appContext);
        initUserDeviceNameDatabaseManager(appContext);
        initSensorManager(appContext);
    }

    private static void initColorManager(@NonNull final Context context) {
        try {
            ColorManager.init(context);
        } catch (IllegalStateException e) {
            Log.w(TAG, String.format("init%s -> The manager was already initialized", ColorManager.class.getSimpleName()));
        }
    }

    private static void initSettingsManager(@NonNull final Context context) {
        try {
            Settings.init(context);
        } catch (IllegalStateException e) {
            Log.w(TAG, String.format("init%s -> The manager was already initialized", Settings.class.getSimpleName()));
        }
    }

    private static void initUserDeviceNameDatabaseManager(@NonNull final Context context) {
        try {
            DeviceNameDatabaseManager.init(context);
        } catch (IllegalStateException e) {
            Log.w(TAG, String.format("init%s -> The manager was already initialized", DeviceNameDatabaseManager.class.getSimpleName()));
        }
    }

    private static void initHistoryDatabaseManager(@NonNull final Context context) {
        try {
            HistoryDatabaseManager.init(context);
        } catch (IllegalStateException e) {
            Log.w(TAG, String.format("init%s -> The manager was already initialized", HistoryDatabaseManager.class.getSimpleName()));
        }
    }

    private static void initSensorManager(@NonNull final Context context) {
        try {
            RHTSensorFacade.init(context);
        } catch (IllegalStateException e) {
            Log.w(TAG, String.format("init%s -> The manager was already initialized", RHTSensorFacade.class.getSimpleName()));
        }
    }
}