package com.pyler.xinstaller;

import java.io.DataOutputStream;
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
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.Toast;

@SuppressLint("WorldReadableFiles")
@SuppressWarnings("deprecation")
public class Utils extends BroadcastReceiver {
	public Context ctx;
	public Resources resources;

	@Override
	public void onReceive(Context context, Intent intent) {
		if (!Common.PACKAGE_DIR.exists()) {
			Common.PACKAGE_DIR.mkdir();
		}
		ctx = context;
		resources = ctx.getResources();
		String action = intent.getAction();

		Bundle extras = intent.getExtras();
		boolean hasExtras = extras != null;
		if (Common.ACTION_BACKUP_APK_FILE.equals(action)) {
			if (hasExtras) {
				String apkFile = extras.getString(Common.FILE);
				backupApkFile(apkFile);
			}
		} else if (Common.ACTION_DELETE_APK_FILE.equals(action)) {
			if (hasExtras) {
				String apkFile = extras.getString(Common.FILE);
				deleteApkFile(apkFile);
			}
		} else if (Common.ACTION_UNINSTALL_SYSTEM_APP.equals(action)) {
			if (hasExtras) {
				String packageName = extras.getString(Common.PACKAGE);
				uninstallSystemApp(packageName);
			}
		} else if (Common.ACTION_BACKUP_PREFERENCES.equals(action)) {
			backupPreferences();
		} else if (Common.ACTION_RESTORE_PREFERENCES.equals(action)) {
			restorePreferences();
		} else if (Common.ACTION_RESET_PREFERENCES.equals(action)) {
			resetPreferences();
		} else if (Common.ACTION_CONFIRM_CHECK_SIGNATURE.equals(action)) {
			confirmCheckSignatures();
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
			String backupApkFile = Common.APP_DIR + fileName;
			File src = new File(apkFile);
			File dst = new File(backupApkFile);
			if (!dst.equals(src)) {
				copyFile(src, dst);
			}
		} catch (Exception e) {
		}
	}

	public void deleteApkFile(String apkFile) {
		File apk = new File(apkFile);
		if (apk.exists()) {
			apk.delete();
		}
	}

	public void uninstallSystemApp(String packageName) {
		PackageManager pm = ctx.getPackageManager();
		PackageInfo pkgInfo;
		try {
			pkgInfo = pm.getPackageInfo(packageName, 0);
		} catch (NameNotFoundException e) {
			return;
		}
		final String apkFile = pkgInfo.applicationInfo.sourceDir;
		boolean installedInSystem = apkFile.startsWith("/system");
		String removeAPK = "rm " + apkFile;
		String removeData = "pm clear " + packageName;
		String remountRW = "mount -o remount,rw /system";
		String remountRO = "mount -o remount,ro /system";

		try {
			Process process = Runtime.getRuntime().exec("su");
			DataOutputStream os = new DataOutputStream(
					process.getOutputStream());
			if (installedInSystem) {
				os.writeBytes(remountRW + "\n");
				os.writeBytes(removeAPK + "\n");
				os.writeBytes(remountRO + "\n");
			} else {
				os.writeBytes(removeAPK + "\n");
			}
			os.writeBytes(removeData + "\n");
			os.writeBytes("exit\n");
			os.flush();
			os.close();
			Toast.makeText(ctx, resources.getString(R.string.app_uninstalled),
					Toast.LENGTH_LONG).show();
		} catch (Exception e) {
		}
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
			SharedPreferences prefs = ctx.getSharedPreferences(
					Common.PACKAGE_PREFERENCES, Context.MODE_WORLD_READABLE);
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

		Toast.makeText(ctx,
				resources.getString(R.string.preferences_backed_up),
				Toast.LENGTH_LONG).show();
	}

	public void restorePreferences() {
		if (!Common.PREFERENCES_BACKUP_FILE.exists()) {
			Toast.makeText(ctx, resources.getString(R.string.no_backup_file),
					Toast.LENGTH_LONG).show();
			return;
		}

		ObjectInputStream input = null;
		try {
			input = new ObjectInputStream(new FileInputStream(
					Common.PREFERENCES_BACKUP_FILE));
			SharedPreferences prefs = ctx.getSharedPreferences(
					Common.PACKAGE_PREFERENCES, Context.MODE_WORLD_READABLE);
			SharedPreferences.Editor prefsEditor = prefs.edit();
			prefsEditor.clear();
			@SuppressWarnings("unchecked")
			Map<String, ?> entries = (Map<String, ?>) input.readObject();
			for (Map.Entry<String, ?> entry : entries.entrySet()) {
				Object value = entry.getValue();
				String key = entry.getKey();
				if (value instanceof Boolean) {
					prefsEditor.putBoolean(key,
							((Boolean) value).booleanValue());
				} else if (value instanceof String) {
					prefsEditor.putString(key, (String) value);
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

		Toast.makeText(ctx, resources.getString(R.string.preferences_restored),
				Toast.LENGTH_LONG).show();
	}

	public void resetPreferences() {
		SharedPreferences prefs = ctx.getSharedPreferences(
				Common.PACKAGE_PREFERENCES, Context.MODE_WORLD_READABLE);
		SharedPreferences.Editor prefsEditor = prefs.edit();
		prefsEditor.clear();
		prefsEditor.commit();

		Toast.makeText(ctx, resources.getString(R.string.preferences_reset),
				Toast.LENGTH_LONG).show();
	}

	public void confirmCheckSignatures() {
		Intent openConfirmCheckSignatures = new Intent(ctx,
				ConfirmCheckSignatures.class);
		openConfirmCheckSignatures.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		ctx.startActivity(openConfirmCheckSignatures);
	}
}
