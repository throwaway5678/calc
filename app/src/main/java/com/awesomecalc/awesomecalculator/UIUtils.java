package com.awesomecalc.awesomecalculator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.util.NoSuchElementException;

/**
 * Created by Thilo on 31.01.2015.
 */
public class UIUtils {

    public static boolean isLayoutWidthWrapContent(@NonNull View v, @NonNull AttributeSet attrs)
    {
        if(v.getLayoutParams() != null)
        {
            return v.getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT;
        }
        else
        {
            int value = attrs.getAttributeIntValue("http://schemas.android.com/apk/res/android", "layout_width", -3);

            if(value == -3)
            {
                return false;
            }
            else
            {
                return value == ViewGroup.LayoutParams.WRAP_CONTENT;
            }
        }
    }

    public static boolean isLayoutHeightWrapContent(@NonNull View v, @NonNull AttributeSet attrs)
    {
        if(v.getLayoutParams() != null)
        {
            return v.getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT;
        }
        else
        {
            int value = attrs.getAttributeIntValue("http://schemas.android.com/apk/res/android", "layout_height", -3);

            if(value == -3)
            {
                return false;
            }
            else
            {
                return value == ViewGroup.LayoutParams.WRAP_CONTENT;
            }
        }
    }

    public static int getCombinedChildrenWidth(@NonNull ViewGroup v)
    {
        int size = 0;

        for(int i = 0; i < v.getChildCount(); i++)
        {
            if(v.getChildAt(i).getLayoutParams().width == ViewGroup.LayoutParams.MATCH_PARENT)
            {
                return ViewGroup.LayoutParams.MATCH_PARENT;
            }
            else if(v.getChildAt(i).getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT)
            {
                throw new AssertionError("could not get layout width: one ore more childs of the View have the attribute WRAP_CONTENT");
            }
            else
            {
                size += v.getChildAt(i).getLayoutParams().width;
            }
        }

        return size;
    }

    public static int getCombinedChildrenHeight(@NonNull ViewGroup v)
    {
        int size = 0;

        for(int i = 0; i < v.getChildCount(); i++)
        {
            if(v.getChildAt(i).getLayoutParams().height == ViewGroup.LayoutParams.MATCH_PARENT)
            {
                return ViewGroup.LayoutParams.MATCH_PARENT;
            }
            else if(v.getChildAt(i).getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT)
            {
                throw new AssertionError("could not get layout height: one ore more childs of the View have the attribute WRAP_CONTENT");
            }
            else
            {
                size += v.getChildAt(i).getLayoutParams().height;
            }
        }

        return size;
    }

    public static Bitmap blurBitmap(@NonNull Bitmap inputBmp, @NonNull Context ctx, int radius)
    {
        return blurBitmapDownscaled(inputBmp, ctx, radius, 1);
    }

    public static Bitmap blurBitmapDownscaled(@NonNull Bitmap inputBmp, @NonNull Context ctx, int radius, float scale)
    {
        RenderScript rs = RenderScript.create(ctx);

        if(scale > 1 || scale <= 0)
        {
            throw new AssertionError("blurBitmapDownscaled: scale parameter must be <= 1 and > 0. Scale is: " + scale);
        }

        //downscale bitmap to make blurring faster
        Bitmap bufferBmp = Bitmap.createScaledBitmap(inputBmp, Math.round(inputBmp.getWidth()*scale), Math.round(inputBmp.getHeight()*scale), false);

        //render
        final Allocation input = Allocation.createFromBitmap(rs, bufferBmp);
        final Allocation output = Allocation.createTyped(rs, input.getType());
        final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        script.setRadius(radius);
        script.setInput(input);
        script.forEach(output);
        output.copyTo(bufferBmp);

        //upscale bitmap to original size
        return Bitmap.createScaledBitmap(bufferBmp, inputBmp.getWidth(), inputBmp.getHeight(), false);
    }

    public static Integer getXRelativeTo(@NonNull View subjectView, @NonNull View referenceView)
    {
        int[] subjectLocation = new int[2];
        int[] referenceLocation = new int[2];

        subjectView.getLocationInWindow(subjectLocation);
        referenceView.getLocationInWindow(referenceLocation);

        return subjectLocation[0] - referenceLocation[0];
    }

    public static Integer getYRelativeTo(@NonNull View subjectView, @NonNull View referenceView)
    {
        int[] subjectLocation = new int[2];
        int[] referenceLocation = new int[2];

        subjectView.getLocationInWindow(subjectLocation);
        referenceView.getLocationInWindow(referenceLocation);

        return subjectLocation[1] - referenceLocation[1];
    }

    public static Point getScreenSize(@NonNull Context ctx)
    {
        WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    public static Integer getScreenWidth(@NonNull Context ctx)
    {
       return getScreenSize(ctx).x;
    }

    public static Integer getScreenHeight(@NonNull Context ctx)
    {
        return getScreenSize(ctx).y;
    }

    public static Rect getViewBounds(@NonNull View v)
    {
        int[] location = new int[2];
        v.getLocationOnScreen(location);
        return new Rect(location[0], location[1], location[0] + v.getWidth(), location[1] + v.getHeight());
    }

    public static interface Condition<T> {
        public boolean test(T testable);
    }

    public static View getGrandparent(@NonNull View child, @NonNull Condition<View> condition) throws NoSuchElementException
    {
        View parent;

        if(condition.test(child))
            return child;

        try {
            parent = (View) child.getParent();
            parent.getClass();
        }
        catch(NullPointerException e)
        {
            throw new NoSuchElementException("No grandparent that matches the condition could be found");
        }

        if(condition.test(parent) ) {
            return (View)child.getParent();
        }
        else {
            return getGrandparent(parent, condition);
        }
    }

    public static void setMargins(@NonNull View v, int left, int top, int right, int bottom)
    {
        ((ViewGroup.MarginLayoutParams) v.getLayoutParams()).setMargins(left, top, right, bottom);
    }

    public static Rect getMargins(@NonNull View v)
    {
        Rect r = new Rect();

        r.left = getMarginLeft(v);
        r.top = getMarginTop(v);
        r.right = getMarginRight(v);
        r.bottom = getMarginBottom(v);

        return r;
    }

    public static void setMarginLeft(@NonNull View v, int left)
    {
        ((ViewGroup.MarginLayoutParams) v.getLayoutParams()).leftMargin = left;
    }

    public static void setMarginTop(@NonNull View v, int top)
    {
        ((ViewGroup.MarginLayoutParams) v.getLayoutParams()).topMargin = top;
    }

    public static void setMarginRight(@NonNull View v, int right)
    {
        ((ViewGroup.MarginLayoutParams) v.getLayoutParams()).rightMargin = right;
    }

    public static void setMarginBottom(@NonNull View v, int bottom)
    {
        ((ViewGroup.MarginLayoutParams) v.getLayoutParams()).bottomMargin = bottom;
    }

    public static int getMarginLeft(@NonNull View v)
    {
        return ((ViewGroup.MarginLayoutParams) v.getLayoutParams()).leftMargin;
    }

    public static int getMarginTop(@NonNull View v)
    {
        return ((ViewGroup.MarginLayoutParams) v.getLayoutParams()).topMargin;
    }

    public static int getMarginRight(@NonNull View v)
    {
        return ((ViewGroup.MarginLayoutParams) v.getLayoutParams()).rightMargin;
    }

    public static int getMarginBottom(@NonNull View v)
    {
        return ((ViewGroup.MarginLayoutParams) v.getLayoutParams()).bottomMargin;
    }

    public static void addPadding(@NonNull View v, int left, int top, int right, int bottom) {
        v.setPadding(
                v.getPaddingLeft() + left,
                v.getPaddingTop() + top,
                v.getPaddingRight() + right,
                v.getPaddingBottom() + bottom);
    }

    public static void addPaddingLeft(@NonNull View v, int left) {
        addPadding(v, left, 0, 0, 0);
    }

    public static void addPaddingTop(@NonNull View v, int top) {
        addPadding(v, 0, top, 0, 0);
    }

    public static void addPaddingRight(@NonNull View v, int right) {
        addPadding(v, 0, 0, right, 0);
    }

    public static void addPaddingBottom(@NonNull View v, int bottom) {
        addPadding(v, 0, 0, 0, bottom);
    }

    public static void invalidateChilds(@NonNull View parent)
    {
        if(!(parent instanceof ViewGroup))
            throw new IllegalArgumentException("parent parameter must be of type ViewGroup");

        for (int i = 0; i < ((ViewGroup) parent).getChildCount(); i++)
        {
            ((ViewGroup) parent).getChildAt(i).invalidate();
        }
    }

    public static void recursiveInvalidateChilds(@NonNull View parent)
    {
        parent.invalidate();

        if(parent instanceof ViewGroup)
            for (int i = 0; i < ((ViewGroup) parent).getChildCount(); i++) {
                recursiveInvalidateChilds(((ViewGroup) parent).getChildAt(i));
            }
    }
}

