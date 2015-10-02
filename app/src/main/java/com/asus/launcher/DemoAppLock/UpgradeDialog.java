package com.asus.launcher.DemoAppLock;

import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

/**
 * Created by MingChun_Hsu on 2015/9/24.
 */
public class UpgradeDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog dialog = new Dialog(getActivity());

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        View promptView = LayoutInflater.from(getActivity()).inflate(R.layout.upgrade_layout, null);
        Button button = (Button) promptView.findViewById(R.id.upgrade_now);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppLockBridge.toPlay(getActivity());
                dialog.dismiss();
            }
        });

        dialog.setContentView(promptView);

//        //Grab the window of the dialog, and change the width
//        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
//        Window window = dialog.getWindow();
//        lp.copyFrom(window.getAttributes());
//        //This makes the dialog take up the full width
//        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
//        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
//        window.setAttributes(lp);

        return dialog;
    }
}
