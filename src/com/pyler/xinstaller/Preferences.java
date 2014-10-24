package com.pyler.xinstaller;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.pyler.xinstaller.R;

public class Preferences extends Activity {
	public static Context context;

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
			getPreferenceManager().setSharedPreferencesMode(
					Context.MODE_WORLD_READABLE);
			addPreferencesFromResource(R.xml.prefs);
			Preference appVersion = getPreferenceScreen().findPreference(
					"app_version");
			PackageManager pm = context.getPackageManager();
			try {
				String versionName = pm.getPackageInfo(
						context.getPackageName(), 0).versionName;
				appVersion.setSummary(versionName);
			} catch (NameNotFoundException e) {
			}
		}
	}

}