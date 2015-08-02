package com.awesomecalc.awesomecalculator;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;


/** intercepts swipes to the right so we can handle them instead of the child scrollview consuming them
 *
 *  CAUTION: using setOnTouchListener() with this layout will cause NullPtrException!
 *
 */

public class DrawerContainerLayout extends RelativeLayout {

    private float mStartX;
    private float mStartY;
    private boolean mIsRightSwipe = false;
    private Runnable mOnSwipeRightListener;
    final int SWIPE_DISTANCE_THRESHOLD = 100;
    final Context mCtx;

    public DrawerContainerLayout(Context ctx)
    {
        super(ctx);
        mCtx = ctx;
    }

    public DrawerContainerLayout(Context ctx, AttributeSet attrs)
    {
        super(ctx, attrs);
        mCtx = ctx;
    }

    public DrawerContainerLayout(Context ctx, AttributeSet attrs, int defStyleAttr)
    {
        super(ctx, attrs, defStyleAttr);
        mCtx = ctx;
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev)
    {


        if(ev.getAction() == MotionEvent.ACTION_DOWN)
        {
            mStartX = ev.getX();
            mStartY = ev.getY();

            //we don't know yet, if it's a swipe or just a click, so never intercept it
            return false;
        }

        else if(ev.getAction() == MotionEvent.ACTION_MOVE)
        {
            float x = ev.getX();
            float y = ev.getY();
            float xDelta = x - mStartX;
            float yDelta = y - mStartY;

            //detect if horizontal swipe
            if(Math.abs(xDelta) > (1.5* Math.abs(yDelta)) && Math.abs(xDelta) > SWIPE_DISTANCE_THRESHOLD)
            {
                //detect if right swipe
                if(xDelta > 0)
                {
                    //we want to intercept this message and handle it in the viewgroup
                    return true;
                }
            }

            //we don't want to handle this message
            return false;

        }
        else
        {
            return super.onInterceptTouchEvent(ev);
        }
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent ev)
    {
        super.onTouchEvent(ev);

        switch(ev.getAction())
        {
            case MotionEvent.ACTION_MOVE:
            {

                float x = ev.getX();
                float y = ev.getY();
                float xDelta = x - mStartX;
                float yDelta = y - mStartY;

                //check if it's a horizontal swipe
                if(Math.abs(xDelta) > Math.abs(yDelta))
                {
                    //check if right swipe
                    if(xDelta > 0)
                    {
                        mIsRightSwipe = true;
                        return true;
                    }
                    else
                    {
                        //dont want to handle left swipes
                        mIsRightSwipe = false;
                        return false;
                    }
                }
                else
                {
                    //dont want to handle vertical swipes
                    return false;
                }
            }
            case MotionEvent.ACTION_CANCEL:
                return true;

            case MotionEvent.ACTION_UP:
                //call only upon gesture ends because ACTION_MOVE is send multiple times during the gesture
                if(mIsRightSwipe)
                    mOnSwipeRightListener.run();

                return true;

            default:
                //dont handle everything else
                return false;

        }
    }

    public void setSwipeRightListener(Runnable listener)
    {
        mOnSwipeRightListener = listener;
    }

}
