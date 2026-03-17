package com.example.wusthelper.mvp.presenter;

import com.example.wusthelper.R;
import com.example.wusthelper.base.BasePresenter;
import com.example.wusthelper.bean.javabean.YellowPageData;
import com.example.wusthelper.mvp.view.YellowPageView;
import com.example.wusthelper.utils.ResourcesUtils;

import java.util.ArrayList;
import java.util.List;

public class YellowPagePresenter extends BasePresenter<YellowPageView> {

    private List<YellowPageData> dataBeanList = new ArrayList<>();

    public List<YellowPageData> getDataBeanList() {
        return dataBeanList;
    }

    @Override
    public void initPresenterData() {

    }

    public void initDataBeanList() {

        List<YellowPageData> childDataBeanList = new ArrayList<>();

        YellowPageData childDataBean = new YellowPageData();
        childDataBean.setChildIcon(ResourcesUtils.getRealDrawable(R.drawable.telephone));
        childDataBean.setDepartmentName("保卫处1");
        childDataBean.setTelephoneNumber("027-68893272");
        childDataBean.setType(YellowPageData.TYPE_CHILD);
        childDataBeanList.add(childDataBean);

        YellowPageData childDataBean1 = new YellowPageData();
        childDataBean1.setChildIcon(ResourcesUtils.getRealDrawable(R.drawable.telephone));
        childDataBean1.setDepartmentName("保卫处2");
        childDataBean1.setTelephoneNumber("027-68893392");
        childDataBean1.setType(YellowPageData.TYPE_CHILD);
        childDataBeanList.add(childDataBean1);

        YellowPageData childDataBean2 = new YellowPageData();
        childDataBean2.setChildIcon(ResourcesUtils.getRealDrawable(R.drawable.telephone));
        childDataBean2.setDepartmentName("黄家湖校区综合办公室");
        childDataBean2.setTelephoneNumber("027-68893276");
        childDataBean2.setType(YellowPageData.TYPE_CHILD);
        childDataBeanList.add(childDataBean2);

        YellowPageData childDataBean3 = new YellowPageData();
        childDataBean3.setChildIcon(ResourcesUtils.getRealDrawable(R.drawable.telephone));
        childDataBean3.setDepartmentName("洪山校区综合办公室");
        childDataBean3.setTelephoneNumber("027-51012586");
        childDataBean3.setType(YellowPageData.TYPE_CHILD);
        childDataBeanList.add(childDataBean3);

        YellowPageData childDataBean4 = new YellowPageData();
        childDataBean4.setChildIcon(ResourcesUtils.getRealDrawable(R.drawable.telephone));
        childDataBean4.setDepartmentName("教务处");
        childDataBean4.setTelephoneNumber("027-68862468");
        childDataBean4.setType(YellowPageData.TYPE_CHILD);
        childDataBeanList.add(childDataBean4);

        YellowPageData childDataBean5 = new YellowPageData();
        childDataBean5.setChildIcon(ResourcesUtils.getRealDrawable(R.drawable.telephone));
        childDataBean5.setDepartmentName("后勤集团");
        childDataBean5.setTelephoneNumber("027-68862221");
        childDataBean5.setType(YellowPageData.TYPE_CHILD);
        childDataBeanList.add(childDataBean5);

        YellowPageData childDataBean6 = new YellowPageData();
        childDataBean6.setChildIcon(ResourcesUtils.getRealDrawable(R.drawable.telephone));
        childDataBean6.setDepartmentName("校医院");
        childDataBean6.setTelephoneNumber("027-68893271");
        childDataBean6.setType(YellowPageData.TYPE_CHILD);
        childDataBeanList.add(childDataBean6);

        YellowPageData parentDataBean = new YellowPageData();
        parentDataBean.setParentIcon(ResourcesUtils.getRealDrawable(R.drawable.student_sign));
        parentDataBean.setParentBackground(ResourcesUtils.getRealDrawable(R.drawable.shape_parent_icon_student));
        parentDataBean.setParentTitle("学生常用");
        parentDataBean.setId(1);
        parentDataBean.setDataBeanList(childDataBeanList);
        parentDataBean.setType(YellowPageData.TYPE_PARENT);
        dataBeanList.add(parentDataBean);

        List<YellowPageData> childDataBeanList1 = new ArrayList<>();

        YellowPageData childDataBean7 = new YellowPageData();
        childDataBean7.setChildIcon(ResourcesUtils.getRealDrawable(R.drawable.telephone));
        childDataBean7.setDepartmentName("学校办公室");
        childDataBean7.setTelephoneNumber("027-68862478");
        childDataBean7.setType(YellowPageData.TYPE_CHILD);
        childDataBeanList1.add(childDataBean7);

        YellowPageData childDataBean8 = new YellowPageData();
        childDataBean8.setChildIcon(ResourcesUtils.getRealDrawable(R.drawable.telephone));
        childDataBean8.setDepartmentName("黄家湖校区综合办公室");
        childDataBean8.setTelephoneNumber("027-68893276");
        childDataBean8.setType(YellowPageData.TYPE_CHILD);
        childDataBeanList1.add(childDataBean8);

        YellowPageData childDataBean9 = new YellowPageData();
        childDataBean9.setChildIcon(ResourcesUtils.getRealDrawable(R.drawable.telephone));
        childDataBean9.setDepartmentName("洪山校区综合办公室");
        childDataBean9.setTelephoneNumber("027-51012586");
        childDataBean9.setType(YellowPageData.TYPE_CHILD);
        childDataBeanList1.add(childDataBean9);

        YellowPageData childDataBean10 = new YellowPageData();
        childDataBean10.setChildIcon(ResourcesUtils.getRealDrawable(R.drawable.telephone));
        childDataBean10.setDepartmentName("纪委(监察处)");
        childDataBean10.setTelephoneNumber("027-68862473");
        childDataBean10.setType(YellowPageData.TYPE_CHILD);
        childDataBeanList1.add(childDataBean10);

        YellowPageData childDataBean11 = new YellowPageData();
        childDataBean11.setChildIcon(ResourcesUtils.getRealDrawable(R.drawable.telephone));
        childDataBean11.setDepartmentName("党委组织部(机关党委)");
        childDataBean11.setTelephoneNumber("027-68862793");
        childDataBean11.setType(YellowPageData.TYPE_CHILD);
        childDataBeanList1.add(childDataBean11);

        YellowPageData childDataBean12 = new YellowPageData();
        childDataBean12.setChildIcon(ResourcesUtils.getRealDrawable(R.drawable.telephone));
        childDataBean12.setDepartmentName("党委统战部");
        childDataBean12.setTelephoneNumber("027-68862589");
        childDataBean12.setType(YellowPageData.TYPE_CHILD);
        childDataBeanList1.add(childDataBean12);

        YellowPageData childDataBean13 = new YellowPageData();
        childDataBean13.setChildIcon(ResourcesUtils.getRealDrawable(R.drawable.telephone));
        childDataBean13.setDepartmentName("党委学生工作部(武装部、学生工作室)");
        childDataBean13.setTelephoneNumber("027-68862673");
        childDataBean13.setType(YellowPageData.TYPE_CHILD);
        childDataBeanList1.add(childDataBean13);

        YellowPageData childDataBean14 = new YellowPageData();
        childDataBean14.setChildIcon(ResourcesUtils.getRealDrawable(R.drawable.telephone));
        childDataBean14.setDepartmentName("工会(妇女委员会、教代会)");
        childDataBean14.setTelephoneNumber("027-68863508");
        childDataBean14.setType(YellowPageData.TYPE_CHILD);
        childDataBeanList1.add(childDataBean14);

        YellowPageData childDataBean15 = new YellowPageData();
        childDataBean15.setChildIcon(ResourcesUtils.getRealDrawable(R.drawable.telephone));
        childDataBean15.setDepartmentName("团委");
        childDataBean15.setTelephoneNumber("027-68862339");
        childDataBean15.setType(YellowPageData.TYPE_CHILD);
        childDataBeanList1.add(childDataBean15);

        YellowPageData parentDataBean1 = new YellowPageData();
        parentDataBean1.setParentIcon(ResourcesUtils.getRealDrawable(R.drawable.party_sign));
        parentDataBean1.setParentBackground(ResourcesUtils.getRealDrawable(R.drawable.shape_parent_icon_party));
        parentDataBean1.setParentTitle("党政部门");
        parentDataBean1.setDataBeanList(childDataBeanList1);
        parentDataBean1 .setType(YellowPageData.TYPE_PARENT);
        parentDataBean1.setId(2);
        dataBeanList.add(parentDataBean1);

        List<YellowPageData> childDataBeanList2 = new ArrayList<>();

        YellowPageData childDataBean16 = new YellowPageData();
        childDataBean16.setChildIcon(ResourcesUtils.getRealDrawable(R.drawable.telephone));
        childDataBean16.setDepartmentName("研究生学位与学科建设处");
        childDataBean16.setTelephoneNumber("027-68862026");
        childDataBean16.setType(YellowPageData.TYPE_CHILD);
        childDataBeanList2.add(childDataBean16);

        YellowPageData childDataBean17 = new YellowPageData();
        childDataBean17.setChildIcon(ResourcesUtils.getRealDrawable(R.drawable.telephone));
        childDataBean17.setDepartmentName("研究生培养教育处");
        childDataBean17.setTelephoneNumber("027-68862116");
        childDataBean17.setType(YellowPageData.TYPE_CHILD);
        childDataBeanList2.add(childDataBean17);

        YellowPageData childDataBean18 = new YellowPageData();
        childDataBean18.setChildIcon(ResourcesUtils.getRealDrawable(R.drawable.telephone));
        childDataBean18.setDepartmentName("研究生招生就业处");
        childDataBean18.setTelephoneNumber("027-68862830");
        childDataBean18.setType(YellowPageData.TYPE_CHILD);
        childDataBeanList2.add(childDataBean18);

        YellowPageData childDataBean19 = new YellowPageData();
        childDataBean19.setChildIcon(ResourcesUtils.getRealDrawable(R.drawable.telephone));
        childDataBean19.setDepartmentName("人事处");
        childDataBean19.setTelephoneNumber("027-68862406");
        childDataBean19.setType(YellowPageData.TYPE_CHILD);
        childDataBeanList2.add(childDataBean19);

        YellowPageData childDataBean20 = new YellowPageData();
        childDataBean20.setChildIcon(ResourcesUtils.getRealDrawable(R.drawable.telephone));
        childDataBean20.setDepartmentName("教务处");
        childDataBean20.setTelephoneNumber("027-68862468");
        childDataBean20.setType(YellowPageData.TYPE_CHILD);
        childDataBeanList2.add(childDataBean20);

        YellowPageData childDataBean21 = new YellowPageData();
        childDataBean21.setChildIcon(ResourcesUtils.getRealDrawable(R.drawable.telephone));
        childDataBean21.setDepartmentName("教务处");
        childDataBean21.setTelephoneNumber("027-68862468");
        childDataBean21.setType(YellowPageData.TYPE_CHILD);
        childDataBeanList2.add(childDataBean21);

        YellowPageData childDataBean22 = new YellowPageData();
        childDataBean22.setChildIcon(ResourcesUtils.getRealDrawable(R.drawable.telephone));
        childDataBean22.setDepartmentName("教学质量监控与评估处");
        childDataBean22.setTelephoneNumber("027-68862055");
        childDataBean22.setType(YellowPageData.TYPE_CHILD);
        childDataBeanList2.add(childDataBean22);

        YellowPageData childDataBean23 = new YellowPageData();
        childDataBean23.setChildIcon(ResourcesUtils.getRealDrawable(R.drawable.telephone));
        childDataBean23.setDepartmentName("发展规划处(高等教育研究所)");
        childDataBean23.setTelephoneNumber("027-68862410");
        childDataBean23.setType(YellowPageData.TYPE_CHILD);
        childDataBeanList2.add(childDataBean23);

        YellowPageData childDataBean24 = new YellowPageData();
        childDataBean24.setChildIcon(ResourcesUtils.getRealDrawable(R.drawable.telephone));
        childDataBean24.setDepartmentName("财务处");
        childDataBean24.setTelephoneNumber("027-68862458");
        childDataBean24.setType(YellowPageData.TYPE_CHILD);
        childDataBeanList2.add(childDataBean24);

        YellowPageData childDataBean25 = new YellowPageData();
        childDataBean25.setChildIcon(ResourcesUtils.getRealDrawable(R.drawable.telephone));
        childDataBean25.setDepartmentName("审计处");
        childDataBean25.setTelephoneNumber("027-68862466");
        childDataBean25.setType(YellowPageData.TYPE_CHILD);
        childDataBeanList2.add(childDataBean25);

        YellowPageData childDataBean26 = new YellowPageData();
        childDataBean26.setChildIcon(ResourcesUtils.getRealDrawable(R.drawable.telephone));
        childDataBean26.setDepartmentName("国有资产与实验室管理处");
        childDataBean26.setTelephoneNumber("027-68862205");
        childDataBean26.setType(YellowPageData.TYPE_CHILD);
        childDataBeanList2.add(childDataBean26);

        YellowPageData childDataBean27 = new YellowPageData();
        childDataBean27.setChildIcon(ResourcesUtils.getRealDrawable(R.drawable.telephone));
        childDataBean27.setDepartmentName("基建与后勤管理处");
        childDataBean27.setTelephoneNumber("027-68862819");
        childDataBean27.setType(YellowPageData.TYPE_CHILD);
        childDataBeanList2.add(childDataBean27);

        YellowPageData childDataBean28 = new YellowPageData();
        childDataBean28.setChildIcon(ResourcesUtils.getRealDrawable(R.drawable.telephone));
        childDataBean28.setDepartmentName("国际交流合作处");
        childDataBean28.setTelephoneNumber("027-68862606");
        childDataBean28.setType(YellowPageData.TYPE_CHILD);
        childDataBeanList2.add(childDataBean28);

        YellowPageData childDataBean29 = new YellowPageData();
        childDataBean29.setChildIcon(ResourcesUtils.getRealDrawable(R.drawable.telephone));
        childDataBean29.setDepartmentName("离退休工作处(离退休党委)");
        childDataBean29.setTelephoneNumber("027-68864266");
        childDataBean29.setType(YellowPageData.TYPE_CHILD);
        childDataBeanList2.add(childDataBean29);

        YellowPageData childDataBean30 = new YellowPageData();
        childDataBean30.setChildIcon(ResourcesUtils.getRealDrawable(R.drawable.telephone));
        childDataBean30.setDepartmentName("保卫处(党委保卫部)");
        childDataBean30.setTelephoneNumber("027-68862246");
        childDataBean30.setType(YellowPageData.TYPE_CHILD);
        childDataBeanList2.add(childDataBean30);

        YellowPageData childDataBean31 = new YellowPageData();
        childDataBean31.setChildIcon(ResourcesUtils.getRealDrawable(R.drawable.telephone));
        childDataBean31.setDepartmentName("教职工住宅建设与改革领导小组办公室");
        childDataBean31.setTelephoneNumber("027-68862967");
        childDataBean31.setType(YellowPageData.TYPE_CHILD);
        childDataBeanList2.add(childDataBean31);

        YellowPageData childDataBean32 = new YellowPageData();
        childDataBean32.setChildIcon(ResourcesUtils.getRealDrawable(R.drawable.telephone));
        childDataBean32.setDepartmentName("采购与招标管理办公室");
        childDataBean32.setTelephoneNumber("027-68862385");
        childDataBean32.setType(YellowPageData.TYPE_CHILD);
        childDataBeanList2.add(childDataBean32);

        YellowPageData parentDataBean2 = new YellowPageData();
        parentDataBean2.setParentIcon(ResourcesUtils.getRealDrawable(R.drawable.administration_sign));
        parentDataBean2.setParentBackground(ResourcesUtils.getRealDrawable(R.drawable.shape_parent_icon_administration));
        parentDataBean2.setParentTitle("行政部门");
        parentDataBean2.setDataBeanList(childDataBeanList2);
        parentDataBean2 .setType(YellowPageData.TYPE_PARENT);
        parentDataBean2.setId(3);
        dataBeanList.add(parentDataBean2);

        List<YellowPageData> childDataBeanList3 = new ArrayList<>();

        YellowPageData childDataBean33 = new YellowPageData();
        childDataBean33.setChildIcon(ResourcesUtils.getRealDrawable(R.drawable.telephone));
        childDataBean33.setDepartmentName("工程训练中心");
        childDataBean33.setTelephoneNumber("027-68893669");
        childDataBean33.setType(YellowPageData.TYPE_CHILD);
        childDataBeanList3.add(childDataBean33);

        YellowPageData childDataBean34 = new YellowPageData();
        childDataBean34.setChildIcon(ResourcesUtils.getRealDrawable(R.drawable.telephone));
        childDataBean34.setDepartmentName("现代教育训练中心");
        childDataBean34.setTelephoneNumber("027-68862211");
        childDataBean34.setType(YellowPageData.TYPE_CHILD);
        childDataBeanList3.add(childDataBean34);

        YellowPageData childDataBean35 = new YellowPageData();
        childDataBean35.setChildIcon(ResourcesUtils.getRealDrawable(R.drawable.telephone));
        childDataBean35.setDepartmentName("图书馆");
        childDataBean35.setTelephoneNumber("027-68862220");
        childDataBean35.setType(YellowPageData.TYPE_CHILD);
        childDataBeanList3.add(childDataBean35);

        YellowPageData childDataBean36 = new YellowPageData();
        childDataBean36.setChildIcon(ResourcesUtils.getRealDrawable(R.drawable.telephone));
        childDataBean36.setDepartmentName("档案馆");
        childDataBean36.setTelephoneNumber("027-68862017");
        childDataBean36.setType(YellowPageData.TYPE_CHILD);
        childDataBeanList3.add(childDataBean36);

        YellowPageData childDataBean37 = new YellowPageData();
        childDataBean37.setChildIcon(ResourcesUtils.getRealDrawable(R.drawable.telephone));
        childDataBean37.setDepartmentName("学报编辑部");
        childDataBean37.setTelephoneNumber("027-68862317");
        childDataBean37.setType(YellowPageData.TYPE_CHILD);
        childDataBeanList3.add(childDataBean37);

        YellowPageData childDataBean38 = new YellowPageData();
        childDataBean38.setChildIcon(ResourcesUtils.getRealDrawable(R.drawable.telephone));
        childDataBean38.setDepartmentName("后勤集团");
        childDataBean38.setTelephoneNumber("027-68862221");
        childDataBean38.setType(YellowPageData.TYPE_CHILD);
        childDataBeanList3.add(childDataBean38);

        YellowPageData childDataBean39 = new YellowPageData();
        childDataBean39.setChildIcon(ResourcesUtils.getRealDrawable(R.drawable.telephone));
        childDataBean39.setDepartmentName("资产经营有限公司(科技园有限公司)");
        childDataBean39.setTelephoneNumber("027-68862221");
        childDataBean39.setType(YellowPageData.TYPE_CHILD);
        childDataBeanList3.add(childDataBean39);

        YellowPageData childDataBean40 = new YellowPageData();
        childDataBean40.setChildIcon(ResourcesUtils.getRealDrawable(R.drawable.telephone));
        childDataBean40.setDepartmentName("校医院");
        childDataBean40.setTelephoneNumber("027-68893271");
        childDataBean40.setType(YellowPageData.TYPE_CHILD);
        childDataBeanList3.add(childDataBean40);

        YellowPageData childDataBean41 = new YellowPageData();
        childDataBean41.setChildIcon(ResourcesUtils.getRealDrawable(R.drawable.telephone));
        childDataBean41.setDepartmentName("耐火材料与冶金省部共建国家重点实验室");
        childDataBean41.setTelephoneNumber("027-68862085");
        childDataBean41.setType(YellowPageData.TYPE_CHILD);
        childDataBeanList3.add(childDataBean41);

        YellowPageData childDataBean42 = new YellowPageData();
        childDataBean42.setChildIcon(ResourcesUtils.getRealDrawable(R.drawable.telephone));
        childDataBean42.setDepartmentName("国际钢铁研究院");
        childDataBean42.setTelephoneNumber("027-68862772");
        childDataBean42.setType(YellowPageData.TYPE_CHILD);
        childDataBeanList3.add(childDataBean42);

        YellowPageData childDataBean43 = new YellowPageData();
        childDataBean43.setChildIcon(ResourcesUtils.getRealDrawable(R.drawable.telephone));
        childDataBean43.setDepartmentName("绿色制造与节能减排中心");
        childDataBean43.setTelephoneNumber("027-68862815");
        childDataBean43.setType(YellowPageData.TYPE_CHILD);
        childDataBeanList3.add(childDataBean43);

        YellowPageData childDataBean44 = new YellowPageData();
        childDataBean44.setChildIcon(ResourcesUtils.getRealDrawable(R.drawable.telephone));
        childDataBean44.setDepartmentName("继续教育学院(职业技术学院)");
        childDataBean44.setTelephoneNumber("027-51012585");
        childDataBean44.setType(YellowPageData.TYPE_CHILD);
        childDataBeanList3.add(childDataBean44);

        YellowPageData childDataBean45 = new YellowPageData();
        childDataBean45.setChildIcon(ResourcesUtils.getRealDrawable(R.drawable.telephone));
        childDataBean45.setDepartmentName("附属天佑医院(临床学院)");
        childDataBean45.setTelephoneNumber("027-87896186");
        childDataBean45.setType(YellowPageData.TYPE_CHILD);
        childDataBeanList3.add(childDataBean45);


        YellowPageData parentDataBean3 = new YellowPageData();
        parentDataBean3.setParentIcon(ResourcesUtils.getRealDrawable(R.drawable.direct_sign));
        parentDataBean3.setParentBackground(ResourcesUtils.getRealDrawable(R.drawable.shape_parent_icon_direct));
        parentDataBean3.setParentTitle("直属部门");
        parentDataBean3.setDataBeanList(childDataBeanList3);
        parentDataBean3 .setType(YellowPageData.TYPE_PARENT);
        parentDataBean3.setId(4);
        dataBeanList.add(parentDataBean3);

    }
}
