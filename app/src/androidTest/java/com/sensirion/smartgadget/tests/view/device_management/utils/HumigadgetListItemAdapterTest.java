package com.sensirion.smartgadget.tests.view.device_management.utils;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.sensirion.libble.devices.BleDevice;
import com.sensirion.smartgadget.R;
import com.sensirion.smartgadget.view.device_management.utils.HumigadgetListItemAdapter;

import java.util.LinkedList;
import java.util.List;

public class HumigadgetListItemAdapterTest extends AndroidTestCase {

    private static final byte rssiTestDevice1 = -21;
    private static final byte rssiTestDevice2 = -43;

    // Test Devices
    @NonNull
    private static final BleDevice mTestDevice1 = new MockBleDevice(rssiTestDevice1);
    @NonNull
    private static final BleDevice mTestDevice2 = new MockBleDevice(rssiTestDevice2);

    @Nullable
    private HumigadgetListItemAdapter mAdapter;

    protected void setUp() throws Exception {
        super.setUp();

        final AssetManager assets = getContext().getAssets();
        final Resources resources = getContext().getResources();

        final String typefaceNormalLocation = resources.getString(R.string.typeface_condensed);
        final String typefaceBoldLocation = resources.getString(R.string.typeface_bold);

        final Typeface typefaceNormal = Typeface.createFromAsset(assets, typefaceNormalLocation);
        final Typeface typefaceBold = Typeface.createFromAsset(assets, typefaceBoldLocation);

        mAdapter = new HumigadgetListItemAdapter(typefaceNormal, typefaceBold);
    }

    @SmallTest
    public void testPrerequisites() {
        assertNotNull("testPrerequisites: mAdapter is needed", mAdapter);
    }

    @SmallTest
    public void testInitialAdapterState() {
        assertNotNull("testInitialAdapterState: mAdapter is needed", mAdapter);
        assertEquals("testInitialAdapterState: The adapter must not contain any device since it was just created", mAdapter.getCount(), 0);
    }

    @SmallTest
    public void testAddAll() {
        assertNotNull("testAddAll: mAdapter is needed", mAdapter);
        final List<BleDevice> devices = new LinkedList<>();
        devices.add(mTestDevice1);
        mAdapter.addAll(devices);
        assertEquals("testAddAll: Test if the device was properly inserted", mTestDevice1, mAdapter.getItem(0));
        devices.add(mTestDevice2);
        mAdapter.addAll(devices);
        assertEquals("testAddAll: Test only the missing element was inserted", devices.size(), mAdapter.getCount());
        //Test the first element, which has a better RSSI, is on the first position
        assertEquals("testAddAll: The list is not properly ordered", mTestDevice1, mAdapter.getItem(0));
        //Test that the second element, which has a worse RSSI, is on the second position
        assertEquals("testAddAll: The list is not properly ordered", mTestDevice2, mAdapter.getItem(1));
    }
}