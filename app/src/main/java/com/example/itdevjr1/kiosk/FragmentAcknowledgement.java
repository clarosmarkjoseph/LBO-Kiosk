package com.example.itdevjr1.kiosk;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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

import com.example.itdevjr1.kiosk.Recycler.RecyclerAcknowledgement;
import com.example.itdevjr1.kiosk.Recycler.RecyclerItemDetails;
import com.github.nkzawa.socketio.client.Socket;
import com.williamww.silkysignature.views.SignaturePad;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;

/**
 * Created by paolohilario on 2/22/18.
 */

public class FragmentAcknowledgement extends Fragment {

    View layout;
    Utilities utilities;
    ActivitySingleton activitySingleton;
    Socket mSocket;
    String branch_id;
    int appointment_id = 0;
    TextView lblModuleTitle,lblModuleCaption;
    RelativeLayout relativeToolbar;
    TextView lblCaption,lblName,lblReferenceNo,lblFullname,lblBranch,lblTechnician,lblBookedAt;
    RecyclerView recyclerClientSignature;
    RecyclerView.LayoutManager recyclerLayoutManager;
    RecyclerView.Adapter recyclerAdapter;
    Button btnClearPad,btnCancel,btnFinish;
    SignaturePad signature_pad;
    boolean isSignatured = false;
    JSONObject objectAppointment;
    JSONArray arrayItems;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout                      = inflater.inflate(R.layout.fragment_acknowledgement,container,false);
        String appointmentString    = this.getArguments().getString("appointment");
        try {
            objectAppointment   = new JSONObject(appointmentString);
            appointment_id      = objectAppointment.optInt("id",0);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        initElements();
        return layout;
    }



    private void initElements() {

        activitySingleton   = new ActivitySingleton();
        utilities           = activitySingleton.Instance().getUtilityClass();

        branch_id           = utilities.getHomeBranchID();
        mSocket             = activitySingleton.Instance().getSocketApplication();
        relativeToolbar     = (RelativeLayout)getActivity().findViewById(R.id.relativeToolbar);
        lblModuleTitle      = (TextView)getActivity().findViewById(R.id.lblModuleTitle);
        lblModuleCaption    = (TextView)getActivity().findViewById(R.id.lblModuleCaption);

        signature_pad       = (SignaturePad)layout.findViewById(R.id.signature_pad);
        btnClearPad         = (Button) layout.findViewById(R.id.btnClearPad);
        btnCancel           = (Button) layout.findViewById(R.id.btnCancel);
        btnFinish           = (Button) layout.findViewById(R.id.btnFinish);

        //transaction details
        lblCaption              =  (TextView)layout.findViewById(R.id.lblModuleCaption);
        lblReferenceNo          =  (TextView)layout.findViewById(R.id.lblReferenceNo);
        lblFullname             =  (TextView)layout.findViewById(R.id.lblFullname);
        lblBranch               =  (TextView)layout.findViewById(R.id.lblBranch);
        lblTechnician           =  (TextView)layout.findViewById(R.id.lblTechnician);
        lblBookedAt             =  (TextView)layout.findViewById(R.id.lblBookedAt);
        recyclerClientSignature =  (RecyclerView) layout.findViewById(R.id.recyclerClientSignature);



        //signature pad elements
        lblName             =  (TextView)layout.findViewById(R.id.lblName);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateAction(0);
            }
        });

        btnFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateAction(1);
            }
        });

        btnClearPad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signature_pad.clear();
            }
        });

        signature_pad.setOnSignedListener(new SignaturePad.OnSignedListener() {

            @Override
            public void onStartSigning() {
                actionPad(0);
            }

            @Override
            public void onSigned() {
                //Event triggered when the pad is signed
                isSignatured = true;
                actionPad(1);
            }

            @Override
            public void onClear() {
                //Event triggered when the pad is cleared
                isSignatured = false;
                actionPad(1);

            }
        });

        showToolbar();
        iterateObjects();
    }

    private Runnable runnableTimer = new Runnable() {
        @Override
        public void run() {

        }
    };

//    private CountDownTimer timerBack = new CountDownTimer(3000,1000) {
//        @Override
//        public void onTick(long l) {
//
//        }
//        @Override
//        public void onFinish() {
//            timerBack.cancel();
//            mSocket.emit("cancelSigning",appointment_id);
//            FragmentIndex test = (FragmentIndex) getActivity().getSupportFragmentManager().findFragmentByTag("FragmentIndex");
//            if (test != null && test.isVisible()) {
//                getFragmentManager().popBackStack("FragmentIndex", FragmentManager.POP_BACK_STACK_INCLUSIVE);
//            }
//            else {
//                //Whatever
//            }
////            if (getActivity().getFragmentManager().getBackStackEntryCount() > 1) {
////
////            }
////            else {
//////                getActivity().onBackPressed();
////            }
//        }
//    };


    private void actionPad(int i) {

        if(i == 0){
            mSocket.emit("startSigning",appointment_id);
        }
        else{
            if(isSignatured==true){
                Bitmap bitmap        =  signature_pad.getSignatureBitmap();
                String bitmapString  = utilities.getStringImage(bitmap);
                mSocket.emit("stopSigning",appointment_id,bitmapString);
            }
            else{
                mSocket.emit("stopSigning",appointment_id,null);
            }
        }
    }

    private void iterateObjects() {


        try {
            arrayItems          = objectAppointment.getJSONArray("items");
            String reference_no = objectAppointment.optString("reference_no","");
            String full_name    = objectAppointment.optString("client_name","");
            String branch       = objectAppointment.optString("branch_name","");
            String contact      = objectAppointment.optString("client_contact","");
            String gender       = objectAppointment.optString("client_gender","");
            String technician   = objectAppointment.optString("technician_name","");
            String platform     = objectAppointment.optString("platform","");

            lblReferenceNo.setText(reference_no);
            lblFullname.setText(full_name);
            lblName.setText(full_name);
            lblBranch.setText(branch);
            lblTechnician.setText(technician);
            lblBookedAt.setText(platform);

            getItems(arrayItems);

        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }



    private void getItems(JSONArray arrayItems) {

        recyclerLayoutManager   = new LinearLayoutManager(getActivity());
        recyclerAdapter         = new RecyclerAcknowledgement(getActivity(),arrayItems);
        recyclerClientSignature.setHasFixedSize(true);
        recyclerClientSignature.setAdapter(recyclerAdapter);
        recyclerClientSignature.setNestedScrollingEnabled(false);
        recyclerClientSignature.setLayoutManager(recyclerLayoutManager);

    }

    private void showToolbar() {
        ToolbarCaptionClass toolbarCaptionClass = new ToolbarCaptionClass();
        ArrayList<String> arrayList = toolbarCaptionClass.returnCaptions(15);
        relativeToolbar.setVisibility(View.VISIBLE);
        lblModuleTitle.setText(String.valueOf(arrayList.get(0)));
        lblModuleCaption.setText(String.valueOf(arrayList.get(1)));
    }

    void validateAction(final int sub){

        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_dialog);
        TextView lbldialog_title            = (TextView) dialog.findViewById(R.id.lbldialog_title);
        TextView lbldialog_message          = (TextView) dialog.findViewById(R.id.lbldialog_message);
        Button btndialog_cancel             = (Button) dialog.findViewById(R.id.btndialog_cancel);
        Button btndialog_confirm            = (Button) dialog.findViewById(R.id.btndialog_confirm);
        ImageButton imgBtnClose             = (ImageButton) dialog.findViewById(R.id.imgBtn_dialog_close);
        RelativeLayout relativeToolbar      = (RelativeLayout) dialog.findViewById(R.id.relativeToolbar);

        if(sub == 0){
            btndialog_cancel.setVisibility(View.VISIBLE);
            relativeToolbar.setBackgroundColor(getActivity().getResources().getColor(R.color.themeRed));
            btndialog_confirm.setBackgroundColor(getActivity().getResources().getColor(R.color.laybareGreen));
            lbldialog_title.setText("Cancel Verification");
            lbldialog_message.setText("Are you sure you want to cancel acknowledgement? ");
            btndialog_confirm.setText("Confirm Cancelation");
            btndialog_cancel.setText("Back");
        }
        else if(sub == 1){
            btndialog_cancel.setVisibility(View.VISIBLE);
            relativeToolbar.setBackgroundColor(getActivity().getResources().getColor(R.color.laybareInfo));
            btndialog_confirm.setBackgroundColor(getActivity().getResources().getColor(R.color.laybareInfo));
            lbldialog_title.setText("Confirm Verification");
            lbldialog_message.setText("Do you want to confirm this signature?");
            btndialog_confirm.setText("Confirm");
            btndialog_cancel.setText("Back");
        }
        else{
            btndialog_cancel.setVisibility(View.GONE);
            relativeToolbar.setBackgroundColor(getActivity().getResources().getColor(R.color.laybareGreen));
            btndialog_confirm.setBackgroundColor(getActivity().getResources().getColor(R.color.laybareGreen));
            lbldialog_title.setText("Transaction Complete");
            lbldialog_message.setText("Your transaction marked as 'COMPLETED'. Thank you for choosing Lay Bare. ");
            btndialog_confirm.setText("Confirm");
        }

        final Dialog myDialog = dialog;

        btndialog_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               sendSignature(sub,myDialog);
            }
        });

        btndialog_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myDialog.dismiss();
            }
        });

        imgBtnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myDialog.dismiss();
            }
        });

        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();

    }

    private void sendSignature(int sub, Dialog myDialog){

        if(sub==0){
            myDialog.dismiss();
            mSocket.emit("cancelSigning",appointment_id);
            getFragmentManager().popBackStack("FragmentIndex",FragmentManager.POP_BACK_STACK_INCLUSIVE);
//            timerBack.cancel();
        }
        else if(sub == 1){
            //send image
            Bitmap bitmap       =  signature_pad.getSignatureBitmap();
            if(isSignatured == false){
                utilities.showDialogMessage("Signature is Empty!","Sorry, Signature is empty. Please signed it before proceed.","error");
                return;
            }
            else{
                String bitmapString  = utilities.getStringImage(bitmap);
                mSocket.emit("finishSigning",appointment_id,bitmapString);
            }
            myDialog.dismiss();
            getFragmentManager().popBackStack("FragmentIndex", FragmentManager.POP_BACK_STACK_INCLUSIVE);
//            timerBack.cancel();
        }
        else{
            getFragmentManager().popBackStack("FragmentIndex", android.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
//            timerBack.cancel();
        }
    }



}
