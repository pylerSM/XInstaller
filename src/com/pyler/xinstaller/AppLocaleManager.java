package com.pyler.xinstaller;

import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

public class AppLocaleManager {
	private Context mContext;
	public static final String SYSTEM = "system";
	public static final String PREF_APP_LOCALE = "app_locale";

	public AppLocaleManager(Context context) {
		mContext = context;
	}

	@SuppressLint("NewApi")
	public void inicialize() {
		if (mContext == null) {
			return;
		}
		String locale = getLocale();
		if (SYSTEM.equals(locale)) {
			locale = Locale.getDefault().toString();
		}
		Locale newLocale;
		if (locale.contains("_")) {
			String[] loc = locale.split("_");
			newLocale = new Locale(loc[0], loc[1]);
		} else {
			newLocale = new Locale(locale);
		}
		Resources resources = mContext.getResources();
		if (resources == null) {
			return;
		}
		Configuration config = resources.getConfiguration();
		config.locale = newLocale;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			config.setLayoutDirection(newLocale);
		}
		resources.updateConfiguration(config, resources.getDisplayMetrics());
	}

	public void setLocale(String locale) {
		if (mContext == null) {
			return;
		}
		if (locale == null || locale.length() == 0) {
			locale = SYSTEM;
		}
		SharedPreferences prefs = mContext.getSharedPreferences(
				mContext.getPackageName() + "_preferences",
				Context.MODE_PRIVATE);
		SharedPreferences.Editor prefsEditor = prefs.edit();
		prefsEditor.putString(PREF_APP_LOCALE, locale);
		prefsEditor.apply();

	}

	public String getLocale() {
		if (mContext == null) {
			return SYSTEM;
		}
		SharedPreferences prefs = mContext.getSharedPreferences(
				mContext.getPackageName() + "_preferences",
				Context.MODE_PRIVATE);
		return prefs.getString(PREF_APP_LOCALE, SYSTEM);
	}
}
