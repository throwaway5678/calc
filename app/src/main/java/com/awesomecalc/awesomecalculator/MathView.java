package com.awesomecalc.awesomecalculator;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Thilo on 12.07.2015.
 */
public class MathView extends ViewGroup implements ViewGroup.OnClickListener {
    private TypedArray mTypedAttributes;
    protected float mTextSize;
    protected float mMinTextSize; //0 if unlimited
    protected float mStrokeWidth = 2.5f;
    protected ColorStateList mTextColor;
    protected ColorStateList mFocusTextColor;
    protected ColorStateList mFocusColor;
    protected Typeface mFont;
    protected boolean mReadOnly = true;
    protected boolean mHasCaret = false;
    protected Paint mDrawingPaint;
    protected int mAlignmentBaseline = 0;
    protected RectF mBackgroundRect;

    //every size is a multiple of mTextSize
    protected final float SUPERSCRIPT_TEXTSCALE_FACTOR = 9/10f; //factor by which superscript text like x^y gets scaled
    protected final float TOKEN_PADDING_FACTOR = 2/10f; //lr padding for every token, so background is wide enough
    protected final float TOKEN_MARGIN_FACTOR = 0; //lr spacing between the tokens
    protected final float CARET_CORNER_RADIUS = 1/3.5f;

    public MathView(Context ctx)
    {
        super(ctx);
        init();
    }

    public MathView(Context ctx, AttributeSet attrs)
    {
        super(ctx, attrs);
        mTypedAttributes = getContext().obtainStyledAttributes(attrs, R.styleable.MathView, 0, 0);
        init();
    }

    public MathView(Context ctx, AttributeSet attrs, int defStyleAttr)
    {
        super(ctx, attrs, defStyleAttr);
        mTypedAttributes = getContext().obtainStyledAttributes(attrs, R.styleable.MathView, 0, 0);
        init();
    }

    private void init()
    {
        mDrawingPaint = new Paint();
        mDrawingPaint.setTextAlign(Paint.Align.LEFT);
        String text = null;
        mBackgroundRect = new RectF();

        setClickable(true);
        setOnClickListener(this);

        if(mTypedAttributes != null)
        {
            text = mTypedAttributes.getString(R.styleable.MathView_text);
            mTextSize = mTypedAttributes.getDimensionPixelSize(R.styleable.MathView_textSize, (int) new TextView(getContext()).getTextSize());
            mMinTextSize = mTypedAttributes.getDimensionPixelSize(R.styleable.MathView_minTextSize, 0);
            mStrokeWidth = mTypedAttributes.getDimension(R.styleable.MathView_strokeWidth, mStrokeWidth);
            mTextColor = mTypedAttributes.getColorStateList(R.styleable.MathView_textColor);
            mFocusTextColor = mTypedAttributes.getColorStateList(R.styleable.MathView_focusTextColor);
            mFocusColor = mTypedAttributes.getColorStateList(R.styleable.MathView_focusColor);
            mReadOnly = mTypedAttributes.getBoolean(R.styleable.MathView_readOnly, true);
        }

        if(text == null)
            text = "";

        if(mTextColor == null)
            mTextColor = getResources().getColorStateList(R.color.black);
        else if(mTextColor.isStateful())
            throw new IllegalArgumentException("token color can not be stateful");

        if(mFocusTextColor == null)
            mFocusTextColor = getResources().getColorStateList(R.color.white);
        else if(mFocusTextColor.isStateful())
            throw new IllegalArgumentException("focus token color can not be stateful");

        if(mFocusColor == null)
            mFocusColor = getResources().getColorStateList(R.color.light_green_A700);
        else if(mFocusColor.isStateful())
            throw new IllegalArgumentException("focus color can not be stateful");

        if(mFont == null)
            mFont = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Light.ttf");

        parseText(text);
    }

    @Override
    public void onClick(View v)
    {
        MathView currentlySelectedView;

        currentlySelectedView = getTopMostMathView().findCaret();

        if(currentlySelectedView != null) {
            currentlySelectedView.mHasCaret = false;
            UIUtils.recursiveInvalidateChilds(currentlySelectedView);
        }
    }

    protected void parseText(String text)
    {
        if(text.isEmpty())
        {
            if(getChildCount() > 0)
                removeAllViews();

            return;
        }

        ArrayList<String> tokens;

        if(MathParser.isToplevelToken(text))
            tokens = MathParser.tokenizeForView(
                    text.substring(text.indexOf('(') + 1, text.length() - 1)); //isolate subtokens
        else if(!MathParser.isFurtherTokenizable(text))
            return;
        else
            tokens = MathParser.tokenizeForView(text);

        for(String token : tokens)
        {
            MathView v;

            if(MathParser.isToplevelToken(token))
            {
                if(MathParser.hasSubstringAt(token, "#pow(", 0)) {
                    v = new MathViewPow(getContext());
                }
                else if(MathParser.hasSubstringAt(token, "#integral(", 0)) {
                    v = new MathViewIntegral(getContext());
                }
                else if(MathParser.hasSubstringAt(token, "#sigma(", 0)) {
                    v = new MathViewSigma(getContext());
                }
                else if(MathParser.hasSubstringAt(token, "#log(", 0)) {
                    v = new MathViewLog(getContext());
                }
                else if(MathParser.hasSubstringAt(token, "#ln(", 0)) {
                    v = new MathViewLn(getContext());
                }
                else if(MathParser.hasSubstringAt(token, "#fract(", 0)) {
                    v = new MathViewFraction(getContext());
                }
                else if(MathParser.hasSubstringAt(token, "#binCoeff(", 0)) {
                    v = new MathViewBinCoeff(getContext());
                }
                else if(MathParser.hasSubstringAt(token, "#root(", 0)) {
                    v = new MathViewRoot(getContext());
                }
                else if(token.charAt(0) == '#') {
                    v = new MathViewFunc(getContext());
                }
                else {
                    throw new MathParser.ParseException("unkown toplevel token");
                }
            }
            else
            {
                if(MathParser.isFurtherTokenizable(token)) {
                    v = new MathView(getContext());
                }
                else if (token.charAt(0) == '(') {
                    v = new MathViewBraceOpen(getContext());
                }
                else if (token.charAt(0) == ')') {
                    v = new MathViewBraceClose(getContext());
                }
                else {
                    v = new MathViewText(getContext());
                }
            }

            v.setReadOnly(mReadOnly);
            v.setText(token);
            v.setTextColor(mTextColor);
            v.setFocusTextColor(mFocusTextColor);
            v.setFocusColor(mFocusColor);
            v.setTypeface(mFont);
            v.setTextSize(mTextSize);
            v.setStrokeWidth(mStrokeWidth);
            v.setMinTextSize(mMinTextSize);

            if(BuildFlags.SHOW_MATHVIEW_VIEW_BOUNDS)
                v.setBackground(getResources().getDrawable(R.drawable.view_bounds));

            addView(v, new MarginLayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        }
    }

    protected static int getAlignmentBaseline(@NonNull View v)
    {
        if(v instanceof  MathView)
            return ((MathView) v).mAlignmentBaseline;
        else
            return v.getMeasuredHeight() / 2;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int combinedWidth = 0;

        //these combined are the height of this view
        int heightAboveCenter = 0; //maximum height of child views above the vertical center of this viewgroup
        int heightBelowCenter = 0; //maximum height of child views below the vertical center of this viewgroup

        //measure all childs except for braces ( )
        for (int i = 0; i < getChildCount(); i++)
        {
            final View child = getChildAt(i);

            if (child instanceof MathViewBraceOpen || child instanceof MathViewBraceClose)
                continue;

            MarginLayoutParams lParams = (MarginLayoutParams) child.getLayoutParams();

            child.measure(
                    getChildMeasureSpec(widthMeasureSpec,
                            getPaddingLeft() + getPaddingRight(),
                            child.getLayoutParams().width),
                    getChildMeasureSpec(heightMeasureSpec,
                            getPaddingTop() + getPaddingBottom(),
                            child.getLayoutParams().height));

            combinedWidth += lParams.leftMargin + child.getMeasuredWidth() + lParams.rightMargin;

            heightAboveCenter = Math.max(
                    heightAboveCenter,
                    lParams.topMargin + getAlignmentBaseline(child));

            heightBelowCenter = Math.max(
                    heightBelowCenter,
                    lParams.bottomMargin + child.getMeasuredHeight() - getAlignmentBaseline(child));
        }

        //now measure all opening braces ( in reverse order
        for (int i = getChildCount()-1; i >= 0; i--)
        {
            //if not opening brace
            if ( !(getChildAt(i) instanceof MathViewBraceOpen) )
                continue;

            final MathView child = (MathView)getChildAt(i);

            child.measure(
                    getChildMeasureSpec(widthMeasureSpec,
                            getPaddingLeft() + getPaddingRight(),
                            child.getLayoutParams().width),
                    getChildMeasureSpec(heightMeasureSpec,
                            getPaddingTop() + getPaddingBottom(),
                            child.getLayoutParams().height));

            heightAboveCenter = Math.max(
                    heightAboveCenter,
                    getAlignmentBaseline(child));

            heightBelowCenter = Math.max(
                    heightBelowCenter,
                    child.getMeasuredHeight() - getAlignmentBaseline(child));

            combinedWidth += UIUtils.getMarginLeft(child) + child.getMeasuredWidth() + UIUtils.getMarginRight(child);
        }

        //now measure all closing braces in order
        for (int i = 0; i < getChildCount(); i++)
        {
            //if not closing brace
            if ( !(getChildAt(i) instanceof MathViewBraceClose) )
                continue;

            final MathView child = (MathView)getChildAt(i);

            child.measure(
                    getChildMeasureSpec(widthMeasureSpec,
                            getPaddingLeft() + getPaddingRight(),
                            child.getLayoutParams().width),
                    getChildMeasureSpec(heightMeasureSpec,
                            getPaddingTop() + getPaddingBottom(),
                            child.getLayoutParams().height));

            heightAboveCenter = Math.max(
                    heightAboveCenter,
                    getAlignmentBaseline(child));

            heightBelowCenter = Math.max(
                    heightBelowCenter,
                    child.getMeasuredHeight() - getAlignmentBaseline(child));

            combinedWidth += UIUtils.getMarginLeft(child) + child.getMeasuredWidth() + UIUtils.getMarginRight(child);
        }

        mAlignmentBaseline = heightAboveCenter + getPaddingTop();

        setMeasuredDimension(
                resolveSize(getPaddingLeft() + combinedWidth + getPaddingRight(), widthMeasureSpec),
                resolveSize(getPaddingTop() + heightAboveCenter + heightBelowCenter + getPaddingBottom(), heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        int maxHeightAboveCenter = 0;

        for (int i = 0; i < getChildCount(); i++) {
            maxHeightAboveCenter = Math.max(maxHeightAboveCenter, getAlignmentBaseline(getChildAt(i)));
        }

        for (int i = 0; i < getChildCount(); i++)
        {
            View child = getChildAt(i);

            int childLeft = (i == 0) ? getPaddingLeft() : getChildAt(i-1).getRight();

            childLeft += UIUtils.getMarginLeft(child)
                    + ( (i >= 1) ? UIUtils.getMarginRight(getChildAt(i-1)) : 0 );

            int childRight = childLeft + child.getMeasuredWidth();

            int childTop = maxHeightAboveCenter - getAlignmentBaseline(child) + getPaddingTop();

            int childBottom = childTop + child.getMeasuredHeight();

            child.layout(childLeft, childTop, childRight, childBottom);
        }
    }

    public void setText(@NonNull String expression)
    {
        parseText(expression);
    }

    public String getText()
    {
        String text = "";

        for (int i = 0; i < getChildCount(); i++) {
            text += ((MathView) getChildAt(i)).getText();
        }

        return text;
    }

    /**
     ** maximum size for the text to scale to
     * @param size in scaled pixels, see docs for TextView.setTextSize, -1.0f for unlimited
     */
    public void setTextSize(float size)
    {
        if(size == mTextSize)
            return;

        mTextSize = size;

        for(int i = 0; i < getChildCount(); i++)
        {
            View c = getChildAt(i);

            if(c instanceof MathView)
                ((MathView) c).setTextSize(size);
            else
                ((TextView) c).setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
        }
    }

    /**
     ** maximum size for the text to scale to
     * @param unit TypedValue enum, see docs for TextView.setTextSize
     * @param size text size in given unit, -1.0f for unlimited
     */
    public void setTextSize(int unit, float size)
    {
        setTextSize(TypedValue.applyDimension(unit, size, getResources().getDisplayMetrics()));
        requestLayout();
    }

    /**
     * @return -1.0f if unlimited
     */
    public float getTextSize()
    {
        return mTextSize;
    }

    /**
     ** maximum size for the text to scale to
     * @param size in scaled pixels, see docs for TextView.setTextSize, -1.0f for unlimited
     */
    public void setMinTextSize(float size)
    {
        if(size == mMinTextSize)
            return;

        mMinTextSize = size;

        for(int i = 0; i < getChildCount(); i++)
        {
            View c = getChildAt(i);

            if(c instanceof MathView)
                ((MathView) c).setMinTextSize(size);
        }
    }

    /**
     ** maximum size for the text to scale to
     * @param unit TypedValue enum, see docs for TextView.setTextSize
     * @param size text size in given unit, -1.0f for unlimited
     */
    public void setMinTextSize(int unit, float size)
    {
        setTextSize(TypedValue.applyDimension(unit, size, getResources().getDisplayMetrics()));
        requestLayout();
    }

    /**
     * @return -1.0f if unlimited
     */
    public float getMinTextSize()
    {
        return mMinTextSize;
    }

    public void setTypeface(@NonNull Typeface font)
    {
        if(font == mFont)
            return;

        mFont = font;

        for(int i = 0; i < getChildCount(); i++)
        {
            View v = getChildAt(i);

            if(v instanceof TextView) {
                ((TextView) v).setTypeface(font);
            }
            else {
                ((MathView) v).setTypeface(font);
            }
        }

        forceLayout();
    }

    public Typeface getTypeface()
    {
        return mFont;
    }

    public void setReadOnly(boolean readOnly)
    {
        mReadOnly = readOnly;
    }

    public boolean getReadOnly()
    {
        return mReadOnly;
    }

    /**
     * @param color stateless text color
     */
    public void setTextColor(@NonNull ColorStateList color)
    {
        if(color == mTextColor)
            return;

        if(color.isStateful())
            throw new IllegalArgumentException("text color can not be stateful");

        mTextColor = color;

        for(int i = 0; i < getChildCount(); i++)
        {
            View child = getChildAt(i);

            if(child instanceof TextView)
                ((TextView) child).setTextColor(color);

            if(child instanceof MathView)
                ((MathView) child).setTextColor(color);
        }

        invalidate();
    }

    public ColorStateList getTextColor()
    {
        return mTextColor;
    }

    public void setFocusTextColor(@NonNull ColorStateList color)
    {
        if(color == mFocusTextColor)
            return;

        mFocusTextColor = color;

        for(int i = 0; i < getChildCount(); i++)
        {
            View child = getChildAt(i);

            if(child instanceof MathView)
                ((MathView) child).setFocusTextColor(color);
        }

        invalidate();
    }

    public ColorStateList getFocusTextColor()
    {
        return mFocusTextColor;
    }

    public void setFocusColor(@NonNull ColorStateList color)
    {
        if(color == mFocusColor)
            return;

        mFocusColor = color;

        for(int i = 0; i < getChildCount(); i++)
        {
            View child = getChildAt(i);

            if(child instanceof MathView)
                ((MathView) child).setFocusColor(color);
        }

        invalidate();
    }

    public ColorStateList getFocusColor()
    {
        return mFocusColor;
    }

    public void setStrokeWidth(float width)
    {
        if(width != mStrokeWidth) {
            mStrokeWidth = width;
            invalidate();
        }
    }

    public float getStrokeWidth()
    {
        return mStrokeWidth;
    }

    public MathView findCaret()
    {
        if(mHasCaret)
            return this;

        MathView v;

        for (int i = 0; i < getChildCount() ; i++)
        {
             v = ((MathView) getChildAt(i)).findCaret();

            if(v != null)
                return v;
        }

        return null;
    }

    protected boolean siblingHasCaret()
    {
        for (int i = 0; i < ((ViewGroup) getParent()).getChildCount(); i++)
        {
            MathView v = (MathView) ((ViewGroup) getParent()).getChildAt(i);

            if (v.mHasCaret)
                return true;
        }

        return false;
    }

    protected boolean grandparentHasCaret()
    {
        if(!(getParent() instanceof MathView))
            return false;

        MathView grandParent = (MathView) getParent();

        MathView topMostGrandparent = getTopMostMathView();

        while(grandParent != topMostGrandparent)
        {
            if(grandParent.mHasCaret)
                return true;

            grandParent = (MathView) grandParent.getParent();
        }

        return false;
    }
    
    protected boolean childHasCaret()
    {
        for (int i = 0; i < getChildCount(); i++) {
            if(getChildAt(i) instanceof MathView)
                if(((MathView) getChildAt(i)).mHasCaret)
                    return true;
        }

        return false;
    }

    protected boolean grandchildHasCaret()
    {
        for (int i = 0; i < getChildCount(); i++) {
            if(getChildAt(i) instanceof MathView)
            {
                if(((MathView) getChildAt(i)).mHasCaret)
                    return true;
                else if(((MathView) getChildAt(i)).grandchildHasCaret())
                    return true;
            }
        }

        return false;
    }

    public MathView getTopMostMathView()
    {
        return (MathView) UIUtils.getGrandparent(this, new UIUtils.Condition<View>() {
            @Override
            public boolean test(View testable)
            {
                return !(testable.getParent() instanceof MathView);
            }
        });
    }
}
