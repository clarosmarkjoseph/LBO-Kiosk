package com.example.itdevjr1.kiosk;

import android.app.Dialog;
import android.app.FragmentManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.github.nkzawa.socketio.client.Socket;
import com.williamww.silkysignature.views.SignaturePad;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
/**
 * Created by paolohilario on 1/25/18.
 */

public class FragmentStep3 extends Fragment {
    View layout;
    private ActivitySingleton activitySingleton;
    Utilities utilities;
    String SERVER_URL = "";
    TextView lblAgreement,lblName;
    SignaturePad mSignaturePad;
    private boolean isSignatured = false;
    Button btnPrev,btnNext,btnClearPad;
    CheckBox checkAgree;
    ArrayList<String>arrayErrorResponse;
    String clientName;
    TextView lblModuleTitle,lblModuleCaption;
    RelativeLayout relativeToolbar;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_step3,container,false);
        initElements();

        return  layout;

    }

    private void initElements(){

        //search client elements
        activitySingleton       = new ActivitySingleton();
        utilities               = activitySingleton.Instance().getUtilityClass();
        SERVER_URL              = utilities.returnIpAddress();
        lblAgreement            = (TextView)layout.findViewById(R.id.lblAgreement);
        lblName                 = (TextView)layout.findViewById(R.id.lblName);
        btnPrev                 = (Button) layout.findViewById(R.id.btnPrev);
        btnNext                 = (Button)layout.findViewById(R.id.btnNext);
        btnClearPad             = (Button)layout.findViewById(R.id.btnClearPad);
        checkAgree              = (CheckBox) layout.findViewById(R.id.checkAgree);
        clientName              = getClientName();
        relativeToolbar         = (RelativeLayout)getActivity().findViewById(R.id.relativeToolbar);
        lblModuleTitle          = (TextView)getActivity().findViewById(R.id.lblModuleTitle);
        lblModuleCaption        = (TextView)getActivity().findViewById(R.id.lblModuleCaption);
        showToolbar();

        utilities.hideProgressDialog();

        String message = "I ";
        message+= " "+clientName+", ";
        message+="of legal age, fully understood that the procedure's involves certain risks to my body, " +
                " which includes scratches, pain, soreness, injury, sickness, irittation, or rash, etc., which may be present and/or after the procedure and I fully accept and assume such risk and responsibility for losses, cost, and damages I may occur. I hereby release and discharge LAY BARE WAXING SALON, its stockholders, directors, franchisees, officers and technicians from all liability, claims, damages, losses, arising from the services they have rendered into me." +
                " I acknowledge that I have read this Agreement and fully understand its terms and conditions.";
        lblAgreement.setText(message);
        lblName.setText(clientName);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            lblAgreement.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);
        }
        mSignaturePad = (SignaturePad) layout.findViewById(R.id.signature_pad);
        mSignaturePad.setOnSignedListener(new SignaturePad.OnSignedListener() {
            @Override
            public void onStartSigning() {
//               mSignaturePad.getSignatureBitmap();
            }

            @Override
            public void onSigned() {
                //Event triggered when the pad is signed
                isSignatured = true;
            }

            @Override
            public void onClear() {
                //Event triggered when the pad is cleared
                isSignatured = false;
            }
        });

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().popBackStack("FragmentStep2", FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bitmap       =  mSignaturePad.getSignatureBitmap();
                if(isSignatured == false){
                    utilities.showDialogMessage("Signature required! ","Sorry, Signature is empty. Please signed it before proceed.","info");
                    return;
                }
                if(checkAgree.isChecked() == false){
                    utilities.showDialogMessage("Terms and agreement!","Please check agree to the terms and conditions.","info");
                    return;
                }
                else{
                    String bitmapString                 = utilities.getStringImage(bitmap);
                    validateAppointment(bitmapString);
                }
            }
        });
        btnClearPad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSignaturePad.clear();
            }
        });

    }



    public String getEndTime(String start_time,int duration){
        String endTime          = "";
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date;
        try {
            date            = format.parse(start_time);
            Calendar cal    = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.MINUTE, duration);
            endTime         = format.format(cal.getTime());
            return endTime;
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        return endTime;
    }

    private void validateAppointment(String bitmapString){

        JSONObject objectClientData         = activitySingleton.Instance().getObjectAppointment();
        JSONObject objectWaiver             = activitySingleton.Instance().getWaivers();
        JSONArray arraySelectedItem         = activitySingleton.Instance().getSelectedArrayItems();
        try {

            objectWaiver.put("signature", bitmapString);
            objectClientData.put("waiver_data", objectWaiver);

            int totalDuration               = 0;
            Calendar calendar               = Calendar.getInstance();
            String todayDate                = utilities.getCurrentDate();

            int start_hour                  = calendar.get(Calendar.HOUR_OF_DAY);
            int start_minute                = calendar.get(Calendar.MINUTE);
            int timeRemainder               = start_minute % 5;
            int inc                         = 5 - timeRemainder;
            start_minute                    = start_minute + inc;
            String end_time                 = "";
            String start_time               = todayDate + " " + start_hour + ":" + start_minute + ":00";
            Date newDate                    = utilities.convertStringToDatetime(start_time);
            start_time                      = utilities.convertDatetimeToString(newDate);

            JSONArray arrayServices = new JSONArray();
            JSONArray arrayProducts = new JSONArray();
            String nextStart                = start_time;

            for (int x = 0; x < arraySelectedItem.length(); x++) {
                JSONObject objectSelected   = arraySelectedItem.getJSONObject(x);
                String item_type            = objectSelected.getString("item_type");
                int id                      = objectSelected.getInt("id");

                if(item_type.equals("services") || item_type.equals("packages")){
                    JSONObject objectService    = new JSONObject();
                    Double price                = objectSelected.getDouble("service_price");
                    int duration                = objectSelected.optInt("service_minutes", 0);
                    end_time                    = getEndTime(nextStart, duration);
                    objectService.put("id", id);
                    objectService.put("price", price);
                    objectService.put("start", start_time);
                    objectService.put("end", end_time);
                    arrayServices.put(objectService);
                    totalDuration+=duration;
                    nextStart = end_time;
                }
                else if(item_type.equals("products")){
                    JSONObject objectProduct    = new JSONObject();
                    Double price                = objectSelected.getDouble("product_price");
                    int quantity                = objectSelected.optInt("item_quantity", 0);
                    objectProduct.put("id", id);
                    objectProduct.put("price", price);
                    objectProduct.put("quantity", quantity);
                    arrayProducts.put(objectProduct);
                }
            }
            getBranchQueuing(arrayServices,arrayProducts,start_time);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void getBranchQueuing(JSONArray arrayServices, JSONArray arrayProducts, String initStartTime){

        try{
            JSONObject objectAppointment = activitySingleton.Instance().getObjectAppointment();
            objectAppointment.put("transaction_date",initStartTime);
            objectAppointment.put("services",arrayServices);
            objectAppointment.put("products",arrayProducts);
            utilities.showProgressDialog("Saving Appointment....");
            String booking_url = SERVER_URL+"/api/kiosk/addAppointments";
            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (Request.Method.POST, booking_url, objectAppointment, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                String result   = response.getString("result");
                                if(result.equals("success")){

                                    String branch_id    = utilities.getHomeBranchID();
                                    Socket mSocket      = activitySingleton.Instance().getSocketApplication();
                                    mSocket.emit("refreshAppointments",Integer.parseInt(branch_id));

                                    utilities.hideProgressDialog();
                                    showCustomPrompt("Successfully Booked!","You have Successfully booked your appointment",0);
                                }
                            }
                            catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            utilities.hideProgressDialog();
                            arrayErrorResponse = utilities.errorHandling(error);
                            Toast.makeText(getActivity(),arrayErrorResponse.get(1),Toast.LENGTH_LONG).show();
                            utilities.showDialogMessage("Connection Error",arrayErrorResponse.get(1),"error");
                        }
                    })
            {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Content-Type", "application/json; charset=utf-8");
                    return params;
                }
            };
            jsObjRequest.setRetryPolicy(
                    new DefaultRetryPolicy(
                            25000,
                            0,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                    )
            );
            MySingleton.getInstance(getActivity()).addToRequestQueue(jsObjRequest);
        }
        catch (JSONException e){
            e.printStackTrace();
        }
    }





    private void showCustomPrompt(String title, String message, final int action) {

        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_dialog);
        dialog.setTitle("Success");
        TextView lbldialog_title    = (TextView) dialog.findViewById(R.id.lbldialog_title);
        TextView lbldialog_message  = (TextView) dialog.findViewById(R.id.lbldialog_message);
        Button btndialog_cancel     = (Button) dialog.findViewById(R.id.btndialog_cancel);
        Button btndialog_confirm    = (Button) dialog.findViewById(R.id.btndialog_confirm);
        ImageButton imgBtnClose     = (ImageButton) dialog.findViewById(R.id.imgBtn_dialog_close);
        lbldialog_title.setText(title);
        lbldialog_message.setText(message);
        btndialog_cancel.setVisibility(View.GONE);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        imgBtnClose.setVisibility(View.GONE);

        btndialog_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (action==0){
                    activitySingleton.Instance().resetAllDetails();
                    dialog.dismiss();
                    getFragmentManager().popBackStack("FragmentIndex", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                }
            }
        });

        imgBtnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activitySingleton.Instance().setClientsArray(new JSONArray());
                dialog.dismiss();
            }
        });
        dialog.show();
    }




    private String getClientName(){
        String name = "";
        try {
            JSONObject objectAppointment = activitySingleton.Instance().getObjectAppointment();
            JSONObject objectClient      = objectAppointment.getJSONObject("client");
            name                         = utilities.capitalize(objectClient.optString("label",""));
            return name;
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return name;
    }

    private void showToolbar() {
        ToolbarCaptionClass toolbarCaptionClass = new ToolbarCaptionClass();
        ArrayList<String> arrayList = toolbarCaptionClass.returnCaptions(10);
        lblModuleTitle.setText(String.valueOf(arrayList.get(0)));
        lblModuleCaption.setText(String.valueOf(arrayList.get(1)));
    }



}