package com.pyler.xinstaller;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.io.File;

public class Preferences extends Activity {
    public static Activity activity;
    public static Context context;
    public static SharedPreferences prefs;
    public static AppLocaleManager appLocaleManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();

        if (!Common.MARSHMALLOW_NEWER) {
            startActivity(new Intent(this, com.pyler.xinstaller.legacy.Preferences.class));
            finish();
        }

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new Settings()).commit();
    }

    @SuppressWarnings("deprecation")
    public static class Settings extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            activity = getActivity();
            getPreferenceManager()
                    .setSharedPreferencesMode(MODE_WORLD_READABLE);
            addPreferencesFromResource(R.xml.preferences);

            appLocaleManager = new AppLocaleManager(context);
            appLocaleManager.initialize();

            prefs = PreferenceManager.getDefaultSharedPreferences(context);

            Preference appVersion = findPreference(Common.PREF_APP_VERSION);
            PackageManager pm = context.getPackageManager();
            try {
                String versionName = pm.getPackageInfo(
                        context.getPackageName(), 0).versionName;
                appVersion.setSummary(versionName);
            } catch (PackageManager.NameNotFoundException e) {
            }

            boolean isExpertModeEnabled = prefs.getBoolean(
                    Common.PREF_ENABLE_EXPERT_MODE, false);

            final Resources resources = getResources();
            PreferenceCategory about = (PreferenceCategory) findPreference(Common.PREF_APP_ABOUT);

            Preference appTranslator = findPreference(Common.PREF_APP_TRANSLATOR);
            String translator = resources.getString(R.string.app_translator);
            if (translator.isEmpty()) {
                about.removePreference(appTranslator);
            }

            appTranslator
                    .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            String translatorUrl = resources
                                    .getString(R.string.app_translator_url);
                            if (!translatorUrl.isEmpty()) {
                                Intent openUrl = new Intent(Intent.ACTION_VIEW);
                                openUrl.setData(Uri.parse(translatorUrl));
                                openUrl.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(openUrl);
                            }
                            return true;
                        }
                    });
            Preference enableExpertMode = findPreference(Common.PREF_ENABLE_EXPERT_MODE);
            enableExpertMode
                    .setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                        @Override
                        public boolean onPreferenceChange(
                                Preference preference, Object newValue) {
                            activity.recreate();
                            return true;
                        }
                    });

            Preference appLocale = findPreference(Common.PREF_APP_LOCALE);
            appLocale
                    .setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                        @Override
                        public boolean onPreferenceChange(
                                Preference preference, Object newValue) {
                            activity.recreate();
                            return true;
                        }
                    });

            Preference enableAppIcon = findPreference(Common.PREF_ENABLE_APP_ICON);
            enableAppIcon.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(
                        Preference preference, Object newValue) {
                    PackageManager packageManager = context.getPackageManager();
                    int state = (Boolean) newValue ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                            : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
                    String settings = Common.PACKAGE_NAME + ".Settings";
                    ComponentName alias = new ComponentName(context, settings);
                    packageManager.setComponentEnabledSetting(alias, state,
                            PackageManager.DONT_KILL_APP);
                    return true;
                }
            });

            Preference resetDeviceProperties = findPreference(Common.PREF_RESET_DEVICE_PROPERTIES);
            resetDeviceProperties
                    .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            SharedPreferences.Editor prefsEditor = prefs.edit();
                            for (String[] property : Common.DEVICE_PROPERTIES) {
                                prefsEditor.remove(property[0]);
                                EditTextPreference devicePropertyPreference = (EditTextPreference) findPreference(property[0]);
                                devicePropertyPreference.setText(property[1]);
                            }
                            prefsEditor.commit();
                            Toast.makeText(
                                    context,
                                    resources
                                            .getString(R.string.preferences_reset),
                                    Toast.LENGTH_LONG).show();
                            return true;
                        }
                    });

            for (String[] property : Common.DEVICE_PROPERTIES) {
                EditTextPreference devicePropertyPreference = (EditTextPreference) findPreference(property[0]);
                String propertyValue = prefs.getString(property[0], null);
                if (propertyValue == null) {
                    devicePropertyPreference.setText(property[1]);
                }
            }
        }

        @Override
        public void onPause() {
            super.onPause();

            // Set preferences file permissions to be world readable
            File prefsDir = new File(getActivity().getApplicationInfo().dataDir, "shared_prefs");
            File prefsFile = new File(prefsDir, getPreferenceManager().getSharedPreferencesName() + ".xml");
            if (prefsFile.exists()) {
                prefsFile.setReadable(true, false);
            }
        }
    }
}