package com.example.wusthelper.ui.activity;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.GridLayoutAnimationController;
import android.view.animation.LayoutAnimationController;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.PopupWindow;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.example.wusthelper.R;
import com.example.wusthelper.adapter.GradeAdapter;
import com.example.wusthelper.adapter.GraduateGradeAdapter;
import com.example.wusthelper.base.activity.BaseMvpActivity;
import com.example.wusthelper.bean.javabean.GradeBean;
import com.example.wusthelper.bean.javabean.GraduateGradeBean;
import com.example.wusthelper.databinding.ActivityGradeBinding;
import com.example.wusthelper.helper.MyDialogHelper;
import com.example.wusthelper.helper.OnPopItemClickListener;
import com.example.wusthelper.helper.PopupWindowGradeLab;
import com.example.wusthelper.helper.SharePreferenceLab;
import com.example.wusthelper.helper.SnackBarHelper;
import com.example.wusthelper.mvp.presenter.GradePresenter;
import com.example.wusthelper.mvp.view.GradeView;
import com.example.wusthelper.utils.ToastUtil;

import org.litepal.LitePal;

import java.util.List;


public class GradeActivity extends BaseMvpActivity<GradeView, GradePresenter,ActivityGradeBinding>
        implements GradeView, View.OnClickListener, OnPopItemClickListener, TextWatcher {

    private static final String TAG = "GradeActivity";

    /**
     * UI
     * */
    private AlertDialog loadingView;

    private GradeAdapter gradeAdapter = new GradeAdapter();
    private GraduateGradeAdapter graduateGradeAdapter = new GraduateGradeAdapter();
    //显示设置菜单的PopWindow
    private PopupWindow popupWindow;


    public static Intent newInstance(Context context) {
        return new Intent(context, GradeActivity.class);
    }
    @Override
    public GradePresenter createPresenter() {
        return new GradePresenter();
    }

    @Override
    public GradeView createView() {
        return this;
    }

    @Override
    public void initView() {

        setSupportActionBar(getBinding().toolbar);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        setListener();
        initGradeContent();

        getPresenter().initPresenterData();
    }

    private void setListener() {
        getBinding().ivBack.setOnClickListener(this);
        getBinding().ivSearch.setOnClickListener(this);
        getBinding().ivMore.setOnClickListener(this);
        getBinding().tvSpinner.setOnClickListener(this);

        getBinding().etSearchGrade.addTextChangedListener(this);

        getBinding().ivSearchCancel.setOnClickListener(this);
    }

    private void initGradeContent() {
//        //设置动画
//        LayoutAnimationController controller =
//                AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation_fall_down);
//        //设置动画
//        getBinding().recyclerGradeActivity.setLayoutAnimation(controller);
//        //开始动画
//        getBinding().recyclerGradeActivity.startLayoutAnimation();

       // setLayoutAnimation();

        if(SharePreferenceLab.getIsGraduate()) {
            graduateGradeAdapter.setAnimationWithDefault(BaseQuickAdapter.AnimationType.AlphaIn);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            getBinding().recyclerGradeActivity.setLayoutManager(layoutManager);
            getBinding().recyclerGradeActivity.setAdapter(graduateGradeAdapter);
        } else {
            gradeAdapter.setAnimationWithDefault(BaseQuickAdapter.AnimationType.AlphaIn);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            getBinding().recyclerGradeActivity.setLayoutManager(layoutManager);
            getBinding().recyclerGradeActivity.setAdapter(gradeAdapter);
        }

    }

    /**
     * 给recycleView设置动画
     */
    private void setLayoutAnimation() {
        Animation animation = AnimationUtils.loadAnimation(getBinding().recyclerGradeActivity.getContext(),
                R.anim.anim_course_item);
        animation.setDuration(100);
        LayoutAnimationController controller = new LayoutAnimationController(animation);
        //设置每个item之间动画播放的间隔  为每个item动画时间的0.15
        controller.setDelay(0.1f);
        //设置每个item动画的播放顺序时随机播放
        controller.setOrder(GridLayoutAnimationController.ORDER_RANDOM);
        //设置动画
        getBinding().recyclerGradeActivity.setLayoutAnimation(controller);
        //开始动画
        getBinding().recyclerGradeActivity.scheduleLayoutAnimation();
    }


    @Override
    public void onGradeListShow(List<GradeBean> list) {
        gradeAdapter.setList(list);
        gradeAdapter.notifyDataSetChanged();
    }

    @Override
    public void onGraduateListShow(List<GraduateGradeBean> list) {
        graduateGradeAdapter.setList(list);
        graduateGradeAdapter.notifyDataSetChanged();
    }

    @Override
    public void showLoading(String msg) {
        loadingView = MyDialogHelper.createLoadingDialog(this,msg, false);
        loadingView.show();
    }

    @Override
    public void cancelDialog() {
        if (loadingView != null)
        loadingView.cancel();
    }

    @Override
    public void showSuccessColorSnackBar(String msg) {
        SnackBarHelper.showColorSnackBar(getBinding().recyclerGradeActivity, msg, R.color.color_snack_bar_update_score);
    }

    @Override
    public void showFailureColorSnackBar(String msg) {
        SnackBarHelper.showColorSnackBar(getBinding().recyclerGradeActivity, msg, R.color.color_snack_bar_no_internet);
    }

    @Override
    public void onClick(View v) {
        if(v.equals(getBinding().ivBack)){
            finish();
        }else if(v.equals(getBinding().ivSearch)){
            showSearch();
        }else if(v.equals(getBinding().ivSearchCancel)){
            cancelSearch();
        }else if(v.equals(getBinding().ivMore)){

            popupWindow = new PopupWindowGradeLab.PopupWindowBuilder(this)
                    .setView(R.layout.pop_grade_menu)
                    .setOnClickListener(this)
                    .setAnimationStyle(R.style.PopupMenuAnimation)
                    .setParent(getBinding().ivMore)
                    .create();

        }else if(v.equals(getBinding().tvSpinner)){
            showListDialog();
        }
    }

    private void showSearch() {
        getBinding().etSearchGrade.setVisibility(View.VISIBLE);
        getBinding().ivSearchCancel.setVisibility(View.VISIBLE);
        getBinding().ivSearch.setVisibility(View.GONE);
        getBinding().tvSpinner.setVisibility(View.GONE);
        getBinding().ivMore.setVisibility(View.GONE);
        showSoftInputFromWindow(this,getBinding().etSearchGrade);
    }

    private void cancelSearch() {
        getBinding().etSearchGrade.setVisibility(View.GONE);
        getBinding().ivSearchCancel.setVisibility(View.GONE);
        getBinding().ivSearch.setVisibility(View.VISIBLE);
        getBinding().tvSpinner.setVisibility(View.VISIBLE);
        getBinding().ivMore.setVisibility(View.VISIBLE);
        getBinding().etSearchGrade.setText("");
        closeInputMethod();
    }

    @Override
    public void showListDialog() {
        if(!SharePreferenceLab.getIsGraduate()){
            getPresenter().initTermList();
            AlertDialog.Builder listDialog = new AlertDialog.Builder(GradeActivity.this);
            listDialog.setTitle("选择学期");
            listDialog.setItems(getPresenter().getTermItems(), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    getBinding().tvSpinner.setText(getPresenter().getTermList().get(which).getShowTerm());
                    getPresenter().changeTerm(which);
                }
            });

            listDialog.show();
        }

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onPopItemClick(View v) {
        switch (v.getId()){
            case R.id.ll_chart_grade:
                popupWindow.dismiss();
                startActivity(GradeChartActivity.newInstance(this));
                break;
            case R.id.ll_scholarship_grade:
                popupWindow.dismiss();
                if(LitePal.findAll(GradeBean.class).size()<1){
                    ToastUtil.show("无成绩信息");
                }else {
                    Intent intent = new Intent(GradeActivity.this, ScholarShipChooseActivity.class);
                    startActivity(intent);
                }
                break;
            case R.id.ll_explain_grade:
                popupWindow.dismiss();
                View v_explain = LayoutInflater.from(this)
                        .inflate(R.layout.dialog_grade_explain, null);
                AlertDialog.Builder builder_explain = new AlertDialog.Builder(GradeActivity.this);
                builder_explain.setView(v_explain);
                builder_explain.setPositiveButton("我知道了", null);
                builder_explain.show();
                break;
            case R.id.ll_GPA_segment_table_grade:
                popupWindow.dismiss();
                View view = LayoutInflater.from(this)
                        .inflate(R.layout.dialog_grade_section, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(GradeActivity.this);
                builder.setView(view);
                builder.setPositiveButton("我知道了", null);
                builder.show();
                break;
            default:
                break;
        }
    }

    /**
     * EditText内容改变的回调
     * */
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        getPresenter().queryGradeWithText(s.toString());
    }

    /**
     * EditText获取焦点并显示软键盘
     */
    public void showSoftInputFromWindow(Activity activity, EditText editText) {
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();
        //activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText,0);
    }

    //直接关闭键盘输入法
    private void closeInputMethod() {
        InputMethodManager m=(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        m.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

}