package com.asus.launcher.DemoAppLock;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Method;

/**
 * Created by MingChun_Hsu on 2015/10/8.
 */
public class AppLockAPI {

    public static final String TODO = "ToDo";
    public static final String PACKAGE_NAME = "PackageName";
    public static final int TODO_SEND_GA = 1;
    public static final int TODO_LOCK = 2;
    public static final int TODO_UNLOCK = 3;

    private static final String TAG = "Launcher_AppLock";
    private static final String LAUNCHER_PACKAGE_NAME = "com.asus.launcher";
    private static final Uri CONTENT_URI_SECURED_NEW = Uri.parse("content://com.asus.launcher.applockprovider/secures");
    private static final Uri CONTENT_URI_SECURED_OLD = Uri.parse("content://applock_mode/secures");
    private static final Uri CONTENT_URI_NEW = Uri.parse("content://com.asus.launcher.applockprovider/locked_apps");
    private static final Uri CONTENT_URI_OLD = Uri.parse("content://applock_mode/locked_apps");
    private static final String ACTIVITY_NOT_FOUND = "App isn\'t installed.";

    public static boolean isLauncherSupport(Context context) {

        Context launcherContext = getPackageContext(context, "com.asus.launcher");

        if (launcherContext != null) {
            try {
                int id = launcherContext.getResources().getIdentifier(
                        "support_lock_from_other_apps", "bool", "com.asus.launcher");
                boolean support = launcherContext.getResources().getBoolean(id);
                Log.d(TAG, "isHave = " + support);
                return support;
            } catch (Resources.NotFoundException e) {
                Log.d(TAG, e.toString());
            }
        }
        return false;
    }

    public static Context getPackageContext(Context context, String packageName) {
        try {
            return context.createPackageContext(packageName, Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Package name " + packageName + " not found");
            return null;
        }
    }

    public static void sendGaMenuDisplay(Context context, String pkg) {
        Intent intent = new Intent();
        intent.setAction("asus.intent.action.APP_LOCK");
        intent.putExtra(TODO, TODO_SEND_GA);
        intent.putExtra(PACKAGE_NAME, pkg);
        context.sendBroadcast(intent);
    }

    public static void lockThisOrNot(Context context, String pkg, Boolean toLock) {
        Intent intent = new Intent();
        intent.setAction("asus.intent.action.APP_LOCK");
        if (toLock) {
            intent.putExtra(TODO, TODO_LOCK);
            intent.putExtra(PACKAGE_NAME, pkg);
        } else {
            intent.putExtra(TODO, TODO_UNLOCK);
            intent.putExtra(PACKAGE_NAME, pkg);
        }
        context.sendBroadcast(intent);
    }

    public static int getAppVersionCode(Context ctx, String pkgName) {
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pkgInfo = pm.getPackageInfo(pkgName, 0);
            return pkgInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return 0;
        }
    }



    // +++ API +++

    public static boolean checkIfLocked(Context context, String pkg) {
        // check new AppLock DB
        Cursor cursorNew = context.getContentResolver().query(
                CONTENT_URI_NEW, new String[]{"name", "value"}, null, null, null);
        if (checkIfLockedGivenCursor(cursorNew, pkg)) {
            return true;
        }

        // check old AppLock DB
        Cursor cursorOld = context.getContentResolver().query(
                CONTENT_URI_OLD, new String[]{"name", "value"}, null, null, null);
        if (checkIfLockedGivenCursor(cursorOld, pkg)) {
            return true;
        }

        // not found
        return false;
    }

    public static boolean checkIfActivated(Context context) {
        // check new AppLock DB
        Cursor cursorNew = context.getContentResolver().query(
                CONTENT_URI_SECURED_NEW, new String[]{"name", "value"}, null, null, null);
        // TODO: crash when uri error
        if (checkIfActivated(cursorNew)) {
            return true;
        }

        // check old AppLock DB
        Cursor cursorOld = context.getContentResolver().query(
                CONTENT_URI_SECURED_OLD, new String[]{"name", "value"}, null, null, null);
        if (checkIfActivated(cursorOld)) {
            return true;
        }

        // not found
        return false;
    }

    public static void toPlay(Context context) {
        Intent startIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + LAUNCHER_PACKAGE_NAME));
        startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(startIntent);
        } catch (android.content.ActivityNotFoundException anfe) {
            Uri uri = isCnSku() ?
                    Uri.parse("http://www.wandoujia.com/apps/" + LAUNCHER_PACKAGE_NAME) :
                    Uri.parse("http://play.google.com/store/apps/details?id=" + LAUNCHER_PACKAGE_NAME);
            startIntent = new Intent(Intent.ACTION_VIEW, uri);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivitySafely(context, startIntent);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    // --- API ---

    private static boolean checkIfLockedGivenCursor(Cursor cursor, String pkg) {
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    Log.v(TAG, cursor.getString(0));
                    if (pkg.equals(cursor.getString(0)) && cursor.getInt(1) == 1) {
                        return true;
                    }
                }
            } finally {
                cursor.close();
            }
        }
        return false;
    }

    private static boolean checkIfActivated(Cursor cursor) {
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    Log.v(TAG, cursor.getString(0));
                    if ("activated".equals(cursor.getString(0)) && "true".equals(cursor.getString(1))) {
                        return true;
                    }
                }
            } finally {
                cursor.close();
            }
        }
        return false;
    }

    private static boolean isCnSku() {
        return isSku("cn") || isSku("cucc") || isSku("cta") || isSku("lr") || isSku("iqy");
    }

    private static boolean isSku(String sku) {
        return sku.equalsIgnoreCase(getSystemProperties("ro.build.asus.sku", "").trim());
    }

    private static String getSystemProperties(String property, String defaultValue) {
        String rtn = defaultValue;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Object ob = c.newInstance();
            Method m = c.getMethod("get", new Class[] {
                    String.class, String.class
            });
            rtn = (String)m.invoke(ob, new Object[] {
                    property, defaultValue
            });
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return rtn;
    }

    private static void startActivitySafely(Context context, Intent intent) {
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, ACTIVITY_NOT_FOUND, Toast.LENGTH_SHORT).show();
        }
    }
}
