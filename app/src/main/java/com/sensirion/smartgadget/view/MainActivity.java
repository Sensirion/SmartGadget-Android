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
package com.sensirion.smartgadget.view;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sensirion.smartgadget.R;
import com.sensirion.smartgadget.peripheral.rht_sensor.RHTSensorFacade;
import com.sensirion.smartgadget.peripheral.rht_sensor.RHTSensorListener;
import com.sensirion.smartgadget.peripheral.rht_sensor.external.RHTHumigadgetSensorManager;
import com.sensirion.smartgadget.persistence.device_name_database.DeviceNameDatabaseManager;
import com.sensirion.smartgadget.utils.ManagerInitializer;
import com.sensirion.smartgadget.utils.Settings;
import com.sensirion.smartgadget.utils.section_manager.SectionManager;
import com.sensirion.smartgadget.utils.section_manager.SectionManagerMobile;
import com.sensirion.smartgadget.utils.section_manager.SectionManagerTablet;
import com.sensirion.smartgadget.utils.view.ApplicationHeaderGenerator;
import com.sensirion.smartgadget.utils.view.SmartGadgetRequirementDialog;
import com.sensirion.smartgadget.view.device_management.ManageDeviceFragment;
import com.sensirion.smartgadget.view.device_management.ScanDeviceFragment;

import java.util.Locale;

import butterknife.BindBool;
import butterknife.BindColor;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnPageChange;
import butterknife.Optional;

public class MainActivity extends FragmentActivity implements View.OnTouchListener, RHTSensorListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final int NO_REMOTE_INSTRUCTION = 0;
    protected int mRemoteInstruction;

    // Attributes only used in mobile devices.
    @Nullable
    @BindView(R.id.view_pager)
    ViewPager mMobileViewPager;
    @Nullable
    @BindView(R.id.section_tabs)
    LinearLayout mMobileTabLayout;
    @BindBool(R.bool.is_tablet)
    boolean mIsTablet;

    //String resources
    @BindString(R.string.no)
    String NEGATION_STRING;
    @BindString(R.string.yes)
    String AFFIRMATION_STRING;
    @BindString(R.string.confirmation_to_close_application)
    String CONFIRMATION_TO_CLOSE_APPLICATION_STRING;
    @BindString(R.string.quit)
    String DO_YOU_WANT_TO_QUIT_STRING;
    @BindString(R.string.connected_device)
    String CONNECTED_DEVICE_STRING;
    @BindString(R.string.lost_connection_device)
    String LOST_DEVICE_CONNECTION;
    @BindString(R.string.inphone_rht_sensor)
    String INPHONE_RHT_SENSOR_STRING;

    //Color resources
    @BindColor(R.color.font_shadow)
    int FONT_SHADOW_COLOR;

    // Attributes used in tablet devices.
    @Nullable
    @BindView(R.id.drawer_layout)
    DrawerLayout mTabletLeftMenuDrawer;
    @Nullable
    @BindView(R.id.left_drawer)
    ListView mTabletLeftMenuListView;

    //Class attributes.
    private SectionManager mSectionsPagerAdapter;
    private Menu mOptionMenu;
    private int mPositionSelected = 0;
    private boolean mIsChildScreen = false;
    private boolean mUserPreferencesModified = false;
    @Nullable
    private Fragment mLastFragment = null;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        final Context appContext = getApplicationContext();
        ManagerInitializer.initializeApplicationManagers(appContext);
        setScreenOrientation();
        initFragmentNavigator();
        final Settings appSettings = Settings.getInstance();
        if (!RHTSensorFacade.getInstance().hasInternalRHTSensor() &&
                appSettings.isSmartGadgetRequirementDisplayed()) {
            (new SmartGadgetRequirementDialog(this)).show();
        }
    }

    private void setScreenOrientation() {
        if (mIsTablet) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT);
        }
    }

    private void initFragmentNavigator() {
        if (mIsTablet) {
            initFragmentNavigatorTablet();
        } else {
            initMobileActionBarWithTabs();
        }
    }

    private void initFragmentNavigatorTablet() {
        initTabletLeftBar();
        initActionBar();
        final ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.action_arrow);
        }
    }

    /**
     * ************************************************************************
     * ***************************** TABLET METHODS ***************************
     * ************************************************************************
     */
    private void initTabletLeftBar() {
        if (mTabletLeftMenuListView == null) {
            Log.e(TAG, "changeTabletSection -> mTabletLeftMenuListView is not initialized (HINT -> ButterKnife.bind(this))");
            return;
        }
        mSectionsPagerAdapter = new SectionManagerTablet(getSupportFragmentManager());
        final String[] mTabletDrawerListViewItems = new String[mSectionsPagerAdapter.getCount()];
        for (int i = 0; i < mTabletDrawerListViewItems.length; i++) {
            mTabletDrawerListViewItems[i] = mSectionsPagerAdapter.getPageTitle(this, i);
        }
        mTabletLeftMenuListView.setAdapter(new ArrayAdapter<>(this, R.layout.drawer_listview_item, mTabletDrawerListViewItems));
        mTabletLeftMenuListView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                changeTabletFragment(mSectionsPagerAdapter.getItem(position));
                changeTabletSection(position, true);
            }
        });
        changeTabletSection(0, false);
    }

    private void changeTabletSection(final int position, final boolean withTransition) {
        if (mTabletLeftMenuDrawer == null || mTabletLeftMenuListView == null) {
            Log.e(TAG, "toggleTabletMenu -> Tablet Views are not initialized (HINT -> ButterKnife.bind(this))");
            return;
        }
        if (position != mPositionSelected || mIsChildScreen || !withTransition) {
            cleanScreen();
            final Fragment fragment = mSectionsPagerAdapter.getItem(position);
            if (fragment == null) {
                Log.e(TAG, "changeTabletSection -> Error in creating fragment");
            } else {
                final FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                if (withTransition) {
                    fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
                }
                fragmentTransaction.replace(R.id.content_frame, fragment);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fragmentTransaction.commit();
                    }
                });
            }
            findViewById(R.id.content_frame).setOnTouchListener(MainActivity.this);
            mPositionSelected = position;
            mIsChildScreen = false;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTabletLeftMenuDrawer.closeDrawer(mTabletLeftMenuListView);
            }
        });
    }

    public void toggleTabletMenu() {
        if (!mIsTablet) {
            Log.e(TAG, "toggleTabletMenu -> The method can only be called from tablet devices");
            return;
        }
        if (mTabletLeftMenuDrawer == null || mTabletLeftMenuListView == null) {
            Log.e(TAG, "toggleTabletMenu -> Tablet Views are not initialized (HINT -> ButterKnife.bind(this))");
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mTabletLeftMenuDrawer.isDrawerOpen(mTabletLeftMenuListView)) {
                    mTabletLeftMenuDrawer.closeDrawer(mTabletLeftMenuListView);
                } else {
                    mTabletLeftMenuDrawer.openDrawer(mTabletLeftMenuListView);
                }
            }
        });
    }

    private void onBackPressedTablet() {
        if (mPositionSelected > 0) {
            changeTabletSection(0, true);
        } else {
            showClosingAdvice();
        }
    }

    /**
     * ***********************************************************************************
     * *************************     MOBILE PHONE METHODS     ****************************
     * ***********************************************************************************
     */

    private void initMobileActionBarWithTabs() {
        if (mMobileViewPager == null) {
            Log.e(TAG, "initMobileActionBarWithTabs -> The mobile View Pager is null. (HINT -> ButterKnife.bind(this)");
            return;
        }
        if (getActionBar() != null) {
            getActionBar().setDisplayShowHomeEnabled(false);
        }
        mSectionsPagerAdapter = new SectionManagerMobile(getSupportFragmentManager());
        mMobileViewPager.setAdapter(mSectionsPagerAdapter);
        mMobileViewPager.setOffscreenPageLimit(mSectionsPagerAdapter.getCount());
        initActionBar();
        addTabsToMobileActionBar();
    }

    @Optional
    @OnPageChange(R.id.view_pager)
    void onTabChanged(final int position) {
        if (mMobileViewPager == null) {
            Log.e(TAG, "initMobileActionBarWithTabs -> The mobile View Pager is null. (HINT -> ButterKnife.bind(this)");
            return;
        }
        if (mPositionSelected == position) {
            onMobileTabReselected(position);
            return;
        }
        if (mIsChildScreen || mUserPreferencesModified) {
            mMobileViewPager.setAdapter(new SectionManagerMobile(getSupportFragmentManager()));
            mIsChildScreen = false;
            mUserPreferencesModified = false;
            mMobileViewPager.setCurrentItem(position);
        }
        cleanScreen();
        updateMobileTabState(position);
    }

    private void addTabsToMobileActionBar() {
        final LinearLayout tabLayout = mMobileTabLayout;
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            tabLayout.addView(createMobileActionBarTab(tabLayout, i));
        }
        tabLayout.getChildAt(0).setBackground(getResources().getDrawable(R.drawable.section_tab_selected));
        tabLayout.refreshDrawableState();
    }

    private View createMobileActionBarTab(@Nullable final ViewGroup root, final int position) {
        final View view = getLayoutInflater().inflate(R.layout.section_tab, root, false);
        final Drawable icon = ((SectionManagerMobile) mSectionsPagerAdapter).getIcon(MainActivity.this, position);
        ((ImageView) view.findViewById(R.id.iconImage)).setImageDrawable(icon);
        ((TextView) view.findViewById(R.id.text)).setText(mSectionsPagerAdapter.getPageTitle(MainActivity.this, position));
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMobileTabSelected(position);
            }
        });
        return view;
    }

    public void onMobileTabSelected(final int position) {
        onMobileTabSelected(position, MainActivity.NO_REMOTE_INSTRUCTION);
    }

    public void onMobileTabSelected(final int position, final int remoteInstruction) {
        if (mMobileViewPager == null) {
            Log.e(TAG, "initMobileActionBarWithTabs -> The mobile View Pager is null. (HINT -> ButterKnife.bind(this)");
            return;
        }
        cleanScreen();
        final String pageTitle = mSectionsPagerAdapter.getPageTitle(getApplicationContext(), position).toUpperCase(Locale.getDefault());
        Log.d(TAG, String.format("onMobileTabSelected -> The tab %s was selected.", pageTitle));
        if (mIsChildScreen || mUserPreferencesModified) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mMobileViewPager.setAdapter(new SectionManagerMobile(getSupportFragmentManager()));
                }
            });
            mIsChildScreen = false;
            mUserPreferencesModified = false;
        }
        mLastFragment = mSectionsPagerAdapter.getItem(position);
        setRemoteInstruction(remoteInstruction);
        mMobileViewPager.setCurrentItem(position);

        updateMobileTabState(position);
    }

    private void onMobileTabReselected(final int position) {
        Log.d(TAG, String.format("onMobileTabReselected -> Tab %d was reselected", position));
        if (mIsChildScreen) {
            onMobileTabSelected(position);
        }
    }

    private void updateMobileTabState(final int newPosition) {
        final LinearLayout tabLayout = (LinearLayout) findViewById(R.id.section_tabs);
        final View oldChild = tabLayout.getChildAt(mPositionSelected);
        oldChild.setBackground(getResources().getDrawable(R.drawable.section_tab_selector));
        final View child = tabLayout.getChildAt(newPosition);
        child.setBackground(getResources().getDrawable(R.drawable.section_tab_selected));
        mPositionSelected = newPosition;
    }

    private void onBackPressedMobile() {
        if (mLastFragment instanceof ManageDeviceFragment) {
            changeFragment(new ScanDeviceFragment());
        } else if (mIsChildScreen) {
            onMobileTabSelected(SectionManagerMobile.POSITION_SETTINGS);
        } else if (mPositionSelected > 0) {
            onMobileTabSelected(SectionManagerMobile.POSITION_DASHBOARD);
        } else {
            showClosingAdvice();
        }
    }

    /**
     * ***********************************************************************************
     * *************************       FRAGMENT MANAGER       ****************************
     * ***********************************************************************************
     */

    public void changeFragment(@NonNull final Fragment destinationFragment) {
        if (mIsTablet) {
            changeTabletFragment(destinationFragment);
        } else {
            changeFragmentMobile(destinationFragment);
        }
        mIsChildScreen = true;
    }

    private void changeTabletFragment(@NonNull final Fragment destinationFragment) {
        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        final Integer position = SectionManagerTablet.getFragmentId(destinationFragment);
        if (position == null) {
            transaction.replace(R.id.content_frame, destinationFragment);
            transaction.commit();
            return;
        }
        changeTabletSection(position, true);
    }

    private void changeFragmentMobile(@NonNull final Fragment destinationFragment) {
        final FragmentManager fm = getSupportFragmentManager();
        final FragmentTransaction transaction = fm.beginTransaction();
        transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        cleanScreen();
        for (int i = 0; i < fm.getBackStackEntryCount(); i++) {
            fm.popBackStack();
        }
        if (mLastFragment != null) {
            transaction.remove(mLastFragment);
        }
        mLastFragment = destinationFragment;
        transaction.replace(R.id.smartgadget_preference_fragment, destinationFragment);
        transaction.commit();
    }

    /**
     * ***********************************************************************************
     * *************************         COMMON METHODS       ****************************
     * ***********************************************************************************
     */

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
        RHTSensorFacade.getInstance().registerListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");
        RHTSensorFacade.getInstance().unregisterListener(this);
        if (mIsTablet && mTabletLeftMenuDrawer != null && mTabletLeftMenuListView != null) {
            if (mTabletLeftMenuDrawer.isDrawerOpen(mTabletLeftMenuListView)) {
                toggleTabletMenu();
            }
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        if (isFinishing()) {
            DeviceNameDatabaseManager.getInstance().closeDatabaseConnection();
            RHTSensorFacade.getInstance().release(getApplicationContext());
        }
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                final Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                toggleTabletMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (mIsTablet) {
            onBackPressedTablet();
        } else {
            onBackPressedMobile();
        }
    }

    private void showClosingAdvice() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(DO_YOU_WANT_TO_QUIT_STRING);
        builder.setCancelable(true);
        builder.setMessage(CONFIRMATION_TO_CLOSE_APPLICATION_STRING);
        builder.setPositiveButton(AFFIRMATION_STRING, new DialogInterface.OnClickListener() {
            public void onClick(@NonNull final DialogInterface dialog, final int which) {
                dialog.cancel();
                finish();
            }
        });
        builder.setNegativeButton(NEGATION_STRING, new DialogInterface.OnClickListener() {
            public void onClick(@NonNull final DialogInterface dialog, final int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void cleanScreen() {
        clearMenu();
        hideKeyboard();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        mOptionMenu = menu;
        return true;
    }

    private void clearMenu() {
        if (mOptionMenu != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mOptionMenu.clear();
                }
            });
        }
    }

    private void hideKeyboard() {
        final InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        // check if no view has focus:
        final View view = this.getCurrentFocus();
        if (view != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            });
        }
    }

    private void initActionBar() {
        final SpannableString title = new SpannableString("B");

        final TypedValue actionbarTittleTextSize = new TypedValue();
        getResources().getValue(R.dimen.actionbar_title_size, actionbarTittleTextSize, true);
        title.setSpan(
                new ApplicationHeaderGenerator(this, "SensirionSimple.ttf",
                        actionbarTittleTextSize.getFloat(), FONT_SHADOW_COLOR
                ), 0, title.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        if (getActionBar() != null) {
            getActionBar().setTitle(title);
            getActionBar().setDisplayShowHomeEnabled(false);
        }
    }

    @Override
    public boolean onTouch(@NonNull final View view, @Nullable final MotionEvent motionEvent) {
        if (mIsTablet) {
            final RHTHumigadgetSensorManager rhtHumigadgetSensorManager = RHTHumigadgetSensorManager.getInstance();
            if (rhtHumigadgetSensorManager.bluetoothIsEnabled(this)) {
                toggleTabletMenu();
                RHTSensorFacade.getInstance().registerListener(this);
            } else {
                rhtHumigadgetSensorManager.requestEnableBluetooth(this);
            }
        }
        return view.performClick();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onGadgetConnectionChanged(@Nullable final String deviceAddress, final boolean deviceIsConnected) {
        if (getIntent() == null) {
            Log.e(TAG, "onGadgetConnectionChanged -> Can't found a valid intent.");
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final String toastText;
                if (deviceIsConnected) {
                    toastText = CONNECTED_DEVICE_STRING;
                } else {
                    toastText = LOST_DEVICE_CONNECTION;
                }
                final String deviceName;
                if (deviceAddress == null) {
                    deviceName = INPHONE_RHT_SENSOR_STRING;
                } else {
                    deviceName = DeviceNameDatabaseManager.getInstance().readDeviceName(deviceAddress);
                }
                Toast.makeText(MainActivity.this, String.format(toastText, deviceName), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onNewRHTSensorData(final float temperature,
                                   final float relativeHumidity,
                                   final String deviceAddress) {
        // Do nothing
    }

    public void setRemoteInstruction(int remoteInstruction) {
        mRemoteInstruction = remoteInstruction;
    }

    public int getRemoteInstruction() {
        return mRemoteInstruction;
    }
}
