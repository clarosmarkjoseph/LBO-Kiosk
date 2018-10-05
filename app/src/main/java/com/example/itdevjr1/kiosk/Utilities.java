package com.example.itdevjr1.kiosk;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;
import com.example.itdevjr1.kiosk.TimerClass.BootSchedule;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by paolohilario on 1/24/18.
 */

public class Utilities {
    Context context;
    NetworkResponse networkResponse;
    ArrayList arrayList = new ArrayList();
    int statusCode = 0;
    String errorData;
    JSONArray jsonArray;
    DataHandler handler;
    String SERVER_URL = "";

    Dialog popup_loading;


    public Utilities(Context ctx){
        this.context    = ctx;
        this.handler    = new DataHandler(context);
        popup_loading   = new Dialog(context);
        popup_loading.requestWindowFeature(Window.FEATURE_NO_TITLE);
        popup_loading.setContentView(R.layout.popup_loading);
        popup_loading.setCancelable(false);
        popup_loading.setCanceledOnTouchOutside(false);
    }


    public boolean logoutRemoveCredential(){
        boolean ifLoggedOut = false;
        handler = new DataHandler(context);
        handler.open();
        handler.deleteService();
        handler.deleteProduct();
        handler.deleteBranch();
        handler.deleteUserAccount();
        handler.deleteToken();
        handler.deleteSchedule();
        handler.close();
        ifLoggedOut = true;
        return ifLoggedOut;
    }

    //handling all errors in request (String,JSONObject,JSONArray)
    public ArrayList<String> errorHandling(VolleyError error){
        networkResponse = error.networkResponse;
        if(networkResponse != null){
            arrayList.clear();
            statusCode = networkResponse.statusCode;
            if(statusCode == 400){
                try {
                    errorData = new String(error.networkResponse.data,"UTF-8");
                    try {
                        JSONObject jsonObject = new JSONObject(errorData);
                        String result         = jsonObject.getString("result");
                        String value          = jsonObject.getString("error");
                        Object json           = new JSONTokener(value).nextValue();

                        String passRequest    = "";
                        if (json instanceof JSONObject){
                            jsonObject            = new JSONObject(value);
                            jsonArray             = jsonObject.getJSONArray("");
                            passRequest+="An error occurred. Please fix the following: \n";
                            for(int x = 0; x < jsonArray.length();x++){
                                passRequest += "* "+jsonArray.get(x).toString()+"\n";
                            }
                            arrayList.add("Message Alert!");
                            arrayList.add(passRequest);
                        }
                        else if (json instanceof JSONArray){
                            jsonArray             = new JSONArray(value);
                            passRequest+="An error occurred. Please fix the following: \n";
                            for(int x = 0; x < jsonArray.length();x++){
                                passRequest += "* "+jsonArray.get(x).toString();
                                if(jsonArray.length() - 1 > x){
                                    passRequest += "\n";
                                }
                            }
                            arrayList.add("Message Alert!");
                            arrayList.add(passRequest);
                        }
                        else{
                            passRequest = value;
                            arrayList.add("Message Alert!");
                            arrayList.add(passRequest);
                        }
                        return arrayList;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            else if(statusCode == 401){
                arrayList.add("Token Expired");
                arrayList.add("Sorry, Token is expired! Redirect to login");
            }
            else{
                try {
                    errorData = new String(error.networkResponse.data,"UTF-8");

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                arrayList.add("Info1");
                arrayList.add("There was a problem connecting to Lay Bare Kiosk. Please check your connection and try again");
                return arrayList;
            }
        }
        else{
            arrayList.add("Info2");
            arrayList.add("There was a problem connecting to Lay Bare Kiosk. Please check your connection and try again");
            return arrayList;
        }

        arrayList.add("Info3");
        arrayList.add("There3 was a problem connecting to Lay Bare Kiosk. Please check your connection and try again");
        return arrayList;
    }

    //general - Get Device name
    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.toLowerCase().startsWith(manufacturer.toLowerCase())) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }
    //general - Capitalize words
    public String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    //returns IP Address
    public String returnIpAddress(){
        handler = new DataHandler(context);
        handler.open();
        Cursor queryServer = handler.returnIPAddress();
        if(queryServer.getCount() > 0){
            queryServer.moveToFirst();
            SERVER_URL = queryServer.getString(0);
            return SERVER_URL;
        }
        handler.close();
        return null;
    }

    //return Token
    public String getToken(){
        handler = new DataHandler(context);
        handler.open();
        String token = null;
        Cursor queryServer = handler.returnToken();
        if(queryServer.getCount() > 0){
            queryServer.moveToFirst();
            token = queryServer.getString(0);
            return token;
        }
        handler.close();
        return null;
    }

    //returns client ID
    public String getUserID(){
        handler = new DataHandler(context);
        handler.open();
        String clientID = "0";
        Cursor queryServer = handler.returnUserAccount();
        if(queryServer.getCount() > 0){
            queryServer.moveToFirst();
            try {
                JSONObject objectData = new JSONObject(queryServer.getString(0));
                Log.e("CLIENT DATA",objectData.toString());
                clientID = objectData.getString("id");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return clientID;
        }
        handler.close();
        return null;
    }
    //returns client email
    public String getClientEmail(){
        handler = new DataHandler(context);
        handler.open();
        String clientID = "0";
        Cursor queryServer = handler.returnUserAccount();
        if(queryServer.getCount() > 0){
            queryServer.moveToFirst();
            try {
                JSONObject objectData = new JSONObject(queryServer.getString(0));
                clientID = objectData.getString("email");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return clientID;
        }
        handler.close();
        return null;
    }

    //returns client fullName
    public String getClientName(){
        handler = new DataHandler(context);
        handler.open();
        String clientName = "";
        Cursor queryServer = handler.returnUserAccount();
        if(queryServer.getCount() > 0){
            queryServer.moveToFirst();
            try {
                JSONObject objectData   = new JSONObject(queryServer.getString(0));
                clientName              = objectData.getString("first_name")+" "+objectData.getString("middle_name")+" "+objectData.getString("last_name");
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
            handler.close();
            return clientName;
        }
        handler.close();
        return null;
    }

    public String getBSHomeBranch(){

        String home_branch = "";
        handler = new DataHandler(context);
        handler.open();
        Cursor queryServer = handler.returnBranch();
        if(queryServer.getCount() > 0) {
            try {
                queryServer.moveToFirst();
                JSONObject objectData   = new JSONObject(queryServer.getString(0));
                home_branch             = objectData.getString("branch_name");
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
        handler.close();
        return home_branch;
    }

    public String getHomeBranchID(){

        String branch_id    = "";
        handler             = new DataHandler(context);
        handler.open();
        Cursor queryServer  = handler.returnBranch();
        if(queryServer.getCount() > 0) {
            try {
                queryServer.moveToFirst();
                JSONObject objectData   = new JSONObject(queryServer.getString(0));
                branch_id             = objectData.getString("id");
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
        handler.close();
        return branch_id;
    }

    //date only
    public String removeTimeFromDate(String dateTime) {

        String returnDate = "";
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = format.parse(dateTime);
            SimpleDateFormat month_date = new SimpleDateFormat("yyyy-MM-dd");
            returnDate = month_date.format(date);
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        return returnDate;
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

    public String getStandardTime(String time) {

        String returnDate = "";
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        try {
            Date date = format.parse(time);
            SimpleDateFormat month_date = new SimpleDateFormat("hh:mm a");
            returnDate = month_date.format(date);
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        return returnDate;
    }



    public String getCompleteDateMonth(String date){
        String returnDate = "";
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date dates = format.parse(date);
            SimpleDateFormat month_date = new SimpleDateFormat("MMMM dd, yyyy");
            returnDate = month_date.format(dates);
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        return returnDate;
    }

    public void showDialogMessage(String title,String message,String type) {

        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_dialog);
        TextView lbldialog_title            = (TextView) dialog.findViewById(R.id.lbldialog_title);
        TextView lbldialog_message          = (TextView) dialog.findViewById(R.id.lbldialog_message);
        Button btndialog_cancel             = (Button) dialog.findViewById(R.id.btndialog_cancel);
        Button btndialog_confirm            = (Button) dialog.findViewById(R.id.btndialog_confirm);
        ImageButton imgBtnClose             = (ImageButton) dialog.findViewById(R.id.imgBtn_dialog_close);
        RelativeLayout relativeToolbar      = (RelativeLayout) dialog.findViewById(R.id.relativeToolbar);

        btndialog_cancel.setVisibility(View.GONE);

        if(type.equals("error")){
            relativeToolbar.setBackgroundColor(context.getResources().getColor(R.color.themeRed));
            btndialog_confirm.setBackgroundColor(context.getResources().getColor(R.color.themeRed));
        }
        else if(type.equals("info")){
            relativeToolbar.setBackgroundColor(context.getResources().getColor(R.color.laybareInfo));
            btndialog_confirm.setBackgroundColor(context.getResources().getColor(R.color.laybareInfo));
        }
        else{
            relativeToolbar.setBackgroundColor(context.getResources().getColor(R.color.laybareGreen));
            btndialog_confirm.setBackgroundColor(context.getResources().getColor(R.color.laybareGreen));
        }

        lbldialog_title.setText(title);
        lbldialog_message.setText(message);

        btndialog_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        imgBtnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
    }

    //get current date -
    public String getCurrentDate() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String todayDate = sdf.format(c.getTime());
        return todayDate.toString();
    }

    //get current time -
    public String getCurrentTime() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
        String todayDate = sdf.format(c.getTime());
        return todayDate.toString();
    }

    public String getCurrentDateTime() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String todayDate = sdf.format(c.getTime());
        return todayDate.toString();
    }

    //converting datetime to miliseconds
    public long convertDateTimeToMilliSeconds(String stringDateTime){

        Date date = null;
        long milliseconds = 0;
        try {
            date         = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(stringDateTime);
            milliseconds = date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return milliseconds;
    }


    public String getCompleteDateString(String date){
        String returnDate = "";
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date dates = format.parse(date);
            SimpleDateFormat month_date = new SimpleDateFormat("EEEE, MMMM dd");
            returnDate = month_date.format(dates);
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        return returnDate;
    }

    //get age
    public int getAge(String dobString){

        Date date = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");
        try {
            date = sdf.parse(dobString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if(date == null) return 0;

        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        dob.setTime(date);

        int year = dob.get(Calendar.YEAR);
        int month = dob.get(Calendar.MONTH);
        int day = dob.get(Calendar.DAY_OF_MONTH);

        dob.set(year, month+1, day);

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)){
            age--;
        }

        return age;
    }

    public void setUniversalBigImage(final ImageView imgPosted, final String image){
        final String urlImage = image.replace(" ","%20");
        Picasso.get()
                .load(urlImage)
                .fit()
                .noFade()
                .error(R.drawable.no_image)
                .into(imgPosted, new Callback() {
                    @Override
                    public void onSuccess() {

                    }
                    @Override
                    public void onError(Exception e) {
                        Picasso.get()
                                .load(urlImage)
                                .noFade()
                                .networkPolicy(NetworkPolicy.OFFLINE)
                                .fit()
                                .error(R.drawable.no_image)
                                .into(imgPosted);
                    }
                });
    }

    public void setUniversalSmallImage(final ImageView imgPosted, final String image){

        final String urlImage = image.replace(" ","%20");
        Picasso.get()
                .load(urlImage)
                .resize(200, 200)
                .noFade()
                .error(R.drawable.no_image)
                .into(imgPosted, new Callback() {
                    @Override
                    public void onSuccess() {

                    }
                    @Override
                    public void onError(Exception e) {
                        Picasso.get()
                                .load(urlImage)
                                .noFade()
                                .networkPolicy(NetworkPolicy.OFFLINE)
                                .resize(200,200)
                                .error(R.drawable.no_image)
                                .into(imgPosted);
                    }
                });
    }



    public boolean checkIfCartHasPackage(JSONArray arrayItems){
        boolean itHas = false;
        int index               = arrayItems.length();
        for (int x = 0; x < index; x++) {
            try {
                JSONObject objectArray =  arrayItems.getJSONObject(x);
                if(objectArray.has("package_services")){
                    itHas = true;
                    return itHas;
                }
                else{
                   continue;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return itHas;
    }

    public boolean validateItem(JSONObject objectSelected,JSONArray arrayItems){
        boolean ifConflict = false;
        int index               = arrayItems.length();
        try {
            int selectedID = objectSelected.getInt("id");
            for (int x = 0; x < index; x++) {
                JSONObject objectArray = arrayItems.getJSONObject(x);
                if(objectArray.has("package_services")){
                    JSONArray arrayPackageService = objectArray.getJSONArray("package_services");
                    for(int y = 0; y < arrayPackageService.length(); y++){
                        int iterateID = arrayPackageService.getInt(y);
                        if(iterateID == selectedID){
                            ifConflict = true;
                            return  ifConflict;
                        }
                    }
                }
                else{
                    return ifConflict;
                }
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return ifConflict;
    }


    public boolean ifItemIsAlready(int selectedID, JSONArray arrayItems){
        boolean ifPackage = false;
        int index               = arrayItems.length();

        for (int x = 0; x < index; x++) {
            try {
                JSONObject objectArray = arrayItems.getJSONObject(x);
                int itemID = objectArray.getInt("id");
                if (objectArray.has("service_code")) {
                    if (itemID == selectedID) {
                        ifPackage = true;
                        return ifPackage;
                    }
                }
                else {
                    if (itemID == selectedID) {
                        ifPackage = true;
                        return ifPackage;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return ifPackage;
    }

    public boolean checkIfPackage(JSONObject objecDetails){

        boolean ifPackage = false;
        if(objecDetails.has("package_services")){
            ifPackage = true;
        }
        return  ifPackage;
    }

    public void scheduleBoot(FragmentActivity a, String end_dateTime, BroadcastReceiver timeReceiver){
        //the Date and time at which you want to execute
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
//            end_dateTime
            date = dateFormatter .parse(end_dateTime);
            //Now create the time and schedule it
            Timer timer = new Timer();
            //Use this if you want to execute it once
            timer.schedule(new BootSchedule(a,timeReceiver), date);
            //Use this if you want to execute it repeatedly
            //int period = 10000;//10secs
            //timer.schedule(new MyTimeTask(), date, period );
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
    }

    //Bitmap to String(Base64)
    public String getStringImage(Bitmap bitmap) {
        String imgString;
        bitmap = resizeImageBitmap(bitmap);
        if(bitmap != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
            byte[] b    = baos.toByteArray();
            imgString   = Base64.encodeToString(b,Base64.DEFAULT);
        }
        else{
            imgString = "";
        }
        return "data:image/jpeg;base64,"+imgString;
    }

    //resize bitmap(atleast 2mb max)
    public Bitmap resizeImageBitmap(Bitmap bitmap) {
        Bitmap returnBitmap = Bitmap.createScaledBitmap(bitmap,(int)(bitmap.getWidth()*0.5), (int)(bitmap.getHeight()*0.5), true);
        return returnBitmap;
    }


    //convert int or string into currenct with zero at the end
    public String convertToCurrency(String num){
        double number = Double.parseDouble(num);
        String str = String.format("%,.2f", number);
        return str;
    }


    public void showProgressDialog(String message){

        TextView lblTitle            = (TextView) popup_loading.findViewById(R.id.lbldialog_title);
        final TextView lblMessage    = (TextView) popup_loading.findViewById(R.id.lbldialog_message);
        Button btnConfirm            = (Button) popup_loading.findViewById(R.id.btnConfirm);
        final Dialog myDialog = popup_loading;
        lblTitle.setText("Loading...");
        lblMessage.setText(message);
        btnConfirm.setVisibility(View.GONE);
        popup_loading.show();

    }

    public void hideProgressDialog(){
        if(popup_loading.isShowing()){
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    popup_loading.dismiss();
                }
            }, 300);
        }
    }

    public boolean checkIfPlugged(){
        boolean isPlugged= false;
        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        isPlugged = plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            isPlugged = isPlugged || plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS;
        }
        Log.e("checkIfPlugged", String.valueOf(isPlugged));
        return isPlugged;
    }

    public boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public Date convertStringToDatetime(String dateTime){
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            date = format.parse(dateTime);
            return date;
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public int getRoomCount(){
        int roomCounts = 0;

        handler.open();

        Cursor cursorBranch = handler.returnBranch();
        cursorBranch.moveToFirst();
        try {
            JSONObject objectBranch = new JSONObject(cursorBranch.getString(0));
            roomCounts = objectBranch.optInt("rooms_count");
            return roomCounts;
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return roomCounts;
    }


    public String convertDatetimeToString(Date dateTime){

        String stringDatetime = "";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        stringDatetime              = dateFormat.format(dateTime);
        return stringDatetime;
    }


    public String getSafeSubstring(String s){
        int maxLength = 10;
        if(!TextUtils.isEmpty(s)){
            if(s.length() >= maxLength){
                return s.substring(0, maxLength)+"...";
            }
        }
        return s;
    }




}
