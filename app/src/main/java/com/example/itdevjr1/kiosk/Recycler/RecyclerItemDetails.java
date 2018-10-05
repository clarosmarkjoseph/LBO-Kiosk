package com.example.itdevjr1.kiosk.Recycler;

import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.itdevjr1.kiosk.ActivitySingleton;
import com.example.itdevjr1.kiosk.R;
import com.example.itdevjr1.kiosk.Utilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Mark on 10/11/2017.
 */


public class RecyclerItemDetails extends RecyclerView.Adapter<RecyclerItemDetails.ViewHolder>{
    Context context;
    Utilities utilities;
    String SERVER_URL = "";
    View layout;
    InputMethodManager imm;
    ActivitySingleton activitySingleton;
    int totalQuantity = 0;
    double totalPrice = 0;
    JSONArray arrayItem;
    TextView lblTotalQty,lblTotalPrice,lblCartLabel;

    public RecyclerItemDetails(Context activity) {
        this.context             = activity;
        this.activitySingleton   = new ActivitySingleton();
        this.utilities           = activitySingleton.Instance().getUtilityClass();
        this.arrayItem           = activitySingleton.Instance().getSelectedArrayItems();
        this.lblCartLabel        = activitySingleton.Instance().getLblCartLabel();
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        layout                  = LayoutInflater.from(context).inflate(R.layout.recycler_display_items,parent,false);
        SERVER_URL              = utilities.returnIpAddress();
        ViewHolder vh           = new ViewHolder(layout);
        imm                     = (InputMethodManager)context.getSystemService(Service.INPUT_METHOD_SERVICE);
        lblTotalQty             = activitySingleton.Instance().getLabelQuantity();
        lblTotalPrice           = activitySingleton.Instance().getLabelPrice();

        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final ViewHolder view   = holder;
        final int pos           = position;

        totalQuantity = Integer.parseInt(lblTotalQty.getText().toString());

        try {
            final JSONObject jsonObject     = arrayItem.getJSONObject(position);
            String type                     = jsonObject.optString("item_type","");
            int initQty                     = jsonObject.optInt("item_quantity",1);

            if(type.equals("services") || type.equals("packages")){
                String[] arraySpinner = new String[] {
                        "x1"
                };

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                        android.R.layout.simple_spinner_item, arraySpinner);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                view.spinnerQuantity.setAdapter(adapter);

                String itemName                 = jsonObject.optString("service_name","");
                String itemDesc                 = jsonObject.optString("service_minutes","");
                double itemPrice                = jsonObject.optDouble("service_price",0);
                view.lblItemName.setText(itemName);
                view.lblDesc.setText(itemDesc+" minutes");

                view.lblTotal.setText("₱ "+utilities.convertToCurrency(String.valueOf(itemPrice)));
                view.lblSubTotal.setText("₱ "+utilities.convertToCurrency(String.valueOf(itemPrice)));

                totalQuantity+=1;
                totalPrice+=itemPrice;

            }
            else{

                int spinnerPos = initQty-=1;
                String[] arraySpinner = new String[] {
                        "x1", "x2", "x3", "x4", "x5"
                };

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                        android.R.layout.simple_spinner_item, arraySpinner);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                view.spinnerQuantity.setAdapter(adapter);
                view.spinnerQuantity.setSelection(spinnerPos);

                String itemName                = jsonObject.optString("product_name","");
                String itemDesc                = jsonObject.optString("product_variant","");
                String itemDesc1               = jsonObject.optString("product_size","");
                final double itemPrice         = jsonObject.optDouble("product_price",0);
                view.lblItemName.setText(itemName);
                view.lblDesc.setText(itemDesc+" / "+itemDesc1);

                view.lblTotal.setText("₱ "+utilities.convertToCurrency(String.valueOf(itemPrice)));
                view.lblSubTotal.setText("₱ "+utilities.convertToCurrency(String.valueOf(itemPrice)));

                totalQuantity+=initQty;
                totalPrice+=itemPrice;

                view.spinnerQuantity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                        int qty = pos+=1;
                        if (qty > 5){
                            utilities.showDialogMessage("Too many item(s)","Sorry, you can only book a maximum of 5 items","error");
                        }
                        else{
                            try {
                                double subTotal = 0;
                                subTotal = qty*itemPrice;
                                holder.lblSubTotal.setText("₱ "+utilities.convertToCurrency(String.valueOf(subTotal)));
                                jsonObject.put("item_quantity",qty);
                                revalidateData();
                            }
                            catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });
            }

            lblTotalQty.setText(String.valueOf(totalQuantity));
            lblTotalPrice.setText("₱ "+utilities.convertToCurrency(String.valueOf(totalPrice)));


            view.imgRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showRemoveConfirmationPopup(pos);
                }
            });


            if(position >= arrayItem.length() - 1){
                revalidateData();
            }

        }

        catch (JSONException e) {
            e.printStackTrace();
        }

    }


    private void showRemoveConfirmationPopup(final int pos){

        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_dialog);
        TextView lbldialog_title            = (TextView) dialog.findViewById(R.id.lbldialog_title);
        TextView lbldialog_message          = (TextView) dialog.findViewById(R.id.lbldialog_message);
        Button btndialog_cancel             = (Button) dialog.findViewById(R.id.btndialog_cancel);
        Button btndialog_confirm            = (Button) dialog.findViewById(R.id.btndialog_confirm);
        ImageButton imgBtnClose             = (ImageButton) dialog.findViewById(R.id.imgBtn_dialog_close);
        RelativeLayout relativeToolbar      = (RelativeLayout) dialog.findViewById(R.id.relativeToolbar);

        btndialog_cancel.setVisibility(View.VISIBLE);
        relativeToolbar.setBackgroundColor(context.getResources().getColor(R.color.laybareWarning));
        btndialog_confirm.setBackgroundColor(context.getResources().getColor(R.color.laybareWarning));

        JSONObject objectItem   = arrayItem.optJSONObject(pos);
        String item_type        = objectItem.optString("item_type");
        String item_name        = "";

        if(item_type.equals("packages") || item_type.equals("services")){
            item_name       = objectItem.optString("service_name");
        }
        else{
            item_name       = objectItem.optString("product_name");
        }


        lbldialog_title.setText("Confirmation");
        lbldialog_message.setText("Are you sure you want to remove "+item_name+" on the list?");

        btndialog_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                arrayItem.remove(pos);
                activitySingleton.Instance().setArrayItems(arrayItem);
                notifyItemRemoved(pos);
                notifyItemRangeChanged(pos,arrayItem.length());

                if(arrayItem.length() > 0){
                    lblCartLabel.setVisibility(View.GONE);
                }
                else{
                    lblCartLabel.setVisibility(View.VISIBLE);
                }
                revalidateData();
                dialog.dismiss();
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

    private void revalidateData() {

        totalPrice              = 0;
        totalQuantity           = 0;

        if(arrayItem.length() > 0){
            for(int x = 0; x < arrayItem.length(); x++){
                try {
                    JSONObject objectRevalidate = arrayItem.getJSONObject(x);
                    String type                 = objectRevalidate.optString("item_type","");
                    if(type.equals("services") || type.equals("packages")){
                        double subTotal = objectRevalidate.optDouble("service_price",0);
                        totalQuantity+=1;
                        totalPrice+=subTotal;
                    }
                    else{
                        int qtyItem         = objectRevalidate.getInt("item_quantity");
                        double prPrice      = objectRevalidate.optDouble("product_price",0);
                        double subTotal     = prPrice*qtyItem;
                        totalQuantity+=qtyItem;
                        totalPrice+=subTotal;
                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            lblTotalQty.setText(String.valueOf(totalQuantity));
            lblTotalPrice.setText("₱ "+utilities.convertToCurrency(String.valueOf(totalPrice)));
        }
        else{
            lblTotalQty.setText("0");
            lblTotalPrice.setText("₱ 0.00");
        }

        if(arrayItem.length() > 0){
            lblCartLabel.setVisibility(View.GONE);
        }
        else{
            lblCartLabel.setVisibility(View.VISIBLE);
        }
    }



    @Override
    public int getItemCount() {
        return arrayItem.length();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        TextView lblItemName,lblDesc,lblTotal,lblSubTotal;
        ImageButton imgRemove;
        View holderView;
        Spinner spinnerQuantity;
        public ViewHolder(final View itemView) {
            super(itemView);
            holderView      = itemView;
            imgRemove           = (ImageButton) itemView.findViewById(R.id.imgRemove);
            lblItemName         = (TextView)itemView.findViewById(R.id.lblItemName);
            lblDesc             = (TextView)itemView.findViewById(R.id.lblDesc);
            lblTotal            = (TextView)itemView.findViewById(R.id.lblTotal);
            lblSubTotal         = (TextView)itemView.findViewById(R.id.lblSubTotal);
            spinnerQuantity     = (Spinner)itemView.findViewById(R.id.spinnerQuantity);
        }
    }


}
