package com.example.wusthelper;

import android.app.Application;
import android.content.Context;

import com.example.wusthelper.helper.SPTool;
import com.example.wusthelper.update.OKHttpUpdateHttpService;
import com.example.wusthelper.utils.ToastUtil;
import com.xuexiang.xupdate.XUpdate;
import com.xuexiang.xupdate.entity.UpdateError;
import com.xuexiang.xupdate.listener.OnUpdateFailureListener;
import com.xuexiang.xupdate.utils.UpdateUtils;

import org.jaaksi.pickerview.widget.DefaultCenterDecoration;
import org.jaaksi.pickerview.widget.PickerView;
import org.litepal.LitePal;

import static com.xuexiang.xupdate.entity.UpdateError.ERROR.CHECK_NO_NEW_VERSION;

/**
 * MyApplication
 * 全局获取context的方法，MyApplication.getContext()
 * 还进行了LitePal的初始化
 * 还有接入了TalkingData的SDK
 * */
public class MyApplication extends Application {

    private static Context mThis;
    private final static String LH="领航官网";
    private final static String BAIDU="百度手机助手";
    private final static String HUAWEI="华为应用市场";
    private final static String XIAOMI="小米应用市场";
    private final static String YINGYONGBAO="应用宝";
    private final static String APP_ID="A7652F31A3014FD68028B857B7931B2F";
    @Override
    public void onCreate() {
        super.onCreate( );
        mThis = this;
        LitePal.initialize(this);
        LitePal.getDatabase();
        initSPTool();
        initXUpdate();
//        TCAgent.LOG_ON=true;
//        // App ID: 在TalkingData创建应用后，进入数据报表页中，在“系统设置”-“编辑应用”页面里查看App ID。
//        // 渠道 ID: 是渠道标识符，可通过不同渠道单独追踪数据。
//        TCAgent.init(this, APP_ID, LH);
//        // 如果已经在AndroidManifest.xml配置了App ID和渠道ID，调用TCAgent.init(this)即可；或与AndroidManifest.xml中的对应参数保持一致。
//        TCAgent.setReportUncaughtExceptions(true);
        PickerView.sOutTextSize = 13;
        PickerView.sCenterTextSize = 15;
        PickerView.sCenterColor = getResources().getColor(R.color.colorClassText);
        PickerView.sOutColor = getResources().getColor(R.color.colorTabText);
        DefaultCenterDecoration.sDefaultLineWidth = 1;
        DefaultCenterDecoration.sDefaultLineColor = getResources().getColor(R.color.color_list_separator);
    }

    public static Context getContext(){
        return mThis;
    }

    private void initSPTool() {
        //初始化SharedPreferences
        SPTool.init(mThis,"Course");//初始化SPTool
    }

    private void initXUpdate() {
        XUpdate.get()
                .debug(true)
                .isWifiOnly(true)                                               //默认设置只在wifi下检查版本更新
                .isGet(true)                                                    //默认设置使用get请求检查版本
                .isAutoMode(false)                                              //默认设置非自动模式，可根据具体使用配置
                .param("versionCode", UpdateUtils.getVersionCode(this))         //设置默认公共请求参数
                .param("appKey", getPackageName())
                .setOnUpdateFailureListener(new OnUpdateFailureListener() {     //设置版本更新出错的监听
                    @Override
                    public void onFailure(UpdateError error) {
                        if (error.getCode() != CHECK_NO_NEW_VERSION) {          //对不同错误进行处理
                            ToastUtil.show(error.toString());
                        }
                    }
                })
                .supportSilentInstall(true)                                     //设置是否支持静默安装，默认是true
                .setIUpdateHttpService(new OKHttpUpdateHttpService())           //这个必须设置！实现网络请求功能。
                .init(this);                                                    //这个必须初始化
    }

}
