package com.pyler.xinstaller;

import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.util.Hashtable;

import android.app.Activity;
import android.app.AndroidAppHelper;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Message;
import android.os.Process;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
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
	public boolean exportApps;
	public boolean openAppsGooglePlay;
	public boolean uninstallSystemApps;
	public boolean autoEnableClearButtons;
	public boolean autoHideInstall;
	public boolean checkLuckyPatcher;
	public XC_MethodHook checkSignaturesHook;
	public XC_MethodHook deletePackageHook;
	public XC_MethodHook installPackageHook;
	public XC_MethodHook systemAppsHook;
	public XC_MethodHook unknownAppsHook;
	public XC_MethodHook verifyAppsHook;
	public XC_MethodHook deviceAdminsHook;
	public XC_MethodHook checkSignaturesFDroidHook;
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
	public XC_MethodHook autoEnableClearButtonsHook;
	public XC_MethodHook autoHideInstallHook;
	public XC_MethodHook computeCertificateHashesHook;
	public XC_MethodHook getPackageInfoHook;
	public boolean disableCheckSignatures;
	public Context mContext;

	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
		prefs = new XSharedPreferences(XInstaller.class.getPackage().getName());
		prefs.makeWorldReadable();
		Class<?> packageParserClass = XposedHelpers.findClass(
				Common.PACKAGEPARSER, null);
		Class<?> jarVerifierClass = XposedHelpers.findClass(Common.JARVERIFIER,
				null);
		Class<?> signatureClass = XposedHelpers.findClass(Common.SIGNATURE,
				null);
		disableCheckSignatures = true;

		getPackageInfoHook = new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param)
					throws Throwable {
				prefs.reload();
				checkLuckyPatcher = prefs.getBoolean(
						Common.PREF_DISABLE_CHECK_LUCKY_PATCHER, false);
				if (isModuleEnabled() && checkLuckyPatcher) {
					String packageName = (String) param.args[0];
					int uid = Binder.getCallingUid();
					String caller = (String) XposedHelpers.callMethod(
							param.thisObject, "getNameForUid", uid);
					if (uid != Common.SYSTEM_UID) {
						if (Common.LUCKYPATCHER_PKG.equals(packageName)
								&& !Common.LUCKYPATCHER_PKG.equals(caller)) {
							param.args[0] = Common.EMPTY_STRING;
						}
					}
				}
			}
		};

		autoHideInstallHook = new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param)
					throws Throwable {
				prefs.reload();
				autoHideInstall = prefs.getBoolean(
						Common.PREF_ENABLE_AUTO_HIDE_INSTALL, false);
				Activity packageInstaller = (Activity) param.thisObject;
				if (isModuleEnabled() && autoHideInstall) {
					packageInstaller.onBackPressed();
				}
			}
		};

		autoEnableClearButtonsHook = new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param)
					throws Throwable {
				prefs.reload();
				autoEnableClearButtons = prefs.getBoolean(
						Common.PREF_ENABLE_AUTO_ENABLE_CLEAR_BUTTON, false);
				if (isModuleEnabled() && autoEnableClearButtons) {
					Button mClearDataButton = (Button) XposedHelpers
							.getObjectField(param.thisObject,
									"mClearDataButton");
					Button mClearCacheButton = (Button) XposedHelpers
							.getObjectField(param.thisObject,
									"mClearCacheButton");
					mClearDataButton.setEnabled(true);
					mClearCacheButton.setEnabled(true);
					mClearDataButton
							.setOnClickListener((OnClickListener) param.thisObject);
					mClearCacheButton
							.setOnClickListener((OnClickListener) param.thisObject);
				}
			}
		};

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
				disableCheckSignatures = false;
			}

			@Override
			protected void afterHookedMethod(MethodHookParam param)
					throws Throwable {
				disableCheckSignatures = true;
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
				exportApps = prefs.getBoolean(Common.PREF_ENABLE_EXPORT_APP,
						false);
				openAppsGooglePlay = prefs.getBoolean(
						Common.PREF_ENABLE_OPEN_APP_GOOGLE_PLAY, false);
				uninstallSystemApps = prefs.getBoolean(
						Common.PREF_ENABLE_UNINSTALL_SYSTEM_APP, false);
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
				int labelId = mResources.getIdentifier("app_name", "id",
						Common.SETTINGS_PKG);
				ImageView appIcon = (ImageView) appSnippet.findViewById(iconId);
				TextView appLabel = (TextView) appSnippet.findViewById(labelId);
				String version = appVersion.getText().toString();
				final Resources res = getXInstallerContext().getResources();
				final String apkFile = pkgInfo.applicationInfo.sourceDir;
				final String packageName = pkgInfo.packageName;
				final String appName = appLabel.getText().toString();
				if (isModuleEnabled() && showPackageName) {
					appVersion.setText(packageName + "\n" + version);
					appVersion.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							ClipboardManager clipboard = (ClipboardManager) mContext
									.getSystemService(Context.CLIPBOARD_SERVICE);
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

				if (isModuleEnabled() && exportApps) {
					appLabel.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							backupApkFile(apkFile);
							Toast.makeText(mContext,
									res.getString(R.string.apk_file_exported),
									Toast.LENGTH_SHORT).show();
						}
					});
					appLabel.setOnLongClickListener(new View.OnLongClickListener() {
						@Override
						public boolean onLongClick(View v) {
							ClipboardManager clipboard = (ClipboardManager) mContext
									.getSystemService(Context.CLIPBOARD_SERVICE);
							ClipData clip = ClipData.newPlainText("text",
									appName);
							clipboard.setPrimaryClip(clip);
							Toast.makeText(mContext,
									res.getString(R.string.app_name_copied),
									Toast.LENGTH_SHORT).show();
							return true;
						}
					});
				}

				if (isModuleEnabled() && openAppsGooglePlay) {
					appIcon.setOnLongClickListener(new View.OnLongClickListener() {
						@Override
						public boolean onLongClick(View v) {
							String uri = "market://details?id=" + packageName;
							Intent openGooglePlay = new Intent(
									Intent.ACTION_VIEW, Uri.parse(uri));
							openGooglePlay
									.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							mContext.startActivity(openGooglePlay);
							return true;
						}
					});
				}

				if (isModuleEnabled() && uninstallSystemApps) {
					OnLongClickListener uninstallSystemApp = new View.OnLongClickListener() {
						@Override
						public boolean onLongClick(View v) {
							uninstallSystemApp(packageName);
							return true;
						}
					};
					Button mUninstallButton = (Button) XposedHelpers
							.getObjectField(param.thisObject,
									"mUninstallButton");
					Button mSpecialDisableButton = (Button) XposedHelpers
							.getObjectField(param.thisObject,
									"mSpecialDisableButton");
					mUninstallButton.setOnLongClickListener(uninstallSystemApp);
					mSpecialDisableButton
							.setOnLongClickListener(uninstallSystemApp);
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
				int id = 5;
				int flags = (Integer) param.args[id];
				if (isModuleEnabled() && isExpertModeEnabled()

				&& debugApps) {
					if ((flags & Common.DEBUG_ENABLE_DEBUGGER) == 0) {
						flags |= Common.DEBUG_ENABLE_DEBUGGER;
					}
				}
				if (isModuleEnabled()) {
					param.args[id] = flags;
				}
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
					if (Common.LOLLIPOP_NEWER) {
						Certificate[][] certChains = (Certificate[][]) XposedHelpers
								.getObjectField(param.thisObject, "certChains");
						Hashtable<String, Certificate[][]> verifiedEntries = null;

						try {
							verifiedEntries = (Hashtable<String, Certificate[][]>) XposedHelpers
									.findField(param.thisObject.getClass(),
											"verifiedEntries").get(
											param.thisObject);
						} catch (NoSuchFieldError e) {
							verifiedEntries = (Hashtable<String, Certificate[][]>) XposedHelpers
									.getObjectField(
											XposedHelpers
													.getSurroundingThis(param.thisObject),
											"verifiedEntries");
						}
						verifiedEntries.put(name, certChains);
					} else {
						Certificate[] certificates = (Certificate[]) XposedHelpers
								.getObjectField(param.thisObject,
										"certificates");
						Hashtable<String, Certificate[]> verifiedEntries = null;
						try {
							verifiedEntries = (Hashtable<String, Certificate[]>) XposedHelpers
									.findField(param.thisObject.getClass(),
											"verifiedEntries").get(
											param.thisObject);
						} catch (NoSuchFieldError e) {
							verifiedEntries = (Hashtable<String, Certificate[]>) XposedHelpers
									.getObjectField(
											XposedHelpers
													.getSurroundingThis(param.thisObject),
											"verifiedEntries");
						}
						verifiedEntries.put(name, certificates);
					}
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

		computeCertificateHashesHook = new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param)
					throws Throwable {
				prefs.reload();
				installUnsignedApps = prefs.getBoolean(
						Common.PREF_ENABLE_INSTALL_UNSIGNED_APP, false);
				if (isModuleEnabled() && isExpertModeEnabled()
						&& installUnsignedApps) {
					PackageInfo pkgInfo = (PackageInfo) param.args[0];
					if (pkgInfo.signatures == null) {
						param.setResult(new String[0]);
						return;
					}
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
				if (isModuleEnabled() && checkSignatures
						&& disableCheckSignatures) {
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
				mContext = (Context) XposedHelpers.getObjectField(
						param.thisObject, "mContext");
				int id = Common.JB_MR1_NEWER ? 2 : 1;
				int flags = (Integer) param.args[id];
				if (isModuleEnabled() && downgradeApps) {
					if ((flags & Common.INSTALL_ALLOW_DOWNGRADE) == 0) {
						flags |= Common.INSTALL_ALLOW_DOWNGRADE;
					}
				}
				if (isModuleEnabled() && forwardLock) {
					if ((flags & Common.INSTALL_FORWARD_LOCK) != 0) {
						flags &= ~Common.INSTALL_FORWARD_LOCK;
					}
				}
				if (isModuleEnabled() && isExpertModeEnabled()

				&& installAppsOnExternal) {
					if ((flags & Common.INSTALL_EXTERNAL) == 0) {
						flags |= Common.INSTALL_EXTERNAL;
					}
				}

				if (isModuleEnabled()) {
					param.args[id] = flags;
				}

				if (isModuleEnabled() && backupApkFiles) {
					String apkFile;
					if (Common.LOLLIPOP_NEWER) {
						apkFile = (String) param.args[0];
					} else {
						Uri packageUri = (Uri) param.args[0];
						apkFile = packageUri.getPath();
					}
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
				int id = Common.JB_MR2_NEWER ? 3 : 2;
				int flags = (Integer) param.args[id];
				if (isModuleEnabled() && keepAppsData) {
					if ((flags & Common.DELETE_KEEP_DATA) == 0) {
						flags |= Common.DELETE_KEEP_DATA;
					}
				}

				if (isModuleEnabled()) {
					param.args[id] = flags;
				}

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

		checkSignaturesFDroidHook = new XC_MethodHook() {
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

					} catch (NameNotFoundException e) {
					}
					Toast.makeText(mContext, versionInfo, Toast.LENGTH_LONG)
							.show();
				}
			}
		};

		autoUninstallHook = new XC_MethodHook() {

			@Override
			protected void beforeHookedMethod(MethodHookParam param)
					throws Throwable {
				prefs.reload();
				autoUninstall = prefs.getBoolean(
						Common.PREF_ENABLE_AUTO_UNINSTALL, false);
				if (isModuleEnabled() && autoUninstall) {
					if (Common.LOLLIPOP_NEWER) {
						Activity packageInstaller = (Activity) param.thisObject;
						packageInstaller.onBackPressed();
						XposedHelpers.callMethod(param.thisObject,
								"startUninstallProgress");
					}
				}
			}

			@Override
			protected void afterHookedMethod(MethodHookParam param)
					throws Throwable {
				prefs.reload();
				autoUninstall = prefs.getBoolean(
						Common.PREF_ENABLE_AUTO_UNINSTALL, false);
				if (isModuleEnabled() && autoUninstall) {
					if (!Common.LOLLIPOP_NEWER) {
						Button mOk = (Button) XposedHelpers.getObjectField(
								param.thisObject, "mOk");
						if (mOk != null) {
							mOk.performClick();
						}
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
						Common.PREF_ENABLE_AUTO_CLOSE_INSTALL, false);
				autoLaunchInstall = prefs.getBoolean(
						Common.PREF_ENABLE_LAUNCH_INSTALL, false);
				deleteApkFiles = prefs.getBoolean(
						Common.PREF_ENABLE_DELETE_APK_FILE_INSTALL, false);
				mContext = AndroidAppHelper.currentApplication();
				Button mDone = (Button) XposedHelpers.getObjectField(
						XposedHelpers.getSurroundingThis(param.thisObject),
						"mDoneButton");

				Button mLaunch = (Button) XposedHelpers.getObjectField(
						XposedHelpers.getSurroundingThis(param.thisObject),
						"mLaunchButton");

				Message msg = (Message) param.args[0];
				boolean installedApp = false;
				if (msg != null) {
					installedApp = (msg.arg1 == Common.INSTALL_SUCCEEDED);
				}

				if (isModuleEnabled() && autoLaunchInstall) {
					if (installedApp) {
						mLaunch.performClick();
					}
				}

				if (isModuleEnabled() && autoCloseInstall) {
					if (installedApp) {
						mDone.performClick();
					}
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

		if (Common.LOLLIPOP_NEWER) {
			// 5.0 and newer
			XposedBridge.hookAllMethods(packageParserClass, "parseBaseApk",
					checkSdkVersionHook);
		} else {
			// 4.0 - 4.4
			XposedBridge.hookAllMethods(packageParserClass, "parsePackage",
					checkSdkVersionHook);
		}

		// 4.0 and newer
		XposedBridge.hookAllMethods(Process.class, "start", debugAppsHook);

		// 4.0 and newer
		XposedBridge.hookAllMethods(MessageDigest.class, "isEqual",
				verifySignatureHook);

		// 4.0 and newer
		XposedBridge.hookAllMethods(View.class,
				"onFilterTouchEventForSecurity", showButtonsHook);

		// 4.0 and newer
		XposedBridge.hookAllMethods(signatureClass, "verify",
				verifySignatureHook);

		// 4.0 and newer
		XposedBridge.hookAllMethods(jarVerifierClass, "verify", verifyJarHook);

		// 4.0 and newer
		XposedBridge.hookAllMethods(packageParserClass, "collectCertificates",
				installUnsignedAppsHook);

	}

	@Override
	public void handleLoadPackage(final LoadPackageParam lpparam)
			throws Throwable {
		if (Common.ANDROID_PKG.equals(lpparam.packageName)
				&& Common.ANDROID_PKG.equals(lpparam.processName)) {
			Class<?> packageManagerClass = XposedHelpers.findClass(
					Common.PACKAGEMANAGERSERVICE, lpparam.classLoader);
			Class<?> devicePolicyManagerClass = XposedHelpers.findClass(
					Common.DEVICEPOLICYMANAGERSERVICE, lpparam.classLoader);

			if (Common.LOLLIPOP_NEWER) {
				// 5.0 and newer
				XposedBridge.hookAllMethods(packageManagerClass,
						"checkUpgradeKeySetLP", checkDuplicatedPermissionsHook);
			}
			// 4.0 and newer
			XposedBridge.hookAllMethods(packageManagerClass, "getPackageInfo",
					getPackageInfoHook);
			// 4.0 and newer
			XposedBridge.hookAllMethods(packageManagerClass, "scanPackageLI",
					scanPackageHook);

			// 4.0 and newer
			XposedBridge.hookAllMethods(packageManagerClass,
					"verifySignaturesLP", verifySignaturesHook);

			// 4.0 and newer
			XposedBridge.hookAllMethods(packageManagerClass,
					"isVerificationEnabled", verifyAppsHook);
			// 4.0 and newer
			XposedBridge.hookAllMethods(packageManagerClass,
					"compareSignatures", checkSignaturesHook);

			// 4.0 and newer
			XposedBridge.hookAllMethods(packageManagerClass, "checkSignatures",
					checkSignaturesHook);

			// 4.0 and newer
			XposedBridge.hookAllMethods(packageManagerClass,
					"checkUidSignatures", checkSignaturesHook);

			// 4.0 and newer
			XposedBridge.hookAllMethods(packageManagerClass, "checkPermission",
					checkPermissionsHook);

			// 4.0 and newer
			XposedBridge.hookAllMethods(packageManagerClass,
					"checkUidPermission", checkPermissionsHook);

			if (Common.LOLLIPOP_NEWER) {
				// 5.0 and newer
				XposedBridge.hookAllMethods(packageManagerClass,
						"installPackageAsUser", installPackageHook);
			} else {
				if (Common.JB_MR1_NEWER) {
					// 4.2 - 4.4
					XposedBridge.hookAllMethods(packageManagerClass,
							"installPackageWithVerificationAndEncryption",
							installPackageHook);
				} else {
					// 4.0 - 4.1
					XposedBridge.hookAllMethods(packageManagerClass,
							"installPackageWithVerification",
							installPackageHook);
				}
			}

			if (Common.JB_MR2_NEWER) {
				// 4.3 and newer
				XposedBridge.hookAllMethods(packageManagerClass,
						"deletePackageAsUser", deletePackageHook);
			} else {
				// 4.0 - 4.2
				XposedBridge.hookAllMethods(packageManagerClass,
						"deletePackage", deletePackageHook);
			}

			// 4.0 and newer
			XposedBridge.hookAllMethods(devicePolicyManagerClass,
					"packageHasActiveAdmins", deviceAdminsHook);
		}
		if (Common.PACKAGEINSTALLER_PKG.equals(lpparam.packageName)) {
			// 4.0 and newer
			XposedHelpers.findAndHookMethod(Common.PACKAGEINSTALLERACTIVITY,
					lpparam.classLoader, "isInstallingUnknownAppsAllowed",
					unknownAppsHook);
			// 4.0 - 4.4
			XposedHelpers.findAndHookMethod(Common.UNINSTALLAPPPROGRESS,
					lpparam.classLoader, "initView", autoCloseUninstallHook);

			if (Common.KITKAT_NEWER) {
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

			if (Common.LOLLIPOP_NEWER) {
				// 5.0 and newer
				XposedHelpers.findAndHookMethod(Common.UNINSTALLERACTIVITY,
						lpparam.classLoader, "showConfirmationDialog",
						autoUninstallHook);
			} else {
				// 4.0 and newer
				XposedHelpers.findAndHookMethod(Common.UNINSTALLERACTIVITY,
						lpparam.classLoader, "onCreate", Bundle.class,
						autoUninstallHook);
			}

			// 4.0 and newer
			XposedHelpers.findAndHookMethod(Common.INSTALLAPPPROGRESS + "$1",
					lpparam.classLoader, "handleMessage", Message.class,
					autoCloseInstallHook);
			// 4.0 and newer
			XposedHelpers.findAndHookMethod(Common.INSTALLAPPPROGRESS,
					lpparam.classLoader, "initView", autoHideInstallHook);

		}

		if (Common.SETTINGS_PKG.equals(lpparam.packageName)) {
			if (Common.JB_NEWER) {
				if (Common.LOLLIPOP_NEWER) {
					// 5.0 and newer
					XposedHelpers.findAndHookMethod(Common.UTILS,
							lpparam.classLoader, "isSystemPackage",
							PackageManager.class, PackageInfo.class,
							systemAppsHook);
				} else {
					// 4.1 - 4.4
					XposedHelpers.findAndHookMethod(Common.INSTALLEDAPPDETAILS,
							lpparam.classLoader, "isThisASystemPackage",
							systemAppsHook);
				}
			}

			// 4.0 and newer
			XposedHelpers.findAndHookMethod(Common.INSTALLEDAPPDETAILS,
					lpparam.classLoader, "setAppLabelAndIcon",
					PackageInfo.class, showPackageNameHook);

			// 4.0 and newer
			XposedHelpers.findAndHookMethod(Common.CANBEONSDCARDCHECKER,
					lpparam.classLoader, "check", ApplicationInfo.class,
					moveAppsHook);

			// 4.0 and newer
			XposedHelpers.findAndHookMethod(Common.INSTALLEDAPPDETAILS,
					lpparam.classLoader, "refreshSizeInfo",
					autoEnableClearButtonsHook);
		}

		if (Common.FDROID_PKG.equals(lpparam.packageName)) {
			// 4.0 and newer
			XposedHelpers.findAndHookMethod(Common.FDROIDAPPDETAILS,
					lpparam.classLoader, "install",
					"org.fdroid.fdroid.data.Apk", checkSignaturesFDroidHook);
		}

		if (Common.BACKUPCONFIRM_PKG.equals(lpparam.packageName)) {
			// 4.0 and newer
			XposedHelpers.findAndHookMethod(Common.BACKUPRESTORECONFIRMATION,
					lpparam.classLoader, "onCreate", Bundle.class,
					autoBackupHook);
		}

		if (Common.GOOGLEPLAY_PKG.equals(lpparam.packageName)) {
			// 4.0 and newer
			XposedHelpers.findAndHookMethod(Common.PACKAGEMANAGERREPOSITORY,
					lpparam.classLoader, "computeCertificateHashes",
					PackageInfo.class, computeCertificateHashesHook);
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
		getXInstallerContext().sendBroadcast(backupApkFile);
	}

	public void deleteApkFile(String apkFile) {
		Intent deleteApkFile = new Intent(Common.ACTION_DELETE_APK_FILE);
		deleteApkFile.setPackage(Common.PACKAGE_NAME);
		deleteApkFile.putExtra(Common.FILE, apkFile);
		getXInstallerContext().sendBroadcast(deleteApkFile);
	}

	public void uninstallSystemApp(String packageName) {
		Intent uninstallSystemApp = new Intent(
				Common.ACTION_UNINSTALL_SYSTEM_APP);
		uninstallSystemApp.setPackage(Common.PACKAGE_NAME);
		uninstallSystemApp.putExtra(Common.PACKAGE, packageName);
		getXInstallerContext().sendBroadcast(uninstallSystemApp);
	}

}
