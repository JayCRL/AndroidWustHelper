package com.example.wusthelper.ui.fragment.mainviewpager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.wusthelper.R;
import com.example.wusthelper.base.fragment.BaseMvpFragment;
import com.example.wusthelper.databinding.FragmentMineBinding;
import com.example.wusthelper.helper.ConfigHelper;
import com.example.wusthelper.helper.MyDialogHelper;
import com.example.wusthelper.mvp.presenter.MinePagePresenter;
import com.example.wusthelper.mvp.view.MineFragmentView;
import com.example.wusthelper.ui.activity.FeedBackActivity;
import com.example.wusthelper.ui.activity.AboutActivity;
import com.example.wusthelper.ui.activity.LoginMvpActivity;
import com.example.wusthelper.ui.activity.MainSettingActivity;
import com.example.wusthelper.ui.activity.UserSettingActivity;
import com.example.wusthelper.ui.dialog.UpdateDialog;
import com.example.wusthelper.utils.ToastUtil;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class MineFragment extends BaseMvpFragment<MineFragmentView, MinePagePresenter, FragmentMineBinding>
        implements MineFragmentView, View.OnClickListener{

    private static final String TAG = "MineFragment";


    //MainActivity设置了隐藏状态栏，需要记录下状态栏高度，自己设置状态栏
    private int height;

    public static MineFragment newInstance() {
        return new MineFragment();
    }

    @Override
    public MinePagePresenter createPresenter() {
        return new MinePagePresenter();
    }

    @Override
    public MineFragmentView createView() {
        return this;
    }

    @Override
    public void onResume() {
        super.onResume();
        getPresenter().initPresenterData();
    }

    @Override
    public void initView() {
        initStatusBar();
        setListener();
    }

    private void setListener() {
        getBinding().cardAboutUs.setOnClickListener(this);
        getBinding().cardFeedback.setOnClickListener(this);
        getBinding().cardLogout.setOnClickListener(this);
        getBinding().cardQq.setOnClickListener(this);
        getBinding().cardUpdate.setOnClickListener(this);
        getBinding().ivUsrImgNew.setOnClickListener(this);
        getBinding().setting.setOnClickListener(this);
    }

    @Override
    public void showName(String name) {
        getBinding().tvUserNameNew.setText(name);
    }

    @Override
    public void showStudentId(String id) {
        getBinding().tvStudentIdNew.setText(id);
    }

    @Override
    public void showUserImage(String path) {
        if(path.equals("")){
            Glide.with(this)
                    //如果用户没有设置图片，路径就是空的，会报错，但是Glide拦截以后，不影响运行使用
                    .load(R.drawable.user_img_holder)
                    .apply(RequestOptions.placeholderOf(R.drawable.user_img_holder))
                    .apply(RequestOptions.centerCropTransform())
                    .apply(RequestOptions.placeholderOf(R.drawable.user_img_holder))
                    .into(getBinding().ivUsrImgNew);
        }else {
            Glide.with(this)
                    .load(path)
                    .apply(RequestOptions.placeholderOf(R.drawable.user_img_holder))
                    .apply(RequestOptions.centerCropTransform())
                    .apply(RequestOptions.placeholderOf(R.drawable.user_img_holder))
                    .into(getBinding().ivUsrImgNew);
        }

    }

    /**
     * 接下来重写的两个方法，是为了储存Fragment的状态栏高度height
     * */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null)
            height = savedInstanceState.getInt("statusBarHeight");
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("statusBarHeight", height);
    }

    /**
     * 设置状态栏高度，在MainActivity调用 （因为在MainActivity 做过处理，隐藏了状态栏）
     * */
    public void setHeight(int statusBarHeight) {
        this.height = statusBarHeight;
    }

    /**
     * 设置状态栏高度，这个在Fragment调用，是为了给状态栏设置高度
     * */
    public void initStatusBar() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
        getBinding().viewStatus.setLayoutParams(lp);
    }

    /**
     * 点击事件
     * */
    @Override
    public void onClick(View v) {
        if(v.equals(getBinding().cardAboutUs)){
            //点击了关于我们的按钮
            onClickCardAboutUs();

        }else if(v.equals(getBinding().cardFeedback)){
            //点击了联系我们的按钮
            onClickCardFeedback();

        }else if(v.equals(getBinding().cardLogout)){
            //点击了注销按钮
            onClickCardLogout();

        }else if(v.equals(getBinding().cardQq)){
            //点击了QQ用户群按钮
            onClickCardQq();

        }else if(v.equals(getBinding().cardUpdate)){
            //点击了更新按钮
            onClickCardUpdate();

        }else if(v.equals(getBinding().ivUsrImgNew)){
            //点击了头像按钮
            onClickIvUsrImgNew();

        }else if(v.equals(getBinding().setting)) {
            //点击了设置
            onClickSettingCardView();
        }
    }

    private void onClickSettingCardView() {
        startActivity(MainSettingActivity.newInstance(getContext()));
    }

    private void onClickCardAboutUs() {
        startActivity(AboutActivity.newInstance(getContext()));
    }

    private void onClickCardFeedback() {
        Intent intentFeedback = FeedBackActivity.newInstance(getContext());
        startActivity(intentFeedback);
    }

    private void onClickCardQq() {
//        if(!joinQQGroup("BuEiEqH1muTbRO3v472iVwyyMGBYGK3d"))
//            ToastUtil.showShortToastCenter("未安装手机qq");
        if(!joinQQGroup("h_P5P68MHr6NEccTDUFQXR5VPJ_wXWCh"))
            ToastUtil.showShortToastCenter("未安装手机qq");
    }

    private void onClickCardUpdate() {

        if(getActivity()==null)
            return;
        if(ConfigHelper.getIfHasNewVersion()){
            UpdateDialog updateDialog = new UpdateDialog();
            updateDialog.show(getActivity().getSupportFragmentManager(), "Update");
        }else {
            ToastUtil.showShortToastCenter("已经是最新版本！");
        }
    }

    private void onClickIvUsrImgNew() {
        startActivity(UserSettingActivity.newInstance(getContext()));
    }

    private void onClickCardLogout() {
        SweetAlertDialog logOutDialog = MyDialogHelper.getCommonDialog(getContext(), SweetAlertDialog.NORMAL_TYPE,
                null, "是否确定退出登录？", "确定", "取消",
                sweetAlertDialog -> {
                    getPresenter().Logout();
                    Intent intent = LoginMvpActivity.newInstance(getContext());
                    startActivity(intent);
                    getActivity().finish();
                    sweetAlertDialog.dismissWithAnimation();
                });
        logOutDialog.show();
    }

//    /****************
//     *
//     * 发起添加群流程。群号：新武科大助手用户群(439648667) 的 key 为： BuEiEqH1muTbRO3v472iVwyyMGBYGK3d
//     * 调用 joinQQGroup(BuEiEqH1muTbRO3v472iVwyyMGBYGK3d) 即可发起手Q客户端申请加群 新武科大助手用户群(439648667)
//     *
//     * @param key 由官网生成的key
//     * @return 返回true表示呼起手Q成功，返回fals表示呼起失败
//     ******************/
//    public boolean joinQQGroup(String key) {
//        Intent intent = new Intent();
//        intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D" + key));
//        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        try {
//            startActivity(intent);
//            return true;
//        } catch (Exception e) {
//            // 未安装手Q或安装的版本不支持
//            return false;
//        }
//    }

    /****************
     *
     * 发起添加群流程。群号：武科大助手用户2群(219356613) 的 key 为： h_P5P68MHr6NEccTDUFQXR5VPJ_wXWCh
     * 调用 joinQQGroup(h_P5P68MHr6NEccTDUFQXR5VPJ_wXWCh) 即可发起手Q客户端申请加群 武科大助手用户2群(219356613)
     *
     * @param key 由官网生成的key
     * @return 返回true表示呼起手Q成功，返回false表示呼起失败
     ******************/
    public boolean joinQQGroup(String key) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26jump_from%3Dwebapi%26k%3D" + key));
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent);
            return true;
        } catch (Exception e) {
            // 未安装手Q或安装的版本不支持
            return false;
        }
    }




}
