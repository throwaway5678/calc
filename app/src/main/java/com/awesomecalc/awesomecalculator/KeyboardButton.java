package com.awesomecalc.awesomecalculator;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.NoSuchElementException;

/**
 * Created by Thilo on 26.01.2015.
 */
public class KeyboardButton extends RelativeLayout implements View.OnLongClickListener, View.OnClickListener {
    private LinearLayout mAlternativeButtonPopUp;
    private View mPrimaryButton;
    private TypedArray mTypedAttributes;
    private AttributeSet mAttrSet;
    private final int SECONDARY_BUTTONS_MARGIN_TO_KEYBOARD_BOUNDS = 5;
    private final int SECONDARY_BUTTONS_OFFSET_OVER_PRIMARY = 10;
    private boolean mAreAlternativesInitialized = false;
    private KeyboardButton mThisView = this;
    private String mKeyCode;
    private int mRowCount = 1;

    private enum ButtonType { Button, ImageButton, DetectAutomatically }

    private float calculateAltButtonX()
    {
        //cache values for performance
        int primaryButtonWidth = getWidth();
        int secondaryContainerWidth = mAlternativeButtonPopUp.getWidth();

        if(secondaryContainerWidth == primaryButtonWidth)
        {
            return getX();
        }
        else if(secondaryContainerWidth < primaryButtonWidth)
        {
            int offset = (primaryButtonWidth - secondaryContainerWidth) / 2;
            return getX() + offset;
        }
        else if(secondaryContainerWidth > primaryButtonWidth)
        {
            int offset = (secondaryContainerWidth - primaryButtonWidth) / 2;
            float distanceLeft = getX();
            float distanceRight = UIUtils.getScreenWidth(getContext()) - (getX() + getWidth());

            if(distanceLeft < distanceRight)
            {
                if(distanceLeft - offset < SECONDARY_BUTTONS_MARGIN_TO_KEYBOARD_BOUNDS)
                {
                    return SECONDARY_BUTTONS_MARGIN_TO_KEYBOARD_BOUNDS;
                }
                else
                {
                    return getX() - offset;
                }
            }
            else
            {
                if(distanceRight - offset < SECONDARY_BUTTONS_MARGIN_TO_KEYBOARD_BOUNDS)
                {
                    return UIUtils.getScreenWidth(getContext()) - SECONDARY_BUTTONS_MARGIN_TO_KEYBOARD_BOUNDS - mAlternativeButtonPopUp.getWidth();
                }
                else
                {
                    return getX() + getWidth() + offset - mAlternativeButtonPopUp.getWidth();
                }
            }
        }
        else
        {
            return 0.0f;
        }
    }

    private float calculateAltButtonY()
    {
        return UIUtils.getYRelativeTo(mThisView, getMathKeyboardInstance()) - mAlternativeButtonPopUp.getHeight() - SECONDARY_BUTTONS_OFFSET_OVER_PRIMARY;
    }

    private void hideAlternativeButtons()
    {
        getMathKeyboardInstance().getRootParent().setLayerType(LAYER_TYPE_HARDWARE, null);

        mAlternativeButtonPopUp.animate()
                .scaleX(0.0f)
                .scaleY(0.0f)
                .y(mAlternativeButtonPopUp.getY() + mAlternativeButtonPopUp.getHeight() / 2)
                .setDuration(150)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(new Runnable() {
                    @Override
                    public void run()
                    {
                        mAlternativeButtonPopUp.setVisibility(INVISIBLE);
                        getMathKeyboardInstance().setBlurred(false);
                        getMathKeyboardInstance().getRootParent().setLayerType(LAYER_TYPE_NONE, null);
                    }
                });
    }

    private void showAlternativeButtons()
    {
        if(!mAreAlternativesInitialized)
        {
            ((RelativeLayout) getMathKeyboardInstance()
                    .findViewById(R.id.alt_buttons_container))
                    .addView(mAlternativeButtonPopUp, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            mAreAlternativesInitialized = true;
        }

        getMathKeyboardInstance().setBlurred(true);

        //do this onPreDraw because getHeight() has to be called after measure()
        mAlternativeButtonPopUp.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw()
            {
                mAlternativeButtonPopUp.setY(calculateAltButtonY() + mAlternativeButtonPopUp.getHeight() / 2);
                mAlternativeButtonPopUp.setX(calculateAltButtonX());
                mAlternativeButtonPopUp.setScaleX(0.0f);
                mAlternativeButtonPopUp.setScaleY(0.0f);

                getMathKeyboardInstance().getRootParent().setLayerType(LAYER_TYPE_HARDWARE, null);

                mAlternativeButtonPopUp.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .y(calculateAltButtonY())
                        .setDuration(150)
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .withEndAction(new Runnable() {
                            @Override
                            public void run()
                            {
                                getMathKeyboardInstance().getRootParent().setLayerType(LAYER_TYPE_NONE, null);
                            }
                        });

                mAlternativeButtonPopUp.getViewTreeObserver().removeOnPreDrawListener(this);
                return true;
            }
        });

        mAlternativeButtonPopUp.setVisibility(VISIBLE);
    }

    public KeyboardButton(Context ctx)
    {
        super(ctx);
        init();
    }

    public KeyboardButton(Context ctx, AttributeSet attrs)
    {
        super(ctx, attrs);
        mAttrSet = attrs;
        mTypedAttributes = getContext().obtainStyledAttributes(attrs, R.styleable.KeyboardButton, 0, 0);
        init();
    }

    public KeyboardButton(Context ctx, AttributeSet attrs, int defStyle)
    {
        super(ctx, attrs, defStyle);
        mAttrSet = attrs;
        mTypedAttributes = getContext().obtainStyledAttributes(attrs, R.styleable.KeyboardButton, 0, 0);
        init();
    }

    @Override
    public void setPressed(boolean pressed)
    {
        mPrimaryButton.setPressed(pressed);
    }

    @Override
    public boolean isPressed()
    {
        return mPrimaryButton.isPressed();
    }

    private MathKeyboard getMathKeyboardInstance()
    {
        MathKeyboard keyboardInstance;

        try
        {
            keyboardInstance = (MathKeyboard) UIUtils.getGrandparent(this, new UIUtils.Condition<View>() {
                @Override
                public boolean test(View testable)
                {
                    return testable instanceof MathKeyboard;
                }
            });
        }
        catch(NoSuchElementException e)
        {
            throw new AssertionError("KeyboardButton must have a grandparent of type MathKeyboard");
        }

        return keyboardInstance;
    }

    private View createPrimaryButton(ButtonType buttonType)
    {
        if(mTypedAttributes != null)
        {
            View tempButton;

            if(UIUtils.isLayoutWidthWrapContent(this, mAttrSet) || UIUtils.isLayoutHeightWrapContent(this, mAttrSet))
            {
                throw new AssertionError("wrap_content is not allowed for KeyboardButton");
            }

            int buttonMarginAll = mTypedAttributes.getDimensionPixelSize(R.styleable.KeyboardButton_buttonMargin, 0);
            int buttonMarginTop = mTypedAttributes.getDimensionPixelSize(R.styleable.KeyboardButton_buttonMarginTop, 0);
            int buttonMarginBottom = mTypedAttributes.getDimensionPixelSize(R.styleable.KeyboardButton_buttonMarginBottom, 0);
            int buttonMarginLeft = mTypedAttributes.getDimensionPixelSize(R.styleable.KeyboardButton_buttonMarginLeft, 0);
            int buttonMarginRight = mTypedAttributes.getDimensionPixelSize(R.styleable.KeyboardButton_buttonMarginRight, 0);

            int buttonPaddingAll = mTypedAttributes.getDimensionPixelSize(R.styleable.KeyboardButton_buttonPadding, 0);
            int buttonPaddingTop = mTypedAttributes.getDimensionPixelSize(R.styleable.KeyboardButton_buttonPaddingTop, 0);
            int buttonPaddingBottom = mTypedAttributes.getDimensionPixelSize(R.styleable.KeyboardButton_buttonPaddingBottom, 0);
            int buttonPaddingLeft = mTypedAttributes.getDimensionPixelSize(R.styleable.KeyboardButton_buttonPaddingLeft, 0);
            int buttonPaddingRight = mTypedAttributes.getDimensionPixelSize(R.styleable.KeyboardButton_buttonPaddingRight, 0);

            Drawable buttonImage = mTypedAttributes.getDrawable(R.styleable.KeyboardButton_buttonImage);
            String buttonText = mTypedAttributes.getString(R.styleable.KeyboardButton_buttonText);

            if(buttonType == ButtonType.DetectAutomatically && buttonImage != null && buttonText != null)
            {
                throw new AssertionError("KeyboardButton can not have attribues buttonImage and buttonText at the same time");
            }
            else if(buttonType == ButtonType.ImageButton
                    || (buttonType == ButtonType.DetectAutomatically && buttonImage != null))
            {
                tempButton = new ImageButton(getContext());

                addView(tempButton, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

                ((ImageButton) tempButton).setImageDrawable(buttonImage);
                ((ImageButton) tempButton).setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            }
            else
            {
                tempButton = new Button(getContext());

                addView(tempButton, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

                float buttonTextSize = mTypedAttributes.getDimension(R.styleable.KeyboardButton_buttonTextSize, -1.0f);
                ColorStateList buttonTextColor = mTypedAttributes.getColorStateList(R.styleable.KeyboardButton_buttonTextColor);
                boolean buttonSingleLine = mTypedAttributes.getBoolean(R.styleable.KeyboardButton_buttonSingleLine, true);
                String buttonFontAssetLocation = mTypedAttributes.getString(R.styleable.KeyboardButton_buttonFont);

                ((Button) tempButton).setText(buttonText);
                ((Button) tempButton).setSingleLine(buttonSingleLine);

                if(buttonTextSize > 0)
                {
                    ((Button) tempButton).setTextSize(buttonTextSize);
                }

                if(buttonTextColor != null)
                {
                    ((Button) tempButton).setTextColor(buttonTextColor);
                }

                if(buttonFontAssetLocation != null)
                {
                    Typeface font = Typeface.createFromAsset(getContext().getAssets(), buttonFontAssetLocation);
                    ((Button)tempButton).setTypeface(font);
                }
                else
                {
                    Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Light.ttf");
                    ((Button)tempButton).setTypeface(font);
                }



            }


            Drawable buttonBackground = mTypedAttributes.getDrawable(R.styleable.KeyboardButton_buttonBackground);
            float buttonAlpha = mTypedAttributes.getFloat(R.styleable.KeyboardButton_alpha, 1.0f);

            if(buttonBackground != null)
            {
                //ATTENTION! background has to be set before padding!
                tempButton.setBackground(buttonBackground);
            }

            RelativeLayout.LayoutParams buttonLayoutParams = (RelativeLayout.LayoutParams) tempButton.getLayoutParams();

            buttonLayoutParams.setMargins(
                    buttonMarginLeft != 0 ? buttonMarginLeft : buttonMarginAll,
                    buttonMarginTop != 0 ? buttonMarginTop : buttonMarginAll,
                    buttonMarginRight != 0 ? buttonMarginRight : buttonMarginAll,
                    buttonMarginBottom != 0 ? buttonMarginBottom : buttonMarginAll);

            tempButton.setLayoutParams(buttonLayoutParams);

            tempButton.setPadding(
                    buttonPaddingLeft != 0 ? buttonPaddingLeft : buttonPaddingAll,
                    buttonPaddingTop != 0 ? buttonPaddingTop : buttonPaddingAll,
                    buttonPaddingRight != 0 ? buttonPaddingRight : buttonPaddingAll,
                    buttonPaddingBottom != 0 ? buttonPaddingBottom : buttonPaddingAll);

            tempButton.setAlpha(buttonAlpha);
            return tempButton;
        }
        else
        {
            throw new RuntimeException("KeyboardButton.mTypedAttributes is null");
        }
    }

    public void setButtonImage(Drawable img)
    {
        if(!(mPrimaryButton instanceof ImageButton))
        {
            mPrimaryButton = createPrimaryButton(ButtonType.ImageButton);
        }

        ((ImageButton) mPrimaryButton).setImageDrawable(img);
    }

    public void setButtonText(String text)
    {
        if(!(mPrimaryButton instanceof Button))
        {
            mPrimaryButton = createPrimaryButton(ButtonType.Button);
        }

        ((Button) mPrimaryButton).setText(text);
    }

    private void init()
    {
        if(mTypedAttributes != null)
        {
            mKeyCode = mTypedAttributes.getString(R.styleable.KeyboardButton_keyCode);
            mRowCount = mTypedAttributes.getInt(R.styleable.KeyboardButton_rowCount, 1);
        }

        if(mRowCount < 1)
        {
            throw new AssertionError("KeyboardButton rowCount attribute can not be smaller than 1");
        }

        mPrimaryButton = createPrimaryButton(ButtonType.DetectAutomatically);

        mPrimaryButton.setOnLongClickListener(this);
        mPrimaryButton.setOnClickListener(this);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev)
    {
        if(mAlternativeButtonPopUp != null)
        {
            switch(ev.getAction())
            {
                case MotionEvent.ACTION_MOVE:

                    Rect primaryButtonArea = UIUtils.getViewBounds(mPrimaryButton);

                    //if movement is not in bounds of the primary button
                    if(!primaryButtonArea.contains((int) ev.getRawX(), (int) ev.getRawY()))
                    {
                        return true;
                    }

                    break;

                case MotionEvent.ACTION_UP:

                    mAlternativeButtonPopUp.invalidate();
                    hideAlternativeButtons();
                    break;
            }

        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent ev)
    {
        if(mAlternativeButtonPopUp != null)
        {
            switch(ev.getAction())
            {
                case MotionEvent.ACTION_MOVE:

                    for(int i=0; i < mAlternativeButtonPopUp.getChildCount(); i++)
                    {
                        for(int j=0; j < ((LinearLayout) mAlternativeButtonPopUp.getChildAt(i)).getChildCount(); j++)
                        {
                            Rect secondaryButtonArea = UIUtils.getViewBounds(((LinearLayout) mAlternativeButtonPopUp.getChildAt(i)).getChildAt(j));

                            if(secondaryButtonArea.contains((int) ev.getRawX(), (int) ev.getRawY()))
                            {
                                getMathKeyboardInstance().getRootParent().invalidate();
                                ((LinearLayout) mAlternativeButtonPopUp.getChildAt(i)).getChildAt(j).setPressed(true);
                            }
                            else
                            {
                                ((LinearLayout) mAlternativeButtonPopUp.getChildAt(i)).getChildAt(j).setPressed(false);
                            }
                        }
                    }

                    return true;

                case MotionEvent.ACTION_UP:

                    getParent().requestDisallowInterceptTouchEvent(false);

                    for(int i=0; i < mAlternativeButtonPopUp.getChildCount(); i++)
                    {
                        for(int j=0; j < ((LinearLayout) mAlternativeButtonPopUp.getChildAt(i)).getChildCount(); j++)
                        {
                            Rect secondaryButtonArea = UIUtils.getViewBounds(((LinearLayout) mAlternativeButtonPopUp.getChildAt(i)).getChildAt(j));

                            if(secondaryButtonArea.contains((int) ev.getRawX(), (int) ev.getRawY()))
                            {
                                ((LinearLayout) mAlternativeButtonPopUp.getChildAt(i)).getChildAt(j).performClick();
                            }

                            ((LinearLayout) mAlternativeButtonPopUp.getChildAt(i)).getChildAt(j).setPressed(false);
                        }
                    }

                    mAlternativeButtonPopUp.invalidate();
                    hideAlternativeButtons();
                    return true;
            }
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public void onClick(View clickedView)
    {
        if(mKeyCode != null && getMathKeyboardInstance().getListener() != null)
        {
            if(mKeyCode.matches("solve"))
            {
                getMathKeyboardInstance().getListener().onSolvePressed();
            }
            else if(mKeyCode.matches("backspace"))
            {
                getMathKeyboardInstance().getListener().onBackspacePressed();
            }
            else if(mKeyCode.matches("clear"))
            {
                getMathKeyboardInstance().getListener().onClearPressed();
            }
            else if(mKeyCode.matches("moveCursorLeft"))
            {
                getMathKeyboardInstance().getListener().onMoveCursorLeftPressed();
            }
            else if(mKeyCode.matches("moveCursorRight"))
            {
                getMathKeyboardInstance().getListener().onMoveCursorRightPressed();
            }
            else
            {
                getMathKeyboardInstance().getListener().onKeyInput(mKeyCode);
            }
        }
    }

    @Override
    public boolean performClick()
    {
        mPrimaryButton.performClick();
        return true;
    }

    @Override
    public boolean onLongClick(View clickedView)
    {
        if(mAlternativeButtonPopUp != null)
        {
            getParent().requestDisallowInterceptTouchEvent(true);
            showAlternativeButtons();
        }
        return true;
    }

    @Override
    public void onFinishInflate()
    {
        if(getChildCount() > 1)
        {
            mAlternativeButtonPopUp = new LinearLayout(getContext());
            //set shadow background drawable
            mAlternativeButtonPopUp.setBackground(getResources().getDrawable(android.R.drawable.dialog_holo_dark_frame));
            mAlternativeButtonPopUp.setVisibility(INVISIBLE);
            mAlternativeButtonPopUp.setOrientation(LinearLayout.VERTICAL);
            int originChildCount = getChildCount()-1;

            if(mRowCount > originChildCount)
            {
                mRowCount = originChildCount;
            }

            if(mRowCount == 1)
            {
                mAlternativeButtonPopUp.addView(new LinearLayout(getContext()), new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                while(getChildCount() > 1)
                {
                    View tempView = getChildAt(1);

                    if(!(tempView instanceof KeyboardButton) && BuildConfig.DEBUG) {
                        throw new AssertionError("childs of KeyboardButton must be of type KeyboardButton");
                    }

                    removeViewAt(1);
                    ((LinearLayout) mAlternativeButtonPopUp.getChildAt(0)).addView(tempView);
                }
            }
            else if(originChildCount % mRowCount == 0 ) //if divisible
            {
                for(int i=0; i < mRowCount; i++)
                {
                    mAlternativeButtonPopUp.addView(new LinearLayout(getContext()), new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                    for(int j=0; j < originChildCount / mRowCount; j++)
                    {
                        View tempView = getChildAt(1);

                        if(!(tempView instanceof KeyboardButton) && BuildConfig.DEBUG)
                        {
                            throw new AssertionError("childs of KeyboardButton must be of type KeyboardButton");
                        }

                        removeViewAt(1);
                        ((LinearLayout) mAlternativeButtonPopUp.getChildAt(i)).addView(tempView);
                    }
                }
            }
            else //not divisble
            {
                mAlternativeButtonPopUp.addView(new LinearLayout(getContext()), new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                for(int j=0; j < originChildCount % mRowCount + originChildCount / mRowCount; j++)
                {
                    View tempView = getChildAt(1);

                    if(!(tempView instanceof KeyboardButton) && BuildConfig.DEBUG)
                    {
                        throw new AssertionError("childs of KeyboardButton must be of type KeyboardButton");
                    }

                    removeViewAt(1);
                    ((LinearLayout) mAlternativeButtonPopUp.getChildAt(0)).addView(tempView);
                }

                for(int i=1; i < mRowCount; i++)
                {
                    mAlternativeButtonPopUp.addView(new LinearLayout(getContext()), new LinearLayout.LayoutParams(
                            UIUtils.getCombinedChildrenWidth((ViewGroup)(mAlternativeButtonPopUp).getChildAt(0)), ViewGroup.LayoutParams.WRAP_CONTENT));

                    for(int j=0; j < originChildCount / mRowCount; j++)
                    {
                        View tempView = getChildAt(1);

                        if(!(tempView instanceof KeyboardButton) && BuildConfig.DEBUG)
                        {
                            throw new AssertionError("childs of KeyboardButton must be of type KeyboardButton");
                        }

                        removeViewAt(1);
                        ((LinearLayout) mAlternativeButtonPopUp.getChildAt(i)).addView(tempView, new LinearLayout.LayoutParams(0, tempView.getLayoutParams().height, 1.0f));
                    }
                }
            }
        }
    }
}


