Dowmload module:
- http://repo.xposed.info/module/com.pyler.xinstaller
- https://github.com/pylerSM/XInstaller/tree/master/releases

Translations
- https://crowdin.com/project/xinstaller

APIs:
- Package name = "package"
- Flags = "flags"
- APK file = "file"
- Task ID = "task"

xinstaller.intent.action.DISABLE_SIGNATURE_CHECK
- disable signature check

xinstaller.intent.action.ENABLE_SIGNATURE_CHECK
- enable signature check

xinstaller.intent.action.DISABLE_PERMISSION_CHECK
- disable permission check

xinstaller.intent.action.ENABLE_PERMISSION_CHECK
- enable permission check

xinstaller.intent.action.CLEAR_APP_DATA
- clear app data
- Parameters: package name (string)

xinstaller.intent.action.CLEAR_APP_CACHE
- clear app cache
- Parameters: package name (string)

xinstaller.intent.action.FORCE_STOP_PACKAGE
- force stop package
- Parameters: package name (string)

xinstaller.intent.action.INSTALL_PACKAGE
- install package
- Parameters: APK file (string), flags (integer; optional)
- Flags: INSTALL_FORWARD_LOCK (0x00000001), INSTALL_REPLACE_EXISTING (0x00000002), INSTALL_ALLOW_TEST (0x00000004),
INSTALL_EXTERNAL (0x00000008), INSTALL_INTERNAL (0x00000010), INSTALL_FROM_ADB (0x00000020), INSTALL_ALL_USERS (0x00000040),
INSTALL_ALLOW_DOWNGRADE (0x00000080);

xinstaller.intent.action.DELETE_PACKAGE
- delete package
- Parameters: package name (string), flags (integer; optional)
- Flags: DELETE_KEEP_DATA (0x00000001), DELETE_ALL_USERS (0x00000002), DELETE_SYSTEM_APP (0x00000004)

xinstaller.intent.action.MOVE_PACKAGE
- move package
- Parameters: package name (string), flags (integer)
- Flags: MOVE_INTERNAL (0x00000001), MOVE_EXTERNAL_MEDIA (0x00000002)

xinstaller.intent.action.RUN_XINSTALLER
- run XInstaller

xinstaller.intent.action.REMOVE_TASK
- remove task
- Parameters: task ID (integer)
