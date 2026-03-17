package com.example.wusthelper.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.example.wusthelper.R;
import com.example.wusthelper.bean.javabean.GradeBean;
import com.example.wusthelper.databinding.DialogGradeBinding;

public class GradeDialogNew extends Dialog {

    public GradeDialogNew(@NonNull Context context) {
        super(context);
    }

    public GradeDialogNew(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    public static class Builder {

        private Context mContext;

        //自定义Dialog对象
        private GradeDialogNew mDialogNew;

        //Dialog的ViewBinding对象
        private DialogGradeBinding binding;

        //Dialog布局
        private View mRootView;

        private LayoutInflater inflater;

        private int mWidth;

        private int mHeight;

        //成绩数据
        private GradeBean mGradeBean;

        public Builder(Context context) {
            this.mContext = context;
            this.mDialogNew = new GradeDialogNew(context);
            this.mGradeBean = new GradeBean();
        }

        public GradeDialogNew.Builder setData(GradeBean bean) {
            this.mGradeBean = bean;
            return this;
        }

        /**
         * 构造函数
         *
         * @return
         */
        public GradeDialogNew create() {

            initData();

            initGradeData();

            dialogSetting();

            mDialogNew.show();

            return mDialogNew;
        }

        /**
         * 初始化数据
         */
        public void initData() {

            mDialogNew = new GradeDialogNew(mContext, R.style.CourseDialog);
            binding = DialogGradeBinding.inflate((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
            mDialogNew.addContentView(binding.getRoot(), new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }

        /**
         * 显示成绩数据
         * */
        private void initGradeData() {
            binding.tvDialogGradeName.setText(mGradeBean.getCourseName());
            binding.tvDialogGradeGrade.setText(mGradeBean.getGrade());
            binding.tvDialogGradePoint.setText(mGradeBean.getGradePoint());
            binding.tvDialogGradeCredit.setText(mGradeBean.getCourseCredit());
            binding.tvDialogGradeHours.setText(mGradeBean.getCourseHours());
            binding.tvDialogGradeEvaluationMode.setText(mGradeBean.getEvaluationMode());
            binding.tvDialogGradeTerm.setText(mGradeBean.getSchoolTerm());
            binding.tvDialogGradeExamNature.setText(mGradeBean.getExamNature());
            binding.tvDialogGradeCourseNature.setText(mGradeBean.getCourseNature());

            if(Double.parseDouble(mGradeBean.getGradePoint())<4.0){
                binding.ivGradeDetailGood.setVisibility(View.GONE);
            }
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
            p.width = (int) (mWidth * 0.8);    //宽度设置为屏幕的宽
            p.height = (int) (mHeight * 0.5);    //高度设置为屏幕的0.6
            mDialogNew.getWindow().setAttributes(p);     //设置生效
        }


    }
}
