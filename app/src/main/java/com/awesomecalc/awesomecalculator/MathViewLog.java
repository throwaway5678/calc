package com.awesomecalc.awesomecalculator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Thilo on 12.07.2015.
 */
public class MathViewLog extends CompositeMathView {

    public MathViewLog(Context ctx)
    {
        super(ctx);
    }

    public MathViewLog(Context ctx, AttributeSet attrs)
    {
        super(ctx, attrs);
    }

    public MathViewLog(Context ctx, AttributeSet attrs, int defStyleAttr)
    {
        super(ctx, attrs, defStyleAttr);
    }

    @Override
    protected void parseText(String text)
    {
        //add "log" child
        TextView v = new TextView(getContext());
        v.setText("log");
        v.setPadding((int) (mTextSize * TOKEN_PADDING_FACTOR), 0, 0, 0);
        v.setTextSize(mTextSize);
        v.setTextColor(mTextColor.getDefaultColor());
        v.setTypeface(mFont);

        final MathView thisView = this;

        v.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v)
            {
                thisView.onClick(thisView);
            }
        });

        if(BuildFlags.SHOW_MATHVIEW_VIEW_BOUNDS)
            v.setBackground(getResources().getDrawable(R.drawable.view_bounds));

        addView(v, new MarginLayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        super.parseText(text);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (getChildCount() != 3)
            throw new MathParser.ParseException("log() can not have more or less than two arguments");

        final View logChild = getChildAt(0);
        final MathView baseChild = (MathView) getChildAt(1);
        final MathView numerusChild = (MathView) getChildAt(2);

        baseChild.setTextSize(Math.max(mTextSize * SUPERSCRIPT_TEXTSCALE_FACTOR, mMinTextSize));

        int heightAboveCenter;
        int heightBelowCenter;

        //extra space for the braces
        UIUtils.setMarginLeft(numerusChild, (int)(mTextSize / 5));
        UIUtils.setMarginRight(numerusChild, (int) (mTextSize / 5));
        UIUtils.setMarginRight(baseChild, (int) (mTextSize / 20));

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

        int combinedWidth = logChild.getMeasuredWidth() + baseChild.getMeasuredWidth() + UIUtils.getMarginRight(baseChild)
                + UIUtils.getMarginLeft(numerusChild) + numerusChild.getMeasuredWidth() + UIUtils.getMarginRight(numerusChild);


        heightAboveCenter = getAlignmentBaseline(numerusChild) + getPaddingTop();

        int baseY = heightAboveCenter - baseChild.getMeasuredHeight()/3;
        int baseBottom = baseY + baseChild.getMeasuredHeight();

        heightBelowCenter = Math.max(logChild.getMeasuredHeight()/2 + baseChild.getMeasuredHeight()/3, getAlignmentBaseline(numerusChild)) + getPaddingBottom();

        mAlignmentBaseline = heightAboveCenter;

        setMeasuredDimension(
                resolveSize(getPaddingLeft() + combinedWidth + getPaddingRight(), widthMeasureSpec),
                resolveSize(heightAboveCenter + heightBelowCenter, heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        final View logChild = getChildAt(0);
        final View baseChild = getChildAt(1);
        final View numerusChild = getChildAt(2);

        numerusChild.layout(
                getPaddingLeft() + logChild.getMeasuredWidth() + baseChild.getMeasuredWidth() + UIUtils.getMarginRight(baseChild) + UIUtils.getMarginLeft(numerusChild),
                getPaddingTop(),
                getPaddingLeft() + logChild.getMeasuredWidth() + baseChild.getMeasuredWidth() + numerusChild.getMeasuredWidth() +  UIUtils.getMarginLeft(numerusChild),
                getPaddingTop() + numerusChild.getMeasuredHeight());

        logChild.layout(
                getPaddingLeft(),
                getAlignmentBaseline(numerusChild) - logChild.getMeasuredHeight() / 2,
                getPaddingLeft() + logChild.getMeasuredWidth(),
                getAlignmentBaseline(numerusChild) + logChild.getMeasuredHeight() / 2);

        baseChild.layout(
                logChild.getRight(),
                logChild.getBottom() - baseChild.getMeasuredHeight() * 2/3,
                logChild.getRight() + baseChild.getMeasuredWidth(),
                logChild.getBottom() + baseChild.getMeasuredHeight() / 3);
    }

    @Override
    protected void dispatchDraw(@NonNull Canvas canvas)
    {
        super.dispatchDraw(canvas);
        mDrawingPaint.setStyle(Paint.Style.STROKE);
        mDrawingPaint.setFlags(Paint.ANTI_ALIAS_FLAG);

        final View logChild = getChildAt(0);
        final View baseChild = getChildAt(1);
        final View numerusChild = getChildAt(2);

        ((TextView) logChild).setTextColor(mDrawingPaint.getColor());

        //draw left brace
        canvas.drawArc(
                new RectF(
                        baseChild.getRight() + UIUtils.getMarginRight(baseChild) + mStrokeWidth,
                        getPaddingTop() - getAlignmentBaseline(numerusChild)*2,
                        baseChild.getRight() + UIUtils.getMarginRight(baseChild) + getAlignmentBaseline(numerusChild)*8,
                        getPaddingTop() + getAlignmentBaseline(numerusChild)*4),
                180 - 19, 19 * 2,
                false, mDrawingPaint);

        //draw right brace
        canvas.drawArc(
                new RectF(
                        getWidth() - getPaddingRight() - getAlignmentBaseline(numerusChild)*8,
                        getPaddingTop() - getAlignmentBaseline(numerusChild)*2,
                        getWidth() - getPaddingRight() - mStrokeWidth,
                        getPaddingTop() + getAlignmentBaseline(numerusChild)*4),
                -19, 19 * 2,
                false, mDrawingPaint);
    }

    @Override
    public String getText()
    {
        return "#log(" + ((MathView) getChildAt(0)).getText() + "," + ((MathView) getChildAt(1)).getText() + ")";
    }

    @Override
    public MathView findCaret()
    {
        MathView v = null;

        if(mHasCaret)
            return this;

        //start at index 1 because of the "log" child
        for (int i = 1; i < getChildCount() ; i++)
        {
            v = ((MathView) getChildAt(i)).findCaret();

            if(v != null)
                return v;
        }

        return v;
    }
}
