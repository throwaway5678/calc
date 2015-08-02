package com.awesomecalc.awesomecalculator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Thilo on 12.07.2015.
 */
public class MathViewFraction extends CompositeMathView {
    private final float FRACTION_LINE_PADDING = 1/8f;

    public MathViewFraction(Context ctx)
    {
        super(ctx);
    }

    public MathViewFraction(Context ctx, AttributeSet attrs)
    {
        super(ctx, attrs);
    }

    public MathViewFraction(Context ctx, AttributeSet attrs, int defStyleAttr)
    {
        super(ctx, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if(getChildCount() != 2)
            throw new RuntimeException("#fract() can not have more or less than 2 childs");

        for (int i = 0; i < getChildCount(); i++)
        {
            final View child = getChildAt(i);

            if(child instanceof MathView)
                ((MathView) child).setTextSize(Math.max(mTextSize * SUPERSCRIPT_TEXTSCALE_FACTOR, mMinTextSize));
            else
                throw new RuntimeException("child of MathView is not of type MathView");
        }

        View numeratorChild = getChildAt(0); //upper child
        View denominatorChild = getChildAt(1); //lower child;

        UIUtils.addPadding(this, (int) (mTextSize / 10), 0, (int) (mTextSize / 10), 0);

        //apply margin to make space for the line inbetween numerator and denominator
        UIUtils.setMarginBottom(numeratorChild, (int)(mTextSize * FRACTION_LINE_PADDING));

        for (int i = 0; i < getChildCount(); i++)
        {
            final View child = getChildAt(i);

            child.measure(
                    getChildMeasureSpec(widthMeasureSpec,
                            getPaddingLeft() + getPaddingRight(),
                            child.getLayoutParams().width),
                    getChildMeasureSpec(heightMeasureSpec,
                            getPaddingTop() + getPaddingBottom(),
                            child.getLayoutParams().height));
        }

        mAlignmentBaseline = numeratorChild.getBottom() + Math.round((denominatorChild.getTop() - numeratorChild.getBottom()) / 2f);

        setMeasuredDimension(
                resolveSize(getPaddingLeft() + Math.max(numeratorChild.getMeasuredWidth(), denominatorChild.getMeasuredWidth()) + getPaddingRight(), widthMeasureSpec),
                resolveSize(getPaddingTop()
                        + numeratorChild.getMeasuredHeight() + UIUtils.getMarginBottom(numeratorChild)
                        + denominatorChild.getMeasuredHeight() + getPaddingBottom(), heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        final View numeratorChild = getChildAt(0);
        final View denominatorChild = getChildAt(1);

        numeratorChild.layout(
                Math.round(getWidth() / 2f - numeratorChild.getMeasuredWidth() / 2f),
                getPaddingTop(),
                Math.round(getWidth() / 2f + numeratorChild.getMeasuredWidth() / 2f),
                getPaddingTop() + numeratorChild.getMeasuredHeight());

        denominatorChild.layout(
                Math.round(getWidth() / 2f - denominatorChild.getMeasuredWidth() / 2f),
                getPaddingTop() + numeratorChild.getBottom() + UIUtils.getMarginBottom(numeratorChild),
                Math.round(getWidth() / 2f + denominatorChild.getMeasuredWidth() / 2f),
                getPaddingTop() + numeratorChild.getBottom() + UIUtils.getMarginBottom(numeratorChild) + denominatorChild.getMeasuredHeight());
    }

    @Override
    protected void dispatchDraw(@NonNull Canvas canvas)
    {
        super.dispatchDraw(canvas);

        final View numeratorChild = getChildAt(0);
        final View denominatorChild = getChildAt(1);

        mDrawingPaint.setFlags(mDrawingPaint.getFlags() & ~Paint.ANTI_ALIAS_FLAG);

        canvas.drawLine(
                getPaddingLeft(),
                Math.round(numeratorChild.getBottom() + (denominatorChild.getTop() - numeratorChild.getBottom()) / 2f),
                getWidth() - getPaddingRight(),
                Math.round(numeratorChild.getBottom() + (denominatorChild.getTop() - numeratorChild.getBottom()) / 2f),
                mDrawingPaint);
    }

    @Override
    public String getText()
    {
        return "#fract(" + ((MathView) getChildAt(0)).getText() + "," + ((MathView) getChildAt(1)).getText() + ")";
    }
}
