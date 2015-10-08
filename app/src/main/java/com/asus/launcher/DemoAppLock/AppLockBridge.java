package com.asus.launcher.DemoAppLock;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.support.v4.app.DialogFragment;

public class AppLockBridge extends AppCompatActivity {

    public static final String PKG_NAME = "com.asus.launcher.DemoAppLock";
    public static boolean sIsLocked;
    public static boolean sActionLock;

    private Menu mMenu;
    private MenuItem mAppLockMenu;
    private boolean mFirstTime = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();

        sIsLocked = AppLockAPI.checkIfLocked(this, PKG_NAME);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        mAppLockMenu = menu.findItem(R.id.action_lock_gallery);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        // Update menu & action
        if (sIsLocked) {
            mAppLockMenu.setTitle(R.string.action_unlock_gallery);
        } else {
            mAppLockMenu.setTitle(R.string.action_lock_gallery);
        }
        sActionLock = sIsLocked;

        // prevent from first launch
        if (mFirstTime == true) {
            mFirstTime = false;
            return true;
        }
        AppLockAPI.sendGaMenuDisplay(this, PKG_NAME);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_lock_gallery) {
            if (!AppLockAPI.isLauncherSupport(getApplicationContext())) {
                DialogFragment dialog = new UpgradeDialog();
                dialog.show(getSupportFragmentManager(), "UpgradeDialog");
            } else if (!AppLockAPI.checkIfActivated(getApplicationContext())) {
                DialogFragment dialog = new ActivateDialog();
                dialog.show(getSupportFragmentManager(), "ActivateDialog");
            } else {
                AppLockAPI.lockThisOrNot(getApplicationContext(), PKG_NAME, !sActionLock);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onLockThis(View view) {
        AppLockAPI.lockThisOrNot(this, PKG_NAME, true);
    }

    public void onUnlockThis(View view) {
        AppLockAPI.lockThisOrNot(this, PKG_NAME, false);
    }

    public void onCheckClick(View view) {
        boolean isLocked = AppLockAPI.checkIfLocked(this, PKG_NAME);
        Toast.makeText(this, "locked? " + isLocked, Toast.LENGTH_LONG).show();
    }

    public void onPlayClick(View view) {
        AppLockAPI.toPlay(this);
    }

    public void onVersionCheck(View view) {
        Toast.makeText(this, "support =" + AppLockAPI.isLauncherSupport(getApplicationContext()), Toast.LENGTH_SHORT).show();
    }

    public void onShowUpgrade(View view) {
        DialogFragment dialog = new UpgradeDialog();
        dialog.show(getSupportFragmentManager(), "UpgradeDialog");
    }

    public void onShowActivate(View view) {
        DialogFragment dialog = new ActivateDialog();
        dialog.show(getSupportFragmentManager(), "ActivateDialog");
    }
}
