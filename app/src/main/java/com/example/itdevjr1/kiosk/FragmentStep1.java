package com.example.itdevjr1.kiosk;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.itdevjr1.kiosk.Adapter.TabAppointmentAdapter;
import com.example.itdevjr1.kiosk.Recycler.RecyclerItemDetails;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by paolohilario on 1/25/18.
 */

public class FragmentStep1 extends Fragment {

    public View layout;
    ActivitySingleton activitySingleton;
    ViewPager viewPagerDisplay;
    TabLayout tabLayout;
    Button btnPrev,btnNext;
    TabAppointmentAdapter tabAppointmentAdapter;
    Utilities utilities;
    public TextView lblTransactionDetails;
    public RecyclerView recyclerItems;
    public RecyclerView.LayoutManager recyclerLayoutManager;
    public RecyclerView.Adapter recyclerAdapter;
    TextView lblTotalQty,lblTotalPrice;
    TextView lblModuleTitle,lblModuleCaption;
    RelativeLayout relativeToolbar;
    JSONObject objectAppointment = new JSONObject();


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        layout          = inflater.inflate(R.layout.fragment_step1,container,false);
        initElements();
        return  layout;
    }

    private void initElements() {

        activitySingleton       = new ActivitySingleton();
        utilities               = activitySingleton.Instance().getUtilityClass();
        viewPagerDisplay        = (ViewPager)layout.findViewById(R.id.viewPagerDisplay);
        tabLayout               = (TabLayout) layout.findViewById(R.id.tabLayout);
        btnNext                 = (Button)layout.findViewById(R.id.btnNext);
        btnPrev                 = (Button)layout.findViewById(R.id.btnPrev);
        lblTotalQty             = (TextView)layout.findViewById(R.id.lblTotalQty);
        lblTotalPrice           = (TextView)layout.findViewById(R.id.lblTotalPrice);
        lblTransactionDetails   = (TextView)layout.findViewById(R.id.lblTransactionDetails);
        recyclerItems           = (RecyclerView)layout.findViewById(R.id.recyclerItems);

        relativeToolbar     = (RelativeLayout)getActivity().findViewById(R.id.relativeToolbar);
        lblModuleTitle      = (TextView)getActivity().findViewById(R.id.lblModuleTitle);
        lblModuleCaption    = (TextView)getActivity().findViewById(R.id.lblModuleCaption);
        showToolbar();

        tabLayout.addTab(tabLayout.newTab().setText("Regular Services"));
        tabLayout.addTab(tabLayout.newTab().setText("Cool Packages"));
        tabLayout.addTab(tabLayout.newTab().setText("Products"));

        tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.themeWhite));
        tabLayout.setTabGravity(TabLayout.GRAVITY_CENTER);
        tabAppointmentAdapter = new TabAppointmentAdapter(getChildFragmentManager(),tabLayout.getTabCount());
        viewPagerDisplay.setAdapter(tabAppointmentAdapter);
        int limit = (tabAppointmentAdapter.getCount() > 1 ? tabAppointmentAdapter.getCount() - 1 : 1);
        viewPagerDisplay.setOffscreenPageLimit(limit);
        viewPagerDisplay.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        setupListener();
        //label total qty & price
        activitySingleton.Instance().setLabelPrice(lblTotalPrice);
        activitySingleton.Instance().setLabelQuantity(lblTotalQty);
        activitySingleton.Instance().setLblCartLabel(lblTransactionDetails);
        setupAppointmentDetails();

    }

    private void setupAppointmentDetails() {

        JSONObject objectBranch         = new JSONObject();
        JSONObject objectClient         = new JSONObject();

        try {
            JSONObject objectClientRes  = activitySingleton.Instance().getClientData();
            objectClient.put("value",objectClientRes.optString("cusid",""));
            objectClient.put("label",objectClientRes.optString("full_name",""));
            objectClient.put("gender",objectClientRes.optString("client_gender",""));
            objectBranch.put("value",Integer.parseInt(utilities.getHomeBranchID()));
            objectBranch.put("label",utilities.getBSHomeBranch());

            objectAppointment.put("transaction_type","branch_booking");
            objectAppointment.put("branch",objectBranch);
            objectAppointment.put("client",objectClient);
            objectAppointment.put("transaction_date",utilities.getCurrentDateTime());
            objectAppointment.put("platform","KIOSK");
            objectAppointment.put("services",new JSONArray());
            objectAppointment.put("products",new JSONArray());
            objectAppointment.put("waiver_data",new JSONObject());

            recyclerLayoutManager   = new LinearLayoutManager(getActivity());
            recyclerAdapter         = new RecyclerItemDetails(getActivity());

            recyclerItems.setHasFixedSize(true);
            recyclerItems.setAdapter(recyclerAdapter);
            recyclerItems.setNestedScrollingEnabled(false);
            recyclerItems.setLayoutManager(recyclerLayoutManager);
            lblTransactionDetails.setVisibility(View.VISIBLE);
            //adapter of item list (right side)
            activitySingleton.Instance().setRecyclerAppointmentList(recyclerAdapter,recyclerItems);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void validateItems(){

        utilities.showProgressDialog("Next: Waiver. Please wait...");
        activitySingleton.Instance().setObjectAppointment(objectAppointment);
        boolean hasService = false;
        JSONArray arraySelectedItems     = activitySingleton.Instance().getSelectedArrayItems();
        if (arraySelectedItems.length() <= 0) {
            utilities.hideProgressDialog();
            utilities.showDialogMessage("No items added!","Please select atleast 1 service or product","info");
            return;
        }
        else {
            for(int x = 0; x < arraySelectedItems.length(); x++){
                JSONObject objectItem   = arraySelectedItems.optJSONObject(x);
                String item_type        = objectItem.optString("item_type");
                if(item_type.equals("services") || item_type.equals("packages")){
                    hasService = true;
                }

                if(x >= arraySelectedItems.length() - 1){
                    if(hasService == false){
                        utilities.hideProgressDialog();
                        utilities.showDialogMessage("No service in list","Please select atleast 1 service / package","info");
                        return;
                    }
                    else{
                        utilities.hideProgressDialog();
                        FragmentStep2 fragmentStep2  = new FragmentStep2();
                        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.setCustomAnimations(R.anim.enter, R.anim.exit,R.anim.left_to_right,R.anim.right_to_left);
                        fragmentTransaction.replace(R.id.frameLayout, fragmentStep2,"FragmentStep2");
                        fragmentTransaction.addToBackStack("FragmentStep1");
                        fragmentTransaction.commit();
                    }
                }
            }
        }
    }


    private void showToolbar() {
        ToolbarCaptionClass toolbarCaptionClass = new ToolbarCaptionClass();
        ArrayList<String> arrayList = toolbarCaptionClass.returnCaptions(8);
        lblModuleTitle.setText(String.valueOf(arrayList.get(0)));
        lblModuleCaption.setText(String.valueOf(arrayList.get(1)));
    }

    private void setupListener(){
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPagerDisplay.setCurrentItem(tab.getPosition());

            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showConfirmationPopup();
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateItems();
            }
        });
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
        lbldialog_message.setText("Are you sure you want to exit appointment? This cannot be undone.");

        btndialog_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                activitySingleton.Instance().resetAllDetails();
                getFragmentManager().popBackStack();
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








}