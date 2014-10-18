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
import android.os.Bundle;
import android.os.Environment;

public class FileUtils extends BroadcastReceiver {
	public static final String PACKAGE_DIR = Environment
			.getExternalStorageDirectory()
			+ File.separator
			+ XInstaller.PACKAGE_TAG + File.separator;
	public static final File APP_DIR = new File(PACKAGE_DIR);

	@Override
	public void onReceive(Context context, Intent intent) {
		if (!APP_DIR.exists()) {
			APP_DIR.mkdir();
		}
		String action = intent.getAction();
		Bundle extras = intent.getExtras();
		boolean hasExtras = (extras != null) ? true : false;
		if (XInstaller.ACTION_DELETE_FILE.equals(action)) {
			if (hasExtras) {
				String file = extras.getString(XInstaller.FILE);
				File apkFile = new File(file);
				deleteFile(apkFile);
			}
		} else if (XInstaller.ACTION_COPY_FILE.equals(action)) {
			if (hasExtras) {
				String source = extras.getString(XInstaller.SOURCE_FILE);
				String destination = extras.getString(XInstaller.TARGET_FILE);
				File srcFile = new File(source);
				File dstFile = new File(destination);
				try {
					copyFile(srcFile, dstFile);
				} catch (IOException e) {
				}
			}
		}
	}

	public static void deleteFile(File file) {
		file.delete();
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
