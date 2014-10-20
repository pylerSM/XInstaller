package com.pyler.disablesignaturecheck;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;

public class FileUtils extends BroadcastReceiver {
	public static final String PACKAGE_DIR = Environment
			.getExternalStorageDirectory()
			+ File.separator
			+ XInstaller.PACKAGE_TAG + File.separator;
	public static final File APP_DIR = new File(PACKAGE_DIR);
	public static Context PACKAGE_CONTEXT;

	@Override
	public void onReceive(Context context, Intent intent) {
		if (!APP_DIR.exists()) {
			APP_DIR.mkdir();
		}
		PACKAGE_CONTEXT = context;
		String action = intent.getAction();
		Bundle extras = intent.getExtras();
		boolean hasExtras = (extras != null) ? true : false;
		if (XInstaller.ACTION_BACKUP_APK_FILE.equals(action)) {
			if (hasExtras) {
				String apkFile = extras.getString(XInstaller.APK_FILE);
				backupApkFile(apkFile);
			}
		}
	}

	public static void backupApkFile(String apkFile) {
		PackageManager pm = PACKAGE_CONTEXT.getPackageManager();
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

	public static void copyFile(File src, File dst) throws IOException {
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
}
