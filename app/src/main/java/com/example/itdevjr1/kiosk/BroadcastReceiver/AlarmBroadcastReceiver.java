package com.example.itdevjr1.kiosk.BroadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.BatteryManager;
import android.util.Log;

import com.example.itdevjr1.kiosk.DataHandler;
import com.example.itdevjr1.kiosk.R;
import com.example.itdevjr1.kiosk.Utilities;

/**
 * Created by paolohilario on 2/19/18.
 */

public class AlarmBroadcastReceiver extends BroadcastReceiver {

    MediaPlayer alarm = null;
    String status = "";
    int plugged = 0;
    DataHandler handler;

    public AlarmBroadcastReceiver(MediaPlayer alarms){
        this.alarm = alarms;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(handler == null){
            handler = new DataHandler(context);
            handler.open();
            Cursor c = handler.getAlarmStatus();
            if(c.getCount() > 0){
                c.moveToFirst();
                status = c.getString(0);
            }
            else{
                status = "on";
                handler.insertAlarm(status);
            }

            handler.close();
        }

        else{
            handler.open();
            Cursor c = handler.getAlarmStatus();
            if(c.getCount() > 0){
                c.moveToFirst();
                status = c.getString(0);
            }
            handler.close();
        }

        if(alarm == null){
            alarm       = MediaPlayer.create(context, R.raw.siren_noise);
        }

        plugged     = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        alarm.setVolume(100, 100);
        alarm.setLooping(true);
        if((plugged == BatteryManager.BATTERY_PLUGGED_AC)) {
            Log.e("BroadCastreceiveralarm","Charger Connected / Charger");
            if(alarm.isPlaying()) {
                alarm.pause();
            }
        }
        else if ((plugged == BatteryManager.BATTERY_PLUGGED_USB)) {
            Log.e("BroadCastreceiveralarm","Charger Connected / USB");
            if (alarm.isPlaying()) {
                alarm.pause();
            }
        }
        else {
            Log.e("BroadCastreceiveralarm","Charger Disconnected");
            if(status.equals("on")) {
                alarm.start();
            }
            else {
                alarm.pause();
            }
        }
        Log.e("myStatuses",status);
    }
}
