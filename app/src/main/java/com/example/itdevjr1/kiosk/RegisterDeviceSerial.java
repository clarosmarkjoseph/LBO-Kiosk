package com.example.itdevjr1.kiosk;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by paolohilario on 2/23/18.
 */

public class RegisterDeviceSerial extends Fragment {

    View layout;
    Utilities utilities;
    static String serial_no = Build.SERIAL;
    TextView lblSerial,lblStatus;
    TextView lblModuleTitle,lblModuleCaption;
    RelativeLayout relativeToolbar;
    ActivitySingleton activitySingleton;
    Socket mSocket;
    DataHandler handler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout           = inflater.inflate(R.layout.register_device_serial,container,false);
        initElements();
        return layout;
    }


    private void initElements() {
        activitySingleton   = new ActivitySingleton();
        utilities           = activitySingleton.Instance().getUtilityClass();

        lblSerial           = (TextView)layout.findViewById(R.id.lblSerial);
        lblStatus           = (TextView)layout.findViewById(R.id.lblStatus);
        relativeToolbar     = (RelativeLayout)getActivity().findViewById(R.id.relativeToolbar);
        lblModuleTitle      = (TextView)getActivity().findViewById(R.id.lblModuleTitle);
        lblModuleCaption    = (TextView)getActivity().findViewById(R.id.lblModuleCaption);
        mSocket             = activitySingleton.Instance().getSocketApplication();
        showToolbar();
        lblSerial.setText(serial_no);
        mSocket.on("checkSerial",checkSerial);

    }


    private void showToolbar() {
        ToolbarCaptionClass toolbarCaptionClass = new ToolbarCaptionClass();
        ArrayList<String> arrayList = toolbarCaptionClass.returnCaptions(16);
        relativeToolbar.setVisibility(View.VISIBLE);
        lblModuleTitle.setText(String.valueOf(arrayList.get(0)));
        lblModuleCaption.setText(String.valueOf(arrayList.get(1)));
    }

    //appointments for today
    public Emitter.Listener checkSerial = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    try {
                        JSONObject data         = (JSONObject) args[0];
                        JSONObject objectSerial = new JSONObject(String.valueOf(data));
                        String  serial          = objectSerial.optString("serial_no","");
                        if(serial.equals(serial_no)){

                            lblStatus.setText("Status: Device succesfully registered \nPlease wait for a sec...");
                            utilities.showProgressDialog("Connected. Please wait....");
                            handler = new DataHandler(getActivity());
                            handler.open();
                            handler.updateDeviceStatus(serial,"true");
                            mSocket.emit("verifiedSerial",true,data);

                            handler.close();
                            final Handler handlers = new Handler();
                            handlers.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mSocket.off("checkSerial");
                                    utilities.hideProgressDialog();
                                    FragmentLogin fragmentLogin     = new FragmentLogin();
                                    FragmentManager fragmentManager = getFragmentManager();
                                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                    fragmentTransaction.setCustomAnimations(R.anim.enter, R.anim.exit,R.anim.left_to_right,R.anim.right_to_left);
                                    fragmentTransaction.replace(R.id.frameLayout, fragmentLogin,"FragmentLogin");
                                    fragmentTransaction.commit();
                                }
                            }, 2000);
                        }
                        else{
                            mSocket.emit("verifiedSerial",objectSerial);
                            utilities.showDialogMessage("Incorrect Credentials","Sorry, user's serial is incorrect","error");
                        }
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });
        }
    };


}
