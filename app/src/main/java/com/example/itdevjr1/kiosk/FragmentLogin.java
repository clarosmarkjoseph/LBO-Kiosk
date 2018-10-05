package com.example.itdevjr1.kiosk;


import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toolbar;

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
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by paolohilario on 1/24/18.
 */

public class FragmentLogin extends Fragment {
    View layout;
    String SERVER_URL = "";
    Utilities utilities;
    EditText txtUsername,txtPassword;
    Button btnSubmit;
    String device = "Branch Kiosk";
    String devicename;
    DataHandler handler;
    ArrayList<String>arrayErrorResponse;
    Toolbar toolbar;
    Typeface myTypeface;
    TextView lblTitle;
    RelativeLayout relativeToolbar;
    TextView lblModuleTitle,lblModuleCaption;
    public Socket mSocket;
    static String serial_no = Build.SERIAL;
    ActivitySingleton activitySingleton;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        layout = inflater.inflate(R.layout.fragment_login,container,false);
        setFonts();
        initElements();
        return layout;
    }


    private void initElements(){

        handler             = new DataHandler(getActivity());
        activitySingleton   = new ActivitySingleton();
        utilities           = activitySingleton.Instance().getUtilityClass();

        SERVER_URL          = utilities.returnIpAddress();
        txtUsername         = (EditText)layout.findViewById(R.id.txtUsername);
        txtPassword         = (EditText)layout.findViewById(R.id.txtPassword);
        btnSubmit           = (Button)layout.findViewById(R.id.btnSubmit);


        relativeToolbar     = (RelativeLayout)getActivity().findViewById(R.id.relativeToolbar);
        lblModuleTitle      = (TextView)getActivity().findViewById(R.id.lblModuleTitle);
        lblModuleCaption    = (TextView)getActivity().findViewById(R.id.lblModuleCaption);
        showToolbar();

        devicename  = utilities.getDeviceName();
        lblTitle    = (TextView)layout.findViewById(R.id.lblTitle);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginBranch();
            }
        });
        lblTitle.setTypeface(myTypeface);

        iterateBranchAssigned();
    }

    private void iterateBranchAssigned(){
        handler.open();
        Cursor cursor       = handler.returnBranch();
        if(cursor.getCount() > 0){
            cursor.moveToFirst();
            try {
                JSONObject objectBranch = new JSONObject(cursor.getString(0));
                String branch_name      = objectBranch.optString("branch_name","Not specified");
                lblTitle.setText("This Device is registered to: " +branch_name);
            }
            catch (JSONException e) {
                e.printStackTrace();
                lblTitle.setText("Branch Kiosk ");
            }
        }
        else{
            lblTitle.setText("Branch Kiosk ");
        }

    }

    private void showToolbar() {

        ToolbarCaptionClass toolbarCaptionClass = new ToolbarCaptionClass();
        ArrayList<String> arrayList = toolbarCaptionClass.returnCaptions(12);
        relativeToolbar.setVisibility(View.VISIBLE);
        lblModuleTitle.setText(String.valueOf(arrayList.get(0)));
        lblModuleCaption.setText(String.valueOf(arrayList.get(1)));

    }

    private void loginBranch(){

        final String username = txtUsername.getText().toString();
        final String password = txtPassword.getText().toString();

        if (username.isEmpty() || username.equals("") || username == null || username.equals("null")){
            utilities.showDialogMessage("Incomplete Details","Please enter your email address","info");
            txtUsername.requestFocus();
        }
        else if (password.isEmpty() || password.equals("") || password == null || password.equals("null")){
            utilities.showDialogMessage("Incomplete Details","Please enter your password","info");
            txtPassword.requestFocus();
        }
        else if (!username.isEmpty() && utilities.isEmailValid(username) == false){
            utilities.showDialogMessage("Invalid Email address","Please enter a valid email address","info");
            txtPassword.requestFocus();
        }
        else{
            utilities.showProgressDialog("Logging in: Please wait...");
            String log_url =  SERVER_URL+"/auth/loginKiosk";
            StringRequest postRequest = new StringRequest(Request.Method.POST, log_url,new Response.Listener<String>() {
                @Override
                public void onResponse(String response)  {
                    try {

                        Log.e("response:",response);
                        JSONObject objectData       = new JSONObject(response);
                        JSONArray arrayService      = objectData.getJSONArray("services");
                        JSONArray arrayProduct      = objectData.getJSONArray("products");
                        JSONObject objectBranch     = objectData.getJSONObject("branches");
                        JSONObject objectUser       = objectData.getJSONObject("user");
                        String token                = objectData.getString("token");
                        JSONArray arrayWaiver       = objectData.getJSONArray("waivers");
                        JSONObject objectSchedule   = objectData.getJSONObject("branch_schedules");

                        handler.open();
                        handler.deleteUserAccount();
                        handler.deleteBranch();
                        handler.deleteProduct();
                        handler.deleteService();
                        handler.deleteToken();
                        handler.deleteWaiver();
                        handler.insertUserAccount(objectUser.toString());
                        handler.insertBranch(objectBranch.toString());
                        handler.insertProduct(arrayProduct.toString());
                        handler.insertService(arrayService.toString());
                        handler.insertToken(token);
                        handler.insertWaiver(arrayWaiver.toString());
                        handler.insertSchedule(objectSchedule.toString());
                        handler.close();
                        loadFragment();

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
                    Log.e("ERROR",arrayErrorResponse.toString());
                    utilities.showDialogMessage("Error!",arrayErrorResponse.get(1),"error");
                }
            })
            {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("email", username);
                    params.put("password", password);
                    params.put("device",device);
                    params.put("device_serial",serial_no);
                    params.put("device_info",devicename);
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
    }

    private void loadFragment() {
        //socket connection
        mSocket  = activitySingleton.Instance().getSocketApplication();
        mSocket.on("sendAppointmentData",sendAppointmentData);
        utilities.hideProgressDialog();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {

                FragmentIndex fragmentIndexFragment = new FragmentIndex();
                getFragmentManager().beginTransaction()
                        .replace(R.id.frameLayout, fragmentIndexFragment)
                        .addToBackStack(null)
                        .setCustomAnimations(R.anim.enter, R.anim.exit,R.anim.left_to_right,R.anim.right_to_left)
                        .commit();
            }
        }, 500);
    }


    //sign to kiosk(admin will press the button)
    public Emitter.Listener sendAppointmentData = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    try {
                        JSONObject objectAppointment    = data.getJSONObject("appointment");
                        String device_id                = data.getString("device_id");
                        int appointment_id              = objectAppointment.getInt("id");
                        showSignature(objectAppointment, appointment_id);
                        if(device_id.equals(serial_no)){
                            showSignature(objectAppointment,appointment_id);
                        }
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };





    private void showSignature(JSONObject objectAppointment, int appointment_id){

        Bundle bundle = new Bundle();
        String myMessage = objectAppointment.toString();
        bundle.putString("appointment", myMessage);
        FragmentAcknowledgement fragmentSplashScreen = new FragmentAcknowledgement();
        if(fragmentSplashScreen.isVisible()){
            utilities.showDialogMessage("Please wait","PLS WET","error");
            mSocket.emit("receiveAppointmentData",objectAppointment,true);
        }
        else{
            mSocket.emit("receiveAppointmentData",objectAppointment,true);
            fragmentSplashScreen.setArguments(bundle);
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frameLayout, fragmentSplashScreen,"FragmentAcknowledgement");
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
    }


    private void setFonts() {
        toolbar             = (Toolbar)layout.findViewById(R.id.myToolbar);
        myTypeface          = Typeface.createFromAsset(getActivity().getAssets(), "fonts/LobsterTwo-Regular.ttf");

    }

}
