package com.example.wusthelper.ui.fragment.mainviewpager;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.GridLayoutAnimationController;
import android.view.animation.LayoutAnimationController;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bigkoo.pickerview.view.OptionsPickerView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.example.wusthelper.MyApplication;
import com.example.wusthelper.R;
import com.example.wusthelper.adapter.CourseAdapter;
import com.example.wusthelper.adapter.DateAdapter;
import com.example.wusthelper.adapter.WeekAdapter;
import com.example.wusthelper.base.fragment.BaseMvpFragment;
import com.example.wusthelper.bean.javabean.CourseBean;
import com.example.wusthelper.bindeventbus.BindEventBus;
import com.example.wusthelper.bindeventbus.coursefragment.CurrentWeekMessage;
import com.example.wusthelper.dbhelper.CourseDB;
import com.example.wusthelper.helper.DrawableLab;
import com.example.wusthelper.helper.OnPopItemClickListener;
import com.example.wusthelper.helper.OptionPickHelper;
import com.example.wusthelper.helper.PopupWindowLabNew;
import com.example.wusthelper.helper.SharePreferenceLab;
import com.example.wusthelper.helper.MyDialogHelper;
import com.example.wusthelper.bean.itembean.DateItemForShow;
import com.example.wusthelper.bean.itembean.WeekItemForShow;
import com.example.wusthelper.databinding.FragmentCourseNewBinding;
import com.example.wusthelper.helper.TimeTools;
import com.example.wusthelper.mvp.view.CourseFragmentView;
import com.example.wusthelper.bean.itembean.CourseListForShow;
import com.example.wusthelper.mvp.presenter.CoursePagePresenter;
import com.example.wusthelper.request.RequestCenter;
import com.example.wusthelper.ui.activity.ChangeScheduleSetting;
import com.example.wusthelper.ui.activity.CourseBgSettingActivity;
import com.example.wusthelper.ui.activity.FontSizeSettingActivity;
import com.example.wusthelper.ui.activity.ImportCalendarActivity;
import com.example.wusthelper.ui.activity.ScanActivity;
import com.example.wusthelper.utils.CourseShareCodec;
import com.example.wusthelper.ui.dialog.SetCurrentWeekDialogFragment;
import com.example.wusthelper.utils.ToastUtil;
import com.zhihu.matisse.Matisse;


import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

//@BindEventBus是指这个Fragment要重写EventBus的接收方法，目前有选择当前周数以后再刷新课表
@BindEventBus
public class CourseFragment extends BaseMvpFragment<CourseFragmentView, CoursePagePresenter, FragmentCourseNewBinding>
        implements CourseFragmentView, View.OnClickListener, OnPopItemClickListener {

    private static final String TAG = "CourseFragment";

    private static final int REQUEST_SCAN = 2;

    /**
     * UI
     * */
    private final CourseAdapter courseAdapter = new CourseAdapter();
    private final DateAdapter dateAdapter = new DateAdapter();
    private final WeekAdapter weekAdapter = new WeekAdapter();

    //用于周数列表的偏移
    private LinearLayoutManager layoutManager;

    //用于显示的Dialog
    private SweetAlertDialog dialog;

    //显示设置菜单的PopWindow
    private PopupWindow popupWindow;
    //MainActivity设置了隐藏状态栏，需要记录下状态栏高度，自己设置状态栏
    private int height;

    //学期选择View
    private OptionsPickerView<String> semesterOptions;
    //校区选择View
    private OptionsPickerView<String> campusOptions;

    private CallBackListener callBackListener;


    /**
     * Fragment单例加载模式
     * */
    public static CourseFragment newInstance() {
        return new CourseFragment();
    }

    /**
     * Mvp框架下创建Presenter
     * */
    @Override
    public CoursePagePresenter createPresenter() {
        return new CoursePagePresenter();
    }
    /**
     * Mvp框架下创建View
     * */
    @Override
    public CourseFragmentView createView() {
        return this;
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void initView() {
        Log.d(TAG, "initView: ");
       // getBinding().tbCourse.tvWeekSelected.setText("第" + getPresenter().masterWeek + "周");
        getPresenter().initPresenterData();
        callBackListener= (CallBackListener) getActivity();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
        initBackGround();
        if(SharePreferenceLab.getIsGraduate()){
            getBinding().tbCourse.ivScan.setVisibility(View.GONE);
        }
        //重新展示一下课表
        getPresenter().showDateList();
        getPresenter().showMonthText();
        getPresenter().showWeekText();
        getPresenter().showSchedule();
        getPresenter().refreshVacationState();
        getPresenter().showWeekTabList();
        setCampusTime(getPresenter().getTimeView(getContext()));
    }


    /**
     * 接下来重写的两个方法，是为了储存Fragment的状态栏高度height
     * */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null)
            height = savedInstanceState.getInt("statusBarHeight");

        Log.d(TAG, "onCreate: "+height);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("statusBarHeight", height);
    }

    /**
     * 懒加载进行的操作 视图的初始化等等
     * */
    @Override
    protected void lazyLoad() {

        Log.d(TAG, "lazyLoad: ");

        setListener();

        initStatusBar();

        initWeekItem();
        initCourseItem();
        initDate();

        checkIsRequestCourse();
    }

    /**
     * 从老版本升级到新版本，课程表数据库迁移了
     * 需要重新导入数据
     * */
    private void checkIsRequestCourse() {
        if(!SharePreferenceLab.getIsRequestCourse()){
            if(!CourseDB.isHavingCourse()){
                showConfirmDialog(null);
            }
        }
    }


    private void setListener() {
        getBinding().tbCourse.ivRefresh.setOnClickListener(this);
        getBinding().tbCourse.ivMore.setOnClickListener(this);
        getBinding().tbCourse.tvWeekSelected.setOnClickListener(this);
        getBinding().tbCourse.tvWeekArrow.setOnClickListener(this);
        getBinding().tbCourse.ivDay.setOnClickListener(this);
        //这个不能删除，ExpandableLinearLayout需要传入一个回调接口，否则会崩溃
        getBinding().elWeekSelect.setOnCollapseListener(() -> Log.d(TAG, "onCollapseEnd: "));

        getBinding().fabCourse.setOnClickListener(this);
        getBinding().tbCourse.ivScan.setOnClickListener(this);
    }



    /**
     * 加载背景图片，为了方便，直接把BinDing对象传了进去,在presenter进行相应的数据处理和显示
     * */
    private void initBackGround() {
        getPresenter().initBackGround(this,getBinding(),height);
    }

    /**
     * 加载周数recyclerView的Item,有从第一周 到 第二十五周
     * */
    private void initWeekItem() {
//        weekAdapter.setAnimationWithDefault(BaseQuickAdapter.AnimationType.SlideInRight);
        weekAdapter.setOnItemClickListener((adapter, view, position) -> {
            int week = position+1;
            //改变选择的周数，并通知P层发生了数据改变
            getPresenter().changeMasterWeek(week);
        });
        layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        getBinding().recyclerCourseWeek.setLayoutManager(layoutManager);
        getBinding().recyclerCourseWeek.setAdapter(weekAdapter);
        //设置周数recyclerview偏移到当前周
        layoutManager.scrollToPositionWithOffset(getPresenter().masterWeek - 1, 0);

    }

    /**
     * 加载日期recyclerview的Item数据，有从周一到周日 用了GridLayoutManager，为了让它帮忙计算view所占的大小
     * */
    private void initDate() {

        //设置recyclerView一行7个数据（周一到周日刚好七天）
        getBinding().recyclerCourseDate.setLayoutManager(new GridLayoutManager(getActivity(),
                7) {
            @Override
            public boolean canScrollVertically() {//禁用滑动事件
                return false;
            }
        });
        getBinding().recyclerCourseDate.setAdapter(dateAdapter);
    }

    /**
     * 加载课程表，课程表的数据是一个二维的，但是recyclerView 的参数只能是List链表，所以在进行逻辑判断的时候注意转化
     * /**
     * *  0  1  2  3  4  5  6
     * *  7  8  9 10 11 12 13
     * * 14 15 16 17 18 19 20
     * * 21 22 23 24 25 26 27
     * * 28 29 30 31 32 33 34
     * * 35 36 37 38 39 40 41
     * * 课程表List数据对应显示的Item二维数组
     */
    private void initCourseItem() {
        //注册数据类型与onBindViewHolder

        //courseAdapter.setAnimationWithDefault(BaseQuickAdapter.AnimationType.AlphaIn);

        setLayoutAnimation();
        //设置recyclerView一行7个数据（周一到周日刚好七天）
        getBinding().recyclerCourseContent.setLayoutManager(new GridLayoutManager(getActivity(),
                7));
        getBinding().recyclerCourseContent.setAdapter(courseAdapter);
    }

    /**
     * 给recycleView设置动画
     */
    private void setLayoutAnimation() {
        Animation animation = AnimationUtils.loadAnimation(getContext(),R.anim.anim_course_item);
        animation.setDuration(100);
        LayoutAnimationController controller = new LayoutAnimationController(animation);
        //设置每个item之间动画播放的间隔  为每个item动画时间的0.15
        controller.setDelay(0.1f);
        //设置每个item动画的播放顺序时随机播放
        controller.setOrder(GridLayoutAnimationController.ORDER_RANDOM);
        //设置动画
        getBinding().recyclerCourseContent.setLayoutAnimation(controller);
        //开始动画
        getBinding().recyclerCourseContent.startLayoutAnimation();
    }


    /**
     * 点击事件的回调接口，因为用了ViewBinding 所以和一般使用的switch 不同 使用的是if else 判断，据说在性能上几乎没有区别
     * */
    @Override
    public void onClick(View v) {

        //TitleCourse标签的Binding对象,需要重新绑定，此处声明是为了绑定点击事件
        if (getBinding().tbCourse.ivRefresh.equals(v)) {
            //确认刷新课表
            showConfirmDialog(null);
        } else if (getBinding().tbCourse.ivMore.equals(v)) {
            //加载popWindow弹窗,这个也有着点击事件的回调
            popupWindow = new PopupWindowLabNew.PopupWindowBuilder(getContext())
                    .setView(R.layout.pop_course_menu)
                    .setOnClickListener(this)
                    .setAnimationStyle(R.style.PopupMenuAnimation)
                    .setParent(getBinding().tbCourse.ivMore)
                    .create();

        } else if (getBinding().tbCourse.tvWeekArrow.equals(v) || getBinding().tbCourse.tvWeekSelected.equals(v)) {
            if (getBinding().elWeekSelect.isExpanded()) {
                getBinding().elWeekSelect.collapse();
                getBinding().tbCourse.tvWeekArrow.setText("ˇ");
            } else {
                getBinding().elWeekSelect.expand();
                getBinding().tbCourse.tvWeekArrow.setText("ˆ");
            }
        } else if (getBinding().tbCourse.ivDay.equals(v)) {
            if(SharePreferenceLab.getBarHide()) {
                MyDialogHelper.getSweetAlertDialog(getActivity(), SweetAlertDialog.NORMAL_TYPE, null,
                        "确认显示底部导航栏吗？", "显示", "取消", new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                callBackListener.setBarState(false);
                                SharePreferenceLab.setBarHide(false);
                                sweetAlertDialog.cancel();
                            }
                        }).show();

            } else {
                MyDialogHelper.getSweetAlertDialog(getActivity(), SweetAlertDialog.NORMAL_TYPE, null,
                        "确认隐藏底部导航栏吗？恢复需再点击此按钮。", "隐藏", "取消", new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                callBackListener.setBarState(true);
                                SharePreferenceLab.setBarHide(true);
                                sweetAlertDialog.cancel();
                            }
                        }).show();
            }

        } else if (getBinding().fabCourse.equals(v)){
            ToastUtil.show("已经回到当前周");
            getPresenter().changeMasterWeek(getPresenter().realWeek);
            layoutManager.scrollToPositionWithOffset(getPresenter().masterWeek - 1, 0);
        }else if(getBinding().tbCourse.ivScan.equals(v)){
            Intent intent = ScanActivity.newInstance(getActivity());
            intent.putExtra("kind",ScanActivity.COURSE);
            startActivityForResult(intent, REQUEST_SCAN);
        }
    }


    /**
     * popWindow的点击事件回调接口
     * 这个没有进行很好的移植，还保留了id 这样的形式*/
    @SuppressLint("NonConstantResourceId")
    @Override
    public void onPopItemClick(View view) {
        switch (view.getId()) {
            case R.id.ll_show_all_class:
                checkedChangePopWindow(view);
//                popupWindow.dismiss();
                break;
            case R.id.ll_select_semester:
                //初始化
                initSemesterOptionPicker();
                semesterOptions.show();
                popupWindow.dismiss();
                break;

            case R.id.ll_select_campus:
                initCampusOptionPicker();
                campusOptions.show();
                popupWindow.dismiss();
                break;

            case R.id.ll_set_current_week:
                SetCurrentWeekDialogFragment setCurrentWeekDialogFragment = new SetCurrentWeekDialogFragment();
                setCurrentWeekDialogFragment.show(getChildFragmentManager(), "");
                popupWindow.dismiss();
                break;

            case R.id.ll_change_background:
                startActivity(CourseBgSettingActivity.newInstance(getContext()));
                popupWindow.dismiss();
                break;

            case R.id.ll_change_setting:
                startActivity(ImportCalendarActivity.newInstance(getContext()));
                popupWindow.dismiss();
//                startActivity(ChangeScheduleSetting.newInstance(getContext()));
//                popupWindow.dismiss();
                break;
            case R.id.ll_set_fontSize:
                startActivity(FontSizeSettingActivity.newInstance(getContext()));
                popupWindow.dismiss();
                break;
            case R.id.ll_import_calendar:
//                startActivity(ImportCalendarActivity.newInstance(getContext()));
//                popupWindow.dismiss();
                break;
            default:
                break;

        }
    }

    /**
     * @param selectSemester
     * 展示课程表更新的确认 dialog
     * 带有参数，一般用于学期选择以后的获取课程表
     * 确认以后就进行网络请求
     * */
    private void showConfirmDialog(String selectSemester) {
        dialog = MyDialogHelper.getCommonDialog(getActivity(), SweetAlertDialog.BUTTON_CONFIRM,
                "更新课程表", "确认更新", sweetAlertDialog -> {
                    dialog.changeAlertType(SweetAlertDialog.PROGRESS_TYPE);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.setCancelable(false);
                    getPresenter().refreshScheduleData(selectSemester);
                });
        dialog.show();
    }



    /**
     * 课程recyclerView 数据变化后，对应的RecyclerView进行刷新
     * */
    @Override
    public void showCourseList(List<CourseListForShow> list) {
        courseAdapter.setList(list);
        courseAdapter.notifyDataSetChanged();
        if(getBinding() != null){
            getBinding().recyclerCourseContent.startLayoutAnimation();
        }
    }


    /**
     * 周数recyclerView 数据变化后，对应的RecyclerView进行刷新
     * */
    @Override
    public void showWeekList(List<WeekItemForShow> list) {
        Log.d(TAG, "notifyWeekDataSetChanged: ");
        weekAdapter.setList(list);
        weekAdapter.notifyDataSetChanged();
    }

    /**
     * 日期recyclerView 数据变化后，对应的RecyclerView进行刷新
     * */
    @Override
    public void showDataList(List<DateItemForShow> list) {
        dateAdapter.setList(list);
        dateAdapter.notifyDataSetChanged();

        TimeTools.getFormatHours();
        TimeTools.getFormatMinutes();
    }

    /**
     * 设置月份的字体显示
     * */
    @Override
    public void setMonthText(String monthText) {
        Log.e(TAG, "qzy146MonthText "+monthText);
        getBinding().tvMonth.setText(monthText);
    }

    /**
     * 右上角黑色字体显示的周数，改变数据的时候调用
     * */
    @Override
    public void setWeeksText(String weeksText) {
        Log.e(TAG, "qzy146WeeksText "+weeksText);
        getBinding().tbCourse.tvWeekSelected.setText(weeksText);
    }

    @Override
    public void showNotThisWeek() {
        getBinding().tbCourse.tvNotThisWeek.setVisibility(View.VISIBLE);
    }

    @Override
    public void removeNotThisWeek() {
        getBinding().tbCourse.tvNotThisWeek.setVisibility(View.GONE);
    }

    /**
     * 展示一个Dialog 参数由Presenter 传入
     * */
    @Override
    public void showCommonDialog(int DialogType, String title, String contentText, String confirmText,
                                 SweetAlertDialog.OnSweetClickListener listener) {
        dialog = MyDialogHelper.getCommonDialog(getContext(), DialogType,
                title, contentText, confirmText);
        dialog.show();
    }


    @Override
    public void cancelDialog() {
        dialog.cancel();
    }

    @Override
    public void showSemesterOptionPicker(String realSemester, String masterSemester, List<String> options) {
        //第二个参数 当前学期 用于显示在选择器顶部
        //第三个参数是 回调接口 ，即选择器选择以后，触发getPresenter().onSemesterOptionsSelect(options1))
        semesterOptions = OptionPickHelper.getSemesterOption(getContext(), "最新学期："+realSemester,
                (options1, options2, options3, v) -> {
                    final String semester = getPresenter().initOptions().get(options1);
                    showConfirmDialog(semester);
                });
        //设置选项
        semesterOptions.setPicker(options);
        semesterOptions.setSelectOptions(options.indexOf(masterSemester));

    }

    @Override
    public void showCampusOptionPicker(int campus, List<String> options) {
        campusOptions = OptionPickHelper.getCampusOption(getContext(), (options1, options2, options3, v) -> {
            String Campus=getPresenter().initCampusOptions().get(options1);
            getPresenter().setCampus(Campus);
            setCampusTime(getPresenter().getTimeView(getContext()));
        });
        campusOptions.setPicker(options);
        campusOptions.setSelectOptions(getPresenter().campus-1);
    }

    /**
     * 设置校区后调用，更新左侧的时间列表
     * */
    @Override
    public void setCampusTime(View view) {
        getBinding().classesTime.removeAllViews();
        getBinding().classesTime.addView(view);
    }

    @Override
    public void showFloatingActionButton(int visibility) {
        getBinding().fabCourse.setVisibility(visibility);
    }

    @Override
    public void showHaveNewSemester() {
        getBinding().tvCourseNoticeNewSemester.setVisibility(View.VISIBLE);
    }

    @Override
    public void cancelHaveNewSemester() {
        getBinding().tvCourseNoticeNewSemester.setVisibility(View.GONE);
    }

    /**
     * 接下来一系列后缀带PopWindow的函数，都是PopWindow点击事件后，可能使用的函数
     * */
    private void checkedChangePopWindow(View view) {
        // 改变设置选项中“只显示本周课程”的勾选状态
        CheckBox showOnlyThisWeekCheckBox = (CheckBox) view.findViewById(R.id.cb_show_all);

        boolean isShowNotThisWeek = SharePreferenceLab.getInstance().getIsShowNotThisWeek(getActivity());
        boolean showOnlyThisWeek = !isShowNotThisWeek;

        // 切换状态
        showOnlyThisWeek = !showOnlyThisWeek;
        showOnlyThisWeekCheckBox.setChecked(showOnlyThisWeek);

        // 底层偏好仍复用原字段：true=显示非本周(灰色)课程；false=隐藏
        SharePreferenceLab.getInstance().setIsShowNotThisWeek(getActivity(), !showOnlyThisWeek);

        // 重新展示课程表
        getPresenter().showSchedule();
    }

    /**
     * 加载SemesterOptionPicker，选择学期
     * */
    private void initSemesterOptionPicker() {
        showSemesterOptionPicker(getPresenter().getRealSemester(),getPresenter().getSelectSemester(),getPresenter().initOptions());
    }
    /**
     * 加载initCampusOptionPicker，选择校区
     * */
    private void initCampusOptionPicker() {
        showCampusOptionPicker(getPresenter().getCampus(),getPresenter().initCampusOptions());
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

    //设置用于隐藏导航栏的回调接口
    public static interface CallBackListener{
         public void setBarState(boolean value);
     }

    //EventBus的使用，不可删除,在修改本周时间确认以后调用(这个是修改真实本周)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveCurrentWeekMessage(CurrentWeekMessage currentWeekMessage){
        int week = currentWeekMessage.getData().getThisWeek();
        //接下来就相当于重新加载了一遍课表，包括Presenter的数据都要刷新
        getPresenter().changeRealWeek(week);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_SCAN:

                if (resultCode == RESULT_OK && data != null) {
                    final String v2PayloadRaw = data.getStringExtra("v2Payload");
                    if (v2PayloadRaw != null && !v2PayloadRaw.isEmpty()) {
                        handleV2CourseImport(v2PayloadRaw);
                        break;
                    }

                    final String studentName = data.getStringExtra(ScanActivity.STUDENT_NAME);
                    final String studentId = data.getStringExtra(ScanActivity.STUDENT_ID);
                    final String token = data.getStringExtra(ScanActivity.TOKEN);
                    final String semester = data.getStringExtra(ScanActivity.SEMESTER);

                    if(SharePreferenceLab.getIsGetQr()){
                        SweetAlertDialog dialog1 = new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE);
                        dialog1.setTitleText("已有情侣课表数据，请先清空数据");
                        dialog1.setCancelText("取消");
                        dialog1.setConfirmText("确定");
                        dialog1.show();
                    }else{
                        SweetAlertDialog dialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.NORMAL_TYPE);
                        dialog.setTitle("是否添加");
                        dialog.setContentText(studentName + '\n' + semester);
                        dialog.setConfirmText("添加");
                        dialog.setCancelText("取消");
                        dialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {

                                sweetAlertDialog.dismiss();
                                SweetAlertDialog dialog1 = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE);
                                dialog1.setTitle("正在加载~");
                                dialog1.show();
                                Log.e(TAG, "onClick: 解密的token == "+token );

                                getPresenter().getRrData(token, semester, dialog1);

                            }
                        });
                        dialog.show();
                    }
                }
                break;
            default:
                break;

        }
    }

    private void handleV2CourseImport(String rawPayload) {
        if (SharePreferenceLab.getIsGetQr()) {
            SweetAlertDialog dialog1 = new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE);
            dialog1.setTitleText("已有情侣课表数据，请先清空数据");
            dialog1.setCancelText("取消");
            dialog1.setConfirmText("确定");
            dialog1.show();
            return;
        }

        CourseShareCodec.DecodeResult decodeResult = CourseShareCodec.decode(rawPayload);
        if (decodeResult.type != CourseShareCodec.DecodeResult.Type.V2 || decodeResult.v2Payload == null) {
            ToastUtil.show("这不是合理的二维码");
            return;
        }

        final CourseShareCodec.V2Payload payload = decodeResult.v2Payload;
        String studentName = payload.student == null ? "未知同学" : payload.student.name;
        String semester = payload.semester == null ? "" : payload.semester;

        SweetAlertDialog dialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.NORMAL_TYPE);
        dialog.setTitle("是否添加");
        dialog.setContentText(studentName + '\n' + semester);
        dialog.setConfirmText("添加");
        dialog.setCancelText("取消");
        dialog.setConfirmClickListener(sweetAlertDialog -> {
            sweetAlertDialog.dismiss();
            CourseDB.deleteQrCourse();
            if (payload.courses != null) {
                for (CourseShareCodec.SharedCourse sharedCourse : payload.courses) {
                    CourseBean courseBean = sharedCourse.toCourseBean();
                    courseBean.setCourseName(courseBean.getCourseName() + "（Ta）");
                    CourseDB.addOneCourse(
                            courseBean,
                            SharePreferenceLab.getInstance().getStudentId(MyApplication.getContext()),
                            semester,
                            CourseBean.TYPE_QR
                    );
                }
            }
            SharePreferenceLab.setIsGetQr(true);
            getPresenter().showDateList();
            getPresenter().showMonthText();
            getPresenter().showWeekText();
            getPresenter().showSchedule();
            getPresenter().refreshVacationState();
            getPresenter().showWeekTabList();
            setCampusTime(getPresenter().getTimeView(getContext()));
            SweetAlertDialog successDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.SUCCESS_TYPE);
            successDialog.setTitle("添加成功！！");
            successDialog.show();
        });
        dialog.show();
    }

    private void getData(final String studentId, String token, final String semester, final String studentName, final SweetAlertDialog dialog) {

        RequestCenter.getCourseData(token, semester,new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String res = response.body().string();
                Log.e(TAG, "onResponse: "+res );
                try {
                    Log.e(TAG, "onResponse:  "+res );
                    final JSONObject jsonObject = new JSONObject(res);
                    final int code = jsonObject.getInt("code");
                    final String message = jsonObject.getString("msg");

                    if (code == 10000) {

                        JSONArray jsonArray = jsonObject.getJSONArray("data");
//                        Log.e(TAG, "onResponse: schedule size: " + jsonArray.length());

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject object = jsonArray.getJSONObject(i);
                            String className = object.getString("className");
                            String classNo = object.getString("classNo");
                            String teacher = object.getString("teacher");
                            String startWeek = object.getString("startWeek");
                            String endWeek = object.getString("endWeek");
                            String classTime = object.getString("section");
                            String classRoom = object.getString("Classroom");
                            String weekday = object.getString("weekDay");

                            int startWeekInt = Integer.parseInt(startWeek);
                            int endWeekInt = Integer.parseInt(endWeek);
                            int classTimeInt = Integer.parseInt(classTime);
                            int weekdayInt = Integer.parseInt(weekday);
                            int color = DrawableLab.getInstance(getActivity()).getDrawableIndex(className);

                            if (weekdayInt == 7){
                                startWeekInt = startWeekInt - 1;
                                endWeekInt =endWeekInt - 1;
                                if (startWeekInt == 0) startWeekInt = 1;
                            }

                            CourseBean courseBean = new CourseBean();
                            courseBean.setCourseName(className);
                            courseBean.setClassNo(classNo);
                            courseBean.setStudentId(SharePreferenceLab.getInstance().getStudentId(MyApplication.getContext()));
                            courseBean.setTeacherName(teacher);
                            courseBean.setClassRoom(classRoom);
                            courseBean.setStartWeek(startWeekInt);
                            courseBean.setEndWeek(endWeekInt);
                            courseBean.setStartTime(classTimeInt);
                            courseBean.setWeekday(weekdayInt);
                            courseBean.setColor(color);

                            CourseDB.addOneCourse(courseBean,SharePreferenceLab.getInstance().getStudentId(MyApplication.getContext()),semester,3);
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                                SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.SUCCESS_TYPE);
                                sweetAlertDialog.setTitle("添加成功！！");
                                sweetAlertDialog.show();
                                //重新展示一下课表
                                getPresenter().showDateList();
                                getPresenter().showMonthText();
                                getPresenter().showWeekText();
                                getPresenter().showSchedule();
                                getPresenter().refreshVacationState();
                                getPresenter().showWeekTabList();
                                setCampusTime(getPresenter().getTimeView(getContext()));
                            }
                        });

                        return;
                    }else if(code == 10001 || code == 30001){
                        Log.e(TAG,"code:" + code);
                        SweetAlertDialog dialog1 = new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE);
                        dialog1.setTitleText("信息过期，请重新生成二维码");
                        dialog1.setConfirmText("确定");
                        dialog1.show();
                        dialog.dismiss();
                        return ;
                    }else if(code == 11000) {
                        SweetAlertDialog dialog1 = new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE);
                        dialog1.setTitleText("教务处错误");
                        dialog1.setConfirmText("确定");
                        dialog1.show();
                        dialog.dismiss();
                        return ;
                    }

                } catch (Exception e) {
                    Log.e(TAG, "onResponse: " + e);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismissWithAnimation();
                            SweetAlertDialog dialog1 = new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE);
                            dialog1.setTitleText("信息过期，请重新生成二维码");
                            dialog1.setCancelText("取消");
                            dialog1.setConfirmText("确定");
                            dialog1.show();
                            return ;
                        }
                    });
                }

            }
        });


    }
}
