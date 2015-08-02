package com.awesomecalc.awesomecalculator;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.NoSuchElementException;


public class MathKeyboard extends LinearLayout {

    private final Context mCtx;
    private ViewPager mKeyboardPager;
    private Integer[] mPageResources;
    private IMathKeyboardListener mListener;
    private final int CURRENT_PAGE_INDICATOR_COLOR_NONSELECTED = 0xFF9FA8DA;
    private final int CURRENT_PAGE_INDICATOR_COLOR_SELECTED = 0xFF3F51B5;
    private TypedArray mAttributes;
    private ViewGroup mRootParent;

    public MathKeyboard(Context ctx)
    {
        super(ctx);
        mCtx = ctx;
        init();
    }

    public MathKeyboard(Context ctx, AttributeSet attrs)
    {
        super(ctx, attrs);
        mCtx = ctx;
        mAttributes = mCtx.obtainStyledAttributes(attrs, R.styleable.MathKeyboard, 0, 0);
        init();

    }

    public MathKeyboard(Context ctx, AttributeSet attrs, int defStyle)
    {
        super(ctx, attrs, defStyle);
        mCtx = ctx;
        mAttributes = mCtx.obtainStyledAttributes(attrs, R.styleable.MathKeyboard, 0, 0);
        init();
    }

    public void setBlurred(boolean blur)
    {
        if(blur)
        {
            findViewById(R.id.blur_overlay).setVisibility(VISIBLE);
        }
        else {
            findViewById(R.id.blur_overlay).setVisibility(INVISIBLE);
        }

        ((BlurredLinearLayout)findViewById(R.id.primary_buttons_container)).setBlurred(blur);
    }

    public ViewGroup getRootParent()
    {
        //init mRootParent on demand because parent can not be accessed in ctor
        if(mAttributes != null && mRootParent == null)
        {
            try {
                mRootParent = (ViewGroup) UIUtils.getGrandparent(this, new UIUtils.Condition<View>() {
                    @Override
                    public boolean test(View testable)
                    {
                        if(testable.getParent() == null ||
                                testable.getId() == mAttributes.getResourceId(R.styleable.MathKeyboard_rootParentView, 0))
                        {
                            return true;
                        }

                        return false;
                    }
                });
            }
            catch(NoSuchElementException e) {
                throw new AssertionError("MathKeyboard must have an attribute rootParentView declared");
            }
            finally {
                mAttributes.recycle();
            }
        }

        return mRootParent;
    }

    private void init()
    {
        LayoutInflater.from(mCtx).inflate(R.layout.view_keyboard, this);

        //init array with all pages of the keyboard
        mPageResources = new Integer[] {
                R.layout.keyboard_page_clipboard,
                R.layout.keyboard_page_constants,
                R.layout.keyboard_page_main,
                R.layout.keyboard_page_functions
        };

        mListener = new IMathKeyboardListener() {
            @Override
            public void onKeyInput(String input)
            {
                //nothing
            }

            @Override
            public void onSolvePressed()
            {
                //nothing
            }

            @Override
            public void onBackspacePressed()
            {
                //nothing
            }

            @Override
            public void onClearPressed()
            {
                //nothing
            }

            @Override
            public void onMoveCursorRightPressed()
            {
                //nothing
            }

            @Override
            public void onMoveCursorLeftPressed()
            {
                //nothing
            }
        };

        mKeyboardPager = (ViewPager)findViewById(R.id.math_keyboard_pager);
        mKeyboardPager.setAdapter(new CCustomPagerAdapter(mCtx, mPageResources));


        //set listener to update page indicators on the bottom of the view
       mKeyboardPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position)
            {
                LinearLayout container = (LinearLayout)findViewById(R.id.keyboard_current_page_indicators);

                //reset all icons to non-selected color
                for(int i=0; i < container.getChildCount(); i++)
                {
                    container.getChildAt(i).setBackgroundColor(CURRENT_PAGE_INDICATOR_COLOR_NONSELECTED);
                }

                //change current icon to selected color
                container.getChildAt(position).setBackgroundColor(CURRENT_PAGE_INDICATOR_COLOR_SELECTED);
            }
        });

        //set current page
        setPage(2);
    }

    public MathKeyboard setListener(IMathKeyboardListener listener)
    {
        mListener = listener;
        return this;
    }

    public IMathKeyboardListener getListener()
    {
        return mListener;
    }

    public ViewPager getUnderlyingViewPager()
    {
        return mKeyboardPager;
    }

    public MathKeyboard setPageSmoothely(Integer pageIndex)
    {
        mKeyboardPager.setCurrentItem(pageIndex, true); //true = transition smoothly to that page
        return this;
    }

    public MathKeyboard setPage(Integer pageIndex)
    {
        mKeyboardPager.setCurrentItem(pageIndex);
        return this;
    }

    private class CCustomPagerAdapter extends PagerAdapter {

        Context mContext;
        LayoutInflater mLayoutInflater;
        private Integer[] mPageResources;


        public CCustomPagerAdapter(Context context, Integer[] pageResources) {
            mContext = context;
            mPageResources = pageResources;
            mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return mPageResources.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((View)object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            int newItemLayoutId;

            if(position <= this.mPageResources.length-1)
            {
                newItemLayoutId = this.mPageResources[position];
            }
            else
            {
                //error?
                newItemLayoutId = this.mPageResources[0];
            }

            View newItem = mLayoutInflater.inflate(newItemLayoutId, container, false);

            container.addView(newItem);

            return newItem;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

    }

    public interface IMathKeyboardListener {

        public void onKeyInput(String input);

        public void onSolvePressed();

        public void onBackspacePressed();

        public void onClearPressed();

        public void onMoveCursorRightPressed();

        public void onMoveCursorLeftPressed();
    }
}
