package com.asus.launcher.DemoAppLock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by MingChun_Hsu on 2015/10/8.
 */
public class AppLockReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (AppLockBridge.PKG_NAME.equals(intent.getStringExtra(AppLockAPI.PACKAGE_NAME))) {
            AppLockBridge.sIsLocked = intent.getBooleanExtra("isLocked", false);

            Log.d("mingchun", "sIsLocked = " + AppLockBridge.sIsLocked);
        }
    }
}
