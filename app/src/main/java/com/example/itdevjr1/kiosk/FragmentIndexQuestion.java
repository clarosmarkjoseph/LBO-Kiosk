package com.example.itdevjr1.kiosk;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by paolohilario on 1/25/18.
 */

public class FragmentIndexQuestion extends Fragment {

    View layout;
    Button btnQuestionNo,btnQuestionYes,btnPrev;
    TextView lblModuleTitle,lblModuleCaption;
    RelativeLayout relativeToolbar;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_index_question,container,false);
        initElements();
        return  layout;
    }

    private void initElements() {


        btnQuestionYes          = (Button)layout.findViewById(R.id.btnQuestionYes);
        btnQuestionNo           = (Button)layout.findViewById(R.id.btnQuestionNo);
        btnPrev                 = (Button)layout.findViewById(R.id.btnPrev);
        relativeToolbar     = (RelativeLayout)getActivity().findViewById(R.id.relativeToolbar);
        lblModuleTitle      = (TextView)getActivity().findViewById(R.id.lblModuleTitle);
        lblModuleCaption    = (TextView)getActivity().findViewById(R.id.lblModuleCaption);
        showToolbar();

        //elements of login
        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                getFragmentManager().popBackStackImmediate("FragmentIndex",0);
                getFragmentManager().popBackStack("FragmentIndexStart", android.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });

        btnQuestionYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentIndexRegister fragment = new FragmentIndexRegister();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.enter, R.anim.exit,R.anim.left_to_right,R.anim.right_to_left);
                fragmentTransaction.replace(R.id.frameLayout, fragment,"FragmentRegister");
                fragmentTransaction.addToBackStack("FragmentQuestion");
                fragmentTransaction.commit();
            }
        });

        btnQuestionNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentIndexVerifyUser fragment = new FragmentIndexVerifyUser();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,android.R.anim.fade_in, android.R.anim.fade_out);
                fragmentTransaction.replace(R.id.frameLayout, fragment,"FragmentVerifyUser");
                fragmentTransaction.addToBackStack("FragmentQuestion");
                fragmentTransaction.commit();
            }
        });


    }

    private void showToolbar() {
        ToolbarCaptionClass toolbarCaptionClass = new ToolbarCaptionClass();
        relativeToolbar.setVisibility(View.VISIBLE);
        ArrayList<String> arrayList = toolbarCaptionClass.returnCaptions(1);
        lblModuleTitle.setText(String.valueOf(arrayList.get(0)));
        lblModuleCaption.setText(String.valueOf(arrayList.get(1)));
    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        getActivity().onBackPressed();
        return super.onOptionsItemSelected(item);
    }


}
