package com.awesomecalc.awesomecalculator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Thilo on 12.07.2015.
 */
public class MathViewRoot extends CompositeMathView {

    public MathViewRoot(Context ctx)
    {
        super(ctx);
    }

    public MathViewRoot(Context ctx, AttributeSet attrs)
    {
        super(ctx, attrs);
    }

    public MathViewRoot(Context ctx, AttributeSet attrs, int defStyleAttr)
    {
        super(ctx, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (getChildCount() != 2)
            throw new MathParser.ParseException("#root() can not have more or less than two arguments");

        MathView indexChild = (MathView) getChildAt(0);
        MathView radicandChild = (MathView) getChildAt(1);

        indexChild.setTextSize(Math.max(mTextSize * SUPERSCRIPT_TEXTSCALE_FACTOR, mMinTextSize));

        int heightAboveCenter;
        int heightBelowCenter;

        //apply margin to make space for the radical/root symbol
        UIUtils.setMargins(
                radicandChild,
                (int)(mTextSize / 3f),
                (int)(mTextSize / 5f),
                (int)(mTextSize / 5f),
                (int)(mTextSize / 10f));

        //padding between caret and root symbol
        UIUtils.addPadding(
                this,
                0,
                0,
                (int)(mTextSize * CARET_CORNER_RADIUS / 3),
                (int)(mTextSize * CARET_CORNER_RADIUS / 2.5f));

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

        int combinedWidth = indexChild.getMeasuredWidth()
                + UIUtils.getMarginLeft(radicandChild) + radicandChild.getMeasuredWidth() + UIUtils.getMarginRight(radicandChild);

        heightAboveCenter = Math.max(
                getAlignmentBaseline(indexChild) + Math.round((indexChild.getMeasuredHeight() - getAlignmentBaseline(indexChild))/2f) ,
                getAlignmentBaseline(radicandChild) + UIUtils.getMarginTop(radicandChild));

        heightBelowCenter = radicandChild.getMeasuredHeight() - getAlignmentBaseline(radicandChild) + UIUtils.getMarginBottom(radicandChild);

        mAlignmentBaseline = heightAboveCenter + getPaddingTop();

        setMeasuredDimension(
                resolveSize(getPaddingLeft() + combinedWidth  + getPaddingRight(), widthMeasureSpec),
                resolveSize(getPaddingTop() + heightAboveCenter + heightBelowCenter + getPaddingBottom(), heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        final View indexChild = getChildAt(0);
        final View radicandChild = getChildAt(1);

        indexChild.layout(
                getPaddingLeft(),
                Math.max(
                        Math.round(getMeasuredHeight()/2f - indexChild.getMeasuredHeight() + (indexChild.getMeasuredHeight() - getAlignmentBaseline(indexChild))/2f),
                        getPaddingTop()),
                getPaddingLeft() + indexChild.getMeasuredWidth(),
                Math.max(
                        Math.round(getMeasuredHeight()/2f + (indexChild.getMeasuredHeight() - getAlignmentBaseline(indexChild))/2f),
                        getPaddingTop() + indexChild.getMeasuredHeight()));

        int radicandChildTop = getAlignmentBaseline(this) - getAlignmentBaseline(radicandChild);

        radicandChild.layout(
                indexChild.getRight() + UIUtils.getMarginLeft(radicandChild),
                getPaddingTop() + radicandChildTop,
                indexChild.getRight() + UIUtils.getMarginLeft(radicandChild) + radicandChild.getMeasuredWidth(),
                getPaddingTop() + radicandChildTop + radicandChild.getMeasuredHeight());
    }

    @Override
    protected void dispatchDraw(@NonNull Canvas canvas)
    {
        super.dispatchDraw(canvas);

        mDrawingPaint.setStyle(Paint.Style.STROKE);
        mDrawingPaint.setFlags(Paint.ANTI_ALIAS_FLAG);

        if(mHasCaret)
            mDrawingPaint.setColor(mFocusColor.getDefaultColor());

        final View indexChild = getChildAt(0);
        final View radicandChild = getChildAt(1);
        Path rootSymbolPath = new Path();

        rootSymbolPath.moveTo(
                indexChild.getRight() - (radicandChild.getLeft() - indexChild.getRight()) / 1.2f,
                indexChild.getBottom() + (radicandChild.getBottom() - indexChild.getBottom()) / 4);

        //right
        rootSymbolPath.lineTo(
                indexChild.getRight() - (radicandChild.getLeft() - indexChild.getRight()) / 2,
                indexChild.getBottom() + mStrokeWidth);

        //down
        rootSymbolPath.lineTo(indexChild.getRight(), radicandChild.getBottom());

        //up
        rootSymbolPath.lineTo(
                radicandChild.getLeft() - mStrokeWidth * 2,
                radicandChild.getTop() - mStrokeWidth * 2);

        //right
        rootSymbolPath.lineTo(
                radicandChild.getRight() + mStrokeWidth * 2,
                radicandChild.getTop() - mStrokeWidth * 2);

        //down
        rootSymbolPath.rLineTo(0, mTextSize / 6);

        canvas.drawPath(rootSymbolPath, mDrawingPaint);
    }

    @Override
    public String getText()
    {
        return "#root(" + ((MathView) getChildAt(0)).getText() + "," + ((MathView) getChildAt(1)).getText() + ")";
    }
}
