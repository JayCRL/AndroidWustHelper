package com.example.wusthelper.mvp.presenter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.wusthelper.R;
import com.example.wusthelper.MyApplication;
import com.example.wusthelper.base.BasePresenter;
import com.example.wusthelper.bean.javabean.CourseBean;
import com.example.wusthelper.databinding.FragmentCourseNewBinding;
import com.example.wusthelper.dbhelper.CourseDB;
import com.example.wusthelper.helper.TimeTools;
import com.example.wusthelper.bean.itembean.CourseListForShow;
import com.example.wusthelper.bean.itembean.DateItemForShow;
import com.example.wusthelper.bean.itembean.WeekItemForShow;
import com.example.wusthelper.bean.javabean.data.CourseData;
import com.example.wusthelper.helper.SharePreferenceLab;

import com.example.wusthelper.bean.javabean.DateBean;
import com.example.wusthelper.mvp.model.CoursePageModel;
import com.example.wusthelper.mvp.view.CourseFragmentView;
import com.example.wusthelper.request.okhttp.listener.DisposeDataListener;
import com.example.wusthelper.ui.fragment.mainviewpager.CourseFragment;
import com.example.wusthelper.utils.ToastUtil;
import com.example.wusthelper.widget.ExpandableLinearLayout;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class CoursePagePresenter extends BasePresenter<CourseFragmentView> {

    private static final String TAG = "CoursePagePresenter";
    private final CoursePageModel coursePageModel;

    /**
     * 这些变量全是数据，用于Fragment的显示
     * */
    //这个List表是用来显示课表的
    public List<CourseListForShow> mCourseList = new ArrayList<>();
    //这个List表是用来周数列表的
    public List<WeekItemForShow> mWeekList = new ArrayList<>();
    //这个List表是用来日期列表的
    public List<DateItemForShow> mDateList = new ArrayList<>();

    //当前课表的学期
    public String selectSemester;
    //真实的学期
    public String realSemester;
    //当前显示的周数
    public int masterWeek;
    //实际的周数
    public int realWeek;
    //显示的月份(和mDateList日期列表一起加载)
    public String masterMonth;
    //学生的学号
    public String studentId;
    //取得的日期数据的缓存
    public DateBean dateBean ;
    //当前的校区
    public int campus;

    //用于获取解析课表的对象
    public CourseData courseData;

    public CoursePagePresenter() {
        this.coursePageModel = new CoursePageModel();
    };

    public void initPresenterData(){
        Log.d(TAG, "initPresenterData: ");

        selectSemester   = SharePreferenceLab.getSelectSemester();
        if (selectSemester == null || selectSemester.trim().isEmpty()) {
            // 兜底：首次进入或缓存异常时，默认展示“最新学期”
            selectSemester = TimeTools.getLatestSemester();
            SharePreferenceLab.setSelectSemester(selectSemester);
        }

        // 真实学期每次进来都按客户端当前时间刷新，避免依赖后台 config
        realSemester     = TimeTools.getLatestSemester();
        SharePreferenceLab.setSemester(realSemester);

        studentId        = SharePreferenceLab.getStudentId();
        if (studentId == null || studentId.trim().isEmpty()) {
            // 兜底：历史路径可能只写入了 sharedPreferences("Course")
            try {
                String sid = SharePreferenceLab.getInstance().getStudentId(MyApplication.getContext());
                if (sid != null && !sid.trim().isEmpty()) {
                    studentId = sid;
                    SharePreferenceLab.setStudentId(sid);
                }
            } catch (Exception ignore) {
            }
        }

        dateBean         = SharePreferenceLab.getDateBean();

        Log.e(TAG, "initPresenterData: dateBean = " + dateBean);
        Log.e(TAG, "initPresenterData: TimeTools.getFormatToday() = " + TimeTools.getFormatToday());
        realWeek         = TimeTools.getWeek(dateBean, TimeTools.getFormatToday());
        masterWeek       = realWeek;
        campus           = SharePreferenceLab.getCampus();
        masterMonth      = coursePageModel.getMonth(dateBean,masterWeek);

        showWeekTabList();
        checkNewSemester();
//        showDateList();
//        showMonthText();
//        showWeekText();

//        if(TimeTools.getWeekday()==7){
//            masterWeek = masterWeek+1;
//            showWeekText("下一周");
//            ToastUtil.show("已经自动展示下一周的课程");
//        }
    }



    public void showWeekText() {

        if(masterWeek!=realWeek){
            getView().showNotThisWeek();
        }else {
            getView().removeNotThisWeek();
        }
        if(masterWeek == 0){
            showWeekText("假期中");
        }else{
            getView().setWeeksText("第" + masterWeek + "周");
        }
    }

    private void showWeekText(String weekText) {

        if(masterWeek!=realWeek){
            getView().showNotThisWeek();
        }else {
            getView().removeNotThisWeek();
        }
        getView().setWeeksText(weekText);
    }

    /**
     * 检查是否处于假期
     * */
    public void refreshVacationState(){
        int state = SharePreferenceLab.getInstance().get_is_vacationing(MyApplication.getContext());
        Log.e(TAG, "refreshVacationState: 没有进入假期   state===="+state );
        if(state == SharePreferenceLab.VACATIONING){
            Log.e(TAG, "refreshVacationState: 进入假期   state===="+state );
            showWeekText("假期中");
        }
    }

    /**
     * 检查是否有新学期的数据，用于学期更替，有新的学期数据后，提示用户更新课表
     * */
    private void checkNewSemester() {
        int courseSize = coursePageModel.getCourseSizeFormDB(studentId,realSemester);
        Log.d(TAG, "checkNewSemester: "+realSemester);
        Log.d(TAG, "checkNewSemester: "+courseSize);
        if(courseSize<=0 && !selectSemester.equals(realSemester)) {
            //如果最新的学期课程数据没有，提示用户更新课程表或者切换学期
            getView().showHaveNewSemester();
        }else{
            getView().cancelHaveNewSemester();
        }
    }

    /**
     * 网络请求
     * 获取指定的学期的课程表
     * */
    public void refreshScheduleData(String semester) {
        if(semester == null){
            // 刷新按钮进入：强制切到“最新学期”再拉取
            String latest = TimeTools.getLatestSemester();
            SharePreferenceLab.setSemester(latest);
            SharePreferenceLab.setSelectSemester(latest);
            setRealSemester(latest);
            setSelectSemester(latest);
            getCourse(latest);
        }else{
            // 学期选择器明确选择：按用户选择拉取
            getCourse(semester);
        }
    }

    private void getCourse(String selectSemester) {
        if(SharePreferenceLab.getIsGraduate()) {
            coursePageModel.getGraduateCourseFromNet(new DisposeDataListener() {
                @Override
                public void onSuccess(Object responseObj) {
                    courseData = (CourseData) responseObj;
                    Log.e(TAG, "onSuccess: "+courseData);
                    if(courseData.isSuccess()){

                        if(!(courseData.data.size()>0)){
                            //如果获取的课表没有数据，则让用户进行判断
                            getView().cancelDialog();
                            getView().showCommonDialog(SweetAlertDialog.BUTTON_CONFIRM,"确认提醒",
                                    "检测到获取的课表数据为空，是否储存？(会导致课表变成空白)",
                                    "确认储存",
                                    sweetAlertDialog -> saveAndRefreshCourse());
                        }else {
                            //有课程数据，进行覆盖课程数据库
                            //改变当前学期为课程表显示的学期
                            SharePreferenceLab.setSelectSemester(selectSemester);
                            setSelectSemester(selectSemester);
                            saveAndRefreshCourse();
                        }
                        checkNewSemester();
                        getView().cancelDialog();
                        getView().showCommonDialog(SweetAlertDialog.SUCCESS_TYPE,"获取课程表成功 \n",
                                "获取到"+courseData.data.size()+"节课",
                                "确定",null);
                    }else{
                        getView().cancelDialog();
                        getView().showCommonDialog( SweetAlertDialog.ERROR_TYPE,null,courseData.getMsg(),
                                "确认",null);
                    }
                    SharePreferenceLab.setIsRequestCourse(true);

                }

                @Override
                public void onFailure(Object reasonObj) {
                    Log.d(TAG, "onFailure: "+reasonObj.toString());
                    getView().cancelDialog();
                    getView().showCommonDialog( SweetAlertDialog.ERROR_TYPE,"获取课程失败 \n",
                            "请求超时，可能是网络波动或者教务处不稳定！",
                            "确定",null);

                }
            });
        } else {
            coursePageModel.getCourseFromNet(selectSemester,new DisposeDataListener() {
                @Override
                public void onSuccess(Object responseObj) {
                    courseData = (CourseData) responseObj;
                    Log.e(TAG, "onSuccess: "+courseData);
                    if(courseData.isSuccess()){

                        if(!(courseData.data.size()>0)){
                            //如果获取的课表没有数据，则让用户进行判断
                            getView().cancelDialog();
                            getView().showCommonDialog(SweetAlertDialog.BUTTON_CONFIRM,"确认提醒",
                                    "检测到获取的课表数据为空，是否储存？(会导致课表变成空白)",
                                    "确认储存",
                                    sweetAlertDialog -> saveAndRefreshCourse());
                        }else {
                            //有课程数据，进行覆盖课程数据库
                            //改变当前学期为课程表显示的学期
                            SharePreferenceLab.setSelectSemester(selectSemester);
                            setSelectSemester(selectSemester);
                            saveAndRefreshCourse();
                            checkNewSemester();
                            getView().cancelDialog();
                            getView().showCommonDialog(SweetAlertDialog.SUCCESS_TYPE,"获取课程表成功 \n",
                                    "获取到"+courseData.data.size()+"节课",
                                    "确定",null);
                        }
                    }else{
                        getView().cancelDialog();
                        getView().showCommonDialog( SweetAlertDialog.ERROR_TYPE,null,courseData.getMsg(),
                                "确认",null);
                    }
                    //版本更新请求过课表
                    SharePreferenceLab.setIsRequestCourse(true);

                }

                @Override
                public void onFailure(Object reasonObj) {
                    Log.d(TAG, "onFailure: "+reasonObj.toString());
                    getView().cancelDialog();
                    getView().showCommonDialog( SweetAlertDialog.ERROR_TYPE,"获取课程失败 \n",
                            "请求超时，可能是网络波动或者教务处不稳定！",
                            "确定",null);

                }
            });
        }

    }

    public void saveAndRefreshCourse() {
        SharePreferenceLab.setIsGetQr(false);

        //每次请求课程表以后，都要重新储存日期等数据
        coursePageModel.saveCurrentWeek();

        initPresenterData();

        //储存课程数据
        coursePageModel.saveAllCourseToDB(courseData.data,selectSemester,studentId);

        showDateList();
        showMonthText();
        showWeekText();

        //刷新课表
        showSchedule();
    }

    public void showMonthText() {
        masterMonth = coursePageModel.getMonth(dateBean,masterWeek);
        getView().setMonthText(masterMonth);
    }

    /**
     * 更改真实周数
     * */
    public void changeRealWeek(int week) {
        coursePageModel.saveRealWeek(week);
        dateBean         = SharePreferenceLab.getDateBean();
        realWeek         = TimeTools.getWeek(dateBean, TimeTools.getFormatToday());
        changeMasterWeek(realWeek);
    }
    public void changeMasterWeek(int week) {
        masterWeek = week;
        showWeekTabList();
        showDateList();
        showWeekText();
        showMonthText();
        showSchedule();
    }

    /**
     * 显示课程表
     * */
    public void showSchedule() {
        //关键代码：coursePageModel.getCourseShowList(selectSemester)
        if(mCourseList.size()>0){
            mCourseList.clear();
        }
        //

        mCourseList.addAll(coursePageModel.getCourseShowListFormDB(studentId,selectSemester,masterWeek));
        Log.e(TAG,"mCourseList = " + mCourseList);
        getView().showCourseList(mCourseList);

        Log.d(TAG, "showSchedule: ");
        if(masterWeek!=realWeek){
            getView().showFloatingActionButton(View.VISIBLE);
        }else{
            getView().showFloatingActionButton(View.GONE);
        }

    }

    /**
     * 显示日期数据（如 12月1日）
     * */
    public void showDateList(){
        if(mDateList.size()>0) mDateList.clear();
        mDateList.addAll(coursePageModel.getDateListFromDB(dateBean,masterWeek));
        getView().showDataList(mDateList);
    }

    /**
     * 显示周数容器的内容
     * */
    public void showWeekTabList(){
        if(mWeekList.size()>0) mWeekList.clear();
        for(int i=0; i<25; i++){
            mWeekList.add(coursePageModel.getWeekIconListFromDB(studentId,selectSemester,i+1,masterWeek,realWeek));
        }
        getView().showWeekList(mWeekList);
    }


//    public void initBackGround(CourseFragment courseFragment, FragmentCourseNewBinding binding,int statusHeight) {
//
//        final String path= SharePreferenceLab.getInstance().getBackgroundPath(MyApplication.getContext());
//        if(!path.equals("")){
//            //如果path不为空，则证明设置了背景图片，进行加载背景图片的逻辑
//            showFullScreenBackGround(courseFragment,path,binding);
//        }else {
//            //如果path为空，进行加载默认图片的逻辑
//            showFullScreenBackGround(courseFragment,R.drawable.defaultbg,binding);
//        }
//
//        if(!SharePreferenceLab.getIsBackgroundFullScreen()){
//            ViewGroup.LayoutParams layoutParams = binding.ivCourseBackgroundFullScreen.getLayoutParams();
//            ViewGroup.LayoutParams layoutParams1 = new ViewGroup.LayoutParams(binding.tbCourse.getRoot().getLayoutParams());
//            ViewGroup.MarginLayoutParams marginLayoutParams= (ViewGroup.MarginLayoutParams)layoutParams;
//            marginLayoutParams.topMargin =  layoutParams1.height+statusHeight;
//            binding.ivCourseBackgroundFullScreen.setLayoutParams(marginLayoutParams);
//            binding.ivCourseBackgroundWhite.setLayoutParams(marginLayoutParams);
//        }else {
//            ViewGroup.LayoutParams layoutParams = binding.ivCourseBackgroundFullScreen.getLayoutParams();
//            ViewGroup.MarginLayoutParams marginLayoutParams= (ViewGroup.MarginLayoutParams)layoutParams;
//            marginLayoutParams.topMargin =0;
//            binding.ivCourseBackgroundFullScreen.setLayoutParams(marginLayoutParams);
//            binding.ivCourseBackgroundWhite.setLayoutParams(layoutParams);
//        }
//    }

    /**
     * 初始化背景
     * */
    public void initBackGround(CourseFragment courseFragment, FragmentCourseNewBinding binding,int statusHeight) {

        final String path= SharePreferenceLab.getInstance().getBackgroundPath(MyApplication.getContext());
        if(!path.equals("")){
            //如果path不为空，则证明设置了背景图片，进行加载背景图片的逻辑
            if(SharePreferenceLab.getIsBackgroundFullScreen()){
                showFullScreenBackGround(courseFragment,path,binding);
            }else{
                showBackGround(courseFragment,path,binding,statusHeight);
            }

        }else {
            //如果path为空，进行加载默认图片的逻辑
            if(SharePreferenceLab.getIsBackgroundFullScreen()){
                showFullScreenBackGround(courseFragment,R.drawable.defaultbg,binding);
            }else{
                showBackGround(courseFragment,R.drawable.defaultbg,binding,statusHeight);
            }
        }

        int alpha = SharePreferenceLab.getBackgroundAlpha();
        binding.ivCourseBackgroundWhite.setAlpha((float)alpha/100);
    }

    private void showFullScreenBackGround(CourseFragment courseFragment, Object path, FragmentCourseNewBinding binding) {

        ViewGroup.LayoutParams layoutParams = binding.ivCourseBackgroundFullScreen.getLayoutParams();
        binding.ivCourseBackground.setLayoutParams(layoutParams);

        RequestOptions options = new RequestOptions().centerCrop();
        binding.ivCourseBackground.setVisibility(View.GONE);
        binding.ivCourseBackgroundFullScreen.setVisibility(View.VISIBLE);
        Glide.with(courseFragment)
                .applyDefaultRequestOptions(options)
                .load(path)
                .into(binding.ivCourseBackgroundFullScreen);
    }

    private void showBackGround(CourseFragment courseFragment, Object path, FragmentCourseNewBinding binding,int statusHeight) {

        ViewGroup.LayoutParams layoutParams = binding.ivCourseBackground.getLayoutParams();
        ViewGroup.LayoutParams layoutParams1 = new ViewGroup.LayoutParams(binding.tbCourse.getRoot().getLayoutParams());
        ViewGroup.MarginLayoutParams marginLayoutParams= (ViewGroup.MarginLayoutParams)layoutParams;
        marginLayoutParams.topMargin =  layoutParams1.height+statusHeight;
        binding.ivCourseBackground.setLayoutParams(marginLayoutParams);

        RequestOptions options = new RequestOptions().centerCrop();
        binding.ivCourseBackground.setVisibility(View.VISIBLE);
        binding.ivCourseBackgroundFullScreen.setVisibility(View.GONE);
        Glide.with(courseFragment)
                .applyDefaultRequestOptions(options)
                .load(path)
                .into(binding.ivCourseBackground);
    }

    public List<String> initOptions() {

        List<String> results = new ArrayList<>();
        String year = studentId.substring(0, 4);
        Log.d(TAG, "initOptions: ");
        for (int i = 0; i < 12; i++) {

            int startYear = Integer.parseInt(year) + i / 2;
            int endYear = Integer.parseInt(year) + i / 2 + 1;
            results.add(String.valueOf(startYear) + '-' + endYear + '-' + (i % 2 + 1));
        }
        return results;
    }

    public List<String> initCampusOptions() {
        final List<String> options = new ArrayList<>();
        options.add("黄家湖校区");
        options.add("青山校区");
        return options;
    }


    public void setCampus(String campus) {
        if (campus.equals("黄家湖校区"))
        {
            this.campus = SharePreferenceLab.HUANGJIAHU;
            SharePreferenceLab.setCampus(SharePreferenceLab.HUANGJIAHU);
        }else if(campus.equals("青山校区")){
            this.campus = SharePreferenceLab.QINGSHAN;
            SharePreferenceLab.setCampus(SharePreferenceLab.QINGSHAN);
        }
    }


    public View getTimeView(Context context) {
        View view;
        if (campus==SharePreferenceLab.HUANGJIAHU){
            view= LayoutInflater.from(context).inflate(R.layout.layout_course_time_huangjiahu,null,false);
        }
        else {
            view=LayoutInflater.from(context).inflate(R.layout.layout_course_time_qingshan,null,false);
        }
        Log.d(TAG, "getTimeView: "+campus);
        return view;
    }

    public void getRrData(String token,String semester, SweetAlertDialog dialog){
        this.coursePageModel.getQRCourse(token, semester, new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                courseData = (CourseData) responseObj;
                Log.e(TAG, "onSuccess: "+courseData);
                if(courseData.getCode().equals("10000")){

                    if((courseData.data.size()>0)){

                        for(CourseBean courseBean: courseData.data){
                            courseBean.setIsDefault(CourseBean.IS_MYSELF);
                            courseBean.setCourseName(courseBean.getCourseName()+"（Ta）");
                        }
                        CourseDB.addAllCourseData(courseData.data,SharePreferenceLab.getInstance().getStudentId(MyApplication.getContext()),semester,CourseBean.TYPE_QR);
                        dialog.dismiss();
                        getView().showCommonDialog(SweetAlertDialog.SUCCESS_TYPE,"获取课程表成功 \n",
                                "添加成功！！",
                                "确定",null);
                        SharePreferenceLab.setIsGetQr(true);
                        //重新展示一下课表
                        showDateList();
                        showMonthText();
                        showWeekText();
                        showSchedule();
                        refreshVacationState();
                        showWeekTabList();

                    }else {
                        dialog.dismiss();
                        getView().showCommonDialog(SweetAlertDialog.ERROR_TYPE,"无课程数据 \n",
                                "",
                                "确定",null);
                        SharePreferenceLab.setIsGetQr(true);
                        //重新展示一下课表
                        showDateList();
                        showMonthText();
                        showWeekText();
                        showSchedule();
                        refreshVacationState();
                        showWeekTabList();
                    }
                }else if(courseData.getCode().equals("10001") || courseData.getCode().equals("30001")){
                    dialog.dismiss();
                    getView().showCommonDialog( SweetAlertDialog.ERROR_TYPE,"信息过期，请重新生成二维码",courseData.getMsg(),
                            "确认",null);

            }else if(courseData.getCode().equals("1000") ){
                    dialog.dismiss();
                    getView().showCommonDialog( SweetAlertDialog.ERROR_TYPE,"教务处错误",courseData.getMsg(),
                            "确认",null);
            }
            }

            @Override
            public void onFailure(Object reasonObj) {
                dialog.dismiss();
                getView().showCommonDialog( SweetAlertDialog.ERROR_TYPE,"信息过期，请重新生成二维码",courseData.getMsg(),
                        "确认",null);
            }
        });
    }



    /**
     * 一些列的get函数，给Activity提供获取Presenter的方法
     * */
//    public List<CourseListForShow> getCourseList() {
//        return mCourseList;
//    }
//
//    public List<WeekItemForShow> getWeekList() {
//        return mWeekList;
//    }
//
//    public List<DateItemForShow> getDateList() {
//        return mDateList;
//    }

    public String getSelectSemester() {
        return selectSemester;
    }

    public String getRealSemester() {
        return realSemester;
    }

    public int getMasterWeek() {
        return masterWeek;
    }

    public int getRealWeek() {
        return realWeek;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setSelectSemester(String selectSemester) {
        this.selectSemester = selectSemester;
    }

    public void setRealSemester(String realSemester) {
        this.realSemester = realSemester;
    }

    public void setMasterWeek(int masterWeek) {

        this.masterWeek = masterWeek;
    }

    public void setRealWeek(int realWeek) {
        this.realWeek = realWeek;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getMasterMonth() {
        return masterMonth;
    }

    public DateBean getDateBean() {
        return dateBean;
    }

    public int getCampus() {
        return campus;
    }

}
