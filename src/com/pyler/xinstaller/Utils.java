package com.pyler.xinstaller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class Utils extends BroadcastReceiver {
	public static final String PACKAGE_TAG = "XInstaller";
	public static final String PACKAGE_DIR = Environment
			.getExternalStorageDirectory()
			+ File.separator
			+ PACKAGE_TAG
			+ File.separator;
	public static final String PACKAGE_NAME = Utils.class.getPackage()
			.getName();
	public static final File APP_DIR = new File(PACKAGE_DIR);
	public static final File PREFERENCES_BACKUP_FILE = new File(PACKAGE_DIR
			+ File.separator + PACKAGE_TAG + ".backup");
	public Context ctx;
	public Resources res;

	@Override
	public void onReceive(Context context, Intent intent) {
		if (!APP_DIR.exists()) {
			APP_DIR.mkdir();
		}
		ctx = context;
		res = ctx.getResources();
		String action = intent.getAction();
		Bundle extras = intent.getExtras();
		boolean hasExtras = (extras != null) ? true : false;
		if (XInstaller.ACTION_BACKUP_APK_FILE.equals(action)) {
			if (hasExtras) {
				String apkFile = extras.getString(XInstaller.APK_FILE);
				backupApkFile(apkFile);
			}
		} else if (XInstaller.ACTION_SET_PREFERENCE.equals(action)) {
			if (hasExtras) {
				String preference = extras.getString(XInstaller.PREFERENCE);
				boolean value = extras.getBoolean(XInstaller.VALUE);
				setPreference(preference, value);

			}
		} else if (XInstaller.ACTION_BACKUP_PREFERENCES.equals(action)) {
			backupPreferences();
		} else if (XInstaller.ACTION_RESTORE_PREFERENCES.equals(action)) {
			restorePreferences();
		} else if (XInstaller.ACTION_RESET_PREFERENCES.equals(action)) {
			resetPreferences();
		}
	}

	public void backupApkFile(String apkFile) {
		PackageManager pm = ctx.getPackageManager();
		try {
			PackageInfo pi = pm.getPackageArchiveInfo(apkFile, 0);
			pi.applicationInfo.publicSourceDir = apkFile;
			pi.applicationInfo.sourceDir = apkFile;
			ApplicationInfo ai = pi.applicationInfo;
			String appName = (String) pm.getApplicationLabel(ai);
			String versionName = pi.versionName;
			String fileName = appName + " " + versionName + ".apk";
			String backupApkFile = PACKAGE_DIR + fileName;
			copyFile(new File(apkFile), new File(backupApkFile));
		} catch (Exception e) {
		}
	}

	public void setPreference(String preference, boolean value) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(ctx);
		prefs.edit().putBoolean(preference, value).apply();
	}

	public void copyFile(File src, File dst) throws IOException {
		InputStream in = new FileInputStream(src);
		OutputStream out = new FileOutputStream(dst);

		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
	}

	public void backupPreferences() {
		if (!PREFERENCES_BACKUP_FILE.exists()) {
			try {
				PREFERENCES_BACKUP_FILE.createNewFile();
			} catch (Exception e) {
			}
		}

		ObjectOutputStream output = null;
		try {
			output = new ObjectOutputStream(new FileOutputStream(
					PREFERENCES_BACKUP_FILE));
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(ctx);
			output.writeObject(prefs.getAll());
		} catch (Exception e) {
		} finally {
			try {
				if (output != null) {
					output.flush();
					output.close();
				}
			} catch (Exception e) {
			}
		}

		Toast.makeText(ctx, res.getString(R.string.preferences_backed_up),
				Toast.LENGTH_LONG).show();
	}

	public void restorePreferences() {
		if (!PREFERENCES_BACKUP_FILE.exists()) {
			Toast.makeText(ctx, res.getString(R.string.no_backup_file),
					Toast.LENGTH_LONG).show();
			return;
		}

		ObjectInputStream input = null;
		try {
			input = new ObjectInputStream(new FileInputStream(
					PREFERENCES_BACKUP_FILE));
			SharedPreferences.Editor prefsEditor = PreferenceManager
					.getDefaultSharedPreferences(ctx).edit();
			prefsEditor.clear();
			@SuppressWarnings("unchecked")
			Map<String, ?> entries = (Map<String, ?>) input.readObject();
			for (Map.Entry<String, ?> entry : entries.entrySet()) {
				Object value = entry.getValue();
				String key = entry.getKey();
				if (value instanceof Boolean) {
					prefsEditor.putBoolean(key,
							((Boolean) value).booleanValue());
				}
			}
			prefsEditor.commit();
		} catch (Exception e) {
		} finally {
			try {
				if (input != null) {
					input.close();
				}
			} catch (Exception e) {
			}
		}

		Toast.makeText(ctx, res.getString(R.string.preferences_restored),
				Toast.LENGTH_LONG).show();
	}

	public void resetPreferences() {
		SharedPreferences.Editor prefsEditor = PreferenceManager
				.getDefaultSharedPreferences(ctx).edit();
		prefsEditor.clear();
		prefsEditor.commit();

		Toast.makeText(ctx, res.getString(R.string.preferences_reset),
				Toast.LENGTH_LONG).show();
	}
}
