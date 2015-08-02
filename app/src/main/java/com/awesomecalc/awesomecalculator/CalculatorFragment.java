package com.awesomecalc.awesomecalculator;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class CalculatorFragment extends Fragment {

    private View mThisView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mThisView = inflater.inflate(R.layout.frag_calculator, container, false);

        return mThisView;
    }
}
