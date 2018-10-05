package com.example.itdevjr1.kiosk;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.itdevjr1.kiosk.Recycler.RecyclerClient;

import org.json.JSONArray;

import java.util.ArrayList;

/**
 * Created by paolohilario on 2/5/18.
 */


public class FragmentIndexClientResult extends Fragment {
    View layout;
    RecyclerView recyclerSearchClient;
    Button btnPrev;
    RecyclerView.LayoutManager recyclerButton_layoutManager;
    RecyclerView.Adapter recyclerButton_adapter;
    JSONArray arrayResult;
    private ActivitySingleton activitySingleton;
    TextView lblSearchCaption;
    RelativeLayout relativeToolbar;
    TextView lblModuleTitle,lblModuleCaption;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_index_client_result,container,false);
        initElements();
        return  layout;
    }

    private void initElements(){
        activitySingleton   = new ActivitySingleton();

        arrayResult             = activitySingleton.Instance().getClientsArray();
        lblSearchCaption        = (TextView) layout.findViewById(R.id.lblSearchCaption);
        btnPrev                 = (Button)layout.findViewById(R.id.btnPrev);
        recyclerSearchClient    = (RecyclerView)layout.findViewById(R.id.recyclerSearchClient);
        lblModuleTitle          = (TextView)getActivity().findViewById(R.id.lblModuleTitle);
        lblModuleCaption        = (TextView)getActivity().findViewById(R.id.lblModuleCaption);
        relativeToolbar         = (RelativeLayout)getActivity().findViewById(R.id.relativeToolbar);
        showToolbar();

        recyclerSearchClient.setVisibility(View.VISIBLE);
        if(arrayResult.length() > 0){
           if(arrayResult.length() > 1){
               lblSearchCaption.setText("There are ("+arrayResult.length()+") results found! ");
           }
           else{
               lblSearchCaption.setText("There are ("+arrayResult.length()+") result found! ");
           }

            lblSearchCaption.setGravity(Gravity.LEFT);
            lblSearchCaption.setTextSize(20);

            recyclerSearchClient.setNestedScrollingEnabled(false);
            recyclerButton_layoutManager    = new GridLayoutManager(getActivity(),2);
            recyclerButton_adapter          = new RecyclerClient(getActivity(),arrayResult);
            recyclerSearchClient.setLayoutManager(recyclerButton_layoutManager);
            recyclerSearchClient.setAdapter(recyclerButton_adapter);

        }
        else{
            lblSearchCaption.setGravity(Gravity.CENTER);
            lblSearchCaption.setTextSize(35);
        }
        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().popBackStack("FragmentIndexSearchClient", FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });
    }

    private void showToolbar() {
        ToolbarCaptionClass toolbarCaptionClass = new ToolbarCaptionClass();
        ArrayList<String> arrayList = toolbarCaptionClass.returnCaptions(7);
        lblModuleTitle.setText(String.valueOf(arrayList.get(0)));
        lblModuleCaption.setText(String.valueOf(arrayList.get(1)));
    }

}
