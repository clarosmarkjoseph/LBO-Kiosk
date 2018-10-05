package com.example.itdevjr1.kiosk;


import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by paolohilario on 2/5/18.
 */

public class FragmentIndexSearchClient extends Fragment {
    View layout;
    InputMethodManager imm;
    Utilities utilities;
    String SERVER_URL       = "";
    ActivitySingleton activitySingleton;
    EditText txtSearchFirstName,txtSearchLastName,txtSearchBday,txtSearchEmail,txtSearchContact;
    Spinner spinner_details;
    Button btnSearchClient,btnPrev,btnFillUp;
    LinearLayout linearContact;
    DateDialogUtilities dateDialogUtilities;
    Calendar dateTime = Calendar.getInstance();
    ArrayList<String> arrayErrorResponse;
    RelativeLayout relativeToolbar;
    TextView lblModuleTitle,lblModuleCaption;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_index_search_client,container,false);
        initElements();
        return layout;
    }

    private void initElements() {
        imm                 = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        activitySingleton   = new ActivitySingleton();
        utilities           = activitySingleton.Instance().getUtilityClass();
        SERVER_URL          = utilities.returnIpAddress();
        //elements in search
        txtSearchFirstName      = (EditText)layout.findViewById(R.id.txtSearchFirstName);
        txtSearchLastName       = (EditText)layout.findViewById(R.id.txtSearchLastName);
        txtSearchBday           = (EditText)layout.findViewById(R.id.txtSearchBday);
        txtSearchEmail          = (EditText)layout.findViewById(R.id.txtSearchEmail);
        txtSearchContact        = (EditText)layout.findViewById(R.id.txtSearchContact);
        spinner_details         = (Spinner)layout.findViewById(R.id.spinner_details);
        btnSearchClient         = (Button)layout.findViewById(R.id.btnSearchClient);
        linearContact           = (LinearLayout)layout.findViewById(R.id.linearContact);
        btnPrev                 = (Button)layout.findViewById(R.id.btnPrev);
        btnFillUp               = (Button)layout.findViewById(R.id.btnFillUp);
        lblModuleTitle          = (TextView)getActivity().findViewById(R.id.lblModuleTitle);
        lblModuleCaption        = (TextView)getActivity().findViewById(R.id.lblModuleCaption);
        relativeToolbar         = (RelativeLayout)getActivity().findViewById(R.id.relativeToolbar);
        showToolbar();

        txtSearchBday.setFocusable( true );
        txtSearchBday.setFocusableInTouchMode( true );
        txtSearchBday.setKeyListener(null);

        txtSearchBday.setLongClickable(false);
        txtSearchEmail.setLongClickable(false);
        txtSearchFirstName.setLongClickable(false);
        txtSearchLastName.setLongClickable(false);
        txtSearchContact.setLongClickable(false);


        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().popBackStack("FragmentVerifyUser", android.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });

        btnSearchClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchClient();
            }
        });

        btnFillUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadRegistration();
            }
        });

        List<String> list = new ArrayList<String>();
        list.add("Please select on how do we verify you?");
        list.add("My Birthdate");
        list.add("My Email address");
        list.add("My Contact No.");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item, list){
            @Override
            public boolean isEnabled(int position) {
                if(position == 0) {
                    return false;
                }
                else {
                    txtSearchBday.setText("");
                    txtSearchContact.setText("");
                    txtSearchEmail.setText("");
                    btnSearchClient.setAlpha((float) 1.0);
                    btnSearchClient.setEnabled(true);
                    return true;
                }
            }
            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position == 0){
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);
                }
                else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };

        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_details.setAdapter(dataAdapter);
        spinner_details.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(i == 1){
                    txtSearchBday.setVisibility(View.VISIBLE);
                    linearContact.setVisibility(View.GONE);
                    txtSearchEmail.setVisibility(View.GONE);
                }
                else if (i == 2){
                    txtSearchBday.setVisibility(View.GONE);
                    linearContact.setVisibility(View.GONE);
                    txtSearchEmail.setVisibility(View.VISIBLE);
                }
                else if (i == 3){
                    txtSearchBday.setVisibility(View.GONE);
                    txtSearchEmail.setVisibility(View.GONE);
                    linearContact.setVisibility(View.VISIBLE);
                }
                else{
                    txtSearchBday.setVisibility(View.GONE);
                    txtSearchEmail.setVisibility(View.GONE);
                    linearContact.setVisibility(View.GONE);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });



        txtSearchBday.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                imm.hideSoftInputFromWindow(getActivity().getWindow().getCurrentFocus().getWindowToken(), 0);
                dateDialogUtilities = new DateDialogUtilities(getActivity(),txtSearchBday,dateTime);
                dateDialogUtilities.setBirthday();
            }
        });

        txtSearchBday.setOnFocusChangeListener(new View.OnFocusChangeListener(){
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b==true){
                    imm.hideSoftInputFromWindow(getActivity().getWindow().getCurrentFocus().getWindowToken(), 0);
                    dateDialogUtilities = new DateDialogUtilities(getActivity(),txtSearchBday,dateTime);
                    dateDialogUtilities.setBirthday();
                }
            }
        });

    }



    //searching new Clients
    private void searchClient() {

        String gender           = "";
        final String first_name = txtSearchFirstName.getText().toString();
        final String last_name  = txtSearchLastName.getText().toString();
        String contact          = txtSearchContact.getText().toString();
        final String email      = txtSearchEmail.getText().toString();
        final String bday       = txtSearchBday.getText().toString();

        if(first_name.isEmpty()){
            utilities.showDialogMessage("Incomplete Details","First name is required! ","info");
            return;
        }
        else if(last_name.isEmpty()){
            utilities.showDialogMessage("Incomplete Details","Last name is required! ","info");
            return;
        }

        else if(contact.isEmpty() && email.isEmpty() && bday.isEmpty()){
            utilities.showDialogMessage("Incomplete Details","Please input atleast 1 of the following: \n*Contact no.\n*Email Address.\n*Birthdate","info");
            return;
        }
        else if(!email.isEmpty() && utilities.isEmailValid(email) == false){
            utilities.showDialogMessage("Invalid Email address","Email address is not valid.","info");
            return;
        }
        else{

            if(contact.length() >= 10 ){
                contact = "+63"+contact;
            }
            utilities.showProgressDialog("Searching Client. Please wait....");
            final String branch_id      = utilities.getHomeBranchID();
            String url                  = SERVER_URL+"/api/kiosk/getClientRecords";
            final String finalGender    = gender;
            final String finalContact  = contact;
            StringRequest jsObjRequest = new StringRequest
                    (Request.Method.POST, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            utilities.hideProgressDialog();
                            JSONObject j = null;
                            try {
                                JSONObject objectFetchData  = new JSONObject(response);
                                JSONArray arrayResponse     = objectFetchData.getJSONArray("user_data_fetch");
                                activitySingleton.Instance().setClientsArray(arrayResponse);
                                if(arrayResponse.length() > 0){
                                   showResults();
                                }
                                else{
                                    utilities.showDialogMessage("No result(s)","No result(s) found. Please try again.","info");
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
                            utilities.showDialogMessage("Error!",arrayErrorResponse.get(1),"error");
                        }
                    })
            {
                @Override
                public Map<String, String> getParams() throws AuthFailureError {
                    Map<String,String> params = new HashMap<>();
                    params.put("first_name", first_name);
                    params.put("last_name", last_name);
                    params.put("bday", bday);
                    params.put("contact", finalContact);
                    params.put("gender", finalGender);
                    params.put("branch_id", branch_id);
                    params.put("email", email);
                    params.put("ifSearch", String.valueOf(true));
                    return params;
                }
            };
            jsObjRequest.setRetryPolicy(
                    new DefaultRetryPolicy(
                            60000,
                            0,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            MySingleton.getInstance(getActivity()).addToRequestQueue(jsObjRequest);
        }
    }

    private void showResults() {
        FragmentIndexClientResult fragment      = new FragmentIndexClientResult();
        FragmentManager fragmentManager         = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.enter, R.anim.exit,R.anim.left_to_right,R.anim.right_to_left);
        fragmentTransaction.add(R.id.frameLayout, fragment,"FragmentClientResult");
        fragmentTransaction.addToBackStack("FragmentIndexSearchClient");
//        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void loadRegistration() {

        btnSearchClient.setAlpha((float) 1.0);
        btnSearchClient.setEnabled(true);
        FragmentIndexRegister fragment      = new FragmentIndexRegister();
        FragmentManager fragmentManager         = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.enter, R.anim.exit,R.anim.left_to_right,R.anim.right_to_left);
        fragmentTransaction.add(R.id.frameLayout, fragment,"FragmentRegister");
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }


    private void showToolbar() {

        ToolbarCaptionClass toolbarCaptionClass = new ToolbarCaptionClass();
        ArrayList<String> arrayList = toolbarCaptionClass.returnCaptions(6);
        lblModuleTitle.setText(String.valueOf(arrayList.get(0)));
        lblModuleCaption.setText(String.valueOf(arrayList.get(1)));

    }


}
