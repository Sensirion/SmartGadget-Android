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
