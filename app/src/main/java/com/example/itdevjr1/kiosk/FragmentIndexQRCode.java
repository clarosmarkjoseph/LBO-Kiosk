package com.example.itdevjr1.kiosk;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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

public class FragmentIndexQRCode extends Fragment {
    View layout;
    InputMethodManager imm;
    Utilities utilities;
    String SERVER_URL       = "";
    ActivitySingleton activitySingleton;
    Button btnPrev;
    RelativeLayout relativeToolbar;
    TextView lblModuleTitle,lblModuleCaption;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_index_qrcode,container,false);
        initElements();
        return layout;
    }

    private void initElements() {
        activitySingleton       = new ActivitySingleton();
        utilities               = activitySingleton.Instance().getUtilityClass();
        btnPrev                 = (Button)layout.findViewById(R.id.btnPrev);
        lblModuleTitle          = (TextView)getActivity().findViewById(R.id.lblModuleTitle);
        lblModuleCaption        = (TextView)getActivity().findViewById(R.id.lblModuleCaption);
        relativeToolbar         = (RelativeLayout)getActivity().findViewById(R.id.relativeToolbar);
        showToolbar();
        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().popBackStack("FragmentVerifyUser", FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });
    }

    private void showToolbar() {
        ToolbarCaptionClass toolbarCaptionClass = new ToolbarCaptionClass();
        ArrayList<String> arrayList = toolbarCaptionClass.returnCaptions(5);
        lblModuleTitle.setText(String.valueOf(arrayList.get(0)));
        lblModuleCaption.setText(String.valueOf(arrayList.get(1)));
    }

}
