package com.example.itdevjr1.kiosk;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by paolohilario on 1/24/18.
 */

public class FragmentIndexStart extends Fragment {

    View layout;
    Utilities utilities;
    CardView cardCheckIn,cardSignWaiver;
    Button btnPrev;
    TextView lblModuleTitle,lblModuleCaption;
    RelativeLayout relativeToolbar;
    ActivitySingleton activitySingleton;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_index_start,container,false);
        initElements();
        return layout;
    }


    private void initElements() {

        activitySingleton   = new ActivitySingleton();
        utilities           = activitySingleton.Instance().getUtilityClass();

        cardCheckIn         = (CardView) layout.findViewById(R.id.cardCheckIn);
        cardSignWaiver      = (CardView) layout.findViewById(R.id.cardSignWaiver);
        btnPrev             = (Button) layout.findViewById(R.id.btnPrev);

        relativeToolbar     = (RelativeLayout)getActivity().findViewById(R.id.relativeToolbar);
        lblModuleTitle      = (TextView)getActivity().findViewById(R.id.lblModuleTitle);
        lblModuleCaption    = (TextView)getActivity().findViewById(R.id.lblModuleCaption);
        showToolbar();

        cardCheckIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadFragment(0);
            }
        });
        cardSignWaiver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadFragment(1);
            }
        });

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().popBackStack("FragmentIndex", FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });


    }


    private void showToolbar() {
        ToolbarCaptionClass toolbarCaptionClass = new ToolbarCaptionClass();
        ArrayList<String> arrayList = toolbarCaptionClass.returnCaptions(0);
        relativeToolbar.setVisibility(View.VISIBLE);
        lblModuleTitle.setText(String.valueOf(arrayList.get(0)));
        lblModuleCaption.setText(String.valueOf(arrayList.get(1)));
    }

    private void loadFragment(int pos) {
       if(pos == 0){
           FragmentIndexQuestion fragment  = new FragmentIndexQuestion();
           FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
           fragmentTransaction.setCustomAnimations(R.anim.enter, R.anim.exit,R.anim.left_to_right,R.anim.right_to_left);
           fragmentTransaction.replace(R.id.frameLayout, fragment,"FragmentQuestion");
           fragmentTransaction.addToBackStack("FragmentIndexStart");
           fragmentTransaction.commit();
       }
       else{
           FragmentSearchClientWaiver fragment  = new FragmentSearchClientWaiver();
           FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
           fragmentTransaction.setCustomAnimations(R.anim.enter, R.anim.exit,R.anim.left_to_right,R.anim.right_to_left);
           fragmentTransaction.replace(R.id.frameLayout, fragment,"FragmentSearchClientWaiver");
           fragmentTransaction.addToBackStack("FragmentIndexStart");
           fragmentTransaction.commit();
       }
    }








}
