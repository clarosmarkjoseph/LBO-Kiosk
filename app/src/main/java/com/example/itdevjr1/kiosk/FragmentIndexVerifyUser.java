package com.example.itdevjr1.kiosk;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by paolohilario on 2/5/18.
 */

public class FragmentIndexVerifyUser extends Fragment {

    View layout;
    InputMethodManager imm;
    Utilities utilities;
    String SERVER_URL       = "";
    ActivitySingleton activitySingleton;
    Button btnLogin,btnSearchDetails,btnScanQRCode,btnPrev;
    RelativeLayout relativeToolbar;
    TextView lblModuleTitle,lblModuleCaption;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_index_verify_user,container,false);
        initElements();
        return layout;
    }

    private void initElements() {
        imm                     = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        activitySingleton       = new ActivitySingleton();
        utilities               = activitySingleton.Instance().getUtilityClass();
        SERVER_URL              = utilities.returnIpAddress();
        btnScanQRCode           = (Button)layout.findViewById(R.id.btnScanQRCode);
        btnLogin                = (Button)layout.findViewById(R.id.btnLogin);
        btnSearchDetails        = (Button)layout.findViewById(R.id.btnSearchDetails);
        btnPrev                 = (Button)layout.findViewById(R.id.btnPrev);

        lblModuleTitle          = (TextView)getActivity().findViewById(R.id.lblModuleTitle);
        lblModuleCaption        = (TextView)getActivity().findViewById(R.id.lblModuleCaption);
        relativeToolbar         = (RelativeLayout)getActivity().findViewById(R.id.relativeToolbar);
        showToolbar();

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().popBackStack("FragmentQuestion", FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });

        btnScanQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                utilities.showDialogMessage("Under Maintenance","Sorry, this feature is not yet available.","info");
//                FragmentIndexQRCode fragment = new FragmentIndexQRCode();
//                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
//                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//                fragmentTransaction.setCustomAnimations(R.anim.enter, R.anim.exit,R.anim.left_to_right,R.anim.right_to_left);
//                fragmentTransaction.add(R.id.frameLayout, fragment,"FragmentQRCode");
//                fragmentTransaction.addToBackStack("FragmentVerifyUser");
//                fragmentTransaction.commit();
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentIndexLogin fragment = new FragmentIndexLogin();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.enter, R.anim.exit,R.anim.left_to_right,R.anim.right_to_left);
                fragmentTransaction.replace(R.id.frameLayout, fragment,"FragmentLogin");
                fragmentTransaction.addToBackStack("FragmentVerifyUser");
                fragmentTransaction.commit();
            }
        });

        btnSearchDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentIndexSearchClient fragment = new FragmentIndexSearchClient();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.enter, R.anim.exit,R.anim.left_to_right,R.anim.right_to_left);
                fragmentTransaction.replace(R.id.frameLayout, fragment,"FragmentIndexSearchClient");
                fragmentTransaction.addToBackStack("FragmentVerifyUser");
                fragmentTransaction.commit();
            }
        });
    }

    private void showToolbar() {
        ToolbarCaptionClass toolbarCaptionClass = new ToolbarCaptionClass();
        ArrayList<String> arrayList = toolbarCaptionClass.returnCaptions(3);
        lblModuleTitle.setText(String.valueOf(arrayList.get(0)));
        lblModuleCaption.setText(String.valueOf(arrayList.get(1)));
    }

}
