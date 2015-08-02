package com.awesomecalc.awesomecalculator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Thilo on 12.07.2015.
 */
public class MathViewText extends NonCompositeMathView {
    private String mText;
    private Rect mTextBounds;

    public MathViewText(Context ctx)
    {
        super(ctx);
        mTextBounds = new Rect();
    }

    public MathViewText(Context ctx, AttributeSet attrs)
    {
        super(ctx, attrs);
        mTextBounds = new Rect();
    }

    public MathViewText(Context ctx, AttributeSet attrs, int defStyleAttr)
    {
        super(ctx, attrs, defStyleAttr);
        mTextBounds = new Rect();
    }

    @Override
    protected void parseText(String text)
    {
        mText = text;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        setPadding(
                (int)(mTextSize * TOKEN_PADDING_FACTOR),
                (int)(mTextSize * TOKEN_PADDING_FACTOR),
                (int)(mTextSize * TOKEN_PADDING_FACTOR),
                (int)(mTextSize * TOKEN_PADDING_FACTOR));

        if(MathParser.isOperator(mText.charAt(0))) {
            UIUtils.setMargins(this, (int) (mTextSize * TOKEN_MARGIN_FACTOR), 0, (int) (mTextSize * TOKEN_MARGIN_FACTOR), 0);
        }

        mDrawingPaint.setTextSize(mTextSize);
        mDrawingPaint.setColor(mTextColor.getDefaultColor());
        mDrawingPaint.setTypeface(mFont);
        mDrawingPaint.setStyle(Paint.Style.FILL);
        mDrawingPaint.setStrokeWidth(mStrokeWidth);
        mDrawingPaint.setFlags(Paint.ANTI_ALIAS_FLAG);

        mDrawingPaint.getTextBounds(mText, 0, mText.length(), mTextBounds);

        int height = mTextBounds.height();
        mDrawingPaint.getTextBounds("3", 0, 1, mTextBounds);
        height = Math.max(height, mTextBounds.height());

        int width = (int) mDrawingPaint.measureText(mText, 0, mText.length());

        mAlignmentBaseline = height/2 + getPaddingTop();

        setMeasuredDimension(
                resolveSize(getPaddingLeft() + width + getPaddingRight(), widthMeasureSpec),
                resolveSize(getPaddingTop() + height + getPaddingBottom(), heightMeasureSpec));

    }

    @Override
    protected void dispatchDraw(@NonNull Canvas canvas)
    {
        super.dispatchDraw(canvas);

        mDrawingPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mDrawingPaint.setStyle(Paint.Style.FILL);

        int xPos = 0;
        int yPos = (int)( (canvas.getHeight() - getPaddingTop() - getPaddingBottom()) / 2
                - (mDrawingPaint.descent() + mDrawingPaint.ascent()) / 2);

        canvas.drawText(mText, getPaddingLeft() + xPos, getPaddingTop() + yPos, mDrawingPaint);
    }

    @Override
    public String getText()
    {
        return mText;
    }
}
