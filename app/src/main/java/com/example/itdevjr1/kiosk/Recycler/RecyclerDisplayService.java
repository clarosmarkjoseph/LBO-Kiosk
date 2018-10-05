package com.example.itdevjr1.kiosk.Recycler;
import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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

//import com.squareup.picasso.Picasso;


/**
 * Created by Mark on 10/11/2017.
 */


public class RecyclerDisplayService extends RecyclerView.Adapter<RecyclerDisplayService.ViewHolder> {
    Context context;
    Utilities utilities;
    String SERVER_URL = "";
    View layout;
    InputMethodManager imm;

    RecyclerView.Adapter recyclerAdapterCart;
    RecyclerView recyclerView;
    ActivitySingleton activitySingleton;
    TextView lblCartLabel;
    EditText txtSearch;
    JSONArray arrayService;
    JSONArray arrayAllServices;

    public RecyclerDisplayService(FragmentActivity activity, JSONArray arrayServices,EditText txtSearch,JSONArray arrayAllServices) {
        this.context                = activity;
        this.activitySingleton      = new ActivitySingleton();
        this.utilities              = activitySingleton.Instance().getUtilityClass();
        this.arrayService           = arrayServices;
        this.recyclerAdapterCart    = activitySingleton.Instance().getRecyclerAppointmentList();
        this.lblCartLabel           = activitySingleton.Instance().getLblCartLabel();
        this.recyclerView           = activitySingleton.Instance().getRecyclerView();
        this.txtSearch              = txtSearch;
        this.arrayAllServices       = arrayAllServices;
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
            final JSONObject jsonObject   = arrayService.getJSONObject(position);
            String service_names          = jsonObject.getString("service_name");
            String image                  = jsonObject.getString("service_picture");
            String image_url              = SERVER_URL+"/images/"+image;

            view.lblName.setText(utilities.getSafeSubstring(utilities.capitalize(service_names)));
            utilities.setUniversalSmallImage(view.imgDetails,image_url);
            view.cardDetails.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showDetails(jsonObject,"services");
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


    public void showDetails(final JSONObject objectDetails, String type){

        final ActivitySingleton activitySingleton = new ActivitySingleton();
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_service_details);
        dialog.setTitle(utilities.capitalize(type));

        dialog.setCancelable(false);
        dialog.show();

        String url                          = utilities.returnIpAddress();
        ImageButton imgBtnClose             = (ImageButton) dialog.findViewById(R.id.imgBtnClose);
        Button btnCancel                    = (Button) dialog.findViewById(R.id.btnCancel);
        Button btnConfirm                   = (Button) dialog.findViewById(R.id.btnConfirm);
        TextView lblTitle                   = (TextView) dialog.findViewById(R.id.lblTitle);
        TextView lblServiceName             = (TextView) dialog.findViewById(R.id.lblServiceName);
        TextView lblServiceDescription      = (TextView) dialog.findViewById(R.id.lblServiceDescription);
        TextView lblDuration                = (TextView) dialog.findViewById(R.id.lblDuration);
        TextView lblPrice                   = (TextView) dialog.findViewById(R.id.lblPrice);
        ImageView imgDetails                = (ImageView) dialog.findViewById(R.id.imgDetails);
        LinearLayout linearForServices      = (LinearLayout) dialog.findViewById(R.id.linearForServices);
        try {

            final int id                = objectDetails.getInt("id");
            final int item_type_id      = objectDetails.getInt("service_type_id");
            String service_name         = objectDetails.optString("service_name","N/A");
            String service_desc         = objectDetails.optString("service_description","N/A");
            String service_img          = objectDetails.optString("service_picture","services/no%20photo.jpg");
            final String service_type   = objectDetails.optString("service_type");
            int duration                = objectDetails.optInt("service_minutes",0);
            double price                = objectDetails.optDouble("service_price",0);

            lblTitle.setText(utilities.capitalize(type));
            linearForServices.setVisibility(View.VISIBLE);
            lblServiceName.setText(utilities.capitalize(service_name));
            lblServiceDescription.setText(utilities.capitalize(service_desc));
            lblDuration.setText(String.valueOf(duration)+" minutes");
            lblPrice.setText("₱ "+utilities.convertToCurrency(String.valueOf(price)));
            setImage(imgDetails,url+"/images/"+service_img);
            btnConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //add to list
                    JSONArray arrayItems    = activitySingleton.Instance().getSelectedArrayItems();
                    int index               = arrayItems.length();
                    String type             = "services";
                    if(index > 0) {
                        if(checkIfPackage(objectDetails) == true){
                            type = "packages";
                            if(checkIfCartHasPackage(arrayItems) == true){
                                utilities.showDialogMessage("One package only","Sorry, you can only choose one(1) package only.","error");
                                return;
                            }
                            if(validatePickedPackageItem(objectDetails,type) == true){
                                utilities.showDialogMessage("Already in your list","Sorry, the selected package item is already in your list","error");
                                return;
                            }
                            else{
                                addToCart(objectDetails,arrayItems,index,type,dialog);
                            }
                        }
                        else{
                            if(ifItemIsAlready(id,arrayItems) == true){
                                utilities.showDialogMessage("Already in your list","Sorry, this item is already in your list. Please choose other service","error");
                                return;
                            }
                            if(checkIfItemIsRestricted(item_type_id) == true){
                                utilities.showDialogMessage("Already in your list","Sorry, the service that you selected cannot combined to the package on your list.","error");
                                return;
                            }
                            else{
                                addToCart(objectDetails,arrayItems,index,type,dialog);
                            }
                        }
                    }
                    else{
                        if(objectDetails.has("package_services")){
                            type = "packages";
                        }
                        else{
                            type = "services";
                        }
                        addToCart(objectDetails,arrayItems,index,type,dialog);
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
        }
        catch (JSONException e) {

            e.printStackTrace();
        }



    }

    private boolean checkIfItemIsRestricted(int selectedItemTypeID) {

        boolean ifConflict = false;
        JSONArray arrayItems = activitySingleton.Instance().getSelectedArrayItems();
        int index          = arrayItems.length();

        for (int x = 0; x < index; x++) {
            try {
                JSONObject objectArray  = arrayItems.getJSONObject(x);
                String item_type        = objectArray.getString("item_type");

                if(item_type.equals("services")){
                    if (objectArray.has("service_type_data")){
                        JSONObject object_type_data = objectArray.optJSONObject("service_type_data");
                        JSONArray arrayRestricted   = object_type_data.optJSONArray("restricted");
                        for (int y = 0; y < arrayRestricted.length(); y++){
                            int restrictedID = arrayRestricted.optInt(y);
                            if(restrictedID == 0){
                                ifConflict = true;
                                return ifConflict;
                            }
                            if(selectedItemTypeID == restrictedID){
                                ifConflict = true;
                                return ifConflict;
                            }
                        }
                    }
                }
                if(item_type.equals("packages")){
                    JSONArray arrayRestricted   = objectArray.optJSONArray("package_services");
                    for (int y = 0; y < arrayRestricted.length(); y++){
                        int restrictedID = Integer.parseInt(arrayRestricted.getString(y));
                        if(restrictedID == 0){
                            ifConflict = true;
                            return ifConflict;
                        }
                        if(selectedItemTypeID == restrictedID){
                            ifConflict = true;
                            return ifConflict;
                        }
                    }
                }
                else{
                    continue;
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return ifConflict;
    }

    public boolean ifItemIsAlready(int selectedID, JSONArray arrayItems){

        boolean ifItemIsAlready = false;
        int index               = arrayItems.length();

        for (int x = 0; x < index; x++) {
            try {
                JSONObject objectArray  = arrayItems.getJSONObject(x);
                int itemID              = objectArray.getInt("id");
                if (itemID == selectedID) {
                    ifItemIsAlready = true;
                    return ifItemIsAlready;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return ifItemIsAlready;
    }

    public boolean checkIfPackage(JSONObject objecDetails){

        boolean ifPackage = false;
        if(objecDetails.has("package_services")){
            ifPackage = true;
        }
        return  ifPackage;
    }

    public boolean checkIfCartHasPackage(JSONArray arrayItems){
        boolean itHas = false;
        int index               = arrayItems.length();
        for (int x = 0; x < index; x++) {
            try {
                JSONObject objectArray  =  arrayItems.getJSONObject(x);
                String itemType         = objectArray.getString("item_type");
                if(itemType.equals("packages")){
                    itHas = true;
                    return itHas;
                }
                else{
                    continue;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return itHas;
    }

    public boolean validatePickedPackageItem(JSONObject objectSelected, String selectedType){

        boolean ifConflict      = false;
        JSONArray arrayItems    = activitySingleton.Instance().getSelectedArrayItems();
        int itemID              = objectSelected.optInt("id");
        JSONArray arraySelectedIdentifier = new JSONArray();
        try {

            if(selectedType.equals("packages")){
                arraySelectedIdentifier = objectSelected.getJSONArray("package_services");
            }

            for (int x = 0; x < arrayItems.length(); x++) {

                JSONObject objectItems          = arrayItems.getJSONObject(x);
                String item_type                = objectItems.optString("item_type");
                int service_type_id             = objectItems.optInt("service_type_id");
                Log.e("item_type",item_type);

                if(item_type.equals("services")){
                    if(arraySelectedIdentifier.length() > 0){
                        for (int y = 0; y < arraySelectedIdentifier.length(); y++) {
                           int selected_id = arraySelectedIdentifier.getInt(y);
                           if(selected_id == service_type_id){
                               ifConflict = true;
                               return ifConflict;
                           }
                        }
                    }
                    else{
                        JSONObject objectData   = objectItems.getJSONObject("service_type_data");
                        arraySelectedIdentifier = objectData.getJSONArray("restricted");
                        for (int y = 0; y < arraySelectedIdentifier.length(); y++) {
                            int selected_id = arraySelectedIdentifier.getInt(y);
                            if(selected_id == service_type_id){
                                ifConflict = true;
                                return ifConflict;
                            }
                        }
                    }
                }
                else if(item_type.equals("packages")){

                    JSONArray arrayPackageServiceID = objectItems.getJSONArray("package_services");
                    for (int y = 0; y < arrayPackageServiceID.length(); y++) {
                        int key = arrayPackageServiceID.getInt(y);
                        if (key == itemID) {
                            ifConflict = true;
                            return ifConflict;
                        }
                    }
                }
            }
            return ifConflict;
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return ifConflict;
    }


    private void addToCart(JSONObject objectDetails,JSONArray arrayItems,int index,String type,Dialog dialog){

        try{

            objectDetails.put("item_type",type);
            objectDetails.put("item_quantity",1);
            arrayItems.put(index,objectDetails);
            activitySingleton.Instance().setArrayItems(arrayItems);
            recyclerAdapterCart.notifyDataSetChanged();
            recyclerView.smoothScrollToPosition(arrayItems.length() - 1);

            if(arrayItems.length() > 0){
                lblCartLabel.setVisibility(View.GONE);
            }
            else{
                lblCartLabel.setVisibility(View.VISIBLE);
            }
            dialog.dismiss();
            txtSearch.setText("");
            txtSearch.clearFocus();
            arrayService = arrayAllServices;
            notifyDataSetChanged();
        }

        catch (JSONException e) {
            e.printStackTrace();
        }

    }



    @Override
    public int getItemCount() {
        return arrayService.length();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        TextView lblName;
        ImageView imgDetails;
        View holderView;
        CardView cardDetails;

        public ViewHolder(final View itemView) {
            super(itemView);
            holderView      = itemView;
            imgDetails      = (ImageView)itemView.findViewById(R.id.imgDetails);
            lblName         = (TextView)itemView.findViewById(R.id.lblName);
            cardDetails     = (CardView)itemView.findViewById(R.id.cardDetails);

        }

    }
}