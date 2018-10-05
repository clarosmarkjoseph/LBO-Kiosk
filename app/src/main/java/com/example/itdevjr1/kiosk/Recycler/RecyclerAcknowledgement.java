package com.example.itdevjr1.kiosk.Recycler;

import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.itdevjr1.kiosk.ActivitySingleton;
import com.example.itdevjr1.kiosk.FragmentStep1;
import com.example.itdevjr1.kiosk.MainActivity;
import com.example.itdevjr1.kiosk.R;
import com.example.itdevjr1.kiosk.Utilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by paolohilario on 2/22/18.
 */

public class RecyclerAcknowledgement extends   RecyclerView.Adapter<RecyclerAcknowledgement.ViewHolder>{


    Context context;
    Utilities utilities;
    String SERVER_URL = "";
    View layout;
    InputMethodManager imm;
    JSONArray arrayItems;
    ActivitySingleton activitySingleton;


    public RecyclerAcknowledgement(Context activity, JSONArray arrayItems) {
        this.context            = activity;
        this.activitySingleton   = new ActivitySingleton();
        this.utilities           = activitySingleton.Instance().getUtilityClass();
        this.arrayItems         = arrayItems;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        layout                  = LayoutInflater.from(context).inflate(R.layout.recycler_acknowledge_items,parent,false);
        SERVER_URL              = utilities.returnIpAddress();
        ViewHolder vh           = new ViewHolder(layout);
        imm                     = (InputMethodManager)context.getSystemService(Service.INPUT_METHOD_SERVICE);

        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        try {
            JSONObject object   = arrayItems.getJSONObject(position);
            ViewHolder view     = holder;
            String desc         = "";
            String item         = "";
            String item_name    = object.getString("item_name");
            String item_type    = object.getString("item_type");
            double amount       = object.getDouble("amount");
            int quantity        = object.getInt("quantity");
            String status       = object.getString("item_status");
            double total_price  = 0;

            if (item_type.equals("service")){
                String book_start_time  = utilities.removeDateFromDateTime(object.getString("book_end_time"));
                String book_end_time    = utilities.removeDateFromDateTime(object.getString("book_start_time"));
                item                    = item_name;
                String minutes          = object.getString("item_duration");
                desc +=utilities.getStandardTime(book_start_time)+" - "+utilities.getStandardTime(book_end_time)+"\n("+minutes+" minutes)";;
                view.lblDesc.setText(desc);
                view.lblItemName.setText(item);
                view.lblTotalPrice.setText("Php "+utilities.convertToCurrency(String.valueOf(amount)));
            }
            else{
                total_price = quantity * amount;
                JSONObject objectItemData   = object.getJSONObject("item_data");
                String variant              = objectItemData.optString("variant",null);
                String size                 = objectItemData.optString("size",null);
                if(size == null){
                    item +=item_name;
                }
                else{
                    item +=item_name+"\n"+variant+" ("+size+")";
                }
                desc +=amount+" x "+quantity;
                view.lblDesc.setText(desc);
                view.lblItemName.setText(item);
                view.lblTotalPrice.setText("Php " +utilities.convertToCurrency(String.valueOf(total_price)));
            }

            if(status.equals("reserved")){
                view.lblStatus.setBackground(context.getResources().getDrawable(R.drawable.circle_blue));
            }
            if(status.equals("cancelled")){
                view.lblStatus.setBackground(context.getResources().getDrawable(R.drawable.circle_red));
            }
            if(status.equals("expired")){
                view.lblStatus.setBackground(context.getResources().getDrawable(R.drawable.circle_yellow));
            }
            if(status.equals("completed")){
                view.lblStatus.setBackground(context.getResources().getDrawable(R.drawable.circle_green));
            }
            view.lblStatus.setText(utilities.capitalize(status));

        }

        catch (JSONException e) {
            e.printStackTrace();
        }

    }



    @Override
    public int getItemCount() {
        return arrayItems.length();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView lblItemName,lblDesc,lblTotalPrice,lblStatus;


        public ViewHolder(final View itemView) {
            super(itemView);
            lblItemName             = (TextView)itemView.findViewById(R.id.lblItemName);
            lblDesc                 = (TextView)itemView.findViewById(R.id.lblDesc);
            lblTotalPrice           = (TextView)itemView.findViewById(R.id.lblTotalPrice);
            lblStatus               = (TextView)itemView.findViewById(R.id.lblStatus);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                }
            });

        }
    }





}
