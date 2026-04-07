package com.example.wusthelper.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.example.wusthelper.R;

public class MyArrowRectangleView extends ViewGroup {
    private int mArrowWidth;
    private int mArrowHeight;
    private int mRadius;
    private int mBackgroundColor;
    private int mArrowOffset;
    private int mShadowColor;
    private int mShadowThickness;

    public MyArrowRectangleView(Context context) {
        this(context, null);
    }

    public MyArrowRectangleView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyArrowRectangleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.MyArrowRectangleView, defStyleAttr, 0);
        for (int i = 0; i < a.getIndexCount(); i++) {
            int attr = a.getIndex(i);
            if (attr == R.styleable.MyArrowRectangleView_arrow_width) {
                mArrowWidth = a.getDimensionPixelSize(attr, mArrowWidth);
            } else if (attr == R.styleable.MyArrowRectangleView_arrow_height) {
                mArrowHeight = a.getDimensionPixelSize(attr, mArrowHeight);
            } else if (attr == R.styleable.MyArrowRectangleView_radius) {
                mRadius = a.getDimensionPixelSize(attr, mRadius);
            } else if (attr == R.styleable.MyArrowRectangleView_background_color) {
                mBackgroundColor = a.getColor(attr, mBackgroundColor);
            } else if (attr == R.styleable.MyArrowRectangleView_arrow_offset) {
                mArrowOffset = a.getDimensionPixelSize(attr, mArrowOffset);
            } else if (attr == R.styleable.MyArrowRectangleView_shadow_color) {
                mShadowColor = a.getColor(attr, mShadowColor);
            } else if (attr == R.styleable.MyArrowRectangleView_shadow_thickness) {
                mShadowThickness = a.getDimensionPixelSize(attr, mShadowThickness);
            }
        }
        a.recycle();

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();
        int maxWidth = 0;
// reserve space for the arrow and round corners
        int maxHeight = mArrowHeight + mRadius;
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            if (child.getVisibility() != GONE) {
                measureChild(child, widthMeasureSpec, heightMeasureSpec);
                maxWidth = Math.max(maxWidth, child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);
                maxHeight = maxHeight + child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
            }
        }

        maxWidth = maxWidth + getPaddingLeft() + getPaddingRight() + mShadowThickness;
        maxHeight = maxHeight + getPaddingTop() + getPaddingBottom() + mShadowThickness;

        setMeasuredDimension(maxWidth, maxHeight);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();
        int topOffset = t + mArrowHeight + mRadius / 2;
        int top = 0;
        int bottom = 0;
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            top = topOffset + i * child.getMeasuredHeight();
            bottom = top + child.getMeasuredHeight();
            child.layout(l, top, r - mRadius / 2 - mShadowThickness, bottom);
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        // disable h/w acceleration for blur mask filter
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(mBackgroundColor);
        paint.setStyle(Paint.Style.FILL);

// set Xfermode for source and shadow overlap
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));

// draw round corner rectangle
        paint.setColor(mBackgroundColor);
        canvas.drawRoundRect(new RectF(0, mArrowHeight, getMeasuredWidth() - mShadowThickness, getMeasuredHeight() - mShadowThickness), mRadius, mRadius, paint);

// draw arrow
        Path path = new Path();
        int startPoint = getMeasuredWidth() - mArrowOffset;
        path.moveTo(startPoint, mArrowHeight);
        path.lineTo(startPoint + mArrowWidth, mArrowHeight);
        path.lineTo(startPoint + mArrowWidth / 2, 0);
        path.close();
        canvas.drawPath(path, paint);

// draw shadow
        if (mShadowThickness > 0) {
            paint.setMaskFilter(new BlurMaskFilter(mShadowThickness, BlurMaskFilter.Blur.OUTER));
            paint.setColor(mShadowColor);
            canvas.drawRoundRect(new RectF(mShadowThickness, mArrowHeight + mShadowThickness, getMeasuredWidth() - mShadowThickness, getMeasuredHeight() - mShadowThickness), mRadius, mRadius, paint);
        }

        super.dispatchDraw(canvas);
    }
}