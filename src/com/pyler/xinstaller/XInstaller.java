package com.pyler.xinstaller;

import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.util.Hashtable;

import android.app.ActivityManager;
import android.app.AndroidAppHelper;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.os.Process;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class XInstaller implements IXposedHookZygoteInit,
		IXposedHookLoadPackage {
	public XSharedPreferences prefs;
	public boolean checkSignatures;
	public boolean checkSignaturesFDroid;
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
	public boolean checkPermissions;
	public boolean backupApkFiles;
	public boolean installUnsignedApps;
	public boolean verifyJar;
	public boolean verifySignature;
	public boolean showButtons;
	public boolean debugApps;
	public boolean autoBackup;
	public boolean showPackageName;
	public boolean showVersions;
	public boolean deleteApkFiles;
	public boolean moveApps;
	public boolean checkSdkVersion;
	public boolean installBackground;
	public boolean uninstallBackground;
	public boolean launchApps;
	public boolean checkDuplicatedPermissions;
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
	public XC_MethodHook checkPermissionsHook;
	public XC_MethodHook installUnsignedAppsHook;
	public XC_MethodHook verifyJarHook;
	public XC_MethodHook verifySignatureHook;
	public XC_MethodHook showButtonsHook;
	public XC_MethodHook debugAppsHook;
	public XC_MethodHook autoBackupHook;
	public XC_MethodHook showPackageNameHook;
	public XC_MethodHook scanPackageHook;
	public XC_MethodHook verifySignaturesHook;
	public XC_MethodHook moveAppsHook;
	public XC_MethodHook checkSdkVersionHook;
	public XC_MethodHook installBackgroundHook;
	public XC_MethodHook uninstallBackgroundHook;
	public XC_MethodHook checkDuplicatedPermissionsHook;
	public boolean JB_NEWER = (Common.SDK >= Build.VERSION_CODES.JELLY_BEAN) ? true
			: false;
	public boolean JB_MR1_NEWER = (Common.SDK >= Build.VERSION_CODES.JELLY_BEAN_MR1) ? true
			: false;
	public boolean JB_MR2_NEWER = (Common.SDK >= Build.VERSION_CODES.JELLY_BEAN_MR2) ? true
			: false;
	public boolean KITKAT_NEWER = (Common.SDK >= Build.VERSION_CODES.KITKAT) ? true
			: false;
	public boolean LOLLIPOP_NEWER = (Common.SDK >= Build.VERSION_CODES.LOLLIPOP) ? true
			: false;
	public boolean signatureCheckOff;
	public Context mContext;

	// classes
	public Class<?> packageManagerClass = XposedHelpers.findClass(
			Common.PACKAGEMANAGERSERVICE, null);
	public Class<?> activityManagerClass = ActivityManager.class;
	public Class<?> devicePolicyManagerClass = XposedHelpers.findClass(
			Common.DEVICEPOLICYMANAGERSERVICE, null);
	public Class<?> packageParserClass = XposedHelpers.findClass(
			Common.PACKAGEPARSER, null);
	public Class<?> jarVerifierClass = XposedHelpers.findClass(
			Common.JARVERIFIER, null);
	public Class<?> signatureClass = XposedHelpers.findClass(Common.SIGNATURE,
			null);

	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
		prefs = new XSharedPreferences(XInstaller.class.getPackage().getName());
		prefs.makeWorldReadable();
		signatureCheckOff = true;

		if (LOLLIPOP_NEWER && !isExpertModeEnabled()) {
			return;
		}

		// hooks
		checkDuplicatedPermissionsHook = new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param)
					throws Throwable {
				prefs.reload();
				checkDuplicatedPermissions = prefs.getBoolean(
						Common.PREF_DISABLE_CHECK_DUPLICATED_PERMISSION, false);
				if (isModuleEnabled() && checkDuplicatedPermissions) {
					param.setResult(true);
					return;
				}
			}
		};

		checkSdkVersionHook = new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param)
					throws Throwable {
				prefs.reload();
				checkSdkVersion = prefs.getBoolean(
						Common.PREF_DISABLE_CHECK_SDK_VERSION, false);
				if (isModuleEnabled() && checkSdkVersion) {
					XposedHelpers.setObjectField(param.thisObject,
							"SDK_VERSION", Common.LATEST_ANDROID_RELEASE);
				}
			}
		};

		moveAppsHook = new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param)
					throws Throwable {
				prefs.reload();
				moveApps = prefs.getBoolean(Common.PREF_ENABLE_MOVE_APP, false);
				if (isModuleEnabled() && moveApps) {
					param.setResult(true);
					return;
				}
			}
		};

		verifySignaturesHook = new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param)
					throws Throwable {
				prefs.reload();
				checkSignatures = prefs.getBoolean(
						Common.PREF_DISABLE_CHECK_SIGNATURE, false);
				if (isModuleEnabled() && checkSignatures) {
					param.setResult(true);
					return;
				}
			}
		};

		scanPackageHook = new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param)
					throws Throwable {
				signatureCheckOff = false;
			}

			@Override
			protected void afterHookedMethod(MethodHookParam param)
					throws Throwable {
				signatureCheckOff = true;
			}
		};

		showPackageNameHook = new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param)
					throws Throwable {
				prefs.reload();
				showPackageName = prefs.getBoolean(
						Common.PREF_ENABLE_SHOW_PACKAGE_NAME, false);
				launchApps = prefs.getBoolean(Common.PREF_ENABLE_LAUNCH_APP,
						false);
				mContext = AndroidAppHelper.currentApplication();
				PackageInfo pkgInfo = (PackageInfo) param.args[0];
				TextView appVersion = (TextView) XposedHelpers.getObjectField(
						param.thisObject, "mAppVersion");
				View mRootView = (View) XposedHelpers.getObjectField(
						param.thisObject, "mRootView");
				Resources mResources = mRootView.getResources();
				int appSnippetId = mResources.getIdentifier("app_snippet",
						"id", Common.SETTINGS_PKG);
				View appSnippet = mRootView.findViewById(appSnippetId);
				int iconId = mResources.getIdentifier("app_icon", "id",
						Common.SETTINGS_PKG);
				ImageView appIcon = (ImageView) appSnippet.findViewById(iconId);
				String version = appVersion.getText().toString();
				final String packageName = pkgInfo.packageName;
				if (isModuleEnabled() && showPackageName) {
					appVersion.setText(packageName + "\n" + version);
					appVersion.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							ClipboardManager clipboard = (ClipboardManager) mContext
									.getSystemService(Context.CLIPBOARD_SERVICE);
							Resources res = getXInstallerContext()
									.getResources();
							ClipData clip = ClipData.newPlainText("text",
									packageName);
							clipboard.setPrimaryClip(clip);
							Toast.makeText(
									mContext,
									res.getString(R.string.package_name_copied),
									Toast.LENGTH_SHORT).show();

						}
					});

				}

				if (isModuleEnabled() && launchApps) {
					appIcon.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							Intent launchIntent = mContext.getPackageManager()
									.getLaunchIntentForPackage(packageName);
							if (launchIntent != null) {
								launchIntent
										.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								mContext.startActivity(launchIntent);
							}
						}
					});
				}
			}
		};

		autoBackupHook = new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param)
					throws Throwable {
				prefs.reload();
				autoBackup = prefs.getBoolean(Common.PREF_ENABLE_AUTO_BACKUP,
						false);
				Button mAllowButton = (Button) XposedHelpers.getObjectField(
						param.thisObject, "mAllowButton");
				if (isModuleEnabled() && autoBackup) {
					mAllowButton.performClick();
				}
			}
		};

		debugAppsHook = new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param)
					throws Throwable {
				prefs.reload();
				debugApps = prefs.getBoolean(Common.PREF_ENABLE_DEBUG_APP,
						false);
				int flags = (Integer) param.args[5];
				if (isModuleEnabled() && isExpertModeEnabled()
						&& (flags & Common.DEBUG_ENABLE_DEBUGGER) == 0
						&& debugApps) {
					// we dont have this flag, add it
					flags |= Common.DEBUG_ENABLE_DEBUGGER;
				}
				param.args[5] = flags;
			}
		};

		showButtonsHook = new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param)
					throws Throwable {
				prefs.reload();
				showButtons = prefs.getBoolean(Common.PREF_ENABLE_SHOW_BUTTON,
						false);
				if (isModuleEnabled() && showButtons) {
					param.setResult(true);
					return;
				}
			}
		};

		verifyJarHook = new XC_MethodHook() {
			@SuppressWarnings("unchecked")
			@Override
			protected void beforeHookedMethod(MethodHookParam param)
					throws Throwable {
				prefs.reload();
				verifyJar = prefs.getBoolean(Common.PREF_DISABLE_VERIFY_JAR,
						false);
				if (isModuleEnabled() && isExpertModeEnabled() && verifyJar) {
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
			protected void beforeHookedMethod(MethodHookParam param)
					throws Throwable {
				prefs.reload();
				verifySignature = prefs.getBoolean(
						Common.PREF_DISABLE_VERIFY_SIGNATURE, false);
				if (isModuleEnabled() && isExpertModeEnabled()
						&& verifySignature) {
					param.setResult(true);
					return;
				}
			}
		};

		installUnsignedAppsHook = new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param)
					throws Throwable {
				prefs.reload();
				installUnsignedApps = prefs.getBoolean(
						Common.PREF_ENABLE_INSTALL_UNSIGNED_APP, false);
				if (isModuleEnabled() && isExpertModeEnabled()
						&& installUnsignedApps) {
					param.setResult(true);
					return;
				}
			}
		};

		checkPermissionsHook = new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param)
					throws Throwable {
				prefs.reload();
				checkPermissions = prefs.getBoolean(
						Common.PREF_DISABLE_CHECK_PERMISSION, false);
				if (isModuleEnabled() && isExpertModeEnabled()
						&& checkPermissions) {
					param.setResult(PackageManager.PERMISSION_GRANTED);
					return;
				}
			}
		};

		checkSignaturesHook = new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param)
					throws Throwable {
				prefs.reload();
				checkSignatures = prefs.getBoolean(
						Common.PREF_DISABLE_CHECK_SIGNATURE, false);
				if (isModuleEnabled() && checkSignatures && signatureCheckOff) {
					param.setResult(PackageManager.SIGNATURE_MATCH);
					return;
				}
			}
		};

		installPackageHook = new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param)
					throws Throwable {
				prefs.reload();
				downgradeApps = prefs.getBoolean(
						Common.PREF_ENABLED_DOWNGRADE_APP, false);
				forwardLock = prefs.getBoolean(
						Common.PREF_DISABLE_FORWARD_LOCK, false);
				installAppsOnExternal = prefs.getBoolean(
						Common.PREF_ENABLE_INSTALL_EXTERNAL_STORAGE, false);
				backupApkFiles = prefs.getBoolean(
						Common.PREF_ENABLE_BACKUP_APK_FILE, false);
				installBackground = prefs.getBoolean(
						Common.PREF_DISABLE_INSTALL_BACKGROUND, false);
				int ID = JB_MR1_NEWER ? 2 : 1;
				int flags = (Integer) param.args[ID];
				if (isModuleEnabled()
						&& (flags & Common.INSTALL_ALLOW_DOWNGRADE) == 0
						&& downgradeApps) {
					// we dont have this flag, add it
					flags |= Common.INSTALL_ALLOW_DOWNGRADE;
				}
				if (isModuleEnabled()
						&& (flags & Common.INSTALL_FORWARD_LOCK) != 0
						&& forwardLock) {
					// we have this flag, remove it
					flags &= ~Common.INSTALL_FORWARD_LOCK;
				}
				if (isModuleEnabled() && isExpertModeEnabled()
						&& (flags & Common.INSTALL_EXTERNAL) == 0
						&& installAppsOnExternal) {
					// we dont have this flag, add it
					flags |= Common.INSTALL_EXTERNAL;
				}
				param.args[ID] = flags;

				if (isModuleEnabled() && backupApkFiles) {
					Uri packageUri = (Uri) param.args[0];
					String apkFile = packageUri.getPath();
					backupApkFile(apkFile);
				}

				if (isModuleEnabled() && installBackground) {
					if (Binder.getCallingUid() == Common.ROOT_UID) {
						param.setResult(null);
					}
				}
			}

		};

		deletePackageHook = new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param)
					throws Throwable {
				prefs.reload();
				keepAppsData = prefs.getBoolean(
						Common.PREF_ENABLE_KEEP_APP_DATA, false);
				uninstallBackground = prefs.getBoolean(
						Common.PREF_DISABLE_UNINSTALL_BACKGROUND, false);
				int ID = JB_MR2_NEWER ? 3 : 2;
				int flags = (Integer) param.args[ID];
				if (isModuleEnabled() && (flags & Common.DELETE_KEEP_DATA) == 0
						&& keepAppsData) {
					// we dont have this flag, add it
					flags |= Common.DELETE_KEEP_DATA;
				}
				param.args[ID] = flags;

				if (isModuleEnabled() && uninstallBackground) {
					if (Binder.getCallingUid() == Common.ROOT_UID) {
						param.setResult(null);
					}
				}
			}

		};

		systemAppsHook = new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param)
					throws Throwable {
				prefs.reload();
				disableSystemApps = prefs.getBoolean(
						Common.PREF_ENABLE_DISABLE_SYSTEM_APP, false);
				if (isModuleEnabled() && disableSystemApps) {
					param.setResult(false);
					return;
				}

			}

		};

		unknownAppsHook = new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param)
					throws Throwable {
				prefs.reload();
				installUnknownApps = prefs.getBoolean(
						Common.PREF_ENABLE_INSTALL_UNKNOWN_APP, false);
				if (isModuleEnabled() && installUnknownApps) {
					param.setResult(true);
					return;
				}

			}

		};

		verifyAppsHook = new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param)
					throws Throwable {
				prefs.reload();
				verifyApps = prefs.getBoolean(Common.PREF_DISABLE_VERIFY_APP,
						false);
				if (isModuleEnabled() && verifyApps) {
					param.setResult(false);
					return;
				}

			}

		};

		deviceAdminsHook = new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param)
					throws Throwable {
				prefs.reload();
				deviceAdmins = prefs.getBoolean(
						Common.PREF_ENABLE_UNINSTALL_DEVICE_ADMIN, false);
				if (isModuleEnabled() && deviceAdmins) {
					param.setResult(false);
					return;
				}

			}

		};

		fDroidInstallHook = new XC_MethodHook() {
			String mInstalledSigID = null;

			@Override
			protected void beforeHookedMethod(MethodHookParam param)
					throws Throwable {
				prefs.reload();
				checkSignaturesFDroid = prefs.getBoolean(
						Common.PREF_DISABLE_CHECK_SIGNATURE_FDROID, false);
				mInstalledSigID = (String) XposedHelpers.getObjectField(
						param.thisObject, "mInstalledSigID");
				if (isModuleEnabled() && checkSignaturesFDroid) {
					XposedHelpers.setObjectField(param.thisObject,
							"mInstalledSigID", null);
				}
			}

			@Override
			protected void afterHookedMethod(MethodHookParam param)
					throws Throwable {
				if (isModuleEnabled() && checkSignaturesFDroid) {
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
				autoInstall = prefs.getBoolean(Common.PREF_ENABLE_AUTO_INSTALL,
						false);
				showVersions = prefs.getBoolean(
						Common.PREF_ENABLE_SHOW_VERSION, false);
				mContext = AndroidAppHelper.currentApplication();
				Button mOk = (Button) XposedHelpers.getObjectField(
						param.thisObject, "mOk");
				if (isModuleEnabled() && autoInstall) {
					XposedHelpers.setObjectField(param.thisObject,
							"mScrollView", null);
					XposedHelpers.setBooleanField(param.thisObject,
							"mOkCanInstall", true);
					mOk.performClick();
				}
				if (isModuleEnabled() && showVersions) {
					Resources res = getXInstallerContext().getResources();
					PackageManager pm = mContext.getPackageManager();
					PackageInfo mPkgInfo = (PackageInfo) XposedHelpers
							.getObjectField(param.thisObject, "mPkgInfo");
					String packageName = mPkgInfo.packageName;
					String versionInfo = res.getString(R.string.new_version)
							+ ": " + mPkgInfo.versionName;
					try {
						PackageInfo pi = pm.getPackageInfo(packageName, 0);
						String currentVersion = pi.versionName;
						versionInfo += "\n"
								+ res.getString(R.string.current_version)
								+ ": " + currentVersion;

					} catch (PackageManager.NameNotFoundException e) {
					}
					Toast.makeText(mContext, versionInfo, Toast.LENGTH_LONG)
							.show();
				}
			}
		};

		autoUninstallHook = new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param)
					throws Throwable {
				prefs.reload();
				autoUninstall = prefs.getBoolean(
						Common.PREF_ENABLE_AUTO_UNINSTALL, false);
				Button mOk = (Button) XposedHelpers.getObjectField(
						param.thisObject, "mOk");
				if (isModuleEnabled() && autoUninstall) {
					if (mOk != null) {
						mOk.performClick();
					}
				}
			}

		};

		autoCloseUninstallHook = new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param)
					throws Throwable {
				prefs.reload();
				autoCloseUninstall = prefs.getBoolean(
						Common.PREF_ENABLE_AUTO_CLOSE_UNINSTALL, false);
				if (isModuleEnabled() && autoCloseUninstall) {
					if (LOLLIPOP_NEWER) {
						XposedHelpers.callMethod(param.thisObject,
								"startUninstallProgress");
					} else {
						Button mOk = (Button) XposedHelpers.getObjectField(
								param.thisObject, "mOkButton");
						mOk.performClick();
					}
				}
			}

		};

		autoCloseInstallHook = new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param)
					throws Throwable {
				prefs.reload();
				autoCloseInstall = prefs.getBoolean(
						Common.PREF_ENABLE_AUTO_CLOSE_INSTALL, false);
				autoLaunchInstall = prefs.getBoolean(
						Common.PREF_ENABLE_LAUNCH_INSTALL, false);
				deleteApkFiles = prefs.getBoolean(
						Common.PREF_ENABLE_DELETE_APK_FILE_INSTALL, false);
				Button mDone = (Button) XposedHelpers.getObjectField(
						XposedHelpers.getSurroundingThis(param.thisObject),
						"mDoneButton");

				Button mLaunch = (Button) XposedHelpers.getObjectField(
						XposedHelpers.getSurroundingThis(param.thisObject),
						"mLaunchButton");

				if (isModuleEnabled() && autoLaunchInstall) {
					mLaunch.performClick();
				}

				if (isModuleEnabled() && autoCloseInstall) {
					mDone.performClick();
				}

				if (isModuleEnabled() && deleteApkFiles) {
					Uri packageUri = (Uri) XposedHelpers.getObjectField(
							XposedHelpers.getSurroundingThis(param.thisObject),
							"mPackageURI");
					String apkFile = packageUri.getPath();
					deleteApkFile(apkFile);
				}
			}

		};

		// enablers
		if (LOLLIPOP_NEWER) {
			// 5.0 and newer
			XposedHelpers.findAndHookMethod(packageManagerClass,
					"verifySignaturesLP",
					"com.android.server.pm.PackageSetting",
					"android.content.pm.PackageParser$Package",
					checkDuplicatedPermissions);
		}

		if (LOLLIPOP_NEWER) {
			// 5.0 and newer
			XposedHelpers.findAndHookMethod(packageParserClass, "parseBaseApk",
					Resources.class, XmlResourceParser.class, int.class,
					String[].class, checkSdkVersionHook);
		} else {
			// 4.0 - 4.4
			try {
				XposedHelpers.findAndHookMethod(packageParserClass,
						"parsePackage", Resources.class,
						XmlResourceParser.class, int.class, String[].class,
						checkSdkVersionHook);
			} catch (NoSuchMethodError nsm) {
				// CM 11
				try {
					XposedHelpers.findAndHookMethod(packageParserClass,
							"parsePackage", Resources.class,
							XmlResourceParser.class, int.class, boolean.class,
							String[].class, checkSdkVersionHook);
				} catch (NoSuchMethodError nsm2) {
				}
			}
		}

		if (JB_MR1_NEWER) {
			// 4.2 and newer
			XposedHelpers.findAndHookMethod(packageManagerClass,
					"scanPackageLI",
					"android.content.pm.PackageParser$Package", int.class,
					int.class, long.class, "android.os.UserHandle",
					scanPackageHook);
		} else {
			// 4.0 - 4.1
			XposedHelpers.findAndHookMethod(packageManagerClass,
					"scanPackageLI",
					"android.content.pm.PackageParser$Package", int.class,
					int.class, long.class, scanPackageHook);
		}

		// 4.0 and newer
		XposedHelpers.findAndHookMethod(packageManagerClass,
				"verifySignaturesLP", "com.android.server.pm.PackageSetting",
				"android.content.pm.PackageParser$Package",
				verifySignaturesHook);

		if (JB_MR1_NEWER) {

			if (LOLLIPOP_NEWER) {
				// 5.0 and newer
				XposedHelpers.findAndHookMethod(Process.class, "start",
						String.class, String.class, int.class, int.class,
						int[].class, int.class, int.class, int.class,
						String.class, String.class, String.class, String.class,
						String[].class, debugAppsHook);
			} else {
				// 4.2 - 4.4
				try {
					XposedHelpers.findAndHookMethod(Process.class, "start",
							String.class, String.class, int.class, int.class,
							int[].class, int.class, int.class, int.class,
							String.class, String[].class, debugAppsHook);
				} catch (NoSuchMethodError nsm) {
					// CM 11
					try {
						XposedHelpers.findAndHookMethod(Process.class, "start",
								String.class, String.class, int.class,
								int.class, int[].class, int.class, int.class,
								int.class, String.class, boolean.class,
								String[].class, debugAppsHook);
					} catch (NoSuchMethodError nsm2) {
					}
				}
			}
		} else {
			// 4.0 - 4.1
			XposedHelpers.findAndHookMethod(Process.class, "start",
					String.class, String.class, int.class, int.class,
					int[].class, int.class, int.class, int.class,
					String[].class, debugAppsHook);
		}

		// 4.0 and newer
		XposedHelpers.findAndHookMethod(MessageDigest.class, "isEqual",
				byte[].class, byte[].class, verifySignatureHook);

		// 4.0 and newer
		XposedHelpers.findAndHookMethod(View.class,
				"onFilterTouchEventForSecurity", MotionEvent.class,
				showButtonsHook);

		if (JB_MR1_NEWER) {
			// 4.2 and newer
			try {
				XposedHelpers.findAndHookMethod(packageManagerClass,
						"isVerificationEnabled", int.class, verifyAppsHook);
			} catch (NoSuchMethodError nsm) {
			}
		} else {
			// 4.0 - 4.1
			XposedHelpers.findAndHookMethod(packageManagerClass,
					"isVerificationEnabled", verifyAppsHook);
		}

		// 4.0 and newer
		XposedHelpers.findAndHookMethod(signatureClass, "verify", byte[].class,
				int.class, int.class, verifySignatureHook);

		// 4.0 and newer
		XposedHelpers.findAndHookMethod(signatureClass, "verify", byte[].class,
				verifySignatureHook);

		// 4.0 and newer
		XposedHelpers.findAndHookMethod(jarVerifierClass, "verify",
				verifyJarHook);

		// 4.0 and newer
		XposedHelpers.findAndHookMethod(packageParserClass,
				"collectCertificates",
				"android.content.pm.PackageParser$Package", int.class,
				installUnsignedAppsHook);

		// 4.0 and newer
		XposedHelpers.findAndHookMethod(packageManagerClass,
				"compareSignatures", Signature[].class, Signature[].class,
				checkSignaturesHook);

		// 4.0 and newer
		XposedHelpers.findAndHookMethod(packageManagerClass, "checkSignatures",
				String.class, String.class, checkSignaturesHook);

		// 4.0 and newer
		XposedHelpers
				.findAndHookMethod(packageManagerClass, "checkUidSignatures",
						int.class, int.class, checkSignaturesHook);

		// 4.0 and newer
		XposedHelpers.findAndHookMethod(packageManagerClass, "checkPermission",
				String.class, String.class, checkPermissionsHook);

		// 4.0 and newer
		XposedHelpers.findAndHookMethod(packageManagerClass,
				"checkUidPermission", String.class, int.class,
				checkPermissionsHook);

		if (JB_MR1_NEWER) {
			// 4.2 and newer
			XposedHelpers.findAndHookMethod(packageManagerClass,
					"installPackageWithVerificationAndEncryption", Uri.class,
					"android.content.pm.IPackageInstallObserver", int.class,
					String.class, "android.content.pm.VerificationParams",
					"android.content.pm.ContainerEncryptionParams",
					installPackageHook);
		} else {
			if (JB_NEWER) {
				// 4.1
				XposedHelpers.findAndHookMethod(packageManagerClass,
						"installPackageWithVerification", Uri.class,
						"android.content.pm.IPackageInstallObserver",
						int.class, String.class, Uri.class,
						"android.content.pm.ManifestDigest",
						"android.content.pm.ContainerEncryptionParams",
						installPackageHook);

			} else {
				// 4.0
				XposedHelpers
						.findAndHookMethod(packageManagerClass,
								"installPackageWithVerification", Uri.class,
								"android.content.pm.IPackageInstallObserver",
								int.class, String.class, Uri.class,
								"android.content.pm.ManifestDigest",
								installPackageHook);

			}
		}

		if (JB_MR2_NEWER) {
			// 4.3 and newer
			XposedHelpers.findAndHookMethod(packageManagerClass,
					"deletePackageAsUser", String.class,
					"android.content.pm.IPackageDeleteObserver", int.class,
					int.class, deletePackageHook);
		} else {
			// 4.0 - 4.2
			XposedHelpers.findAndHookMethod(packageManagerClass,
					"deletePackage", String.class,
					"android.content.pm.IPackageDeleteObserver", int.class,
					deletePackageHook);
		}

		if (JB_MR1_NEWER) {
			// 4.2 and newer
			XposedHelpers.findAndHookMethod(devicePolicyManagerClass,
					"packageHasActiveAdmins", String.class, int.class,
					deviceAdminsHook);
		} else {
			// 4.0 - 4.1
			XposedHelpers.findAndHookMethod(devicePolicyManagerClass,
					"packageHasActiveAdmins", String.class, deviceAdminsHook);
		}

	}

	@Override
	public void handleLoadPackage(final LoadPackageParam lpparam)
			throws Throwable {
		if (LOLLIPOP_NEWER && !isExpertModeEnabled()) {
			return;
		}
		if (Common.PACKAGEINSTALLER_PKG.equals(lpparam.packageName)) {
			// 4.0 and newer
			XposedHelpers.findAndHookMethod(Common.PACKAGEINSTALLERACTIVITY,
					lpparam.classLoader, "isInstallingUnknownAppsAllowed",
					unknownAppsHook);
			if (LOLLIPOP_NEWER) {
				// 5.0 and newer
				XposedHelpers.findAndHookMethod(Common.UNINSTALLAPPPROGRESS,
						lpparam.classLoader, "showConfirmationDialog",
						autoCloseUninstallHook);
			}
			if (KITKAT_NEWER) {
				// 4.4 and newer
				XposedHelpers.findAndHookMethod(
						Common.PACKAGEINSTALLERACTIVITY, lpparam.classLoader,
						"isVerifyAppsEnabled", verifyAppsHook);
			}

			// 4.0 and newer
			XposedHelpers
					.findAndHookMethod(Common.PACKAGEINSTALLERACTIVITY,
							lpparam.classLoader, "startInstallConfirm",
							autoInstallHook);

			// 4.0 and newer
			XposedHelpers.findAndHookMethod(Common.UNINSTALLERACTIVITY,
					lpparam.classLoader, "onCreate", Bundle.class,
					autoUninstallHook);

			// 4.0 and newer
			XposedHelpers.findAndHookMethod(Common.UNINSTALLAPPPROGRESS,
					lpparam.classLoader, "initView", autoCloseUninstallHook);

			// 4.0 and newer
			XposedHelpers.findAndHookMethod(Common.INSTALLAPPPROGRESS + "$1",
					lpparam.classLoader, "handleMessage", Message.class,
					autoCloseInstallHook);

		}

		if (Common.SETTINGS_PKG.equals(lpparam.packageName)) {
			if (JB_NEWER) {
				// 4.1 and newer
				XposedHelpers.findAndHookMethod(Common.INSTALLEDAPPDETAILS,
						lpparam.classLoader, "isThisASystemPackage",
						systemAppsHook);
			}

			// 4.0 and newer
			XposedHelpers.findAndHookMethod(Common.INSTALLEDAPPDETAILS,
					lpparam.classLoader, "setAppLabelAndIcon",
					PackageInfo.class, showPackageNameHook);

			// 4.0 and newer
			XposedHelpers.findAndHookMethod(Common.CANBEONSDCARDCHECKER,
					lpparam.classLoader, "check", ApplicationInfo.class,
					moveAppsHook);
		}

		if (Common.FDROID_PKG.equals(lpparam.packageName)) {
			// 4.0 and newer
			XposedHelpers.findAndHookMethod(Common.FDROIDAPPDETAILS,
					lpparam.classLoader, "install",
					"org.fdroid.fdroid.data.Apk", fDroidInstallHook);
		}

		if (Common.BACKUPCONFIRM_PKG.equals(lpparam.packageName)) {
			// 4.0 and newer
			XposedHelpers.findAndHookMethod(Common.BACKUPRESTORECONFIRMATION,
					lpparam.classLoader, "onCreate", Bundle.class,
					autoBackupHook);
		}
	}

	public boolean isModuleEnabled() {
		prefs.reload();
		boolean enabled = prefs.getBoolean(Common.PREF_ENABLE_MODULE, true);
		return enabled;
	}

	public boolean isExpertModeEnabled() {
		prefs.reload();
		boolean enabled = prefs.getBoolean(Common.PREF_ENABLE_EXPERT_MODE,
				false);
		return enabled;
	}

	public Context getXInstallerContext() {
		Context context = null;
		try {
			context = mContext.createPackageContext(Common.PACKAGE_NAME, 0);
		} catch (NameNotFoundException e) {
		}
		return context;
	}

	public void backupApkFile(String apkFile) {
		Intent backupApkFile = new Intent(Common.ACTION_BACKUP_APK_FILE);
		backupApkFile.setPackage(Common.PACKAGE_NAME);
		backupApkFile.putExtra(Common.FILE, apkFile);
		mContext.sendBroadcast(backupApkFile);
	}

	public void deleteApkFile(String apkFile) {
		Intent deleteApkFile = new Intent(Common.ACTION_DELETE_APK_FILE);
		deleteApkFile.setPackage(Common.PACKAGE_NAME);
		deleteApkFile.putExtra(Common.FILE, apkFile);
		mContext.sendBroadcast(deleteApkFile);
	}

}
