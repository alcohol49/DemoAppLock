package com.asus.launcher.DemoAppLock;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.support.v4.app.DialogFragment;

import java.lang.reflect.Method;

public class AppLockBridge extends AppCompatActivity {

    static final String PKG_NAME = "com.asus.launcher.DemoAppLock";
    static boolean sIsLocked;
    private Menu menu;
    private boolean mfirstTime = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();

        sIsLocked = checkIfLocked(this, PKG_NAME);
        final Button button = (Button) findViewById(R.id.button5);
        if (sIsLocked) {
            button.setText("解鎖圖片庫");
        } else {
            button.setText("上鎖圖片庫");
        }
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!isLauncherSupport()) {
                    DialogFragment dialog = new UpgradeDialog();
                    dialog.show(getSupportFragmentManager(), "UpgradeDialog");
                    Log.d("mingchun", "UpgradeDialog");
                } else if (!checkIfActivated(getApplicationContext())) {
                    DialogFragment dialog = new ActivateDialog();
                    dialog.show(getSupportFragmentManager(), "ActivateDialog");
                    Log.d("mingchun", "ActivateDialog");
                } else {
                    lockThis(getApplicationContext(), PKG_NAME);
                    sIsLocked = !sIsLocked;
                    Log.d("mingchun", "lockThis");
                }

                if (sIsLocked) {
                    button.setText("解鎖圖片庫");
                } else {
                    button.setText("上鎖圖片庫");
                }
            }
        });

        updateMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;

        updateMenu();
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mfirstTime == true) {
            mfirstTime = false;
            return true;
        }
        sendGaMenuDisplay(PKG_NAME);
        updateMenu();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_lock_gallery) {

            if (!isLauncherSupport()) {
                DialogFragment dialog = new UpgradeDialog();
                dialog.show(getSupportFragmentManager(), "UpgradeDialog");
            } else if (!checkIfActivated(getApplicationContext())) {
                DialogFragment dialog = new ActivateDialog();
                dialog.show(getSupportFragmentManager(), "ActivateDialog");
            } else {
                lockThis(getApplicationContext(), PKG_NAME);
                sIsLocked = !sIsLocked;
            }

            updateMenu();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateMenu() {
        if (menu != null) {
            MenuItem mi = menu.findItem(R.id.action_lock_gallery);
            if (checkIfLocked(this, PKG_NAME)) {
                mi.setTitle(R.string.action_unlock_gallery);
            } else {
                mi.setTitle(R.string.action_lock_gallery);
            }
        }
    }

    public void onLockThis(View view) {
        lockThis(this, PKG_NAME);
    }

    public void onCheckClick(View view) {
        boolean isLocked = checkIfLocked(this, PKG_NAME);
        Toast.makeText(this, "locked? " + isLocked, Toast.LENGTH_LONG).show();
    }

    public void onPlayClick(View view) {
        toPlay(this);
    }

    public void onVersionCheck(View view) {
//        Toast.makeText(this, "Launcher version = " + getAppVersionCode(this, LAUNCHER_PACKAGE_NAME), Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "support =" + isLauncherSupport(), Toast.LENGTH_SHORT).show();

        // test
        DialogFragment dialog = new UpgradeDialog();
        dialog.show(getSupportFragmentManager(), "UpgradeDialog");
    }

    // -- API --

    public Context getPackageContext(String packageName) {
        try {
            return getApplicationContext().createPackageContext(packageName, Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("mingchun", "Package name " + packageName + " not found");
            return null;
        }
    }

    public boolean isLauncherSupport() {

        Context launcherContext = getPackageContext("com.asus.launcher");

        if (launcherContext != null) {
            try {
                int id = launcherContext.getResources().getIdentifier(
                        "support_lock_from_other_apps", "bool", "com.asus.launcher");
                boolean support = launcherContext.getResources().getBoolean(id);
                Log.d("mingchun", "isHave = " + support);
                return support;
            } catch (Resources.NotFoundException e) {
                Log.d("mingchun", e.toString());
            }
        }

        return false;
    }

    void sendGaMenuDisplay(String pkg) {
        Log.d("mignchun", "sendGaMenuDisplay");

        Intent intent = new Intent();
        intent.setAction("asus.intent.action.APP_LOCK");
        intent.putExtra("PackageName", pkg);
        intent.putExtra("Todo", "SendAnalytics");
        sendBroadcast(intent);
    }

    public static void lockThis(Context context, String pkg) {
        Intent intent = new Intent();
        intent.setAction("asus.intent.action.APP_LOCK");
        intent.putExtra("PackageName", pkg);
        intent.putExtra("Todo", "LockThis");
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

    private static final String TAG = "Launcher_AppLock";
    private static final String LAUNCHER_PACKAGE_NAME = "com.asus.launcher";
    private static final Uri CONTENT_URI_SECURED_NEW = Uri.parse("content://com.asus.launcher.applockprovider/secures");
    private static final Uri CONTENT_URI_SECURED_OLD = Uri.parse("content://applock_mode/secures");
    private static final Uri CONTENT_URI_NEW = Uri.parse("content://com.asus.launcher.applockprovider/locked_apps");
    private static final Uri CONTENT_URI_OLD = Uri.parse("content://applock_mode/locked_apps");
    private static final String ACTIVITY_NOT_FOUND = "App isn\'t installed.";

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
