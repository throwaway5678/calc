package com.awesomecalc.awesomecalculator.unused;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Thilo on 07.01.2015.
 */
public class CSwipeListener implements View.OnTouchListener {

    private final GestureDetector mGestureDetector;

    CSwipeListener(Context ctx)
    {
        mGestureDetector = new GestureDetector(ctx, new CGestureListener());
    }

    //to be overridden
    public void onSwipeLeft() {}

    //to be overridden
    public void onSwipeRight() {}

    //to be overridden
    public void onSwipeUp() {}

    //to be overridden
    public void onSwipeDown() {}



    public boolean onTouch(View v, MotionEvent event)
    {
        return mGestureDetector.onTouchEvent(event);
    }


    private final class CGestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_DISTANCE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e)
        {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY)
        {
            float distanceX = event2.getX() - event1.getX();
            float distanceY = event2.getY() - event1.getY();

            if (Math.abs(distanceX) > Math.abs(distanceY) && Math.abs(distanceX) > SWIPE_DISTANCE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD)
            {
                if (distanceX > 0)
                {
                    onSwipeRight();
                }
                else
                {
                    onSwipeLeft();
                }

                return true;
            }
            else if (Math.abs(distanceY) > Math.abs(distanceX) && Math.abs(distanceY) > SWIPE_DISTANCE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD)
            {
                if (distanceY > 0)
                {
                    onSwipeDown();
                }
                else
                {
                    onSwipeUp();
                }

                return true;
            }

            return false;
        }
    }
}

