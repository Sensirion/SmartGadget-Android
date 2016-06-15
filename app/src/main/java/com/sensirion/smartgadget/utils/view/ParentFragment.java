package com.sensirion.smartgadget.utils.view;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public abstract class ParentFragment extends Fragment {

    private final String TAG = this.getClass().getSimpleName();

    @Nullable
    protected Activity mActivity = null;
    @Nullable
    protected Unbinder unbinder = null;
    protected boolean viewInflated = false;

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        if (!Activity.class.isInstance(context)) {
            throw new RuntimeException("Must attach an Activity");
        }
        mActivity = (Activity) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null) {
            unbinder.unbind();
            viewInflated = false;
        }
    }

    /**
     * Gets the parent activity of the device.
     *
     * @return {@link android.app.Activity} of the fragment.
     */
    @Nullable
    public Activity getParent() {
        final Activity activity = super.getActivity();
        if (activity == null) {
            return mActivity;
        }
        mActivity = activity;
        return mActivity;
    }
}