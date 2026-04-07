package com.example.wusthelper.ui.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;

import com.airbnb.lottie.LottieAnimationView;
import com.example.wusthelper.R;
import com.example.wusthelper.bean.javabean.CourseBean;
import com.example.wusthelper.helper.SharePreferenceLab;
import com.example.wusthelper.ui.activity.EditActivity;
import com.zhy.magicviewpager.transformer.RotateYTransformer;

import java.util.ArrayList;
import java.util.List;

public class CourseDialogNew extends Dialog {

    public CourseDialogNew(@NonNull Context context) {
        super(context);
    }


    public CourseDialogNew(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }


    public static class Builder {

        private Context mContext;

        //数据集
        private List<CourseBean> mListBean;

        //自定义Dialog对象
        private CourseDialogNew mDialogNew;

        //View集合
        private List<View> mListView;

        //ViewPager
        private ViewPager mViewPager;

        //Dialog布局
        private View mRootView;

        //Lottie动画View集合
        private List<LottieAnimationView> lottieAnimationViews;

        //PagerAdapter
        private BasePagerAdapter adapter;

        private LayoutInflater inflater;

        private int mWidth;

        private int mHeight;

        private static final String TAG = "CourseDialogNew";

        public Builder(Context context) {
            this.mContext = context;
            this.mDialogNew = new CourseDialogNew(context);
            this.lottieAnimationViews = new ArrayList<>();
        }

        public Builder setList(List<CourseBean> list) {
            Log.e(TAG,"list" + list);
            this.mListBean = list;
            return this;
        }


        /**
         * 构造函数
         *
         * @return
         */
        public CourseDialogNew create() {

            initData();

            dialogSetting();

            initPagerData();

            initPager();

            mDialogNew.show();
            lottieAnimationViews.get(0).playAnimation();

            return mDialogNew;
        }

        /**
         * 初始化数据
         */
        public void initData() {
            mListView = new ArrayList<>();
            mDialogNew = new CourseDialogNew(mContext, R.style.CourseDialog);

            inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            mRootView = inflater.inflate(R.layout.dialog_course, null);
            mDialogNew.addContentView(mRootView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }

        /**
         * dialog一些设置  ( 高度  and 背景透明)
         */
        private void dialogSetting() {
            mDialogNew.getWindow().setBackgroundDrawableResource(android.R.color.transparent);  //设置对话框背景透明 ，对于AlertDialog 就不管用了
            DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
            mWidth = dm.widthPixels;
            mHeight = dm.heightPixels;
            android.view.WindowManager.LayoutParams p = mDialogNew.getWindow().getAttributes();  //获取对话框当前的参数值
            p.width = (int) (mWidth);    //宽度设置为屏幕的宽
            p.height = (int) (mHeight * 0.6);    //高度设置为屏幕的0.6
            mDialogNew.getWindow().setAttributes(p);     //设置生效
        }

        /**
         * 初始化数据列表 (List<View></>)
         */
        private void initPagerData() {
            for (int i = 0; i < mListBean.size(); i++) {
                CourseBean courseBean = mListBean.get(i);
                View view = inflater.inflate(R.layout.item_dialog_course, null);
                FrameLayout frameLayoutNormal = view.findViewById(R.id.normal_type);
                FrameLayout frameLayoutQr = view.findViewById(R.id.qr_type);
                FrameLayout frameLayoutSearchCourse = view.findViewById(R.id.searchClass_type);
                if(courseBean.getClassType() == CourseBean.TYPE_QR){
                    frameLayoutNormal.setVisibility(View.GONE);
                    frameLayoutQr.setVisibility(View.VISIBLE);
                    LottieAnimationView lottie = view.findViewById(R.id.mLottieView_qr);
                    lottieAnimationViews.add(lottie);
                }else if(courseBean.getClassType() == CourseBean.TYPE_SEARCH){
                    frameLayoutNormal.setVisibility(View.GONE);
                    frameLayoutQr.setVisibility(View.GONE);
                    frameLayoutSearchCourse.setVisibility(View.VISIBLE);
                    LottieAnimationView lottie = view.findViewById(R.id.mLottieView_searchClass);
                    lottieAnimationViews.add(lottie);
                } else {
                    LottieAnimationView lottie = view.findViewById(R.id.mLottieView);
                    lottieAnimationViews.add(lottie);
                }


                //给View添加数据
                initViewData(courseBean, view, i);

                //点击pager
                view.setOnClickListener(v -> {
                    Intent intent = EditActivity.newInstance(mContext,
                            courseBean, 1
                    );
                    mContext.startActivity(intent);
                    mDialogNew.dismiss();
                });

                mListView.add(view);
            }
        }

        /**
         * 配置每一个pager的数据
         *
         * @param courseBean
         * @param view
         */
        @SuppressLint("SetTextI18n")
        private void initViewData(CourseBean courseBean, View view, int position) {
            TextView isCurrentWeekTv, classNameTv, classroomTv, teacherNameTv, classNoTv, classWeekTv, qrCourse;
            RelativeLayout background;
            View qrView;
            if(courseBean.getClassType() == CourseBean.TYPE_QR) {
                qrView = view.findViewById(R.id.qr_view);
                qrView.setVisibility(View.VISIBLE);
                qrCourse = view.findViewById(R.id.dialog_qr_course);
                qrCourse.setVisibility(View.VISIBLE);
            }
            isCurrentWeekTv = view.findViewById(R.id.dialog_is_current_week);
            classNameTv = view.findViewById(R.id.dialog_class_name);
            classroomTv = view.findViewById(R.id.dialog_classroom);
            teacherNameTv = view.findViewById(R.id.dialog_teacher_name);
            classNoTv = view.findViewById(R.id.dialog_class);
            classWeekTv = view.findViewById(R.id.dialog_class_week);
            background = view.findViewById(R.id.dialog_background_rel);

            String isCurrentWeekStr;
            if (courseBean.isInClass()) {
                if (position == 0) {
                    background.setBackgroundColor(mContext.getResources().getColor(R.color.colorClass4));
//                    isCurrentWeekTv.setTextColor(Color.parseColor("#ffffff"));
//                    classNameTv.setTextColor(Color.parseColor("#ffffff"));
//                    classroomTv.setTextColor(Color.parseColor("#ffffff"));
//                    teacherNameTv.setTextColor(Color.parseColor("#ffffff"));
//                    classNoTv.setTextColor(Color.parseColor("#ffffff"));
//                    classWeekTv.setTextColor(Color.parseColor("#ffffff"));
//                    background.setBackgroundColor(Color.parseColor("#D1C16C"));
                    isCurrentWeekStr = "本周";
                } else {
                    background.setBackgroundColor(Color.parseColor("#ffffff"));
                    isCurrentWeekStr = "非本周";
                }
            } else {
                isCurrentWeekStr = "非本周";
            }
            isCurrentWeekTv.setText(isCurrentWeekStr);
            classNameTv.setText(courseBean.getCourseName());
            classroomTv.setText(courseBean.getClassRoom());
            teacherNameTv.setText(courseBean.getTeacherName());
            classNoTv.setText(courseBean.getClassNo());
            Log.e(TAG,"courseBean.getStartWeek() + \"-\" + courseBean.getEndWeek() + \"周\"" +courseBean.getStartWeek() +" " +courseBean.getEndWeek());
            classWeekTv.setText(courseBean.getStartWeek() + "-" + courseBean.getEndWeek() + "周");
            if(SharePreferenceLab.getIsItalic()) {
                isCurrentWeekTv.setTypeface(null, Typeface.ITALIC);
                classNameTv.setTypeface(null, Typeface.ITALIC);
                classroomTv.setTypeface(null, Typeface.ITALIC);
                teacherNameTv.setTypeface(null, Typeface.ITALIC);
                classNoTv.setTypeface(null, Typeface.ITALIC);
                classWeekTv.setTypeface(null, Typeface.ITALIC);
            } else {
                isCurrentWeekTv.setTypeface(null, Typeface.NORMAL);
                classNameTv.setTypeface(null, Typeface.NORMAL);
                classroomTv.setTypeface(null, Typeface.NORMAL);
                teacherNameTv.setTypeface(null, Typeface.NORMAL);
                classNoTv.setTypeface(null, Typeface.NORMAL);
                classWeekTv.setTypeface(null, Typeface.NORMAL);
            }
        }

        /**
         * 初始化ViewPager
         */
        private void initPager() {
            mViewPager = mRootView.findViewById(R.id.course_viewPager);
            adapter = new BasePagerAdapter(mListView);
            mViewPager.setPageMargin(60);

            mViewPager.setOffscreenPageLimit(mListView.size());
            mViewPager.setAdapter(adapter);
            mViewPager.setPageTransformer(true, new RotateYTransformer());

            mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    stopAllLottie();
                    lottieAnimationViews.get(position).playAnimation();
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }

        /**
         * 停止所有动画
         */
        private void stopAllLottie() {
            for (LottieAnimationView view : lottieAnimationViews) {
                view.pauseAnimation();
            }
        }
    }
}
