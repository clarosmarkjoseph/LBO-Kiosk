package com.example.itdevjr1.kiosk;
import android.app.Dialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by paolohilario on 2/20/18.
 */

public class FragmentIndex extends Fragment {

    View layout;
    Utilities utilities;
    RelativeLayout relativeStart;
    TextView lblBranch,lblDate,lblClockTime,lblTap,lblTitle;
    ImageButton imgBtnSettings;
    String currentDate = "";
    BroadcastReceiver timeReceiver;
    private final SimpleDateFormat watchFormat = new SimpleDateFormat("hh:mm a");
    private ArrayList<String> arrayErrorResponse;
    String token = null;
    String SERVER_URL = "";
    RelativeLayout relativeToolbar;
    DataHandler handler;
    IntentFilter mTime = new IntentFilter(Intent.ACTION_TIME_TICK);
    private ActivitySingleton activitySingleton;
    boolean ifBranchIsOpen = true;
    JSONObject objectSchedule;
    InputMethodManager imm;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_index,container,false);
        initElements();
        return layout;
    }


    private void initElements() {

        imm                 = (InputMethodManager)getActivity().getSystemService(Service.INPUT_METHOD_SERVICE);
        activitySingleton   = new ActivitySingleton();
        utilities           = activitySingleton.Instance().getUtilityClass();
        
        handler             = new DataHandler(getActivity());
        currentDate         = utilities.getCompleteDateString(utilities.getCurrentDate());
        relativeStart       = (RelativeLayout)layout.findViewById(R.id.relativeStart);
        imgBtnSettings      = (ImageButton)layout.findViewById(R.id.imgBtnSettings);
        lblDate             = (TextView)layout.findViewById(R.id.lblDate);
        lblBranch           = (TextView)layout.findViewById(R.id.lblBranch);
        lblClockTime        = (TextView)layout.findViewById(R.id.lblClockTime);
        lblTitle            = (TextView)layout.findViewById(R.id.lblTitle);
        lblTap              = (TextView)layout.findViewById(R.id.lblTap);
        relativeToolbar     = (RelativeLayout)getActivity().findViewById(R.id.relativeToolbar);
        SERVER_URL          = utilities.returnIpAddress();


        timeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context ctx, Intent intent) {
                if (intent.getAction().compareTo(Intent.ACTION_TIME_TICK) == 0) {
                    lblClockTime.setText(watchFormat.format(new Date()));
                }
            }
        };

        getActivity().registerReceiver(timeReceiver,mTime);
        handler.open();
        Cursor querySchedule = handler.returnSchedule();
        if(querySchedule.getCount() > 0){
            querySchedule.moveToFirst();
            try {

                objectSchedule = new JSONObject(querySchedule.getString(0));
                String sched_type   = objectSchedule.getString("type");

                if(sched_type.equals("closed")){
                    ifBranchIsOpen = false;
                    lblTitle.setText("BRANCH IS CLOSED");
                    lblBranch.setText(utilities.capitalize(utilities.getBSHomeBranch()));
                    lblTap.setText("TAP ANYWHERE TO EXIT THE KIOSK");
                    lblDate.setVisibility(View.GONE);
                    lblClockTime.setVisibility(View.GONE);
                }
                else{

                    String start_time       = objectSchedule.getString("start")+":00";
                    String end_time         = objectSchedule.getString("end")+":00";
                    String current_datetime = utilities.getCurrentDateTime();
                    if(utilities.convertDateTimeToMilliSeconds(current_datetime)
                            >= utilities.convertDateTimeToMilliSeconds(start_time)
                            &&  utilities.convertDateTimeToMilliSeconds(current_datetime)
                            <= utilities.convertDateTimeToMilliSeconds(end_time)){

                        lblDate.setText(currentDate);
                        lblClockTime.setText(utilities.getCurrentTime());
                        lblBranch.setText(utilities.capitalize(utilities.getBSHomeBranch()));
                        utilities.scheduleBoot(getActivity(),end_time,timeReceiver);
                    }
                    else{
                        ifBranchIsOpen = false;
                        lblTitle.setText("KIOSK IS UNAVAILABLE");
                        lblBranch.setText(utilities.capitalize(utilities.getBSHomeBranch()));
                        lblDate.setText("Branch Operations starts at "+utilities.getStandardTime(utilities.removeDateFromDateTime(start_time))+" - "+utilities.getStandardTime(utilities.removeDateFromDateTime(end_time)));
                        lblTap.setText("TAP ANYWHERE TO EXIT THE KIOSK");
                        lblClockTime.setVisibility(View.GONE);
                    }
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
        handler.close();

        relativeToolbar.setVisibility(View.GONE);
        relativeStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              startKiosk();
            }
        });

        imgBtnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verifyUser();
            }
        });
    }

    private void startKiosk() {
        if(ifBranchIsOpen == false){
            getActivity().finish();
        }
        else{
            FragmentIndexStart fragment  = new FragmentIndexStart();
            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            fragmentTransaction.replace(R.id.frameLayout, fragment,"FragmentIndexStart");
            fragmentTransaction.addToBackStack("FragmentIndex");
            fragmentTransaction.commit();
        }
    }

    private void verifyUser() {

        token           =  utilities.getToken();
        if(token == null || token.equals("null") || token.equals("")){
            utilities.showDialogMessage("Settings is not available","Sorry, the settings is only available when administrator is already logged-in","info");
        }
        else{

            final Dialog dialog                 = new Dialog(getActivity());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.popup_dialog);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            TextView lbldialog_title            = (TextView) dialog.findViewById(R.id.lbldialog_title);
            TextView lbldialog_message          = (TextView) dialog.findViewById(R.id.lbldialog_message);
            Button btndialog_cancel             = (Button) dialog.findViewById(R.id.btndialog_cancel);
            Button btndialog_confirm            = (Button) dialog.findViewById(R.id.btndialog_confirm);
            ImageButton imgBtnClose             = (ImageButton) dialog.findViewById(R.id.imgBtn_dialog_close);
            final EditText txtUserPassword      = (EditText) dialog.findViewById(R.id.txtUserPassword);
            LinearLayout linearPassword         = (LinearLayout) dialog.findViewById(R.id.linearPassword);
            linearPassword.setVisibility(View.VISIBLE);
            btndialog_cancel.setVisibility(View.GONE);

            txtUserPassword.setLongClickable(false);
            lbldialog_title.setText("Administrator Dashboard");
            lbldialog_message.setText("Hi, please enter administrator / account password");

            final Dialog myDialog = dialog;
            btndialog_confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    checkAccount(myDialog,txtUserPassword);
                }
            });
            imgBtnClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    imm.hideSoftInputFromWindow(txtUserPassword.getWindowToken(), 0);
                    myDialog.dismiss();
                }
            });

            dialog.show();
        }
    }

    private void checkAccount(final Dialog dialog, final EditText txtUserPassword) {

        String password = txtUserPassword.getText().toString();
        if(password.trim() == null){
            utilities.showDialogMessage("Password must not empty","Please provide a password","error");
            return;
        }
        if (password.trim().isEmpty()){
            utilities.showDialogMessage("Password must not empty","Please provide a valid password","error");
            return;
        }
        else{

            utilities.showProgressDialog("Verifying User....");
            String url      = SERVER_URL+"/api/kiosk/settings/getSettings?token="+token;
            final String myPassword = password;
            StringRequest postRequest = new StringRequest(Request.Method.POST, url,new Response.Listener<String>() {
                @Override
                public void onResponse(String response)  {
                    utilities.hideProgressDialog();
                    dialog.dismiss();
                    loadSettings();
                }
            },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        imm.hideSoftInputFromWindow(txtUserPassword.getWindowToken(), 0);
                        utilities.hideProgressDialog();
                        arrayErrorResponse  = utilities.errorHandling(error);
                        String content      = arrayErrorResponse.get(1);
                        utilities.showDialogMessage("Failed to Aunthenticate!",content,"error");
                    }
                })
            {
                @Override
                public Map<String, String> getParams() throws AuthFailureError {
                    Map<String,String> params = new HashMap<>();
                    params.put("password", myPassword);
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
    }

    private void loadSettings() {

        FragmentKioskSettings fragment              = new FragmentKioskSettings();
        FragmentTransaction fragmentTransaction     = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.enter, R.anim.exit,R.anim.left_to_right,R.anim.right_to_left);
        fragmentTransaction.replace(R.id.frameLayout, fragment,"FragmentSettings");
        fragmentTransaction.addToBackStack("FragmentIndex");
        fragmentTransaction.commit();
    }






    @Override
    public void onDestroy() {
        Log.e("Destroy","Destroy");
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e("Pause","Pause");
        if (timeReceiver != null){
            getActivity().unregisterReceiver(timeReceiver);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e("Resume","Resume");
        if (timeReceiver != null){
            getActivity().registerReceiver(timeReceiver,mTime);
        }

    }
}
