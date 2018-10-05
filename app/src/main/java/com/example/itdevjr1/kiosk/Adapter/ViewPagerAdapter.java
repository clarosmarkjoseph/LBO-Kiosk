package com.example.itdevjr1.kiosk.Adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.itdevjr1.kiosk.FragmentStep1;
import com.example.itdevjr1.kiosk.FragmentStep2;
import com.example.itdevjr1.kiosk.FragmentStep3;
import com.example.itdevjr1.kiosk.Utilities;

/**
 * Created by paolohilario on 1/15/18.
 */

public class ViewPagerAdapter extends FragmentStatePagerAdapter {
    int fragmentCount = 0;
    Context context;
    Utilities utilities;


    public ViewPagerAdapter(FragmentManager fm, Context ctx, int fragmentCount1) {
        super(fm);
        this.fragmentCount = fragmentCount1;
        this.context = ctx;
        this.utilities = new Utilities(ctx);
    }


    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        if (position == 0){
            fragment = new FragmentStep1();
        }
        else if (position == 1){
            fragment = new FragmentStep2();
        }
        else if(position == 2){
            fragment = new FragmentStep3();
        }

        Bundle bundle= new Bundle();
        bundle.putInt("position",position);
        fragment.setArguments(bundle);
        return fragment;
    }


    @Override
    public int getCount() {
        return fragmentCount;
    }


}
