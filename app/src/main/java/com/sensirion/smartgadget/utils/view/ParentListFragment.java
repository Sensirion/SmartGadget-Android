package com.sensirion.smartgadget.utils.view;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;

import butterknife.Unbinder;

public abstract class ParentListFragment extends ListFragment {

    @Nullable
    protected Activity mActivity = null;
    @Nullable
    protected Unbinder unbinder = null;

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