package com.pyler.xinstaller;

import java.io.File;
import java.security.cert.Certificate;
import java.util.Hashtable;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class XInstaller implements IXposedHookZygoteInit,
		IXposedHookLoadPackage {
	public XSharedPreferences prefs;
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
	public boolean backupApkFiles;
	public boolean installUnsignedApps;
	public boolean verifyJar;
	public boolean verifySignature;
	public boolean enableShowButtons;
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
	public XC_MethodHook installUnsignedAppsHook;
	public XC_MethodHook verifyJarHook;
	public XC_MethodHook verifySignatureHook;
	public XC_MethodHook enableShowButtonsHook;
	public boolean JB_MR2_NEWER;
	public boolean JB_MR1_NEWER;
	public boolean KITKAT_NEWER;
	public boolean APIEnabled;
	public Context mContext;
	public Object packageManagerObj;
	public Object activityManagerObj;
	public BroadcastReceiver systemAPI;

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
	public static final String ACTION_REMOVE_TASK = "xinstaller.intent.action.REMOVE_TASK";

	public static final String FILE = "file";
	public static final String FLAGS = "flags";
	public static final String PACKAGE = "package";
	public static final String TASK = "task";

	// utils
	public static final String ACTION_BACKUP_APK_FILE = "xinstaller.intent.action.BACKUP_APK_FILE";
	public static final String ACTION_SET_PREFERENCE = "xinstaller.intent.action.SET_PREFERENCE";

	public static final String APK_FILE = "apk_file";
	public static final String PREFERENCE = "preference";
	public static final String VALUE = "value";

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
	public static final String PREF_ENABLE_BACKUP_APP_PACKAGE = "enable_backup_app_packages";
	public static final String PREF_ENABLE_BACKUP_APK_FILE = "enable_backup_apk_files";
	public static final String PREF_ENABLE_INSTALL_UNSIGNED_APP = "enable_install_unsigned_apps";
	public static final String PREF_DISABLE_VERIFY_JAR = "disable_verify_jar";
	public static final String PREF_DISABLE_VERIFY_SIGNATURE = "disable_verify_signatures";
	public static final String PREF_ENABLE_SHOW_BUTTON = "enable_show_buttons";

	// constants
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
	public static final String packageParser = "android.content.pm.PackageParser";
	public static final String jarVerifier = "java.util.jar.JarVerifier$VerifierEntry";
	public static final String signature = "java.security.Signature";
	public static final String androidSystem = "android";

	// classes
	public Class<?> packageManagerClass = XposedHelpers.findClass(
			packageManagerService, null);
	public Class<?> activityManagerClass = XposedHelpers.findClass(
			activityManager, null);
	public Class<?> devicePolicyManagerClass = XposedHelpers.findClass(
			devicePolicyManager, null);
	public Class<?> packageParserClass = XposedHelpers.findClass(packageParser,
			null);
	public Class<?> jarVerifierClass = XposedHelpers.findClass(jarVerifier,
			null);
	public Class<?> signatureClass = XposedHelpers.findClass(signature, null);

	// flags
	public static final int DELETE_KEEP_DATA = 0x00000001;
	public static final int INSTALL_ALLOW_DOWNGRADE = 0x00000080;
	public static final int INSTALL_FORWARD_LOCK = 0x00000001;
	public static final int INSTALL_EXTERNAL = 0x00000008;
	public static final int INSTALL_REPLACE_EXISTING = 0x00000002;
	public static final int REMOVE_TASK_KILL_PROCESS = 0x0001;

	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
		prefs = new XSharedPreferences(XInstaller.class.getPackage().getName());
		prefs.makeWorldReadable();
		APIEnabled = false;

		// hooks
		packageManagerHook = new XC_MethodHook() {
			@Override
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
						IntentFilter systemApi = new IntentFilter();
						systemApi.addAction(ACTION_DISABLE_SIGNATURE_CHECK);
						systemApi.addAction(ACTION_ENABLE_SIGNATURE_CHECK);
						systemApi.addAction(ACTION_DISABLE_PERMISSION_CHECK);
						systemApi.addAction(ACTION_ENABLE_PERMISSION_CHECK);
						systemApi.addAction(ACTION_INSTALL_PACKAGE);
						systemApi.addAction(ACTION_CLEAR_APP_DATA);
						systemApi.addAction(ACTION_FORCE_STOP_PACKAGE);
						systemApi.addAction(ACTION_DELETE_PACKAGE);
						systemApi.addAction(ACTION_CLEAR_APP_CACHE);
						systemApi.addAction(ACTION_MOVE_PACKAGE);
						systemApi.addAction(ACTION_RUN_XINSTALLER);
						systemApi.addAction(ACTION_REMOVE_TASK);
						mContext.registerReceiver(systemAPI, systemApi);
						APIEnabled = true;

						// Utils
						IntentFilter appApi = new IntentFilter();
						appApi.addAction(ACTION_BACKUP_APK_FILE);
						appApi.addAction(ACTION_SET_PREFERENCE);
						Utils utils = new Utils();
						getXInstallerContext().registerReceiver(utils, appApi);
					}
				}
			}
		};

		enableShowButtonsHook = new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param)
					throws Throwable {
				prefs.reload();
				enableShowButtons = prefs.getBoolean(PREF_ENABLE_SHOW_BUTTON,
						false);
				if (isModuleEnabled() && enableShowButtons) {
					param.setResult(true);
				}
			}
		};

		verifyJarHook = new XC_MethodHook() {
			@SuppressWarnings("unchecked")
			@Override
			protected void afterHookedMethod(MethodHookParam param)
					throws Throwable {
				prefs.reload();
				verifyJar = prefs.getBoolean(PREF_DISABLE_VERIFY_JAR, false);
				if (isModuleEnabled() && verifyJar) {
					String name = (String) XposedHelpers.getObjectField(
							param.thisObject, "name");
					Certificate[] certificates = (Certificate[]) XposedHelpers
							.getObjectField(param.thisObject, "certificates");
					Hashtable<String, Certificate[]> verifiedEntries = null;
					try {
						verifiedEntries = (Hashtable<String, Certificate[]>) XposedHelpers
								.findField(param.thisObject.getClass(),
										"verifiedEntries")
								.get(param.thisObject);
					} catch (NoSuchFieldError e) {
						verifiedEntries = (Hashtable<String, Certificate[]>) XposedHelpers
								.getObjectField(XposedHelpers
										.getSurroundingThis(param.thisObject),
										"verifiedEntries");
					}
					verifiedEntries.put(name, certificates);
					param.setResult(null);
				}
			}
		};

		verifySignatureHook = new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param)
					throws Throwable {
				prefs.reload();
				verifySignature = prefs.getBoolean(
						PREF_DISABLE_VERIFY_SIGNATURE, false);
				if (isModuleEnabled() && verifySignature) {
					param.setResult(true);
				}
			}
		};

		installUnsignedAppsHook = new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param)
					throws Throwable {
				prefs.reload();
				installUnsignedApps = prefs.getBoolean(
						PREF_ENABLE_INSTALL_UNSIGNED_APP, false);
				if (isModuleEnabled() && installUnsignedApps) {
					param.setResult(true);
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
						PREF_ENABLE_INSTALL_EXTERNAL_STORAGE, false);
				backupApkFiles = prefs.getBoolean(PREF_ENABLE_BACKUP_APK_FILE,
						false);
				int ID = JB_MR1_NEWER ? 2 : 1;
				int flags = (Integer) param.args[ID];
				if (isModuleEnabled() && (flags & INSTALL_ALLOW_DOWNGRADE) == 0
						&& downgradeApps) {
					// we dont have this flag, add it
					flags |= INSTALL_ALLOW_DOWNGRADE;
				}
				if (isModuleEnabled() && (flags & INSTALL_FORWARD_LOCK) != 0
						&& forwardLock) {
					// we have this flag, remove it
					flags &= ~INSTALL_FORWARD_LOCK;
				}
				if (isModuleEnabled() && (flags & INSTALL_EXTERNAL) == 0
						&& installAppsOnExternal) {
					// we dont have this flag, add it
					flags |= INSTALL_EXTERNAL;
				}
				param.args[ID] = flags;
				if (isModuleEnabled() && backupApkFiles) {
					Uri packageUri = (Uri) param.args[0];
					String apkFile = packageUri.getPath();
					backupApkFile(apkFile);
				}
			}

		};

		deletePackageHook = new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param)
					throws Throwable {
				prefs.reload();
				keepAppsData = prefs.getBoolean(PREF_ENABLE_KEEP_APP_DATA,
						false);
				int ID = JB_MR2_NEWER ? 3 : 2;
				int flags = (Integer) param.args[ID];
				if (isModuleEnabled() && (flags & DELETE_KEEP_DATA) == 0
						&& keepAppsData) {
					// we dont have this flag, add it
					flags |= DELETE_KEEP_DATA;
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
				mInstalledSigID = (String) XposedHelpers.getObjectField(
						param.thisObject, "mInstalledSigID");
				if (isModuleEnabled() && signaturesCheckFDroid) {
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
				autoInstall = prefs.getBoolean(PREF_ENABLE_AUTO_INSTALL, true);
				Button mOk = (Button) XposedHelpers.getObjectField(
						param.thisObject, "mOk");
				if (isModuleEnabled() && autoInstall) {
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
				autoUninstall = prefs.getBoolean(PREF_ENABLE_AUTO_UNINSTALL,
						true);
				Button mOk = (Button) XposedHelpers.getObjectField(
						param.thisObject, "mOk");
				if (isModuleEnabled() && autoUninstall) {
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
						PREF_ENABLE_AUTO_CLOSE_UNINSTALL, true);
				Button mOk = (Button) XposedHelpers.getObjectField(
						param.thisObject, "mOkButton");
				if (isModuleEnabled() && autoCloseUninstall) {
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
						PREF_ENABLE_AUTO_CLOSE_INSTALL, true);
				autoLaunchInstall = prefs.getBoolean(
						PREF_ENABLE_LAUNCH_INSTALL, false);
				Button mDone = (Button) XposedHelpers.getObjectField(
						XposedHelpers.getSurroundingThis(param.thisObject),
						"mDoneButton");

				if (isModuleEnabled() && autoCloseInstall) {
					mDone.performClick();
				}
				Button mLaunch = (Button) XposedHelpers.getObjectField(
						XposedHelpers.getSurroundingThis(param.thisObject),
						"mLaunchButton");
				if (isModuleEnabled() && autoLaunchInstall) {
					mLaunch.performClick();
				}
			}

		};

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
						String apkFile = extras.getString(FILE);
						Integer flag = extras.getInt(FLAGS);
						if (apkFile != null) {
							if (flag != null) {
								int flags = flag;
								installPackage(apkFile, flags);
							} else {
								installPackage(apkFile, 0);
							}
						}
					}
				} else if (ACTION_CLEAR_APP_DATA.equals(action)) {
					if (hasExtras) {
						String packageName = extras.getString(PACKAGE);
						if (packageName != null) {
							clearAppData(packageName);
						}
					}
				} else if (ACTION_FORCE_STOP_PACKAGE.equals(action)) {
					if (hasExtras) {
						String packageName = extras.getString(PACKAGE);
						if (packageName != null) {
							forceStopPackage(packageName);
						}
					}
				} else if (ACTION_DELETE_PACKAGE.equals(action)) {
					if (hasExtras) {
						String packageName = extras.getString(PACKAGE);
						Integer flag = extras.getInt(FLAGS);
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
						String packageName = extras.getString(PACKAGE);
						if (packageName != null) {
							clearAppCache(packageName);
						}
					}
				} else if (ACTION_MOVE_PACKAGE.equals(action)) {
					if (hasExtras) {
						String packageName = extras.getString(PACKAGE);
						Integer flag = extras.getInt(FLAGS);
						if (packageName != null) {
							if (flag != null) {
								int flags = flag;
								movePackage(packageName, flags);
							}
						}
					}
				} else if (ACTION_RUN_XINSTALLER.equals(action)) {
					runXInstaller();
				} else if (ACTION_REMOVE_TASK.equals(action)) {
					if (hasExtras) {
						Integer taskId = extras.getInt(TASK);
						if (taskId != null) {
							int task = taskId;
							removeTask(task);
						}
					}
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
		XposedHelpers.findAndHookMethod(View.class,
				"onFilterTouchEventForSecurity", MotionEvent.class,
				enableShowButtonsHook);

		XposedHelpers.findAndHookMethod(packageManagerClass,
				"isVerificationEnabled", int.class, verifyAppsHook);

		XposedHelpers.findAndHookMethod(signatureClass, "verify", byte[].class,
				int.class, int.class, verifySignatureHook);

		XposedHelpers.findAndHookMethod(signatureClass, "verify", byte[].class,
				verifySignatureHook);

		XposedHelpers.findAndHookMethod(jarVerifierClass, "verify",
				verifyJarHook);

		XposedHelpers.findAndHookMethod(packageParserClass,
				"collectCertificates",
				"android.content.pm.PackageParser$Package", int.class,
				installUnsignedAppsHook);

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
					"installPackageWithVerificationAndEncryption", Uri.class,
					"android.content.pm.IPackageInstallObserver", int.class,
					String.class, "android.content.pm.VerificationParams",
					"android.content.pm.ContainerEncryptionParams",
					installPackageHook);
		} else {
			XposedHelpers.findAndHookMethod(packageManagerClass,
					"installPackageWithVerification", Uri.class,
					"android.content.pm.IPackageInstallObserver", int.class,
					String.class, Uri.class,
					"android.content.pm.ManifestDigest",
					"android.content.pm.ContainerEncryptionParams",
					installPackageHook);
		}

		if (JB_MR2_NEWER) {
			XposedHelpers.findAndHookMethod(packageManagerClass,
					"deletePackageAsUser", String.class,
					"android.content.pm.IPackageDeleteObserver", int.class,
					int.class, deletePackageHook);
		} else {
			XposedHelpers.findAndHookMethod(packageManagerClass,
					"deletePackage", String.class,
					"android.content.pm.IPackageDeleteObserver", int.class,
					deletePackageHook);
		}

		if (JB_MR1_NEWER) {
			XposedHelpers.findAndHookMethod(devicePolicyManagerClass,
					"packageHasActiveAdmins", String.class, int.class,
					deviceAdminsHook);
		} else {
			XposedHelpers.findAndHookMethod(devicePolicyManagerClass,
					"packageHasActiveAdmins", String.class, deviceAdminsHook);
		}
	}

	@Override
	public void handleLoadPackage(final LoadPackageParam lpparam)
			throws Throwable {
		if (PACKAGEINSTALLER_PKG.equals(lpparam.packageName)) {
			XposedHelpers.findAndHookMethod(packageInstallerActivity,
					lpparam.classLoader, "isInstallingUnknownAppsAllowed",
					unknownAppsHook);
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
			XposedHelpers.findAndHookMethod(installAppProgress + "$1",
					lpparam.classLoader, "handleMessage", Message.class,
					autoCloseInstallHook);
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

	// system API

	public void forceStopPackage(String packageName) {
		XposedHelpers.callMethod(activityManagerObj, "forceStopPackage",
				packageName);
	}

	public void clearAppData(String packageName) {
		XposedHelpers.callMethod(activityManagerObj,
				"clearApplicationUserData", packageName, null);
	}

	public void removeTask(int taskId) {
		XposedHelpers.callMethod(activityManagerObj, "removeTask", taskId,
				REMOVE_TASK_KILL_PROCESS);
	}

	public void clearAppCache(String packageName) {
		XposedHelpers.callMethod(packageManagerObj,
				"deleteApplicationCacheFiles", packageName, null);
	}

	public void movePackage(String packageName, int flags) {
		XposedHelpers.callMethod(packageManagerObj, "movePackage", packageName,
				null, flags);
	}

	public void installPackage(String apkFile, int flags) {
		Uri apk = Uri.fromFile(new File(apkFile));
		enableModule(false);
		if ((flags & INSTALL_REPLACE_EXISTING) == 0) {
			flags |= INSTALL_REPLACE_EXISTING;
		}
		XposedHelpers.callMethod(packageManagerObj, "installPackage", apk,
				null, flags);
		enableModule(true);

	}

	public void deletePackage(String packageName, int flags) {
		enableModule(false);
		if (JB_MR2_NEWER) {
			int userId = (Integer) XposedHelpers.callStaticMethod(
					ActivityManager.class, "getCurrentUser");
			XposedHelpers.callMethod(packageManagerObj, "deletePackageAsUser",
					packageName, null, userId, flags);
		} else {
			XposedHelpers.callMethod(packageManagerObj, "deletePackage",
					packageName, null, flags);
		}
		enableModule(true);
	}

	public void disableSignatureCheck(boolean disabled) {
		Intent disableSignatureCheck = new Intent(ACTION_SET_PREFERENCE);
		disableSignatureCheck.setPackage(PACKAGE_NAME);
		disableSignatureCheck
				.putExtra(PREFERENCE, PREF_DISABLE_SIGNATURE_CHECK);
		disableSignatureCheck.putExtra(VALUE, disabled);
		mContext.sendBroadcast(disableSignatureCheck);

	}

	public void disablePermissionCheck(boolean disabled) {
		Intent disablePermissionCheck = new Intent(ACTION_SET_PREFERENCE);
		disablePermissionCheck.setPackage(PACKAGE_NAME);
		disablePermissionCheck.putExtra(PREFERENCE,
				PREF_DISABLE_PERMISSION_CHECK);
		disablePermissionCheck.putExtra(VALUE, disabled);
		mContext.sendBroadcast(disablePermissionCheck);

	}

	public void runXInstaller() {
		Intent launchIntent = mContext.getPackageManager()
				.getLaunchIntentForPackage(PACKAGE_NAME);
		if (launchIntent != null) {
			launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mContext.startActivity(launchIntent);
		}
	}

	public boolean isModuleEnabled() {
		prefs.reload();
		boolean enabled = prefs.getBoolean(PREF_ENABLE_MODULE, true);
		return enabled;
	}

	public void enableModule(boolean enabled) {
		Intent enableModule = new Intent(ACTION_SET_PREFERENCE);
		enableModule.setPackage(PACKAGE_NAME);
		enableModule.putExtra(PREFERENCE, PREF_ENABLE_MODULE);
		enableModule.putExtra(VALUE, enabled);
		mContext.sendBroadcast(enableModule);

	}

	public Context getXInstallerContext() {
		Context XInstallerContext;
		try {
			XInstallerContext = mContext.createPackageContext(PACKAGE_NAME, 0);
		} catch (NameNotFoundException e) {
			XInstallerContext = null;
		}
		return XInstallerContext;
	}

	public void backupApkFile(String apkFile) {
		Intent backupApkFile = new Intent(ACTION_BACKUP_APK_FILE);
		backupApkFile.setPackage(PACKAGE_NAME);
		backupApkFile.putExtra(APK_FILE, apkFile);
		mContext.sendBroadcast(backupApkFile);

	}
}
