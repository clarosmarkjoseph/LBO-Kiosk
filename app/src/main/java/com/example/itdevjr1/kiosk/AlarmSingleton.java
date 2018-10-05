package com.example.itdevjr1.kiosk;

import android.media.MediaPlayer;

/**
 * Created by paolohilario on 2/19/18.
 */

public class AlarmSingleton {
    MediaPlayer alarm;
    private static AlarmSingleton instance;


    public static AlarmSingleton Instance() {
        if (instance == null) {
            instance = new AlarmSingleton();
        }
        return instance;
    }

    public void setAlarm(MediaPlayer alarm){
        this.alarm = alarm;
    }
    public MediaPlayer getAlarm(){
       return this.alarm;
    }



}
