package com.awesomecalc.awesomecalculator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Thilo on 16.07.2015.
 */
public class CompositeMathView extends MathView {

    public CompositeMathView(Context ctx)
    {
        super(ctx);
    }

    public CompositeMathView(Context ctx, AttributeSet attrs)
    {
        super(ctx, attrs);
    }

    public CompositeMathView(Context ctx, AttributeSet attrs, int defStyleAttr)
    {
        super(ctx, attrs, defStyleAttr);
    }

    @Override
    public void onClick(View v)
    {
        MathView currentlySelectedView;

        if(mHasCaret)
        {
            mHasCaret = false;
            UIUtils.recursiveInvalidateChilds(this);
        }
        else
        {
            currentlySelectedView = getTopMostMathView().findCaret();

            if(currentlySelectedView != null) {
                currentlySelectedView.mHasCaret = false;
                UIUtils.recursiveInvalidateChilds(currentlySelectedView);
            }

            mHasCaret = true;
            UIUtils.recursiveInvalidateChilds(this);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        //padding for the caret
        setPadding(
                (int)mStrokeWidth,
                (int)mStrokeWidth,
                (int)mStrokeWidth,
                (int)mStrokeWidth);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev)
    {
        if(ev.getAction() == MotionEvent.ACTION_DOWN)
        {
            if(grandchildHasCaret())
                return false;

            if(!mHasCaret)
                return true;
        }

        return super.onInterceptTouchEvent(ev);
    }

    @Override
    protected void dispatchDraw(@NonNull Canvas canvas)
    {
        super.dispatchDraw(canvas);

        if(mHasCaret)
            drawCaret(canvas);

        mDrawingPaint.setTypeface(mFont);
        mDrawingPaint.setTextSize(mTextSize);
        mDrawingPaint.setStrokeWidth(mStrokeWidth);

        if(mHasCaret)
            mDrawingPaint.setColor(mFocusColor.getDefaultColor());
        else if(grandparentHasCaret()) {
            mDrawingPaint.setColor(mFocusColor.getDefaultColor());
            UIUtils.recursiveInvalidateChilds(this);
        }
        else
            mDrawingPaint.setColor(mTextColor.getDefaultColor());
    }

    protected void drawCaret(@NonNull Canvas canvas)
    {
        mDrawingPaint.setStyle(Paint.Style.STROKE);
        mDrawingPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mDrawingPaint.setColor(mFocusColor.getDefaultColor());
        mDrawingPaint.setStrokeWidth(mStrokeWidth);

        mBackgroundRect.left = 0 + mDrawingPaint.getStrokeWidth()/2;
        mBackgroundRect.top = 0 + mDrawingPaint.getStrokeWidth()/2;
        mBackgroundRect.right = getWidth() - mDrawingPaint.getStrokeWidth()/2;
        mBackgroundRect.bottom = getHeight() - mDrawingPaint.getStrokeWidth()/2;

        canvas.drawRoundRect(mBackgroundRect, mTextSize * CARET_CORNER_RADIUS, mTextSize * CARET_CORNER_RADIUS, mDrawingPaint);
    }
}
