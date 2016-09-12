package com.pyler.xinstaller;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.view.View;
import android.widget.Button;

import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.util.Hashtable;
import java.util.Locale;

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
    public boolean installUnknownApps;
    public boolean verifyApps;
    public boolean installAppsOnExternal;
    public boolean deviceAdmins;
    public boolean checkPermissions;
    public boolean backupApkFiles;
    public boolean verifyJar;
    public boolean verifySignature;
    public boolean showButtons;
    public boolean debugApps;
    public boolean autoBackup;
    public boolean checkSdkVersion;
    public boolean installBackground;
    public boolean uninstallBackground;
    public boolean checkLuckyPatcher;
    public boolean backupAllApps;
    public boolean grantRuntimePermissions;
    public XC_MethodHook checkSignaturesHook;
    public XC_MethodHook deletePackageHook;
    public XC_MethodHook installPackageHook;
    public XC_MethodHook unknownAppsHook;
    public XC_MethodHook verifyAppsHook;
    public XC_MethodHook deviceAdminsHook;
    public XC_MethodHook checkSignaturesFDroidHook;
    public XC_MethodHook checkPermissionsHook;
    public XC_MethodHook verifyJarHook;
    public XC_MethodHook verifySignatureHook;
    public XC_MethodHook showButtonsHook;
    public XC_MethodHook debugAppsHook;
    public XC_MethodHook autoBackupHook;
    public XC_MethodHook scanPackageHook;
    public XC_MethodHook verifySignaturesHook;
    public XC_MethodHook checkSdkVersionHook;
    public XC_MethodHook getPackageInfoHook;
    public boolean disableCheckSignatures;
    public Context mContext;

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        if (!Common.MARSHMALLOW_NEWER) {
            return;
        }

        prefs = new XSharedPreferences(XInstaller.class.getPackage().getName());
        prefs.makeWorldReadable();
        disableCheckSignatures = true;

        getPackageInfoHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param)
                    throws Throwable {
                reloadPreferences();
                checkLuckyPatcher = prefs.getBoolean(
                        Common.PREF_DISABLE_CHECK_LUCKY_PATCHER, false);
                if (isModuleEnabled() && checkLuckyPatcher) {
                    String packageName = (String) param.args[0];
                    int uid = Binder.getCallingUid();
                    String caller = (String) XposedHelpers.callMethod(
                            param.thisObject, "getNameForUid", uid);

                    if (uid != Common.SYSTEM_UID && Common.LUCKYPATCHER_PKG.equals(packageName)
                            && !Common.LUCKYPATCHER_PKG.equals(caller)) {
                        param.args[0] = Common.EMPTY_STRING;
                    }
                }
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param)
                    throws Throwable {
                reloadPreferences();
                backupAllApps = prefs.getBoolean(
                        Common.PREF_ENABLE_BACKUP_ALL_APPS, false);
                if (isModuleEnabled() && backupAllApps) {
                    PackageInfo packageInfo = (PackageInfo) param.getResult();
                    if (packageInfo != null) {
                        int flags = packageInfo.applicationInfo.flags;
                        if ((flags & ApplicationInfo.FLAG_ALLOW_BACKUP) == 0) {
                            flags |= ApplicationInfo.FLAG_ALLOW_BACKUP;
                        }
                        packageInfo.applicationInfo.flags = flags;
                        param.setResult(packageInfo);
                    }
                }
            }
        };

        checkSdkVersionHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param)
                    throws Throwable {
                reloadPreferences();
                checkSdkVersion = prefs.getBoolean(
                        Common.PREF_DISABLE_CHECK_SDK_VERSION, false);
                if (isModuleEnabled() && checkSdkVersion) {
                    XposedHelpers.setObjectField(param.thisObject,
                            "SDK_VERSION", Common.LATEST_ANDROID_RELEASE);
                }
            }
        };

        verifySignaturesHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param)
                    throws Throwable {
                reloadPreferences();
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


        autoBackupHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param)
                    throws Throwable {
                reloadPreferences();
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
                reloadPreferences();
                debugApps = prefs.getBoolean(Common.PREF_ENABLE_DEBUG_APP,
                        false);
                int id = 5;
                int flags = (Integer) param.args[id];
                if (isModuleEnabled() && debugApps && (flags & Common.DEBUG_ENABLE_DEBUGGER) == 0) {
                    flags |= Common.DEBUG_ENABLE_DEBUGGER;
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
                reloadPreferences();
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
                reloadPreferences();
                verifyJar = prefs.getBoolean(Common.PREF_DISABLE_VERIFY_JAR,
                        false);
                if (isModuleEnabled() && verifyJar) {
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
                reloadPreferences();
                verifySignature = prefs.getBoolean(
                        Common.PREF_DISABLE_VERIFY_SIGNATURE, false);
                if (isModuleEnabled()
                        && verifySignature) {
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
                if (isModuleEnabled()
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
                grantRuntimePermissions = prefs.getBoolean(
                        Common.PREF_ENABLE_GRANT_RUNTIME_PERMISSIONS, false);

                mContext = (Context) XposedHelpers.getObjectField(
                        param.thisObject, "mContext");
                boolean isInstallStage = "installStage".equals(param.method
                        .getName());
                int flags, id;

                if (isInstallStage) {
                    id = 4;
                    flags = (Integer) XposedHelpers.getObjectField(
                            param.args[id], "installFlags");
                } else {
                    id = 2;
                    flags = (Integer) param.args[id];
                }
                if (isModuleEnabled() && downgradeApps && (flags & Common.INSTALL_ALLOW_DOWNGRADE) == 0) {
                    flags |= Common.INSTALL_ALLOW_DOWNGRADE;
                }
                if (isModuleEnabled() && forwardLock && (flags & Common.INSTALL_FORWARD_LOCK) != 0) {
                    flags &= ~Common.INSTALL_FORWARD_LOCK;
                }
                if (isModuleEnabled()
                        && installAppsOnExternal && (flags & Common.INSTALL_EXTERNAL) == 0) {
                    flags |= Common.INSTALL_EXTERNAL;
                }

                if (isModuleEnabled() && grantRuntimePermissions
                        && (flags & Common.INSTALL_GRANT_RUNTIME_PERMISSIONS) == 0) {
                    flags |= Common.INSTALL_GRANT_RUNTIME_PERMISSIONS;
                }

                if (isModuleEnabled()) {
                    if (isInstallStage) {
                        Object sessions = param.args[id];
                        XposedHelpers.setIntField(sessions, "installFlags",
                                flags);
                        param.args[id] = sessions;
                    } else {

                        param.args[id] = flags;
                    }
                }

                if (isModuleEnabled() && installBackground && Binder.getCallingUid() == Common.ROOT_UID) {
                    param.setResult(null);
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
                int id = 3;
                int flags = (Integer) param.args[id];

                if (isModuleEnabled() && keepAppsData && (flags & Common.DELETE_KEEP_DATA) == 0) {
                    flags |= Common.DELETE_KEEP_DATA;
                }

                if (isModuleEnabled() && uninstallBackground && Binder.getCallingUid() == Common.ROOT_UID) {
                    param.setResult(null);
                }

                if (isModuleEnabled()) {
                    param.args[id] = flags;
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

    }

    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam)
            throws Throwable {
        if (!Common.MARSHMALLOW_NEWER) {
            return;
        }

        if (Common.ANDROID_PKG.equals(lpparam.packageName)
                && Common.ANDROID_PKG.equals(lpparam.processName)) {
            Class<?> packageManagerClass = XposedHelpers.findClass(
                    Common.PACKAGEMANAGERSERVICE, lpparam.classLoader);
            Class<?> devicePolicyManagerClass = XposedHelpers.findClass(
                    Common.DEVICEPOLICYMANAGERSERVICE, lpparam.classLoader);
            Class<?> packageParserClass = XposedHelpers.findClass(
                    Common.PACKAGEPARSER, lpparam.classLoader);
            Class<?> jarVerifierClass = XposedHelpers.findClass(
                    Common.JARVERIFIER, lpparam.classLoader);
            Class<?> signatureClass = XposedHelpers.findClass(Common.SIGNATURE,
                    lpparam.classLoader);

            // 5.0 and newer
            XposedBridge.hookAllMethods(packageParserClass, "parseBaseApk",
                    checkSdkVersionHook);

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
            XposedBridge.hookAllMethods(jarVerifierClass, "verify",
                    verifyJarHook);

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

            XposedBridge.hookAllMethods(packageManagerClass,
                    "installPackageAsUser", installPackageHook);
            XposedBridge.hookAllMethods(packageManagerClass,
                    "installStage", installPackageHook);

            // 5.0 and newer
            XposedBridge.hookAllMethods(packageManagerClass,
                    "deletePackage", deletePackageHook);


            // 4.0 and newer
            XposedBridge.hookAllMethods(devicePolicyManagerClass,
                    "packageHasActiveAdmins", deviceAdminsHook);

        }
        if (Common.PACKAGEINSTALLER_PKG.equals(lpparam.packageName) || Common.GOOGLE_PACKAGEINSTALLER_PKG.equals(lpparam.packageName)) {
            // 5.0 and newer
            XposedHelpers.findAndHookMethod(
                    Common.PACKAGEINSTALLERACTIVITY, lpparam.classLoader,
                    "isUnknownSourcesEnabled", unknownAppsHook);

            // 4.4 and newer
            XposedHelpers.findAndHookMethod(
                    Common.PACKAGEINSTALLERACTIVITY, lpparam.classLoader,
                    "isVerifyAppsEnabled", verifyAppsHook);

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

        if (isModuleEnabled() && changeDevicePropertiesEnabled()) {
            changeDeviceProperties();
        }
    }

    public void reloadPreferences() {
        if (isModuleEnabled()) {
            prefs.reload();
        }
    }

    public boolean isModuleEnabled() {
        prefs.reload();
        return prefs.getBoolean(Common.PREF_ENABLE_MODULE, true);
    }

    public boolean changeDevicePropertiesEnabled() {
        prefs.reload();
        return prefs.getBoolean(
                Common.PREF_ENABLE_CHANGE_DEVICE_PROPERTIES, false);
    }

    public void changeDeviceProperties() {
        prefs.reload();
        for (String[] property : Common.DEVICE_PROPERTIES) {
            String propertyValue = prefs.getString(property[0], null);
            if (propertyValue != null) {
                String buildFieldName = property[0].replace("device_", "");
                buildFieldName = buildFieldName.toUpperCase(Locale.ENGLISH);
                XposedHelpers.setStaticObjectField(Build.class, buildFieldName,
                        propertyValue);
            }
        }
    }

    public Context getXInstallerContext() {
        Context context = null;
        try {
            context = mContext.createPackageContext(Common.PACKAGE_NAME, 0);
        } catch (NameNotFoundException e) {
        }
        return context;
    }

}
