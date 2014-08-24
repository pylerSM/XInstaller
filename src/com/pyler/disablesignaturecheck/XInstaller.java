package com.pyler.disablesignaturecheck;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class XInstaller implements IXposedHookZygoteInit,
		IXposedHookLoadPackage {
	XSharedPreferences prefs;
	boolean signaturesCheck;
	boolean signaturesCheckFDroid;
	boolean keepAppsData;
	boolean downgradeApps;
	boolean forwardLock;
	boolean disableSystemApps;
	boolean installUnknownApps;
	boolean verifyApps;
	boolean installAppsOnExternal;
	boolean deviceAdmins;
	boolean autoInstall;
	boolean autoUninstall;
	boolean autoCloseUninstall;
	XC_MethodHook compareSignaturesHook;
	XC_MethodHook deletePackageHook;
	XC_MethodHook installPackageHook;
	XC_MethodHook systemAppsHook;
	XC_MethodHook unknownAppsHook;
	XC_MethodHook verifyAppsHook;
	XC_MethodHook deviceAdminsHook;
	XC_MethodHook fDroidInstallHook;
	XC_MethodHook autoInstallHook;
	XC_MethodHook autoUninstallHook;
	XC_MethodHook autoCloseUninstallHook;
	boolean JB_MR2_NEWER;
	boolean JB_MR1_NEWER;
	boolean KITKAT_NEWER;

	// flags
	int DELETE_KEEP_DATA = 0x00000001;
	int INSTALL_ALLOW_DOWNGRADE = 0x00000080;
	int INSTALL_FORWARD_LOCK = 0x00000001;
	int INSTALL_EXTERNAL = 0x00000008;

	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
		String packageManagerService = "com.android.server.pm.PackageManagerService";
		String devicePolicyManager = "com.android.server.DevicePolicyManagerService";
		prefs = new XSharedPreferences(XInstaller.class.getPackage().getName());
		prefs.makeWorldReadable();

		// hooks

		compareSignaturesHook = new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param)
					throws Throwable {
				prefs.reload();
				signaturesCheck = prefs.getBoolean("disable_signatures_check",
						false);
			}

			@Override
			protected void afterHookedMethod(MethodHookParam param)
					throws Throwable {
				if (signaturesCheck) {
					param.setResult(PackageManager.SIGNATURE_MATCH);
				}
			}
		};

		installPackageHook = new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param)
					throws Throwable {
				prefs.reload();
				downgradeApps = prefs.getBoolean("enable_downgrade_apps", true);
				forwardLock = prefs.getBoolean("disable_forward_lock", true);
				installAppsOnExternal = prefs.getBoolean(
						"enable_install_external_storage", false);
				int ID = JB_MR1_NEWER ? 2 : 1;
				int flags = (Integer) param.args[ID];
				if ((flags & INSTALL_ALLOW_DOWNGRADE) == 0 && downgradeApps) {
					// we dont have this flag, add it!
					flags |= INSTALL_ALLOW_DOWNGRADE;
					param.args[ID] = flags;
				}
				if ((flags & INSTALL_FORWARD_LOCK) != 0 && forwardLock) {
					// we have this flag, remove it!
					flags &= ~INSTALL_FORWARD_LOCK;
					param.args[ID] = flags;

				}
				if ((flags & INSTALL_EXTERNAL) == 0 && installAppsOnExternal) {
					// we dont have this flag, remove it!
					flags |= INSTALL_EXTERNAL;
					param.args[ID] = flags;

				}
			}

		};

		deletePackageHook = new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param)
					throws Throwable {
				prefs.reload();
				keepAppsData = prefs.getBoolean("enable_keep_apps_data", false);
				int ID = JB_MR2_NEWER ? 3 : 2;
				int flags = (Integer) param.args[ID];
				if ((flags & DELETE_KEEP_DATA) == 0 && keepAppsData) {
					// we dont have this flag, add it!
					flags |= DELETE_KEEP_DATA;
					param.args[ID] = flags;
				}

			}

		};

		systemAppsHook = new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param)
					throws Throwable {
				prefs.reload();
				disableSystemApps = prefs.getBoolean(
						"enable_disable_system_apps", true);
				if (disableSystemApps) {
					param.setResult(false);
				}

			}

		};

		unknownAppsHook = new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param)
					throws Throwable {
				prefs.reload();
				installUnknownApps = prefs.getBoolean(
						"enable_install_unknown_apps", true);
				if (installUnknownApps) {
					param.setResult(true);
				}

			}

		};

		verifyAppsHook = new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param)
					throws Throwable {
				prefs.reload();
				verifyApps = prefs.getBoolean("disable_verify_apps", true);
				if (verifyApps) {
					param.setResult(false);
				}

			}

		};

		deviceAdminsHook = new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param)
					throws Throwable {
				prefs.reload();
				deviceAdmins = prefs.getBoolean(
						"enable_uninstall_device_admins", true);
				if (deviceAdmins) {
					param.setResult(false);
				}

			}

		};

		fDroidInstallHook = new XC_MethodHook() {
			String mInstalledSigID = null;

			@Override
			protected void beforeHookedMethod(MethodHookParam param)
					throws Throwable {
				prefs.reload();
				signaturesCheckFDroid = prefs.getBoolean(
						"disable_signatures_check_fdroid", false);
				if (signaturesCheckFDroid) {
					mInstalledSigID = (String) XposedHelpers.getObjectField(
							param.thisObject, "mInstalledSigID");
					XposedHelpers.setObjectField(param.thisObject,
							"mInstalledSigID", null);
				}
			}

			@Override
			protected void afterHookedMethod(MethodHookParam param)
					throws Throwable {
				if (signaturesCheckFDroid) {
					XposedHelpers.setObjectField(param.thisObject,
							"mInstalledSigID", mInstalledSigID);
				}
			}

		};

		autoInstallHook = new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param)
					throws Throwable {
				prefs.reload();
				autoInstall = prefs.getBoolean("enable_auto_install", true);
				if (autoInstall) {
					Button mOk = (Button) XposedHelpers.getObjectField(
							param.thisObject, "mOk");
					XposedHelpers.setBooleanField(param.thisObject,
							"mOkCanInstall", true);
					mOk.performClick();
				}
			}

		};

		autoUninstallHook = new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param)
					throws Throwable {
				prefs.reload();
				autoUninstall = prefs.getBoolean("enable_auto_uninstall", true);
				if (autoUninstall) {
					Button mOk = (Button) XposedHelpers.getObjectField(
							param.thisObject, "mOk");
					mOk.performClick();
				}
			}

		};

		autoCloseUninstallHook = new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param)
					throws Throwable {
				prefs.reload();
				autoCloseUninstall = prefs.getBoolean(
						"enable_auto_close_uninstall", true);
				if (autoCloseUninstall) {
					Button mOk = (Button) XposedHelpers.getObjectField(
							param.thisObject, "mOkButton");
					mOk.performClick();
				}
			}

		};

		// checks

		JB_MR1_NEWER = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) ? true
				: false;
		JB_MR2_NEWER = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) ? true
				: false;
		KITKAT_NEWER = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) ? true
				: false;

		// enablers

		XposedHelpers.findAndHookMethod(packageManagerService, null,
				"compareSignatures", Signature[].class, Signature[].class,
				compareSignaturesHook);

		if (JB_MR1_NEWER) {
			XposedHelpers.findAndHookMethod(packageManagerService, null,
					"installPackageWithVerificationAndEncryption", Uri.class,
					"android.content.pm.IPackageInstallObserver", int.class,
					String.class, "android.content.pm.VerificationParams",
					"android.content.pm.ContainerEncryptionParams",
					installPackageHook);
		} else {
			XposedHelpers.findAndHookMethod(packageManagerService, null,
					"installPackageWithVerification", Uri.class,
					"android.content.pm.IPackageInstallObserver", int.class,
					String.class, Uri.class,
					"android.content.pm.ManifestDigest",
					"android.content.pm.ContainerEncryptionParams",
					installPackageHook);
		}

		if (JB_MR2_NEWER) {
			XposedHelpers.findAndHookMethod(packageManagerService, null,
					"deletePackageAsUser", String.class,
					"android.content.pm.IPackageDeleteObserver", int.class,
					int.class, deletePackageHook);
		} else {
			XposedHelpers.findAndHookMethod(packageManagerService, null,
					"deletePackage", String.class,
					"android.content.pm.IPackageDeleteObserver", int.class,
					deletePackageHook);
		}

		if (JB_MR1_NEWER) {
			XposedHelpers.findAndHookMethod(devicePolicyManager, null,
					"packageHasActiveAdmins", String.class, int.class,
					deviceAdminsHook);
		} else {
			XposedHelpers.findAndHookMethod(devicePolicyManager, null,
					"packageHasActiveAdmins", String.class, deviceAdminsHook);
		}

	}

	@Override
	public void handleLoadPackage(final LoadPackageParam lpparam)
			throws Throwable {
		String PACKAGEINSTALLER_PKG = "com.android.packageinstaller";
		String SETTINGS_PKG = "com.android.settings";
		String FDROID_PKG = "org.fdroid.fdroid";
		String installedAppDetails = "com.android.settings.applications.InstalledAppDetails";
		String packageInstallerActivity = "com.android.packageinstaller.PackageInstallerActivity";
		String uninstallerActivity = "com.android.packageinstaller.UninstallerActivity";
		String uninstallAppProgress = "com.android.packageinstaller.UninstallAppProgress";
		String fDroidAppDetails = "org.fdroid.fdroid.AppDetails";

		if (PACKAGEINSTALLER_PKG.equals(lpparam.packageName)) {
			findAndHookMethod(packageInstallerActivity, lpparam.classLoader,
					"isInstallingUnknownAppsAllowed", unknownAppsHook);
			if (KITKAT_NEWER) {
				XposedHelpers.findAndHookMethod(packageInstallerActivity,
						lpparam.classLoader, "isVerifyAppsEnabled",
						verifyAppsHook);
			}
			XposedHelpers
					.findAndHookMethod(packageInstallerActivity,
							lpparam.classLoader, "startInstallConfirm",
							autoInstallHook);
			XposedHelpers.findAndHookMethod(uninstallerActivity,
					lpparam.classLoader, "onCreate", Bundle.class,
					autoUninstallHook);
			XposedHelpers.findAndHookMethod(uninstallAppProgress,
					lpparam.classLoader, "initView", autoCloseUninstallHook);
		}

		if (SETTINGS_PKG.equals(lpparam.packageName)) {
			XposedHelpers
					.findAndHookMethod(installedAppDetails,
							lpparam.classLoader, "isThisASystemPackage",
							systemAppsHook);
		}

		if (FDROID_PKG.equals(lpparam.packageName)) {
			XposedHelpers.findAndHookMethod(fDroidAppDetails,
					lpparam.classLoader, "install",
					"org.fdroid.fdroid.data.Apk", fDroidInstallHook);
		}
	}

}