package com.example.itdevjr1.kiosk;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import pl.droidsonroids.gif.GifTextView;

/**
 * Created by paolohilario on 2/22/18.
 */

public class FragmentNoInternet extends Fragment {

    View layout;
    GifTextView gifTextView;
    TextView lblModuleTitle, lblModuleCaption;
    RelativeLayout relativeToolbar;
    LinearLayout linearRetry,linearWifi;
    RelativeLayout relativeLoading;
    Utilities utilities;
    String SERVER_URL       = "";
    DataHandler handler;
    String token            = "";
    private ArrayList<String> arrayErrorResponse;
    String module_type      = "";
    static String serial_no = Build.SERIAL;
    String device_status    = "false";
    private ActivitySingleton activitySingleton;
    String versionName     = BuildConfig.VERSION_NAME;



    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_no_internet,container,false);
        initElements();
        return  layout;

    }

    private void initElements() {

        activitySingleton   = new ActivitySingleton();
        utilities           = activitySingleton.Instance().getUtilityClass();
        
        relativeToolbar     = (RelativeLayout)getActivity().findViewById(R.id.relativeToolbar);
        lblModuleTitle      = (TextView)getActivity().findViewById(R.id.lblModuleTitle);
        lblModuleCaption    = (TextView)getActivity().findViewById(R.id.lblModuleCaption);
        linearRetry         = (LinearLayout) layout.findViewById(R.id.linearRetry);
        linearWifi          = (LinearLayout) layout.findViewById(R.id.linearWifi);
        gifTextView         = (GifTextView)layout.findViewById(R.id.gifTextView);
        relativeLoading     = (RelativeLayout)layout.findViewById(R.id.relativeLoading);
        handler             = new DataHandler(getActivity());
        SERVER_URL          = utilities.returnIpAddress();
        token               = utilities.getToken();
        module_type         = this.getArguments().getString("type");
        showToolbar();

        linearWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            }
        });

        linearRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reloadToken();
            }
        });

    }

    private void reloadToken() {

        gifTextView.setVisibility(View.GONE);
        relativeLoading.setVisibility(View.VISIBLE);

        if(module_type.equals("device")){

            utilities.showProgressDialog("Checking Device if registered....");
            String log_url =  SERVER_URL+"/api/kiosk/checkDeviceIfRegistered";
            StringRequest postRequest = new StringRequest(Request.Method.POST, log_url,new Response.Listener<String>() {
                @Override
                public void onResponse(String response)  {
                    try {
                        utilities.hideProgressDialog();
                        JSONObject objectResult     = new JSONObject(response);
                        JSONArray arrayDevice       = objectResult.getJSONArray("data");
                        boolean checkIfLatest       = objectResult.getBoolean("ifKioskIsUpdated");

                        if(checkIfLatest == false){
                            utilities.hideProgressDialog();
                            showUpdateForm();
                        }
                        else{
                            if(arrayDevice.length() > 0){
                                device_status = "true";
                                handler.open();
                                Cursor queryDevice = handler.returnDeviceStatus();
                                if(queryDevice.getCount() <= 0) {
                                    handler.insertDeviceStatus(serial_no, device_status);
                                }
                                else{
                                    handler.updateDeviceStatus(serial_no,device_status);
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
                    Log.e("response","ERROR");
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
        else{

            utilities.showProgressDialog("Checking Branch aunthentication...");
            String branch_id    = utilities.getHomeBranchID();
            String log_url      =  SERVER_URL+"/api/kiosk/checkLoggedInToken/"+branch_id+"/"+versionName+"/APP_LAYBARE_KIOSK_VERSION?token="+token;
            StringRequest postRequest = new StringRequest(Request.Method.GET, log_url,new Response.Listener<String>() {
                @Override
                public void onResponse(String response)  {
                try {
                    utilities.hideProgressDialog();
                    JSONObject objectResult         = new JSONObject(response);
                    JSONObject objectClient         = objectResult.getJSONObject("data");
                    JSONObject objectSchedule       = objectResult.getJSONObject("branch_schedules");
                    boolean checkIfLatest           = objectResult.getBoolean("ifKioskIsUpdated");

                    if(checkIfLatest == false){
                        utilities.hideProgressDialog();
                        showUpdateForm();
                    }
                    else{
                        handler                         = new DataHandler(getActivity());
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
                        if(utilities.logoutRemoveCredential() == true){
                            loadToMainMenu(false);
                        }
                        else{
                            loadToMainMenu(false);
                        }
                    }
                    else{

                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // Do something after 5s = 5000ms
                                gifTextView.setVisibility(View.VISIBLE);
                                relativeLoading.setVisibility(View.GONE);
                            }
                        }, 1000);
                    }
                }
            });

            postRequest.setRetryPolicy(
                    new DefaultRetryPolicy(
                            6000,
                            3,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            MySingleton.getInstance(getActivity()).addToRequestQueue(postRequest);
        }


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

        btndialog_cancel.setVisibility(View.GONE);
        imgBtnClose.setVisibility(View.GONE);

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





    private void showToolbar() {
        relativeToolbar.setVisibility(View.VISIBLE);
        ToolbarCaptionClass toolbarCaptionClass = new ToolbarCaptionClass();
        ArrayList<String> arrayList = toolbarCaptionClass.returnCaptions(13);
        lblModuleTitle.setText(String.valueOf(arrayList.get(0)));
        lblModuleCaption.setText(String.valueOf(arrayList.get(1)));
    }



    private void loadToMainMenu(final boolean ifLoggedIn){


        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                if (ifLoggedIn == true){
                    FragmentIndex fragmentIndexFragment = new FragmentIndex();
                    FragmentManager fragmentManager     = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,android.R.anim.fade_in, android.R.anim.fade_out);
                    fragmentTransaction.replace(R.id.frameLayout, fragmentIndexFragment);
                    fragmentTransaction.commit();
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



}
