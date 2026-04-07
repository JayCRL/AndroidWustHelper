package com.example.wusthelper.ui.activity;

import static com.example.wusthelper.utils.ResourcesUtils.getRealString;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.listener.OnOptionsSelectChangeListener;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.example.wusthelper.MyApplication;
import com.example.wusthelper.R;
import com.example.wusthelper.adapter.SchoolBusAdapter;
import com.example.wusthelper.base.activity.BaseActivity;
import com.example.wusthelper.bean.itembean.BusItemForShow;
import com.example.wusthelper.bean.javabean.SchoolBusBean;
import com.example.wusthelper.databinding.ActivitySchoolBusBinding;
import com.example.wusthelper.dbhelper.BusDBHelper;
import com.example.wusthelper.helper.SPTool;
import com.example.wusthelper.utils.ToastUtil;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

public class SchoolBusActivity extends BaseActivity<ActivitySchoolBusBinding> implements View.OnClickListener{
    public String BUS_STARTING = "com.android.linghang.wustcampus.Util.SharedKey.starting";
    public String BUS_ENDING = "com.android.linghang.wustcampus.Util.SharedKey.ending";
    private SchoolBusBean mBusBean;
    private SchoolBusAdapter mBusAdapter = new SchoolBusAdapter();
    private OptionsPickerView pvOptions;
    private List<BusItemForShow> mTimeList;
    private ArrayList<String> options1Items = new ArrayList<>();
    private ArrayList<ArrayList<String>> options2Items = new ArrayList<>();
    private ArrayList<ArrayList<ArrayList<String>>> options3Items = new ArrayList<>();
    private String searchStart;
    private String searchEnd;
    private boolean searchIsWork;
    private static final String TAG = "SchoolBusActivity";

    public static Intent newInstance(Context context) {
        Intent intent = new Intent(context, SchoolBusActivity.class);
        return intent;
    }

    @Override
    public void initView() {
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        changeStatusBarTextColor(false);
        initListener();
        getBinding().rvSchoolBus.setLayoutManager(new LinearLayoutManager(SchoolBusActivity.this, LinearLayoutManager.VERTICAL, false));
        getBinding().rvSchoolBus.setAdapter(mBusAdapter);
        setSupportActionBar(getBinding().toolbarBus);

        getOptionsData();
        initDate();
    }

    private void initListener() {
        getBinding().relativeBus.setOnClickListener(this);
        getBinding().toolbarBus.setNavigationOnClickListener( new View.OnClickListener( ) {
            @Override
            public void onClick(View v) {
                finish();
            }
        } );
    }

    @Override
    public void onClick(View v) {
        if(v.equals(getBinding().relativeBus)){
            pvOptions.show();
        }
    }

    public void initDate() {
        BusDBHelper.initBusDB();
        mTimeList = new ArrayList<>();
        refreshInterFace(SPTool.get(BUS_STARTING, getRealString(R.string.huangjiahu)),
                SPTool.get(BUS_ENDING, getRealString(R.string.qingshan)),
                true);
        initOptionPicker();
    }

    private void refreshInterFace(String startStr, String endStr, boolean iw) {
        searchStart = startStr;
        searchEnd = endStr;
        searchIsWork = iw;

        SPTool.put(BUS_STARTING, startStr);
        SPTool.put(BUS_ENDING, endStr);

        getBinding().tvBusStart.setText(searchStart);
        getBinding().tvBusEnd.setText(searchEnd);
        if (searchIsWork) {
            getBinding().tvBusWorkday.setText("周一至周五");
        } else {
            getBinding().tvBusWorkday.setText("周末");
        }

        mBusBean = queryBus(searchStart, searchEnd, searchIsWork ? 1 :0);
        setRecyclerView();
    }

    private void setRecyclerView() {
        if (mBusBean == null) {
            //没有找到对应的校车表
            ToastUtil.show("没有找到对应的校车表");
        } else {
            mTimeList.clear();
            for (int i = 0;i < mBusBean.getHours().size();++i) {
                Log.d(TAG, "-->mBusBean.getHours().get(i)-->" + mBusBean.getHours().get(i));
                BusItemForShow busTime = new BusItemForShow(Integer.parseInt(mBusBean.getHours().get(i)), Integer.parseInt(mBusBean.getMinutes().get(i)));
                mTimeList.add(busTime);
            }

            if (mBusAdapter == null) {
                mBusAdapter = new SchoolBusAdapter();
            } else {
                mBusAdapter.setList(mTimeList);
            }
//            getBinding().rvSchoolBus.setAdapter(mBusAdapter);
        }
    }

    private SchoolBusBean queryBus(String s, String e, int w) {
        List<SchoolBusBean> beans = LitePal.where("starting = ? and destination = ? and isWorkDay = ?",s, e, String.valueOf(w)).find(SchoolBusBean.class);
        if (beans.size() > 0) {
            Log.d(TAG, "----->bean.size()-->" + beans.size());
            return beans.get(0);
        } else {
            Log.d(TAG, "----->bean.size()-->" + beans.size());
            return null;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initOptionPicker() {

        //条件选择器初始化
        int index0, index1;
        index0 = options1Items.indexOf(searchStart);
        Log.d(TAG, "-----+++++>index 0 " + index0);
        index1 = options2Items.get(index0).indexOf(searchEnd);
        Log.d(TAG, "-----+++++>index 1 " + index1);

        pvOptions = new OptionsPickerBuilder(this, new OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int options2, int options3, View v) {
                //返回的分别是三个级别的选中位置
                refreshInterFace(options1Items.get(options1), options2Items.get(options1).get(options2), options3 == 0);
            }
        })
                .setTitleText("校区选择")
                .setContentTextSize(18)//设置滚轮文字大小
                .setDividerColor(Color.LTGRAY)//设置分割线的颜色
                .setSelectOptions(index0, index1, 0)//默认选中项
                .setBgColor(Color.WHITE)
                .setTitleBgColor(Color.WHITE)
                .setTitleColor(Color.GRAY)
                .setCancelColor(Color.GRAY)
                .setOutSideCancelable(true)
                .setSubmitColor(getResources().getColor(R.color.green_ok))
                .setTextColorCenter(getResources().getColor(R.color.green_ok))
                .setLabels("校区", "校区", "")
                .isRestoreItem(false)//切换时是否还原，设置默认选中第一项。
                .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
                .setBackgroundId(0x11101010) //设置外部遮罩颜色
                .setOptionsSelectChangeListener(new OnOptionsSelectChangeListener() {
                    @Override
                    public void onOptionsSelectChanged(int options1, int options2, int options3) {
                        String str = "options1: " + options1 + "\noptions2: " + options2 + "\noptions3: " + options3;
                        //Toast.makeText(SchoolBusActivity.this, str, Toast.LENGTH_SHORT).show();
                    }
                })
                .build();
        // pvOptions.setSelectOptions(1,1);
        /*pvOptions.setPicker(options1Items);//一级选择器*/
        pvOptions.setPicker(options1Items, options2Items, options3Items);//二级选择器
        /*pvOptions.setPicker(options1Items, options2Items,options3Items);//三级选择器*/

    }

    private void getOptionsData() {
        //选项1
        options1Items.add(getRealString(R.string.huangjiahu));
        options1Items.add(getRealString(R.string.qingshan));
        options1Items.add(getRealString(R.string.hongshan));

        //选项2
        ArrayList<String> mtempOptions2_1 = new ArrayList<>();
        mtempOptions2_1.add(getRealString(R.string.qingshan));
        mtempOptions2_1.add(getRealString(R.string.hongshan));
        ArrayList<String> mtempOptions2_2 = new ArrayList<>();
        mtempOptions2_2.add(getRealString(R.string.huangjiahu));
        mtempOptions2_2.add(getRealString(R.string.hongshan));
        ArrayList<String> mtempOptions2_3 = new ArrayList<>();
        mtempOptions2_3.add(getRealString(R.string.huangjiahu));
        mtempOptions2_3.add(getRealString(R.string.qingshan));
        options2Items.add(mtempOptions2_1);
        options2Items.add(mtempOptions2_2);
        options2Items.add(mtempOptions2_3);

        //选项3
        String workDay = "工作日";
        String weekend = "周末";
        ArrayList<ArrayList<String>> options3Items_01 = new ArrayList<ArrayList<String>>();
        ArrayList<ArrayList<String>> options3Items_02 = new ArrayList<ArrayList<String>>();
        ArrayList<ArrayList<String>> options3Items_03 = new ArrayList<ArrayList<String>>();

        ArrayList<String> options3Items_01_01=new ArrayList<String>();
        options3Items_01_01.add(workDay);
        options3Items_01_01.add(weekend);
        options3Items_01.add(options3Items_01_01);
        ArrayList<String> options3Items_01_02=new ArrayList<String>();
        options3Items_01_02.add(workDay);
        options3Items_01.add(options3Items_01_02);

        ArrayList<String> options3Items_02_01=new ArrayList<String>();
        options3Items_02_01.add(workDay);
        options3Items_02_01.add(weekend);
        options3Items_02.add(options3Items_02_01);
        ArrayList<String> options3Items_02_02=new ArrayList<String>();
        options3Items_02_02.add(workDay);
        options3Items_02.add(options3Items_02_02);

        ArrayList<String> options3Items_03_01=new ArrayList<String>();
        options3Items_03_01.add(workDay);
        options3Items_03.add(options3Items_03_01);
        ArrayList<String> options3Items_03_02=new ArrayList<String>();
        options3Items_03_02.add(workDay);
        options3Items_03.add(options3Items_03_02);

        options3Items.add(options3Items_01);
        options3Items.add(options3Items_02);
        options3Items.add(options3Items_03);

        /*--------数据源添加完毕---------*/
    }
}