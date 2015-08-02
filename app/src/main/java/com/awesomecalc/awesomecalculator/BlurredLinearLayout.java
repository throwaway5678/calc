package com.awesomecalc.awesomecalculator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * Created by Thilo on 16.02.2015.
 *
 * contents/childs of this layout are blurred when called setBlurred(true)
 */
public class BlurredLinearLayout extends LinearLayout {
    private Bitmap mCanvasBufferBmp;
    private boolean mBlurred;

    public BlurredLinearLayout(Context ctx)
    {
        super(ctx);
    }

    public BlurredLinearLayout(Context ctx, AttributeSet attrs)
    {
        super(ctx, attrs);
    }

    public BlurredLinearLayout(Context ctx, AttributeSet attrs, int defStyle)
    {
        super(ctx, attrs, defStyle);
    }

    public void setBlurred(boolean blur)
    {
        mBlurred = blur;
        invalidate();
    }

    @Override
    protected void dispatchDraw(@NonNull Canvas canvas)
    {
        if(mCanvasBufferBmp == null)
        {
            mCanvasBufferBmp = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
        }

        if(mBlurred && BuildFlags.BLUR && UserConfig.BLUR)
        {
            Canvas bufferCanvas = new Canvas(mCanvasBufferBmp);

            super.dispatchDraw(bufferCanvas);

            canvas.drawBitmap(UIUtils.blurBitmapDownscaled(mCanvasBufferBmp, getContext(), 3, 0.5f), 0.0f, 0.0f, null);
        }
        else {
            super.dispatchDraw(canvas);
        }
    }
}
