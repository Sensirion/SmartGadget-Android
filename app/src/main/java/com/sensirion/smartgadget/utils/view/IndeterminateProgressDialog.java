package com.sensirion.smartgadget.utils.view;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

public class IndeterminateProgressDialog extends ProgressDialog {

    private final String TAG = this.getClass().getSimpleName();

    public IndeterminateProgressDialog(@NonNull final Context context) {
        super(context);
        setIndeterminate(true);
    }

    public IndeterminateProgressDialog(@NonNull final Context context, @NonNull final String title, @NonNull final String message, final boolean isCancelable) {
        this(context);
        setTitle(title);
        setMessage(message);
        setCancelable(isCancelable);
    }

    /**
     * Use IndeterminateProgressDialog#show(activity) instead.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public void show() {
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            Log.e(TAG, "show -> Calling this method outside of the UI thread will produce an exception. (HINT use 'show (activity))'");
        }
        super.show();
    }

    /**
     * @see android.app.ProgressDialog#show()
     */
    public void show(@NonNull final Activity activity) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                IndeterminateProgressDialog.super.show();
            }
        });
    }

    /**
     * Use IndeterminateProgressDialog#setMessage(CharSequence, Activity) instead.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public void setMessage(@NonNull final CharSequence message) {
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            Log.e(TAG, "setMessage -> Calling this method outside of the UI thread will produce an exception. (HINT use 'setMessage(CharSequence, activity))'");
        }
        super.setMessage(message);
    }

    /**
     * @see android.app.ProgressDialog#setMessage(CharSequence)
     */
    public void setMessage(@NonNull final CharSequence message, @NonNull final Activity activity) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                IndeterminateProgressDialog.super.setMessage(message);
            }
        });
    }
}