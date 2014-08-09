package mx.furbyx92.android.xposed.mods.privacyAOSPA;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.util.HashMap;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;


/**
 * Created by furbyx92 on 8/08/14.
 */
public class PrivacyFix implements IXposedHookLoadPackage {
    private static final String PACKAGE_NAME = PrivacyFix.class.getPackage().getName();

    private static final String AOSPA_SETTINGS_PACKAGE = "com.android.settings";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals(AOSPA_SETTINGS_PACKAGE)) {
            return;
        }

        findAndHookMethod(
                "com.android.settings.applications.AppOpsState",
                lpparam.classLoader,
                "getAppEntry",
                Context.class,
                HashMap.class,
                String.class,
                ApplicationInfo.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        Context context = (Context) param.args[0];
                        String packageName = (String) param.args[2];
                        ApplicationInfo appInfo = (ApplicationInfo) param.args[3];
                        PackageManager mPm = context.getPackageManager();
                        try {
                            appInfo = mPm.getApplicationInfo(packageName,
                                    PackageManager.GET_DISABLED_COMPONENTS
                                            | PackageManager.GET_UNINSTALLED_PACKAGES);
                        } catch (PackageManager.NameNotFoundException e) {
                            XposedBridge.log("Unable to find info for package " + packageName);
                        }
                        appInfo.flags = 0;
                        param.args[3] = appInfo;
                    }
                });
    }
}
