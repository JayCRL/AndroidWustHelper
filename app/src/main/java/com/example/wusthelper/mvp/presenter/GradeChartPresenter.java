package com.example.wusthelper.mvp.presenter;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.util.Log;

import com.example.wusthelper.base.BasePresenter;
import com.example.wusthelper.helper.SharePreferenceLab;
import com.example.wusthelper.bean.javabean.GradeBean;
import com.example.wusthelper.bean.javabean.TermKV;
import com.example.wusthelper.mvp.model.GradeModel;
import com.example.wusthelper.mvp.view.GradeChartView;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.jsoup.helper.StringUtil.isNumeric;


public class GradeChartPresenter extends BasePresenter<GradeChartView> {

    private static final String TAG = "GradeChartPresenter";

    private GradeModel gradeModel;

    private List<TermKV> mTermKVList = new ArrayList<>();//用于每个学期的条形图的下标判断依据 比如大一上，大二下
    private List<TermKV> mYearKVList = new ArrayList<>();//用于学年的条形图的下标判断依据 比如 大一，大二

    private double mOverallGpa;    //平均绩点
    private double mAverageGrade;  //平均成绩

    public GradeChartPresenter() {
        gradeModel = new GradeModel();
    }

    @Override
    public void initPresenterData() {
        initTermList();
        initYearList();//加载学年条形表----的下标数据（大一，大二）
        initGradeChartGpaData();
        initAverageGradeData();
    }

    private void initGradeChartGpaData() {
        List<GradeBean> beans = LitePal.findAll(GradeBean.class);
        float cj = (float) computeGpaData(beans);
        getView().showGradeChartGpa(mOverallGpa+"");
    }

    private void initAverageGradeData() {
        getView().showGradeChartScore(mAverageGrade+"");
    }

    public void initTermList() {
        List<TermKV> termList = new ArrayList<>();
        List<GradeBean> gradeBeanList = LitePal.order("schoolTerm asc,courseName desc").find(GradeBean.class);
        if (gradeBeanList.size() == 0) {
            return;
        }
        List<String> tempList = new ArrayList<>();
        // 去重算法
        tempList.add(gradeBeanList.get(0).getSchoolTerm());
        for (int i = 0; i < gradeBeanList.size(); ++i) {
            if (!tempList.get(tempList.size() - 1).equals(gradeBeanList.get(i).getSchoolTerm())) {
                tempList.add( gradeBeanList.get(i).getSchoolTerm());
            }
        }
        for(int i=0;i<tempList.size();i++){
            termList.add(new TermKV(tempList.get(i), getShowTermStr(tempList.get(i),i)));
        }
        mTermKVList.clear();
        mTermKVList.addAll(termList);
    }

    public void initYearList() {
        List<TermKV> termList = new ArrayList<>();
        List<GradeBean> gradeBeanList = LitePal.order("schoolTerm asc,courseName desc").find(GradeBean.class);
        if (gradeBeanList.size() == 0) {
            return;
        }
        List<String> tempList = new ArrayList<>();
        // 去重算法   特定计算学年 2018-2019-1和2018-2019-2属于同一学年
        tempList.add(gradeBeanList.get(0).getSchoolTerm());
        for (int i = 0; i < gradeBeanList.size(); ++i) {
            String[] School_Year=tempList.get(tempList.size() - 1).split("-");
            String[] School_Term=gradeBeanList.get(i).getSchoolTerm().split("-");
            if(School_Year.length!=3||School_Term.length!=3){
                Log.d(TAG, "initYearList: "+"ERROR"+"返回的学期值有问题，会导致数组越界");
                break;
            }
            if (!School_Year[0].equals(School_Term[0])){
                tempList.add( gradeBeanList.get(i).getSchoolTerm());
            }
        }

        for(int i=0;i<tempList.size();i++){
            termList.add(new TermKV(tempList.get(i), getShowYearStr(tempList.get(i),i)));
        }

        mYearKVList.clear();
        mYearKVList.addAll(termList);


    }



    public void initPieChart(PieChart pieChartGradeView){
        Description description = new Description();
        description.setEnabled(false);
        pieChartGradeView.setDescription(description);
        //设置半透明圆环的半径,看着就有一种立体的感觉, 0为透明
        pieChartGradeView.setTransparentCircleRadius(0f);

        //设置初始旋转角度
        pieChartGradeView.setRotationAngle(-15);
        //图例
        Legend legend = pieChartGradeView.getLegend();
        legend.setEnabled(false);

        //和四周相隔一段距离,显示数据
        pieChartGradeView.setExtraOffsets(26, 5, 26, 5);

        //设置pieChart图表是否可以手动旋转
        pieChartGradeView.setRotationEnabled(false);
        //设置piecahrt图表点击Item高亮是否可用
        pieChartGradeView.setHighlightPerTapEnabled(true);
        // 设置pieChart图表展示动画效果
        pieChartGradeView.animateY(1400, Easing.EasingOption.EaseInOutQuad);
        //设置pieChart是否只显示饼图上百分比不显示文字
        pieChartGradeView.setDrawEntryLabels(true);
        //是否绘制PieChart内部中心文本
        pieChartGradeView.setDrawCenterText(false);

        pieChartGradeView.setData(getPieData());

    }

    private PieData getPieData() {
        PieDataSet dataSet = new PieDataSet(getPieChartData(),"Label");

        // VORDIPLOM_COLORS
        ArrayList<Integer> colors = new ArrayList<Integer>();
        int[] MATERIAL_COLORS = {
                Color.rgb(200, 172, 255)
        };
        for (int c : MATERIAL_COLORS) {
            colors.add(c);
        }
        for (int c : ColorTemplate.VORDIPLOM_COLORS) {
            colors.add(c);
        }
        dataSet.setColors(colors);
        PieData pieData = new PieData(dataSet);
        //数据连接线距图形片内部边界的距离，为百分数
        dataSet.setValueLinePart1OffsetPercentage(80f);

        //设置连接线的颜色
        dataSet.setValueLineColor(Color.LTGRAY);
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

        //设置饼块之间的间隔
        dataSet.setSliceSpace(1f);
        dataSet.setHighlightEnabled(true);

        pieData.setDrawValues(true);
        pieData.setValueFormatter(new PercentFormatter());
        pieData.setValueTextSize(10f);
        pieData.setValueTextColor(Color.DKGRAY);

        return pieData;
    }


    private List<PieEntry> getPieChartData() {
        List<GradeBean> gradeBeanList = LitePal.order("gradePoint asc").find(GradeBean.class);
        if (gradeBeanList.size() == 0) {
            return null;
        }
        List<String> tempList = new ArrayList<>();
        List<PieEntry> tempPie = new ArrayList<>();

        // 去重算法
        tempList.add(gradeBeanList.get(0).getGradePoint());
        for (int i = 0; i < gradeBeanList.size(); ++i) {
            if (!tempList.get(tempList.size() - 1).equals(gradeBeanList.get(i).getGradePoint())) {
                tempList.add(gradeBeanList.get(i).getGradePoint());
            }
        }

        for (String a : tempList) {
            PieEntry pieEntry = new PieEntry((float) (LitePal.where("gradePoint = ?", a).find(GradeBean.class).size() * 100.0 / gradeBeanList.size()), a);
            pieEntry.setX(Float.parseFloat(a));
            tempPie.add(pieEntry);
            Log.d(TAG, "->Label->" + pieEntry.getLabel() + "-->Value" + pieEntry.getValue() + "-->X" + pieEntry.getX() + "-->Y" + pieEntry.getY());
        }
        return tempPie;
    }


    private String getShowTermStr(String d,int position) {
        String xq = "", nj = "";
        String[] xn = d.split("-");
        if (xn[xn.length - 1].equals("1")) {
            xq = "上";
        } else if (xn[xn.length - 1].equals("2")) {
            xq = "下";
        }
        switch (position) {
            case 0:
            case 1:
                nj = "大一";
                break;
            case 2:
            case 3:
                nj = "大二";
                break;
            case 4:
            case 5:
                nj = "大三";
                break;
            case 6:
            case 7:
                nj = "大四";
                break;
            case 8:
            case 9:
                nj = "大五";
                break;
            case 10:
            case 11:
                nj = "大六";
                break;
            default:
                nj = "大六";
                break;
        }
        return nj + xq;
    }

    private String getShowYearStr(String d,int position) {//通过学期返回学年比如d="2018-2019-1"年，返回"大一"
        String nj = "";
        switch (position) {
            case 0:
                nj = "大一";
                break;
            case 1:
                nj = "大二";
                break;
            case 2:
                nj = "大三";
                break;
            case 3:
                nj = "大四";
                break;
            case 4:
                nj = "大五";
                break;
            case 5:
                nj = "大六";
                break;
            default:
                nj = "大六";
                break;
        }
        return nj ;
    }

    /**
     * @param barChart
     * @param ifYear
     * */
    public void initBarChart(BarChart barChart, int ifYear) {
        /***图表设置***/
        //背景颜色
        barChart.setBackgroundColor(Color.WHITE);
        //不显示图表网格
        barChart.setDrawGridBackground(false);
        //背景阴影
        barChart.setDrawBarShadow(false);
        barChart.setHighlightFullBarEnabled(false);
        //双击不放大
        barChart.setDoubleTapToZoomEnabled(false);
        //禁止缩放操作
        barChart.setScaleEnabled(false);
        //显示边框
        barChart.setDrawBorders(false);
        //设置动画效果
        barChart.animateY(1200);
        //不显示右下角描述
        Description description = new Description();
        description.setEnabled(false);
        barChart.setDescription(description);

        barChart.setExtraOffsets(5, 15, 5, 15);

        /***XY轴的设置***/
        //X轴设置显示位置在底部
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);

        YAxis leftAxis = barChart.getAxisLeft();
        YAxis rightAxis = barChart.getAxisRight();
        //保证Y轴从0开始，不然会上移一点
        leftAxis.setAxisMinimum(0f);
        rightAxis.setAxisMinimum(0f);

        //去掉横线和竖线
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);
        rightAxis.setEnabled(false);

        final ArrayList<String> list = (ArrayList<String>) getTermShowList(ifYear);
        //自定义设置横坐标
        Log.d(TAG, "initBarChart: "+list.toString());
        IAxisValueFormatter xValueFormatter = new IAxisValueFormatter( ) {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return list.get((int) value);
            }
        };
        xAxis.setValueFormatter(xValueFormatter);
        //设置为true当一个页面显示条目过多，X轴值隔一个显示一个
        xAxis.setGranularityEnabled(true);

        /***折线图例 标签 设置***/
        Legend legend = barChart.getLegend();
        legend.setEnabled(false);

    }

    private Object getTermShowList(int ifYear) {
        List<String> list = new ArrayList<>();
        if (ifYear==0){
            for (TermKV kv : mTermKVList) {
                list.add(kv.getShowTerm());
            }
        }else {
            for (TermKV kv : mYearKVList) {
                list.add(kv.getShowTerm());
            }
        }
        return list;
    }

    public void showBarChart(String name, BarChart mBarChart) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        List<Float> cjlist = new ArrayList<>();
        for (int i = 0;i < mTermKVList.size();++i) {
            List<GradeBean> beans = LitePal.where("schoolTerm = ? ", mTermKVList.get(i).getTrueTerm()).find(GradeBean.class);
            float cj = (float) computeGpaData(beans);
            cjlist.add(cj);
            BarEntry barEntry = new BarEntry(i, cj);
            entries.add(barEntry);
        }
        Collections.sort(cjlist);
        int mingpa = (int) Math.floor(cjlist.get(0));
        Log.d(TAG, "--------->min" + mingpa);
        int maxgpa = (int) Math.ceil(cjlist.get(cjlist.size() - 1));
        Log.d(TAG, "--------->max" + maxgpa);

        //只显示绩点附近的纵坐标，距离绩点超过1个单位的不显示
        mBarChart.getAxisLeft().setLabelCount(maxgpa - mingpa + 1,true);
        mBarChart.getAxisLeft().setAxisMinimum(mingpa);
        mBarChart.getAxisLeft().setAxisMaximum(maxgpa);
        // 每一个BarDataSet代表一类柱状图
        BarDataSet barDataSet = new BarDataSet(entries, name);
        initBarDataSet(barDataSet);
        BarData data = new BarData(barDataSet);
        // 间距
        data.setBarWidth(0.55f);
        mBarChart.setData(data);
    }




    public void showAcademicYearBarChart(String name, BarChart mYearBarChart) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        List<Float> cjlist = new ArrayList<>();
        for (int i = 0;i < mYearKVList.size();++i) {
            String mTrueYear_NextTerm;
            String mTrueTerm=mYearKVList.get(i).getTrueTerm();
            String[] TermList=mTrueTerm.split("-");//用于分割学期的String数组
            if(TermList[2].equals("1")){//如果是第一学期，那么下一学期为第二学期
                TermList[2]="2";
                mTrueYear_NextTerm=TermList[0]+"-"+TermList[1]+"-"+TermList[2];
            }else {
                TermList[2]="1";
                mTrueYear_NextTerm=TermList[0]+"-"+TermList[1]+"-"+TermList[2];
            }
            List<GradeBean> beans = LitePal.where("schoolTerm = ? ", mTrueTerm).find(GradeBean.class);
            List<GradeBean> beans_NextTerm=LitePal.where("schoolTerm = ? ",mTrueYear_NextTerm).find(GradeBean.class);
            beans.addAll(beans_NextTerm);
            Log.d(TAG, "showYearBarChart: size="+beans.size()+"***"+beans_NextTerm.size()+"****"+mTrueYear_NextTerm+"***"+mTrueTerm);
            float cj = (float) computeGpaData(beans);
            cjlist.add(cj);
            BarEntry barEntry = new BarEntry(i, cj);
            entries.add(barEntry);
        }
        Collections.sort(cjlist);
        int mingpa = (int) Math.floor(cjlist.get(0));
        Log.d(TAG, "--------->min" + mingpa);
        int maxgpa = (int) Math.ceil(cjlist.get(cjlist.size() - 1));
        Log.d(TAG, "--------->max" + maxgpa);

        //只显示绩点附近的纵坐标，距离绩点超过1个单位的不显示
        mYearBarChart.getAxisLeft().setLabelCount(maxgpa - mingpa + 1,true);
        mYearBarChart.getAxisLeft().setAxisMinimum(mingpa);
        mYearBarChart.getAxisLeft().setAxisMaximum(maxgpa);
        // 每一个BarDataSet代表一类柱状图
        BarDataSet barDataSet = new BarDataSet(entries, name);
        initBarDataSet(barDataSet);
        BarData data = new BarData(barDataSet);
        // 间距
        data.setBarWidth(0.35f);
        mYearBarChart.setData(data);
    }


    private void initBarDataSet(BarDataSet barDataSet) {
        barDataSet.setColors(new int[]{
                Color.rgb(197,255,140), Color.rgb(255,142,156),
                Color.rgb(255, 247, 139), Color.rgb(255, 211, 140),
                Color.rgb(140, 235, 255)});
        barDataSet.setFormLineWidth(1f);
        barDataSet.setFormSize(15.f);

        //显示柱状图顶部值
        barDataSet.setDrawValues(true);
        barDataSet.setValueTextSize(12f);
        //点击高亮颜色加深30
        barDataSet.setHighLightAlpha(30);
    }


    /**
     * 计算平均绩点GPA
     * 平均绩点=∑学分*绩点　÷　∑学分
     * @return
     */
    public double computeGpaData(List<GradeBean> mGradeBeanList){//必须对计算平均绩点的公式非常熟练才能看懂，希望以后可以优化
        List<GradeBean> mGpaDataList= new ArrayList<>();//用于储存一次计算学分所用到的课程名，防止重复
        // 学分×绩点和，学分总和
        double xfcjdh=0,xfzh=0;
        // 学分×成绩和
        double xfccjh=0;
        if (mGradeBeanList.size() < 1){
            return 0;
        } else {
            //初始化计算学分的列表，主要是去重
            mGpaDataList=initMlistName(mGradeBeanList);
            //计算，学分×绩点和学分总和，用于计算平均绩点GPA
            for (int i = 0;i < mGpaDataList.size();++i){
                xfzh+=Double.parseDouble( mGpaDataList.get(i).getCourseCredit() );
                xfcjdh+=Double.parseDouble( mGpaDataList.get(i).getGradePoint() )*Double.parseDouble( mGpaDataList.get(i).getCourseCredit() );
                xfccjh+=addGrade(mGpaDataList.get( i ));

            }

            if (xfzh==0){
                return 0;
            }
            mOverallGpa = xfcjdh/xfzh;
            Log.d(TAG, "computeGpaData: +学分="+xfzh+"分子="+xfcjdh);
            @SuppressLint("DefaultLocale") String a = String.format("%.3f", mOverallGpa);
            mOverallGpa = Double.parseDouble(a);
            mAverageGrade = xfccjh/xfzh;
            @SuppressLint("DefaultLocale") String b = String.format("%.3f", mAverageGrade);
            mAverageGrade = Double.parseDouble(b);
            Log.d("------->", "------>" + Double.parseDouble(b));
            return Double.parseDouble(a);
        }
    }

    private double addGrade(GradeBean gradeBean) {
        //计算，学分×成绩和，用于计算平均成绩GPA，比如平均95分
        switch (gradeBean.getGrade( ).trim()) {
            case "A":
                return 95 * Double.parseDouble( gradeBean.getCourseCredit() );

            case "A-":
                return 87 * Double.parseDouble( gradeBean.getCourseCredit( ) );

            case "B+":
                return 83 * Double.parseDouble( gradeBean.getCourseCredit( ) );

            case "B":
                return 79.5 * Double.parseDouble( gradeBean.getCourseCredit( ) );

            case "B-":
                return 76 * Double.parseDouble( gradeBean.getCourseCredit( ) );

            case "C+":
                return 73 * Double.parseDouble( gradeBean.getCourseCredit( ) );

            case "C":
                return 69.5 * Double.parseDouble( gradeBean.getCourseCredit( ) );

            case "C-":
                return 65.5 * Double.parseDouble( gradeBean.getCourseCredit( ) );

            case "D":
                return 61.5 * Double.parseDouble( gradeBean.getCourseCredit( ) );

            case "F":
                return 30 * Double.parseDouble( gradeBean.getCourseCredit( ) );

            default:
                return Double.parseDouble( gradeBean.getGrade( ) ) * Double.parseDouble( gradeBean.getCourseCredit( ) );

        }
    }

    private List<GradeBean> initMlistName(List<GradeBean> mGradeBeanList) {
        //去除重复的科目，因为数据库查找的列表有包含同样名字的科目（主要是挂科）
        //一般逻辑是，如果有多个名字一样的科目，选择最高成绩进行覆盖，只计算一个
        List<GradeBean> mGpaDataList = new ArrayList<>();
        for (GradeBean gradeBean : mGradeBeanList){
            if(mGpaDataList.size()==0){
                mGpaDataList.add(gradeBean);
                continue;
            }
            int i = 0;
            for(;i<mGpaDataList.size();i++){
                if(!mGpaDataList.get(i).getCourseName().equals(gradeBean.getCourseName())){
                    //如果名字不相同则添加

                }else{
                    //名字相同，则与原数据进行比较，然后计算
                    if(compareGrade(gradeBean.getGrade(),mGpaDataList.get(i).getGrade())){
                        //如果分数大于则覆盖
                        mGpaDataList.set(i, gradeBean);
                    }   //小于则不覆盖
                    break;
                }
            }
            if(i==mGpaDataList.size()){
                //如果查找完成没有重复就添加
                mGpaDataList.add(gradeBean);
            }
        }
        return mGpaDataList;
    }

    private boolean compareGrade(String zcj, String zcj1) {
        try{
            if(isNumeric(zcj)){
                if(Double.parseDouble( zcj )>Double.parseDouble( zcj1 ))
                    return true;
                else
                    return false;
            }else{
                //如果成绩是字母型的直接覆盖，因为最低的只有F
                return true;
            }
        }catch (Exception e){
            return true;
        }
    }

}
