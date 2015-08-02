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
public class MathViewBraceClose extends NonCompositeMathView {

    private final float BRACE_SIZE_DEGREES = 35; //brace size in degrees. Must be <= 58
    private final float BRACE_VERTICAL_PADDING = 1/20f;
    private final float BRACE_HORIZONTAL_PADDING = 3/10f;

    public MathViewBraceClose(Context ctx)
    {
        super(ctx);
    }

    public MathViewBraceClose(Context ctx, AttributeSet attrs)
    {
        super(ctx, attrs);
    }

    public MathViewBraceClose(Context ctx, AttributeSet attrs, int defStyleAttr)
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
        View matchingBrace = null;
        int nestedBracesDepth = 0;
        int height;
        int width;

        UIUtils.setMargins(this, 0, 0, (int) (mTextSize * TOKEN_MARGIN_FACTOR / 8), 0);

        for(int i = ((MathView) getParent()).indexOfChild(this)-1; i >= 0; i--)
        {
            final View sibling = ((MathView) getParent()).getChildAt(i);

            if(sibling instanceof  MathView)
            {
                if (sibling instanceof MathViewBraceClose)
                    nestedBracesDepth++;

                if (sibling instanceof MathViewBraceOpen && nestedBracesDepth > 0) {
                    nestedBracesDepth--;
                }
                else if (sibling instanceof MathViewBraceOpen) {
                    matchingBrace = sibling;
                    break;
                }
            }
        }

        if(matchingBrace != null) {
            height = matchingBrace.getMeasuredHeight();
            width = matchingBrace.getMeasuredWidth();

            setPadding(
                    matchingBrace.getPaddingLeft(),
                    matchingBrace.getPaddingTop(),
                    matchingBrace.getPaddingRight(),
                    matchingBrace.getPaddingBottom());
        }
        else {
            height = calculateTextHeight() + (int)(mTextSize * TOKEN_PADDING_FACTOR) * 2;

            setPadding(
                    (int)(height * BRACE_HORIZONTAL_PADDING),
                    (int)(height * BRACE_VERTICAL_PADDING),
                    (int)(height * BRACE_HORIZONTAL_PADDING),
                    (int)(height * BRACE_VERTICAL_PADDING));

            width = (int)(height * 3/20f);

            height += getPaddingTop() + getPaddingBottom();
            width += getPaddingLeft() + getPaddingRight();
        }

        mAlignmentBaseline = (matchingBrace != null) ? getAlignmentBaseline(matchingBrace) : height / 2 + getPaddingTop();

        setMeasuredDimension(
                resolveSize(width, widthMeasureSpec),
                resolveSize(height, heightMeasureSpec));
    }

    @Override
    protected void dispatchDraw(@NonNull Canvas canvas)
    {
        super.dispatchDraw(canvas);

        mDrawingPaint.setStyle(Paint.Style.STROKE);
        mDrawingPaint.setFlags(Paint.ANTI_ALIAS_FLAG);

        //draw right brace
        canvas.drawArc(
                new RectF(
                        getWidth() - getPaddingRight() - getHeight()*3.5f,
                        -getHeight(),
                        getWidth() - mStrokeWidth - getPaddingRight(),
                        getHeight()*2),
                - BRACE_SIZE_DEGREES/2, BRACE_SIZE_DEGREES,
                false, mDrawingPaint);
    }

    @Override
    public String getText()
    {
        return ")";
    }
}
