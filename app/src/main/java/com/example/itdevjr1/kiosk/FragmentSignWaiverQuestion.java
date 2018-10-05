package com.example.itdevjr1.kiosk;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.itdevjr1.kiosk.Recycler.RecyclerWaiverForWalkIn;
import com.example.itdevjr1.kiosk.Recycler.RecyclerWaiverForm;
import com.williamww.silkysignature.views.SignaturePad;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by paolohilario on 2/27/18.
 */

public class FragmentSignWaiverQuestion extends Fragment {

    View layout;
    Button btnPrev,btnNext;
    RelativeLayout relativeToolbar;
    TextView lblModuleTitle,lblModuleCaption,lblNoResult;
    DataHandler handler;
    RecyclerView recyclerClientWaiver;
    RecyclerView.LayoutManager recyclerLayoutManager;
    RecyclerView.Adapter recyclerAdapter;
    JSONArray arrayWaiverAnswer;
    JSONArray arrayWaiver;
    JSONArray arrayDisallowedService;
    JSONArray arrayItems;
    String gender               = "";
    String transaction_id       = "";
    String client_name          = "";
    JSONObject objectClientWaiver = new JSONObject();
    ActivitySingleton activitySingleton;
    Utilities utilities;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_sign_waiver_question,container,false);
        initElements();
        return layout;
    }

    private void initElements() {
        handler                 = new DataHandler(getActivity());
        activitySingleton       = new ActivitySingleton();
        utilities               = activitySingleton.Instance().getUtilityClass();
        lblModuleTitle          = (TextView)getActivity().findViewById(R.id.lblModuleTitle);
        lblModuleCaption        = (TextView)getActivity().findViewById(R.id.lblModuleCaption);
        relativeToolbar         = (RelativeLayout)getActivity().findViewById(R.id.relativeToolbar);
        btnPrev                 = (Button) layout.findViewById(R.id.btnPrev);
        btnNext                 = (Button) layout.findViewById(R.id.btnNext);
        recyclerClientWaiver    = (RecyclerView) layout.findViewById(R.id.recyclerClientWaiver);
        lblNoResult             = (TextView)layout.findViewById(R.id.lblNoResult);
        gender                  = this.getArguments().getString("gender");
        transaction_id          = this.getArguments().getString("transaction_id");
        client_name             = this.getArguments().getString("client_name");
        try {
            arrayItems          = new JSONArray(this.getArguments().getString("items"));
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        showToolbar();

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                objectClientWaiver = new JSONObject();
                getFragmentManager().popBackStack("FragmentIndexStart", FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateWaiver();
            }
        });


        handler.open();
        Cursor queryWaiver      = handler.returnWaiver();
        if(queryWaiver.getCount() > 0){
            lblNoResult.setVisibility(View.GONE);
            recyclerClientWaiver.setVisibility(View.VISIBLE);
            queryWaiver.moveToFirst();
            try {
                arrayWaiver             = new JSONArray(queryWaiver.getString(0));
                arrayWaiverAnswer       = new JSONArray();
                iterateWaiver();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else{
            lblNoResult.setVisibility(View.VISIBLE);
            recyclerClientWaiver.setVisibility(View.GONE);
        }
        handler.close();

    }

    private void validateWaiver() {
        boolean ifMonthlyCycle = activitySingleton.Instance().getClientCycleStatus();
        boolean ifConflict = false;

        if(ifMonthlyCycle == true){

            for(int x = 0; x <arrayItems.length(); x++){
                try {
                    int clientItemID = arrayItems.getInt(x);
                    for (int y = 0; y < arrayDisallowedService.length(); y++){
                        int disallowedItemID = arrayDisallowedService.getInt(y);
                        Log.e("item",String.valueOf(clientItemID)+ " - "+disallowedItemID);
                        if(clientItemID == disallowedItemID){
                            utilities.showDialogMessage("Items not allowed to book.","Sorry, you cannot continue to book this appointment because you cannot have service it while you have your monthly period.","error");
                            ifConflict = true;
                            break;
                        }
                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
                if (x == arrayItems.length() - 1 && ifConflict == false){
                    nextSignature();
                }
            }
        }
        else{
            nextSignature();
        }
    }

    void nextSignature(){

        Bundle bundle = new Bundle();
        bundle.putString("gender", gender);
        bundle.putString("transaction_id", String.valueOf(transaction_id));
        bundle.putString("client_name", client_name);
        FragmentSignWaiver fragmentActivity  = new FragmentSignWaiver();
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentActivity.setArguments(bundle);
        fragmentTransaction.setCustomAnimations(R.anim.enter, R.anim.exit,R.anim.left_to_right,R.anim.right_to_left);
        fragmentTransaction.replace(R.id.frameLayout, fragmentActivity,"FragmentSignWaiver");
        fragmentTransaction.addToBackStack("FragmentSignWaiverQuestion");
        fragmentTransaction.commit();
    }

    private void iterateWaiver() {

        try {
            for (int x = 0; x<arrayWaiver.length(); x++){
                try {
                    JSONArray  arrayOption;
                    JSONObject jObj                     = arrayWaiver.getJSONObject(x);
                    String question                     = jObj.getString("question");
                    String target_gender                = jObj.getString("target_gender");
                    String question_type                = jObj.getString("question_type");
                    JSONObject jObjectArrayQuestionData = jObj.getJSONObject("question_data");
                    String default_selected             = jObjectArrayQuestionData.getString("default_selected");
                    String placeholder                  = jObjectArrayQuestionData.getString("placeholder");
                    String waiver_message               = "";
                    String waiver_option                = "";
                    JSONArray disallowed_services       = new JSONArray();

                    if(target_gender.equals("female") && gender.equals("male")){
                        continue;
                    }
                    else{

                        JSONObject objQuestion      = new JSONObject();
                        JSONObject objQuestionData  = new JSONObject();
                        boolean isSelected;
                        if(default_selected.equals("YES")){
                            isSelected = true;
                        }
                        else{
                            isSelected = false;
                        }
                        if (jObjectArrayQuestionData.has("message")) {
                            waiver_message = jObjectArrayQuestionData.getString("message");
                            objQuestionData.put("message",waiver_message);
                            objQuestionData.put("answer","");
                        }
                        if (jObjectArrayQuestionData.has("options")) {
                            waiver_option = jObjectArrayQuestionData.getString("options");
                            arrayOption     = new JSONArray(waiver_option);
                            for (int z = 0; z < arrayOption.length(); z++){
                                JSONObject objOption = arrayOption.getJSONObject(z);
                                objOption.put("answer","");
                            }
                            objQuestionData.put("options",arrayOption);
                            objQuestionData.put("selected_option",0);
                        }
                        if (jObjectArrayQuestionData.has("disallowed_services")) {
                            disallowed_services = jObjectArrayQuestionData.getJSONArray("disallowed_services");
                            objQuestionData.put("disallowed",disallowed_services);
                            objQuestionData.put("answer","");
                            arrayDisallowedService = disallowed_services;
                        }
                        else{
                            objQuestionData.put("answer","");
                        }
                        objQuestion.put("question",question);
                        objQuestion.put("selected",isSelected);
                        objQuestion.put("data",objQuestionData);
                        objQuestion.put("question_type",question_type);
                        objQuestion.put("placeholder",placeholder);
                        arrayWaiverAnswer.put(objQuestion);
                    }

                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            objectClientWaiver.put("signature","");
            objectClientWaiver.put("questions",arrayWaiverAnswer);
            objectClientWaiver.put("strokes",0);
            activitySingleton.Instance().setWalkINObjectWaiver(objectClientWaiver);
            startDisplayingWaiver();

        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void startDisplayingWaiver() {


        recyclerAdapter       = new RecyclerWaiverForWalkIn(getActivity(),arrayItems);
        recyclerLayoutManager = new LinearLayoutManager(getActivity());
        recyclerClientWaiver.setHasFixedSize(true);
        recyclerClientWaiver.setAdapter(recyclerAdapter);
        recyclerClientWaiver.setLayoutManager(recyclerLayoutManager);
        recyclerClientWaiver.setItemAnimator(new DefaultItemAnimator());

    }


    private void showToolbar() {
        ToolbarCaptionClass toolbarCaptionClass = new ToolbarCaptionClass();
        ArrayList<String> arrayList = toolbarCaptionClass.returnCaptions(17);
        lblModuleTitle.setText(String.valueOf(arrayList.get(0)));
        lblModuleCaption.setText(String.valueOf(arrayList.get(1)));
    }


}
