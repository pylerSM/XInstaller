package com.pyler.xinstaller;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class Preferences extends Activity {
	public static Context context;
	public static Activity activity;
	public static Resources resources;
	public static SharedPreferences prefs;
	public static AppLocaleManager appLocaleManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = getApplicationContext();
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new Settings()).commit();
	}

	public static class Settings extends PreferenceFragment {

		@SuppressWarnings("deprecation")
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			activity = getActivity();
			resources = context.getResources();
			getPreferenceManager().setSharedPreferencesMode(
					Context.MODE_WORLD_READABLE);
			addPreferencesFromResource(R.xml.preferences);

			prefs = PreferenceManager.getDefaultSharedPreferences(context);

			appLocaleManager = new AppLocaleManager(context);
			String currentAppLocale = prefs.getString(
					AppLocaleManager.PREF_APP_LOCALE, AppLocaleManager.SYSTEM);
			appLocaleManager.setLocale(currentAppLocale);
			appLocaleManager.inicialize();

			Preference appVersion = findPreference("app_version");
			PackageManager pm = context.getPackageManager();
			try {
				String versionName = pm.getPackageInfo(
						context.getPackageName(), 0).versionName;
				appVersion.setSummary(versionName);
			} catch (NameNotFoundException e) {
			}

			boolean isExpertModeEnabled = prefs.getBoolean(
					Common.PREF_ENABLE_EXPERT_MODE, false);

			PreferenceCategory installationsEnable = (PreferenceCategory) findPreference("installations_enable");
			PreferenceCategory miscEnable = (PreferenceCategory) findPreference("misc_enable");
			PreferenceCategory miscDisable = (PreferenceCategory) findPreference("misc_disable");
			PreferenceCategory about = (PreferenceCategory) findPreference("about");

			Preference installUnsignedApps = findPreference(Common.PREF_ENABLE_INSTALL_UNSIGNED_APP);
			Preference installOnExternal = findPreference(Common.PREF_ENABLE_INSTALL_EXTERNAL_STORAGE);
			Preference debugApps = findPreference(Common.PREF_ENABLE_DEBUG_APP);
			Preference checkPermissions = findPreference(Common.PREF_DISABLE_CHECK_PERMISSION);
			Preference verifyJar = findPreference(Common.PREF_DISABLE_VERIFY_JAR);
			Preference verifySignature = findPreference(Common.PREF_DISABLE_VERIFY_SIGNATURE);

			Preference appTranslator = findPreference("app_translator");
			String translator = resources.getString(R.string.app_translator);
			if (translator.isEmpty()) {
				about.removePreference(appTranslator);
			}
			if (!isExpertModeEnabled) {
				installationsEnable.removePreference(installUnsignedApps);
				installationsEnable.removePreference(installOnExternal);
				miscEnable.removePreference(debugApps);
				miscDisable.removePreference(checkPermissions);
				miscDisable.removePreference(verifyJar);
				miscDisable.removePreference(verifySignature);
			}

			Preference enableAppIcon = findPreference("enable_app_icon");
			enableAppIcon
					.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
						@Override
						public boolean onPreferenceChange(
								Preference preference, Object newValue) {
							PackageManager packageManager = context
									.getPackageManager();
							int state = (Boolean) newValue ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
									: PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
							String settings = Common.PACKAGE_NAME + ".Settings";
							ComponentName alias = new ComponentName(context,
									settings);
							packageManager.setComponentEnabledSetting(alias,
									state, PackageManager.DONT_KILL_APP);
							return true;
						}
					});
			Preference backupPreferences = findPreference("backup_preferences");
			backupPreferences
					.setOnPreferenceClickListener(new OnPreferenceClickListener() {
						@Override
						public boolean onPreferenceClick(Preference preference) {
							Intent backupPreferences = new Intent(
									Common.ACTION_BACKUP_PREFERENCES);
							backupPreferences.setPackage(Common.PACKAGE_NAME);
							context.sendBroadcast(backupPreferences);
							return true;
						}
					});
			Preference restorePreferences = findPreference("restore_preferences");
			restorePreferences
					.setOnPreferenceClickListener(new OnPreferenceClickListener() {
						@Override
						public boolean onPreferenceClick(Preference preference) {
							Intent restorePreferences = new Intent(
									Common.ACTION_RESTORE_PREFERENCES);
							restorePreferences.setPackage(Common.PACKAGE_NAME);
							context.sendBroadcast(restorePreferences);
							return true;
						}
					});
			Preference resetPreferences = findPreference("reset_preferences");
			resetPreferences
					.setOnPreferenceClickListener(new OnPreferenceClickListener() {
						@Override
						public boolean onPreferenceClick(Preference preference) {
							Intent resetPreferences = new Intent(
									Common.ACTION_RESET_PREFERENCES);
							resetPreferences.setPackage(Common.PACKAGE_NAME);
							context.sendBroadcast(resetPreferences);
							return true;
						}
					});
			appTranslator
					.setOnPreferenceClickListener(new OnPreferenceClickListener() {
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

			Preference appLocale = findPreference("app_locale");
			appLocale
					.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
						@Override
						public boolean onPreferenceChange(
								Preference preference, Object newValue) {
							activity.recreate();
							return true;
						}
					});
			Preference appHelp = findPreference("app_help");
			appHelp.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					Intent openUrl = new Intent(Intent.ACTION_VIEW);
					openUrl.setData(Uri.parse(Common.URL_HELP));
					openUrl.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(openUrl);
					return true;
				}
			});

			Preference resetDeviceProperties = findPreference("reset_device_properties");
			resetDeviceProperties
					.setOnPreferenceClickListener(new OnPreferenceClickListener() {
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
	}

}