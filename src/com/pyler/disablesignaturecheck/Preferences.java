package com.pyler.disablesignaturecheck;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceFragment;

public class Preferences extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new Settings()).commit();
	}

	public static class Settings extends PreferenceFragment {

		@SuppressWarnings("deprecation")
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			getPreferenceManager()
					.setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
			addPreferencesFromResource(R.xml.prefs);
		}

	}
}