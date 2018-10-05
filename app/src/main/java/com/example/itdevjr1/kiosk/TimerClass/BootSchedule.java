package com.example.itdevjr1.kiosk.TimerClass;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.itdevjr1.kiosk.FragmentStep1;
import com.example.itdevjr1.kiosk.R;

import java.util.TimerTask;

/**
 * Created by paolohilario on 2/8/18.
 */
public class BootSchedule extends TimerTask {
    FragmentActivity context;
    BroadcastReceiver broadcastReceiver;

    public BootSchedule(FragmentActivity activity,BroadcastReceiver timeReceiver) {
        this.context = activity;
        this.broadcastReceiver = timeReceiver;
    }

    public void run() {

        //write your code here
        context.runOnUiThread(new Runnable() {
            public void run() {
                final Dialog dialog                 = new Dialog(context);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.popup_loading);
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                TextView lblTitle            = (TextView) dialog.findViewById(R.id.lbldialog_title);
                final TextView lblMessage          = (TextView) dialog.findViewById(R.id.lbldialog_message);
                Button btnConfirm            = (Button) dialog.findViewById(R.id.btnConfirm);

                final Dialog myDialog = dialog;
                lblTitle.setText("Branch operation is Finished!");
                final CountDownTimer countDownTimer = new CountDownTimer(20000,1000) {

                    public void onTick(long millisUntilFinished) {
                        lblMessage.setText("Branch is closed at this time. Shutting down in "+millisUntilFinished / 1000+ " seconds");
                    }
                    public void onFinish() {

                        lblMessage.setText("Branch is closed at this time. Shutting down in 0 second");
                        dialog.dismiss();
                        myDialog.dismiss();
                        context.finish();
                    }
                };
                countDownTimer.start();

                btnConfirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        myDialog.dismiss();
                        context.finish();
                    }
                });

                dialog.show();
            }
        });




    }
}