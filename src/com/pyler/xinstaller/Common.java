package com.pyler.xinstaller;

import java.io.File;

import android.os.Build;
import android.os.Environment;

public class Common {
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
	public static final String ACTION_SET_INSTALL_LOCATION = "xinstaller.intent.action.SET_INSTALL_LOCATION";
	public static final String ACTION_DISABLE_SDK_VERSION_CHECK = "xinstaller.intent.action.DISABLE_SDK_VERSION_CHECK";
	public static final String ACTION_ENABLE_SDK_VERSION_CHECK = "xinstaller.intent.action.ENABLE_SDK_VERSION_CHECK";

	public static final String FILE = "file";
	public static final String FLAGS = "flags";
	public static final String PACKAGE = "package";
	public static final String TASK = "task";
	public static final String LOCATION = "location";

	// utils
	public static final String ACTION_BACKUP_APK_FILE = "xinstaller.intent.action.BACKUP_APK_FILE";
	public static final String ACTION_DELETE_APK_FILE = "xinstaller.intent.action.DELETE_APK_FILE";
	public static final String ACTION_SET_PREFERENCE = "xinstaller.intent.action.SET_PREFERENCE";
	public static final String ACTION_BACKUP_PREFERENCES = "xinstaller.intent.action.BACKUP_PREFERENCES";
	public static final String ACTION_RESTORE_PREFERENCES = "xinstaller.intent.action.RESTORE_PREFERENCES";
	public static final String ACTION_RESET_PREFERENCES = "xinstaller.intent.action.RESET_PREFERENCES";

	public static final String PREFERENCE = "preference";
	public static final String VALUE = "value";

	// prefs
	public static final String PREF_ENABLE_MODULE = "enable_module";
	public static final String PREF_ENABLE_EXPERT_MODE = "enable_expert_mode";
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
	public static final String PREF_ENABLE_DEBUG_APP = "enable_debug_apps";
	public static final String PREF_ENABLE_AUTO_BACKUP = "enable_auto_backup";
	public static final String PREF_ENABLE_SHOW_PACKAGE_NAME = "enable_show_package_name";
	public static final String PREF_ENABLE_SHOW_VERSION = "enable_show_version";
	public static final String PREF_ENABLE_DELETE_APK_FILE_INSTALL = "enable_delete_apk_files_install";
	public static final String PREF_ENABLE_MOVE_APP = "enable_move_apps";
	public static final String PREF_DISABLE_SDK_VERSION_CHECK = "disable_sdk_version_check";
	public static final String PREF_DISABLE_INSTALL_BACKGROUND = "disable_install_background";
	public static final String PREF_DISABLE_UNINSTALL_BACKGROUND = "disable_uninstall_background";
	public static final String PREF_ENABLE_LAUNCH_APP = "enable_launch_apps";

	// constants
	public static final String PACKAGE_NAME = Common.class.getPackage()
			.getName();
	public static final String PACKAGE_PREFERENCES = PACKAGE_NAME
			+ "_preferences";
	public static final String PACKAGE_TAG = "XInstaller";
	public static final String APP_DIR = Environment
			.getExternalStorageDirectory()
			+ File.separator
			+ PACKAGE_TAG
			+ File.separator;
	public static final File PACKAGE_DIR = new File(APP_DIR);
	public static final File PREFERENCES_BACKUP_FILE = new File(APP_DIR
			+ File.separator + PACKAGE_TAG + ".backup");
	public static final int SDK = Build.VERSION.SDK_INT;
	public static final int LATEST_ANDROID_RELEASE = Build.VERSION_CODES.LOLLIPOP;
	public static final String PACKAGEINSTALLER_PKG = "com.android.packageinstaller";
	public static final String SETTINGS_PKG = "com.android.settings";
	public static final String FDROID_PKG = "org.fdroid.fdroid";
	public static final String BACKUPCONFIRM_PKG = "com.android.backupconfirm";

	// classes
	public static final String PACKAGEMANAGERSERVICE = "com.android.server.pm.PackageManagerService";
	public static final String DEVICEPOLICYMANAGERSERVICE = "com.android.server.DevicePolicyManagerService";
	public static final String INSTALLEDAPPDETAILS = "com.android.settings.applications.InstalledAppDetails";
	public static final String PACKAGEINSTALLERACTIVITY = "com.android.packageinstaller.PackageInstallerActivity";
	public static final String INSTALLAPPPROGRESS = "com.android.packageinstaller.InstallAppProgress";
	public static final String CANBEONSDCARDCHECKER = "com.android.settings.applications.CanBeOnSdCardChecker";
	public static final String UNINSTALLERACTIVITY = "com.android.packageinstaller.UninstallerActivity";
	public static final String UNINSTALLAPPPROGRESS = "com.android.packageinstaller.UninstallAppProgress";
	public static final String FDROIDAPPDETAILS = "org.fdroid.fdroid.AppDetails";
	public static final String PACKAGEPARSER = "android.content.pm.PackageParser";
	public static final String JARVERIFIER = "java.util.jar.JarVerifier$VerifierEntry";
	public static final String SIGNATURE = "java.security.Signature";
	public static final String BACKUPRESTORECONFIRMATION = "com.android.backupconfirm.BackupRestoreConfirmation";
	public static final String ANDROID = "android";

	// flags
	public static final int DELETE_KEEP_DATA = 0x00000001;
	public static final int INSTALL_ALLOW_DOWNGRADE = 0x00000080;
	public static final int INSTALL_FORWARD_LOCK = 0x00000001;
	public static final int INSTALL_EXTERNAL = 0x00000008;
	public static final int INSTALL_REPLACE_EXISTING = 0x00000002;
	public static final int REMOVE_TASK_KILL_PROCESS = 0x0001;
	public static final int DEBUG_ENABLE_DEBUGGER = 0x1;
	public static final int ROOT_UID = 0;
}
