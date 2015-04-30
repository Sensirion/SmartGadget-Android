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

import com.sensirion.libble.BleManager;
import com.sensirion.smartgadget.R;
import com.sensirion.smartgadget.peripheral.rht_sensor.RHTSensorFacade;
import com.sensirion.smartgadget.peripheral.rht_sensor.RHTSensorListener;
import com.sensirion.smartgadget.peripheral.rht_sensor.external.RHTHumigadgetSensorManager;
import com.sensirion.smartgadget.persistence.device_name_database.DeviceNameDatabaseManager;
import com.sensirion.smartgadget.utils.ManagerInitializer;
import com.sensirion.smartgadget.utils.section_manager.SectionManager;
import com.sensirion.smartgadget.utils.section_manager.SectionManagerMobile;
import com.sensirion.smartgadget.utils.section_manager.SectionManagerTablet;
import com.sensirion.smartgadget.utils.view.ApplicationHeaderGenerator;

import java.util.Locale;

public class MainActivity extends FragmentActivity implements View.OnTouchListener, RHTSensorListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    // Attributes used in tablet devices.
    private DrawerLayout mTabletLeftMenuDrawer;
    private ListView mTabletLeftMenuListView;

    // Attributes only used in non-tablet devices.
    private ViewPager mMobileViewPager;

    //Class attributes.
    private SectionManager mSectionsPagerAdapter;
    private Menu mOptionMenu;
    private int mPositionSelected = 0;
    private boolean mIsTablet;
    private boolean mIsChildScreen = false;
    private boolean mUserPreferencesModified = false;
    @Nullable
    private Fragment mLastFragment = null;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ManagerInitializer.initializeApplicationManagers(getApplicationContext());
        setScreenOrientation();
        setContentView(R.layout.activity_main);
        initFragmentNavigator();
    }

    private void setScreenOrientation() {
        mIsTablet = getResources().getBoolean(R.bool.is_tablet);
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
        mSectionsPagerAdapter = new SectionManagerTablet(getSupportFragmentManager());

        final String[] mTabletDrawerListViewItems = new String[mSectionsPagerAdapter.getCount()];
        for (int i = 0; i < mTabletDrawerListViewItems.length; i++) {
            mTabletDrawerListViewItems[i] = mSectionsPagerAdapter.getPageTitle(this, i);
        }
        mTabletLeftMenuDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mTabletLeftMenuListView = (ListView) findViewById(R.id.left_drawer);
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
        RHTHumigadgetSensorManager.getInstance().updateConnectedDeviceList();
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
        if (mIsTablet) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mTabletLeftMenuDrawer.isDrawerOpen(mTabletLeftMenuListView)) {
                        mTabletLeftMenuDrawer.closeDrawer(mTabletLeftMenuListView);
                        Log.i(TAG, "toggleTabletMenu -> Closing tablet left menu.");
                    } else {
                        mTabletLeftMenuDrawer.openDrawer(mTabletLeftMenuListView);
                        Log.i(TAG, "toggleTabletMenu -> Opening tablet left menu");
                    }
                }
            });
        }
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
        if (getActionBar() != null) {
            getActionBar().setDisplayShowHomeEnabled(false);
        }
        mSectionsPagerAdapter = new SectionManagerMobile(getSupportFragmentManager());
        mMobileViewPager = (ViewPager) findViewById(R.id.view_pager);
        mMobileViewPager.setAdapter(mSectionsPagerAdapter);
        mMobileViewPager.setOffscreenPageLimit(mSectionsPagerAdapter.getCount());
        mMobileViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(final int position) {
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
        });
        initActionBar();
        addTabsToMobileActionBar();
    }

    private void addTabsToMobileActionBar() {
        final LinearLayout tabLayout = (LinearLayout) findViewById(R.id.section_tabs);
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

    private void onMobileTabSelected(final int position) {
        cleanScreen();
        RHTHumigadgetSensorManager.getInstance().requestEnableBluetooth(MainActivity.this);
        final String pageTitle = mSectionsPagerAdapter.getPageTitle(getApplicationContext(), position).toUpperCase(Locale.getDefault());
        Log.i(TAG, String.format("onMobileTabSelected -> The tab %s was selected.", pageTitle));
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
        mMobileViewPager.setCurrentItem(position);
        mLastFragment = mSectionsPagerAdapter.getItem(position);
        updateMobileTabState(position);
        RHTHumigadgetSensorManager.getInstance().updateConnectedDeviceList();
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
        if (mIsChildScreen) {
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
        Log.i(TAG, String.format("changeFragment -> Trying to change fragment %s for %s", mLastFragment, destinationFragment));
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
        Log.i(TAG, "onResume()");
        RHTSensorFacade.getInstance().registerListener(this);
        RHTHumigadgetSensorManager bleManager = RHTHumigadgetSensorManager.getInstance();
        bleManager.updateConnectedDeviceList();
        BleManager.getInstance().setAllNotificationsEnabled(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "onPause()");
        RHTSensorFacade.getInstance().unregisterListener(this);
        if (mIsTablet) {
            if (mTabletLeftMenuDrawer.isDrawerOpen(mTabletLeftMenuListView)) {
                toggleTabletMenu();
            }
        }
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy()");
        if (isFinishing()) {
            Log.w(TAG, "onDestroy() --> isFinishing()");
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

        builder.setTitle(getResources().getString(R.string.quit));
        builder.setCancelable(true);
        builder.setMessage(getResources().getString(R.string.confirmation_to_close_application));
        builder.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(@NonNull final DialogInterface dialog, final int which) {
                dialog.cancel();
                finish();
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
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
                        actionbarTittleTextSize.getFloat(), getResources().getColor(
                        R.color.font_shadow)
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
            final RHTHumigadgetSensorManager bleManager = RHTHumigadgetSensorManager.getInstance();
            if (bleManager.bluetoothIsEnabled()) {
                toggleTabletMenu();
                RHTSensorFacade.getInstance().registerListener(this);
            } else {
                bleManager.requestEnableBluetooth(this);
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
            Log.w(TAG, "onGadgetConnectionChanged -> Can't found a valid intent.");
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final String toastText;
                if (deviceIsConnected) {
                    toastText = getResources().getString(R.string.connected_device);
                } else {
                    toastText = getResources().getString(R.string.lost_connection_device);
                }
                final String deviceName;
                if (deviceAddress == null) {
                    deviceName = getString(R.string.inphone_rht_sensor);
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
    public void onNewRHTSensorData(final float temperature, final float relativeHumidity, final String deviceAddress) {
    }
}