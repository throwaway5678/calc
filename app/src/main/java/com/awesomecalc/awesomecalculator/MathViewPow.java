package com.awesomecalc.awesomecalculator;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Thilo on 12.07.2015.
 */
public class MathViewPow extends CompositeMathView {

    public MathViewPow(Context ctx)
    {
        super(ctx);
    }

    public MathViewPow(Context ctx, AttributeSet attrs)
    {
        super(ctx, attrs);
    }

    public MathViewPow(Context ctx, AttributeSet attrs, int defStyleAttr)
    {
        super(ctx, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (getChildCount() != 2)
            throw new MathParser.ParseException("pow() can not have more or less than two arguments");

        MathView baseChild = (MathView) getChildAt(0);
        MathView expoChild = (MathView) getChildAt(1);

        expoChild.setTextSize(Math.max(mTextSize * SUPERSCRIPT_TEXTSCALE_FACTOR, mMinTextSize));

        baseChild.measure(
                getChildMeasureSpec(widthMeasureSpec, getPaddingLeft() + getPaddingRight(),
                        baseChild.getLayoutParams().width),
                getChildMeasureSpec(heightMeasureSpec, getPaddingTop() + getPaddingBottom(),
                        baseChild.getLayoutParams().height));

        expoChild.measure(
                getChildMeasureSpec(widthMeasureSpec, getPaddingLeft() + getPaddingRight(),
                        expoChild.getLayoutParams().width),
                getChildMeasureSpec(heightMeasureSpec, getPaddingTop() + getPaddingBottom(),
                        expoChild.getLayoutParams().height));

        int heightAboveCenter = Math.round(getAlignmentBaseline(baseChild) - baseChild.getMeasuredHeight() / 4f + getAlignmentBaseline(expoChild));

        int heightBelowCenter = Math.max(baseChild.getMeasuredHeight() - getAlignmentBaseline(baseChild), expoChild.getMeasuredHeight() - heightAboveCenter);

        mAlignmentBaseline = heightAboveCenter + getPaddingTop();

        setMeasuredDimension(
                resolveSize(getPaddingLeft() + baseChild.getMeasuredWidth() + expoChild.getMeasuredWidth() + getPaddingRight(), widthMeasureSpec),
                resolveSize(getPaddingTop() + heightAboveCenter + heightBelowCenter + getPaddingBottom() , heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        final View baseChild = getChildAt(0);
        final View expoChild = getChildAt(1);

        baseChild.layout(
                getPaddingLeft(),
                mAlignmentBaseline - getAlignmentBaseline(baseChild),
                getPaddingLeft() + baseChild.getMeasuredWidth(),
                mAlignmentBaseline - getAlignmentBaseline(baseChild) + baseChild.getMeasuredHeight());

        expoChild.layout(
                getPaddingLeft() + baseChild.getMeasuredWidth(),
                getPaddingTop(),
                getPaddingLeft() + baseChild.getMeasuredWidth() + expoChild.getMeasuredWidth(),
                getPaddingTop() + expoChild.getMeasuredHeight());
    }

    @Override
    public String getText() {
        return "#pow(" + ((MathView) getChildAt(0)).getText() + "," + ((MathView) getChildAt(1)).getText() + ")";
    }
}
