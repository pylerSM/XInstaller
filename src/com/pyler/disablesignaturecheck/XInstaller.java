package com.pyler.disablesignaturecheck;

import java.io.File;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
<<<<<<< HEAD
import android.os.Message;
=======
import android.os.Environment;
import android.os.Message;
import android.preference.PreferenceManager;
>>>>>>> 7a87eb664fb84695567f5a467d0a129c32b85e7a
import android.widget.Button;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class XInstaller implements IXposedHookZygoteInit,
		IXposedHookLoadPackage {
<<<<<<< HEAD
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
	boolean autoCloseInstall;
	boolean autoLaunchInstall;
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
	XC_MethodHook autoCloseInstallHook;
	boolean JB_MR2_NEWER;
	boolean JB_MR1_NEWER;
	boolean KITKAT_NEWER;
=======
	public static XSharedPreferences prefs;
	public boolean signaturesCheck;
	public boolean signaturesCheckFDroid;
	public boolean keepAppsData;
	public boolean downgradeApps;
	public boolean forwardLock;
	public boolean disableSystemApps;
	public boolean installUnknownApps;
	public boolean verifyApps;
	public boolean installAppsOnExternal;
	public boolean deviceAdmins;
	public boolean autoInstall;
	public boolean autoUninstall;
	public boolean autoCloseUninstall;
	public boolean autoCloseInstall;
	public boolean autoLaunchInstall;
	public boolean permissionsCheck;
	public XC_MethodHook checkSignaturesHook;
	public XC_MethodHook deletePackageHook;
	public XC_MethodHook installPackageHook;
	public XC_MethodHook systemAppsHook;
	public XC_MethodHook unknownAppsHook;
	public XC_MethodHook verifyAppsHook;
	public XC_MethodHook deviceAdminsHook;
	public XC_MethodHook fDroidInstallHook;
	public XC_MethodHook autoInstallHook;
	public XC_MethodHook autoUninstallHook;
	public XC_MethodHook autoCloseUninstallHook;
	public XC_MethodHook autoCloseInstallHook;
	public XC_MethodHook packageManagerHook;
	public XC_MethodHook checkPermissionsHook;
	public XC_MethodHook activityManagerHook;
	public static boolean JB_MR2_NEWER;
	public static boolean JB_MR1_NEWER;
	public static boolean KITKAT_NEWER;
	public static boolean APIEnabled;
	public static Context mContext;
	public static Object packageManagerObj;
	public static Object activityManagerObj;
	public static BroadcastReceiver systemAPI;

	// intents
	public static final String ACTION_INSTALL_PACKAGE = "xinstaller.intent.action.INSTALL_PACKAGE";
	public static final String ACTION_DISABLE_SIGNATURE_CHECK = "xinstaller.intent.action.DISABLE_SIGNATURE_CHECK";
	public static final String ACTION_ENABLE_SIGNATURE_CHECK = "xinstaller.intent.action.ENABLE_SIGNATURE_CHECK";
	public static final String ACTION_DISABLE_PERMISSION_CHECK = "xinstaller.intent.action.DISABLE_PERMISSION_CHECK";
	public static final String ACTION_ENABLE_PERMISSION_CHECK = "xinstaller.intent.action.ENABLE_PERMISSION_CHECK";
	public static final String ACTION_CLEAR_APP_DATA = "xinstaller.intent.action.CLEAR_APP_DATA";
	public static final String ACTION_FORCE_STOP_PACKAGE = "xinstaller.intent.action.FORCE_STOP_PACKAGE";
	public static final String ACTION_DELETE_PACKAGE = "xinstaller.intent.action.DELETE_PACKAGE";
	public static final String ACTION_CLEAR_APP_CACHE = "xinstaller.intent.action.CLEAR_APP_CACHE";
	public static final String ACTION_MOVE_PACKAGE = "xinstaller.intent.action.MOVE_PACKAGE";
	public static final String ACTION_RUN_XINSTALLER = "xinstaller.intent.action.RUN_XINSTALLER";

	// prefs
	public static final String PREF_ENABLE_MODULE = "enable_module";
	public static final String PREF_DISABLE_SIGNATURE_CHECK = "disable_signatures_check";
	public static final String PREF_DISABLE_PERMISSION_CHECK = "disable_permissions_check";
	public static final String PREF_ENABLED_DOWNGRADE_APP = "enable_downgrade_apps";
	public static final String PREF_DISABLE_SIGNATURE_CHECK_FDROID = "disable_signatures_check_fdroid";
	public static final String PREF_DISABLE_VERIFY_APP = "disable_verify_apps";
	public static final String PREF_ENABLE_AUTO_UNINSTALL = "enable_auto_uninstall";
	public static final String PREF_ENABLE_AUTO_INSTALL = "enable_auto_install";
	public static final String PREF_ENABLE_KEEP_APP_DATA = "enable_keep_apps_data";
	public static final String PREF_ENABLE_INSTALL_UNKNOWN_APP = "enable_install_unknown_apps";
	public static final String PREF_ENABLE_UNINSTALL_DEVICE_ADMIN = "enable_uninstall_device_admins";
	public static final String PREF_DISABLE_FORWARD_LOCK = "disable_forward_lock";
	public static final String PREF_DISABLE_SYSTEM_APP = "disable_system_apps";
	public static final String PREF_ENABLE_DISABLE_SYSTEM_APP = "enable_disable_system_apps";
	public static final String PREF_ENABLE_AUTO_CLOSE_UNINSTALL = "enable_auto_close_uninstall";
	public static final String PREF_ENABLE_AUTO_CLOSE_INSTALL = "enable_auto_close_install";
	public static final String PREF_ENABLE_LAUNCH_INSTALL = "enable_auto_launch_install";
	public static final String PREF_ENABLE_INSTALL_EXTERNAL_STORAGE = "enable_install_external_storage";

	// constants
	public static final String PACKAGE_TAG = "XInstaller";
	public static final String PACKAGE_DIR = Environment
			.getExternalStorageDirectory()
			+ File.separator
			+ PACKAGE_TAG
			+ File.separator;
	public static final String PACKAGE_NAME = XInstaller.class.getPackage()
			.getName();
	public static final String PACKAGEINSTALLER_PKG = "com.android.packageinstaller";
	public static final String SETTINGS_PKG = "com.android.settings";
	public static final String FDROID_PKG = "org.fdroid.fdroid";
	public static final String packageManagerService = "com.android.server.pm.PackageManagerService";
	public static final String devicePolicyManager = "com.android.server.DevicePolicyManagerService";
	public static final String installedAppDetails = "com.android.settings.applications.InstalledAppDetails";
	public static final String packageInstallerActivity = "com.android.packageinstaller.PackageInstallerActivity";
	public static final String installAppProgress = "com.android.packageinstaller.InstallAppProgress";
	public static final String uninstallerActivity = "com.android.packageinstaller.UninstallerActivity";
	public static final String uninstallAppProgress = "com.android.packageinstaller.UninstallAppProgress";
	public static final String fDroidAppDetails = "org.fdroid.fdroid.AppDetails";
	public static final String activityManager = "android.app.ActivityManager";
	public static final String androidSystem = "android";

	// classes
	public Class<?> packageManagerClass = XposedHelpers.findClass(
			packageManagerService, null);
	public Class<?> activityManagerClass = XposedHelpers.findClass(
			activityManager, null);
>>>>>>> 7a87eb664fb84695567f5a467d0a129c32b85e7a

	// flags
	public static final int DELETE_KEEP_DATA = 0x00000001;
	public static final int INSTALL_ALLOW_DOWNGRADE = 0x00000080;
	public static final int INSTALL_FORWARD_LOCK = 0x00000001;
	public static final int INSTALL_EXTERNAL = 0x00000008;
	public static final int INSTALL_REPLACE_EXISTING = 0x00000002;

	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
		prefs = new XSharedPreferences(XInstaller.class.getPackage().getName());
		prefs.makeWorldReadable();
		APIEnabled = false;

		// hooks

		packageManagerHook = new XC_MethodHook() {
			@Override
<<<<<<< HEAD
			protected void afterHookedMethod(MethodHookParam param)
					throws Throwable {
				prefs.reload();
				signaturesCheck = prefs.getBoolean("disable_signatures_check",
						false);
				if (signaturesCheck) {
=======
			protected void afterHookedMethod(final MethodHookParam param)
					throws Throwable {
				packageManagerObj = param.thisObject;
			}
		};

		activityManagerHook = new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param)
					throws Throwable {
				activityManagerObj = param.thisObject;
				Context context = (Context) XposedHelpers.getObjectField(
						param.thisObject, "mContext");
				if (context == null && param.args.length != 0) {
					context = (Context) param.args[0];
				}
				if (context != null) {
					if (isModuleEnabled() && !APIEnabled
							&& androidSystem.equals(context.getPackageName())) {
						mContext = context;
						IntentFilter intentFilter = new IntentFilter();
						intentFilter.addAction(ACTION_DISABLE_SIGNATURE_CHECK);
						intentFilter.addAction(ACTION_ENABLE_SIGNATURE_CHECK);
						intentFilter.addAction(ACTION_DISABLE_PERMISSION_CHECK);
						intentFilter.addAction(ACTION_ENABLE_PERMISSION_CHECK);
						intentFilter.addAction(ACTION_INSTALL_PACKAGE);
						intentFilter.addAction(ACTION_CLEAR_APP_DATA);
						intentFilter.addAction(ACTION_FORCE_STOP_PACKAGE);
						intentFilter.addAction(ACTION_DELETE_PACKAGE);
						intentFilter.addAction(ACTION_CLEAR_APP_CACHE);
						intentFilter.addAction(ACTION_MOVE_PACKAGE);
						intentFilter.addAction(ACTION_RUN_XINSTALLER);
						mContext.registerReceiver(systemAPI, intentFilter);
						APIEnabled = true;
					}
				}
			}
		};

		checkPermissionsHook = new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param)
					throws Throwable {
				prefs.reload();
				permissionsCheck = prefs.getBoolean(
						PREF_DISABLE_PERMISSION_CHECK, false);
				if (isModuleEnabled() && permissionsCheck) {
					param.setResult(PackageManager.PERMISSION_GRANTED);
				}
			}
		};

		checkSignaturesHook = new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param)
					throws Throwable {
				prefs.reload();
				signaturesCheck = prefs.getBoolean(
						PREF_DISABLE_SIGNATURE_CHECK, false);
				if (isModuleEnabled() && signaturesCheck) {
>>>>>>> 7a87eb664fb84695567f5a467d0a129c32b85e7a
					param.setResult(PackageManager.SIGNATURE_MATCH);
				}
			}
		};

		installPackageHook = new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param)
					throws Throwable {
				prefs.reload();
				downgradeApps = prefs.getBoolean(PREF_ENABLED_DOWNGRADE_APP,
						true);
				forwardLock = prefs.getBoolean(PREF_DISABLE_FORWARD_LOCK, true);
				installAppsOnExternal = prefs.getBoolean(
<<<<<<< HEAD
						"enable_install_external_storage", false);
				int ID = JB_MR1_NEWER ? 2 : 1;
				int flags = (Integer) param.args[ID];
				if ((flags & INSTALL_ALLOW_DOWNGRADE) == 0 && downgradeApps) {
					// we dont have this flag, add it
					flags |= INSTALL_ALLOW_DOWNGRADE;
					param.args[ID] = flags;
				}
				if ((flags & INSTALL_FORWARD_LOCK) != 0 && forwardLock) {
					// we have this flag, remove it
					flags &= ~INSTALL_FORWARD_LOCK;
					param.args[ID] = flags;

				}
				if ((flags & INSTALL_EXTERNAL) == 0 && installAppsOnExternal) {
					// we dont have this flag, add it
					flags |= INSTALL_EXTERNAL;
					param.args[ID] = flags;

=======
						PREF_ENABLE_INSTALL_EXTERNAL_STORAGE, false);
				int ID = JB_MR1_NEWER ? 2 : 1;
				int flags = (Integer) param.args[ID];
				if ((flags & INSTALL_ALLOW_DOWNGRADE) == 0
						&& isModuleEnabled() && downgradeApps) {
					// we dont have this flag, add it
					flags |= INSTALL_ALLOW_DOWNGRADE;
				}
				if ((flags & INSTALL_FORWARD_LOCK) != 0
						&& isModuleEnabled() && forwardLock) {
					// we have this flag, remove it
					flags &= ~INSTALL_FORWARD_LOCK;
				}
				if ((flags & INSTALL_EXTERNAL) == 0 && isModuleEnabled()
						&& installAppsOnExternal) {
					// we dont have this flag, add it
					flags |= INSTALL_EXTERNAL;
>>>>>>> 7a87eb664fb84695567f5a467d0a129c32b85e7a
				}
				param.args[ID] = flags;
			}

		};

		deletePackageHook = new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param)
					throws Throwable {
				prefs.reload();
<<<<<<< HEAD
				keepAppsData = prefs.getBoolean("enable_keep_apps_data", false);
				int ID = JB_MR2_NEWER ? 3 : 2;
				int flags = (Integer) param.args[ID];
				if ((flags & DELETE_KEEP_DATA) == 0 && keepAppsData) {
					// we dont have this flag, add it
					flags |= DELETE_KEEP_DATA;
					param.args[ID] = flags;
=======
				keepAppsData = prefs.getBoolean(PREF_ENABLE_KEEP_APP_DATA,
						false);
				int ID = JB_MR2_NEWER ? 3 : 2;
				int flags = (Integer) param.args[ID];
				if ((flags & DELETE_KEEP_DATA) == 0 && isModuleEnabled()
						&& keepAppsData) {
					// we dont have this flag, add it
					flags |= DELETE_KEEP_DATA;
>>>>>>> 7a87eb664fb84695567f5a467d0a129c32b85e7a
				}
				param.args[ID] = flags;
			}

		};

		systemAppsHook = new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param)
					throws Throwable {
				prefs.reload();
				disableSystemApps = prefs.getBoolean(PREF_DISABLE_SYSTEM_APP,
						true);
				if (isModuleEnabled() && disableSystemApps) {
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
						PREF_ENABLE_INSTALL_UNKNOWN_APP, true);
				if (isModuleEnabled() && installUnknownApps) {
					param.setResult(true);
				}

			}

		};

		verifyAppsHook = new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param)
					throws Throwable {
				prefs.reload();
				verifyApps = prefs.getBoolean(PREF_DISABLE_VERIFY_APP, true);
				if (isModuleEnabled() && verifyApps) {
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
						PREF_ENABLE_UNINSTALL_DEVICE_ADMIN, true);
				if (isModuleEnabled() && deviceAdmins) {
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
						PREF_DISABLE_SIGNATURE_CHECK_FDROID, false);
				if (isModuleEnabled() && signaturesCheckFDroid) {
					mInstalledSigID = (String) XposedHelpers.getObjectField(
							param.thisObject, "mInstalledSigID");
					XposedHelpers.setObjectField(param.thisObject,
							"mInstalledSigID", null);
				}
			}

			@Override
			protected void afterHookedMethod(MethodHookParam param)
					throws Throwable {
				if (isModuleEnabled() && signaturesCheckFDroid) {
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
<<<<<<< HEAD
				autoInstall = prefs.getBoolean("enable_auto_install", true);
				if (autoInstall) {
=======
				autoInstall = prefs.getBoolean(PREF_ENABLE_AUTO_INSTALL, true);
				if (isModuleEnabled() && autoInstall) {
>>>>>>> 7a87eb664fb84695567f5a467d0a129c32b85e7a
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
<<<<<<< HEAD
				autoUninstall = prefs.getBoolean("enable_auto_uninstall", true);
				if (autoUninstall) {
=======
				autoUninstall = prefs.getBoolean(PREF_ENABLE_AUTO_UNINSTALL,
						true);
				if (isModuleEnabled() && autoUninstall) {
>>>>>>> 7a87eb664fb84695567f5a467d0a129c32b85e7a
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
<<<<<<< HEAD
						"enable_auto_close_uninstall", true);
				if (autoCloseUninstall) {
=======
						PREF_ENABLE_AUTO_CLOSE_UNINSTALL, true);
				if (isModuleEnabled() && autoCloseUninstall) {
>>>>>>> 7a87eb664fb84695567f5a467d0a129c32b85e7a
					Button mOk = (Button) XposedHelpers.getObjectField(
							param.thisObject, "mOkButton");
					mOk.performClick();
				}
			}

		};

		autoCloseInstallHook = new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param)
					throws Throwable {
				prefs.reload();
				autoCloseInstall = prefs.getBoolean(
<<<<<<< HEAD
						"enable_auto_close_install", true);
				autoLaunchInstall = prefs.getBoolean(
						"enable_auto_launch_install", false);
				if (autoCloseInstall) {
=======
						PREF_ENABLE_AUTO_CLOSE_INSTALL, true);
				autoLaunchInstall = prefs.getBoolean(
						PREF_ENABLE_LAUNCH_INSTALL, false);
				if (isModuleEnabled() && autoCloseInstall) {
>>>>>>> 7a87eb664fb84695567f5a467d0a129c32b85e7a
					Button mOk = (Button) XposedHelpers.getObjectField(
							XposedHelpers.getSurroundingThis(param.thisObject),
							"mDoneButton");
					mOk.performClick();
				}
				if (autoLaunchInstall) {
					Button mLaunch = (Button) XposedHelpers.getObjectField(
							XposedHelpers.getSurroundingThis(param.thisObject),
							"mLaunchButton");
					mLaunch.performClick();
				}
			}

		};

<<<<<<< HEAD
=======
		// system API

		systemAPI = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {

				String action = intent.getAction();
				Bundle extras = intent.getExtras();
				boolean hasExtras = (extras != null) ? true : false;
				if (ACTION_DISABLE_SIGNATURE_CHECK.equals(action)) {
					disableSignatureCheck(true);
				} else if (ACTION_ENABLE_SIGNATURE_CHECK.equals(action)) {
					disableSignatureCheck(false);
				} else if (ACTION_DISABLE_PERMISSION_CHECK.equals(action)) {
					disablePermissionCheck(true);
				} else if (ACTION_ENABLE_PERMISSION_CHECK.equals(action)) {
					disablePermissionCheck(false);
				} else if (ACTION_INSTALL_PACKAGE.equals(action)) {
					if (hasExtras) {
						String apkFile = extras.getString("file");
						Integer flag = extras.getInt("flags");
						if (apkFile != null) {
							if (flag != null) {
								int flags = flag;
								installPackage(apkFile, flags);
							} else {
								installPackage(apkFile,
										INSTALL_REPLACE_EXISTING);
							}
						}
					}
				} else if (ACTION_CLEAR_APP_DATA.equals(action)) {
					if (hasExtras) {
						String packageName = extras.getString("package");
						if (packageName != null) {
							clearAppData(packageName);
						}
					}
				} else if (ACTION_FORCE_STOP_PACKAGE.equals(action)) {
					if (hasExtras) {
						String packageName = extras.getString("package");
						if (packageName != null) {
							forceStopPackage(packageName);
						}
					}
				} else if (ACTION_DELETE_PACKAGE.equals(action)) {
					if (hasExtras) {
						String packageName = extras.getString("package");
						Integer flag = extras.getInt("flags");
						if (packageName != null) {
							if (flag != null) {
								int flags = flag;
								deletePackage(packageName, flags);
							} else {
								deletePackage(packageName, 0);
							}
						}
					}
				} else if (ACTION_CLEAR_APP_CACHE.equals(action)) {
					if (hasExtras) {
						String packageName = extras.getString("package");
						if (packageName != null) {
							clearAppCache(packageName);
						}
					}
				} else if (ACTION_MOVE_PACKAGE.equals(action)) {
					if (hasExtras) {
						String packageName = extras.getString("package");
						Integer flag = extras.getInt("flags");
						if (packageName != null) {
							if (flag != null) {
								int flags = flag;
								movePackage(packageName, flags);
							}
						}
					}
				} else if (ACTION_RUN_XINSTALLER.equals(action)) {
					runXInstaller();
				}
			}

		};

>>>>>>> 7a87eb664fb84695567f5a467d0a129c32b85e7a
		// checks

		JB_MR1_NEWER = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) ? true
				: false;
		JB_MR2_NEWER = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) ? true
				: false;
		KITKAT_NEWER = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) ? true
				: false;

		// enablers

<<<<<<< HEAD
		findAndHookMethod(packageManagerService, null, "compareSignatures",
				Signature[].class, Signature[].class, compareSignaturesHook);

		if (JB_MR1_NEWER) {
			findAndHookMethod(packageManagerService, null,
=======
		XposedBridge.hookAllConstructors(packageManagerClass,
				packageManagerHook);

		XposedBridge.hookAllConstructors(activityManagerClass,
				activityManagerHook);

		XposedHelpers.findAndHookMethod(packageManagerClass,
				"compareSignatures", Signature[].class, Signature[].class,
				checkSignaturesHook);

		XposedHelpers.findAndHookMethod(packageManagerClass, "checkSignatures",
				String.class, String.class, checkSignaturesHook);

		XposedHelpers
				.findAndHookMethod(packageManagerClass, "checkUidSignatures",
						int.class, int.class, checkSignaturesHook);

		XposedHelpers.findAndHookMethod(packageManagerClass, "checkPermission",
				String.class, String.class, checkPermissionsHook);

		XposedHelpers.findAndHookMethod(packageManagerClass,
				"checkUidPermission", String.class, int.class,
				checkPermissionsHook);

		if (JB_MR1_NEWER) {
			XposedHelpers.findAndHookMethod(packageManagerClass,
>>>>>>> 7a87eb664fb84695567f5a467d0a129c32b85e7a
					"installPackageWithVerificationAndEncryption", Uri.class,
					"android.content.pm.IPackageInstallObserver", int.class,
					String.class, "android.content.pm.VerificationParams",
					"android.content.pm.ContainerEncryptionParams",
					installPackageHook);
		} else {
<<<<<<< HEAD
			findAndHookMethod(packageManagerService, null,
=======
			XposedHelpers.findAndHookMethod(packageManagerClass,
>>>>>>> 7a87eb664fb84695567f5a467d0a129c32b85e7a
					"installPackageWithVerification", Uri.class,
					"android.content.pm.IPackageInstallObserver", int.class,
					String.class, Uri.class,
					"android.content.pm.ManifestDigest",
					"android.content.pm.ContainerEncryptionParams",
					installPackageHook);
		}

		if (JB_MR2_NEWER) {
<<<<<<< HEAD
			findAndHookMethod(packageManagerService, null,
=======
			XposedHelpers.findAndHookMethod(packageManagerClass,
>>>>>>> 7a87eb664fb84695567f5a467d0a129c32b85e7a
					"deletePackageAsUser", String.class,
					"android.content.pm.IPackageDeleteObserver", int.class,
					int.class, deletePackageHook);
		} else {
<<<<<<< HEAD
			findAndHookMethod(packageManagerService, null, "deletePackage",
					String.class, "android.content.pm.IPackageDeleteObserver",
					int.class, deletePackageHook);
=======
			XposedHelpers.findAndHookMethod(packageManagerClass,
					"deletePackage", String.class,
					"android.content.pm.IPackageDeleteObserver", int.class,
					deletePackageHook);
>>>>>>> 7a87eb664fb84695567f5a467d0a129c32b85e7a
		}

		if (JB_MR1_NEWER) {
			findAndHookMethod(devicePolicyManager, null,
					"packageHasActiveAdmins", String.class, int.class,
					deviceAdminsHook);
		} else {
			findAndHookMethod(devicePolicyManager, null,
					"packageHasActiveAdmins", String.class, deviceAdminsHook);
		}
	}

	@Override
	public void handleLoadPackage(final LoadPackageParam lpparam)
			throws Throwable {
<<<<<<< HEAD
		String PACKAGEINSTALLER_PKG = "com.android.packageinstaller";
		String SETTINGS_PKG = "com.android.settings";
		String FDROID_PKG = "org.fdroid.fdroid";
		String installedAppDetails = "com.android.settings.applications.InstalledAppDetails";
		String packageInstallerActivity = "com.android.packageinstaller.PackageInstallerActivity";
		String installAppProgress = "com.android.packageinstaller.InstallAppProgress";
		String uninstallerActivity = "com.android.packageinstaller.UninstallerActivity";
		String uninstallAppProgress = "com.android.packageinstaller.UninstallAppProgress";
		String fDroidAppDetails = "org.fdroid.fdroid.AppDetails";

=======
>>>>>>> 7a87eb664fb84695567f5a467d0a129c32b85e7a
		if (PACKAGEINSTALLER_PKG.equals(lpparam.packageName)) {
			XposedHelpers.findAndHookMethod(packageInstallerActivity,
					lpparam.classLoader, "isInstallingUnknownAppsAllowed",
					unknownAppsHook);
			if (KITKAT_NEWER) {
				findAndHookMethod(packageInstallerActivity,
						lpparam.classLoader, "isVerifyAppsEnabled",
						verifyAppsHook);
			}
			XposedHelpers
					.findAndHookMethod(packageInstallerActivity,
							lpparam.classLoader, "startInstallConfirm",
							autoInstallHook);
<<<<<<< HEAD
			findAndHookMethod(uninstallerActivity, lpparam.classLoader,
					"onCreate", Bundle.class, autoUninstallHook);
			findAndHookMethod(uninstallAppProgress, lpparam.classLoader,
					"initView", autoCloseUninstallHook);
			findAndHookMethod(installAppProgress + "$1", lpparam.classLoader,
					"handleMessage", Message.class, autoCloseInstallHook);
=======
			XposedHelpers.findAndHookMethod(uninstallerActivity,
					lpparam.classLoader, "onCreate", Bundle.class,
					autoUninstallHook);
			XposedHelpers.findAndHookMethod(uninstallAppProgress,
					lpparam.classLoader, "initView", autoCloseUninstallHook);
			XposedHelpers.findAndHookMethod(installAppProgress + "$1",
					lpparam.classLoader, "handleMessage", Message.class,
					autoCloseInstallHook);
>>>>>>> 7a87eb664fb84695567f5a467d0a129c32b85e7a
		}

		if (SETTINGS_PKG.equals(lpparam.packageName)) {
			findAndHookMethod(installedAppDetails, lpparam.classLoader,
					"isThisASystemPackage", systemAppsHook);
		}

		if (FDROID_PKG.equals(lpparam.packageName)) {
<<<<<<< HEAD
			findAndHookMethod(fDroidAppDetails, lpparam.classLoader, "install",
					"org.fdroid.fdroid.data.Apk", fDroidInstallHook);
=======
			XposedHelpers.findAndHookMethod(fDroidAppDetails,
					lpparam.classLoader, "install",
					"org.fdroid.fdroid.data.Apk", fDroidInstallHook);
		}
	}

	// system API

	public static void forceStopPackage(String packageName) {
		XposedHelpers.callMethod(activityManagerObj, "forceStopPackage",
				packageName);
	}

	public static void clearAppData(String packageName) {
		XposedHelpers.callMethod(activityManagerObj,
				"clearApplicationUserData", packageName, null);
	}

	public static void clearAppCache(String packageName) {
		XposedHelpers.callMethod(packageManagerObj,
				"deleteApplicationCacheFiles", packageName, null);
	}

	public static void movePackage(String packageName, int flags) {
		XposedHelpers.callMethod(packageManagerObj, "movePackage", packageName,
				null, flags);
	}

	public static void installPackage(String apkFile, int flags) {
		Uri apk = Uri.fromFile(new File(apkFile));
		enableModule(false);
		XposedHelpers.callMethod(packageManagerObj, "installPackage", apk,
				null, flags);
		enableModule(true);

	}

	public static void deletePackage(String packageName, int flags) {
		enableModule(false);
		if (JB_MR2_NEWER) {
			int userId = -2; // USER_CURRENT
			XposedHelpers.callMethod(packageManagerObj, "deletePackageAsUser",
					packageName, null, userId, flags);
		} else {
			XposedHelpers.callMethod(packageManagerObj, "deletePackage",
					packageName, null, flags);
>>>>>>> 7a87eb664fb84695567f5a467d0a129c32b85e7a
		}
		enableModule(true);
	}

	public static void disableSignatureCheck(boolean disabled) {
		try {
			Context PACKAGE_CONTEXT = mContext.createPackageContext(
					PACKAGE_NAME, Context.CONTEXT_IGNORE_SECURITY);
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(PACKAGE_CONTEXT);
			prefs.edit().putBoolean(PREF_DISABLE_SIGNATURE_CHECK, disabled)
					.apply();
		} catch (NameNotFoundException e) {
		}
	}

	public static void disablePermissionCheck(boolean disabled) {
		try {
			Context PACKAGE_CONTEXT = mContext.createPackageContext(
					PACKAGE_NAME, Context.CONTEXT_IGNORE_SECURITY);
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(PACKAGE_CONTEXT);
			prefs.edit().putBoolean(PREF_DISABLE_PERMISSION_CHECK, disabled)
					.apply();
		} catch (NameNotFoundException e) {
		}
	}

	public static void runXInstaller() {
		Intent launchIntent = mContext.getPackageManager()
				.getLaunchIntentForPackage(PACKAGE_NAME);
		if (launchIntent != null) {
			launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mContext.startActivity(launchIntent);
		}
	}

	public static boolean isModuleEnabled() {
		boolean enabled;
		prefs.reload();
		enabled = prefs.getBoolean(PREF_ENABLE_MODULE, true);
		return enabled;
	}

<<<<<<< HEAD
}
=======
	public static void enableModule(boolean enabled) {
		try {
			Context PACKAGE_CONTEXT = mContext.createPackageContext(
					PACKAGE_NAME, Context.CONTEXT_IGNORE_SECURITY);
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(PACKAGE_CONTEXT);
			prefs.edit().putBoolean(PREF_ENABLE_MODULE, enabled).apply();
		} catch (NameNotFoundException e) {
		}
	}
}
>>>>>>> 7a87eb664fb84695567f5a467d0a129c32b85e7a
