package com.sensirion.smartgadget.view.preference;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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
import com.sensirion.smartgadget.peripheral.rht_sensor.external.RHTHumigadgetSensorManager;
import com.sensirion.smartgadget.utils.Settings;
import com.sensirion.smartgadget.utils.view.ParentListFragment;
import com.sensirion.smartgadget.utils.view.SectionAdapter;
import com.sensirion.smartgadget.view.MainActivity;
import com.sensirion.smartgadget.view.device_management.ScanDeviceFragment;
import com.sensirion.smartgadget.view.glossary.GlossaryFragment;
import com.sensirion.smartgadget.view.preference.adapter.PreferenceAdapter;

import java.util.Calendar;

import static android.content.pm.PackageManager.NameNotFoundException;

public class SmartgadgetPreferenceFragment extends ParentListFragment {

    private static final String TAG = SmartgadgetPreferenceFragment.class.getSimpleName();

    @Nullable
    private Toast mLastAboutToast = null;

    @Nullable
    private SectionAdapter mSectionAdapter;
    private PreferenceAdapter mConnectionsAdapter;
    private PreferenceAdapter mUserPreferencesAdapter;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initPreferencesList();
        setListAdapter(mSectionAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() -> Refreshing number of devices.");
        mConnectionsAdapter.clear();
        refreshPreferenceAdapter();
        refreshUserPreferenceAdapter();
        setListAdapter(mSectionAdapter);
    }

    @Override
    @NonNull
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_smartgadget_list, container, false);
    }

    private void initPreferencesList() {
        mSectionAdapter = new SectionAdapter() {
            @Nullable
            @Override
            protected View getHeaderView(@NonNull final String caption, final int itemIndex, @Nullable final View convertView, @Nullable final ViewGroup parent) {
                TextView headerTextView = (TextView) convertView;
                if (convertView == null) {
                    final Typeface typefaceBold = Typeface.createFromAsset(getParent().getAssets(), "HelveticaNeueLTStd-Bd.otf");
                    headerTextView = (TextView) View.inflate(getParent(), R.layout.listitem_scan_header, null);
                    headerTextView.setTypeface(typefaceBold);
                }
                headerTextView.setText(caption);
                return headerTextView;
            }
        };
        initConnectionPreferenceAdapter();
        initUserPreferenceAdapter();
        mSectionAdapter.addSectionToAdapter(getString(R.string.header_connections), mConnectionsAdapter);
        mSectionAdapter.addSectionToAdapter(getString(R.string.header_user_prefs), mUserPreferencesAdapter);
        mSectionAdapter.addSectionToAdapter(getString(R.string.header_app_information), obtainAppInformationAdapter());
    }

    /**
     * ************************************************************************
     * *********************** CONNECTION PREFERENCES *************************
     * ************************************************************************
     */

    private void initConnectionPreferenceAdapter() {
        mConnectionsAdapter = new PreferenceAdapter(getParent());
        refreshPreferenceAdapter();
    }

    private void refreshPreferenceAdapter() {
        final String title = obtainConnectedDevicesTitle();
        final View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (RHTHumigadgetSensorManager.getInstance().bluetoothIsEnabled()) {
                    getListView().setVisibility(View.GONE);
                    ((MainActivity) getParent()).changeFragment(new ScanDeviceFragment());
                }
                Log.w(TAG, "initConnectionPreferences -> Bluetooth has to be active in order to scan for new devices.");
                RHTHumigadgetSensorManager.getInstance().requestEnableBluetooth(getParent());
            }
        };
        mConnectionsAdapter.addPreference(title, null, clickListener);
    }

    @NonNull
    private String obtainConnectedDevicesTitle() {
        final int numberConnectedGadgets = BleManager.getInstance().getConnectedBleDeviceCount();
        if (numberConnectedGadgets == 0) {
            return getString(R.string.label_smart_gadgets);
        }
        return String.format("%s (%d)", getString(R.string.label_smart_gadgets), numberConnectedGadgets);
    }


    /**
     * ************************************************************************
     * *************************** USER PREFERENCES ***************************
     * ************************************************************************
     */
    private void initUserPreferenceAdapter() {
        mUserPreferencesAdapter = new PreferenceAdapter(getParent());
        refreshUserPreferenceAdapter();
    }

    private void refreshUserPreferenceAdapter() {
        mUserPreferencesAdapter.clear();
        addTemperatureUnitPreferenceAdapter();
        addSeasonPreferenceAdapter();
    }

    @SuppressLint("CommitPrefEdits")
    private void addTemperatureUnitPreferenceAdapter() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getParent().getApplicationContext());

        final String title = getResources().getString(R.string.label_temperature_unit);

        String summary = prefs.getString(getResources().getString(R.string.key_pref_temp_unit), Settings.UNKNOWN_VALUE);
        if (summary.equals(Settings.UNKNOWN_VALUE)) {
            Log.d(TAG, "addTemperaturePreferenceAdapter -> Temperature settings is not known ");
            summary = getResources().getTextArray(R.array.array_temp_unit)[0].toString();
            prefs.edit().putString(getResources().getString(R.string.key_pref_temp_unit), summary).commit();
        }

        final View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(@NonNull final View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getParent());
                builder.setCancelable(true)
                        .setTitle(R.string.title_button_choice)
                        .setItems(R.array.array_temp_unit, new DialogInterface.OnClickListener() {
                            public void onClick(@NonNull final DialogInterface dialog, final int which) {
                                final String newSummary = getResources().getTextArray(R.array.array_temp_unit)[which].toString();
                                Log.d(TAG, String.format("addTemperaturePreferenceAdapter -> Selected temperature unit %s.", newSummary));
                                final TextView summaryTextView = (TextView) v.findViewById(R.id.preference_summary);
                                prefs.edit().putString(getResources().getString(R.string.key_pref_temp_unit), newSummary).commit();
                                summaryTextView.setText(newSummary);
                            }
                        });
                final AlertDialog dialog = builder.create();
                dialog.show();
            }
        };
        mUserPreferencesAdapter.addPreference(title, summary, clickListener);
    }

    @SuppressLint("CommitPrefEdits")
    private void addSeasonPreferenceAdapter() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getParent().getApplicationContext());

        final String title = getResources().getString(R.string.label_season);

        String summary = prefs.getString(getResources().getString(R.string.key_pref_season), Settings.UNKNOWN_VALUE);
        if (summary.equals(Settings.UNKNOWN_VALUE)) {
            summary = getResources().getTextArray(R.array.array_season)[0].toString();
            prefs.edit().putString(getResources().getString(R.string.key_pref_season), summary).commit();
        }

        final View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(@NonNull final View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getParent());
                builder.setCancelable(true)
                        .setTitle(R.string.title_button_choice)
                        .setItems(R.array.array_season, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                final String newSummary = getResources().getTextArray(R.array.array_season)[which].toString();
                                final TextView summaryTextView = (TextView) v.findViewById(R.id.preference_summary);
                                prefs.edit().putString(getResources().getString(R.string.key_pref_season), newSummary).commit();
                                summaryTextView.setText(newSummary);
                            }
                        });
                final AlertDialog dialog = builder.create();
                dialog.show();
            }
        };
        mUserPreferencesAdapter.addPreference(title, summary, clickListener);
    }


    /**
     * ************************************************************************
     * ***************************  APP INFORMATION ***************************
     * ************************************************************************
     */
    @NonNull
    private PreferenceAdapter obtainAppInformationAdapter() {
        final PreferenceAdapter appInformationAdapter = new PreferenceAdapter(getParent());
        //  addGlossaryAdapter(appInformationAdapter);
        addShowAboutAdapter(appInformationAdapter);
        return appInformationAdapter;
    }

    private void addGlossaryAdapter(@NonNull final PreferenceAdapter adapter) {
        final String title = getResources().getString(R.string.label_glossary);

        final View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getListView().setVisibility(View.GONE);
                ((MainActivity) getParent()).changeFragment(new GlossaryFragment());
            }
        };
        adapter.addPreference(title, null, clickListener);
    }

    private void addShowAboutAdapter(@NonNull final PreferenceAdapter adapter) {
        final String title = getResources().getString(R.string.label_about);

        final View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(@NonNull final View v) {
                showAboutText();
            }
        };
        adapter.addPreference(title, null, clickListener);
    }

    private void showAboutText() {
        String versionName = null;
        try {
            versionName = getParent().getPackageManager().getPackageInfo(getParent().getPackageName(), 0).versionName;
        } catch (@NonNull final NameNotFoundException e) {
            Log.e(TAG, "showAboutText -> The following error was produced when obtaining the version name -> ", e);
        }

        final StringBuilder aboutText = new StringBuilder();

        final int deviceYear = Calendar.getInstance().get(Calendar.YEAR);
        aboutText.append(getString(R.string.app_name))
                .append(" ")
                .append(getString(R.string.app_platform))
                .append(" ")
                .append(versionName)
                .append(System.getProperty("line.separator"))
                .append(getString(R.string.txt_about_url))
                .append(System.getProperty("line.separator"))
                .append(getString(R.string.txt_about_char_copyright))
                .append(" ")
                .append((deviceYear <= 2015 ? 2015 : deviceYear))
                .append(" ")
                .append(getString(R.string.txt_about_sensirionag));

        synchronized (this) {
            if (mLastAboutToast != null) {
                mLastAboutToast.cancel();
                mLastAboutToast = null;
            }
            mLastAboutToast = Toast.makeText(getParent(), aboutText.toString(), Toast.LENGTH_LONG);
            mLastAboutToast.show();
        }
    }
}