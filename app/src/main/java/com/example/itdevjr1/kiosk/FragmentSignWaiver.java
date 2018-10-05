package com.example.itdevjr1.kiosk;

import android.app.Dialog;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by paolohilario on 2/28/18.
 */

public class FragmentSignWaiver extends Fragment {
    View layout;
    Button btnPrev,btnNext,btnClearPad;
    RelativeLayout relativeToolbar;
    TextView lblModuleTitle,lblModuleCaption,lblAgreement,lblName;
    SignaturePad signaturePad;
    private boolean isSignatured = false;
    CheckBox checkAgree;
    String gender               = "";
    String transaction_id       = "";
    String client_name          = "";
    ActivitySingleton activitySingleton;
    Utilities utilities;
    String SERVER_URL = "";
    ArrayList<String>arrayErrorResponse;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_sign_waiver,container,false);
        initElements();
        return layout;
    }

    private void initElements() {
        activitySingleton       = new ActivitySingleton();
        utilities               = activitySingleton.Instance().getUtilityClass();

        lblModuleTitle          = (TextView)getActivity().findViewById(R.id.lblModuleTitle);
        lblModuleCaption        = (TextView)getActivity().findViewById(R.id.lblModuleCaption);
        relativeToolbar         = (RelativeLayout)getActivity().findViewById(R.id.relativeToolbar);
        btnPrev                 = (Button) layout.findViewById(R.id.btnPrev);
        btnNext                 = (Button) layout.findViewById(R.id.btnNext);
        btnClearPad             = (Button) layout.findViewById(R.id.btnClearPad);
        lblName                 = (TextView)layout.findViewById(R.id.lblName);
        lblAgreement            = (TextView)layout.findViewById(R.id.lblAgreement);
        signaturePad            = (SignaturePad)layout.findViewById(R.id.signaturePad);
        checkAgree              = (CheckBox) layout.findViewById(R.id.checkAgree);
        gender                  = this.getArguments().getString("gender");
        transaction_id          = this.getArguments().getString("transaction_id");
        client_name             = this.getArguments().getString("client_name");
        SERVER_URL              = utilities.returnIpAddress();

        String message = "I ";
        message+= " "+client_name+", ";
        message+="of legal age, fully understood that the procedure's involves certain risks to my body, " +
                " which includes scratches, pain, soreness, injury, sickness, irittation, or rash, etc., which may be present and/or after the procedure and I fully accept and assume such risk and responsibility for losses, cost, and damages I may occur. I hereby release and discharge LAY BARE WAXING SALON, its stockholders, directors, franchisees, officers and technicians from all liability, claims, damages, losses, arising from the services they have rendered into me." +
                " I acknowledge that I have read this Agreement and fully understand its terms and conditions.";
        lblAgreement.setText(message);

        showToolbar();
        lblName.setText(client_name);
        signaturePad.setOnSignedListener(new SignaturePad.OnSignedListener() {
            @Override
            public void onStartSigning() {

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
                activitySingleton.Instance().resetAllDetails();
                getFragmentManager().popBackStack("FragmentSignWaiverQuestion", FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateWaiver();
            }
        });

        btnClearPad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signaturePad.clear();
            }
        });

    }

    private void validateWaiver() {
        if (checkAgree.isChecked() == false){
            utilities.showDialogMessage("Incomplete Action","Please check the terms and agreement to continue","error");
            return;
        }
        if(isSignatured == false){
            utilities.showDialogMessage("Incomplete Signature","Please sign your waiver to continue","error");
            return;
        }
        else{
            saveWaiver();
        }
    }

    private void saveWaiver() {

        try {

            String token            = utilities.getToken();
            Bitmap signatureBitmap  = signaturePad.getSignatureBitmap();
            String imageSignature   = utilities.getStringImage(signatureBitmap);

            JSONObject objectWaiver = activitySingleton.Instance().getWalkINObjectWaiver();
            objectWaiver.put("signature",imageSignature);
            JSONObject objectSubmit = new JSONObject();
            objectSubmit.put("transaction_id",transaction_id);
            objectSubmit.put("object_waiver",objectWaiver);

            utilities.showProgressDialog("Saving Appointment....");

            String booking_url = SERVER_URL+"/api/kiosk/addWaiver?token="+token;
            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (Request.Method.POST, booking_url, objectSubmit, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                String result   = response.getString("result");
                                if(result.equals("success")){
                                    utilities.hideProgressDialog();
                                    showCustomPrompt("Waiver Signed!","Waiver is successfully saved! Please wait to be called. ",0);
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
                            10000,
                            3,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                    )
            );
            MySingleton.getInstance(getActivity()).addToRequestQueue(jsObjRequest);

        } catch (JSONException e) {
            e.printStackTrace();
        }



    }

    private void showCustomPrompt(String title, String message, final int action) {

        String branch_id    = utilities.getHomeBranchID();
        Socket mSocket      = activitySingleton.Instance().getSocketApplication();
        mSocket.emit("refreshAppointments",Integer.parseInt(branch_id));
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
                    getFragmentManager().popBackStack("FragmentIndex", android.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
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

    private void showToolbar() {
        ToolbarCaptionClass toolbarCaptionClass = new ToolbarCaptionClass();
        ArrayList<String> arrayList = toolbarCaptionClass.returnCaptions(18);
        lblModuleTitle.setText(String.valueOf(arrayList.get(0)));
        lblModuleCaption.setText(String.valueOf(arrayList.get(1)));
    }


}
