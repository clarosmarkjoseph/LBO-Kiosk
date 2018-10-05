package com.example.itdevjr1.kiosk.Recycler;
import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.itdevjr1.kiosk.ActivitySingleton;
import com.example.itdevjr1.kiosk.FragmentStep1;
import com.example.itdevjr1.kiosk.MainActivity;
import com.example.itdevjr1.kiosk.R;
import com.example.itdevjr1.kiosk.Utilities;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Mark on 10/11/2017.
 */


public class RecyclerClient extends RecyclerView.Adapter<RecyclerClient.ViewHolder>{

    Context context;
    Utilities utilities;
    String SERVER_URL = "";
    View layout;
    InputMethodManager imm;
    JSONArray arrayClient;

    ActivitySingleton activitySingleton;
    public RecyclerClient(Context activity, JSONArray arrayClients) {
        this.context             = activity;
        this.activitySingleton   = new ActivitySingleton();
        this.utilities           = activitySingleton.Instance().getUtilityClass();
        this.arrayClient         = arrayClients;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        layout                  = LayoutInflater.from(context).inflate(R.layout.recycler_client,parent,false);
        SERVER_URL              = utilities.returnIpAddress();
        ViewHolder vh           = new ViewHolder(layout);
        imm                     = (InputMethodManager)context.getSystemService(Service.INPUT_METHOD_SERVICE);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {


        try {
            JSONObject object   = arrayClient.getJSONObject(position);
            ViewHolder view     = holder;
            String clientName   = object.getString("full_name");
            String bday         = object.getString("client_bdate");
            String email        = object.getString("client_email");
            String contact      = object.getString("client_mobile");
            String image        = "";
            view.lblProfileName.setText(clientName);
            view.lblProfileEmail.setText(email);
            view.lblProfileBday.setText(utilities.getCompleteDateMonth(bday));
            view.lblProfileContact.setText(contact);

            if(object.has("client_profile")){
                image = object.getString("client_profile");
                if(!image.isEmpty() || !image.equals("")) {
                    String img = SERVER_URL+"/images/users/"+image.replace(" ","%20");
                    utilities.setUniversalSmallImage(holder.imgProfile,img);
                }
            }
            if(object.has("user_picture")){
                image = object.getString("user_picture");
                if(!image.isEmpty() || !image.equals("")) {
                    String img = SERVER_URL+"/images/users/"+image.replace(" ","%20");
                    utilities.setUniversalSmallImage(holder.imgProfile,img);
                }
            }
        }

        catch (JSONException e) {
            e.printStackTrace();
        }

    }



    @Override
    public int getItemCount() {
        return arrayClient.length();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView lblProfileName,lblProfileBday,lblProfileEmail,lblProfileContact;
        ImageView imgProfile;


        public ViewHolder(final View itemView) {
            super(itemView);
            lblProfileName         = (TextView)itemView.findViewById(R.id.lblProfileName);
            lblProfileBday         = (TextView)itemView.findViewById(R.id.lblProfileBday);
            lblProfileEmail        = (TextView)itemView.findViewById(R.id.lblProfileEmail);
            lblProfileContact      = (TextView)itemView.findViewById(R.id.lblProfileContact);
            imgProfile             = (ImageView)itemView.findViewById(R.id.imgProfile);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    showConfirmation(position);
                }
            });
        }
    }


    private void showConfirmation(int position) {

        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_client_details);
        dialog.setTitle("Custom Dialog");

        TextView lblClientID        = (TextView) dialog.findViewById(R.id.lblClientID);
        TextView lblClientName      = (TextView) dialog.findViewById(R.id.lblClientName);
        TextView lblClientGender    = (TextView) dialog.findViewById(R.id.lblClientGender);
        TextView lblClientEmail     = (TextView) dialog.findViewById(R.id.lblClientEmail);
        TextView lblClientContact   = (TextView) dialog.findViewById(R.id.lblClientContact);
        TextView lblClientBday      = (TextView) dialog.findViewById(R.id.lblClientBday);
        ImageView imgClient         = (ImageView)dialog.findViewById(R.id.imgClient);

        Button btndialog_cancel     = (Button)dialog.findViewById(R.id.btndialog_cancel);
        Button btndialog_confirm    = (Button)dialog.findViewById(R.id.btndialog_confirm);
        ImageButton imgBtnClose     = (ImageButton)dialog.findViewById(R.id.imgBtn_dialog_close);

        JSONObject objectClient     = null;
        try {

            objectClient            = arrayClient.getJSONObject(position);
            String client_id        = objectClient.optString("clientid","N/A");
            String client_name      = objectClient.optString("full_name");
            String client_gender    = objectClient.optString("client_gender","N/A");
            String client_bday      = objectClient.optString("client_bdate","N/A");
            String client_email     = objectClient.optString("client_email","N/A");
            String client_mobile    = objectClient.optString("client_mobile","N/A");
            String profilePic       = objectClient.optString("client_profile","N/A");
            String system_id        = objectClient.optString("cusid","N/A");

            if(system_id.equals("N/A") || system_id.equals("") || system_id.equals("null") || system_id.equals(null)){
                lblClientID.setVisibility(View.GONE);
            }
            if(!profilePic.equals("") || !profilePic.equals("null") || !profilePic.equals(null) || !profilePic.isEmpty()){
                String img = SERVER_URL+"/images/users/"+profilePic.replace(" ","%20");
                utilities.setUniversalBigImage(imgClient,img);
            }

            lblClientID.setText(system_id);
            lblClientName.setText(client_name);
            lblClientGender.setText(utilities.capitalize(client_gender));
            lblClientEmail.setText(client_email);
            lblClientContact.setText(client_mobile);
            lblClientBday.setText(utilities.getCompleteDateMonth(utilities.removeTimeFromDate(client_bday)));

            final JSONObject finalObjectClient = objectClient;
            btndialog_confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    MainActivity myActivity = (MainActivity)context;
                    activitySingleton.Instance().setClientData(finalObjectClient);
                    FragmentStep1 fragmentActivity  = new FragmentStep1();
                    FragmentTransaction fragmentTransaction = myActivity.getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.setCustomAnimations(R.anim.enter, R.anim.exit,R.anim.left_to_right,R.anim.right_to_left);
                    fragmentTransaction.replace(R.id.frameLayout, fragmentActivity,"FragmentStep1");
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
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

        }
        catch (JSONException e) {
            e.printStackTrace();
        }




        dialog.show();


    }


}