package com.example.wusthelper.ui.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.View;

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.example.wusthelper.MyApplication;
import com.example.wusthelper.R;
import com.example.wusthelper.base.activity.BaseActivity;
import com.example.wusthelper.databinding.ActivityAboutUsBinding;
import com.example.wusthelper.helper.SharePreferenceLab;
import com.example.wusthelper.request.WustApi;
import com.example.wusthelper.utils.AppVersionUtils;
import com.example.wusthelper.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;

public class AboutActivity extends BaseActivity<ActivityAboutUsBinding>
        implements View.OnClickListener{

    private OptionsPickerView<String> RefreshOptions;

    public static Intent newInstance(Context context) {
        return new Intent(context, AboutActivity.class);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void initView() {
        getWindow().setStatusBarColor(Color.WHITE);
        onSetOnListener();

        getBinding().tvAboutUsVersion.setText("v" + AppVersionUtils.getVersionName(AboutActivity.this));
    }

    private void onSetOnListener() {

        getBinding().ivBack.setOnClickListener(this);
        getBinding().llAuthor.setOnClickListener(this);
        getBinding().llPolicy.setOnClickListener(this);
        getBinding().llOpenNotice.setOnClickListener(this);
        getBinding().llRefreshWidget.setOnClickListener(this);
        getBinding().llOfficialWebsite.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        if(v.equals(getBinding().ivBack)){
           finish();
        }else if(v.equals(getBinding().llAuthor)){
            startActivity(new Intent(this, AuthorActivity.class));
        }else if(v.equals(getBinding().llPolicy)){
            OtherWebActivity.setName("用户协议与隐私政策");
            OtherWebActivity.setUrl(WustApi.PRIVACY_URL);
            Intent privacy = OtherWebActivity.getInstance(getBaseContext());
            startActivity(privacy);
        }else if(v.equals(getBinding().llRefreshWidget)){
            startActivity(new Intent(this, WidgetSettingsActivity.class));
//            initOptionPicker();
//            RefreshOptions.show();
        }else if(v.equals(getBinding().llOfficialWebsite)){
            Intent official_web_intent = OtherWebActivity.getInstance(getBaseContext());
            OtherWebActivity.setUrl(WustApi.OFFICIALWEB_URL);
            OtherWebActivity.setName("领航官网");
            startActivity(official_web_intent);
        }
    }


    /****************
     *
     * 发起添加群流程。群号：新武科大助手用户群(439648667) 的 key 为： BuEiEqH1muTbRO3v472iVwyyMGBYGK3d
     * 调用 joinQQGroup(BuEiEqH1muTbRO3v472iVwyyMGBYGK3d) 即可发起手Q客户端申请加群 新武科大助手用户群(439648667)
     *
     * @param key 由官网生成的key
     * @return 返回true表示呼起手Q成功，返回fals表示呼起失败
     ******************/
    public boolean joinQQGroup(String key) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D" + key));
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent);
            return true;
        } catch (Exception e) {
            // 未安装手Q或安装的版本不支持
            return false;
        }
    }

    private void initOptionPicker()
    {
        final List<String> options = new ArrayList<>();
        options.add("倒计时");
        options.add("每日课表");
        options.add("一周课表");
        RefreshOptions = new OptionsPickerBuilder(this, new OnOptionsSelectListener() {

            @Override
            public void onOptionsSelect(int options1, int options2, int options3, View v) {
                String s1 =options.get(options1);
                if(s1.equals("倒计时")){
                    SharePreferenceLab.getInstance().setWidgetCountdownBgPath(MyApplication.getContext(),"");
                }else if(s1.equals("每日课表")){
                    SharePreferenceLab.getInstance().setWidgetTodayBgPath(MyApplication.getContext(),"");
                }else if(s1.equals("一周课表")){
                    SharePreferenceLab.getInstance().setWidgetWeekBgPath(MyApplication.getContext(),"");
                }
                ToastUtil.showShortToastCenter("小组件的背景已经重置，请重新添加小组件");
            }

        }).setTitleText("选择重置的组件")
                .setContentTextSize(20)
                .setDividerColor(Color.LTGRAY)
                .setBgColor(Color.WHITE)
                .setTitleBgColor(Color.WHITE)
                .setTitleColor(Color.GRAY)
                .setCancelColor(Color.GRAY)
                .setOutSideCancelable(true)
                .setSubmitColor(getResources().getColor(R.color.green_ok))
                .setTextColorCenter(getResources().getColor(R.color.green_ok))
                .build();
        RefreshOptions.setPicker(options);
        RefreshOptions.setSelectOptions(0);
    }

}
