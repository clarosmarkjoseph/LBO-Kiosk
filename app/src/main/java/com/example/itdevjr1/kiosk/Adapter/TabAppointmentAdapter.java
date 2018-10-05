package com.example.itdevjr1.kiosk.Adapter;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.itdevjr1.kiosk.FragmentAppointmentPackage;
import com.example.itdevjr1.kiosk.FragmentAppointmentProduct;
import com.example.itdevjr1.kiosk.FragmentAppointmentService;
import com.example.itdevjr1.kiosk.Utilities;

/**
 * Created by paolohilario on 2/6/18.
 */

public class TabAppointmentAdapter extends FragmentStatePagerAdapter {
    Context context;
    Utilities utilities;

    int count_tabs = 0;
    public TabAppointmentAdapter(FragmentManager fm, int tabs) {
        super(fm);
        this.count_tabs = tabs;
    }

    @Override
    public Fragment getItem(int position) {
        if(position == 0){
            FragmentAppointmentService serviceTab = new FragmentAppointmentService();
            return serviceTab;
        }
        if(position == 1){
            FragmentAppointmentPackage packageTab = new FragmentAppointmentPackage();
            return packageTab;
        }
        else{
            FragmentAppointmentProduct productTab = new FragmentAppointmentProduct();
            return  productTab;
        }
    }


    @Override
    public int getCount() {
        return count_tabs;
    }


}
