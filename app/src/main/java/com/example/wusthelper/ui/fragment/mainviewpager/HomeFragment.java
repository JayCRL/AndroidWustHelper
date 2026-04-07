package com.example.wusthelper.ui.fragment.mainviewpager;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.bigkoo.convenientbanner.ConvenientBanner;
import com.bigkoo.convenientbanner.holder.CBViewHolderCreator;
import com.bigkoo.convenientbanner.holder.Holder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import android.graphics.drawable.Drawable;
import com.example.wusthelper.MyApplication;
import com.example.wusthelper.R;
import com.example.wusthelper.adapter.LocalImageHolderView;
import com.example.wusthelper.adapter.NetImageHolderView;
import com.example.wusthelper.base.fragment.BaseMvpFragment;
import com.example.wusthelper.bean.javabean.CycleImageBean;
import com.example.wusthelper.databinding.FragmentHomeBinding;
import com.example.wusthelper.helper.MyDialogHelper;
import com.example.wusthelper.helper.SharePreferenceLab;
import com.example.wusthelper.mvp.presenter.HomePagePresenter;
import com.example.wusthelper.mvp.view.HomeFragmentView;
import com.example.wusthelper.ui.activity.AiQaActivity;
import com.example.wusthelper.ui.activity.CampusCatActivity;
import com.example.wusthelper.ui.activity.CampusPartnerActivity;
import com.example.wusthelper.ui.activity.CompetitionActivity;
import com.example.wusthelper.ui.activity.CountdownActivity;
import com.example.wusthelper.ui.activity.CreditsStatisticsActivity;
import com.example.wusthelper.ui.activity.FeedBackActivity;
import com.example.wusthelper.ui.activity.GradeActivity;
import com.example.wusthelper.ui.activity.NewEmptyClassRoomActivity;
import com.example.wusthelper.ui.activity.OtherWebActivity;
import com.example.wusthelper.ui.activity.PhysicalDetailActivity;
import com.example.wusthelper.ui.activity.SchoolBusActivity;
import com.example.wusthelper.ui.activity.SchoolCalendarActivity;
import com.example.wusthelper.ui.activity.SecondHandActivity;
import com.example.wusthelper.ui.activity.YellowPageActivity;
import com.example.wusthelper.ui.dialog.PhysicalLoginDialog;
import com.example.wusthelper.ui.dialog.listener.PhysicalLoginDialogListener;

import java.util.List;

public class HomeFragment extends BaseMvpFragment<HomeFragmentView, HomePagePresenter, FragmentHomeBinding>
        implements HomeFragmentView, View.OnClickListener, PhysicalLoginDialogListener {

    private int height;
    private AlertDialog loadingView;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public HomePagePresenter createPresenter() {
        return new HomePagePresenter();
    }

    @Override
    public HomeFragmentView createView() {
        return this;
    }

    @Override
    public void initView() {
        initStatusBar();
        setListener();
        // 主页不需要返回键，隐藏它
        if (getBinding().includeTitle != null && getBinding().includeTitle.ivTitleBack != null) {
            getBinding().includeTitle.ivTitleBack.setVisibility(View.GONE);
        }
    }

    @Override
    protected void lazyLoad() {
        getPresenter().getCycleImageData(getContext());
    }

    private void setListener() {
        getBinding().cardScoreNew.setOnClickListener(this);
        getBinding().cardCountdownNew.setOnClickListener(this);
        getBinding().cardCreditsStatisticsNew.setOnClickListener(this);
        getBinding().cardSecondHand.setOnClickListener(this);
        getBinding().cardCompetition.setOnClickListener(this);
        getBinding().cardCampusPartner.setOnClickListener(this);
        getBinding().cardStudyHelp.setOnClickListener(this);
        // 空教室/蹭课入口
        getBinding().cardStudyHelp.setOnLongClickListener(v -> {
            startActivity(NewEmptyClassRoomActivity.newInstance(getContext()));
            return true;
        });
        getBinding().cardAiQa.setOnClickListener(this);
        getBinding().cardCampusCat.setOnClickListener(this);
        getBinding().cardSchoolBusNew.setOnClickListener(this);
        getBinding().cardSchoolCalendarNew.setOnClickListener(this);
        getBinding().cardPhysical.setOnClickListener(this);
        getBinding().cardYellowPageNew.setOnClickListener(this);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            height = savedInstanceState.getInt("statusBarHeight");
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("statusBarHeight", height);
    }

    public void setHeight(int statusBarHeight) {
        this.height = statusBarHeight;
    }

    public void initStatusBar() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
        getBinding().viewStatus.setLayoutParams(lp);
    }

    @Override
    public void onClick(View v) {
        if(v.equals(getBinding().cardScoreNew)){
            startActivity(GradeActivity.newInstance(getContext()));
        }else if(v.equals(getBinding().cardCountdownNew)){
            startActivity(CountdownActivity.newInstance(getContext()));
        }else if(v.equals(getBinding().cardCreditsStatisticsNew)){
            startActivity(CreditsStatisticsActivity.newInstance(getContext()));
        }else if(v.equals(getBinding().cardSecondHand)){
            startActivity(SecondHandActivity.newInstance(getContext()));
        }else if(v.equals(getBinding().cardCompetition)){
            startActivity(CompetitionActivity.newInstance(getContext()));
        }else if(v.equals(getBinding().cardCampusPartner)){
            startActivity(CampusPartnerActivity.newInstance(getContext()));
        }else if(v.equals(getBinding().cardStudyHelp)){
            // 学习互助页面暂未对接：改为直接进入空教室/蹭课
            startActivity(NewEmptyClassRoomActivity.newInstance(getContext()));
        }else if(v.equals(getBinding().cardAiQa)){
            startActivity(AiQaActivity.newInstance(getContext()));
        }else if(v.equals(getBinding().cardCampusCat)){
            startActivity(CampusCatActivity.newInstance(getContext()));
        }else if(v.equals(getBinding().cardSchoolBusNew)){
            startActivity(SchoolBusActivity.newInstance(getContext()));
        }else if(v.equals(getBinding().cardSchoolCalendarNew)){
            startActivity(SchoolCalendarActivity.newInstance(getContext()));
        }else if(v.equals(getBinding().cardPhysical)){
            if(SharePreferenceLab.getInstance().getIsPhysicalLogin(MyApplication.getContext())){
                startActivity(PhysicalDetailActivity.newInstance(getActivity()));
            }else{
                showPhysicalLoginDialog();
            }
        }else if(v.equals(getBinding().cardYellowPageNew)){
            startActivity(YellowPageActivity.newInstance(getContext()));
        }
    }

    @Override
    public void showCycleImageFromNet(List<CycleImageBean> data) {
        Log.e("HomeFragment", "showCycleImageFromNet size=" + (data == null ? 0 : data.size()));

        // 直接展示 Banner：之前的“等首图 ready 再 setPages”在部分机型/生命周期下可能回调不触发，导致 banner 一直 invisible。
        getBinding().banner1.setVisibility(View.VISIBLE);

        getBinding().banner1.setPages(new CBViewHolderCreator() {
            @Override
            public Holder createHolder(View itemView) {
                return new NetImageHolderView(itemView);
            }

            @Override
            public int getLayoutId() {
                return R.layout.card_app;
            }
        }, data)
                .setPageIndicatorAlign(ConvenientBanner.PageIndicatorAlign.ALIGN_PARENT_RIGHT)
                .setPointViewVisible(true);

        // 轮播策略：多张图才自动轮播，单张图不轮播
        if (data != null && data.size() > 1) {
            getBinding().banner1.setCanLoop(true);
            getBinding().banner1.startTurning(3000);
        } else {
            getBinding().banner1.setCanLoop(false);
            getBinding().banner1.stopTurning();
        }
    }

    @Override
    public void showCycleImageFromLocal(List<Bitmap> data) {
        Log.e("HomeFragment", "showCycleImageFromLocal size=" + (data == null ? 0 : data.size()));
        getBinding().banner1.setVisibility(View.VISIBLE);
        getBinding().banner1.setPages(new CBViewHolderCreator() {
            @Override
            public Holder createHolder(View itemView) {
                return new LocalImageHolderView(itemView, getActivity());
            }

            @Override
            public int getLayoutId() {
                return R.layout.card_app;
            }
        }, data).setPageIndicatorAlign(ConvenientBanner.PageIndicatorAlign.ALIGN_PARENT_RIGHT)
                .setPointViewVisible(true);
        getBinding().banner1.startTurning();
    }

    @Override
    public void showHomeNotice(String text) {
        getBinding().tvHomeNotice.setVisibility(View.VISIBLE);
        getBinding().tvHomeNotice.setText("公告：" + text);
    }

    @Override
    public void hideHomeNotice() {
        getBinding().tvHomeNotice.setVisibility(View.GONE);
    }

    @Override
    public void showPhysicalLoginDialog() {
        if(getActivity()==null) {
            return;
        }
        PhysicalLoginDialog physicalLoginDialog = new PhysicalLoginDialog(this);
        physicalLoginDialog.show(getActivity().getSupportFragmentManager(), "PhysicalLogin");
    }

    @Override
    public void showLoadDialog() {
        if(loadingView==null){
            loadingView = MyDialogHelper.createLoadingDialog(getContext(),"登录中...", false);
        }
        loadingView.show();
    }

    @Override
    public void cancelLoadDialog() {
        if(loadingView!=null){
            loadingView.cancel();
        }
    }

    @Override
    public void startPhysicalDetailActivity() {
        startActivity(PhysicalDetailActivity.newInstance(getContext()));
    }

    @Override
    public void loginPhysical(String password) {
        getPresenter().LoginPhysical(password);
    }

    @Override
    public void onResume() {
        super.onResume();
        // 轮播图禁用自动轮播（避免串图），这里只做一次 stopTurning 保证状态一致
        getBinding().banner1.stopTurning();
    }

    @Override
    public void onPause() {
        super.onPause();
        getBinding().banner1.stopTurning();
    }
}
