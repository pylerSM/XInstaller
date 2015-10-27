package com.pyler.xinstaller;

import android.app.AndroidAppHelper;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import de.robv.android.xposed.XposedBridge;

/**
 * Checks if the module is enabled by hooking into itself.
 */
public class ModuleEnabledChecker implements IXposedHookLoadPackage {

	@Override
	public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
		if (!lpparam.packageName.equals("com.pyler.xinstaller"))
			return;
		
			findAndHookMethod("com.pyler.xinstaller.Preferences", lpparam.classLoader, "isModuleEnabled", new XC_MethodReplacement() {
					@Override
					protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
						XposedBridge.log("We have replaced it.");
						return new Boolean(true);
					}
				});
		
	}

}
