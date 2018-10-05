package com.example.itdevjr1.kiosk.Recycler;

import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.itdevjr1.kiosk.ActivitySingleton;
import com.example.itdevjr1.kiosk.R;
import com.example.itdevjr1.kiosk.Utilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paolohilario on 2/28/18.
 */

public class RecyclerWaiverForWalkIn extends RecyclerView.Adapter<RecyclerWaiverForWalkIn.ViewHolder> {


    Context context;
    Utilities utilities;
    String SERVER_URL = "";
    View layout;
    //    String myGender = "";
    InputMethodManager imm;
    ViewHolder viewLast;
    JSONObject objWaiverAnswer;
    JSONObject objWaiverAnswerReplica;
    JSONArray arrayQuestion;
    JSONArray arrayItems;
    ActivitySingleton activitySingleton;

    public RecyclerWaiverForWalkIn(FragmentActivity activity,JSONArray arrayItems) {
        this.context            = activity;
        this.activitySingleton  = new ActivitySingleton();
        this.utilities          = activitySingleton.Instance().getUtilityClass();
        this.objWaiverAnswer    = activitySingleton.Instance().getWalkINObjectWaiver();

        try {
            this.arrayQuestion       = objWaiverAnswer.getJSONArray("questions");
            this.arrayItems          = arrayItems;
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        layout                  =  LayoutInflater.from(context).inflate(R.layout.recycler_waiver,parent,false);
        SERVER_URL              = utilities.returnIpAddress();
        objWaiverAnswerReplica  = objWaiverAnswer;
        ViewHolder vh           = new ViewHolder(layout);
        imm                     = (InputMethodManager)context.getSystemService(Service.INPUT_METHOD_SERVICE);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        final ViewHolder view = holder;
        viewLast              = holder;
        try {
            view.waiver_answer                  = "";
            view.arrayOption                    = new JSONArray();
            view.array_disallowed_services      = new JSONArray();
            view.waiver_signature               = objWaiverAnswer.getString("signature");//1.5
            view.jsonObject                     = arrayQuestion.getJSONObject(position);//1
            view.waiver_question                = view.jsonObject.getString("question");//2
            view.waiver_placeholder             = view.jsonObject.getString("placeholder");//2
            view.waiver_question_type           = view.jsonObject.getString("question_type");//2
            view.isSelected                     = view.jsonObject.getBoolean("selected");//2
            view.jObjectArrayQuestionData       = view.jsonObject.getJSONObject("data");//2

            if (view.jObjectArrayQuestionData.has("message")) {
                view.waiver_message = view.jObjectArrayQuestionData.getString("message");
            }
            if (view.jObjectArrayQuestionData.has("answer")) {
                view.waiver_answer = view.jObjectArrayQuestionData.getString("answer");
            }
            if (view.jObjectArrayQuestionData.has("options")) {
                view.arrayOption     = view.jObjectArrayQuestionData.getJSONArray("options");
            }
            if (view.jObjectArrayQuestionData.has("disallowed_services")) {
                view.array_disallowed_services = view.jObjectArrayQuestionData.getJSONArray("disallowed_services");
            }
            view.switchAnswerAndQuestion.setChecked(view.isSelected);
            view.switchAnswerAndQuestion.setText(view.waiver_question);
            view.switchAnswerAndQuestion.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (position==0){
                        if (isChecked == false){
                            alertQuestion(view,position,"Please indicate the type of wax you used", view.waiver_placeholder);
                        }
                        else{
                            view.linearRemarks.setVisibility(View.GONE);
                            view.lblRemarks.setText("");
                            updateWaiver(true,view,"");
                        }
                    }
                    else if(position == 5){
                        if (isChecked == true){
                            alertRadioButton(view.waiver_message,view.arrayOption,view);
                        }
                        else{
                            view.linearRemarks.setVisibility(View.GONE);
                            view.lblRemarks.setText("");
                            updateWaiver(false,view,"");
                        }
                    }
                    else if(position == 6){

                        if (isChecked == true){
                            alertQuestion(view,position,view.waiver_message, "Remarks");
                        }
                        else{
                            view.linearRemarks.setVisibility(View.GONE);
                            view.lblRemarks.setText("");
                            updateWaiver(false,view,"");
                        }
                    }
                    else {
                        if (isChecked == true){
                            alertQuestion(view,position,view.waiver_message, view.waiver_placeholder);
                        }
                        else{
                            view.linearRemarks.setVisibility(View.GONE);
                            view.lblRemarks.setText("");
                            updateWaiver(false,view,"");
                        }
                    }
                }
            });
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void alertQuestion(final ViewHolder view, final int position, String message, String waiver_placeholder){

        final ViewHolder myView = view;
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_waiver);
        TextView lblWaiverTitle      = (TextView) dialog.findViewById(R.id.lblWaiverTitle);
        TextView lblWaiverQuestion   = (TextView) dialog.findViewById(R.id.lblWaiverQuestion);
        Button btnCancel             = (Button) dialog.findViewById(R.id.btnCancel);
        Button btnConfirm            = (Button) dialog.findViewById(R.id.btnConfirm);
        ImageButton imgBtnClose      = (ImageButton) dialog.findViewById(R.id.imgBtnClose);
        final EditText txtAnswer     = (EditText) dialog.findViewById(R.id.txtAnswer);

        lblWaiverTitle.setText("Waiver Question");
        lblWaiverQuestion.setText( message);
        txtAnswer.setHint(waiver_placeholder);
        txtAnswer.setLongClickable(false);

        final Dialog myDialog = dialog;

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String answer = txtAnswer.getText().toString();
                if(answer.isEmpty()){
                    Toast.makeText(context,"Please provide a remarks of your current status.",Toast.LENGTH_SHORT).show();
                    return;
                }
                else{
                    myView.linearRemarks.setVisibility(View.VISIBLE);
                    myView.waiver_answer = utilities.capitalize(answer);
                    myView.lblRemarks.setText(myView.waiver_answer);
                    imm.hideSoftInputFromWindow(txtAnswer.getWindowToken(), 0);
                    boolean selected;
                    if(position == 0){
                        selected= false;
                    }
                    else{
                        selected = true;
                    }
                    dialog.dismiss();
                    updateWaiver(selected,myView,answer.toUpperCase());
                }
                myDialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(position==0) {
                    myView.switchAnswerAndQuestion.setChecked(true);
                }
                else{
                    myView.switchAnswerAndQuestion.setChecked(false);
                    myView.waiver_answer = "";
                }
                myView.linearRemarks.setVisibility(View.GONE);
                imm.hideSoftInputFromWindow(txtAnswer.getWindowToken(), 0);
                myDialog.dismiss();
            }
        });

        imgBtnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(position==0) {
                    myView.switchAnswerAndQuestion.setChecked(true);
                }
                else{
                    myView.switchAnswerAndQuestion.setChecked(false);
                    myView.waiver_answer = "";
                }
                myView.linearRemarks.setVisibility(View.GONE);
                imm.hideSoftInputFromWindow(txtAnswer.getWindowToken(), 0);
                myDialog.dismiss();
            }
        });

        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
    }


    private void updateWaiver(boolean isSelected, ViewHolder view, String answer) {
        try {
            view.jsonObject.put("selected",isSelected);
            view.jObjectArrayQuestionData.put("answer",answer);
            activitySingleton.Instance().setWaivers(objWaiverAnswer);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private class getListIndex{
        public int index;
    }

    private void alertRadioButton(final String message, JSONArray options, final ViewHolder view) {

        final getListIndex getIndex = new getListIndex();
        final List<String> listSelectionLabel   = new ArrayList<>();
        final List<String> listSelectionMessage = new ArrayList<>();
        try {
            for(int x = 0; x < options.length(); x++){
                JSONObject jsonObject   = options.getJSONObject(x);
                String label            = jsonObject.getString("label");
                String msg              = jsonObject.getString("message");
                listSelectionLabel.add(x, label);
                listSelectionMessage.add(x, msg);
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }



        AlertDialog.Builder builder = new AlertDialog.Builder(context);//ERROR ShowDialog cannot be resolved to a type
        builder.setTitle(message);
        final CharSequence[] items = listSelectionLabel.toArray(new CharSequence[listSelectionLabel.size()]);
        builder.setSingleChoiceItems(items, -1,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        getIndex.index = item;
                    }
                });

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String passMessage = listSelectionMessage.get(getIndex.index);
                if(getIndex.index == 0){
                    utilities.showDialogMessage("Show us your proof",passMessage.toString(),"info");
                    updateWaiver(true,view,"Yes, I have a Inform Consent");
                }
                else{
                    alertQuestion(view,5,passMessage,"Enter your prc number if you are a doctor");
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                view.switchAnswerAndQuestion.setChecked(false);
            }
        });

        AlertDialog alert = builder.create();
        alert.show();

    }


    @Override
    public int getItemCount() {
        return arrayQuestion.length();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        TextView lblRemarks;
        Switch switchAnswerAndQuestion;

        JSONArray arrayOption               = new JSONArray();
        JSONArray array_disallowed_services = new JSONArray();
        JSONObject jsonObject               = new JSONObject();
        JSONObject jObjectArrayQuestionData = new JSONObject();

        boolean isSelected;
        String waiver_placeholder           = "";
        String waiver_signature             = "";
        String waiver_question              = "";
        String waiver_question_type         = "";
        String waiver_message               = "";
        String waiver_answer                = "";

        LinearLayout linearRemarks;
        public ViewHolder(View itemView) {
            super(itemView);
            switchAnswerAndQuestion = (Switch)itemView.findViewById(R.id.switchAnswerAndQuestion);
            lblRemarks              = (TextView)itemView.findViewById(R.id.lblRemarks);
            linearRemarks           = (LinearLayout)itemView.findViewById(R.id.linearRemarks);
        }
    }





}