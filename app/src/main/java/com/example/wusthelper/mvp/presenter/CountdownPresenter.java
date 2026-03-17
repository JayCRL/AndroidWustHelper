package com.example.wusthelper.mvp.presenter;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.wusthelper.base.BasePresenter;
import com.example.wusthelper.bean.javabean.CountDownAddData;
import com.example.wusthelper.bean.javabean.CountDownBean;
import com.example.wusthelper.bean.javabean.CountDownChangeData;
import com.example.wusthelper.bean.javabean.CountDownData;
import com.example.wusthelper.bean.javabean.data.BaseData;
import com.example.wusthelper.mvp.model.CountdownModel;
import com.example.wusthelper.mvp.view.CountdownView;
import com.example.wusthelper.request.okhttp.exception.OkHttpException;
import com.example.wusthelper.request.okhttp.listener.DisposeDataListener;
import com.example.wusthelper.utils.CountDownUtils;
import com.example.wusthelper.utils.ToastUtil;

import org.litepal.LitePal;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class CountdownPresenter extends BasePresenter<CountdownView> {

    /**
     * 写一下这个类的生命周期    为了能看懂
     * <p>
     * 最重要的是handle的理解
     * <p>
     * 首先   initPresenter（） 查本地数据库  找到与网络未同步的所有倒计时
     * <p>
     * 然后   deleteNotNet（）  便利离线删除但没有上传到云端的数据   然后删除  用msg.what 来计数
     * 当该删除的都删除了   走到handle的第二个if语句   开始同步改变的数据
     * 然后上传没有同步的数据
     * 最后请求倒计时列表   刷新数据
     */

    private CountdownModel model;

    private final String[] sayings =
            {
                    "正当利用时间!你要理解什么，不要舍近求远。",
                    "放弃时间的人，时间也放弃他。",
                    "时间就是生命，时间就是速度，时间就是力量。",
                    "在所有批评家中，最伟大、最正确，最天才的是时间。",
                    "最不善于利用时间的人最爱抱怨时光短暂。",
                    "时间比理性创造出更多的皈依者。",
                    "人生天地之间，若白驹过隙，忽然而已。"
            };

    //倒计时数据
    private CountDownData countDownData;

    public CountDownData getCountDownData() {
        return countDownData;
    }

    //已完成的个数
    private int numOfTODO;

    public int getNumOfTODO() {
        return numOfTODO;
    }

    //处理activity回调标记
    public final int RESULT = 6;

    public CountdownPresenter() {
        model = new CountdownModel();
    }

    public static final int NEED_SUM = 1;
    public static final int NOT_NEED_SUM = 2;

    //数据库中删除标记为为1的数据集
    private List<CountDownBean> myDeleteCountDownList;
    //数据库中上传标记位为0的数据集
    private List<CountDownBean> myNotUpload;
    //数据库中改变标记位为1的数据集
    private List<CountDownBean> myChangeCountDownList;


    public String getRandomSaying() {
        return sayings[(int) (Math.random() * 1000) % sayings.length];
    }

    public void getShareCountdown(String onlyId) {
        this.model.getShareCountdown(onlyId, new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                final BaseData baseBean = (BaseData) responseObj;
                if (baseBean.getCode().equals("" + 10000)) {
                    ToastUtil.show("添加成功");
                    //同步网络
                    getCountDownFormNet();
                } else {
                    ToastUtil.show("添加失败" + baseBean.getMsg());
                }
            }

            @Override
            public void onFailure(Object reasonObj) {
                ToastUtil.show("请求失败");
            }
        });
    }

    private static final String TAG = "CountdownPresenter";

    /**
     * 从网上获取数据
     */
    public void getCountDownFormNet() {
        this.model.getCountDownFormNet(new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                countDownData = (CountDownData) responseObj;
                if (countDownData.getCode().equals("" + 10000)) {
                    //处理倒计时数据
                    for (int i = 0; i < countDownData.countDownList.size(); i++) {
                        @SuppressLint("SimpleDateFormat")
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date date;
                        try {
                            date = sdf.parse(countDownData.countDownList.get(i).getCrateTimeString());
                            long Time = date.getTime();
                            countDownData.countDownList.get(i).setCreateTime(Time);
                            countDownData.countDownList.get(i).setOnNet(true);
                            sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                            date = sdf.parse(countDownData.countDownList.get(i).getTargetTimeString());
                            Time = date.getTime();
                            countDownData.countDownList.get(i).setTargetTime(Time);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }

                    //保存到数据库
                    List<CountDownBean> myDBList = LitePal.where(" isDelete==0 ").find(CountDownBean.class);
                    if (myDBList.size() != countDownData.countDownList.size()) {
                        LitePal.deleteAll(CountDownBean.class);
                        for (CountDownBean countDown : countDownData.countDownList) {
                            countDown.assignBaseObjId(0);
                            countDown.setOnNet(true);
                            countDown.save();
                        }
                    }
                    refreshData(null);
                }
            }

            @Override
            public void onFailure(Object reasonObj) {
                ToastUtil.show("获取列表失败");
                refreshData(model.getCountDownForDB());
            }
        });
    }

    /**
     * countDownBeans为空时  表示数据从网络上获取   不需要重新加载数据
     * countDownBeans不为空时  ，表示数据从本地数据库获取   需要重新加载
     * 因为adapter加载数据之后  地址绑定到了adapter  所以不能浅复制
     *
     * @param countDownBeans
     */
    private void refreshData(List<CountDownBean> countDownBeans) {
        if(getView() != null){
            getView().dialogDismiss();
            if (countDownBeans != null) {
                countDownData.countDownList = countDownBeans;
            }
            sortList();
            if (countDownData.countDownList == null || countDownData.countDownList.isEmpty()) {
                getView().emptyResult();
            } else {
                getView().refreshData();
            }
        }
    }

    /**
     * 给倒计时列表排序
     */
    private void sortList() {
        //完成倒计时集
        List<CountDownBean> finishlist = new ArrayList<>();
        //未完成倒计时集
        List<CountDownBean> Todolist = new ArrayList<>();
        for (CountDownBean count : countDownData.countDownList) {
            if (!CountDownUtils.checkState(count.getTargetTime())) {
                finishlist.add(count);
            } else {
                Todolist.add(count);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            //按照完成时间  从大到小排序列表
            Collections.sort(finishlist, (o1, o2) -> {
                if (o1.getTargetTime() > o2.getTargetTime()) {
                    return -1;
                } else if (o1.getTargetTime() < o2.getTargetTime()) {
                    return 1;
                }
                return 0;
            });
            //按照完成时间  从小到大排序列表
            Collections.sort(Todolist, (o1, o2) -> {
                if (o1.getTargetTime() > o2.getTargetTime()) {
                    return 1;
                } else if (o1.getTargetTime() < o2.getTargetTime()) {
                    return -1;
                }
                return 0;
            });
        }

        numOfTODO = Todolist.size();
        countDownData.countDownList.clear();

        countDownData.countDownList.addAll(Todolist);
        countDownData.countDownList.addAll(finishlist);
    }


    @Override
    public void initPresenterData() {
        myChangeCountDownList = this.model.getCountdownChangeFromDB();
        myNotUpload = this.model.getCountdownAddFromDB();
        myDeleteCountDownList = this.model.getCountdownDeleteFromDB();
        countDownData = new CountDownData();
        deleteNotNet();
    }

    //计数器  (放在这里感觉会好找一点    其他地方也没有使用过)
    private int times = 0;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == myDeleteCountDownList.size()) {
                times = 0;
                addNotOnNet();
            }
            else if (msg.arg1 == myNotUpload.size()) {
                times = 0;
                changeNotNet();
            }
            else if (msg.arg2 == myChangeCountDownList.size()) {
                times = 0;
                getCountDownFormNet();
            }
        }
    };

    /**
     * 删除不同步的倒计时
     */
    private void deleteNotNet() {
        if (!myDeleteCountDownList.isEmpty()) {
            for (CountDownBean bean : myDeleteCountDownList) {
                deleteCountDown(bean, NEED_SUM);
            }
            //model.deleteAlreadyDeleteFromDB();
        } else {
            Message message = Message.obtain();
            message.what = times;
            message.arg1 = -1;
            message.arg2 = -1;
            handler.sendMessage(message);
        }
    }

    /**
     * 添加与网络不同步的倒计时
     */
    private void addNotOnNet() {
        if (!myNotUpload.isEmpty()) {
            for (CountDownBean countDownBean : myNotUpload) {
                upload2Net(countDownBean, NEED_SUM);
            }
            //model.deleteOnNetFromDB();
        } else {
            Message message = Message.obtain();
            message.arg1 = times;
            message.what = -1;
            message.arg2 = -1;
            handler.sendMessage(message);
        }
    }

    /**
     * 改变与网络不同步的倒计时
     */
    private void changeNotNet() {
        if (!myChangeCountDownList.isEmpty()) {
            for (CountDownBean countDown : myChangeCountDownList) {
                changeCountDown(countDown, NEED_SUM);
            }
            //model.deleteChangedFromDB();
        } else {
            Message message = Message.obtain();
            message.arg2 = times;
            message.arg1 = -1;
            message.what = -1;
            handler.sendMessage(message);
        }
    }

    /**
     * 这里的kind 只是为了计数   用来实现本地数据与网络同步的功能   因为其逻辑与相应的操做  逻辑差不多   所以加一个标志为来区分
     * NEED_SUM 指需要计数
     * NOT_NEED_SUM 不需要计数
     * <p>
     * 下同⬇⬇⬇⬇⬇⬇⬇⬇⬇⬇⬇⬇⬇⬇
     *
     * @param countDownBean
     * @param kind
     */
    public void deleteCountDown(CountDownBean countDownBean, int kind) {

        this.model.deleteCountDownFromNet(countDownBean.getOnlyId(), new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                BaseData baseData = (BaseData) responseObj;
                if (baseData.getCode().equals("" + 10000)) {
                    ToastUtil.show("删除成功");
                    model.deleteCountdownFromDB(countDownBean.getOnlyId());
                } else {
                    ToastUtil.show("删除失败 因为" + baseData.getMsg());
                }
                if (kind == NEED_SUM) {
                    times++;
                    Message message = Message.obtain();
                    message.what = times;
                    message.arg1 = -1;
                    message.arg2 = -1;
                    handler.sendMessage(message);
                } else {
                    countDownData.countDownList.remove(countDownBean);
                    refreshData(null);
                }
            }

            @Override
            public void onFailure(Object reasonObj) {
                countDownBean.setDelete(true);
                model.deleteCountdownFromDB(countDownBean.getOnlyId());
                countDownBean.save();
                //失败是就从数据库中拿信息
                refreshData(model.getCountDownForDB());
                ToastUtil.show("删除失败");
            }
        });
    }

    public void changeCountDown(final CountDownBean countDownBean, int kind) {
        Log.e(TAG, "changeCountDown: save " );

        this.model.changeCountDown(countDownBean, new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
               // Log.e(TAG, "onSuccess: "+responseObj );
                CountDownChangeData countDownChangeData = (CountDownChangeData) responseObj;
                if (countDownChangeData.getCode().equals("" + 10000)) {
                    Date date;
                    try {
                        countDownChangeData.countDownBean.setCreateTime(countDownBean.getCreateTime());
                        @SuppressLint("SimpleDateFormat")
                        SimpleDateFormat sdf = CountDownUtils.format;
                        date = sdf.parse(countDownChangeData.countDownBean.getTargetTimeString());
                        long Time = date.getTime();
                        countDownChangeData.countDownBean.setTargetTime(Time);

                        model.deleteCountdownFromDB(countDownBean.getOnlyId());
                        countDownChangeData.countDownBean.setOnNet(true);
                        countDownChangeData.countDownBean.save();
                        ToastUtil.show("保存成功");
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if (kind == NEED_SUM) {
                        times++;
                        Message message = Message.obtain();
                        message.arg2 = times;
                        message.arg1 = -1;
                        message.what = -1;
                        handler.sendMessage(message);
                    } else {
                        //删除之前的
                        countDownData.countDownList.remove(countDownBean);
                        //添加新的
                        countDownData.countDownList.add(countDownChangeData.countDownBean);
                        refreshData(null);
                    }
                }
            }

            @Override
            public void onFailure(Object reasonObj) {
                countDownBean.setChange(false);
                model.deleteCountdownFromDB(countDownBean.getOnlyId());
                countDownBean.save();
                refreshData(model.getCountDownForDB());
                ToastUtil.show("改变失败");
            }
        });
    }

    public void upload2Net(final CountDownBean countDownBean, int kind) {
        Log.e(TAG, "upload2Net: save " );
        this.model.upload2Net(countDownBean, new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                Log.e(TAG, "onSuccess: "+responseObj );
                CountDownAddData countDownAddData = (CountDownAddData) responseObj;
                if (countDownAddData.getCode().equals("" + 10000)) {
                    times++;
                    countDownBean.setOnlyId(countDownAddData.onlyId);
                    countDownBean.setOnNet(true);
                    countDownBean.save();
                    ToastUtil.show("上传成功");
                    if (kind == NEED_SUM) {
                        Message message = Message.obtain();
                        message.arg1 = times;
                        message.what = -1;
                        message.arg2 = -1;
                        handler.sendMessage(message);
                    } else {
                        //将其添加到列表中
                        countDownData.countDownList.add(countDownBean);
                        refreshData(null);
                    }
                } else {
                    countDownData.countDownList.add(countDownBean);
                    ToastUtil.show("上传失败");
                }
            }
            @Override
            public void onFailure(Object reasonObj) {
                countDownBean.setOnNet(false);
                model.deleteCountdownFromDB(countDownBean.getOnlyId());
                countDownBean.save();
                refreshData(model.getCountDownForDB());
                ToastUtil.show("上传失败");
            }
        });
    }

//
//    public static final int ADD_COUNTDOWN = 1;
//    public static final int CHANGE_COUNTDOWN = 1;
//
//    public Dialog createDialog(int kind){
//        if(kind == ADD_COUNTDOWN){
//
//        }
//    }
}
