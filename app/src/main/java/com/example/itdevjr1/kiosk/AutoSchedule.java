package com.example.itdevjr1.kiosk;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by paolohilario on 2/12/18.
 */

public class AutoSchedule  {

    private static AutoSchedule instance;
    JSONArray arrayBranchSchedule = new JSONArray();
    Utilities utilities;

    public static AutoSchedule Instance() {
        if (instance == null) {
            instance = new AutoSchedule();
        }
        return instance;
    }


    public void setCurrentSchedule(JSONArray arraySchedules){
        this.arrayBranchSchedule = arraySchedules;
    }
    public JSONArray getCurrentSchedule(){
        return this.arrayBranchSchedule;
    }



//    private void setArraySchedules(JSONObject objectClientData,String start_time) {
//
//    }

//    public boolean ifScheduleIsConflict(Context context,String initStart,String miliStart,String miliEnd){
//
//        boolean ifConflict = false;
//
//        try {
//
//            Date time1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(miliStart);
//            Calendar calendar1 = Calendar.getInstance();
//            calendar1.setTime(time1);
//
//            Date time2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(miliEnd);
//            Calendar calendar2 = Calendar.getInstance();
//            calendar2.setTime(time2);
//            calendar2.add(Calendar.DATE, 1);
//
//            Date d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(initStart);
//            Calendar calendar3 = Calendar.getInstance();
//            calendar3.setTime(d);
//            calendar3.add(Calendar.DATE, 1);
//
//            Date x = calendar3.getTime();
//
//            if (x.after(calendar1.getTime()) && x.before(calendar2.getTime())) {
//                //checkes whether the current time is between 14:49:00 and 20:11:13.
//                ifConflict = true;
//                Toast.makeText(context,"Positive: Index"+x+" IFCONFLICT: "+ifConflict,Toast.LENGTH_SHORT).show();
//                return ifConflict;
//            }
//        }
//        catch (ParseException e) {
//            e.printStackTrace();
//        }
//        return ifConflict;
//    }



    public int getLatestTime(int roomCount){
        int index = -1;
        for(int x = 0; x < this.arrayBranchSchedule.length(); x++){
            try {
                JSONObject objectSched = arrayBranchSchedule.getJSONObject(x);

            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return index;
    }

    //Time only(24 hrs)
    public String removeDateFromDateTime(String dateTime) {

        String returnDate = "";
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = format.parse(dateTime);
            SimpleDateFormat month_date = new SimpleDateFormat("HH:mm:ss");
            returnDate = month_date.format(date);
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        return returnDate;
    }

    public String addTime(){
        String newTime = "";
        String myTime = "14:10";
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date d = null;
        try {
            d = df.parse(myTime);
            Calendar cal = Calendar.getInstance();
            cal.setTime(d);
            cal.add(Calendar.MINUTE, 10);
            newTime = String.valueOf(cal.getTime());
            return newTime;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return newTime;
    }





}
