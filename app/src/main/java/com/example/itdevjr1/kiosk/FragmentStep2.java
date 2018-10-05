package com.example.itdevjr1.kiosk;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.itdevjr1.kiosk.Recycler.RecyclerWaiverForm;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by paolohilario on 1/25/18.
 */

public class FragmentStep2 extends Fragment {
    View layout;
    Utilities utilities;
    private ActivitySingleton activitySingleton;
    RecyclerView recyclerWaiver;
    RecyclerView.LayoutManager recyclerWaiver_layoutManager;
    RecyclerView.Adapter recyclerWaiver_adapter;
    JSONArray arrayWaiverAnswer;
    JSONArray arrayWaiver;

    DataHandler handler;
    Button btnPrev,btnNext;
    TextView lblModuleTitle,lblModuleCaption;
    RelativeLayout relativeToolbar;

    String SERVER_URL                   = "";
    JSONObject jsonObjectAnswer         = new JSONObject();
    String gender                       = "";
    JSONArray arrayDisallowedService    = new JSONArray();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_step2,container,false);
        initElements();
        return  layout;
    }

    private void initElements() {
        handler                 = new DataHandler(getActivity());
        activitySingleton       = new ActivitySingleton();
        utilities               = new Utilities(getActivity());
        SERVER_URL              = utilities.returnIpAddress();
        recyclerWaiver          = (RecyclerView)layout.findViewById(R.id.recyclerWaiver);
        btnPrev                 = (Button)layout.findViewById(R.id.btnPrev);
        btnNext                 = (Button)layout.findViewById(R.id.btnNext);
        gender                  = activitySingleton.Instance().getClientGender();
        lblModuleTitle          = (TextView)getActivity().findViewById(R.id.lblModuleTitle);
        lblModuleCaption        = (TextView)getActivity().findViewById(R.id.lblModuleCaption);
        relativeToolbar         = (RelativeLayout)getActivity().findViewById(R.id.relativeToolbar);
        showToolbar();

        handler.open();
        Cursor queryWaiver      = handler.returnWaiver();
        if(queryWaiver.getCount() > 0){
            queryWaiver.moveToFirst();
            try {
                arrayWaiver             = new JSONArray(queryWaiver.getString(0));
                arrayWaiverAnswer       = new JSONArray();

                iterateWaiver();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        handler.close();

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().popBackStack();
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateWaiver();
            }
        });

    }


    private void validateWaiver() {

        JSONArray arraySelectedItems     = activitySingleton.Instance().getSelectedArrayItems();
        boolean ifMonthlyCycle           = activitySingleton.Instance().getClientCycleStatus();
        try {
            if(ifMonthlyCycle == true){
                for(int x = 0; x < arraySelectedItems.length(); x++){
                    JSONObject objectItems      = arraySelectedItems.getJSONObject(x);
                    String item_type            = objectItems.optString("item_type");
                    String item_name            = objectItems.optString("service_name","these");
                    if(item_type.equals("services") || item_type.equals("packages")){
                        int id   = objectItems.optInt("id");
                        Log.e("ID",arrayDisallowedService+" - "+id);
                        for(int y = 0; y < arrayDisallowedService.length(); y++){
                            int disallowed_id = arrayDisallowedService.optInt(y);
                            if(disallowed_id == id){
                                utilities.showDialogMessage("Items not allowed to book.","Sorry, you cannot continue to book this appointment because you cannot have "+item_name+" service during your monthly period.","error");
                                return;
                            }
                        }
                    }
                    if(x >= arraySelectedItems.length() - 1){
                        nextSignature();
                    }
                }
            }
            else{
                nextSignature();
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void nextSignature() {
        utilities.showProgressDialog("Next: Waiver. Please wait...");
        utilities.hideProgressDialog();
        FragmentStep3 fragmentStep3  = new FragmentStep3();
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.enter, R.anim.exit,R.anim.left_to_right,R.anim.right_to_left);
        fragmentTransaction.replace(R.id.frameLayout, fragmentStep3,"FragmentStep3");
        fragmentTransaction.addToBackStack("FragmentStep2");
        fragmentTransaction.commit();
    }


    private void iterateWaiver() {

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
                        arrayDisallowedService = jObjectArrayQuestionData.getJSONArray("disallowed_services");
                        objQuestionData.put("disallowed",arrayDisallowedService);
                        objQuestionData.put("answer","");
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
        try {
            jsonObjectAnswer.put("signature","");
            jsonObjectAnswer.put("questions",arrayWaiverAnswer);
            jsonObjectAnswer.put("strokes",0);
            activitySingleton.Instance().setWaivers(jsonObjectAnswer);
            startDisplayingWaiver();
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void startDisplayingWaiver() {
        recyclerWaiver_adapter       = new RecyclerWaiverForm(getActivity());
        recyclerWaiver_layoutManager = new LinearLayoutManager(getActivity());
        recyclerWaiver.setHasFixedSize(true);
        recyclerWaiver.setAdapter(recyclerWaiver_adapter);
        recyclerWaiver.setLayoutManager(recyclerWaiver_layoutManager);
        recyclerWaiver.setItemAnimator(new DefaultItemAnimator());

    }

    private void showToolbar() {
        ToolbarCaptionClass toolbarCaptionClass = new ToolbarCaptionClass();
        ArrayList<String> arrayList = toolbarCaptionClass.returnCaptions(9);
        lblModuleTitle.setText(String.valueOf(arrayList.get(0)));
        lblModuleCaption.setText(String.valueOf(arrayList.get(1)));
    }



}