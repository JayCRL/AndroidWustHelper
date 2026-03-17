package com.example.wusthelper.helper;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import com.example.wusthelper.R;

public class AnimationHelper extends FrameLayout {
    private static final int LEFT=0x1;
    private static final int RIGHT=0x2;
    private static final int TOP=0x4;
    private static final int BOTTOM=0x8;
    private static final String TAG = "IconView";
    public AnimationHelper(Context context) {
        this(context,null);
    }
    private boolean aniAble;
    private boolean isOriginalLocation;
    private boolean init;
    private int startPosition;
    private int x,y;
    private int dx,dy;
    private boolean finishedX,finishedY;
    private FinishAnimation finishAnimation;
    private Interpolator interpolator;
    private boolean alpha;
    public AnimationHelper(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public AnimationHelper(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array=context.obtainStyledAttributes(attrs, R.styleable.AnimationHelper);
        startPosition=array.getInt(R.styleable.AnimationHelper_startPosition,0);
        x=y=0;
        aniAble=array.getBoolean(R.styleable.AnimationHelper_animationAble,false);
        isOriginalLocation=true;
        interpolator=new DecelerateInterpolator();
        init=false;
        alpha=true;
    }

    public void setStartPosition(int startPosition) {
        this.startPosition = startPosition;
    }

    public void setAniAble(boolean aniAble) {
        this.aniAble = aniAble;
    }
    public interface FinishAnimation
    {
        void finishAnimation();
    }
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        View view=getChildAt(0);
        if (!init)
        {
            x=(int) view.getX();
            y=(int)view.getY();
            init=true;
        }

    }

    public void performTranslate(long du)
    {

        calculationDistance();
        final FinishAnimation finishAnimation=new FinishAnimation() {
            @Override
            public void finishAnimation() {
                isOriginalLocation=false;
                if (AnimationHelper.this.finishAnimation!=null)
                    AnimationHelper.this.finishAnimation.finishAnimation();
            }
        };
        if (dx!=0)performTranslateAnimate(dx,false,true,du,finishAnimation);
        else finishedX=true;
        if (dy!=0)performTranslateAnimate(dy,false,false,du,finishAnimation);
        else finishedY=true;
    }
    private void calculationDistance()
    {
        View view=getChildAt(0);
        switch (startPosition)
        {
            case LEFT:
                dx=-x-view.getWidth()/5;
                dy=0;
                break;
            case RIGHT:
                dx=getWidth()/5-x;
                dy=0;
                break;
            case TOP:
                dx=0;
                dy=-y-view.getHeight();
                break;
            case BOTTOM:
                dx=0;
                dy=getHeight()-y;
                break;
            case LEFT|TOP:
                dx=-(x+view.getWidth()/5);
                dy=-(y+view.getHeight());
                break;
            case LEFT|BOTTOM:
                dx=-(x+view.getWidth()/5);
                dy=-(y-getHeight());
                break;
            case RIGHT|TOP:
                dx=-(x-getWidth()/5);
                dy=-y-view.getHeight();
                break;
            case RIGHT|BOTTOM:
                dx=getWidth()/5-x;
                dy=getHeight()-y;
                break;
            default:
                dx=dy=0;
                break;
        }
    }
    public void translateToGone()
    {
        calculationDistance();
        getChildAt(0).setTranslationX(dx);
        getChildAt(0).setTranslationY(dy);
    }
    public void startAnimation()
    {
        startAnimation(500);
    }
    public void startAnimation(long du)
    {
        if (aniAble)
        {
            calculationDistance();
            final FinishAnimation finishAnimation=new FinishAnimation() {
                @Override
                public void finishAnimation() {
                    isOriginalLocation=true;
                    if (AnimationHelper.this.finishAnimation!=null)
                        AnimationHelper.this.finishAnimation.finishAnimation();
                }
            };
            if (dx!=0)performTranslateAnimate(dx,true,true,du,finishAnimation);
            else finishedX=true;
            if (dy!=0)performTranslateAnimate(dy,true,false,du,finishAnimation);
            else finishedY=true;
        }
        if(finishAnimation!=null&&finishedY&&finishedX)finishAnimation.finishAnimation();

    }
    private void performTranslateAnimate(final int dx, final boolean isStart, final boolean isX, long du, final FinishAnimation finishAnimation)
    {

        ValueAnimator valueAnimatorX;
        ValueAnimator colorAnimation;
        if (isStart){
            valueAnimatorX=ValueAnimator.ofFloat(dx,0);
            colorAnimation=ValueAnimator.ofFloat(0,1);
        }
        else {
            valueAnimatorX=ValueAnimator.ofFloat(0,dx);
            colorAnimation=ValueAnimator.ofFloat(1,0);
        }
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                getChildAt(0).setAlpha((float)valueAnimator.getAnimatedValue());
            }
        });
        colorAnimation.setDuration(600);
        colorAnimation.setInterpolator(new DecelerateInterpolator());
        valueAnimatorX.addUpdateListener(new AnimationHelper.TranslateUpdateListener(isX,getChildAt(0)));
        valueAnimatorX.setInterpolator(interpolator);
        valueAnimatorX.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (isX)finishedX=false;
                else finishedY=false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (isX)finishedX=true;
                else finishedY=true;
                if (finishAnimation!=null&&finishedX&&finishedY)
                    finishAnimation.finishAnimation();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }
            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        getChildAt(0).setVisibility(VISIBLE);
        valueAnimatorX.setDuration(du).start();
        if (alpha)colorAnimation.start();
    }
    class TranslateUpdateListener implements ValueAnimator.AnimatorUpdateListener
    {
        private boolean isX;
        private View target;
        public TranslateUpdateListener(boolean isX, View target) {
            this.isX = isX;
            this.target = target;
        }
        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            if (isX)target.setTranslationX((float)valueAnimator.getAnimatedValue());
            else target.setTranslationY((float)valueAnimator.getAnimatedValue());
        }
    }

    public void setFinishAnimation(FinishAnimation finishAnimation) {
        this.finishAnimation = finishAnimation;
    }
    public void setOriginalLocation(boolean originalLocation) {
        isOriginalLocation = originalLocation;
    }

    public void setInterpolator(Interpolator interpolator) {
        this.interpolator = interpolator;
    }

    public void setAlpha(boolean alpha) {
        this.alpha = alpha;
    }
}
