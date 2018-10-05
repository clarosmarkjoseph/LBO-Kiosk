package com.example.itdevjr1.kiosk.Recycler;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.itdevjr1.kiosk.ActivitySingleton;
import com.example.itdevjr1.kiosk.FragmentSignWaiverQuestion;
import com.example.itdevjr1.kiosk.MainActivity;
import com.example.itdevjr1.kiosk.R;
import com.example.itdevjr1.kiosk.Utilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by paolohilario on 2/27/18.
 */

public class RecyclerSearchClientWaiver extends RecyclerView.Adapter<RecyclerSearchClientWaiver.ViewHolder> {
    JSONArray arrayQueuing;
    Activity context;
    View layout;
    Utilities utilities;
    String SERVER_URL;
    ActivitySingleton activitySingleton;

    public RecyclerSearchClientWaiver(Activity ctx, JSONArray arrayQueuing){
        this.context                = ctx;
        this.arrayQueuing           = arrayQueuing;
        this.activitySingleton      = new ActivitySingleton();
        this.utilities              = activitySingleton.Instance().getUtilityClass();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        layout                  = LayoutInflater.from(context).inflate(R.layout.recycler_sign_waiver,parent,false);
        SERVER_URL              = utilities.returnIpAddress();
        ViewHolder vh           = new ViewHolder(layout);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final ViewHolder view   = holder;
        try{

            JSONObject objectQueuing = arrayQueuing.getJSONObject(position);
            final String gender      = objectQueuing.optString("client_gender","N/A");
            String full_name         = objectQueuing.optString("full_name","");
            String birthdate         = objectQueuing.optString("client_bdate","N/A");
            String image             = SERVER_URL+"/images/users/"+objectQueuing.optString("client_profile","no photo.jpg");
            String contact           = objectQueuing.optString("client_mobile","N/A");
            String email             = objectQueuing.optString("client_email","N/A");
            String reference_no      = objectQueuing.optString("reference_no","N/A");
            boolean ifWaiverSigned   = objectQueuing.optBoolean("ifWaiverSigned",false);
            String platform          = objectQueuing.optString("platform","N/A");
            String waiverSigned = "";


            view.lblProfileName.setText(utilities.capitalize(full_name));
            view.lblProfileBday.setText(utilities.getCompleteDateMonth(birthdate));
            view.lblGender.setText(utilities.capitalize(gender));
            view.lblProfileEmail.setText(email);
            view.lblProfileContact.setText(contact);
            view.lblReferenceNo.setText(reference_no);
            view.lblPlatform.setText(String.valueOf(platform));

            if(ifWaiverSigned == true){
                waiverSigned = "Signed";
                view.lblWaiver.setBackground(context.getResources().getDrawable(R.drawable.circle_green));
            }
            else{
                view.lblWaiver.setBackground(context.getResources().getDrawable(R.drawable.circle_yellow));
                waiverSigned = "Unsigned";
            }

            if(contact.equals("null")){
                view.lblProfileContact.setText("N/A");
            }


            holder.lblWaiver.setText(waiverSigned);
            utilities.setUniversalSmallImage(holder.imgProfile,image);

        }
        catch (JSONException e){
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return arrayQueuing.length();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView lblGender,lblProfileName,lblProfileBday,lblProfileEmail,lblProfileContact,lblReferenceNo,lblWaiver,lblPlatform;

        ImageView imgProfile;

        public ViewHolder(final View itemView) {
            super(itemView);
            lblGender                  = (TextView)itemView.findViewById(R.id.lblGender);
            lblProfileName             = (TextView)itemView.findViewById(R.id.lblProfileName);
            lblProfileBday             = (TextView)itemView.findViewById(R.id.lblProfileBday);
            lblProfileEmail            = (TextView)itemView.findViewById(R.id.lblProfileEmail);
            lblProfileContact          = (TextView)itemView.findViewById(R.id.lblProfileContact);
            lblReferenceNo             = (TextView)itemView.findViewById(R.id.lblReferenceNo);
            lblWaiver                  = (TextView)itemView.findViewById(R.id.lblWaiver);
            lblPlatform                = (TextView)itemView.findViewById(R.id.lblPlatform);

            imgProfile                 = (ImageView) itemView.findViewById(R.id.imgProfile);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    try {
                        JSONObject objectQueuing    = arrayQueuing.getJSONObject(position);
                        final int transaction_id    = objectQueuing.optInt("transaction_id",0);
                        final String gender         = objectQueuing.optString("client_gender","N/A");
                        final String client_name    = objectQueuing.optString("full_name","N/A");
                        final JSONArray arrayItems  = objectQueuing.getJSONArray("items");
                        boolean ifWaiverSigned      = objectQueuing.optBoolean("ifWaiverSigned",true);
                        if(ifWaiverSigned == true){
                            utilities.showDialogMessage("Already Signed","You already signed your waiver. Thank you for signing and please wait for your turn to call by our representative","success");
                        }
                        else{
                            Bundle bundle = new Bundle();
                            bundle.putString("gender", gender);
                            bundle.putString("transaction_id", String.valueOf(transaction_id));
                            bundle.putString("client_name", client_name);
                            bundle.putString("items", arrayItems.toString());

                            MainActivity myActivity = (MainActivity)context;
                            FragmentSignWaiverQuestion fragmentActivity  = new FragmentSignWaiverQuestion();
                            FragmentTransaction fragmentTransaction = myActivity.getSupportFragmentManager().beginTransaction();
                            fragmentActivity.setArguments(bundle);
                            fragmentTransaction.setCustomAnimations(R.anim.enter, R.anim.exit,R.anim.left_to_right,R.anim.right_to_left);
                            fragmentTransaction.replace(R.id.frameLayout, fragmentActivity,"FragmentSignWaiverQuestion");
                            fragmentTransaction.addToBackStack("FragmentIndexStart");
                            fragmentTransaction.commit();
                        }


                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });

        }
    }


}
