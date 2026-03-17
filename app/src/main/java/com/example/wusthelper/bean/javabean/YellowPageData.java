package com.example.wusthelper.bean.javabean;

import android.graphics.drawable.Drawable;

import java.util.List;

public class YellowPageData {

    public static final int TYPE_PARENT = 1;

    public static final int TYPE_CHILD = 2;

    private boolean isExpanded;

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    private int type;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    private Drawable parentIcon;

    private String parentTitle;

    private Drawable childIcon;

    private Drawable parentBackground;

    public Drawable getParentBackground() {
        return parentBackground;
    }

    public void setParentBackground(Drawable parentBackground) {
        this.parentBackground = parentBackground;
    }

    private String departmentName;

    private String telephoneNumber;

    private List<YellowPageData> dataBeanList;

    public List<YellowPageData> getDataBeanList() {
        return dataBeanList;
    }

    public void setDataBeanList(List<YellowPageData> dataBeanList) {
        this.dataBeanList = dataBeanList;
    }

    public Drawable getParentIcon() {
        return parentIcon;
    }

    public void setParentIcon(Drawable parentIcon) {
        this.parentIcon = parentIcon;
    }

    public String getParentTitle() {
        return parentTitle;
    }

    public void setParentTitle(String parentTitle) {
        this.parentTitle = parentTitle;
    }

    public Drawable getChildIcon() {
        return childIcon;
    }

    public void setChildIcon(Drawable childIcon) {
        this.childIcon = childIcon;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getTelephoneNumber() {
        return telephoneNumber;
    }

    public void setTelephoneNumber(String telephoneNumber) {
        this.telephoneNumber = telephoneNumber;
    }

}
