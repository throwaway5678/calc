package com.awesomecalc.awesomecalculator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Thilo on 12.07.2015.
 */
public class MathViewBraceOpen extends NonCompositeMathView {

    private final float BRACE_SIZE_DEGREES = 35; //brace size in degrees. Must be <= 58
    private final float BRACE_VERTICAL_PADDING = 1/20f;
    private final float BRACE_HORIZONTAL_PADDING = 3/10f;

    public MathViewBraceOpen(Context ctx)
    {
        super(ctx);
    }

    public MathViewBraceOpen(Context ctx, AttributeSet attrs)
    {
        super(ctx, attrs);
    }

    public MathViewBraceOpen(Context ctx, AttributeSet attrs, int defStyleAttr)
    {
        super(ctx, attrs, defStyleAttr);
    }

    @Override
    protected void parseText(String text)
    {
        //nothing
    }

    private int calculateTextHeight()
    {
        Paint p = new Paint();
        p.setTextSize(mTextSize);
        p.setTypeface(mFont);
        p.setStyle(Paint.Style.FILL);
        Rect textBounds = new Rect();

        p.getTextBounds("3", 0, 1, textBounds);

        return textBounds.height();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int heightAboveCenter = 0;
        int heightBelowCenter = 0;
        int nestedBracesDepth = 0;

        UIUtils.setMargins(this, 0, 0, (int)(mTextSize * TOKEN_MARGIN_FACTOR / 8), 0);

        if(((MathView) getParent()).getChildCount()-1 > ((MathView) getParent()).indexOfChild(this))
            for (int i = ((MathView) getParent()).indexOfChild(this)+1; i < ((MathView) getParent()).getChildCount(); i++)
            {
                final View sibling = ((MathView) getParent()).getChildAt(i);

                if (sibling instanceof MathView)
                {
                    if (sibling instanceof MathViewBraceOpen)
                        nestedBracesDepth++;

                    if (sibling instanceof MathViewBraceClose && nestedBracesDepth > 0)
                        nestedBracesDepth--;
                    else if (sibling instanceof MathViewBraceClose)
                        break;
                }

                heightAboveCenter = Math.max(heightAboveCenter, getAlignmentBaseline(sibling));
                heightBelowCenter = Math.max(heightBelowCenter, sibling.getMeasuredHeight() - getAlignmentBaseline(sibling));
            }
        else
        {
            heightAboveCenter = heightBelowCenter = calculateTextHeight() / 2 + (int)(mTextSize * TOKEN_PADDING_FACTOR);
        }

        int combinedHeight = Math.max(heightAboveCenter, heightBelowCenter) * 2;

        setPadding(
                (int) (combinedHeight * BRACE_HORIZONTAL_PADDING),
                (int) (combinedHeight * BRACE_VERTICAL_PADDING),
                (int) (combinedHeight * BRACE_HORIZONTAL_PADDING),
                (int) (combinedHeight * BRACE_VERTICAL_PADDING));

        mAlignmentBaseline = Math.max(heightAboveCenter, heightBelowCenter) + getPaddingTop();

        setMeasuredDimension(
                resolveSize(getPaddingLeft() + (int)(combinedHeight * 3/20f)  + getPaddingRight(), widthMeasureSpec),
                resolveSize(getPaddingTop() + combinedHeight  + getPaddingBottom(), heightMeasureSpec));
    }

    @Override
    protected void dispatchDraw(@NonNull Canvas canvas)
    {
        super.dispatchDraw(canvas);

        mDrawingPaint.setStyle(Paint.Style.STROKE);
        mDrawingPaint.setFlags(Paint.ANTI_ALIAS_FLAG);

        //draw left brace
        canvas.drawArc(
                new RectF(
                        getPaddingLeft() + mStrokeWidth,
                        -getHeight(),
                        getPaddingLeft() + getHeight()*3.5f,
                        getHeight()*2),
                180-BRACE_SIZE_DEGREES/2, BRACE_SIZE_DEGREES,
                false, mDrawingPaint);
    }

    @Override
    public String getText()
    {
        return "(";
    }
}
