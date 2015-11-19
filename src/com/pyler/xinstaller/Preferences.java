package com.pyler.xinstaller;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
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
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.List;
import android.os.SystemClock;
import android.os.Build;

public class Preferences extends PreferenceActivity
{
	public static Context context;
	public static Activity activity;
	public static Resources resources;
	public static SharedPreferences prefs;
	public static AppLocaleManager appLocaleManager;
	public static HelpFragmentTablet helpFragTab;
	public static HelpFragmentPhone helpFragPhone;
	public static boolean isLarge;
	public static String url = "file:///android_asset/Help.html";
    private long lastClickTime = 0;

	/**
	 * We let Xposed toggle this. {@see com.pyler.xinstaller.XInstaller}
	 */
	public boolean isModuleEnabled() {
		return false;
	}

	@Override
	public void onBuildHeaders(List<Header> target) {
		loadHeadersFromResource(R.xml.preferences, target);
		context = getApplicationContext();
		isLarge = onIsMultiPane();
	}

	@Override
	protected void onResume() {
		super.onResume();
		boolean isModuleEnabled = isModuleEnabled();
		boolean isEnabledSettings = isEnabledInSettings();

		if (!isModuleEnabled || !isEnabledSettings)
			showError(isModuleEnabled, isEnabledSettings);
	}

	/**
	 * Shows an error at the bottom of the screen.
	 */
	@SuppressLint("InflateParams")
	private void showError(boolean module, boolean settings) {

		LayoutInflater inflater = getLayoutInflater();
		final View view = inflater.inflate(R.layout.disabled_footer, null,
                                           false);
		TextView tv1 = (TextView) view.findViewById(android.R.id.text1);
		TextView tv2 = (TextView) view.findViewById(android.R.id.text2);
		if (!module) {
			tv1.setText(R.string.xposed_module_not_active);
			tv2.setText(R.string.xposed_module_not_active_subtext);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isPackageInstalled("de.robv.android.xposed.installer")) {
                        Intent i = new Intent();
                        i.setAction("de.robv.android.xposed.installer.OPEN_SECTION");
                        i.putExtra("opentab", 1);
                        startActivity(i);
                    // ART and TW make things so complicated.
                    } else {
                        String url = "http://repo.xposed.info/module/de.robv.xposed.android.installer";
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        if (Common.LOLLIPOP_NEWER) {
                            
                            if (isPackageInstalled("touchwiz")) {
       
                                url = "http://google.com/search?q=xposed+" + Build.VERSION.RELEASE + "+touchwiz";
                            }
                            else
                                url = "http://forum.xda-developers.com/showthread.php?t=3034811";
                                
                        }
                        i.setData(Uri.parse(url));
                        startActivity(i);
                    }
                }
            });
		}
        else if (!settings) {
			tv1.setText(R.string.module_disabled_in_settings);
			tv2.setText(R.string.module_disabled_in_settings_subtext);
			view.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        context
                            .getSharedPreferences(Common.PACKAGE_PREFERENCES, Context.MODE_WORLD_READABLE)
                            .edit().putBoolean(Common.PREF_ENABLE_MODULE, true)
						    .apply();
                        view.setVisibility(View.GONE);
                    }

                });
		}
        else
			return;

		setListFooter(view);
	}

    private boolean isPackageInstalled(String pname) {
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo(pname, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
    
	public boolean isEnabledInSettings() {
		boolean isEnabledInSettings = PreferenceManager
            .getDefaultSharedPreferences(this).getBoolean(
            Common.PREF_ENABLE_MODULE, true);

		return isEnabledInSettings;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// We remove the help button in the help menu. Kinda silly if we don't.
		HelpFragmentPhone helpFragment = (HelpFragmentPhone) getFragmentManager()
            .findFragmentByTag("help");

		if (helpFragment == null || !helpFragment.isVisible())
			getMenuInflater().inflate(R.menu.menu, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
            case R.id.action_help:
                if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
                    break;
                }
                lastClickTime = SystemClock.elapsedRealtime();
                showHelpFrag();
                break;
            case android.R.id.home:
                onBackPressed();
                break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Shows the HelpFragment.
	 */
	public synchronized void showHelpFrag() {
		FragmentManager fm = getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();

		// We really don't want spam-clickers to have a whole back stack.
		Fragment oldFrag = fm.findFragmentByTag("help");
		if (oldFrag != null) {
            return;
		}

		if (isLarge) {

			helpFragTab = new HelpFragmentTablet();
			helpFragTab.show(ft, "help");

		}
        else {
			helpFragPhone = new HelpFragmentPhone();
			ft.addToBackStack(null)
                .replace(android.R.id.content, helpFragPhone, "help")
                .commit();
		}
	}

	/**
	 * Fix crash on 4.4+
	 */
	@Override
	protected boolean isValidFragment(String name) {
		return name.equals(Settings.class.getName());
	}

	/**
	 * The PreferenceFragment with the settings in it.
	 */
	public static class Settings extends PreferenceFragment implements
    OnPreferenceClickListener, Preference.OnPreferenceChangeListener
    {

		/**
		 * Empty constructor.
		 */
		public Settings() {
		}

		public static Settings newInstance(String prefsId) {
			Settings settings = new Settings();
			Bundle b = new Bundle();
			b.putString("settings", prefsId);
			settings.setArguments(b);
			return settings;
		}

		@SuppressWarnings("deprecation")
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			activity = getActivity();
			context = activity.getApplicationContext();

			resources = context.getResources();
			getPreferenceManager().setSharedPreferencesMode(
                Context.MODE_WORLD_READABLE);

			// If we are on a tablet
			isLarge = ((PreferenceActivity) activity).onIsMultiPane();

			ActionBar ab = activity.getActionBar();
			if (ab != null) {
				if (!isLarge) {
					ab.setDisplayHomeAsUpEnabled(true);
				}
                else
					ab.setTitle(R.string.app_name);
			}
			String whichSettings = getArguments().getString("settings");
			int prefsType = getPrefsType(whichSettings);

			addPreferencesFromResource(prefsType);

			prefs = PreferenceManager.getDefaultSharedPreferences(context);

			appLocaleManager = new AppLocaleManager(context);
			appLocaleManager.initialize();

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

		@Override
		public void onResume() {
			super.onResume();

			int prefsType = getPrefsType(getArguments().getString("settings"));
			boolean isExpertModeEnabled = prefs.getBoolean(
                Common.PREF_ENABLE_EXPERT_MODE, false);

			PreferenceScreen prefScreen = getPreferenceScreen();

			PreferenceCategory prefCatEnable = (PreferenceCategory) prefScreen
                .findPreference(Common.PREF_ENABLE);
			PreferenceCategory prefCatDisable = (PreferenceCategory) prefScreen
                .findPreference(Common.PREF_DISABLE);

			switch (prefsType) {
                case Common.PREF_CATEGORY_INSTALLATIONS: {
                        Preference installUnsignedApps = findPreference(Common.PREF_ENABLE_INSTALL_UNSIGNED_APP);
                        if (!isExpertModeEnabled) {

                            Preference installOnExternal = findPreference(Common.PREF_ENABLE_INSTALL_EXTERNAL_STORAGE);

                            prefCatEnable.removePreference(installUnsignedApps);
                            prefCatEnable.removePreference(installOnExternal);
                        }
                        if (Common.LOLLIPOP_NEWER)
                            prefCatEnable.removePreference(installUnsignedApps);

                    }
                    break;
                case Common.PREF_CATEGORY_MISC:
                    if (!isExpertModeEnabled) {
                        Preference checkPermissions = findPreference(Common.PREF_DISABLE_CHECK_PERMISSION);
                        Preference verifyJar = findPreference(Common.PREF_DISABLE_VERIFY_JAR);
                        Preference verifySignature = findPreference(Common.PREF_DISABLE_VERIFY_SIGNATURE);
                        prefCatEnable.removePreference(checkPermissions);
                        prefCatDisable.removePreference(verifyJar);
                        prefCatDisable.removePreference(verifySignature);
                    }
                    break;
                case Common.PREF_CATEGORY_XINSTALLER: {

                        CustomSwitchPreference enableModule = (CustomSwitchPreference) findPreference(Common.PREF_ENABLE_MODULE);
                        enableModule
                            .setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                                /**
                                 * This is separate because
                                 * OnPreferenceChangeListeners do NOT like
                                 * SwitchPreferences. It could technically be in the
                                 * normal switch, but it is easier to mark it this
                                 * way.
                                 */
                                @Override
                                public boolean onPreferenceChange(
                                Preference preference, Object newValue) {
                                    if (((CustomSwitchPreference) preference)
                                        .isChecked() != (Boolean) newValue)
                                        recreateApp();
                                    return true;
                                }
                            });

                        Preference enableAppIcon = findPreference(Common.PREF_ENABLE_APP_ICON);
                        enableAppIcon.setOnPreferenceChangeListener(this);

                        Preference enableExpertMode = findPreference(Common.PREF_ENABLE_EXPERT_MODE);
                        enableExpertMode.setOnPreferenceChangeListener(this);

                        Preference appLocale = findPreference(Common.PREF_APP_LOCALE);
                        appLocale.setOnPreferenceChangeListener(this);

                        Preference appHelp = findPreference(Common.PREF_APP_HELP);
                        appHelp.setOnPreferenceClickListener(this);

                        Preference appAbout = findPreference(Common.PREF_APP_ABOUT);
                        appAbout.setOnPreferenceClickListener(this);

                        Preference appBackupRestorePreferences = findPreference(Common.PREF_APP_BACKUP_RESTORE);
                        appBackupRestorePreferences.setOnPreferenceClickListener(this);
                    }
                    break;
                case Common.PREF_CATEGORY_BACKUP_RESTORE: {
                        Preference backupPreferences = findPreference(Common.PREF_BACKUP_PREFERENCES);
                        backupPreferences.setOnPreferenceClickListener(this);

                        Preference restorePreferences = findPreference(Common.PREF_RESTORE_PREFERENCES);
                        restorePreferences.setOnPreferenceClickListener(this);

                        Preference resetPreferences = findPreference(Common.PREF_RESET_PREFERENCES);
                        resetPreferences.setOnPreferenceClickListener(this);

                    }
                    break;
                case Common.PREF_CATEGORY_ABOUT: {
                        Preference appVersion = findPreference(Common.PREF_APP_VERSION);
                        PackageManager pm = context.getPackageManager();

                        try {
                            String versionName = pm.getPackageInfo(
                                context.getPackageName(), 0).versionName;
                            appVersion.setSummary(versionName);
                        }
                        catch (NameNotFoundException e) {
                        }
                        Preference appTranslator = findPreference(Common.PREF_APP_TRANSLATOR);
                        String translator = resources
                            .getString(R.string.app_translator);
                        if (appTranslator != null) {
                            if (translator.isEmpty())
                                prefScreen.removePreference(appTranslator);
                            else
                                appTranslator.setOnPreferenceClickListener(this);
                        }
                    }
                    break;
                case Common.PREF_CATEGORY_DEVICE_PROPERTIES: {
                        Preference resetDeviceProperties = findPreference(Common.PREF_RESET_DEVICE_PROPERTIES);
                        resetDeviceProperties.setOnPreferenceClickListener(this);

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

		/**
		 * Returns the current preference screen name.
		 * 
		 * It is actually the R.xml preference file, so it doubles its usage. :)
		 */
		public static int getPrefsType(String prefsType) {
			if ("xinstaller".equals(prefsType)) {
				return Common.PREF_CATEGORY_XINSTALLER;
			}
            else if ("installations".equals(prefsType)) {
				return Common.PREF_CATEGORY_INSTALLATIONS;
			}
            else if ("uninstallations".equals(prefsType)) {
				return Common.PREF_CATEGORY_UNINSTALLATIONS;
			}
            else if ("apps_info".equals(prefsType)) {
				return Common.PREF_CATEGORY_APPS_INFO;
			}
            else if ("misc".equals(prefsType)) {
				return Common.PREF_CATEGORY_MISC;
			}
            else if ("device_properties".equals(prefsType)) {
				return Common.PREF_CATEGORY_DEVICE_PROPERTIES;
			}
            else if ("backup_restore_preferences".equals(prefsType)) {
				return Common.PREF_CATEGORY_BACKUP_RESTORE;
			}
            else if ("about".equals(prefsType)) {
				return Common.PREF_CATEGORY_ABOUT;
			}
			// Oops.
			return 0;
		}

		/**
		 * Allows us to easily recreate everything.
		 */
		public void recreateApp() {
			if (((Preferences) getActivity()).onIsMultiPane())
				getActivity().recreate();
			else {
				Intent b = new Intent(getActivity(), Preferences.class);
				b.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                           | Intent.FLAG_ACTIVITY_CLEAR_TASK
                           | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(b);
				getActivity().finish();
			}
		}

		public void switchToNewScreen(Settings settings, Preference preference) {
			Preferences prefsActivity = (Preferences) getActivity();

			prefsActivity.startPreferencePanel(
                "com.pyler.xinstaller.Preferences$Settings",
                settings.getArguments(), preference.getTitleRes(),
                // don't care here.
                null, null, 0);
		}

		@Override
		public boolean onPreferenceClick(Preference preference) {
			String preferenceKey = preference.getKey();
			if (Common.PREF_APP_ABOUT.equals(preferenceKey)) {
				Settings settings = Settings.newInstance(Common.PREF_APP_ABOUT);
				switchToNewScreen(settings, preference);
				return true;
			}
            else if (Common.PREF_APP_BACKUP_RESTORE.equals(preferenceKey)) {
				Settings settings = Settings
                    .newInstance(Common.PREF_APP_BACKUP_RESTORE);
				switchToNewScreen(settings, preference);
				return true;
			}
            else if (Common.PREF_BACKUP_PREFERENCES.equals(preferenceKey)) {
				Intent backupPreferences = new Intent(
                    Common.ACTION_BACKUP_PREFERENCES);
				backupPreferences.setPackage(Common.PACKAGE_NAME);
				context.sendBroadcast(backupPreferences);
				return true;
			}
            else if (Common.PREF_RESTORE_PREFERENCES.equals(preferenceKey)) {
				Intent restorePreferences = new Intent(
                    Common.ACTION_RESTORE_PREFERENCES);
				restorePreferences.setPackage(Common.PACKAGE_NAME);
				context.sendBroadcast(restorePreferences);
				return true;
			}
            else if (Common.PREF_RESET_PREFERENCES.equals(preferenceKey)) {
				Intent resetPreferences = new Intent(
                    Common.ACTION_RESET_PREFERENCES);
				resetPreferences.setPackage(Common.PACKAGE_NAME);
				context.sendBroadcast(resetPreferences);
				return true;
			}
            else if (Common.PREF_APP_TRANSLATOR.equals(preferenceKey)) {

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
            else if (Common.PREF_APP_HELP.equals(preferenceKey)) {
				((Preferences) getActivity()).showHelpFrag();
				return true;
			}
            else if (Common.PREF_RESET_DEVICE_PROPERTIES
                     .equals(preferenceKey)) {
				SharedPreferences.Editor prefsEditor = prefs.edit();
				for (String[] property : Common.DEVICE_PROPERTIES) {
					prefsEditor.remove(property[0]);
					EditTextPreference devicePropertyPreference = (EditTextPreference) findPreference(property[0]);
					devicePropertyPreference.setText(property[1]);
				}
				prefsEditor.commit();
				Toast.makeText(context,
                               resources.getString(R.string.preferences_reset),
                               Toast.LENGTH_LONG).show();
				return true;
			}
			return true;
		}

		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			String preferenceKey = preference.getKey();
			if (Common.PREF_ENABLE_APP_ICON.equals(preferenceKey)) {
				PackageManager packageManager = context.getPackageManager();
				int state = (Boolean) newValue ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                    : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
				String settings = Common.PACKAGE_NAME + ".Settings";
				ComponentName alias = new ComponentName(context, settings);
				packageManager.setComponentEnabledSetting(alias, state,
                                                          PackageManager.DONT_KILL_APP);
				return true;
			}
            else if (Common.PREF_ENABLE_EXPERT_MODE.equals(preferenceKey)
                     || Common.PREF_APP_LOCALE.equals(preferenceKey)) {
				recreateApp();
				return true;
			}
			return true;
		}
	}

	/**
	 * Shows the help readme for tablets as a dialog.
	 */
	public static class HelpFragmentTablet extends DialogFragment
    {

		private AlertDialog dialog;

		/**
		 * Empty constructor.
		 */
		public HelpFragmentTablet() {
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			activity = getActivity();
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {

			dialog = new AlertDialog.Builder(activity)
                .setTitle(R.string.app_name)
                .setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
										int which) {
                        dialog.dismiss();
                        getActivity().getFragmentManager()
                            .beginTransaction()
                            .remove(HelpFragmentTablet.this)
                            .commit();
                    }
                }).create();

			WebView wv = new WebView(getActivity());
			dialog.setView(wv);

			wv.loadUrl(url);

			return dialog;
		}
	}

	/**
	 * Shows the help readme for phones in a fullscreen Fragment.
	 */
	public static class HelpFragmentPhone extends Fragment
    {

		private static String oldTitle;

		/**
		 * Empty constructor.
		 */
		public HelpFragmentPhone() {
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			oldTitle = (String) getActivity().getTitle();

			getActivity().setTitle(R.string.help);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup parent,
                                 Bundle savedInstanceState) {
			super.onCreateView(inflater, parent, savedInstanceState);

			WebView v = new WebView(getActivity());

			ActionBar ab = getActivity().getActionBar();
			if (ab != null) {
				ab.setDisplayHomeAsUpEnabled(true);
				ab.setHomeButtonEnabled(true);
			}
			return v;
		}

		@Override
		public void onViewCreated(View v, Bundle savedInstanceState) {
			super.onViewCreated(v, savedInstanceState);
			// Having the help button in the help screen is silly.
			setHasOptionsMenu(false);
			getActivity().invalidateOptionsMenu();

			WebView wv = (WebView) v;
			wv.loadUrl(url);
		}

		@Override
		public void onStop() {
			getActivity().setTitle(oldTitle);
			ActionBar ab = getActivity().getActionBar();
			if (ab != null && oldTitle.equals(getString(R.string.app_name))) {
				ab.setDisplayHomeAsUpEnabled(false);
				ab.setHomeButtonEnabled(false);
			}
			getActivity().invalidateOptionsMenu();
			super.onStop();
		}
	}
}
