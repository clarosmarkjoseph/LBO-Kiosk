package com.example.itdevjr1.kiosk;

/**
 * Created by OrangeApps Zeus on 12/15/2015.
 */
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.BatteryManager;
import android.util.Log;

public class MyReceiver extends BroadcastReceiver
{
    DataHandler handler;
    BroadcastReceiver receiver;
    MediaPlayer alarm;
    @Override
    public void onReceive(Context context, Intent intent){
        alarm = MediaPlayer.create(context, R.raw.siren_noise);
        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        if((plugged == BatteryManager.BATTERY_PLUGGED_AC)) {
            if(alarm.isPlaying()) {
                alarm.pause();
            }
        }
        else if ((plugged == BatteryManager.BATTERY_PLUGGED_USB)) {
            if(alarm.isPlaying()) {
                alarm.pause();
            }
        }
        else {
            alarm.start();
        }





//        Log.e("STATUS","STATUS"+status);
//
//        receiver  = new BroadcastReceiver() {
//            public void onReceive(Context context, Intent intent) {
//                int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
//
//                alarm.setVolume(100, 100);
//                alarm.setLooping(true);
//                alarm.start();
//
//                if((plugged == BatteryManager.BATTERY_PLUGGED_AC))
//                {
//                    if(alarm.isPlaying())
//                    {
//                        alarm.pause();
//                    }
//                }
//                else if ((plugged == BatteryManager.BATTERY_PLUGGED_USB))
//                {
//                    if(alarm.isPlaying())
//                    {
//                        alarm.pause();
//                    }
//                }
//                else
//                {
//                    alarm.start();
//                }
//            }
//
//        };
//        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
//        context.registerReceiver(receiver, filter);
    }
}