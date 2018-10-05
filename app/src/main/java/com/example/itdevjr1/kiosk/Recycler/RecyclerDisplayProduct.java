package com.example.itdevjr1.kiosk.Recycler;

import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
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
import android.widget.TextView;

import com.example.itdevjr1.kiosk.ActivitySingleton;
import com.example.itdevjr1.kiosk.R;
import com.example.itdevjr1.kiosk.Utilities;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by Mark on 10/11/2017.
 */


public class RecyclerDisplayProduct extends RecyclerView.Adapter<RecyclerDisplayProduct.ViewHolder>{
    Context context;
    Utilities utilities;
    String SERVER_URL = "";
    View layout;
    InputMethodManager imm;
    JSONArray arrayProduct;
    JSONArray arrayAllProducts;
    RecyclerView.Adapter recyclerAdapterCart;
    RecyclerView recyclerView;
    ActivitySingleton activitySingleton;
    EditText txtSearch;


    public RecyclerDisplayProduct(FragmentActivity activity, JSONArray arrayProducts, EditText txtSearch, JSONArray arrayAllProducts) {
        this.context                = activity;
        this.activitySingleton      = new ActivitySingleton();
        this.utilities              = activitySingleton.Instance().getUtilityClass();
        this.arrayProduct           = arrayProducts;
        this.recyclerAdapterCart    = activitySingleton.Instance().getRecyclerAppointmentList();
        this.recyclerView           = activitySingleton.Instance().getRecyclerView();
        this.arrayAllProducts       = arrayAllProducts;
        this.txtSearch              = txtSearch;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        layout                  = LayoutInflater.from(context).inflate(R.layout.recycler_service,parent,false);
        SERVER_URL              = utilities.returnIpAddress();
        ViewHolder vh           = new ViewHolder(layout);
        imm                     = (InputMethodManager)context.getSystemService(Service.INPUT_METHOD_SERVICE);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final ViewHolder view = holder;

        try {
            final JSONObject jsonObject = arrayProduct.getJSONObject(position);
            String product_name         = jsonObject.getString("product_name");
            String image                = jsonObject.getString("product_picture");
            String image_url            = SERVER_URL+"/images/"+image;

            view.lblName.setText(utilities.getSafeSubstring(utilities.capitalize(product_name)));
            utilities.setUniversalSmallImage(view.imgDetails,image_url);
            view.cardDetails.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showDetails(jsonObject);
                }
            });
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void setImage(final ImageView imgView, String imgUrl){

        final String urlImage = imgUrl.replace(" ","%20");
        Picasso.get()
                .load(urlImage)
                .resize(600, 600)
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
                                .resize(600,600)
                                .error(R.drawable.no_image)
                                .into(imgView);
                    }
                });
    }


    public void showDetails(final JSONObject objectSelectedItem){

        final String type                           = "products";
        final ActivitySingleton activitySingleton   = new ActivitySingleton();
        final Dialog dialog                         = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_service_details);
        dialog.setTitle(utilities.capitalize(type));
        String url                          = utilities.returnIpAddress();
        ImageButton imgBtnClose             = (ImageButton) dialog.findViewById(R.id.imgBtnClose);
        Button btnCancel                    = (Button) dialog.findViewById(R.id.btnCancel);
        Button btnConfirm                   = (Button) dialog.findViewById(R.id.btnConfirm);
        TextView lblTitle                   = (TextView) dialog.findViewById(R.id.lblTitle);
        TextView lblServiceName             = (TextView) dialog.findViewById(R.id.lblServiceName);
        TextView lblServiceDescription      = (TextView) dialog.findViewById(R.id.lblServiceDescription);
        TextView lblProductVariant          = (TextView) dialog.findViewById(R.id.lblProductVariant);
        TextView lblProductSize             = (TextView) dialog.findViewById(R.id.lblProductSize);
        TextView lblPrice                   = (TextView) dialog.findViewById(R.id.lblPrice);
        ImageView imgDetails                = (ImageView) dialog.findViewById(R.id.imgDetails);
        LinearLayout linearForProducts      = (LinearLayout) dialog.findViewById(R.id.linearForProducts);


        try {

            final int productID     = objectSelectedItem.getInt("id");
            String product_name     = objectSelectedItem.optString("product_name","N/A");
            String product_desc     = objectSelectedItem.optString("product_description","N/A");
            String product_img      = objectSelectedItem.optString("product_picture","products/no%20photo.jpg");
            String product_variant  = objectSelectedItem.optString("product_variant","N/A");
            String product_size     = objectSelectedItem.optString("product_size","N/A");
            double price            = objectSelectedItem.optDouble("product_price",0);

            lblTitle.setText(utilities.capitalize(type));
            linearForProducts.setVisibility(View.VISIBLE);
            lblServiceName.setText(utilities.capitalize(product_name));
            lblServiceDescription.setText(utilities.capitalize(product_desc));
            lblProductSize.setText(product_size);
            lblProductVariant.setText(product_variant);
            lblPrice.setText(utilities.convertToCurrency(String.valueOf(price)));
            setImage(imgDetails,url+"/images/"+product_img);

            btnConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    JSONArray arrayItems    = activitySingleton.Instance().getSelectedArrayItems();
                    int index               = arrayItems.length();

                    if(index > 0) {
                        if(ifItemIsAlready(productID,arrayItems) == true){
                            utilities.showDialogMessage("Already in your list","Sorry, this item is already in your list. Please choose other service","error");
                            return;
                        }

                        else{
                            arrayItems.put(objectSelectedItem);
                            addToCart(objectSelectedItem,arrayItems,index,type,dialog);
                        }
                    }
                    else{
                        arrayItems.put(objectSelectedItem);
                        addToCart(objectSelectedItem,arrayItems,index,type,dialog);
                    }
                }
            });

            btnCancel.setOnClickListener(new View.OnClickListener() {
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

            dialog.setCancelable(false);
            dialog.show();
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public boolean ifItemIsAlready(int selectedID, JSONArray arrayItems){

        boolean ifItemIsAlready = false;
        int index               = arrayItems.length();

        for (int x = 0; x < index; x++) {
            try {
                JSONObject objectArray  = arrayItems.getJSONObject(x);
                int itemID              = objectArray.getInt("id");
                if(objectArray.has("product_code")){
                    if (itemID == selectedID) {
                        ifItemIsAlready = true;
                        return ifItemIsAlready;
                    }
                }
                else{
                    continue;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return ifItemIsAlready;
    }

    private void addToCart(JSONObject objectDetails,JSONArray arrayItems,int index,String type,Dialog dialog){
        try{
            objectDetails.put("item_type",type);
            objectDetails.put("item_quantity",1);
            arrayItems.put(index,objectDetails);
            activitySingleton.Instance().setArrayItems(arrayItems);
            recyclerAdapterCart.notifyDataSetChanged();
            recyclerView.smoothScrollToPosition(arrayItems.length() - 1);

            dialog.dismiss();
            txtSearch.setText("");
            txtSearch.clearFocus();
            arrayProduct = arrayAllProducts;
            notifyDataSetChanged();
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    public int getItemCount() {
        return arrayProduct.length();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        TextView lblName;
        ImageView imgDetails;
        CardView cardDetails;

        public ViewHolder(final View itemView) {
            super(itemView);

            imgDetails      = (ImageView)itemView.findViewById(R.id.imgDetails);
            lblName         = (TextView)itemView.findViewById(R.id.lblName);
            cardDetails     = (CardView)itemView.findViewById(R.id.cardDetails);



        }


    }
}