package com.sensirion.smartgadget.tests;

import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;

import com.sensirion.smartgadget.R;
import com.sensirion.smartgadget.view.MainActivity;

public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {


    @Nullable
    private ViewPager mPager;

    public MainActivityTest() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mPager = (ViewPager) getActivity().findViewById(R.id.view_pager);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        mPager = null;
    }

    @SmallTest
    public void testViewPagerNonNull() {
        assertNotNull(mPager);
    }
}
