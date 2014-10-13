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
<<<<<<< HEAD
			getPreferenceManager()
					.setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
=======
			getPreferenceManager().setSharedPreferencesMode(
					Context.MODE_WORLD_READABLE);
>>>>>>> 7a87eb664fb84695567f5a467d0a129c32b85e7a
			addPreferencesFromResource(R.xml.prefs);
		}

	}
}