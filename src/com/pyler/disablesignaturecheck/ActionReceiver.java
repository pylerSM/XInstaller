package com.pyler.disablesignaturecheck;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class ActionReceiver extends BroadcastReceiver {
	public String ACTION_DISABLE_SIGNATURE_CHECK = "xinstaller.intent.action.DISABLE_SIGNATURE_CHECK";
	public String ACTION_ENABLE_SIGNATURE_CHECK = "xinstaller.intent.action.ENABLE_SIGNATURE_CHECK";
	public String ACTION_DISABLE_PERMISSION_CHECK = "xinstaller.intent.action.DISABLE_PERMISSION_CHECK";
	public String ACTION_ENABLE_PERMISSION_CHECK = "xinstaller.intent.action.ENABLE_PERMISSION_CHECK";

	public String PREF_DISABLE_SIGNATURE_CHECK = "disable_signatures_check";
	public String PREF_DISABLE_PERMISSION_CHECK = "disable_permissions_check";

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		if (ACTION_DISABLE_SIGNATURE_CHECK.equals(action)) {
			prefs.edit().putBoolean(PREF_DISABLE_SIGNATURE_CHECK, true).apply();

		} else if (ACTION_ENABLE_SIGNATURE_CHECK.equals(action)) {
			prefs.edit().putBoolean(PREF_DISABLE_SIGNATURE_CHECK, false)
					.apply();
		} else if (ACTION_DISABLE_PERMISSION_CHECK.equals(action)) {
			prefs.edit().putBoolean(PREF_DISABLE_PERMISSION_CHECK, true)
					.apply();
		} else if (ACTION_ENABLE_PERMISSION_CHECK.equals(action)) {
			prefs.edit().putBoolean(PREF_DISABLE_PERMISSION_CHECK, false)
					.apply();
		}
	}

}