package com.awesomecalc.awesomecalculator;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;


/**
 * intercepts swipe touch events because otherwise the child buttons would eat the event and
 * it wouldn't be possible to swipe when touching a child button
 *
 *  CAUTION: using setOnTouchListener() with this layout will probably cause NullPtrException!
 */

public class KeyboardViewPager extends ViewPager {

    private float mStartX;
    private float mStartY;
    private final float SWIPE_DISTANCE_THRESHOLD = 50; //minimum distance to swipe

    public KeyboardViewPager(Context ctx)
    {
        super(ctx);
    }

    public KeyboardViewPager(Context ctx, AttributeSet attrs)
    {
        super(ctx, attrs);
    }

    @Override
    public void setOnTouchListener(OnTouchListener listener)
    {
        throw new AssertionError("setOnTouchListener can not be used with KeyboardViewPager");
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev)
    {
        switch(ev.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                mStartX = ev.getX();
                mStartY = ev.getY();
                return super.onInterceptTouchEvent(ev);

            case MotionEvent.ACTION_MOVE:
            {
                float currentX = ev.getX();
                float currentY = ev.getY();

                float deltaX = currentX - mStartX;
                float deltaY = currentY - mStartY;

                if(Math.abs(deltaX) > Math.abs(deltaY) && Math.abs(deltaX) > SWIPE_DISTANCE_THRESHOLD)
                {
                    return true;
                }

                return super.onInterceptTouchEvent(ev);
            }

            default:
                return super.onInterceptTouchEvent(ev);
        }
    }



}
