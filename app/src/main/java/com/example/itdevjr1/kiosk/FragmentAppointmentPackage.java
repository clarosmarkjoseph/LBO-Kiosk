package com.example.itdevjr1.kiosk;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.itdevjr1.kiosk.Recycler.RecyclerDisplayService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by paolohilario on 8/17/18.
 */

public class FragmentAppointmentPackage extends Fragment {

    View layout;
    DataHandler handler;
    RecyclerView recyclerService;
    RecyclerView.Adapter recyclerAdapter;
    RecyclerView.LayoutManager recyclerLayoutManager;
    ActivitySingleton activitySingleton;
    EditText txtSearchItem;
    JSONArray arrayFiltered = new JSONArray();
    JSONArray arrayServices = new JSONArray();
    ProgressBar progressBar;
    TextView lblCaption;
    boolean isLoaded = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_appointment_service, container, false);
        return layout;
    }

    private void initElements() {

        lblCaption = (TextView) layout.findViewById(R.id.lblCaption);
        progressBar = (ProgressBar) layout.findViewById(R.id.progressBar);
        txtSearchItem = (EditText) layout.findViewById(R.id.txtSearchItem);
        recyclerService = (RecyclerView) layout.findViewById(R.id.recyclerService);
        recyclerLayoutManager = new GridLayoutManager(getActivity(), 5);
        txtSearchItem.setHint("Search Cool Packages");
        txtSearchItem.clearFocus();
        iterateServices();
        txtSearchItem.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterData(String.valueOf(s));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }


    private void iterateServices() {
        handler = new DataHandler(getActivity());
        handler.open();
        Cursor query = handler.returnService();

        if (query.getCount() > 0) {
            query.moveToFirst();
            try {
                JSONArray arrayRes = new JSONArray(query.getString(0));
                arrayServices = new JSONArray();
                String gender = activitySingleton.Instance().getClientGender();
                for (int x = 0; x < arrayRes.length(); x++) {

                    JSONObject jsonObject = arrayRes.getJSONObject(x);
                    String service_gender = jsonObject.getString("service_gender");

                    if (gender.equals(service_gender)) {
                        if (!jsonObject.has("service_type_data")){
                            arrayServices.put(jsonObject);
                        }
                    } else {
                        continue;
                    }
                }
                handler.close();
                arrayFiltered = arrayServices;
                displayAdapter();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            displayAdapter();
        }
    }


    private void filterData(String s) {
        loadProgressBar();

        if (s.length() == 0) {
            arrayFiltered = arrayServices;
            displayAdapter();
        } else {
            arrayFiltered = new JSONArray();
            for (int y = 0; y < arrayServices.length(); y++) {
                try {
                    String name = arrayServices.getJSONObject(y).getString("service_name");
                    String desc = arrayServices.getJSONObject(y).getString("service_description");
                    if (name.toLowerCase().contains(s.toLowerCase()) == true || desc.toLowerCase().contains(s.toLowerCase())) {
                        try {
                            arrayFiltered.put(arrayServices.get(y));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            displayAdapter();
        }
    }


    public void loadProgressBar() {
        recyclerService.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar() {

        progressBar.setVisibility(View.GONE);
        if (arrayFiltered.length() > 0) {
            recyclerService.setVisibility(View.VISIBLE);
            lblCaption.setVisibility(View.GONE);
        } else {
            recyclerService.setVisibility(View.GONE);
            lblCaption.setVisibility(View.VISIBLE);
            lblCaption.setText("No Item(s) to display");
        }
    }

    private void displayAdapter() {
        recyclerAdapter = new RecyclerDisplayService(getActivity(), arrayFiltered,txtSearchItem,arrayServices);
        recyclerService.setAdapter(recyclerAdapter);
        recyclerService.setLayoutManager(recyclerLayoutManager);
        hideProgressBar();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && !isLoaded) {
            isLoaded = true;
            initElements();
        }
    }

}



