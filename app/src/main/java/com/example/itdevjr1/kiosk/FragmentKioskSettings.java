package com.example.itdevjr1.kiosk;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by paolohilario on 2/19/18.
 */

public class FragmentKioskSettings extends Fragment {
    View layout;
    CardView cardAlarm,cardWifi,cardShutdown;
    ImageView imgAlarm;
    TextView lblAlarm,lblAlarmCaption;
    DataHandler handler;
    Button btnPrev;
    String status = "";
    Utilities utilities;
    String SERVER_URL = "";
    private ArrayList<String> arrayErrorResponse;
    TextView lblModuleTitle,lblModuleCaption;
    RelativeLayout relativeToolbar;
    ActivitySingleton activitySingleton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_kiosk_settings,container,false);
        initElements();
        return layout;

    }

    private void initElements() {
        activitySingleton   = new ActivitySingleton();
        utilities           = activitySingleton.Instance().getUtilityClass();
        handler             = new DataHandler(getActivity());
        cardAlarm           = (CardView)layout.findViewById(R.id.cardAlarm);
        cardWifi            = (CardView)layout.findViewById(R.id.cardWifi);
        cardShutdown        = (CardView)layout.findViewById(R.id.cardShutdown);
        imgAlarm            = (ImageView)layout.findViewById(R.id.imgAlarm);
        lblAlarm            = (TextView)layout.findViewById(R.id.lblAlarm);
        lblAlarmCaption     = (TextView)layout.findViewById(R.id.lblAlarmCaption);
        btnPrev             = (Button)layout.findViewById(R.id.btnPrev);

        relativeToolbar     = (RelativeLayout)getActivity().findViewById(R.id.relativeToolbar);
        lblModuleTitle      = (TextView)getActivity().findViewById(R.id.lblModuleTitle);
        lblModuleCaption    = (TextView)getActivity().findViewById(R.id.lblModuleCaption);
        showToolbar();

        checkIfAlarmIsEnabled();

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showConfirmationPopup();
            }
        });

        cardAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               setAlarm();
            }
        });

        cardWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            }
        });
        cardShutdown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verifyLogout();
            }
        });

    }


    private void showConfirmationPopup(){

        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_dialog);
        TextView lbldialog_title            = (TextView) dialog.findViewById(R.id.lbldialog_title);
        TextView lbldialog_message          = (TextView) dialog.findViewById(R.id.lbldialog_message);
        Button btndialog_cancel             = (Button) dialog.findViewById(R.id.btndialog_cancel);
        Button btndialog_confirm            = (Button) dialog.findViewById(R.id.btndialog_confirm);
        ImageButton imgBtnClose             = (ImageButton) dialog.findViewById(R.id.imgBtn_dialog_close);
        RelativeLayout relativeToolbar      = (RelativeLayout) dialog.findViewById(R.id.relativeToolbar);

        btndialog_cancel.setVisibility(View.VISIBLE);

        relativeToolbar.setBackgroundColor(getActivity().getResources().getColor(R.color.laybareWarning));
        btndialog_confirm.setBackgroundColor(getActivity().getResources().getColor(R.color.laybareWarning));

        lbldialog_title.setText("Confirmation");
        lbldialog_message.setText("Are you sure you want to exit Kiosk Settings? This cannot be undone.");

        btndialog_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                getFragmentManager().popBackStack("FragmentIndex", FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });
        btndialog_cancel.setOnClickListener(new View.OnClickListener() {
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

    private void showToolbar() {

        ToolbarCaptionClass toolbarCaptionClass = new ToolbarCaptionClass();
        ArrayList<String> arrayList = toolbarCaptionClass.returnCaptions(11);
        relativeToolbar.setVisibility(View.VISIBLE);
        lblModuleTitle.setText(String.valueOf(arrayList.get(0)));
        lblModuleCaption.setText(String.valueOf(arrayList.get(1)));

    }

    private void verifyLogout() {

        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_dialog);
        TextView lbldialog_title            = (TextView) dialog.findViewById(R.id.lbldialog_title);
        TextView lbldialog_message          = (TextView) dialog.findViewById(R.id.lbldialog_message);
        Button btndialog_cancel             = (Button) dialog.findViewById(R.id.btndialog_cancel);
        Button btndialog_confirm            = (Button) dialog.findViewById(R.id.btndialog_confirm);
        ImageButton imgBtnClose             = (ImageButton) dialog.findViewById(R.id.imgBtn_dialog_close);

        btndialog_cancel.setVisibility(View.VISIBLE);
        lbldialog_title.setText("Log-Out Confirmation");
        lbldialog_message.setText("Are you sure you want to logged-out this Kiosk("+utilities.capitalize(utilities.getBSHomeBranch())+")");
        final Dialog myDialog = dialog;
        btndialog_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myDialog.dismiss();
                logoutUser();
            }
        });

        imgBtnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myDialog.dismiss();
            }
        });
        btndialog_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myDialog.dismiss();
            }
        });
        dialog.show();

    }

    private void logoutUser() {
        utilities.showProgressDialog("Logging out...("+utilities.getBSHomeBranch()+")");
        SERVER_URL              = utilities.returnIpAddress();
        final String token      = utilities.getToken();
        final String user_id    = utilities.getUserID();
        String url              = SERVER_URL+"/api/user/destroyToken";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,new Response.Listener<String>() {
                @Override
                public void onResponse(String response)  {
                    utilities.hideProgressDialog();

                    ActivitySingleton activitySingleton = new ActivitySingleton();
                    activitySingleton.Instance().resetAllDetails();

                    handler = new DataHandler(getActivity());
                    handler.open();
                    handler.deleteUserAccount();
                    handler.deleteBranch();
                    handler.deleteProduct();
                    handler.deleteService();
                    handler.deleteToken();
                    handler.deleteWaiver();
                    handler.deleteSchedule();
                    handler.close();
//                    getFragmentManager().popBackStack(null, android.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    FragmentLogin fragmentLogin = new FragmentLogin();
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.setCustomAnimations(R.anim.enter, R.anim.exit,R.anim.left_to_right,R.anim.right_to_left);
                    fragmentTransaction.replace(R.id.frameLayout, fragmentLogin,"FragmentLogin");
                    fragmentTransaction.commit();
                }
            },
            new Response.ErrorListener(){
                @Override
                public void onErrorResponse(VolleyError error) {
                    utilities.hideProgressDialog();
                    arrayErrorResponse = utilities.errorHandling(error);
                    String title    = arrayErrorResponse.get(0);
                    String content  = arrayErrorResponse.get(1);
                    utilities.showDialogMessage("Please try again!",content,"error");
                }
            })
            {
                @Override
                public Map<String, String> getParams() throws AuthFailureError {
                    Map<String,String> params = new HashMap<>();
                    params.put("token", token);
                    params.put("user_id", user_id);
                    return params;
                }
            };

        postRequest.setRetryPolicy(
                new DefaultRetryPolicy(
                        6000,
                        3,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getInstance(getActivity()).addToRequestQueue(postRequest);

    }


    private void setAlarm() {

        if(status.equals("on")){
            status = "off";
            imgAlarm.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.a_alarm_on));
            lblAlarm.setText("Enable Alarm");
            handler = new DataHandler(getActivity());
            handler.open();
            handler.setAlarm("off");
            handler.close();
            if(utilities.checkIfPlugged() == false){
                AlarmSingleton alarmSingleton = new AlarmSingleton();
                alarmSingleton.Instance().getAlarm().pause();
            }
            utilities.showDialogMessage("Successfully set-up!","You have successfully turned-off The Kiosk Alarm","success");
        }
        else{
            status = "on";
            imgAlarm.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.a_alarm_off));
            lblAlarm.setText("Disable Alarm");
            handler = new DataHandler(getActivity());
            handler.open();
            handler.setAlarm("on");
            handler.close();
            if(utilities.checkIfPlugged() == false){
                AlarmSingleton alarmSingleton = new AlarmSingleton();
                alarmSingleton.Instance().getAlarm().start();
            }
            utilities.showDialogMessage("Successfully set-up!","You have successfully turned-on The Kiosk Alarm","success");

        }
    }

    private void checkIfAlarmIsEnabled() {

        handler.open();
        Cursor query  = handler.getAlarmStatus();
        if(query.getCount() > 0){
            query.moveToFirst();
            status      = query.getString(0);
            if(status.equals("on")){
                imgAlarm.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.a_alarm_off));
                lblAlarm.setText("Disable Alarm");
            }
            else{
                imgAlarm.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.a_alarm_on));
                lblAlarm.setText("Enable Alarm");
            }
        }
        handler.close();
    }


}
