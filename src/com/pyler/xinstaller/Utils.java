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

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class Utils extends BroadcastReceiver {
	public Context ctx;
	public Resources res;

	@Override
	public void onReceive(Context context, Intent intent) {
		if (!Common.PACKAGE_DIR.exists()) {
			Common.PACKAGE_DIR.mkdir();
		}
		ctx = context;
		res = ctx.getResources();
		String action = intent.getAction();
		Bundle extras = intent.getExtras();
		boolean hasExtras = (extras != null) ? true : false;
		if (Common.ACTION_BACKUP_APK_FILE.equals(action)) {
			if (hasExtras) {
				String apkFile = extras.getString(Common.FILE);
				backupApkFile(apkFile);
			}
		} else if (Common.ACTION_SET_PREFERENCE.equals(action)) {
			if (hasExtras) {
				String preference = extras.getString(Common.PREFERENCE);
				boolean value = extras.getBoolean(Common.VALUE);
				setPreference(preference, value);

			}
		} else if (Common.ACTION_BACKUP_PREFERENCES.equals(action)) {
			backupPreferences();
		} else if (Common.ACTION_RESTORE_PREFERENCES.equals(action)) {
			restorePreferences();
		} else if (Common.ACTION_RESET_PREFERENCES.equals(action)) {
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
			String backupApkFile = Common.PACKAGE_DIR + fileName;
			copyFile(new File(apkFile), new File(backupApkFile));
		} catch (Exception e) {
		}
	}

	@SuppressLint("WorldReadableFiles")
	public void setPreference(String preference, boolean value) {
		SharedPreferences prefs = ctx.getSharedPreferences(
				Common.PACKAGE_PREFERENCES, Context.MODE_WORLD_READABLE);
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
		if (!Common.PREFERENCES_BACKUP_FILE.exists()) {
			try {
				Common.PREFERENCES_BACKUP_FILE.createNewFile();
			} catch (Exception e) {
			}
		}

		ObjectOutputStream output = null;
		try {
			output = new ObjectOutputStream(new FileOutputStream(
					Common.PREFERENCES_BACKUP_FILE));
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
		if (!Common.PREFERENCES_BACKUP_FILE.exists()) {
			Toast.makeText(ctx, res.getString(R.string.no_backup_file),
					Toast.LENGTH_LONG).show();
			return;
		}

		ObjectInputStream input = null;
		try {
			input = new ObjectInputStream(new FileInputStream(
					Common.PREFERENCES_BACKUP_FILE));
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
