package com.asus.launcher.DemoAppLock;

import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;


/**
 * Created by MingChun_Hsu on 2015/9/24.
 */
public class ActivateDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog dialog = new Dialog(getActivity());

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        View promptView = LayoutInflater.from(getActivity()).inflate(R.layout.activate_layout, null);
        Button button = (Button) promptView.findViewById(R.id.lock_now);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppLockBridge.lockThisOrNot(getActivity(), AppLockBridge.PKG_NAME, AppLockBridge.sIsLocked);
                dialog.dismiss();
            }
        });

        dialog.setContentView(promptView);

        return dialog;
    }
}
