package com.example.itdevjr1.kiosk;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
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
import java.util.Map;

/**
 * Created by paolohilario on 2/5/18.
 */

public class FragmentIndexRegister extends Fragment {

    View layout;
    Button btnSubmit,btnPrev;
    EditText txtFirstName,txtLastName,txtBday,txtContact,txtEmail;
    InputMethodManager imm;
    RadioGroup radioGroup;
    Utilities utilities;
    String SERVER_URL       = "";
    JSONArray clientArray   = new JSONArray();
    ArrayList<String> arrayErrorResponse;
    ActivitySingleton activitySingleton;
    DateDialogUtilities dateDialogUtilities;
    Calendar dateTime = Calendar.getInstance();
    RelativeLayout relativeToolbar;
    TextView lblModuleTitle,lblModuleCaption;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_index_register,container,false);
        initElements();
        return layout;
    }

    private void initElements() {
        imm                     = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        activitySingleton       = new ActivitySingleton();
        utilities               = activitySingleton.Instance().getUtilityClass();
        SERVER_URL              = utilities.returnIpAddress();
        activitySingleton       = new ActivitySingleton();

        //elements of register
        txtFirstName            = (EditText)layout.findViewById(R.id.txtFirstName);
        txtLastName             = (EditText)layout.findViewById(R.id.txtLastName);
        txtBday                 = (EditText)layout.findViewById(R.id.txtBday);
        txtContact              = (EditText)layout.findViewById(R.id.txtContact);
        txtEmail                = (EditText)layout.findViewById(R.id.txtEmail);
        radioGroup              = (RadioGroup)layout.findViewById(R.id.radioGroup);
        btnSubmit               = (Button)layout.findViewById(R.id.btnSubmit);
        btnPrev                 = (Button)layout.findViewById(R.id.btnPrev);

        lblModuleTitle          = (TextView)getActivity().findViewById(R.id.lblModuleTitle);
        lblModuleCaption        = (TextView)getActivity().findViewById(R.id.lblModuleCaption);
        relativeToolbar         = (RelativeLayout)getActivity().findViewById(R.id.relativeToolbar);
        showToolbar();

        txtBday.setFocusable( true );
        txtBday.setFocusableInTouchMode( true );
        txtBday.setKeyListener(null);

        txtBday.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                imm.hideSoftInputFromWindow(getActivity().getWindow().getCurrentFocus().getWindowToken(), 0);
                dateDialogUtilities = new DateDialogUtilities(getActivity(),txtBday,dateTime);
                dateDialogUtilities.setBirthday();
            }
        });

        txtBday.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View arg0, boolean hasfocus) {
                if (hasfocus) {
                    imm.hideSoftInputFromWindow(getActivity().getWindow().getCurrentFocus().getWindowToken(), 0);
                    dateDialogUtilities = new DateDialogUtilities(getActivity(),txtBday,dateTime);
                    dateDialogUtilities.setBirthday();
                }
            }
        });

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showConfirmationPopup();
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateClient();
            }
        });

        disableCopyPasteEditText();

    }

    private void disableCopyPasteEditText(){
        txtFirstName.setLongClickable(false);
        txtLastName.setLongClickable(false);
        txtBday.setLongClickable(false);
        txtEmail.setLongClickable(false);
        txtContact.setLongClickable(false);
    }



    private void showConfirmationPopup(){

        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_dialog);
        TextView lbldialog_title            = (TextView) dialog.findViewById(R.id.lbldialog_title);
        TextView lbldialog_message          = (TextView) dialog.findViewById(R.id.lbldialog_message);
        Button btndialog_cancel             = (Button) dialog.findViewById(R.id.btndialog_cancel);
        Button btndialog_confirm            = (Button) dialog.findViewById(R.id.btndialog_confirm);
        ImageButton imgBtnClose             = (ImageButton) dialog.findViewById(R.id.imgBtn_dialog_close);
        RelativeLayout relativeToolbar      = (RelativeLayout) dialog.findViewById(R.id.relativeToolbar);

        btndialog_cancel.setVisibility(View.VISIBLE);

        relativeToolbar.setBackgroundColor(getActivity().getResources().getColor(R.color.laybareWarning));
        btndialog_confirm.setBackgroundColor(getActivity().getResources().getColor(R.color.laybareWarning));

        lbldialog_title.setText("Confirmation");
        lbldialog_message.setText("Are you sure you want to exit? This cannot be undone.");

        btndialog_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                activitySingleton.Instance().resetAllDetails();
                getFragmentManager().popBackStack("FragmentQuestion",FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });
        btndialog_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

            }
        });
        imgBtnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
    }


    private void showToolbar() {
        ToolbarCaptionClass toolbarCaptionClass = new ToolbarCaptionClass();
        ArrayList<String> arrayList = toolbarCaptionClass.returnCaptions(2);
        lblModuleTitle.setText(String.valueOf(arrayList.get(0)));
        lblModuleCaption.setText(String.valueOf(arrayList.get(1)));
    }

    //validate reg
    private void validateClient() {

        activitySingleton.Instance().setClientsArray(new JSONArray());
        clientArray     = activitySingleton.getClientsArray();
        String gender           = "";
        final String first_name = txtFirstName.getText().toString();
        final String last_name  = txtLastName.getText().toString();
        final String contact    = txtContact.getText().toString();
        final String email      = txtEmail.getText().toString();
        final String bday       = txtBday.getText().toString();
        int radioClicked        = radioGroup.getCheckedRadioButtonId();

        if(first_name.isEmpty()){
            utilities.showDialogMessage("Incomplete Details","First name is required! ","info");
            return;
        }
        else if(last_name.isEmpty()){
            utilities.showDialogMessage("Incomplete Details","Last name is required! ","info");
            return;
        }
        else if(radioClicked == 0){
            utilities.showDialogMessage("Incomplete Details","Please select your gender.","info");
            return;
        }
        else if(bday.isEmpty()){
            utilities.showDialogMessage("Incomplete Details","Please put your birthdate.","info");
            return;
        }
        else if(utilities.getAge(bday) <= 13 ){
            utilities.showDialogMessage("Under age","We are not allowed to register your account because you are under age. Age limit is 13 yrs old and up","info");
            return;
        }
        else if(contact.isEmpty() && email.isEmpty()){
            utilities.showDialogMessage("Incomplete Details","Please input atleast 1 of the following: *Contact no.\n*Email Address.","info");
            return;
        }
        else if(!email.isEmpty() && utilities.isEmailValid(email) == false){
            utilities.showDialogMessage("Invalid Email address","Email address is not valid.","info");
            return;
        }


        else{
            String finalContact = "";
            utilities.showProgressDialog("Saving record......");
            if(radioClicked == R.id.radioFemale){
                gender = "female";
            }
            else{
                gender = "male";
            }
            if(contact.length() >= 10){
                finalContact = "+63"+contact;
            }

            JSONObject objectUser = new JSONObject();
            try {
                objectUser.put("first_name",first_name);
                objectUser.put("last_name",last_name);
                objectUser.put("contact",finalContact);
                objectUser.put("email",email);
                objectUser.put("birthday",bday);
                objectUser.put("gender",gender);
                activitySingleton.Instance().setClientData(objectUser);
                final String branch_id      = utilities.getHomeBranchID();
                String url                  = SERVER_URL+"/api/kiosk/saveNewUser";
                final String finalGender    = gender;

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
                                    clientArray                 = activitySingleton.getClientsArray();

                                    JSONObject objectSelfData = objectFetchData.getJSONObject("user_self_data");
                                    activitySingleton.Instance().setClientData(objectSelfData);
                                    utilities.hideProgressDialog();
                                    FragmentStep1 fragmentActivity  = new FragmentStep1();
                                    final FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                                    fragmentTransaction.setCustomAnimations(R.anim.enter, R.anim.exit,R.anim.left_to_right,R.anim.right_to_left);
                                    fragmentTransaction.replace(R.id.frameLayout, fragmentActivity,"FragmentStep1");
                                    fragmentTransaction.addToBackStack("FragmentIndexRegister");
                                    fragmentTransaction.commit();

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
                                NetworkResponse networkResponse;
                                networkResponse = error.networkResponse;
                                if(networkResponse != null){
                                    int statusCode  = networkResponse.statusCode;
                                    if(statusCode == 400){
                                        showCustomPrompt("Already registered in the system","Sorry, this information is already in our database. Would you like to search it in the 'Profile Details?'");
                                        return;
                                    }
                                    else{
                                        utilities.showDialogMessage("Error!",arrayErrorResponse.get(1),"error");
                                        return;
                                    }
                                }
                                else{
                                    utilities.showDialogMessage("Error!",arrayErrorResponse.get(1),"error");
                                    return;
                                }
                            }
                        })
                {
                    @Override
                    public Map<String, String> getParams() throws AuthFailureError {
                        Map<String,String> params = new HashMap<>();
                        params.put("first_name", first_name);
                        params.put("last_name", last_name);
                        params.put("bday", bday);
                        params.put("contact", contact);
                        params.put("gender", finalGender);
                        params.put("branch_id", branch_id);
                        params.put("email", email);
                        params.put("ifSearch", String.valueOf(false));
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
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void showCustomPrompt(String title, String message) {

        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_dialog);
        dialog.setTitle("Custom Dialog");
        TextView lbldialog_title    = (TextView) dialog.findViewById(R.id.lbldialog_title);
        TextView lbldialog_message  = (TextView) dialog.findViewById(R.id.lbldialog_message);
        Button btndialog_cancel     = (Button) dialog.findViewById(R.id.btndialog_cancel);
        Button btndialog_confirm    = (Button) dialog.findViewById(R.id.btndialog_confirm);
        ImageButton imgBtnClose     = (ImageButton) dialog.findViewById(R.id.imgBtn_dialog_close);
        lbldialog_title.setText(title);
        lbldialog_message.setText(message);

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        btndialog_cancel.setVisibility(View.VISIBLE);
        btndialog_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activitySingleton.Instance().resetAllDetails();
                dialog.dismiss();
                FragmentIndexVerifyUser fragment = new FragmentIndexVerifyUser();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.enter, R.anim.exit,R.anim.left_to_right,R.anim.right_to_left);
                fragmentTransaction.add(R.id.frameLayout, fragment,"FragmentVerifyUser");
                fragmentTransaction.addToBackStack("FragmentRegister");
                fragmentTransaction.commit();
            }
        });
        imgBtnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activitySingleton.Instance().resetAllDetails();
                dialog.dismiss();
            }
        });
        btndialog_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activitySingleton.Instance().resetAllDetails();
                dialog.dismiss();
            }
        });

        dialog.show();
    }


}
