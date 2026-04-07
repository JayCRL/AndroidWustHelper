package com.example.wusthelper.helper;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
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


public class PopupWindowLabNew implements View.OnClickListener {

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

    public PopupWindowLabNew(Context context) {
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
        LinearLayout showAllClassLinearLayout = (LinearLayout) mContentView.findViewById(R.id.ll_show_all_class);
        LinearLayout setCurrentWeekLinearLayout = (LinearLayout) mContentView.findViewById(R.id.ll_set_current_week);
        LinearLayout changeBackgroundLinearLayout = (LinearLayout) mContentView.findViewById(R.id.ll_change_background);
        //    LinearLayout shareMyScheduleLinearLayout = (LinearLayout) view.findViewById(R.id.ll_share_my_schedule);
        LinearLayout selectSemesterLinearLayout = (LinearLayout) mContentView.findViewById(R.id.ll_select_semester);
        if(SharePreferenceLab.getIsGraduate()) {
            selectSemesterLinearLayout.setVisibility(View.GONE);
        }
        LinearLayout selectCampusLinearLayout = (LinearLayout) mContentView.findViewById(R.id.ll_select_campus);
        CheckBox isShowAllCheckBox = (CheckBox) mContentView.findViewById(R.id.cb_show_all);

        LinearLayout homepageLinearLayout = (LinearLayout) mContentView.findViewById(R.id.ll_change_homepage);
        LinearLayout chooseSundayLinearLayout = (LinearLayout) mContentView.findViewById(R.id.ll_change_setting);
        LinearLayout fontSizeLinearLayout = (LinearLayout) mContentView.findViewById(R.id.ll_set_fontSize);
        LinearLayout importCalendarLinearLayout = (LinearLayout) mContentView.findViewById(R.id.ll_import_calendar);
        CheckBox homepageCheckBox = (CheckBox) mContentView.findViewById(R.id.change_homepage_check);
        CheckBox ChooseSundayCheckBox = (CheckBox) mContentView.findViewById(R.id.choose_sunday_check);
        //    LinearLayout scanLinearLayout = view.findViewById(R.id.ll_scan);

        boolean isShowNotThisWeek = SharePreferenceLab.getInstance().getIsShowNotThisWeek(mContext);
        boolean isShowBackground = SharePreferenceLab.getInstance().getIsShowBackground(mContext);
        // 勾选表示“只显示本周课程”（隐藏非本周的灰色课程）
        isShowAllCheckBox.setChecked(!isShowNotThisWeek);
        boolean isHomepage = SharePreferenceLab.getInstance().getHomepage_settings(mContext);
        homepageCheckBox.setChecked(isHomepage);

        ChooseSundayCheckBox.setChecked(SharePreferenceLab.getInstance().getIsChooseSundayFirst(mContext));

        showAllClassLinearLayout.setOnClickListener(this);
        selectSemesterLinearLayout.setOnClickListener(this);
        setCurrentWeekLinearLayout.setOnClickListener(this);
        changeBackgroundLinearLayout.setOnClickListener(this);
        chooseSundayLinearLayout.setOnClickListener(this);
        selectCampusLinearLayout.setOnClickListener(this);
        homepageLinearLayout.setOnClickListener(this);
        fontSizeLinearLayout.setOnClickListener(this);
        importCalendarLinearLayout.setOnClickListener(this);
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
        private PopupWindowLabNew popupWindowLabNew;

        public PopupWindowBuilder(Context context) {
            popupWindowLabNew = new PopupWindowLabNew(context);
        }

        /**
         * 设置ViewId
         *
         * @param resLayoutId
         * @return
         */
        public PopupWindowBuilder setView(int resLayoutId) {
            popupWindowLabNew.resLayoutId = resLayoutId;
            popupWindowLabNew.mContentView = null;
            return this;
        }

        /**
         * （重载）
         * 设置View
         *
         * @param contentView
         * @return
         */
        public PopupWindowBuilder setView(View contentView) {
            popupWindowLabNew.resLayoutId = -1;
            popupWindowLabNew.mContentView = contentView;
            return this;
        }

        /**
         * 设置弹窗动画
         *
         * @param animationStyle
         * @return
         */
        public PopupWindowBuilder setAnimationStyle(int animationStyle) {
            popupWindowLabNew.mAnimationStyle = animationStyle;
            return this;
        }

        public PopupWindowBuilder setClippingEnable(boolean enable) {
            popupWindowLabNew.mClipEnable = enable;
            return this;
        }

        /**
         * 设置点击事件接口
         *
         * @param listener
         * @return
         */
        public PopupWindowBuilder setOnClickListener(OnPopItemClickListener listener) {
            popupWindowLabNew.myItemClickListener = listener;
            return this;
        }

        /**
         * 设置父组件
         *
         * @param parent
         * @return
         */
        public PopupWindowBuilder setParent(View parent) {
            popupWindowLabNew.parent = parent;
            return this;
        }

        /**
         * 创建函数
         * (此处返回PopupWindow是为了与旧版的代码兼容，正常感觉返回的是popupWindowLabNew这个类，然后用这个类去管理PopupWindow)
         *
         * @return
         */
        public PopupWindow create() {
            popupWindowLabNew.build();
            return popupWindowLabNew.mPopupWindow;
        }

    }
}

















