package com.example.itdevjr1.kiosk;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.itdevjr1.kiosk.Recycler.RecyclerSearchClientWaiver;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by paolohilario on 2/22/18.
 */

public class FragmentSearchClientWaiver extends Fragment {
    View layout;
    RecyclerView recyclerClientQueue;
    RecyclerView.LayoutManager recyclerLayoutManager;
    RecyclerView.Adapter recyclerAdapter;

    RelativeLayout relativeToolbar;
    TextView lblModuleTitle,lblModuleCaption,lblNoResult;
    Utilities utilities;
    String SERVER_URL = "";
    DataHandler handler;

    ImageButton imgBtnSearch;
    Button btnPrev;
    EditText txtSearch;

    ArrayList<String>arrayErrorResponse;
    ActivitySingleton activitySingleton;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_search_client_sign_waiver,container,false);
        initElements();
        return layout;

    }

    private void initElements(){
        activitySingleton   = new ActivitySingleton();
        utilities           = activitySingleton.Instance().getUtilityClass();
        SERVER_URL              = utilities.returnIpAddress();
        recyclerClientQueue     = (RecyclerView)layout.findViewById(R.id.recyclerClientQueue);
        lblModuleTitle          = (TextView)getActivity().findViewById(R.id.lblModuleTitle);
        lblModuleCaption        = (TextView)getActivity().findViewById(R.id.lblModuleCaption);
        relativeToolbar         = (RelativeLayout)getActivity().findViewById(R.id.relativeToolbar);
        txtSearch               = (EditText)layout.findViewById(R.id.txtSearch);
        imgBtnSearch            = (ImageButton)layout.findViewById(R.id.imgBtnSearch);
        lblNoResult             = (TextView) layout.findViewById(R.id.lblNoResult);
        btnPrev                 = (Button) layout.findViewById(R.id.btnPrev);
        imgBtnSearch.setEnabled(false);
        imgBtnSearch.setAlpha((float) 0.5);

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activitySingleton.Instance().resetAllDetails();
                getFragmentManager().popBackStack("FragmentIndexStart", FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });

        imgBtnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchClient();
            }
        });

        txtSearch.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {

            }
            public void beforeTextChanged(CharSequence string, int start, int count, int after) {

            }
            public void onTextChanged(CharSequence string, int start, int before, int count) {
                String txtName = string.toString().trim();
                if (txtName.length() <= 0){
                    imgBtnSearch.setEnabled(false);
                    imgBtnSearch.setAlpha((float) 0.5);
                }
                else{
                    imgBtnSearch.setEnabled(true);
                    imgBtnSearch.setAlpha((float) 1);
                }
            }
        });
        showToolbar();



    }

    private void searchClient() {

        hideKeyBoard(layout);
        final String client_name    = txtSearch.getText().toString();
        final String branch_id      = utilities.getHomeBranchID();
        if(client_name.trim().isEmpty()){
            utilities.showDialogMessage("Incomplete Details","Please enter your full name","info");
        }
        else if(!client_name.contains(" ")){
            utilities.showDialogMessage("Incomplete Details","Please enter your full name","info");
        }
        else{

            lblNoResult.setText("Loading result(s)....");
            utilities.showProgressDialog("Searching client....");
            String log_url =  SERVER_URL+"/api/kiosk/searchClient";
            StringRequest postRequest = new StringRequest(Request.Method.POST, log_url,new Response.Listener<String>() {
                @Override
                public void onResponse(String response)  {
                    try {
//                        final Handler handler = new Handler();
//                        handler.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//
//                            }
//                        }, 1000);
                        utilities.hideProgressDialog();
                        Log.e("RESPONSE",response.toString());
                        JSONArray arrayDevice   = new JSONArray(response);
                        if(arrayDevice.length() > 0){
                            recyclerClientQueue.setVisibility(View.VISIBLE);
                            lblNoResult.setVisibility(View.GONE);
                            showData(arrayDevice);
                        }
                        else{
                            lblNoResult.setText("No Client(s) found!");
                            recyclerClientQueue.setVisibility(View.GONE);
                            lblNoResult.setVisibility(View.VISIBLE);
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            },
                    new Response.ErrorListener(){
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            lblNoResult.setText("No Client(s) found!");
                            utilities.hideProgressDialog();
                            arrayErrorResponse = utilities.errorHandling(error);
                            recyclerClientQueue.setVisibility(View.GONE);
                            lblNoResult.setVisibility(View.VISIBLE);
                            utilities.showDialogMessage("Error",arrayErrorResponse.get(1).toString(),"error");
                        }
                    })
            {
                @Override
                public Map<String, String> getParams() throws AuthFailureError {
                    Map<String,String> params = new HashMap<>();
                    params.put("client_name", client_name);
                    params.put("branch_id", branch_id);
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



    private void showData(JSONArray arrayQueuing){

        recyclerLayoutManager   = new LinearLayoutManager(getActivity());
        recyclerAdapter         = new RecyclerSearchClientWaiver(getActivity(),arrayQueuing);
        recyclerClientQueue.setAdapter(recyclerAdapter);
        recyclerClientQueue.setNestedScrollingEnabled(false);
        recyclerClientQueue.setLayoutManager(recyclerLayoutManager);

    }


    private void showToolbar() {
        ToolbarCaptionClass toolbarCaptionClass = new ToolbarCaptionClass();
        ArrayList<String> arrayList = toolbarCaptionClass.returnCaptions(14);
        lblModuleTitle.setText(String.valueOf(arrayList.get(0)));
        lblModuleCaption.setText(String.valueOf(arrayList.get(1)));
    }

    private void hideKeyBoard(View view) {
        InputMethodManager in = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(view.getWindowToken(),0);
    }


}
