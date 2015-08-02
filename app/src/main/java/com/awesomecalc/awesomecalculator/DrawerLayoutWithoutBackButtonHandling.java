package com.awesomecalc.awesomecalculator;

import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.view.KeyEvent;

/**
 * DrawerLayout without backbutton handling. Back button needs to be handled by the activity
 */
public class DrawerLayoutWithoutBackButtonHandling extends DrawerLayout {

    public DrawerLayoutWithoutBackButtonHandling(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public DrawerLayoutWithoutBackButtonHandling(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DrawerLayoutWithoutBackButtonHandling(Context context) {
        super(context);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return false;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
}
