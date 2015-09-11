package com.pyler.xinstaller;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

@SuppressLint("WorldReadableFiles")
public class ConfirmCheckSignatures extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		@SuppressWarnings("deprecation")
		SharedPreferences prefs = getSharedPreferences(
				Common.PACKAGE_PREFERENCES, Context.MODE_WORLD_READABLE);
		final SharedPreferences.Editor prefsEditor = prefs.edit();
		AlertDialog.Builder signatureCheckDialog = new AlertDialog.Builder(
				this, android.R.style.Theme_DeviceDefault_Dialog);
		signatureCheckDialog.setTitle(R.string.check_signatures);
		signatureCheckDialog
				.setMessage(R.string.confirm_check_signatures_message);
		signatureCheckDialog.setCancelable(true);
		signatureCheckDialog.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
						prefsEditor.putBoolean(
								Common.PREF_DISABLE_CHECK_SIGNATURE, true)
								.apply();
						final Handler handler = new Handler();
						handler.postDelayed(new Runnable() {
							@Override
							public void run() {
								prefsEditor.putBoolean(
										Common.PREF_DISABLE_CHECK_SIGNATURE,
										false).apply();
							}
						}, 30 * 1000);
						finish();
					}
				});
		signatureCheckDialog.setNegativeButton(android.R.string.cancel,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
						finish();
					}
				});

		AlertDialog confirmCheckSignatureDialog = signatureCheckDialog.create();
		confirmCheckSignatureDialog.show();

	}

}
