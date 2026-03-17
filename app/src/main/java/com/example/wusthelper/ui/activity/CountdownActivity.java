package com.example.wusthelper.ui.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemLongClickListener;
import com.example.wusthelper.R;
import com.example.wusthelper.adapter.CountdownAdapter;
import com.example.wusthelper.base.activity.BaseMvpActivity;
import com.example.wusthelper.databinding.ActivityCountdownBinding;
import com.example.wusthelper.helper.TimeTools;
import com.example.wusthelper.bean.javabean.CountDownBean;
import com.example.wusthelper.mvp.presenter.CountdownPresenter;
import com.example.wusthelper.mvp.view.CountdownView;
import com.example.wusthelper.utils.CountDownUtils;
import com.example.wusthelper.utils.ScreenUtils;
import com.example.wusthelper.utils.ToastUtil;

import org.jetbrains.annotations.NotNull;
import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class CountdownActivity extends BaseMvpActivity<CountdownView, CountdownPresenter, ActivityCountdownBinding>
        implements CountdownView, OnItemLongClickListener {


    //从小组件跳转到添加页面，就传入WIDGET
    public static final int WIDGET = 10;
    public static final int APP = 11;

    public static Intent newInstance(Context context) {
        return new Intent(context, CountdownActivity.class);
    }

    @Override
    public CountdownPresenter createPresenter() {
        return new CountdownPresenter();
    }

    @Override
    public CountdownView createView() {
        return this;
    }

    private CountdownAdapter adapter;

    private Dialog dialog;



    @Override
    protected void onStart() {
        super.onStart();
        new Thread(() -> {
            try {
                Thread.sleep(5*100);
                runOnUiThread(()->{
                    int kind = getIntent().getIntExtra("kind",APP);
                    Log.d(TAG, "initView: "+kind);
                    if(kind == WIDGET){
                        //添加倒计时事件
                        showDialog(null);
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        int kind = getIntent().getIntExtra("kind",APP);
//        Log.d(TAG, "initView: "+kind);
//        if(kind == WIDGET){
//            //添加倒计时事件
//            showDialog(null);
//        }
//    }

    @Override
    public void initView() {

        getPresenter().initPresenterData();

        //改变状态栏颜色
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));

        initListener();

        //初始化名人名言
        getBinding().tvCountdownSaying.setText(getPresenter().getRandomSaying());

        //初始化时间
        getBinding().tvCountdownDate.setText(TimeTools.getFormatToday());

        initRecycleView();

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    /**
     * 初始化监听
     */
    private void initListener() {
        //toolbar 返回点击事件
        getBinding().tbCountDown.setNavigationOnClickListener(v -> finish());

        getBinding().fabAdd.setOnClickListener(v -> {
            //添加倒计时事件
            showDialog(null);
        });

        getBinding().fabDelete.setOnClickListener(v -> {
            multipleSelectionDeletion();
        });

        getBinding().countdownSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    for(CountDownBean countDownBean : getPresenter().getCountDownData().countDownList) {
                        countDownBean.setCheck(true);
                        adapter.notifyDataSetChanged();
                    }
                }else {
                    for(CountDownBean countDownBean : getPresenter().getCountDownData().countDownList) {
                        countDownBean.setCheck(false);
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        });

        //打开二维码扫描
        getBinding().countdownScan.setOnClickListener(view -> {
            Intent intent = ScanActivity.newInstance(CountdownActivity.this);
            intent.putExtra("kind", ScanActivity.COUNTDOWN);
            startActivityForResult(intent, getPresenter().RESULT);
        });
    }

    /**
     * 初始化recycleView
     */
    private void initRecycleView() {
        adapter = new CountdownAdapter(this);
        adapter.setOnItemLongClickListener(this);
        getBinding().recyclerCountdown.setAdapter(adapter);
        getBinding().recyclerCountdown.setLayoutManager(new LinearLayoutManager(this));
    }

    /**
     * 扫描二维码的回调
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == getPresenter().RESULT) {
                final String onlyId = data.getStringExtra("onlyId");
                if (!LitePal.where("onlyId ==?", onlyId).find(CountDownBean.class).isEmpty()) {
                    ToastUtil.show("已有此倒计时");
                } else {
                    getPresenter().getShareCountdown(onlyId);
                }
            }
        }
    }

    /**
     * 数据为空时回调
     */
    @SuppressLint("SetTextI18n")
    @Override
    public void emptyResult() {
        getBinding().countdownNoContent.getRoot().setVisibility(View.VISIBLE);
        getBinding().fvCountdownValue.setVisibility(View.GONE);

        //显示进行个数
        getBinding().tvCountdownNumofTosize.setText("正在进行 : " + getPresenter().getNumOfTODO());
    }


    private static final String TAG = "CountdownActivity";


    /**
     *刷新数据回调
     */
    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    @Override
    public void refreshData() {
        getBinding().countdownNoContent.getRoot().setVisibility(View.GONE);
//                            rv_describe.setVisibility(View.GONE);
        getBinding().fvCountdownValue.setVisibility(View.VISIBLE);

        adapter.setList(getPresenter().getCountDownData().countDownList);
        adapter.notifyDataSetChanged();

        //显示进行个数
        getBinding().tvCountdownNumofTosize.setText("正在进行 : " + getPresenter().getNumOfTODO());
    }

    /**
     * adapter 回调接口   处理长按事件
     * @param adapter
     * @param view
     * @param position
     * @return
     */
    @Override
    public boolean onItemLongClick(@NonNull @NotNull BaseQuickAdapter adapter, @NonNull @NotNull View view, int position) {
        Vibrator vib = (Vibrator)this.getSystemService(Service.VIBRATOR_SERVICE);
//			vibrator.vibrate(1000);//只震动一秒，一次
        long[] pattern = {10,80};
        //两个参数，一个是自定义震动模式，
        //数组中数字的含义依次是静止的时长，震动时长，静止时长，震动时长。。。时长的单位是毫秒
        //第二个是“是否反复震动”,-1 不重复震动
        //第二个参数必须小于pattern的长度，不然会抛ArrayIndexOutOfBoundsException
        vib.vibrate(pattern, -1);
        getBinding().fabAdd.setVisibility(View.GONE);
        getBinding().fabDelete.setVisibility(View.VISIBLE);
        getBinding().countdownSelect.setVisibility(View.VISIBLE);
        this.adapter.setIsLongClick(true);
        adapter.notifyDataSetChanged();
//        SweetAlertDialog alertDialog = new SweetAlertDialog(CountdownActivity.this, SweetAlertDialog.NORMAL_TYPE);
//        alertDialog.setContentText("确定删除此项倒计时？");
//        alertDialog.setConfirmText("确定");
//        alertDialog.setCancelText("取消");
//        alertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
//            @SuppressLint("NotifyDataSetChanged")
//            @RequiresApi(api = Build.VERSION_CODES.N)
//            @Override
//            public void onClick(SweetAlertDialog sweetAlertDialog) {
//
//                getPresenter().deleteCountDown(getPresenter().getCountDownData().countDownList.get(position), CountdownPresenter.NOT_NEED_SUM);
//                adapter.notifyDataSetChanged();
//                sweetAlertDialog.cancel();
//                //小组件广播
//                //CountDownWidgetProvider.sendREFESHBoradcast(getApplicationContext());
//            }
//        });
//        alertDialog.show();
        return false;
    }

    /**
     * 显示修改或添加倒计时弹窗
     * @param count  若count为空   表示添加倒计时    反之   则表示修改倒计时
     */
    public void showDialog(CountDownBean count) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_countdown_add, null);
        builder.setView(view);
        dialog = builder.create();

        initDialogListener(view, count);

        dialog.show();
        dialogSettings();
        Log.d(TAG, "showDialog: ");
    }

    /**
     * dialog设置大小  位置  (必须在show之后设置)
     */
    private void dialogSettings() {
        Window window = dialog.getWindow();
        window.setBackgroundDrawableResource(android.R.color.transparent);
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.height = ScreenUtils.dp2px(280);
        layoutParams.width = ScreenUtils.dp2px(320);
        layoutParams.gravity = Gravity.CENTER;
        window.setAttributes(layoutParams);
    }

    /**
     * 初始化View的各个组件的点击事件
     *
     * @param view
     * @param count
     */
    private void initDialogListener(View view, CountDownBean count) {
        ImageView imgBck, QRCode;
        TextView textTime, textTitle;
        EditText editName, editNote;
        Button btnOk;
        imgBck = view.findViewById(R.id.img_countdown_add_back);
        textTitle = view.findViewById(R.id.text_countdown_add_title);
        editName = view.findViewById(R.id.edit_countdown_add_name);
        editNote = view.findViewById(R.id.edit_countdown_add_note);
        textTime = view.findViewById(R.id.text_countdown_add_time);
        btnOk = view.findViewById(R.id.btn_countdown_add_ok);
        QRCode = view.findViewById(R.id.image_QR_code_countdown);
        imgBck.setOnClickListener(v -> {
            dialog.cancel();
        });

        textTime.setOnClickListener(v -> {
            showTimePicker(textTime);
        });
        if (count != null) {
            textTitle.setText("修改倒计时");
            editName.setText(count.getName());
            textTime.setText(CountDownUtils.getShowTime(count.getTargetTime()));
            editNote.setText(count.getNote());
            QRCode.setVisibility(View.VISIBLE);
            QRCode.setOnClickListener(v-> {
                Log.e(TAG, "initDialogListener: count isOnNet = "+count.isOnNet() );
                if (count.isOnNet()) {
                    Intent intent = CountDownShareActivity.newInstance(this,count.getOnlyId(),count.getName());
                    //dialog.cancel();  //(此处在跳转共享倒计时页面是否把当前dialog隐藏    看个人习惯)
                    startActivity(intent);
                }else{
                    ToastUtil.show("还没有上传哟！");
                }
            });
        }else{
            QRCode.setVisibility(View.INVISIBLE);
        }

        btnOk.setOnClickListener(v -> {
            String name = editName.getText().toString();
            String note = editNote.getText().toString();
            if ("".equals(name)) {
                showAlert("请输入课程名称！");
                return;
            } else if (TextUtils.isEmpty(textTime.getText().toString())) {
                showAlert("请输入考试时间！");
                return;
            }

            if(note.equals("")){
                note = "无";
            }
            CountDownBean countDownBean = new CountDownBean(name, note,
                    System.currentTimeMillis(),
                    CountDownUtils.getTimeLong(textTime.getText().toString()));

            Log.e(TAG, "initDialogListener: onclick  ！！！" );
            if (count == null) {
                countDownBean.setId(new Random().nextInt());
                getPresenter().upload2Net(countDownBean, CountdownPresenter.NOT_NEED_SUM);
            } else {
                Log.e(TAG, "initDialogListener: change  ！！！" );
                countDownBean.setOnlyId(count.getOnlyId());
                getPresenter().changeCountDown(countDownBean, CountdownPresenter.NOT_NEED_SUM);
            }
        });
    }

    /**
     * 显示时间选择器
     * @param textTime
     */
    private void showTimePicker(TextView textTime) {
        TimePickerView pvTime = new TimePickerBuilder(CountdownActivity.this, (date, v) -> {
            long time = date.getTime();
            if (!CountDownUtils.isFuture(time)) {
                showAlert("请选择未来的一个时间！");
                return;
            }
            textTime.setText(CountDownUtils.getShowTime(time));
        })
                .setType(new boolean[]{true, true, true, true, true, false})
                .setLabel("年", "月", "日", "时", "分", "秒")//默认设置为年月日时分秒
                .isDialog(true) //默认设置false ，内部实现将DecorView 作为它的父控件。
                .build();
        pvTime.show();
    }

    @Override
    public void dialogDismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.cancel();
        }
    }

    /**
     * 显示错误的操作的dialog
     * @param content
     */
    private void showAlert(String content) {
        SweetAlertDialog alertDialog = new SweetAlertDialog(CountdownActivity.this, SweetAlertDialog.ERROR_TYPE);
        alertDialog.setTitle("提示");
        alertDialog.setContentText(content);
        alertDialog.setConfirmButton("确定", null);
        alertDialog.show();
    }

    /**
     * 长按多选删除逻辑
     */
    private void multipleSelectionDeletion() {
        boolean isCheckItem = false;
        for(CountDownBean countDownBean : getPresenter().getCountDownData().countDownList) {
            if(countDownBean.isCheck()) {
                isCheckItem = true;
                break;
            }
        }
        if(isCheckItem) {
            SweetAlertDialog alertDialog = new SweetAlertDialog(CountdownActivity.this, SweetAlertDialog.NORMAL_TYPE);
            alertDialog.setContentText("确定删除所选倒计时？");
            alertDialog.setConfirmText("确定");
            alertDialog.setCancelText("取消");
            alertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @SuppressLint("NotifyDataSetChanged")
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    List<CountDownBean> countDownBeans = getPresenter().getCountDownData().countDownList;
                    List<CountDownBean> newCountDownBeans = new ArrayList<>();
                    Log.d(TAG,"countDownBeans" + countDownBeans);
                    for(int i = 0; i <countDownBeans.size(); i++) {
                        if(countDownBeans.get(i).isCheck()) {
                            Log.d(TAG,"delete" + getPresenter().getCountDownData().countDownList.get(i));
                            getPresenter().deleteCountDown(countDownBeans.get(i), CountdownPresenter.NOT_NEED_SUM);
                        }
                    }
                    getBinding().countdownSelect.setChecked(false);
                    adapter.setIsLongClick(false);
                    adapter.notifyDataSetChanged();
                    getBinding().fabDelete.setVisibility(View.GONE);
                    getBinding().countdownSelect.setVisibility(View.GONE);
                    getBinding().fabAdd.setVisibility(View.VISIBLE);
                    sweetAlertDialog.cancel();
                    //小组件广播
                    //CountDownWidgetProvider.sendREFESHBoradcast(getApplicationContext());
                }
            });
            alertDialog.show();
        } else {
            adapter.setIsLongClick(false);
            adapter.notifyDataSetChanged();
            getBinding().fabDelete.setVisibility(View.GONE);
            getBinding().countdownSelect.setVisibility(View.GONE);
            getBinding().fabAdd.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 全选，取消
     */
//    private void selectAll() {
//        if(!selectAll) {
//            selectAll = true;
//            for(CountDownBean countDownBean : getPresenter().getCountDownData().countDownList) {
//                countDownBean.setCheck(true);
//                getBinding().countdownSelect.setImageResource(R.drawable.ic_baseline_check_circle_outline_24);
//                adapter.notifyDataSetChanged();
//            }
//        }else {
//            selectAll = false;
//            for(CountDownBean countDownBean : getPresenter().getCountDownData().countDownList) {
//                getBinding().countdownSelect.setImageResource(R.drawable.ic_baseline_radio_button_unchecked_24);
//                countDownBean.setCheck(false);
//                adapter.notifyDataSetChanged();
//            }
//        }
//    }

    @Override
    public void onBackPressed() {
        if(adapter.getIsLongClick()) {
            getBinding().countdownSelect.setChecked(false);
            adapter.setIsLongClick(false);
            for(CountDownBean countDownBean : getPresenter().getCountDownData().countDownList) {
                countDownBean.setCheck(false);
            }
            adapter.notifyDataSetChanged();
            getBinding().fabDelete.setVisibility(View.GONE);
            getBinding().countdownSelect.setVisibility(View.GONE);
            getBinding().fabAdd.setVisibility(View.VISIBLE);
        }else {
            finish();
        }
    }
}
