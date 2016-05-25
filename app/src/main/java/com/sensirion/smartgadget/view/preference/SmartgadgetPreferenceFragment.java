package com.sensirion.smartgadget.view.preference;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.sensirion.libble.BleManager;
import com.sensirion.smartgadget.R;
import com.sensirion.smartgadget.peripheral.rht_sensor.RHTSensorFacade;
import com.sensirion.smartgadget.peripheral.rht_sensor.RHTSensorListener;
import com.sensirion.smartgadget.peripheral.rht_sensor.external.RHTHumigadgetSensorManager;
import com.sensirion.smartgadget.utils.Settings;
import com.sensirion.smartgadget.utils.view.ParentListFragment;
import com.sensirion.smartgadget.utils.view.SectionAdapter;
import com.sensirion.smartgadget.view.MainActivity;
import com.sensirion.smartgadget.view.device_management.ScanDeviceFragment;
import com.sensirion.smartgadget.view.glossary.GlossaryFragment;
import com.sensirion.smartgadget.view.preference.adapter.PreferenceAdapter;

import java.util.Calendar;

import butterknife.BindString;
import butterknife.ButterKnife;

import static android.content.pm.PackageManager.NameNotFoundException;

public class SmartgadgetPreferenceFragment extends ParentListFragment implements RHTSensorListener {

    // Class name
    @NonNull
    private static final String TAG = SmartgadgetPreferenceFragment.class.getSimpleName();

    // XML resources
    @BindString(R.string.app_name)
    String APP_NAME;
    @BindString(R.string.app_platform)
    String APP_PLATFORM;
    @BindString(R.string.txt_about_url)
    String APP_ABOUT_URL;
    @BindString(R.string.txt_about_char_copyright)
    String ABOUT_CHAR_COPYRIGHT;
    @BindString(R.string.typeface_condensed)
    String CONDENSED_TYPEFACE;
    @BindString(R.string.typeface_bold)
    String BOLD_TYPEFACE;
    @BindString(R.string.label_season)
    String SEASON_PREFERENCE_LABEL;
    @BindString(R.string.label_smart_gadgets)
    String DEVICES_PREFERENCE_LABEL;
    @BindString(R.string.key_pref_season)
    String KEY_PREFERENCE_SEASON;
    @BindString(R.string.label_glossary)
    String GLOSSARY_PREFERENCE_LABEL;
    @BindString(R.string.about_sensirion_ag)
    String ABOUT_SENSIRION_AG;
    @BindString(R.string.header_connections)
    String CONNECTION_HEADER;
    @BindString(R.string.key_pref_temp_unit)
    String TEMPERATURE_PREFERENCE_KEY;
    @BindString(R.string.label_temperature_unit)
    String TEMPERATURE_PREFERENCE_LABEL;
    @BindString(R.string.header_user_prefs)
    String USER_PREFERENCES_HEADER;
    @BindString(R.string.label_about)
    String ABOUT_PREFERENCE_LABEL;
    @BindString(R.string.header_app_information)
    String APP_INFORMATION_HEADER;

    // Layout Adapters
    @Nullable
    private SectionAdapter mSectionAdapter;
    @Nullable
    private PreferenceAdapter mConnectionsAdapter;
    @Nullable
    private PreferenceAdapter mUserPreferencesAdapter;

    // Last printed Toast
    @Nullable
    private Toast mLastAboutToast = null;

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() -> Refreshing number of devices.");
        if (mConnectionsAdapter == null) {
            initPreferencesList();
        }
        mConnectionsAdapter.clear();
        refreshPreferenceAdapter();
        refreshUserPreferenceAdapter();
        setListAdapter(mSectionAdapter);
        RHTSensorFacade.getInstance().registerListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        RHTSensorFacade.getInstance().unregisterListener(this);
    }

    @Override
    @NonNull
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_smartgadget_list, container, false);
        ButterKnife.bind(this, view);
        initPreferencesList();
        return view;
    }

    private void initPreferencesList() {
        mSectionAdapter = new SectionAdapter() {
            @NonNull
            @Override
            protected View getHeaderView(@NonNull final String caption,
                                         final int itemIndex,
                                         @Nullable final View convertView,
                                         @Nullable final ViewGroup parent) {
                TextView listItemHeader = (TextView) convertView;
                if (listItemHeader == null) {
                    final AssetManager assets = getContext().getAssets();
                    final Typeface typefaceBold = Typeface.createFromAsset(assets, BOLD_TYPEFACE);
                    listItemHeader = (TextView) View.inflate(getParent(), R.layout.listitem_scan_header, null);
                    listItemHeader.setTypeface(typefaceBold);
                }
                listItemHeader.setText(caption);
                return listItemHeader;
            }
        };
        initConnectionPreferenceAdapter();
        initUserPreferenceAdapter();
        if (mConnectionsAdapter == null) {
            Log.e(TAG, "initPreferencesList -> Connection adapter can't be null");
            return;
        }
        if (mUserPreferencesAdapter == null) {
            Log.e(TAG, "initPreferencesList -> User preferences adapter can't be null");
            return;
        }
        mSectionAdapter.addSectionToAdapter(CONNECTION_HEADER, mConnectionsAdapter);
        mSectionAdapter.addSectionToAdapter(USER_PREFERENCES_HEADER, mUserPreferencesAdapter);
        mSectionAdapter.addSectionToAdapter(APP_INFORMATION_HEADER, getAppInformationAdapter());
    }

    /**
     * ************************************************************************
     * *********************** CONNECTION PREFERENCES *************************
     * ************************************************************************
     */

    private void initConnectionPreferenceAdapter() {
        final AssetManager assets = getContext().getAssets();
        final Typeface typefaceCondensed = Typeface.createFromAsset(assets, CONDENSED_TYPEFACE);
        mConnectionsAdapter = new PreferenceAdapter(typefaceCondensed);
        refreshPreferenceAdapter();
    }

    private void refreshPreferenceAdapter() {
        if (mConnectionsAdapter == null) {
            Log.e(TAG, "refreshPreferenceAdapter -> Connection adapter can't be null");
            return;
        }
        final String title = getConnectedDevicesTitle();
        final View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (RHTHumigadgetSensorManager.getInstance().bluetoothIsEnabled()) {
                    getListView().setVisibility(View.GONE);
                    final MainActivity mainActivity = (MainActivity) getParent();
                    if (mainActivity == null) {
                        Log.e(TAG, "refreshPreferenceAdapter.onClick -> getParent() returned null");
                    } else {
                        mainActivity.changeFragment(new ScanDeviceFragment());
                    }
                }
                Log.w(TAG, "initConnectionPreferences -> Bluetooth has to be active in" +
                        " order to scan for new devices.");
                RHTHumigadgetSensorManager.getInstance().requestEnableBluetooth(getParent());
            }
        };
        mConnectionsAdapter.addPreference(title, null, clickListener);
    }

    @NonNull
    private String getConnectedDevicesTitle() {
        final int numberConnectedGadgets = BleManager.getInstance().getConnectedBleDeviceCount();
        if (numberConnectedGadgets == 0) {
            return DEVICES_PREFERENCE_LABEL;
        }
        return String.format("%s (%d)", DEVICES_PREFERENCE_LABEL, numberConnectedGadgets);
    }


    /**
     * ************************************************************************
     * *************************** USER PREFERENCES ***************************
     * ************************************************************************
     */
    private void initUserPreferenceAdapter() {
        final AssetManager assets = getContext().getAssets();
        final Typeface typefaceCondensed = Typeface.createFromAsset(assets, CONDENSED_TYPEFACE);
        mUserPreferencesAdapter = new PreferenceAdapter(typefaceCondensed);
        refreshUserPreferenceAdapter();
    }

    private void refreshUserPreferenceAdapter() {
        if (mUserPreferencesAdapter == null) {
            Log.e(TAG, "refreshUserPreferenceAdapter -> mUserPreferenceAdapter can't be null");
            return;
        }
        mUserPreferencesAdapter.clear();
        addTemperatureUnitPreferenceAdapter();
        addSeasonPreferenceAdapter();
    }

    @SuppressLint("CommitPrefEdits")
    private void addTemperatureUnitPreferenceAdapter() {
        if (mUserPreferencesAdapter == null) {
            Log.e(TAG, "addTemperatureUnitPreferenceAdapter -> mUserPreferenceAdapter can't be null");
            return;
        }
        final Context appContext = getContext().getApplicationContext();
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(appContext);

        String summary = prefs.getString(TEMPERATURE_PREFERENCE_KEY, Settings.UNKNOWN_VALUE);
        if (summary.equals(Settings.UNKNOWN_VALUE)) {
            Log.d(TAG, "addTemperaturePreferenceAdapter -> Temperature settings is not known ");
            summary = getResources().getTextArray(R.array.array_temp_unit)[0].toString();
            prefs.edit().putString(TEMPERATURE_PREFERENCE_KEY, summary).commit();
        }

        final View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(@NonNull final View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getParent());
                builder.setCancelable(true)
                        .setTitle(R.string.title_button_choice)
                        .setItems(R.array.array_temp_unit, new DialogInterface.OnClickListener() {
                            public void onClick(@NonNull final DialogInterface dialog,
                                                final int which) {
                                final String newSummary =
                                        getResources().getTextArray(R.array.array_temp_unit)[which].toString();
                                Log.d(TAG, String.format(
                                        "addTemperaturePreferenceAdapter -> Selected temperature unit %s.",
                                        newSummary));
                                final TextView summaryTextView = (TextView) v.findViewById(R.id.preference_summary);
                                prefs.edit().putString(TEMPERATURE_PREFERENCE_KEY, newSummary).commit();
                                summaryTextView.setText(newSummary);
                            }
                        });
                final AlertDialog dialog = builder.create();
                dialog.show();
            }
        };
        mUserPreferencesAdapter.addPreference(TEMPERATURE_PREFERENCE_LABEL, summary, clickListener);
    }

    @SuppressLint("CommitPrefEdits")
    private void addSeasonPreferenceAdapter() {
        if (mUserPreferencesAdapter == null) {
            Log.e(TAG, "addSeasonPreferenceAdapter -> mUserPreferenceAdapter can't be null");
            return;
        }
        final Context appContext = getContext().getApplicationContext();
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(appContext);

        String summary = prefs.getString(KEY_PREFERENCE_SEASON, Settings.UNKNOWN_VALUE);
        if (summary.equals(Settings.UNKNOWN_VALUE)) {
            summary = getResources().getTextArray(R.array.array_season)[0].toString();
            prefs.edit().putString(KEY_PREFERENCE_SEASON, summary).commit();
        }

        final View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(@NonNull final View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getParent());
                builder.setCancelable(true)
                        .setTitle(R.string.title_button_choice)
                        .setItems(R.array.array_season, new DialogInterface.OnClickListener() {
                            public void onClick(@NonNull final DialogInterface dialog,
                                                final int which) {
                                final String newSummary =
                                        getResources().
                                                getTextArray(R.array.array_season)[which].
                                                toString();
                                prefs.edit().putString(KEY_PREFERENCE_SEASON, newSummary).commit();
                                final TextView summaryView = ((TextView) v.findViewById(R.id.preference_summary));
                                summaryView.setText(newSummary);
                            }
                        });
                final AlertDialog dialog = builder.create();
                dialog.show();
            }
        };
        mUserPreferencesAdapter.addPreference(SEASON_PREFERENCE_LABEL, summary, clickListener);
    }

    /**
     * ************************************************************************
     * ***************************  APP INFORMATION ***************************
     * ************************************************************************
     */
    @NonNull
    private PreferenceAdapter getAppInformationAdapter() {
        final AssetManager assets = getContext().getAssets();
        final Typeface typefaceCondensed = Typeface.createFromAsset(assets, CONDENSED_TYPEFACE);
        final PreferenceAdapter appInformationAdapter = new PreferenceAdapter(typefaceCondensed);
        //  addGlossaryAdapter(appInformationAdapter);
        addShowAboutAdapter(appInformationAdapter);
        return appInformationAdapter;
    }

    private void addGlossaryAdapter(@NonNull final PreferenceAdapter adapter) {
        final View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getListView().setVisibility(View.GONE);
                final MainActivity mainActivity = (MainActivity) getParent();
                if (mainActivity == null) {
                    Log.e(TAG, "addGlossaryAdapter -> Cannot obtain the MainActivity.");
                } else {
                    mainActivity.changeFragment(new GlossaryFragment());
                }
            }
        };
        adapter.addPreference(GLOSSARY_PREFERENCE_LABEL, null, clickListener);
    }

    private void addShowAboutAdapter(@NonNull final PreferenceAdapter adapter) {
        final View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(@NonNull final View v) {
                showAboutText();
            }
        };
        adapter.addPreference(ABOUT_PREFERENCE_LABEL, null, clickListener);
    }


    private void showAboutText() {
        String versionName = null;
        final PackageManager packageManager = getContext().getPackageManager();
        final String packageName = getContext().getPackageName();
        try {
            versionName = packageManager.getPackageInfo(packageName, 0).versionName;
        } catch (final NameNotFoundException e) {
            Log.e(TAG, "showAboutText -> The following error was produced " +
                    "when obtaining the version name -> ", e);
        }

        final StringBuilder aboutText = new StringBuilder();

        final int deviceYear = Calendar.getInstance().get(Calendar.YEAR);
        aboutText.append(APP_NAME)
                .append(" ")
                .append(APP_PLATFORM)
                .append(" ")
                .append(versionName)
                .append(System.getProperty("line.separator"))
                .append(APP_ABOUT_URL)
                .append(System.getProperty("line.separator"))
                .append(ABOUT_CHAR_COPYRIGHT)
                .append(" ")
                .append((deviceYear <= 2015 ? 2015 : deviceYear))
                .append(" ")
                .append(ABOUT_SENSIRION_AG);

        synchronized (this) {
            if (mLastAboutToast != null) {
                mLastAboutToast.cancel();
                mLastAboutToast = null;
            }
            mLastAboutToast = Toast.makeText(getParent(), aboutText.toString(), Toast.LENGTH_LONG);
            mLastAboutToast.show();
        }
    }

    @Override
    public void onNewRHTSensorData(float temperature, float relativeHumidity, @Nullable String deviceAddress) {
        // do nothing
    }

    @Override
    public void onGadgetConnectionChanged(@NonNull String deviceAddress, boolean deviceIsConnected) {
        refreshPreferenceAdapter();
    }
}