package com.awesomecalc.awesomecalculator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

import com.awesomecalc.awesomecalculator.MathView;
import com.awesomecalc.awesomecalculator.UIUtils;

/**
 * Created by Thilo on 31.07.2015.
 */
public class NonCompositeMathView extends MathView {

    public NonCompositeMathView(Context ctx)
    {
        super(ctx);
    }

    public NonCompositeMathView(Context ctx, AttributeSet attrs)
    {
        super(ctx, attrs);
    }

    public NonCompositeMathView(Context ctx, AttributeSet attrs, int defStyleAttr)
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
    protected void dispatchDraw(@NonNull Canvas canvas)
    {
        super.dispatchDraw(canvas);

        if(mHasCaret)
            drawCaret(canvas);

        mDrawingPaint.setTypeface(mFont);
        mDrawingPaint.setTextSize(mTextSize);
        mDrawingPaint.setStrokeWidth(mStrokeWidth);

        if(mHasCaret)
            mDrawingPaint.setColor(mFocusTextColor.getDefaultColor());
        else if(grandparentHasCaret()) {
            mDrawingPaint.setColor(mFocusColor.getDefaultColor());
            UIUtils.recursiveInvalidateChilds(this);
        }
        else
            mDrawingPaint.setColor(mTextColor.getDefaultColor());
    }

    protected void drawCaret(@NonNull Canvas canvas)
    {
        mDrawingPaint.setStyle(Paint.Style.FILL);
        mDrawingPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mDrawingPaint.setColor(mFocusColor.getDefaultColor());

        mBackgroundRect.left = 0;
        mBackgroundRect.top = 0;
        mBackgroundRect.right = getWidth();
        mBackgroundRect.bottom = getHeight();

        canvas.drawRoundRect(mBackgroundRect, mTextSize * CARET_CORNER_RADIUS, mTextSize * CARET_CORNER_RADIUS, mDrawingPaint);
    }

}
