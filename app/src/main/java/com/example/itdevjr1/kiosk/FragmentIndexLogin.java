package com.example.itdevjr1.kiosk;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by paolohilario on 2/5/18.
 */

public class FragmentIndexLogin extends Fragment {

    View layout;
    InputMethodManager imm;
    Utilities utilities;
    String SERVER_URL       = "";
    ActivitySingleton activitySingleton;
    EditText txtLoginEmail,txtLoginPassword;
    Button btnPrev,btnLoginClient;
    ArrayList<String>arrayErrorResponse;
    RelativeLayout relativeToolbar;
    TextView lblModuleTitle,lblModuleCaption;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_index_login,container,false);
        initElements();
        return layout;
    }

    private void initElements() {
        imm                 = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        activitySingleton   = new ActivitySingleton();
        utilities           = activitySingleton.Instance().getUtilityClass();
        SERVER_URL          = utilities.returnIpAddress();
        txtLoginEmail       = (EditText)layout.findViewById(R.id.txtLoginEmail);
        txtLoginPassword    = (EditText)layout.findViewById(R.id.txtLoginPassword);
        btnPrev             = (Button) layout.findViewById(R.id.btnPrev);
        btnLoginClient      = (Button) layout.findViewById(R.id.btnLoginClient);

        lblModuleTitle      = (TextView)getActivity().findViewById(R.id.lblModuleTitle);
        lblModuleCaption    = (TextView)getActivity().findViewById(R.id.lblModuleCaption);
        relativeToolbar     = (RelativeLayout)getActivity().findViewById(R.id.relativeToolbar);

        txtLoginEmail.setLongClickable(false);
        txtLoginPassword.setLongClickable(false);

        showToolbar();

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().popBackStack("FragmentVerifyUser", FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });

        btnLoginClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginClient();
            }
        });
    }

    private void loginClient() {

        final String email    = txtLoginEmail.getText().toString();
        final String password = txtLoginPassword.getText().toString();


        if(email.isEmpty()){
            utilities.showDialogMessage("Error!","Please enter your email address","info");
            return;
        }
        if(password.isEmpty()){
            utilities.showDialogMessage("Error!","Please enter your email address","info");
            return;
        }
        if(utilities.isEmailValid(email) == false){
            utilities.showDialogMessage("Error!","Please enter a valid email address","error");
            return;
        }
        else{

            btnLoginClient.setEnabled(false);
            btnLoginClient.setAlpha((float) 0.5);
            utilities.showProgressDialog("Logging in....");
            String url =  SERVER_URL+"/api/kiosk/loginClient";
            StringRequest postRequest = new StringRequest(Request.Method.POST, url,new Response.Listener<String>() {
                @Override
                public void onResponse(String response)  {
                    try {
                        JSONObject objectClient = new JSONObject(response);
                        showConfirmation(objectClient);

                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        btnLoginClient.setEnabled(true);
                        btnLoginClient.setAlpha(1);
                        utilities.hideProgressDialog();
                        arrayErrorResponse = utilities.errorHandling(error);
                        utilities.showDialogMessage("Error!",arrayErrorResponse.get(1),"error");
                    }
                })
            {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("email", email);
                    params.put("password", password);
                    return params;
                }
            };
            postRequest.setRetryPolicy(
                    new DefaultRetryPolicy(
                            6000,
                            3,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            MySingleton.getInstance(getActivity()).addToRequestQueue(postRequest);

        }
    }

    private void showConfirmation(final JSONObject objectClient) {

        utilities.hideProgressDialog();
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_client_details);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        TextView lblClientID        = (TextView) dialog.findViewById(R.id.lblClientID);
        TextView lblClientName      = (TextView) dialog.findViewById(R.id.lblClientName);
        TextView lblClientGender    = (TextView) dialog.findViewById(R.id.lblClientGender);
        TextView lblClientEmail     = (TextView) dialog.findViewById(R.id.lblClientEmail);
        TextView lblClientContact   = (TextView) dialog.findViewById(R.id.lblClientContact);
        TextView lblClientBday      = (TextView) dialog.findViewById(R.id.lblClientBday);


        final TextView lblSeconds         = (TextView) dialog.findViewById(R.id.lblSeconds);
        LinearLayout linearLoading  = (LinearLayout) dialog.findViewById(R.id.linearLoading);
        ImageView imgClient         = (ImageView)dialog.findViewById(R.id.imgClient);

        Button btndialog_cancel     = (Button)dialog.findViewById(R.id.btndialog_cancel);
        Button btndialog_confirm    = (Button)dialog.findViewById(R.id.btndialog_confirm);
        ImageButton imgBtnClose     = (ImageButton)dialog.findViewById(R.id.imgBtn_dialog_close);

        btndialog_confirm.setVisibility(View.GONE);
        btndialog_cancel.setVisibility(View.GONE);
        linearLoading.setVisibility(View.VISIBLE);

        String client_id        = objectClient.optString("clientid","N/A");
        String system_id        = objectClient.optString("cusid","N/A");
        String client_name      = objectClient.optString("full_name");
        String client_gender    = objectClient.optString("client_gender","N/A");
        String client_bday      = objectClient.optString("client_bdate","N/A");
        String client_email     = objectClient.optString("client_email","N/A");
        String client_mobile    = objectClient.optString("client_mobile","N/A");
        String profilePic       = objectClient.optString("client_profile","N/A");

        if(system_id.equals("N/A") || system_id.equals("") || system_id.equals("null") || system_id.equals(null)){
            lblClientID.setVisibility(View.GONE);
        }
        if(!profilePic.equals("") || !profilePic.equals("null") || !profilePic.equals(null) || !profilePic.isEmpty()){
            String img = SERVER_URL+"/images/users/"+profilePic.replace(" ","%20");
            utilities.setUniversalBigImage(imgClient,img);
            setImage(imgClient,img);

        }


        lblClientID.setText(system_id);
        lblClientName.setText(client_name);
        lblClientGender.setText(utilities.capitalize(client_gender));
        lblClientEmail.setText(client_email);
        lblClientContact.setText(client_mobile);
        lblClientBday.setText(utilities.getCompleteDateMonth(client_bday));
        activitySingleton.Instance().setClientData(objectClient);




        final CountDownTimer countDownTimer = new CountDownTimer(4000,1000) {

            public void onTick(long millisUntilFinished) {
                lblSeconds.setText(millisUntilFinished / 1000+ ".....");
            }
            public void onFinish() {

                dialog.dismiss();
                lblSeconds.setText("0....");

                FragmentStep1 fragmentActivity  = new FragmentStep1();
                final FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.enter, R.anim.exit,R.anim.left_to_right,R.anim.right_to_left);
                fragmentTransaction.replace(R.id.frameLayout, fragmentActivity,"FragmentStep1");
                fragmentTransaction.addToBackStack("FragmentIndexLogin");
                fragmentTransaction.commit();

            }
        };
        countDownTimer.start();
        imgBtnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                countDownTimer.cancel();
                btnLoginClient.setEnabled(true);
                btnLoginClient.setAlpha(1);
                dialog.dismiss();
            }
        });
        dialog.show();
    }


    private void showToolbar() {
        ToolbarCaptionClass toolbarCaptionClass = new ToolbarCaptionClass();
        ArrayList<String> arrayList = toolbarCaptionClass.returnCaptions(4);
        lblModuleTitle.setText(String.valueOf(arrayList.get(0)));
        lblModuleCaption.setText(String.valueOf(arrayList.get(1)));
    }


    public void setImage(final ImageView imgView, String imgUrl){

        final String urlImage = imgUrl.replace(" ","%20");
        Picasso.get()
                .load(urlImage)
                .resize(800, 800)
                .noFade()
                .error(R.drawable.no_image)
                .into(imgView, new Callback() {
                    @Override
                    public void onSuccess() {

                    }
                    @Override
                    public void onError(Exception e) {
                        Picasso.get()
                                .load(urlImage)
                                .noFade()
                                .networkPolicy(NetworkPolicy.OFFLINE)
                                .resize(800,800)
                                .error(R.drawable.no_image)
                                .into(imgView);
                    }
                });

    }



}
