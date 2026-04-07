package com.example.wusthelper.helper;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.example.wusthelper.R;

public class PopupWindowGradeLab implements View.OnClickListener{
    private int mWidth;
    private int mHeight;

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    private PopupWindow mPopupWindow;
    private Context mContext;


    //PopupWindow的ViewId
    private int resLayoutId;

    //PopupWindow的View
    private View mContentView;

    //动画Id
    private int mAnimationStyle = -1;

    //依赖的父组件 (不能为空！！！)
    private View parent;

    //子项点击事件的回调接口
    private OnPopItemClickListener myItemClickListener;
    private boolean mClipEnable = true;

    public PopupWindowGradeLab(Context context) {
        this.mContext = context;
    }

    private PopupWindow build() {
        //初始化View与PopupWindow
        initView();
        //设置动画
        if (mAnimationStyle != -1) {
            mPopupWindow.setAnimationStyle(mAnimationStyle);
        }

        //设置子项点击事件
        setViewClick();

        //设置一些属性
        apply(mPopupWindow);

//        if (mWidth == 0 || mHeight == 0) {
//
//        }

        //显示PopupWindow  (这里的父组件不能为空，因为PopupWindow是一个依附于其他组件的弹窗)
        if (parent != null) {
            showAtParentBottom(parent);
        }

        //更新PopupWindow
        mPopupWindow.update();

        return mPopupWindow;
    }

    /**
     * 初始化布局与PopupWindow
     */
    private void initView() {
        if (mContentView == null) {
            mContentView = LayoutInflater.from(mContext).inflate(resLayoutId, null);
        }

        if (mWidth != 0 && mHeight != 0) {
            mPopupWindow = new PopupWindow(mContentView, mWidth, mHeight);
        } else {
            mPopupWindow = new PopupWindow(mContentView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            mPopupWindow.getContentView().measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            //如果外面没有设置宽高的情况下，计算宽高并赋值
            mWidth = mPopupWindow.getContentView().getMeasuredWidth();
            mHeight = mPopupWindow.getContentView().getMeasuredHeight();
        }
    }


    /**
     * 设置点击事件
     */
    private void setViewClick() {
        LinearLayout chart = (LinearLayout) mContentView.findViewById(R.id.ll_chart_grade);
        LinearLayout explain = (LinearLayout) mContentView.findViewById(R.id.ll_explain_grade);
        LinearLayout scholarship = (LinearLayout) mContentView.findViewById(R.id.ll_scholarship_grade);
        LinearLayout GPASegmentTable = (LinearLayout) mContentView.findViewById(R.id.ll_GPA_segment_table_grade);

        if(SharePreferenceLab.getIsGraduate()) {
            scholarship.setVisibility(View.GONE);
            chart.setVisibility(View.GONE);
        }

        chart.setOnClickListener(this);
        explain.setOnClickListener(this);
        scholarship.setOnClickListener(this);
        GPASegmentTable.setOnClickListener(this);
    }

    private static final String TAG = "PopWindowLab";

    /**
     * 显示PopupWindow
     *
     * @param parent
     * @return
     */
    private PopupWindow showAtParentBottom(View parent) {
        if (mPopupWindow != null) {

            int[] location = new int[2];
            //获取父组件坐标  （父组件左上点的坐标）
            parent.getLocationOnScreen(location);

            //此处 -4/5 倍的PopupWindow的宽度，为了让PopupWindow显示窗口向右移一点  而不是右对齐
            mPopupWindow.showAtLocation(parent, Gravity.NO_GRAVITY, location[0] + parent.getWidth() / 2 - 4 * getWidth() / 5
                    , location[1] + parent.getHeight());

            //设置半透明蒙层
            backgroundAlpha(0.7f);

            //设置取消监听事件
            mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    //取消半透明蒙层
                    backgroundAlpha(1.0f);
                }
            });
        }
        return mPopupWindow;
    }

    /**
     * 设置蒙层透明度
     *
     * @param f
     */
    private void backgroundAlpha(float f) {

        WindowManager.LayoutParams lp = ((Activity) mContext).getWindow().getAttributes();

        lp.alpha = f;

        ((Activity) mContext).getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);//背景变暗

        ((Activity) mContext).getWindow().setAttributes(lp);

    }

    /**
     * 设置一些PopupWindow属性
     *
     * @param popupWindow
     */
    private void apply(PopupWindow popupWindow) {
        popupWindow.setClippingEnabled(mClipEnable);
        popupWindow.setFocusable(true);
        popupWindow.setTouchable(true);
        popupWindow.setOutsideTouchable(true);
    }

    @Override
    public void onClick(View v) {
        myItemClickListener.onPopItemClick(v);
    }


    //此处是使用Builder来构建PopupWindowLabNew
    public static class PopupWindowBuilder {
        private PopupWindowGradeLab gradeLab;

        public PopupWindowBuilder(Context context) {
            gradeLab = new PopupWindowGradeLab(context);
        }

        /**
         * 设置ViewId
         *
         * @param resLayoutId
         * @return
         */
        public PopupWindowGradeLab.PopupWindowBuilder setView(int resLayoutId) {
            gradeLab.resLayoutId = resLayoutId;
            gradeLab.mContentView = null;
            return this;
        }

        /**
         * （重载）
         * 设置View
         *
         * @param contentView
         * @return
         */
        public PopupWindowGradeLab.PopupWindowBuilder setView(View contentView) {
            gradeLab.resLayoutId = -1;
            gradeLab.mContentView = contentView;
            return this;
        }

        /**
         * 设置弹窗动画
         *
         * @param animationStyle
         * @return
         */
        public PopupWindowGradeLab.PopupWindowBuilder setAnimationStyle(int animationStyle) {
            gradeLab.mAnimationStyle = animationStyle;
            return this;
        }

        public PopupWindowGradeLab.PopupWindowBuilder setClippingEnable(boolean enable) {
            gradeLab.mClipEnable = enable;
            return this;
        }

        /**
         * 设置点击事件接口
         *
         * @param listener
         * @return
         */
        public PopupWindowGradeLab.PopupWindowBuilder setOnClickListener(OnPopItemClickListener listener) {
            gradeLab.myItemClickListener = listener;
            return this;
        }

        /**
         * 设置父组件
         *
         * @param parent
         * @return
         */
        public PopupWindowGradeLab.PopupWindowBuilder setParent(View parent) {
            gradeLab.parent = parent;
            return this;
        }

        /**
         * 创建函数
         * (此处返回PopupWindow是为了与旧版的代码兼容，正常感觉返回的是popupWindowLabNew这个类，然后用这个类去管理PopupWindow)
         *
         * @return
         */
        public PopupWindow create() {
            gradeLab.build();
            return gradeLab.mPopupWindow;
        }

    }
}
