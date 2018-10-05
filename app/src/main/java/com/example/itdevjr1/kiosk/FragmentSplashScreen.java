package com.example.itdevjr1.kiosk;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by IT DEV on 12/15/2015.
 */
public class FragmentSplashScreen extends Fragment {
    public  String SERVER_URL = "",token = "";
    DataHandler handler;
    View layout;
    Utilities utilities;
    ArrayList<String>arrayErrorResponse;
    ActivitySingleton activitySingleton;
    static String serial_no = Build.SERIAL;
    String device_status = "false";
    public Socket mSocket;
    RelativeLayout relativeToolbar;
    String versionName     = BuildConfig.VERSION_NAME;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_splashscreen,container,false);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                initElement();
            }
        });
        return layout;
    }

    private void initElement() {

        activitySingleton   = new ActivitySingleton();
        utilities           = activitySingleton.Instance().getUtilityClass();

        relativeToolbar     = (RelativeLayout)getActivity().findViewById(R.id.relativeToolbar);
        SERVER_URL          = "https://lbo.lay-bare.com";
//        SERVER_URL          = "http://lbo-testing.azurewebsites.net";
        handler             = new DataHandler(getActivity());
        relativeToolbar.setVisibility(View.GONE);
        handler.open();
        Cursor c = handler.returnIPAddress();

        if(c.getCount() <= 0){
            handler.insertIpAddress(SERVER_URL);
        }

        Cursor queryDevice = handler.returnDeviceStatus();
        if(queryDevice.getCount() <= 0){
            handler.insertDeviceStatus(serial_no,device_status);
            checkDeviceIfRegistered();
        }
        else{
            queryDevice.moveToFirst();
            device_status = queryDevice.getString(1);
            Cursor user = handler.returnUserAccount();
            if(user.getCount() > 0){
                checkUserTokenIsValid();
            }
            else{
                if(device_status.equals("false")){
                    checkDeviceIfRegistered();
                }
                else{
                    loadToMainMenu(false);
                }
            }
        }

        handler.close();
    }

    private void checkDeviceIfRegistered() {

        utilities.showProgressDialog("Checking Device if registered....");
        String log_url =  SERVER_URL+"/api/kiosk/checkDeviceIfRegistered";
        StringRequest postRequest = new StringRequest(Request.Method.POST, log_url,new Response.Listener<String>() {
            @Override
            public void onResponse(String response)  {
                    try {
                        utilities.hideProgressDialog();
                        Log.e("print",response);
                        JSONObject objectResult = new JSONObject(response);
                        JSONObject objectDevice  = objectResult.optJSONObject("data");
                        boolean checkIfLatest    = objectResult.getBoolean("ifKioskIsUpdated");

                        if(checkIfLatest == false){
                            utilities.hideProgressDialog();
                            showUpdateForm();
                        }
                        else{

                            if(objectDevice.has("id") || objectDevice.has("branch_name")){

                                device_status       = "true";
                                handler.open();
                                handler.deleteUserAccount();
                                handler.deleteBranch();
                                Cursor queryDevice = handler.returnDeviceStatus();
                                Cursor queryBranch = handler.returnBranch();

                                if(queryDevice.getCount() <= 0) {
                                    handler.insertDeviceStatus(serial_no, device_status);
                                }
                                else{
                                    handler.updateDeviceStatus(serial_no, device_status);
                                }

                                if(queryBranch.getCount() <= 0){
                                    handler.insertBranch(String.valueOf(objectDevice));
                                }
                                else{
                                    handler.updateBranch(String.valueOf(objectDevice));
                                }
                                handler.close();
                                FragmentLogin fragmentLogin     = new FragmentLogin();
                                FragmentManager fragmentManager = getFragmentManager();
                                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,android.R.anim.fade_in, android.R.anim.fade_out);
                                fragmentTransaction.replace(R.id.frameLayout, fragmentLogin);
                                fragmentTransaction.commit();
                            }
                            else{
                                handler.open();
                                handler.deleteUserAccount();
                                handler.deleteBranch();
                                handler.close();
                                RegisterDeviceSerial registerDeviceSerial   = new RegisterDeviceSerial();
                                FragmentManager fragmentManager             = getActivity().getSupportFragmentManager();
                                FragmentTransaction fragmentTransaction     = fragmentManager.beginTransaction();
                                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,android.R.anim.fade_in, android.R.anim.fade_out);
                                fragmentTransaction.replace(R.id.frameLayout, registerDeviceSerial);
                                fragmentTransaction.commit();
                            }
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            },
            new Response.ErrorListener(){
                @Override
                public void onErrorResponse(VolleyError error) {
                    utilities.hideProgressDialog();
                    FragmentNoInternet fragmentNoInternet       = new FragmentNoInternet();
                    FragmentManager fragmentManager             = getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction     = fragmentManager.beginTransaction();
                    fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,android.R.anim.fade_in, android.R.anim.fade_out);
                    fragmentTransaction.replace(R.id.frameLayout, fragmentNoInternet);
                    Bundle bundle = new Bundle();
                    bundle.putString("type", "device");
                    fragmentNoInternet.setArguments(bundle);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                }
            })
            {
                @Override
                public Map<String, String> getParams() throws AuthFailureError {
                    Map<String,String> params = new HashMap<>();
                    params.put("serial_no", serial_no);
                    params.put("kiosk_version_no", versionName);
                    params.put("configuration", "APP_LAYBARE_KIOSK_VERSION");
                    return params;
                }
            };

        postRequest.setRetryPolicy(
                new DefaultRetryPolicy(
                        10000,
                        3,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getInstance(getActivity()).addToRequestQueue(postRequest);
    }


    private void showUpdateForm() {

        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_dialog);
        TextView lbldialog_title            = (TextView) dialog.findViewById(R.id.lbldialog_title);
        TextView lbldialog_message          = (TextView) dialog.findViewById(R.id.lbldialog_message);
        Button btndialog_cancel             = (Button) dialog.findViewById(R.id.btndialog_cancel);
        Button btndialog_confirm            = (Button) dialog.findViewById(R.id.btndialog_confirm);
        ImageButton imgBtnClose             = (ImageButton) dialog.findViewById(R.id.imgBtn_dialog_close);
        RelativeLayout relativeToolbar      = (RelativeLayout) dialog.findViewById(R.id.relativeToolbar);

        imgBtnClose.setVisibility(View.GONE);
        btndialog_cancel.setVisibility(View.GONE);

        relativeToolbar.setBackgroundColor(getActivity().getResources().getColor(R.color.laybareInfo));
        btndialog_confirm.setBackgroundColor(getActivity().getResources().getColor(R.color.laybareInfo));

        lbldialog_title.setText("New Software Update!");
        lbldialog_message.setText("Please update to continue using the Kiosk.");

        btndialog_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent viewIntent =
                            new Intent("android.intent.action.VIEW",
                                    Uri.parse( "https://play.google.com/store/apps/details?id=" + getActivity().getPackageName()));
                    startActivity(viewIntent);
                }
                catch(Exception e) {
                    Toast.makeText(getActivity(),"Unable to Connect Try Again...",
                            Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
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


    private void checkUserTokenIsValid() {

        utilities.showProgressDialog("Checking Device Aunthentication...");
        token = utilities.getToken();
        String branch_id    = utilities.getHomeBranchID();
        String log_url      =  SERVER_URL+"/api/kiosk/checkLoggedInToken/"+branch_id+"/"+versionName+"/APP_LAYBARE_KIOSK_VERSION?token="+token;
        Log.e("log_url",log_url);
        StringRequest postRequest = new StringRequest(Request.Method.GET, log_url,new Response.Listener<String>() {
            @Override
            public void onResponse(String response)  {
                try {
                    JSONObject objectClients    = new JSONObject(response);
                    JSONObject objectClient     = objectClients.getJSONObject("data");
                    JSONObject objectSchedule   = objectClients.getJSONObject("branch_schedules");
                    boolean checkIfLatest       = objectClients.getBoolean("ifKioskIsUpdated");

                    if(checkIfLatest == false){
                        utilities.hideProgressDialog();
                        showUpdateForm();
                    }
                    else{
                        handler = new DataHandler(getActivity());
                        handler.open();
                        handler.deleteUserAccount();
                        handler.insertUserAccount(objectClient.toString());
                        Cursor querySchedule = handler.returnSchedule();
                        if(querySchedule.getCount() > 0){
                            handler.updateSchedule(objectSchedule.toString());
                        }
                        else{
                            handler.insertSchedule(objectSchedule.toString());
                        }
                        handler.close();
                        loadToMainMenu(true);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        },
            new Response.ErrorListener(){
                @Override
                public void onErrorResponse(VolleyError error) {
                    utilities.hideProgressDialog();
                    arrayErrorResponse = utilities.errorHandling(error);
                    String title = arrayErrorResponse.get(0);
                    if(title.equals("Token Expired")){
                        Toast.makeText(getActivity(),arrayErrorResponse.get(1),Toast.LENGTH_SHORT).show();
                        if(utilities.logoutRemoveCredential() == true){
                            loadToMainMenu(false);
                        }
                        else{
                            loadToMainMenu(false);
                        }
                    }
                    else{
                        FragmentNoInternet fragmentNoInternet       = new FragmentNoInternet();
                        FragmentManager fragmentManager             = getActivity().getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction     = fragmentManager.beginTransaction();
                        fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,android.R.anim.fade_in, android.R.anim.fade_out);
                        fragmentTransaction.replace(R.id.frameLayout, fragmentNoInternet);
                        Bundle bundle = new Bundle();
                        bundle.putString("type", "token");
                        fragmentNoInternet.setArguments(bundle);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();
                    }
                }
            });

        postRequest.setRetryPolicy(
                new DefaultRetryPolicy(
                        10000,
                        3,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getInstance(getActivity()).addToRequestQueue(postRequest);

    }

    private void loadToMainMenu(final boolean ifLoggedIn){

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                utilities.hideProgressDialog();
                if (ifLoggedIn == true){

                    handler = new DataHandler(getActivity());
                    handler.open();
                    Cursor c = handler.returnWaiver();
                    c.moveToFirst();
                    try {
                        ActivitySingleton activitySingleton = new ActivitySingleton();
                        JSONArray arrayWaiver               = new JSONArray(c.getString(0));
                        mSocket  = activitySingleton.Instance().getSocketApplication();
                        mSocket.on("sendAppointmentData",sendAppointmentData);
                        mSocket.on("signingTimeout",signingTimeout);
                        mSocket.on("refreshKiosk",refreshKiosk);

                        FragmentIndex fragmentIndexFragment = new FragmentIndex();
                        FragmentManager fragmentManager     = getFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,android.R.anim.fade_in, android.R.anim.fade_out);
                        fragmentTransaction.replace(R.id.frameLayout, fragmentIndexFragment,"FragmentIndex");
                        fragmentTransaction.commit();
                        handler.close();
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    FragmentLogin fragmentLogin = new FragmentLogin();
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,android.R.anim.fade_in, android.R.anim.fade_out);
                    fragmentTransaction.replace(R.id.frameLayout, fragmentLogin);
                    fragmentTransaction.commit();
                }
            }
        });

    }


    //sign to kiosk(admin will press the button)
    public Emitter.Listener sendAppointmentData = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            //error
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    try {
                        JSONObject objectAppointment    = data.getJSONObject("appointment");
                        String device_id                = data.getString("device_id");
                        Log.e("objectAppointment", String.valueOf(objectAppointment));
                        Log.e("device_id", String.valueOf(device_id));
                        if(device_id.equals(serial_no)){
                            showSignature(objectAppointment);
                        }
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    //cancel acknowledgement
    public Emitter.Listener signingTimeout = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    try {
                        String device_id                        = data.getString("device_id");
                        if(device_id.equals(serial_no)){
                            hideSignature();
                        }
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    //refresh kiosk(go to index)
    public Emitter.Listener refreshKiosk = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String device_id  = (String) args[0];
                    if(device_id.equals(serial_no)){
                        hideSignature();
                    }
                }
            });
        }
    };

    private void hideSignature() {
        getFragmentManager().popBackStack("FragmentIndex", FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }


    private void showSignature(JSONObject objectAppointment){

        Bundle bundle = new Bundle();
        String myMessage = objectAppointment.toString();
        bundle.putString("appointment", myMessage);

        Fragment fragmentIndex = getActivity().getSupportFragmentManager().findFragmentById(R.id.frameLayout);

        if(fragmentIndex instanceof FragmentIndex){

            FragmentAcknowledgement fragmentAcknowledgement = new FragmentAcknowledgement();
            if(fragmentAcknowledgement != null && fragmentAcknowledgement.isVisible()){
                mSocket.emit("receiveAppointmentData",objectAppointment,false);
//                utilities.showDialogMessage("Please wait","PLS WET","error");
                return;
            }
            else{
                mSocket.emit("receiveAppointmentData",objectAppointment,true);
                fragmentAcknowledgement.setArguments(bundle);
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.frameLayout, fragmentAcknowledgement,"FragmentAcknowledgement");
                fragmentTransaction.addToBackStack("FragmentIndex");
                fragmentTransaction.commit();
                return;
            }
        }
        else{
            mSocket.emit("receiveAppointmentData",objectAppointment,false);
//            utilities.showDialogMessage("Please wait","Sorry, some person are using the kiosk right now. Please try again","error");
            return;
        }
    }









}
